package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.List;
import java.util.Random;

import zone.moddev.mc.ironagefurniture.api.surface.FurnitureSurfaceDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBedsideCabinet;
import zone.moddev.mc.ironagefurniture.client.particle.SurfaceDisplayEffects;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BedsideCabinet extends Block {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool LOWER_OPEN = PropertyBool.create("lower_open");
	public static final PropertyBool UPPER_OPEN = PropertyBool.create("upper_open");
	public static final double TOP_HEIGHT = 12.0D / 16;

	private static final AxisAlignedBB BOUNDS = box(1, 0, 1, 15, 12, 15);
	private static final AxisAlignedBB[] FRAME = {
		box(1, 10.5, 1, 15, 12, 15),
		box(2, 2, 2, 14, 3, 14),
		box(2, 3, 2, 3, 10.5, 14),
		box(13, 3, 2, 14, 10.5, 14),
		box(3, 3, 2, 13, 10.5, 3),
		box(3, 6.25, 3, 13, 6.75, 14),
		box(2, 0, 2, 4, 2, 4),
		box(12, 0, 2, 14, 2, 4),
		box(2, 0, 12, 4, 2, 14),
		box(12, 0, 12, 14, 2, 14)
	};
	private static final AxisAlignedBB LOWER_DRAWER = box(3.125, 3.125, 7, 12.875, 6.125, 14.5);
	private static final AxisAlignedBB UPPER_DRAWER = LOWER_DRAWER.offset(0, 3.75D / 16, 0);
	private static final double DRAWER_TRAVEL = 1.5D / 16;
	private static final double DRAWER_SPLIT = 6.5D / 16;

	public BedsideCabinet(Material material, float resistance, float hardness) {
		super(material);
		this.setResistance(resistance);
		this.setHardness(hardness);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH)
			.withProperty(LOWER_OPEN, false).withProperty(UPPER_OPEN, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, LOWER_OPEN, UPPER_OPEN);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		EnumFacing front = placer == null ? EnumFacing.SOUTH : placer.getHorizontalFacing().getOpposite();
		return this.getDefaultState().withProperty(FACING, front);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rotation) {
		return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityBedsideCabinet cabinet = getCabinet(world, pos);
		return state.withProperty(LOWER_OPEN, cabinet != null && cabinet.isDrawerOpen(false))
			.withProperty(UPPER_OPEN, cabinet != null && cabinet.isDrawerOpen(true));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntityBedsideCabinet cabinet = getCabinet(world, pos);
		if (player.isSneaking() || cabinet == null) {
			return false;
		}

		RayTraceResult hit = cabinet.getSurfaceSetting().hasAnyItem()
			? SurfaceItemHit.traceFromPlayer(state, world, pos, player) : null;
		if (SurfaceItemHit.isItemHit(hit)) {
			return SurfaceDisplayBlocker.interact(world, pos, cabinet, player, hand, held,
				(float)(hit.hitVec.xCoord - pos.getX()), (float)(hit.hitVec.zCoord - pos.getZ()));
		}

		if (side == EnumFacing.UP && hitY >= TOP_HEIGHT) {
			if (SurfaceDisplayBlocker.placesAsBlock(held)
					&& held.getItem() != Items.FLOWER_POT && FurnitureSurfaceDisplay.find(held) == null) {
				return false;
			}

			return SurfaceDisplayBlocker.interact(world, pos, cabinet, player, hand, held, hitX, hitZ);
		}

		if (side != state.getValue(FACING) || hitY < 3.0D / 16 || hitY >= 10.5D / 16) {
			return false;
		}

		if (!world.isRemote) {
			player.displayGUIChest(cabinet.getDrawer(hitY >= DRAWER_SPLIT));
		}

		return true;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityBedsideCabinet();
	}

	private static TileEntityBedsideCabinet getCabinet(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityBedsideCabinet ? (TileEntityBedsideCabinet)tile : null;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityBedsideCabinet cabinet = getCabinet(world, pos);
		return cabinet == null ? 0 : cabinet.getDisplayLight();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
		TileEntityBedsideCabinet cabinet = getCabinet(world, pos);
		if (cabinet != null) {
			SurfaceDisplayEffects.tickSurface(world, new Vec3d(pos).addVector(0.5D, TOP_HEIGHT, 0.5D),
				cabinet.getSurfaceSetting(), 1.0D, random);
		}
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState actual = this.getActualState(state, world, pos);
		return BOUNDS.union(drawerBox(actual, false)).union(drawerBox(actual, true));
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB filter,
			List<AxisAlignedBB> collisions, Entity entity) {
		IBlockState actual = this.getActualState(state, world, pos);
		EnumFacing facing = actual.getValue(FACING);

		for (AxisAlignedBB part : FRAME) {
			addCollisionBoxToList(pos, filter, collisions, rotate(part, facing));
		}

		addCollisionBoxToList(pos, filter, collisions, drawerBox(actual, false));
		addCollisionBoxToList(pos, filter, collisions, drawerBox(actual, true));
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		IBlockState actual = this.getActualState(state, world, pos);
		EnumFacing facing = actual.getValue(FACING);
		RayTraceResult hit = this.rayTrace(pos, start, end, drawerBox(actual, false));
		hit = nearest(start, hit, this.rayTrace(pos, start, end, drawerBox(actual, true)));

		for (AxisAlignedBB part : FRAME) {
			hit = nearest(start, hit, this.rayTrace(pos, start, end, rotate(part, facing)));
		}

		return SurfaceItemHit.trace(world, pos, start, end, hit);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		return SurfaceItemHit.selected(world, pos, this.getBoundingBox(state, world, pos).offset(pos));
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (SurfaceItemHit.removeIfHit(world, pos, player)) {
			return false;
		}

		TileEntityBedsideCabinet cabinet = getCabinet(world, pos);
		if (willHarvest && cabinet != null && !cabinet.getUnresolvedItems().hasNoTags()) {
			return true;
		}

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
		TileEntityBedsideCabinet cabinet = getCabinet(world, pos);
		if (cabinet != null && !drops.isEmpty()) {
			NBTTagCompound saved = cabinet.getUnresolvedItems();
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
		if (!world.isRemote) {
			TileEntityBedsideCabinet cabinet = getCabinet(world, pos);
			if (cabinet != null && world.getGameRules().getBoolean("doTileDrops")) {
				SurfaceDisplayBlocker.dropItems(world, pos, cabinet);
				InventoryHelper.dropInventoryItems(world, pos, cabinet.getDrawer(true));
				InventoryHelper.dropInventoryItems(world, pos, cabinet.getDrawer(false));
			} else if (cabinet != null) {
				cabinet.getSurfaceSetting().clear();
			}

			SurfaceDisplayBlocker.release(world, pos);
		}

		super.breakBlock(world, pos, state);
	}

	private static AxisAlignedBB drawerBox(IBlockState state, boolean upper) {
		AxisAlignedBB drawer = upper ? UPPER_DRAWER : LOWER_DRAWER;
		if (state.getValue(upper ? UPPER_OPEN : LOWER_OPEN)) {
			drawer = drawer.offset(0, 0, DRAWER_TRAVEL);
		}

		return rotate(drawer, state.getValue(FACING));
	}

	private static AxisAlignedBB box(double x, double y, double z, double maxX, double maxY, double maxZ) {
		return new AxisAlignedBB(x / 16, y / 16, z / 16, maxX / 16, maxY / 16, maxZ / 16);
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

	private static RayTraceResult nearest(Vec3d start, RayTraceResult first, RayTraceResult second) {
		return second != null && (first == null || start.squareDistanceTo(second.hitVec) < start.squareDistanceTo(first.hitVec))
			? second : first;
	}
}
