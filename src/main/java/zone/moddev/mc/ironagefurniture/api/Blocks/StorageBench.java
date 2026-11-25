package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.Collections;
import java.util.List;

import zone.moddev.mc.ironagefurniture.api.PaddedBenchColourHelper;
import zone.moddev.mc.skysfurniturecore.api.seating.BenchType;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockStorageBench;
import zone.moddev.mc.ironagefurniture.api.Properties.PaddedBenchColourProperty;
import zone.moddev.mc.ironagefurniture.api.entity.Seat;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityStorageBench;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public final class StorageBench extends Chair {
	public static final PropertyBool PADDED = PropertyBool.create("padded");
	public static final PropertyEnum<BenchType> TYPE = PropertyEnum.create("type", BenchType.class, type -> type != BenchType.MIDDLE);

	private static final AxisAlignedBB WOOD_BOUNDS = new AxisAlignedBB(1.0D / 16, 0, 1.0D / 16, 15.0D / 16, 0.5D, 15.0D / 16);
	private static final AxisAlignedBB PADDED_BOUNDS = WOOD_BOUNDS.setMaxY(9.0D / 16);
	private static final double PANEL_BOTTOM = 2.0D / 16;
	private static final double PANEL_TOP = 7.0D / 16;

	public StorageBench(String name) {
		super(Material.WOOD, name, 10, 1.5F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH)
			.withProperty(PADDED, false).withProperty(TYPE, BenchType.SINGLE));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty<?>[] { FACING, PADDED, TYPE },
			new IUnlistedProperty<?>[] { PaddedBenchColourProperty.COLOUR });
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		EnumFacing front = placer == null ? EnumFacing.SOUTH : placer.getHorizontalFacing().getOpposite();
		return this.getDefaultState().withProperty(FACING, front).withProperty(PADDED, ItemBlockStorageBench.isPadded(stack));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(PADDED, (meta & 4) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | (state.getValue(PADDED) ? 4 : 0);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityStorageBench bench = getBench(world, pos);
		TileEntityStorageBench partner = getPartner(world, pos, state, bench);
		return state.withProperty(TYPE, partner == null ? BenchType.SINGLE : bench.getPair());
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(PADDED) ? PaddedBenchColourHelper.withRenderColour(state, world, pos) : state;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			return false;
		}

		if (side == state.getValue(FACING) && hitY >= PANEL_BOTTOM && hitY < PANEL_TOP) {
			if (!world.isRemote) {
				TileEntityStorageBench bench = getBench(world, pos);
				if (bench != null) {
					player.displayGUIChest(bench.getAccessibleInventory());
				}
			}
			return true;
		}

		if (side == EnumFacing.UP || side.getAxis().isHorizontal() && hitY >= PANEL_TOP) {
			if (!world.isRemote && !player.isRiding()) {
				this.sit(world, pos, state, player);
			}
			return true;
		}

		return false;
	}

	private void sit(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		for (Seat seat : world.getEntitiesWithinAABB(Seat.class, new AxisAlignedBB(pos))) {
			if (!seat.SeatCoordinates.Match(pos.getX(), pos.getY(), pos.getZ())) {
				continue;
			}
			if (!seat.isBeingRidden()) {
				player.startRiding(seat);
			}
			return;
		}

		double seatHeight = state.getValue(PADDED) ? 6.0D / 16 : 5.0D / 16;
		Seat seat = new Seat(world, pos.getX(), pos.getY(), pos.getZ(), seatHeight);
		if (world.spawnEntity(seat)) {
			if (!player.startRiding(seat)) {
				seat.setDead();
			}
			world.updateComparatorOutputLevel(pos, this);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		AxisAlignedBB bounds = state.getValue(PADDED) ? PADDED_BOUNDS : WOOD_BOUNDS;
		BenchType type = this.getActualState(state, world, pos).getValue(TYPE);

		if (type == BenchType.SINGLE) {
			return bounds;
		}

		EnumFacing joined = connection(state.getValue(FACING), type);
		return bounds.union(bounds.offset(joined.getFrontOffsetX() / 16.0D, 0, joined.getFrontOffsetZ() / 16.0D));
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB filter,
			List<AxisAlignedBB> collisions, Entity entity) {
		if (!(entity instanceof Seat)) {
			addCollisionBoxToList(pos, filter, collisions, this.getBoundingBox(state, world, pos));
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityStorageBench();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (world.isRemote) {
			return;
		}

		TileEntityStorageBench bench = getBench(world, pos);
		if (bench == null) {
			return;
		}

		if (state.getValue(PADDED) && ItemBlockStorageBench.isPadded(stack)) {
			bench.setColour(ItemBlockStorageBench.getColour(stack));
		}

		if (isAvailable(world, pos, state, bench) && !this.tryPair(world, pos, state, bench, BenchType.RIGHT)) {
			this.tryPair(world, pos, state, bench, BenchType.LEFT);
		}
	}

	private boolean tryPair(World world, BlockPos pos, IBlockState state, TileEntityStorageBench bench, BenchType type) {
		BlockPos next = pos.offset(connection(state.getValue(FACING), type));
		if (!world.isBlockLoaded(next)) {
			return false;
		}

		IBlockState otherState = world.getBlockState(next);
		TileEntityStorageBench other = getBench(world, next);
		if (!matches(state, otherState, bench, other) || !isAvailable(world, next, otherState, other)) {
			return false;
		}

		bench.setPair(type);
		other.setPair(opposite(type));
		return true;
	}

	private static boolean isAvailable(World world, BlockPos pos, IBlockState state, TileEntityStorageBench bench) {
		BenchType type = bench.getPair();
		return type == BenchType.SINGLE || world.isBlockLoaded(pos.offset(connection(state.getValue(FACING), type)))
			&& getPartner(world, pos, state, bench) == null;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed) {
		if (world.isRemote) {
			return;
		}

		TileEntityStorageBench bench = getBench(world, pos);
		if (bench != null && bench.getPair() != BenchType.SINGLE && isAvailable(world, pos, state, bench)) {
			bench.setPair(BenchType.SINGLE);
		}
	}

	public static TileEntityStorageBench getPartner(IBlockAccess world, BlockPos pos, IBlockState state, TileEntityStorageBench bench) {
		if (!(state.getBlock() instanceof StorageBench) || bench == null || bench.getPair() == BenchType.SINGLE) {
			return null;
		}

		BlockPos next = pos.offset(connection(state.getValue(FACING), bench.getPair()));
		if (world instanceof World && !((World)world).isBlockLoaded(next)) {
			return null;
		}

		IBlockState otherState = world.getBlockState(next);
		TileEntityStorageBench other = getBench(world, next);
		return matches(state, otherState, bench, other) && other.getPair() == opposite(bench.getPair()) ? other : null;
	}

	private static boolean matches(IBlockState state, IBlockState otherState, TileEntityStorageBench bench, TileEntityStorageBench other) {
		return other != null && state.getBlock() == otherState.getBlock() && state.getValue(FACING) == otherState.getValue(FACING)
			&& state.getValue(PADDED).equals(otherState.getValue(PADDED)) && (!state.getValue(PADDED) || bench.getColour() == other.getColour());
	}

	private static EnumFacing connection(EnumFacing front, BenchType type) {
		return type == BenchType.LEFT ? front.rotateYCCW() : front.rotateY();
	}

	private static BenchType opposite(BenchType type) {
		return type == BenchType.LEFT ? BenchType.RIGHT : BenchType.LEFT;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult hit, World world, BlockPos pos, EntityPlayer player) {
		return state.getValue(PADDED) ? ItemBlockStorageBench.paddedStack(this, PaddedBenchColourHelper.getColour(world, pos))
			: new ItemStack(this);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ItemStack drop = state.getValue(PADDED) ? ItemBlockStorageBench.paddedStack(this, PaddedBenchColourHelper.getColour(world, pos))
			: new ItemStack(this);
		TileEntityStorageBench bench = getBench(world, pos);
		if (bench != null) {
			NBTTagCompound raw = bench.getUnresolvedItems();
			if (!raw.hasNoTags()) {
				drop.setTagInfo("BlockEntityTag", raw);
			}
		}
		return Collections.singletonList(drop);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (!world.isRemote && willHarvest) {
			this.onBlockHarvested(world, pos, state, player);
			return true;
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack tool) {
		try {
			super.harvestBlock(world, player, pos, state, tile, tool);
		} finally {
			if (world.getBlockState(pos).getBlock() == this) {
				world.setBlockToAir(pos);
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityStorageBench bench = getBench(world, pos);
		if (!world.isRemote && bench != null) {
			TileEntityStorageBench partner = getPartner(world, pos, state, bench);
			if (partner != null) {
				partner.setPair(BenchType.SINGLE);
			}

			if (world.getGameRules().getBoolean("doTileDrops")) {
				InventoryHelper.dropInventoryItems(world, pos, bench.getInventory());
			}
		}
		super.breakBlock(world, pos, state);
	}

	private static TileEntityStorageBench getBench(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
			: world.getTileEntity(pos);
		return tile instanceof TileEntityStorageBench ? (TileEntityStorageBench)tile : null;
	}
}
