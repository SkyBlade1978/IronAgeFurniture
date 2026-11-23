package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.List;

import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityToolRack;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public final class ToolRack extends BlockHBase {
	public static final PropertyInteger CONNECTIONS = PropertyInteger.create("connections", 0, 3);

	private static final int WEST_END = 1;
	private static final int EAST_END = 2;

	public ToolRack() {
		super(Material.WOOD);
		this.setHardness(1.0F);
		this.setResistance(10);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(CONNECTIONS, 0));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, CONNECTIONS);
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
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return side.getAxis().isHorizontal() ? this.getDefaultState().withProperty(FACING, side) : this.getDefaultState();
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			if (hasSupport(world, pos, facing)) {
				return super.canPlaceBlockAt(world, pos);
			}
		}
		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		return side.getAxis().isHorizontal() && hasSupport(world, pos, side) && super.canPlaceBlockAt(world, pos);
	}

	private static boolean hasSupport(IBlockAccess world, BlockPos pos, EnumFacing front) {
		BlockPos behind = pos.offset(front.getOpposite());
		return world.getBlockState(behind).isSideSolid(world, behind, front);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed) {
		EnumFacing facing = state.getValue(FACING);
		if (!world.isRemote && world.isBlockLoaded(pos.offset(facing.getOpposite())) && !hasSupport(world, pos, facing)) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFacing front = state.getValue(FACING);
		int connections = 0;
		if (this.connectsTo(world, pos.offset(front.rotateYCCW()), front)) {
			connections |= WEST_END;
		}
		if (this.connectsTo(world, pos.offset(front.rotateY()), front)) {
			connections |= EAST_END;
		}
		return state.withProperty(CONNECTIONS, connections);
	}

	private boolean connectsTo(IBlockAccess world, BlockPos pos, EnumFacing front) {
		if (world instanceof World && !((World)world).isBlockLoaded(pos)) {
			return false;
		}
		IBlockState neighbor = world.getBlockState(pos);
		return neighbor.getBlock() == this && neighbor.getValue(FACING) == front;
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
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState actual = this.getActualState(state, world, pos);
		int connections = actual.getValue(CONNECTIONS);
		double left = (connections & WEST_END) == 0 ? 1.0D / 16 : 0;
		double right = (connections & EAST_END) == 0 ? 15.0D / 16 : 1;
		return rotateNorthBounds(new AxisAlignedBB(left, 11.0D / 16, 12.5D / 16, right, 13.0D / 16, 1),
			actual.getValue(FACING));
	}

	public static AxisAlignedBB getToolBounds(int slot, ItemStack stack) {
		double centre = (slot == 0 ? 11.5D : 4.5D) / 16;
		if (stack.getItem() == Items.SHIELD) {
			// Half-size vanilla plate, with its back resting against the hook.
			return new AxisAlignedBB(centre - 3.0D / 16, 1.25D / 16, 12.0D / 16,
				centre + 3.0D / 16, 12.25D / 16, 12.5D / 16);
		}

		ShelfLayout layout = SurfaceContentRegistry.describe(stack).getShelfLayout();
		boolean compact = layout == ShelfLayout.COMPACT_TOOL || layout == ShelfLayout.FLINT_AND_STEEL_ROW;
		return new AxisAlignedBB(centre - 3.0D / 16, (compact ? 6.0D : 1.0D) / 16, 11.5D / 16,
			centre + 3.0D / 16, 12.5D / 16, 12.625D / 16);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		RayTraceResult nearest = this.rayTrace(pos, start, end, this.getBoundingBox(state, world, pos));
		TileEntityToolRack rack = getRack(world, pos);
		if (rack == null) {
			return nearest;
		}

		for (int slot = 0; slot < TileEntityToolRack.SLOTS; slot++) {
			ItemStack tool = rack.getTool(slot);
			if (tool == null) {
				continue;
			}
			AxisAlignedBB bounds = rotateNorthBounds(getToolBounds(slot, tool), state.getValue(FACING));
			RayTraceResult hit = this.rayTrace(pos, start, end, bounds);
			if (hit != null && (nearest == null || start.squareDistanceTo(hit.hitVec) < start.squareDistanceTo(nearest.hitVec))) {
				hit.subHit = slot;
				nearest = hit;
			}
		}

		return nearest;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
		if (hit != null && pos.equals(hit.getBlockPos()) && hit.subHit >= 0 && hit.subHit < TileEntityToolRack.SLOTS) {
			TileEntityToolRack rack = getRack(world, pos);
			ItemStack tool = rack == null ? null : rack.getTool(hit.subHit);
			if (tool != null) {
				return rotateNorthBounds(getToolBounds(hit.subHit, tool), state.getValue(FACING)).offset(pos);
			}
		}
		return super.getSelectedBoundingBox(state, world, pos);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() || held != null && !isTool(held)) {
			return false;
		}

		TileEntityToolRack rack = getRack(world, pos);
		if (rack == null) {
			return false;
		}
		if (world.isRemote) {
			return true;
		}

		EnumFacing positive = state.getValue(FACING).rotateY();
		double across = (hitX - 0.5D) * positive.getFrontOffsetX() + (hitZ - 0.5D) * positive.getFrontOffsetZ();
		int slot = across >= 0 ? 0 : 1;
		if (held != null) {
			if (rack.insertTool(slot, held) && !player.capabilities.isCreativeMode && --held.stackSize <= 0) {
				player.setHeldItem(hand, null);
			}
		} else {
			ItemStack tool = rack.getTool(slot);
			if (tool != null && ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(player.inventory), tool.copy(), false) == null) {
				rack.removeTool(slot);
			}
		}
		return true;
	}

	public static boolean isTool(ItemStack stack) {
		if (stack == null || stack.stackSize <= 0 || stack.getItem() == null) {
			return false;
		}
		Item item = stack.getItem();
		ShelfLayout layout = SurfaceContentRegistry.describe(stack).getShelfLayout();
		return layout == ShelfLayout.TOOL || layout == ShelfLayout.COMPACT_TOOL || layout == ShelfLayout.FLINT_AND_STEEL_ROW
			|| item == Items.SHIELD || item instanceof ItemTool || item instanceof ItemHoe || item instanceof ItemSword
			|| !item.getToolClasses(stack).isEmpty();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityToolRack();
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
		TileEntityToolRack rack = getRack(world, pos);
		if (rack != null && !drops.isEmpty()) {
			NBTTagCompound unresolved = rack.getUnresolvedTools();
			if (!unresolved.hasNoTags()) {
				drops.get(0).setTagInfo("BlockEntityTag", unresolved);
			}
		}
		return drops;
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
		TileEntityToolRack rack = getRack(world, pos);
		if (!world.isRemote && rack != null && world.getGameRules().getBoolean("doTileDrops")) {
			for (int slot = 0; slot < TileEntityToolRack.SLOTS; slot++) {
				ItemStack stack = rack.getTool(slot);
				if (stack != null) {
					spawnAsEntity(world, pos, stack);
				}
			}
		}
		super.breakBlock(world, pos, state);
	}

	private static TileEntityToolRack getRack(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
			: world.getTileEntity(pos);
		return tile instanceof TileEntityToolRack ? (TileEntityToolRack)tile : null;
	}
}
