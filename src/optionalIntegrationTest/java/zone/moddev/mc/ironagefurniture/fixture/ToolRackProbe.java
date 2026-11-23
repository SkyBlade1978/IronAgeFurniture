package zone.moddev.mc.ironagefurniture.fixture;

import java.util.List;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.ToolRack;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityToolRack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

final class ToolRackProbe {
	private static final BlockPos SAVE = new BlockPos(127, 120, 405);
	private static final BlockPos WEAPONS_SAVE = SAVE.east(4);

	private ToolRackProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block oak = Ironagefurniture.BlockRegistry.get("tool_rack_wood_ironage_oak");

		for (EnumFacing front : EnumFacing.Plane.HORIZONTAL) {
			BlockPos row = origin.east(10 * front.getHorizontalIndex());
			testRow(world, player, oak, row, front);
			testSwordAndShield(world, player, oak, row.south(8), front);
		}

		place(world, player, oak, SAVE, EnumFacing.NORTH);
		place(world, player, oak, SAVE.east(), EnumFacing.NORTH);
		rack(world, SAVE).insertTool(1, pickaxe());
		rack(world, SAVE.east()).insertTool(0, shears());
		require(mask(world, SAVE) == 2 && mask(world, SAVE.east()) == 1, "Rack seam did not connect across a chunk boundary");
		place(world, player, oak, WEAPONS_SAVE, EnumFacing.WEST);
		rack(world, WEAPONS_SAVE).insertTool(0, ShelvingPantryProbe.enchantedSword());
		rack(world, WEAPONS_SAVE).insertTool(1, shield());
		player.inventory.clear();
		LogManager.getLogger().info("IAF TOOL RACK PROBE PASSED: four mounts, continuous rails, exact swords and shields, hopper feed and drops");
	}

	private static void testRow(WorldServer world, FakePlayer player, Block oak, BlockPos first, EnumFacing front) {
		EnumFacing across = front.rotateY();
		BlockPos middle = first.offset(across);
		BlockPos last = middle.offset(across);
		BlockPos unsupported = last.offset(across, 2);
		world.getChunkFromBlockCoords(unsupported);
		require(!oak.canPlaceBlockOnSide(world, unsupported, front), "Tool rack placed in mid-air");
		place(world, player, oak, first, front);
		place(world, player, oak, middle, front);
		place(world, player, oak, last, front);
		require(mask(world, first) == 2 && mask(world, middle) == 3 && mask(world, last) == 1, "Incorrect rack row lengths");
		AxisAlignedBB body = world.getBlockState(middle).getBoundingBox(world, middle);
		require(across.getAxis() == EnumFacing.Axis.X ? body.minX == 0 && body.maxX == 1 : body.minZ == 0 && body.maxZ == 1,
			"Rail selection stopped short at a seam");

		TileEntityToolRack rack = rack(world, middle);
		ItemStack tool = pickaxe();
		click(world, player, middle, front, 0, tool.copy(), EnumHand.MAIN_HAND);
		require(player.getHeldItemMainhand() == null && ItemStack.areItemStacksEqual(tool, rack.getTool(0)), "Hand placement lost tool data");
		ItemStack replacement = shears();
		click(world, player, middle, front, 0, replacement.copy(), EnumHand.MAIN_HAND);
		require(ItemStack.areItemStacksEqual(replacement, player.getHeldItemMainhand()) && ItemStack.areItemStacksEqual(tool, rack.getTool(0)),
			"Occupied hook consumed or replaced its tool");

		requireHangingHit(world, middle, front, 0);

		for (int slot = 0; slot < 36; slot++) {
			player.inventory.setInventorySlotContents(slot, new ItemStack(Blocks.COBBLESTONE, 64));
		}
		click(world, player, middle, front, 0, null, EnumHand.OFF_HAND);
		require(ItemStack.areItemStacksEqual(tool, rack.getTool(0)), "A full inventory lost a displayed tool");
		player.inventory.clear();
		click(world, player, middle, front, 0, null, EnumHand.MAIN_HAND);
		require(rack.getTool(0) == null && ItemStack.areItemStacksEqual(tool, player.inventory.getStackInSlot(0)),
			"Empty-hand retrieval lost tool data");
		player.inventory.clear();

		rack.insertTool(0, tool);
		world.setBlockState(middle.up(), Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN));
		TileEntityHopper hopper = (TileEntityHopper)world.getTileEntity(middle.up());
		hopper.setInventorySlotContents(0, replacement.copy());
		hopper.update();
		require(hopper.getStackInSlot(0) == null && ItemStack.areItemStacksEqual(replacement, rack.getTool(1)),
			"Hopper missed the open hook");
		ItemStack extra = new ItemStack(Items.IRON_AXE);
		hopper.setInventorySlotContents(0, extra.copy());
		for (int tick = 0; tick < 16; tick++) {
			hopper.update();
		}
		require(ItemStack.areItemStacksEqual(extra, hopper.getStackInSlot(0)), "A full rack consumed a tool");
		hopper.clear();
		world.setBlockToAir(middle.up());

		world.setBlockToAir(middle.offset(front.getOpposite()));
		require(world.isAirBlock(middle) && mask(world, first) == 0 && mask(world, last) == 0, "Wall removal did not split the run");
		List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(middle).expand(1, 1, 1));
		require(count(drops, tool) == 1 && count(drops, replacement) == 1 && count(drops, new ItemStack(oak)) == 1,
			"Support failure lost or duplicated a tool or rack");
	}

	private static void testSwordAndShield(WorldServer world, FakePlayer player, Block block, BlockPos pos, EnumFacing front) {
		place(world, player, block, pos, front);
		ItemStack sword = ShelvingPantryProbe.enchantedSword();
		ItemStack shield = shield();
		TileEntityToolRack rack = rack(world, pos);
		click(world, player, pos, front, 0, sword.copy(), EnumHand.MAIN_HAND);
		click(world, player, pos, front, 1, shield.copy(), EnumHand.MAIN_HAND);
		require(player.getHeldItemMainhand() == null && ItemStack.areItemStacksEqual(sword, rack.getTool(0))
			&& ItemStack.areItemStacksEqual(shield, rack.getTool(1)), "Rack did not retain the exact sword and shield");
		requireHangingHit(world, pos, front, 0);
		requireHangingHit(world, pos, front, 1);

		click(world, player, pos, front, 1, null, EnumHand.MAIN_HAND);
		require(rack.getTool(1) == null && ItemStack.areItemStacksEqual(shield, player.inventory.getStackInSlot(0)),
			"Rack pickup lost the shield design, enchantment or damage");
		click(world, player, pos, front, 1, player.getHeldItemMainhand(), EnumHand.MAIN_HAND);
		world.destroyBlock(pos, true);
		List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos));
		require(count(drops, sword) == 1 && count(drops, shield) == 1, "Breaking a rack lost or duplicated equipment");
		player.inventory.clear();
	}

	private static void requireHangingHit(WorldServer world, BlockPos pos, EnumFacing front, int slot) {
		EnumFacing across = front.rotateY();
		double side = slot == 0 ? 0.21875D : -0.21875D;
		Vec3d target = new Vec3d(pos).addVector(0.5D + across.getFrontOffsetX() * side - front.getFrontOffsetX() * 0.35D,
			0.3D, 0.5D + across.getFrontOffsetZ() * side - front.getFrontOffsetZ() * 0.35D);
		RayTraceResult hit = world.rayTraceBlocks(target.add(new Vec3d(front.getDirectionVec()).scale(2)), target);
		require(hit != null && pos.equals(hit.getBlockPos()) && hit.subHit == slot, "Hanging item missed the ray on " + front);
	}

	private static long count(List<EntityItem> drops, ItemStack expected) {
		return drops.stream().filter(drop -> ItemStack.areItemStacksEqual(drop.getEntityItem(), expected)).count();
	}

	private static void place(WorldServer world, FakePlayer player, Block rack, BlockPos pos, EnumFacing front) {
		BlockPos wall = pos.offset(front.getOpposite());
		world.setBlockState(wall, Blocks.STONE.getDefaultState());
		player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(rack));
		require(player.getHeldItemMainhand().onItemUse(player, world, wall, EnumHand.MAIN_HAND, front, 0.5F, 0.5F, 0.5F)
			== EnumActionResult.SUCCESS && world.getBlockState(pos).getBlock() == rack
			&& world.getBlockState(pos).getValue(BlockHorizontal.FACING) == front, "Wall rack player placement failed");
	}

	private static void click(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing front, int slot, ItemStack held, EnumHand hand) {
		EnumFacing across = front.rotateY();
		double side = slot == 0 ? 0.21875D : -0.21875D;
		IBlockState state = world.getBlockState(pos);
		player.setHeldItem(hand, held);
		require(state.getBlock().onBlockActivated(world, pos, state, player, hand, held, front,
			(float)(0.5D + across.getFrontOffsetX() * side), 0.75F, (float)(0.5D + across.getFrontOffsetZ() * side)),
			"Tool rack interaction was rejected");
	}

	static void verifyReload(WorldServer world) {
		world.getChunkProvider().loadChunk(SAVE.getX() >> 4, SAVE.getZ() >> 4);
		world.getChunkProvider().loadChunk(SAVE.east().getX() >> 4, SAVE.getZ() >> 4);
		TileEntity tile = world.getTileEntity(SAVE);
		if (!(tile instanceof TileEntityToolRack)) {
			return;
		}

		require(mask(world, SAVE) == 2 && mask(world, SAVE.east()) == 1
			&& ItemStack.areItemStacksEqual(pickaxe(), rack(world, SAVE).getTool(1))
			&& ItemStack.areItemStacksEqual(shears(), rack(world, SAVE.east()).getTool(0)),
			"Reload lost exact tools or the across-chunk join");
		LogManager.getLogger().info("IAF TOOL RACK RELOAD PASSED: joined wall rail and exact tool NBT");
		TileEntityToolRack weapons = rack(world, WEAPONS_SAVE);
		if (weapons != null) {
			require(ItemStack.areItemStacksEqual(ShelvingPantryProbe.enchantedSword(), weapons.getTool(0))
				&& ItemStack.areItemStacksEqual(shield(), weapons.getTool(1)), "Reload lost exact sword or shield NBT");
			LogManager.getLogger().info("IAF TOOL RACK EQUIPMENT RELOAD PASSED: exact sword and patterned shield");
		}
	}

	private static TileEntityToolRack rack(WorldServer world, BlockPos pos) {
		return (TileEntityToolRack)world.getTileEntity(pos);
	}

	private static int mask(WorldServer world, BlockPos pos) {
		return world.getBlockState(pos).getActualState(world, pos).getValue(ToolRack.CONNECTIONS);
	}

	static ItemStack pickaxe() {
		ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE, 1, 75).setStackDisplayName("Wall Rack Pickaxe");
		stack.addEnchantment(Enchantments.EFFICIENCY, 2);
		return stack;
	}

	static ItemStack shield() {
		ItemStack stack = ShelvingPantryProbe.patternedShield();
		stack.addEnchantment(Enchantments.UNBREAKING, 2);
		return stack;
	}

	private static ItemStack shears() {
		return new ItemStack(Items.SHEARS, 1, 18).setStackDisplayName("Wall Rack Shears");
	}

	private static void require(boolean condition, String problem) {
		if (!condition) {
			throw new IllegalStateException(problem);
		}
	}
}
