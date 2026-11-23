package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.List;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Shared placement and connected frame state for wall-mounted and freestanding racks. */
public abstract class RackBlock extends BlockHBase {

	public static final PropertyInteger CONNECTIONS = PropertyInteger.create("connections", 0, 15);
	public static final PropertyBool DATA = PropertyBool.create("data");
	public static final PropertyBool STANDING = PropertyBool.create("standing");

	protected static final int UP = 1;
	protected static final int RIGHT = 2;
	protected static final int DOWN = 4;
	protected static final int LEFT = 8;

	protected RackBlock(Material materialIn, float resistance, float hardness) {
		super(materialIn);
		this.blockResistance = resistance;
		this.blockHardness = hardness;
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setCreativeTab(Ironagefurniture.ironagefurnitureTab);
		this.setLightOpacity(0);
		this.setDefaultState(this.blockState.getBaseState()
			.withProperty(FACING, EnumFacing.NORTH)
			.withProperty(CONNECTIONS, Integer.valueOf(0))
			.withProperty(DATA, Boolean.valueOf(false))
			.withProperty(STANDING, Boolean.valueOf(false)));
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		if (this.canStandingRackStay(worldIn, pos, EnumFacing.NORTH)) {
			return super.canPlaceBlockAt(worldIn, pos);
		}

		for (EnumFacing facing : FACING.getAllowedValues()) {
			if (this.canWallRackStay(worldIn, pos, facing)) {
				return super.canPlaceBlockAt(worldIn, pos);
			}
		}

		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.UP) {
			return this.canStandingRackStay(worldIn, pos, EnumFacing.NORTH)
				&& super.canPlaceBlockOnSide(worldIn, pos, side);
		}

		return side.getAxis().isHorizontal() && this.canWallRackStay(worldIn, pos, side)
			&& super.canPlaceBlockOnSide(worldIn, pos, side);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		if (side == EnumFacing.UP && this.canStandingRackStay(world, pos, EnumFacing.NORTH)) {
			EnumFacing facing = placer != null ? placer.getHorizontalFacing().getOpposite() : EnumFacing.NORTH;

			return this.getDefaultState()
				.withProperty(FACING, facing)
				.withProperty(CONNECTIONS, Integer.valueOf(0))
				.withProperty(DATA, Boolean.valueOf(false))
				.withProperty(STANDING, Boolean.valueOf(true));
		}

		return this.getDefaultState()
			.withProperty(FACING, side.getAxis().isHorizontal() ? side : EnumFacing.NORTH)
			.withProperty(CONNECTIONS, Integer.valueOf(0))
			.withProperty(DATA, Boolean.valueOf(false))
			.withProperty(STANDING, Boolean.valueOf(false));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(CONNECTIONS,
			Integer.valueOf(this.getConnectionMask(worldIn, pos, state.getValue(FACING),
				state.getValue(STANDING).booleanValue())));
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		this.notifyRackAndNeighbors(worldIn, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		if (!this.canRackStay(worldIn, pos, state)) {
			if (!worldIn.isRemote) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
			}

			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
			return;
		}

