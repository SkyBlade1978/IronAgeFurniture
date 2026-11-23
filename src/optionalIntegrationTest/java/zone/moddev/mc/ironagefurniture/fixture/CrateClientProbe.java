package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageCrate;
import zone.moddev.mc.ironagefurniture.api.container.ContainerCrate;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;
import zone.moddev.mc.ironagefurniture.client.gui.GuiCrate;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
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
public final class CrateClientProbe {
	private static BlockPos pos;
	private static Future<?> prepared;
	private static int step;
	private static int ticks;

	private CrateClientProbe() { }

	static void start(Minecraft client, BlockPos cratePos) {
		pos = cratePos;
		prepared = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			WorldServer world = player.getServerWorld();
			Block block = Block.getBlockFromName("ironagefurniture:crate_wood_ironage_spruce");
			world.setBlockState(cratePos, block.getDefaultState(), 3);
			TileEntityCrate crate = (TileEntityCrate)world.getTileEntity(cratePos);
			crate.getInventory().setInventorySlotContents(15, apples());
			crate.writeLabel(new ItemStack(Items.SIGN), "Orchard apples");
			player.inventory.clear();
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (pos == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();
		try {
			require(++ticks < 220, "Crate client stalled at step " + step);
			if (!prepared.isDone()) return;
			prepared.get();

			TileEntity tile = client.world.getTileEntity(pos);
			if (!(tile instanceof TileEntityCrate) || client.player.getHeldItemMainhand() != null) return;
			TileEntityCrate crate = (TileEntityCrate)tile;
			ContainerCrate open = client.currentScreen instanceof GuiCrate ? (ContainerCrate)client.player.openContainer : null;
			switch (step) {
			case 0:
				click(client, 0.62D);
				break;
			case 1:
				if (open == null || open.getSlot(15).getStack() == null) return;
				require(open.inventorySlots.size() == 52 && ItemStack.areItemStacksEqual(apples(), open.getSlot(15).getStack())
					&& "Orchard apples".equals(open.getTitle()), "Wrong grid, label or transmitted stack");
				VanillaFoodClientProbe.screenshot(client, "phase5-crate-inventory.png");
				client.playerController.windowClick(open.windowId, 15, 0, ClickType.QUICK_MOVE, client.player);
				break;
			case 2:
				if (open == null || open.getSlot(15).getStack() != null) return;
				require(client.player.inventory.hasItemStack(apples()), "Crate shift-click lost the stack");
				client.player.closeScreen();
				break;
			case 3:
				if (client.currentScreen != null) return;
				toggle(client);
				break;
			case 4:
				if (!crate.isSealed()) return;
				require(client.currentScreen == null, "The latch opened a menu instead of sealing");
				toggle(client);
				break;
			case 5:
				if (crate.isSealed()) return;
				StorageHopperClientProbe.start(client, pos.east(2));
				pos = null;
				LogManager.getLogger().info("IAF CRATE NETWORK PASSED: native 4x4 menu, shift-click, label, seal and unseal");
				return;
			default:
				throw new IllegalStateException("Unexpected crate client step " + step);
			}
			step++;
		} catch (Exception exception) {
			client.shutdown();
			throw exception;
		}
	}

	private static void toggle(Minecraft client) {
		client.player.connection.sendPacket(new CPacketEntityAction(client.player, CPacketEntityAction.Action.START_SNEAKING));
		click(client, 0.82D);
		client.player.connection.sendPacket(new CPacketEntityAction(client.player, CPacketEntityAction.Action.STOP_SNEAKING));
	}

	private static void click(Minecraft client, double height) {
		Vec3d point = new Vec3d(pos).addVector(0.5D, height, 15.0D / 16);
		require(client.world.getBlockState(pos).getValue(StorageCrate.FACING) == EnumFacing.SOUTH, "Crate facing not synced");
		require(client.playerController.processRightClickBlock(client.player, client.world, null, pos, EnumFacing.SOUTH,
			point, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS, "Crate click did not send an interaction");
	}

	private static ItemStack apples() {
		return new ItemStack(Items.APPLE, 7).setStackDisplayName("Orchard stock");
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new IllegalStateException(message);
	}
}
