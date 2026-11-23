package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;
import zone.moddev.mc.ironagefurniture.client.gui.GuiFurnitureLabel;
import zone.moddev.mc.ironagefurniture.client.gui.ShelfLabelInputProbe;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
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

@Mod.EventBusSubscriber(Side.CLIENT)
public final class ShelfLabelClientProbe {
	private static final String LABEL = "Boat fittings";
	private static BlockPos pos;
	private static ItemStack sign;
	private static Future<?> prepared;
	private static int ticks;
	private static int step;

	private ShelfLabelClientProbe() { }

	static void start(Minecraft client) {
		pos = client.player.getPosition().east(2);
		sign = new ItemStack(Items.SIGN, 2).setStackDisplayName("Shelf signs");
		client.playerController.setGameType(GameType.SURVIVAL);

		prepared = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
			WorldServer world = player.getServerWorld();
			Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
			world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
			world.setBlockState(pos, shelf.getDefaultState().withProperty(RackBlock.STANDING, true)
				.withProperty(RackBlock.DATA, true));
			ShelfLevel level = ((TileEntityShelvingUnit)world.getTileEntity(pos)).getLevel(true);
			level.addMedia(new ItemStack(Items.BOAT));
			level.markSurfaceSettingChanged();

			player.interactionManager.setGameType(GameType.SURVIVAL);
			player.inventory.clear();
			player.setHeldItem(EnumHand.MAIN_HAND, sign.copy());
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
			require(++ticks < 400, "Label probe stalled at step " + step);
			if (!prepared.isDone()) {
				return;
			}
			prepared.get();

			ItemStack held = client.player.getHeldItemMainhand();
			GuiFurnitureLabel editor = client.currentScreen instanceof GuiFurnitureLabel ? (GuiFurnitureLabel)client.currentScreen : null;
			TileEntity tile = client.world.getTileEntity(pos);
			ShelfLevel level = tile instanceof TileEntityShelvingUnit ? ((TileEntityShelvingUnit)tile).getLevel(true) : null;

			switch (step) {
			case 0:
				if (!ItemStack.areItemStacksEqual(held, sign) || level == null || level.getMediaCount() != 1) return;
				click(client, held, 0.65D, 0.25D);
				break;
			case 1:
				if (editor == null) return;
				ShelfLabelInputProbe.type(editor, "Cancel this", false);
				break;
			case 2:
				if (client.currentScreen != null) return;
				require(ItemStack.areItemStacksEqual(held, sign) && level.getLabelSign() == null, "Cancel placed a sign");
				click(client, held, 0.65D, 0.25D);
				break;
			case 3:
				if (editor == null) return;
				ShelfLabelInputProbe.type(editor, LABEL, true);
				break;
			case 4:
				ItemStack expectedSign = sign.copy();
				expectedSign.stackSize = 1;
				if (client.currentScreen != null || !LABEL.equals(level.getLabelText())
						|| !ItemStack.areItemStacksEqual(held, expectedSign)) return;
				require(ItemStack.areItemStacksEqual(level.getLabelSign(), expectedSign)
					&& level.getMediaItem(0).getItem() == Items.BOAT, "Sign did not preserve the displayed boat and its own item data");
				client.player.inventory.currentItem = (client.player.inventory.currentItem + 1) % 9;
				break;
			case 5:
				if (client.player.getHeldItemMainhand() != null) return;
				click(client, null, 0.52D, 0.21875D);
				break;
			case 6:
				if (editor == null) return;
				ShelfLabelInputProbe.type(editor, " 2", true);
				break;
			case 7:
				if (client.currentScreen != null || !(LABEL + " 2").equals(level.getLabelText())) return;
				pos = null;
				LogManager.getLogger().info("IAF SHELF LABEL NETWORK PASSED: native editor, cancel, exact consumption, sync and empty-hand editing");
				VanillaFoodNetworkClientProbe.complete(client);
				return;
			default:
				throw new IllegalStateException("Unexpected shelf label step " + step);
			}
			step++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static void click(Minecraft client, ItemStack held, double height, double depth) {
		require(client.playerController.processRightClickBlock(client.player, client.world, held, pos, EnumFacing.NORTH,
			new Vec3d(pos).addVector(0.5D, height, depth), EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS,
			"Shelf label click failed at " + pos);
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