		this.notifyRackAndNeighbors(worldIn, pos);
	}

	public boolean tryPlaceRackFromRackClick(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!this.isRackItem(heldItem)) {
			return false;
		}

		EnumFacing currentFacing = state.getValue(FACING);
		EnumFacing placementSide = this.getRackPlacementSide(currentFacing, side, hitX, hitY, hitZ);

		if (placementSide == null) {
			return false;
		}

		Block rackBlock = ((ItemBlock)heldItem.getItem()).getBlock();
		return this.tryPlaceRack(worldIn, pos.offset(placementSide), currentFacing, rackBlock,
			state.getValue(STANDING).booleanValue(), playerIn, hand, heldItem);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		// A hopper must see the intake on an empty rack, including old saves with DATA=false.
		return true;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		double depth = this.getRackDepth();
		double back = state.getValue(STANDING) ? (1.0D + depth) / 2.0D : 1.0D;
		AxisAlignedBB box = new AxisAlignedBB(0, 0, back - depth, 1, 1, back);
		return this.rotateToFacing(box, state.getValue(FACING));
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		return this.getBoundingBox(state, worldIn, pos).offset(pos);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		super.addCollisionBoxToList(pos, entityBox, collidingBoxes, this.getBoundingBox(state, worldIn, pos));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState()
			.withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
			.withProperty(CONNECTIONS, Integer.valueOf(0))
			.withProperty(DATA, Boolean.valueOf((meta & 4) != 0))
			.withProperty(STANDING, Boolean.valueOf((meta & 8) != 0));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(FACING).getHorizontalIndex();
		if (state.getValue(DATA).booleanValue()) {
			meta |= 4;
		}
		if (state.getValue(STANDING).booleanValue()) {
			meta |= 8;
		}

		return meta;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { CONNECTIONS, DATA, FACING, STANDING });
	}

	private EnumFacing getRackPlacementSide(EnumFacing rackFacing, EnumFacing clickedSide, float hitX, float hitY,
			float hitZ) {
		if (clickedSide == EnumFacing.UP || clickedSide == EnumFacing.DOWN) {
			return clickedSide;
		}

		if (this.isRackLateralSide(rackFacing, clickedSide)) {
			return clickedSide;
		}

		if (clickedSide != rackFacing) {
			return null;
		}

		if (hitY >= 0.70F) {
			return EnumFacing.UP;
		}

		if (hitY <= 0.30F) {
			return EnumFacing.DOWN;
		}

		double localX = this.getLocalX(rackFacing, hitX, hitZ);
		return localX >= 0.5D ? this.rotateClockwise(rackFacing) : this.rotateCounterClockwise(rackFacing);
	}

	private boolean tryPlaceRack(World worldIn, BlockPos placePos, EnumFacing rackFacing, Block rackBlock,
			boolean standing, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem) {
		if (!this.isRackFamily(rackBlock)
				|| placePos.getY() < 0 || placePos.getY() >= worldIn.getHeight()
				|| !worldIn.getWorldBorder().contains(placePos) || !worldIn.isBlockModifiable(playerIn, placePos)
				|| !worldIn.getBlockState(placePos).getBlock().isReplaceable(worldIn, placePos)
				|| !((RackBlock)rackBlock).canRackStay(worldIn, placePos, rackFacing, standing)
				|| !playerIn.canPlayerEdit(placePos, rackFacing, heldItem)) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		}

		IBlockState placedState = rackBlock.getDefaultState()
			.withProperty(FACING, rackFacing)
			.withProperty(CONNECTIONS, Integer.valueOf(0))
			.withProperty(DATA, Boolean.valueOf(false))
			.withProperty(STANDING, Boolean.valueOf(standing));

		if (!worldIn.setBlockState(placePos, placedState, 3)) {
			return false;
		}

		IBlockState actualState = worldIn.getBlockState(placePos);

		if (actualState.getBlock() == rackBlock) {
			ItemBlock.setTileEntityNBT(worldIn, playerIn, placePos, heldItem);
			rackBlock.onBlockPlacedBy(worldIn, placePos, actualState, playerIn, heldItem);
		}

		SoundType soundType = rackBlock.getSoundType(actualState, worldIn, placePos, playerIn);
		worldIn.playSound(playerIn, placePos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
			(soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

		if (!playerIn.capabilities.isCreativeMode) {
			heldItem.stackSize--;

			if (heldItem.stackSize <= 0) {
				playerIn.setHeldItem(hand, null);
			}
		}

		return true;
	}

	protected double getLocalX(EnumFacing facing, float hitX, float hitZ) {
		EnumFacing right = this.rotateClockwise(facing);
		return 0.5D + (hitX - 0.5D) * right.getFrontOffsetX()
			+ (hitZ - 0.5D) * right.getFrontOffsetZ();
	}

	private boolean canRackStay(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		return this.canRackStay(worldIn, pos, state.getValue(FACING),
			state.getValue(STANDING).booleanValue());
	}

	private boolean canRackStay(IBlockAccess worldIn, BlockPos pos, EnumFacing facing, boolean standing) {
		return standing ? this.canStandingRackStay(worldIn, pos, facing)
			: this.canWallRackStay(worldIn, pos, facing);
	}

	private boolean canWallRackStay(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
		BlockPos supportPos = pos.offset(facing.getOpposite());
		return worldIn.getBlockState(supportPos).isSideSolid(worldIn, supportPos, facing);
	}

	private boolean canStandingRackStay(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
		BlockPos supportPos = pos.down();
		IBlockState supportState = worldIn.getBlockState(supportPos);

		if (supportState.isSideSolid(worldIn, supportPos, EnumFacing.UP)) {
			return true;
		}

		return supportState.getBlock() == this
			&& supportState.getValue(FACING) == facing
			&& supportState.getValue(STANDING).booleanValue();
	}

	private int getConnectionMask(IBlockAccess worldIn, BlockPos pos, EnumFacing facing, boolean standing) {
		int connections = 0;

		if (this.connectsTo(worldIn, pos.up(), facing, standing)) {
			connections |= UP;
		}
		if (this.connectsTo(worldIn, pos.offset(this.rotateClockwise(facing)), facing, standing)) {
			connections |= RIGHT;
		}
		if (this.connectsTo(worldIn, pos.down(), facing, standing)) {
			connections |= DOWN;
		}
		if (this.connectsTo(worldIn, pos.offset(this.rotateCounterClockwise(facing)), facing, standing)) {
			connections |= LEFT;
		}

		return connections;
	}

	private boolean connectsTo(IBlockAccess worldIn, BlockPos pos, EnumFacing facing, boolean standing) {
		IBlockState state = worldIn.getBlockState(pos);
		return state.getBlock() == this
			&& state.getValue(FACING) == facing
			&& state.getValue(STANDING).booleanValue() == standing
			&& this.canRackStay(worldIn, pos, facing, standing);
	}

	private void notifyRackAndNeighbors(World worldIn, BlockPos pos) {
		this.notifyRack(worldIn, pos);
		this.notifyRackNeighbors(worldIn, pos);
	}

	protected void notifyRackNeighbors(World worldIn, BlockPos pos) {
		this.notifyRack(worldIn, pos.up());
		this.notifyRack(worldIn, pos.down());

		for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
			this.notifyRack(worldIn, pos.offset(direction));
		}
	}

	private void notifyRack(World worldIn, BlockPos pos) {
		if (worldIn.isRemote) {
			return;
		}

		IBlockState state = worldIn.getBlockState(pos);

		if (state.getBlock() == this) {
			worldIn.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	protected boolean isRackItem(ItemStack heldItem) {
		return heldItem != null && heldItem.stackSize > 0
			&& heldItem.getItem() instanceof ItemBlock
			&& this.isRackFamily(((ItemBlock)heldItem.getItem()).getBlock());
	}

	private boolean isRackLateralSide(EnumFacing rackFacing, EnumFacing clickedSide) {
		return clickedSide == this.rotateClockwise(rackFacing)
			|| clickedSide == this.rotateCounterClockwise(rackFacing);
	}

	protected AxisAlignedBB rotateToFacing(AxisAlignedBB box, EnumFacing facing) {
		return rotateNorthBounds(box, facing);
	}

	private EnumFacing rotateClockwise(EnumFacing facing) {
		switch (facing) {
		case NORTH:
			return EnumFacing.EAST;
		case EAST:
			return EnumFacing.SOUTH;
		case SOUTH:
			return EnumFacing.WEST;
		case WEST:
			return EnumFacing.NORTH;
		default:
			return facing;
		}
	}

	private EnumFacing rotateCounterClockwise(EnumFacing facing) {
		switch (facing) {
		case NORTH:
			return EnumFacing.WEST;
		case WEST:
			return EnumFacing.SOUTH;
		case SOUTH:
			return EnumFacing.EAST;
		case EAST:
			return EnumFacing.NORTH;
		default:
			return facing;
		}
	}

	public abstract boolean isRackFamily(Block block);

	protected abstract double getRackDepth();

	protected <T extends TileEntity> T getRackTile(World world, BlockPos pos, Class<T> type) {
		if (world.getBlockState(pos).getBlock() != this) {
			return null;
		}

		TileEntity tile = world.getTileEntity(pos);
		return type.isInstance(tile) ? type.cast(tile) : null;
	}
}
