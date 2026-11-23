package zone.moddev.mc.ironagefurniture.fixture;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBench;
import zone.moddev.mc.ironagefurniture.api.Enumerations.BenchType;
import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockStorageBench;
import zone.moddev.mc.ironagefurniture.api.entity.Seat;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityStorageBench;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

final class StorageBenchProbe {
	private static final BlockPos SAVE = new BlockPos(100, 120, 405);
	private static final BlockPos DOUBLE_SAVE = new BlockPos(111, 120, 405);

	private StorageBenchProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block block = Ironagefurniture.BlockRegistry.get("storage_bench_wood_ironage_oak");

		FakePlayer companion = FakePlayerFactory.get(world, new GameProfile(UUID.randomUUID(), "BenchVisitor"));
		companion.connection = player.connection;

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			BlockPos pos = origin.east(5 * facing.getHorizontalIndex());
			verifyBench(world, player, block, pos, facing, new ItemStack(block));
			verifyBench(world, player, block, pos.south(9), facing, ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.MAGENTA));
			ItemStack pairStack = facing.getAxis() == EnumFacing.Axis.X ? new ItemStack(block)
				: ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.MAGENTA);
			verifyPair(world, player, companion, block, origin.south(24).east(10 * facing.getHorizontalIndex()), facing, pairStack);
		}

		verifyIncompatible(world, player, block, origin.south(40));
		place(world, player, block, DOUBLE_SAVE, EnumFacing.SOUTH, ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.CYAN));
		place(world, player, block, DOUBLE_SAVE.east(), EnumFacing.SOUTH, ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.CYAN));
		benchAt(world, DOUBLE_SAVE).getInventory().setInventorySlotContents(26, proofItem());
		benchAt(world, DOUBLE_SAVE.east()).getInventory().setInventorySlotContents(0, secondItem());
		requirePair(world, DOUBLE_SAVE, DOUBLE_SAVE.east());
		LogManager.getLogger().info("IAF DOUBLE STORAGE BENCH PROBE PASSED: both finishes, four facings, two seats, shared hoppers and split drops");

		place(world, player, block, SAVE, EnumFacing.NORTH, ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.BLUE));
		((TileEntityStorageBench)world.getTileEntity(SAVE)).getInventory().setInventorySlotContents(26, proofItem());
		LogManager.getLogger().info("IAF STORAGE BENCH PROBE PASSED: plain and padded share a block, four facings, separate seating and storage, hoppers and exact drops");
	}

	private static void verifyBench(WorldServer world, FakePlayer player, Block block, BlockPos pos, EnumFacing facing, ItemStack stack) {
		IBlockState state = place(world, player, block, pos, facing, stack);
		TileEntityStorageBench bench = (TileEntityStorageBench)world.getTileEntity(pos);
		IInventory inventory = bench.getInventory();
		verifyHoppers(world, pos, inventory);

		Vec3d centre = new Vec3d(pos).addVector(0.5D, 0.3D, 0.5D);
		Vec3d eye = centre.add(new Vec3d(facing.getDirectionVec()).scale(1.5D));
		RayTraceResult hit = world.rayTraceBlocks(eye, centre);
		require(hit != null && pos.equals(hit.getBlockPos()) && hit.sideHit == facing, "Front hit missed the storage bench");
		player.setPosition(eye.xCoord, eye.yCoord, eye.zCoord);
		require(block.onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, null, facing, 0.5F, 0.3F, 0.5F)
			&& player.openContainer instanceof ContainerChest && !player.isRiding()
			&& ((ContainerChest)player.openContainer).getLowerChestInventory() == inventory && inventory.getSizeInventory() == 27,
			"Front panel did not open the bench inventory");
		player.closeScreen();

		float top = ItemBlockStorageBench.isPadded(stack) ? 9.0F / 16 : 0.5F;
		require(block.getBoundingBox(state, world, pos).maxY == top
			&& block.onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, null, EnumFacing.UP, 0.5F, top, 0.5F)
			&& player.getRidingEntity() instanceof Seat && player.openContainer == player.inventoryContainer,
			"Seat hit did not mount independently of the inventory");
		player.dismountRidingEntity();
		require(ItemStack.areItemStacksEqual(stack, block.getPickBlock(state, hit, world, pos, player)), "Pick block lost the bench finish");

		inventory.setInventorySlotContents(26, proofItem());
		world.destroyBlock(pos, true);
		int benchDrops = 0;
		int storedDrops = 0;
		for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
			ItemStack item = drop.getEntityItem();
			if (ItemStack.areItemStacksEqual(stack, item)) {
				benchDrops++;
			} else if (ItemStack.areItemsEqual(item, proofItem()) && ItemStack.areItemStackTagsEqual(item, proofItem())) {
				storedDrops += item.stackSize;
			}
		}
		require(benchDrops == 1 && storedDrops == proofItem().stackSize, "Broken bench lost or duplicated contents or colour");
	}

	private static void verifyPair(WorldServer world, FakePlayer player, FakePlayer companion, Block block, BlockPos left,
			EnumFacing facing, ItemStack stack) {
		EnumFacing direction = facing.rotateYCCW();
		BlockPos right = left.offset(direction);
		BlockPos third = right.offset(direction);

		place(world, player, block, left, facing, stack);
		place(world, player, block, right, facing, stack);
		place(world, player, block, third, facing, stack);
		requirePair(world, left, right);
		require(actual(world, third).getValue(StorageBench.TYPE) == BenchType.SINGLE, "A third bench joined the pair");
		AxisAlignedBB bounds = actual(world, left).getBoundingBox(world, left);
		require((direction == EnumFacing.EAST && bounds.maxX == 1) || (direction == EnumFacing.WEST && bounds.minX == 0)
			|| (direction == EnumFacing.SOUTH && bounds.maxZ == 1) || (direction == EnumFacing.NORTH && bounds.minZ == 0),
			"Paired bench bounds did not reach the seam");

		TileEntityStorageBench leftBench = benchAt(world, left);
		TileEntityStorageBench rightBench = benchAt(world, right);
		leftBench.getInventory().setInventorySlotContents(0, proofItem());
		IItemHandler leftPort = leftBench.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
		IItemHandler rightPort = rightBench.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
		require(leftPort.getSlots() == 54 && rightPort.getSlots() == 54
			&& leftPort.insertItem(53, secondItem(), false) == null
			&& ItemStack.areItemStacksEqual(rightPort.getStackInSlot(0), proofItem())
			&& ItemStack.areItemStacksEqual(rightBench.getInventory().getStackInSlot(26), secondItem()),
			"Automation did not share the whole pair in the same slot order");

		for (BlockPos half : new BlockPos[] {left, right}) {
			player.setPosition(half.getX() + 0.5D, half.getY() + 0.5D, half.getZ() + 0.5D);
			require(block.onBlockActivated(world, half, world.getBlockState(half), player, EnumHand.MAIN_HAND, null, facing,
				0.5F, 0.3F, 0.5F) && player.openContainer instanceof ContainerChest, "Double bench panel did not open");
			IInventory opened = ((ContainerChest)player.openContainer).getLowerChestInventory();
			require(opened.getSizeInventory() == 54 && ItemStack.areItemStacksEqual(opened.getStackInSlot(0), proofItem())
				&& ItemStack.areItemStacksEqual(opened.getStackInSlot(53), secondItem()), "Pair panels showed different inventories");
			player.closeScreen();
		}

		block.onBlockActivated(world, left, world.getBlockState(left), player, EnumHand.MAIN_HAND, null, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
		block.onBlockActivated(world, right, world.getBlockState(right), companion, EnumHand.MAIN_HAND, null, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
		require(player.getRidingEntity() instanceof Seat && companion.getRidingEntity() instanceof Seat
			&& player.getRidingEntity() != companion.getRidingEntity(), "Both halves did not retain their own seat");
		player.dismountRidingEntity();
		companion.dismountRidingEntity();

		world.destroyBlock(left, true);
		require(actual(world, right).getValue(StorageBench.TYPE) == BenchType.SINGLE
			&& actual(world, third).getValue(StorageBench.TYPE) == BenchType.SINGLE && rightPort.getSlots() == 27
			&& ItemStack.areItemStacksEqual(rightPort.getStackInSlot(26), secondItem()),
			"Breaking one half changed the surviving contents or paired the third bench");
		ItemStack remainder = proofItem();
		require(leftPort.insertItem(0, remainder, false) == remainder && rightPort.insertItem(53, remainder, false) == remainder,
			"An old automation handle wrote into a split or removed slot");
		int dropped = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(left).expand(1, 1, 1)).stream()
			.map(EntityItem::getEntityItem).filter(item -> ItemStack.areItemsEqual(item, proofItem())
				&& ItemStack.areItemStackTagsEqual(item, proofItem())).mapToInt(item -> item.stackSize).sum();
		require(dropped == proofItem().stackSize, "Split bench lost or duplicated its contents");
	}

	private static void verifyIncompatible(WorldServer world, FakePlayer player, Block block, BlockPos pos) {
		place(world, player, block, pos, EnumFacing.SOUTH, ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.RED));
		place(world, player, block, pos.east(), EnumFacing.SOUTH, ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.CYAN));
		place(world, player, block, pos.west(), EnumFacing.SOUTH, new ItemStack(block));
		place(world, player, block, pos.east(2), EnumFacing.NORTH, ItemBlockStorageBench.paddedStack(block, PaddedBenchColour.CYAN));
		for (int x = -1; x <= 2; x++) {
			require(actual(world, pos.east(x)).getValue(StorageBench.TYPE) == BenchType.SINGLE, "Incompatible benches joined");
		}
	}

	private static void requirePair(WorldServer world, BlockPos left, BlockPos right) {
		require(actual(world, left).getValue(StorageBench.TYPE) == BenchType.LEFT
			&& actual(world, right).getValue(StorageBench.TYPE) == BenchType.RIGHT, "Missing reciprocal storage bench join");
	}

	private static TileEntityStorageBench benchAt(WorldServer world, BlockPos pos) {
		return (TileEntityStorageBench)world.getTileEntity(pos);
	}

	private static IBlockState actual(WorldServer world, BlockPos pos) {
		return world.getBlockState(pos).getActualState(world, pos);
	}

	private static IBlockState place(WorldServer world, FakePlayer player, Block block, BlockPos pos, EnumFacing facing, ItemStack stack) {
		world.getChunkFromBlockCoords(pos);
		player.rotationYaw = facing.getOpposite().getHorizontalAngle();
		IBlockState state = block.getStateForPlacement(world, pos, EnumFacing.UP, 0.5F, 0.5F, 0.5F, 0, player, stack);
		require(state.getValue(BlockHorizontal.FACING) == facing && state.getValue(StorageBench.PADDED) == ItemBlockStorageBench.isPadded(stack),
			"Placement chose the wrong facing or padded state");
		require(((ItemBlock)Item.getItemFromBlock(block)).placeBlockAt(stack.copy(), player, world, pos, EnumFacing.UP,
			0.5F, 0.5F, 0.5F, state), "Bench item did not place");
		return world.getBlockState(pos);
	}

	private static void verifyHoppers(WorldServer world, BlockPos pos, IInventory inventory) {
		IBlockState hopper = Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN);
		world.setBlockState(pos.up(), hopper);
		TileEntityHopper input = (TileEntityHopper)world.getTileEntity(pos.up());
		ItemStack sample = new ItemStack(Items.BOOK).setStackDisplayName("Bench hopper sample");
		input.setInventorySlotContents(0, sample.copy());
		input.update();
		require(input.getStackInSlot(0) == null && ItemStack.areItemStacksEqual(sample, inventory.getStackInSlot(0)),
			"Top hopper did not feed the storage bench");
		world.setBlockToAir(pos.up());

		world.setBlockState(pos.down(), hopper);
		TileEntityHopper output = (TileEntityHopper)world.getTileEntity(pos.down());
		output.update();
		require(inventory.getStackInSlot(0) == null && ItemStack.areItemStacksEqual(sample, output.getStackInSlot(0)),
			"Lower hopper did not extract from the storage bench");
		output.clear();
		world.setBlockToAir(pos.down());
	}

	static void verifyReload(WorldServer world) {
		world.getChunkProvider().loadChunk(SAVE.getX() >> 4, SAVE.getZ() >> 4);
		TileEntity tile = world.getTileEntity(SAVE);
		if (!(tile instanceof TileEntityStorageBench)) {
			return;
		}

		TileEntityStorageBench bench = (TileEntityStorageBench)tile;
		IBlockState state = world.getBlockState(SAVE);
		require(state.getValue(StorageBench.PADDED) && state.getValue(BlockHorizontal.FACING) == EnumFacing.NORTH
			&& bench.getColour() == PaddedBenchColour.BLUE && ItemStack.areItemStacksEqual(proofItem(), bench.getInventory().getStackInSlot(26)),
			"Reload lost the storage bench facing, cushion or items");
		LogManager.getLogger().info("IAF STORAGE BENCH RELOAD PASSED: exact NBT and cushion");

		world.getChunkProvider().loadChunk(DOUBLE_SAVE.getX() >> 4, DOUBLE_SAVE.getZ() >> 4);
		world.getChunkProvider().loadChunk(DOUBLE_SAVE.east().getX() >> 4, DOUBLE_SAVE.getZ() >> 4);
		if (world.getTileEntity(DOUBLE_SAVE) instanceof TileEntityStorageBench) {
			requirePair(world, DOUBLE_SAVE, DOUBLE_SAVE.east());
			IInventory combined = benchAt(world, DOUBLE_SAVE).getAccessibleInventory();
			require(combined.getSizeInventory() == 54 && ItemStack.areItemStacksEqual(proofItem(), combined.getStackInSlot(26))
				&& ItemStack.areItemStacksEqual(secondItem(), combined.getStackInSlot(27)), "Across-chunk join or contents lost on reload");
			LogManager.getLogger().info("IAF DOUBLE STORAGE BENCH RELOAD PASSED: join and both inventories across a chunk boundary");
		}
	}

	private static ItemStack proofItem() {
		return new ItemStack(Items.GOLDEN_APPLE, 3, 1).setStackDisplayName("Bench provisions");
	}

	private static ItemStack secondItem() {
		return new ItemStack(Items.BOOK, 2).setStackDisplayName("Second seat's books");
	}

	private static void require(boolean condition, String problem) {
		if (!condition) {
			throw new IllegalStateException(problem);
		}
	}
}
