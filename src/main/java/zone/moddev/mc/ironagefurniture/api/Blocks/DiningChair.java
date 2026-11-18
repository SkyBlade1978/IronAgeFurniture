package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.List;

import zone.moddev.mc.ironagefurniture.api.entity.Seat;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DiningChair extends Chair {
	public static final PropertyBool TUCKED = PropertyBool.create("tucked");

	private static final double TUCKED_OFFSET = 10.0D / 16.0D;
	private static final AxisAlignedBB SEAT_BOUNDS = new AxisAlignedBB(
		1.0D / 16.0D, 0, 2.0D / 16.0D, 15.0D / 16.0D, 8.0D / 16.0D, 1);
	private static final AxisAlignedBB BACK_BOUNDS = new AxisAlignedBB(
		1.0D / 16.0D, 8.0D / 16.0D, 14.0D / 16.0D, 15.0D / 16.0D, 25.0D / 16.0D, 1);
	private static final AxisAlignedBB CHAIR_BOUNDS = SEAT_BOUNDS.union(BACK_BOUNDS);

	public DiningChair(Material material, String name, float resistance, float hardness) {
		super(material, name, resistance, hardness);
		this.setDefaultState(this.blockState.getBaseState()
			.withProperty(FACING, EnumFacing.NORTH)
			.withProperty(TUCKED, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, TUCKED);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return super.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, meta, placer, stack)
			.withProperty(TUCKED, false);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState()
			.withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
			.withProperty(TUCKED, (meta & 4) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int facing = state.getValue(FACING).getHorizontalIndex();
		return state.getValue(TUCKED) ? facing | 4 : facing;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		boolean tucked = state.getValue(TUCKED);

		if (player.isSneaking()) {
			if (!tucked && !this.isFacingDiningTable(world, pos, state)) {
				return false;
			}

			if (!world.isRemote && this.getComparatorInputOverride(state, world, pos) == 0) {
				world.setBlockState(pos, state.withProperty(TUCKED, !tucked), 3);
			}

			return true;
		}

		if (tucked || world.isRemote || this.getComparatorInputOverride(state, world, pos) != 0) {
			return true;
		}

		super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changedBlock) {
		if (!world.isRemote && state.getValue(TUCKED) && !this.isFacingDiningTable(world, pos, state)) {
			world.setBlockState(pos, state.withProperty(TUCKED, false), 3);
		}

		super.neighborChanged(state, world, pos, changedBlock);
	}

	private boolean isFacingDiningTable(World world, BlockPos pos, IBlockState state) {
		BlockPos tablePos = pos.offset(state.getValue(FACING).getOpposite());
		Block block = world.getBlockState(tablePos).getBlock();
		return block instanceof DiningTable && !(block instanceof LowTable);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return this.positionBounds(CHAIR_BOUNDS, state);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entity) {
		if (entity instanceof Seat) {
			return;
		}

		addCollisionBoxToList(pos, entityBox, collidingBoxes, this.positionBounds(SEAT_BOUNDS, state));
		addCollisionBoxToList(pos, entityBox, collidingBoxes, this.positionBounds(BACK_BOUNDS, state));
	}

	private AxisAlignedBB positionBounds(AxisAlignedBB box, IBlockState state) {
		EnumFacing facing = state.getValue(FACING);
		AxisAlignedBB rotated = this.rotateFromSouth(box, facing);

		if (!state.getValue(TUCKED)) {
			return rotated;
		}

		EnumFacing towardTable = facing.getOpposite();
		return rotated.offset(towardTable.getFrontOffsetX() * TUCKED_OFFSET, 0,
			towardTable.getFrontOffsetZ() * TUCKED_OFFSET);
	}

	private AxisAlignedBB rotateFromSouth(AxisAlignedBB box, EnumFacing facing) {
		switch (facing) {
		case NORTH:
			return new AxisAlignedBB(1 - box.maxX, box.minY, 1 - box.maxZ, 1 - box.minX, box.maxY, 1 - box.minZ);
		case EAST:
			return new AxisAlignedBB(box.minZ, box.minY, 1 - box.maxX, box.maxZ, box.maxY, 1 - box.minX);
		case WEST:
			return new AxisAlignedBB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX);
		default:
			return box;
		}
	}
}
