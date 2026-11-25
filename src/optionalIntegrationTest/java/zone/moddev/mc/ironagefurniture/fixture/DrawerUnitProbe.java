package zone.moddev.mc.ironagefurniture.fixture;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.DrawerUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.DrawerUnit.Pair;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDrawerUnit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

final class DrawerUnitProbe {
	private static final BlockPos SAVED_POS = new BlockPos(88, 120, 385);

	private DrawerUnitProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block full = Ironagefurniture.BlockRegistry.get("drawer_unit_wood_ironage_oak");
		Block half = Ironagefurniture.BlockRegistry.get("half_drawer_unit_wood_ironage_oak");
		Block birch = Ironagefurniture.BlockRegistry.get("drawer_unit_wood_ironage_birch");

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			BlockPos row = origin.east(facing.getHorizontalIndex() * 8);
			verifyUnit(world, player, full, facing, row, 1.0D);
			verifyUnit(world, player, half, facing, row.south(12), 0.5D);
		}

		place(world, SAVED_POS, full, EnumFacing.SOUTH);
		place(world, SAVED_POS.east(), full, EnumFacing.SOUTH);
		place(world, SAVED_POS.up(), full, EnumFacing.SOUTH);
		place(world, SAVED_POS.east().up(), full, EnumFacing.SOUTH);
		place(world, SAVED_POS.west(), half, EnumFacing.SOUTH);
		place(world, SAVED_POS.east(2), birch, EnumFacing.SOUTH);
		place(world, SAVED_POS.up(2), full, EnumFacing.NORTH);
		require(state(world, SAVED_POS.west()).getValue(DrawerUnit.PAIR) == Pair.SINGLE
			&& state(world, SAVED_POS.east(2)).getValue(DrawerUnit.PAIR) == Pair.SINGLE
			&& !state(world, SAVED_POS.up()).getValue(DrawerUnit.ABOVE),
			"Incompatible depth, wood or orientation changed drawer joins");
		unit(world, SAVED_POS).getDrawer(true).setInventorySlotContents(5, leftItem());
		unit(world, SAVED_POS.east()).getDrawer(false).setInventorySlotContents(8, rightItem());
		LogManager.getLogger().info("IAF DRAWER UNIT PROBE PASSED: both depths, four facings, wide drawers, three-high stacks, hoppers and split drops");
	}

	private static void verifyUnit(WorldServer world, FakePlayer player, Block block, EnumFacing facing,
			BlockPos left, double depth) {
		BlockPos right = left.offset(facing.rotateYCCW());
		BlockPos extra = right.offset(facing.rotateYCCW());

		place(world, left, block, facing);
		AxisAlignedBB bounds = block.getBoundingBox(state(world, left), world, left);
		double actualDepth = facing.getAxis() == EnumFacing.Axis.Z ? bounds.maxZ - bounds.minZ : bounds.maxX - bounds.minX;
		require(actualDepth == depth && bounds.minY == 0 && bounds.maxY == 1, "Incorrect drawer unit bounds on " + facing);
		verifyHoppers(world, left);
		place(world, right, block, facing);
		place(world, extra, block, facing);
		requirePair(world, left, right);
		require(state(world, extra).getValue(DrawerUnit.PAIR) == Pair.SINGLE, "A third block joined the drawer pair");

		for (int y = 1; y <= 2; y++) {
			place(world, left.up(y), block, facing);
			place(world, right.up(y), block, facing);
			requirePair(world, left.up(y), right.up(y));
		}
		require(state(world, left).getValue(DrawerUnit.ABOVE) && state(world, right.up(2)).getValue(DrawerUnit.BELOW)
			&& state(world, left.up()).getValue(DrawerUnit.ABOVE) && state(world, left.up()).getValue(DrawerUnit.BELOW),
			"Middle row did not connect both ways");

		TileEntityDrawerUnit leftUnit = unit(world, left);
		TileEntityDrawerUnit rightUnit = unit(world, right);
		leftUnit.getDrawer(true).setInventorySlotContents(0, leftItem());
		rightUnit.getDrawer(true).setInventorySlotContents(0, rightItem());
		rightUnit.getDrawer(false).setInventorySlotContents(0, new ItemStack(Items.POTATO, 3));

		IInventory wide = click(world, player, block, right, facing, true, depth);
		require(wide.getSizeInventory() == 18 && ItemStack.areItemStacksEqual(wide.getStackInSlot(0), leftItem())
			&& ItemStack.areItemStacksEqual(wide.getStackInSlot(9), rightItem()), "Incorrect wide inventory ordering or contents");
		require(leftUnit.isDrawerOpen(true) && rightUnit.isDrawerOpen(true) && !rightUnit.isDrawerOpen(false)
			&& !unit(world, left.up()).isDrawerOpen(true), "Opening a wide drawer affected the wrong pieces");
		AxisAlignedBB openBounds = block.getBoundingBox(state(world, left), world, left);
		double openDepth = facing.getAxis() == EnumFacing.Axis.Z
			? openBounds.maxZ - openBounds.minZ : openBounds.maxX - openBounds.minX;
		require(Math.abs(openDepth - depth - depth * 3 / 8) < 0.0001D,
			"Open drawer collision did not extend to its pulled-out front on " + facing);
		player.closeScreen();

		IInventory bottom = click(world, player, block, left, facing, false, depth);
		require(bottom.getSizeInventory() == 18 && bottom.getStackInSlot(9).getItem() == Items.POTATO
			&& !leftUnit.isDrawerOpen(true) && rightUnit.isDrawerOpen(false), "Lower drawer is not independent");
		player.closeScreen();

		world.destroyBlock(left, true);
		require(state(world, right).getValue(DrawerUnit.PAIR) == Pair.SINGLE && !rightUnit.isDrawerOpen(false)
			&& ItemStack.areItemStacksEqual(rightUnit.getDrawer(true).getStackInSlot(0), rightItem()),
			"Breaking one half changed the remaining inventory");
		int itemCount = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(left).expand(1, 1, 1)).stream()
			.map(EntityItem::getEntityItem).filter(stack -> ItemStack.areItemsEqual(stack, leftItem())
				&& ItemStack.areItemStackTagsEqual(stack, leftItem())).mapToInt(stack -> stack.stackSize).sum();
		require(itemCount == leftItem().stackSize, "Breaking one half lost or duplicated its exact contents");
		require(click(world, player, block, right, facing, true, depth).getSizeInventory() == 9,
			"Surviving half did not return to a single inventory");
		player.closeScreen();
	}

	private static IInventory click(WorldServer world, FakePlayer player, Block block, BlockPos pos,
			EnumFacing facing, boolean upper, double depth) {
		Vec3d center = new Vec3d(pos).addVector(0.5D, upper ? 0.7D : 0.3D, 0.5D);
		Vec3d front = new Vec3d(facing.getDirectionVec());
		Vec3d start = center.add(front.scale(1.6D));
		Vec3d inside = center.subtract(front.scale(0.5D - depth / 2));
		RayTraceResult hit = world.rayTraceBlocks(start, inside);
		require(hit != null && hit.getBlockPos().equals(pos) && hit.sideHit == facing, "Drawer ray missed " + facing);
		player.setPosition(start.xCoord, start.yCoord, start.zCoord);
		require(block.onBlockActivated(world, pos, world.getBlockState(pos), player, EnumHand.MAIN_HAND, null, facing,
			0.5F, (float)(hit.hitVec.yCoord - pos.getY()), 0.5F) && player.openContainer instanceof ContainerChest,
			"Drawer did not open on " + facing);
		return ((ContainerChest)player.openContainer).getLowerChestInventory();
	}

	private static void verifyHoppers(WorldServer world, BlockPos pos) {
		IBlockState hopper = Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN);
		world.setBlockState(pos.up(), hopper);
		TileEntityHopper input = (TileEntityHopper)world.getTileEntity(pos.up());
		ItemStack delivery = new ItemStack(Items.PAPER).setStackDisplayName("Hopper delivery");
		input.setInventorySlotContents(0, delivery.copy());
		input.update();
		require(input.getStackInSlot(0) == null && ItemStack.areItemStacksEqual(unit(world, pos).getDrawer(true).getStackInSlot(0), delivery),
			"Vanilla hopper could not fill a drawer");
		world.setBlockToAir(pos.up());

		world.setBlockState(pos.down(), hopper);
		TileEntityHopper output = (TileEntityHopper)world.getTileEntity(pos.down());
		output.update();
		require(unit(world, pos).getDrawer(true).getStackInSlot(0) == null
			&& ItemStack.areItemStacksEqual(output.getStackInSlot(0), delivery), "Vanilla hopper could not extract from a drawer");
		world.setBlockToAir(pos.down());
	}

	static void verifyReload(WorldServer world) {
		world.getChunkProvider().loadChunk(SAVED_POS.getX() >> 4, SAVED_POS.getZ() >> 4);
		if (!(world.getBlockState(SAVED_POS).getBlock() instanceof DrawerUnit)) {
			return;
		}
		requirePair(world, SAVED_POS, SAVED_POS.east());
		requirePair(world, SAVED_POS.up(), SAVED_POS.east().up());
		require(state(world, SAVED_POS).getValue(DrawerUnit.ABOVE)
			&& ItemStack.areItemStacksEqual(unit(world, SAVED_POS).getDrawer(true).getStackInSlot(5), leftItem())
			&& ItemStack.areItemStacksEqual(unit(world, SAVED_POS.east()).getDrawer(false).getStackInSlot(8), rightItem())
			&& !unit(world, SAVED_POS).isDrawerOpen(true), "Drawer pairs or items changed after reloading");
		LogManager.getLogger().info("IAF DRAWER UNIT RELOAD PASSED: paired stacks retained their joins and exact drawer contents");
	}

	private static void requirePair(WorldServer world, BlockPos left, BlockPos right) {
		require(state(world, left).getValue(DrawerUnit.PAIR) == Pair.LEFT && state(world, right).getValue(DrawerUnit.PAIR) == Pair.RIGHT,
			"Missing reciprocal drawer pair at " + left + " and " + right);
	}

	private static IBlockState state(WorldServer world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getActualState(world, pos);
	}

	private static void place(WorldServer world, BlockPos pos, Block unit, EnumFacing facing) {
		world.getChunkFromBlockCoords(pos);
		world.setBlockState(pos, unit.getDefaultState().withProperty(DrawerUnit.FACING, facing), 3);
	}

	private static TileEntityDrawerUnit unit(WorldServer world, BlockPos pos) {
		return (TileEntityDrawerUnit)world.getTileEntity(pos);
	}

	private static ItemStack leftItem() {
		return new ItemStack(Items.BOOK, 3).setStackDisplayName("Left drawer");
	}

	private static ItemStack rightItem() {
		return new ItemStack(Items.APPLE, 7).setStackDisplayName("Right drawer");
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
