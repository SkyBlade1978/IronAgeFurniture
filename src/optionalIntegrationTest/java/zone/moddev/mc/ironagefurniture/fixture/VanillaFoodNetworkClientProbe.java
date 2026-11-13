package zone.moddev.mc.ironagefurniture.fixture;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Future;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;

/** Real client packets prove placement, no simultaneous eating, and synchronised pickup. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class VanillaFoodNetworkClientProbe {
	private static BlockPos support;
	private static Future<?> pending;
	private static int step;
	private static int ticks;
	private static int cases;
	private static final String NAME = "Network food portion";

	private VanillaFoodNetworkClientProbe() { }

	static void start(Minecraft client, int verifiedCases) {
		client.gameSettings.pauseOnLostFocus = false;
		client.displayGuiScreen(null);
		cases = verifiedCases;
		support = client.player.getPosition().east(2);
		client.player.inventory.currentItem = 0;
		client.playerController.setGameType(GameType.SURVIVAL);
		pending = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			WorldServer world = player.getServerWorld();
			world.setBlockState(support, Blocks.PLANKS.getDefaultState());
			world.setBlockToAir(support.up());
			player.interactionManager.setGameType(GameType.SURVIVAL);
			player.inventory.clear();
			player.inventory.currentItem = 0;
			player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BREAD, 3).setStackDisplayName(NAME));
			player.getFoodStats().setFoodLevel(10);
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (support == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();
		try {
			if (++ticks > 200) throw new IllegalStateException("Food packet test timed out at step " + step
				+ "; hand=" + client.player.getHeldItemMainhand() + "; slot1=" + client.player.inventory.getStackInSlot(1)
				+ "; display=" + client.world.getBlockState(support.up()));
			if (pending != null) {
				if (!pending.isDone()) return;
				pending.get(); pending = null;
			}
			if (step == 0) {
				if (client.world.getBlockState(support).getBlock() != Blocks.PLANKS
						|| !isBread(client.player.getHeldItemMainhand(), 3)) return;
				setSneaking(client, true);
				ItemStack held = client.player.getHeldItemMainhand();
				client.playerController.processRightClickBlock(client.player, client.world, held,
					support, EnumFacing.UP, new Vec3d(support).addVector(0.5D, 1.0D, 0.5D), EnumHand.MAIN_HAND);
				// This is Minecraft 1.10's immediate fall-through after the cancelled block click.
				client.playerController.processRightClick(client.player, client.world, held, EnumHand.MAIN_HAND);
				require(!client.player.isHandActive(), "Food placement also started client eating");
				step = 1;
			} else if (step == 1) {
				TileEntity tile = client.world.getTileEntity(support.up());
				if (!(tile instanceof SurfaceSettingHost) || !isBread(client.player.getHeldItemMainhand(), 2)) return;
				require(isBread(((SurfaceSettingHost)tile).getSurfaceSetting().getCompatibilityItem(), 1), "Client food NBT did not sync");
				setSneaking(client, false);
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = serverPlayer(client);
					require(!player.isHandActive(), "Food placement also started server eating");
					require(isBread(player.getHeldItemMainhand(), 2), "Server did not consume exactly one food");
					player.inventory.setInventorySlotContents(1, player.getHeldItemMainhand());
					player.setHeldItem(EnumHand.MAIN_HAND, null);
					player.sendContainerToPlayer(player.inventoryContainer);
				});
				step = 2;
			} else if (step == 2) {
				if (client.player.getHeldItemMainhand() != null || !isBread(client.player.inventory.getStackInSlot(1), 2)) return;
				client.playerController.processRightClickBlock(client.player, client.world, null,
					support.up(), EnumFacing.UP, new Vec3d(support).addVector(0.5D, 1.1D, 0.5D), EnumHand.MAIN_HAND);
				step = 3;
			} else if (step == 3) {
				if (!client.world.isAirBlock(support.up()) || !isBread(client.player.inventory.getStackInSlot(1), 3)) return;
				require(client.world.getBlockState(support).getBlock() == Blocks.PLANKS, "Network pickup removed vanilla support");
				require(client.player.getHeldItemMainhand() == null, "Network pickup duplicated food into the empty hand");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = serverPlayer(client);
					require(player.getServerWorld().isAirBlock(support.up()) && player.getHeldItemMainhand() == null
						&& isBread(player.inventory.getStackInSlot(1), 3),
						"Server pickup diverged from client");
					player.interactionManager.setGameType(GameType.CREATIVE);
					player.getFoodStats().setFoodLevel(20);
				});
				step = 4;
			} else {
				client.playerController.setGameType(GameType.CREATIVE);
				support = null;
				LogManager.getLogger().info("IAF VANILLA FOOD NETWORK CLIENT PROBE PASSED");
				Files.write(Paths.get("surface-bounds-pass.properties"),
					("status=PASS\ncases=" + (cases + 1) + "\nnetworkFood=PASS\n").getBytes(StandardCharsets.UTF_8));
				LogManager.getLogger().info("IAF SURFACE BOUNDS CLIENT PROBE PASSED: {} cases", cases + 1);
				VanillaFoodClientProbe.prepareGallery(client);
			}
		} catch (Exception failure) {
			setSneaking(client, false);
			client.shutdown();
			throw failure;
		}
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static boolean isBread(ItemStack stack, int count) {
		return stack != null && stack.getItem() == Items.BREAD && stack.stackSize == count && NAME.equals(stack.getDisplayName());
	}

	private static void setSneaking(Minecraft client, boolean sneaking) {
		KeyBinding.setKeyBindState(client.gameSettings.keyBindSneak.getKeyCode(), sneaking);
		client.player.movementInput.sneak = sneaking;
		client.player.setSneaking(sneaking);
		client.player.connection.sendPacket(new CPacketEntityAction(client.player,
			sneaking ? CPacketEntityAction.Action.START_SNEAKING : CPacketEntityAction.Action.STOP_SNEAKING));
	}

	private static void require(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
}
