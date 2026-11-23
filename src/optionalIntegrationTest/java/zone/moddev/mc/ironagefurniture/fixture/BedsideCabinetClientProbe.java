package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.BedsideCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBedsideCabinet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class BedsideCabinetClientProbe {
	private static final EnumFacing FRONT = EnumFacing.SOUTH;
	private static final String UPPER_TITLE = "container.ironagefurniture.upper_drawer";
	private static final String LOWER_TITLE = "container.ironagefurniture.lower_drawer";
	private static BlockPos pos;
	private static Future<?> prepared;
	private static int step;
	private static int ticks;

	private BedsideCabinetClientProbe() { }

	static void start(Minecraft client, BlockPos cabinetPos) {
		pos = cabinetPos;
		prepared = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			WorldServer world = player.getServerWorld();
			Block block = Block.getBlockFromName("ironagefurniture:bedside_cabinet_wood_ironage_oak");
			world.setBlockState(cabinetPos, block.getDefaultState().withProperty(BedsideCabinet.FACING, FRONT), 3);
			TileEntityBedsideCabinet cabinet = (TileEntityBedsideCabinet)world.getTileEntity(cabinetPos);
			cabinet.getDrawer(true).setInventorySlotContents(0, book());
			cabinet.getDrawer(false).setInventorySlotContents(0, vase());
			player.inventory.clear();
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
			require(++ticks < 240, "Bedside cabinet client stalled at step " + step);
			if (!prepared.isDone()) return;
			prepared.get();
			TileEntity tile = client.world.getTileEntity(pos);
			if (!(tile instanceof TileEntityBedsideCabinet) || client.player.getHeldItemMainhand() != null) return;
			TileEntityBedsideCabinet cabinet = (TileEntityBedsideCabinet)tile;
			ContainerChest screen = client.currentScreen instanceof GuiChest
				? (ContainerChest)client.player.openContainer : null;

			switch (step) {
			case 0:
				click(client, 0.53D);
				break;
			case 1:
				if (screen == null || !cabinet.isDrawerOpen(true) || screen.getSlot(0).getStack() == null) return;
				require(!cabinet.isDrawerOpen(false) && screen.inventorySlots.size() == 45
					&& I18n.format(UPPER_TITLE).equals(screen.getLowerChestInventory().getDisplayName().getUnformattedText())
					&& ItemStack.areItemStacksEqual(book(), screen.getSlot(0).getStack()), "Wrong upper drawer contents or title");
				client.playerController.windowClick(screen.windowId, 0, 0, ClickType.QUICK_MOVE, client.player);
				break;
			case 2:
				if (screen == null || screen.getSlot(0).getStack() != null) return;
				require(client.player.inventory.hasItemStack(book()), "Shift-click did not move the upper drawer book");
				client.player.closeScreen();
				break;
			case 3:
				if (client.currentScreen != null || cabinet.isDrawerOpen(true)) return;
				click(client, 0.28D);
				break;
			case 4:
				if (screen == null || !cabinet.isDrawerOpen(false) || screen.getSlot(0).getStack() == null) return;
				require(!cabinet.isDrawerOpen(true)
					&& I18n.format(LOWER_TITLE).equals(screen.getLowerChestInventory().getDisplayName().getUnformattedText())
					&& ItemStack.areItemStacksEqual(vase(), screen.getSlot(0).getStack()),
					"Opening or shift-clicking the upper drawer changed the lower drawer");
				client.player.closeScreen();
				break;
			case 5:
				if (client.currentScreen != null || cabinet.isDrawerOpen(false)) return;
				CrateClientProbe.start(client, pos.south());
				pos = null;
				LogManager.getLogger().info("IAF BEDSIDE CABINET NETWORK PASSED: separate titles and contents, shift-click and open state sync");
				return;
			default:
				throw new IllegalStateException("Unexpected bedside cabinet step " + step);
			}

			step++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static void click(Minecraft client, double height) {
		Vec3d click = new Vec3d(pos).addVector(0.5D, height, 14.5D / 16);
		IBlockState state = client.world.getBlockState(pos);
		require(state.getValue(BedsideCabinet.FACING) == FRONT, "Cabinet facing did not sync");
		require(client.playerController.processRightClickBlock(client.player, client.world, null, pos, FRONT, click,
			EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS, "Cabinet interaction did not send a click");
	}

	private static ItemStack book() {
		return new ItemStack(Items.BOOK, 2).setStackDisplayName("Bedside reading");
	}

	private static ItemStack vase() {
		return new ItemStack(Items.FLOWER_POT).setStackDisplayName("Bedside flower pot");
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
