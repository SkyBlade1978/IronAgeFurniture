package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBulkBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.ironagefurniture.client.gui.GuiBulkBarrel;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class BulkBarrelClientProbe {
	private static BlockPos pos;
	private static Future<?> pending;
	private static int step;
	private static int ticks;

	private BulkBarrelClientProbe() { }

	static void start(Minecraft client) {
		pos = client.player.getPosition().north(2);
		client.player.inventory.currentItem = 0;
		client.playerController.setGameType(GameType.SURVIVAL);
		pending = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			Block barrel = Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak");
			player.getServerWorld().setBlockState(pos, barrel.getDefaultState());
			player.inventory.clear();
			player.inventory.currentItem = 0;
			player.interactionManager.setGameType(GameType.SURVIVAL);
			player.setHeldItem(EnumHand.MAIN_HAND, potatoes(64));
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (pos == null || event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		try {
			require(++ticks < 200, "Bulk barrel packet test timed out at step " + step);
			if (pending != null) {
				if (!pending.isDone()) {
					return;
				}
				pending.get();
				pending = null;
			}

			TileEntity found = client.world.getTileEntity(pos);
			if (!(found instanceof TileEntityBarrel)) {
				return;
			}

			TileEntityBarrel tile = (TileEntityBarrel)found;
			int count = tile.getBulkStorage().getCount();
			if (step == 0 && count == 0 && ItemStack.areItemStacksEqual(client.player.getHeldItemMainhand(), potatoes(64))) {
				require(!open(client), "Empty barrel rendered open");
				click(client);
				step = 1;
			} else if (step == 1 && count == 64 && client.player.getHeldItemMainhand() == null) {
				require(open(client), "Solid barrel did not render as open on the client");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = serverPlayer(client);
					player.setHeldItem(EnumHand.MAIN_HAND, potatoes(5));
					player.sendContainerToPlayer(player.inventoryContainer);
				});
				step = 2;
			} else if (step == 2 && ItemStack.areItemStacksEqual(client.player.getHeldItemMainhand(), potatoes(5))) {
				click(client);
				step = 3;
			} else if (step == 3 && count == 69 && client.player.getHeldItemMainhand() == null) {
				click(client);
				step = 4;
			} else if (step == 4 && menuShows(client, 69)) {
				VanillaFoodClientProbe.screenshot(client, "phase5-bulk-barrel-gui.png");
				take(client, ContainerBulkBarrel.TAKE_ONE);
				step = 5;
			} else if (step == 5 && count == 68 && menuShows(client, 68)
					&& ItemStack.areItemStacksEqual(client.player.inventory.getStackInSlot(0), potatoes(1))) {
				take(client, ContainerBulkBarrel.TAKE_STACK);
				step = 6;
			} else if (step == 6 && count == 4 && menuShows(client, 4)
					&& ItemStack.areItemStacksEqual(client.player.inventory.getStackInSlot(0), potatoes(64))
					&& ItemStack.areItemStacksEqual(client.player.inventory.getStackInSlot(1), potatoes(1))) {
				take(client, ContainerBulkBarrel.TAKE_STACK);
				step = 7;
			} else if (step == 7 && count == 0 && client.currentScreen == null
					&& ItemStack.areItemStacksEqual(client.player.inventory.getStackInSlot(1), potatoes(5))) {
				require(!open(client), "The lid did not shut after the last client withdrawal");
				LogManager.getLogger().info("IAF BULK BARREL NETWORK PASSED: top deposit, count, exact withdrawals and closing lid");
				BarrelLabelClientProbe.start(client, pos);
				pos = null;
			}
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static boolean open(Minecraft client) {
		IBlockState state = client.world.getBlockState(pos);
		return state.getBlock().getActualState(state, client.world, pos).getValue(StorageBarrel.OPEN);
	}

	private static void click(Minecraft client) {
		EnumActionResult result = client.playerController.processRightClickBlock(client.player, client.world,
			client.player.getHeldItemMainhand(), pos, EnumFacing.UP,
			new Vec3d(pos).addVector(0.5D, 1, 0.5D), EnumHand.MAIN_HAND);
		require(result == EnumActionResult.SUCCESS, "Bulk barrel click fell through to item use");
	}

	private static boolean menuShows(Minecraft client, int count) {
		if (!(client.currentScreen instanceof GuiBulkBarrel) || !(client.player.openContainer instanceof ContainerBulkBarrel)) {
			return false;
		}

		ContainerBulkBarrel menu = (ContainerBulkBarrel)client.player.openContainer;
		return menu.getCount() == count && menu.getCapacity() == 128
			&& ItemStack.areItemStacksEqual(menu.getSlot(0).getStack(), potatoes(1));
	}

	private static void take(Minecraft client, int action) {
		client.playerController.sendEnchantPacket(client.player.openContainer.windowId, action);
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static ItemStack potatoes(int count) {
		return new ItemStack(Items.POTATO, count).setStackDisplayName("Market potatoes");
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
