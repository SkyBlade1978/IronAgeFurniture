package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBench;
import zone.moddev.mc.skysfurniturecore.api.seating.BenchType;
import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockStorageBench;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityStorageBench;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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
public final class DoubleStorageBenchClientProbe {
	private static BlockPos left;
	private static Future<?> prepared;
	private static int ticks;
	private static int step;

	private DoubleStorageBenchClientProbe() { }

	static void start(Minecraft client, BlockPos origin) {
		left = origin;
		prepared = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
			WorldServer world = player.getServerWorld();
			Block oak = Block.getBlockFromName("ironagefurniture:storage_bench_wood_ironage_oak");
			Block birch = Block.getBlockFromName("ironagefurniture:storage_bench_wood_ironage_birch");
			ItemStack padded = ItemBlockStorageBench.paddedStack(birch, PaddedBenchColour.CYAN);

			for (BlockPos floor : BlockPos.getAllInBox(origin.add(-1, -1, -1), origin.add(5, -1, 6))) {
				world.setBlockState(floor, Blocks.STONE.getDefaultState());
			}
			place(world, player, origin, oak, new ItemStack(oak));
			place(world, player, origin.east(), oak, new ItemStack(oak));
			place(world, player, origin.east(3), birch, padded);
			place(world, player, origin.east(4), birch, padded);
			((TileEntityStorageBench)world.getTileEntity(origin)).getInventory().setInventorySlotContents(26,
				new ItemStack(Items.APPLE).setStackDisplayName("Window seat apple"));
			((TileEntityStorageBench)world.getTileEntity(origin.east())).getInventory().setInventorySlotContents(0,
				new ItemStack(Items.BOOK).setStackDisplayName("Window seat book"));
			player.inventory.clear();
			player.sendContainerToPlayer(player.inventoryContainer);
			player.setPositionAndUpdate(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 3.5D);
		});
	}

	private static void place(WorldServer world, EntityPlayerMP player, BlockPos pos, Block bench, ItemStack stack) {
		IBlockState state = bench.getDefaultState().withProperty(StorageBench.PADDED, ItemBlockStorageBench.isPadded(stack));
		world.setBlockState(pos, state);
		bench.onBlockPlacedBy(world, pos, state, player, stack);
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (left == null || event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		try {
			require(++ticks < 220, "Double storage bench client stalled at step " + step);
			if (!prepared.isDone()) {
				return;
			}
			prepared.get();
			if (!(client.world.getTileEntity(left) instanceof TileEntityStorageBench)) {
				return;
			}

			switch (step) {
			case 0:
				if (client.player.getDistanceSqToCenter(left) > 20 || !paired(client, left) || !paired(client, left.east(3))) {
					return;
				}
				click(client, left.east());
				break;
			case 1:
				if (!checkScreen(client)) {
					return;
				}
				client.player.closeScreen();
				break;
			case 2:
				if (client.currentScreen != null) {
					return;
				}
				click(client, left);
				break;
			case 3:
				if (!checkScreen(client)) {
					return;
				}
				client.player.closeScreen();
				client.player.setPositionAndRotation(left.getX() + 2.45D, left.getY() + 0.35D, left.getZ() + 5.5D, 180.0F, 17.0F);
				client.player.prevRotationYaw = 180.0F;
				client.player.prevRotationPitch = 17.0F;
				break;
			case 4:
				if (client.currentScreen != null || ticks % 16 != 0) {
					return;
				}
				VanillaFoodClientProbe.screenshot(client, "phase5-double-storage-benches.png");
				ToolRackClientProbe.start(client, left.east(12));
				left = null;
				LogManager.getLogger().info("IAF DOUBLE STORAGE BENCH NETWORK PASSED: synced plain and padded joins and 54-slot GUI from both panels");
				return;
			default:
				throw new IllegalStateException("Unexpected double storage bench step " + step);
			}
			step++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static boolean paired(Minecraft client, BlockPos pos) {
		IBlockState left = client.world.getBlockState(pos);
		IBlockState right = client.world.getBlockState(pos.east());
		return left.getBlock() instanceof StorageBench && right.getBlock() instanceof StorageBench
			&& left.getActualState(client.world, pos).getValue(StorageBench.TYPE) == BenchType.LEFT
			&& right.getActualState(client.world, pos.east()).getValue(StorageBench.TYPE) == BenchType.RIGHT;
	}

	private static boolean checkScreen(Minecraft client) {
		if (!(client.currentScreen instanceof GuiChest)) {
			return false;
		}

		IInventory inventory = ((ContainerChest)client.player.openContainer).getLowerChestInventory();
		require(inventory.getSizeInventory() == 54, "Expected 54 slots from the bench pair");
		ItemStack apple = inventory.getStackInSlot(26);
		ItemStack book = inventory.getStackInSlot(27);
		if (apple == null || book == null) {
			return false;
		}
		require("Window seat apple".equals(apple.getDisplayName()) && "Window seat book".equals(book.getDisplayName()),
			"Bench inventories appeared in different slot orders");
		return true;
	}

	private static void click(Minecraft client, BlockPos pos) {
		Vec3d click = new Vec3d(pos).addVector(0.5D, 0.3D, 0.94D);
		require(client.playerController.processRightClickBlock(client.player, client.world, null, pos, EnumFacing.SOUTH, click,
			EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS, "Client rejected the bench panel click");
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
