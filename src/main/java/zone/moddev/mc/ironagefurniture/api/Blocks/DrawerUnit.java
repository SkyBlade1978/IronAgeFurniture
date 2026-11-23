package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.List;

import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDrawerUnit;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class DrawerUnit extends Block {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyEnum<Pair> PAIR = PropertyEnum.create("pair", Pair.class);
	public static final PropertyBool ABOVE = PropertyBool.create("above");
	public static final PropertyBool BELOW = PropertyBool.create("below");
	public static final PropertyBool LOWER_OPEN = PropertyBool.create("lower_open");
	public static final PropertyBool UPPER_OPEN = PropertyBool.create("upper_open");

	private final boolean halfDepth;

	public DrawerUnit(boolean halfDepth) {
		super(Material.WOOD);
		this.halfDepth = halfDepth;
		this.setResistance(10);
		this.setHardness(1.5F);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH)
			.withProperty(PAIR, Pair.SINGLE).withProperty(ABOVE, false).withProperty(BELOW, false)
			.withProperty(LOWER_OPEN, false).withProperty(UPPER_OPEN, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, PAIR, ABOVE, BELOW, LOWER_OPEN, UPPER_OPEN);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		EnumFacing front = placer == null ? EnumFacing.SOUTH : placer.getHorizontalFacing().getOpposite();
		return this.getDefaultState().withProperty(FACING, front);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		Pair pair = meta >> 2 == 1 ? Pair.LEFT : meta >> 2 == 2 ? Pair.RIGHT : Pair.SINGLE;
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(PAIR, pair);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | (state.getValue(PAIR).ordinal() << 2);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rotation) {
		return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return mirror == Mirror.NONE ? state : state.withProperty(FACING, mirror.mirror(state.getValue(FACING)))
			.withProperty(PAIR, state.getValue(PAIR).opposite());
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && state.getValue(PAIR) == Pair.SINGLE) {
			EnumFacing front = state.getValue(FACING);

			if (!this.tryPair(world, pos, state, front.rotateY(), Pair.RIGHT)) {
				this.tryPair(world, pos, state, front.rotateYCCW(), Pair.LEFT);
			}
		}
	}

	private boolean tryPair(World world, BlockPos pos, IBlockState state, EnumFacing direction, Pair side) {
		BlockPos next = pos.offset(direction);
		if (!world.isBlockLoaded(next)) {
			return false;
		}

		IBlockState neighbor = world.getBlockState(next);
		if (!matches(state, neighbor) || neighbor.getValue(PAIR) != Pair.SINGLE) {
			return false;
		}

		// Each block owns its own items. Only the shape and GUI depend on the pair.
		world.setBlockState(next, neighbor.withProperty(PAIR, side.opposite()), 2);
		world.setBlockState(pos, state.withProperty(PAIR, side), 2);
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed) {
		BlockPos next = partnerPosition(pos, state);
		if (!world.isRemote && next != null && world.isBlockLoaded(next)
				&& this.getPartner(world, pos, state) == null) {
			world.setBlockState(pos, state.withProperty(PAIR, Pair.SINGLE), 2);
		}
	}

	private static boolean matches(IBlockState state, IBlockState other) {
		return other.getBlock() == state.getBlock() && other.getValue(FACING) == state.getValue(FACING);
	}

	private static BlockPos partnerPosition(BlockPos pos, IBlockState state) {
		EnumFacing front = state.getValue(FACING);

		switch (state.getValue(PAIR)) {
		case LEFT:
			return pos.offset(front.rotateYCCW());
		case RIGHT:
			return pos.offset(front.rotateY());
		default:
			return null;
		}
	}

	private BlockPos getPartner(IBlockAccess world, BlockPos pos, IBlockState state) {
		BlockPos next = partnerPosition(pos, state);
		if (next == null || world instanceof World && !((World)world).isBlockLoaded(next)) {
			return null;
		}

		IBlockState neighbor = world.getBlockState(next);
		return matches(state, neighbor) && neighbor.getValue(PAIR) == state.getValue(PAIR).opposite() ? next : null;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityDrawerUnit unit = getUnit(world, pos);
		return state.withProperty(PAIR, this.getPartner(world, pos, state) == null ? Pair.SINGLE : state.getValue(PAIR))
			.withProperty(ABOVE, matches(state, world.getBlockState(pos.up())))
			.withProperty(BELOW, matches(state, world.getBlockState(pos.down())))
			.withProperty(UPPER_OPEN, unit != null && unit.isDrawerOpen(true))
			.withProperty(LOWER_OPEN, unit != null && unit.isDrawerOpen(false));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() || side != state.getValue(FACING)) {
			return false;
		}

		TileEntityDrawerUnit unit = getUnit(world, pos);
		if (unit == null) {
			return false;
		}

		if (!world.isRemote) {
			player.displayGUIChest(this.getDrawer(world, pos, state, unit, hitY >= 0.5F));
		}

		return true;
	}

	private IInventory getDrawer(World world, BlockPos pos, IBlockState state, TileEntityDrawerUnit unit, boolean upper) {
		BlockPos next = this.getPartner(world, pos, state);
		TileEntityDrawerUnit partner = next == null ? null : getUnit(world, next);

		if (partner == null) {
			return unit.getDrawer(upper);
		}

		return state.getValue(PAIR) == Pair.LEFT ? unit.getWideDrawer(upper, partner) : partner.getWideDrawer(upper, unit);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityDrawerUnit();
	}

	private static TileEntityDrawerUnit getUnit(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityDrawerUnit ? (TileEntityDrawerUnit)tile : null;
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
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return !this.halfDepth || side == state.getValue(FACING).getOpposite();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState actual = this.getActualState(state, world, pos);
		AxisAlignedBB bounds = this.frameBounds(actual);

		if (actual.getValue(LOWER_OPEN)) {
			bounds = bounds.union(this.extensionBounds(actual, false));
		}
		if (actual.getValue(UPPER_OPEN)) {
			bounds = bounds.union(this.extensionBounds(actual, true));
		}

		return bounds;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB filter,
			List<AxisAlignedBB> collisions, Entity entity) {
		IBlockState actual = this.getActualState(state, world, pos);
		addCollisionBoxToList(pos, filter, collisions, this.frameBounds(actual));

		if (actual.getValue(LOWER_OPEN)) {
			addCollisionBoxToList(pos, filter, collisions, this.extensionBounds(actual, false));
		}
		if (actual.getValue(UPPER_OPEN)) {
			addCollisionBoxToList(pos, filter, collisions, this.extensionBounds(actual, true));
		}
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		IBlockState actual = this.getActualState(state, world, pos);
		RayTraceResult nearest = this.rayTrace(pos, start, end, this.frameBounds(actual));

		if (actual.getValue(LOWER_OPEN)) {
			nearest = nearer(start, nearest, this.rayTrace(pos, start, end, this.extensionBounds(actual, false)));
		}
		if (actual.getValue(UPPER_OPEN)) {
			nearest = nearer(start, nearest, this.rayTrace(pos, start, end, this.extensionBounds(actual, true)));
		}

		return nearest;
	}

	private static RayTraceResult nearer(Vec3d start, RayTraceResult first, RayTraceResult second) {
		return second != null && (first == null || start.squareDistanceTo(second.hitVec) < start.squareDistanceTo(first.hitVec))
			? second : first;
	}

	private AxisAlignedBB frameBounds(IBlockState state) {
		return rotate(new AxisAlignedBB(0, 0, 0, 1, 1, this.halfDepth ? 0.5D : 1), state.getValue(FACING));
	}

	private AxisAlignedBB extensionBounds(IBlockState state, boolean upper) {
		double front = this.halfDepth ? 0.5D : 1;
		Pair pair = state.getValue(PAIR);
		double level = upper ? 8 : 0;
		double bottom = level + 1.125D;
		double top = level + 6.875D;
		double left = pair == Pair.RIGHT ? 0 : 1.125D;
		double right = pair == Pair.LEFT ? 16 : 14.875D;
		return rotate(new AxisAlignedBB(left / 16, bottom / 16, front, right / 16, top / 16, front + 2.0D / 16),
			state.getValue(FACING));
	}

	private static AxisAlignedBB rotate(AxisAlignedBB box, EnumFacing facing) {
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

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		TileEntityDrawerUnit unit = getUnit(world, pos);
		if (willHarvest && unit != null && !unit.getUnresolvedItems().hasNoTags()) {
			return true;
		}

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
		TileEntityDrawerUnit unit = getUnit(world, pos);
		if (unit != null && !drops.isEmpty()) {
			NBTTagCompound saved = unit.getUnresolvedItems();
			if (!saved.hasNoTags()) {
				drops.get(0).setTagInfo("BlockEntityTag", saved);
			}
		}

		return drops;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack tool) {
		super.harvestBlock(world, player, pos, state, tile, tool);
		if (world.getBlockState(pos).getBlock() == this) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityDrawerUnit unit = getUnit(world, pos);

		if (unit != null && !world.isRemote && world.getGameRules().getBoolean("doTileDrops")) {
			InventoryHelper.dropInventoryItems(world, pos, unit.getDrawer(true));
			InventoryHelper.dropInventoryItems(world, pos, unit.getDrawer(false));
		}

		super.breakBlock(world, pos, state);
	}

	public enum Pair implements IStringSerializable {
		SINGLE, LEFT, RIGHT;

		Pair opposite() {
			return this == LEFT ? RIGHT : this == RIGHT ? LEFT : SINGLE;
		}

		@Override
		public String getName() {
			return this == LEFT ? "left" : this == RIGHT ? "right" : "single";
		}
	}
}
