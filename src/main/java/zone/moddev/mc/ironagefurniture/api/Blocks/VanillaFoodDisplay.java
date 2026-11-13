package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.Random;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaFoodPlacement;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityVanillaFoodDisplay;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Food-only display, separate from the general furniture reservation block. */
public final class VanillaFoodDisplay extends Block {
	private static VanillaFoodDisplay registeredBlock;

	public VanillaFoodDisplay() {
		super(Material.CIRCUITS);
		this.setHardness(0.0F);
		registeredBlock = this;
	}

	public static boolean place(World world, BlockPos pos, ItemStack stack, EnumFacing facing) {
		if (world.isRemote || registeredBlock == null || !VanillaFoodPlacement.isKnownFood(stack)
				|| world.getBlockState(pos).getBlock() != Blocks.AIR
				|| !VanillaFoodPlacement.isSupport(world, pos.down())) return false;
		if (!world.setBlockState(pos, registeredBlock.getDefaultState(), 3)) return false;
		TileEntityVanillaFoodDisplay tile = tile(world, pos);
		if (tile == null) {
			world.setBlockToAir(pos);
			return false;
		}
		tile.getSurfaceSetting().setSingleItem(stack, facing);
		tile.markSurfaceSettingChanged();
		return true;
	}

	private static TileEntityVanillaFoodDisplay tile(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityVanillaFoodDisplay ? (TileEntityVanillaFoodDisplay)tile : null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntityVanillaFoodDisplay tile = tile(world, pos);
		if (tile == null || player.isSneaking() || held != null && held.stackSize > 0
				|| !VanillaFoodPlacement.mayEdit(world, pos, player, held)) return false;
		if (!world.isRemote) {
			ItemStack removed = tile.getSurfaceSetting().remove(Slot.CENTER);
			world.setBlockToAir(pos);
			if (removed != null && !player.inventory.addItemStackToInventory(removed)) player.dropItem(removed, false);
		}
		return true;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
			EntityPlayer player, boolean willHarvest) {
		if (player.capabilities.isCreativeMode) {
			TileEntityVanillaFoodDisplay tile = tile(world, pos);
			if (tile != null) tile.getSurfaceSetting().clear();
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityVanillaFoodDisplay tile = tile(world, pos);
		if (tile != null) {
			for (Slot slot : Slot.values()) {
				ItemStack stack = tile.getSurfaceSetting().remove(slot);
				if (!world.isRemote && stack != null) Block.spawnAsEntity(world, pos, stack);
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
		if (!world.isRemote && !VanillaFoodPlacement.isSupport(world, pos.down())) world.setBlockToAir(pos);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityVanillaFoodDisplay tile = tile(world, pos);
		AxisAlignedBB bounds = tile == null ? null : tile.getSurfaceSetting().getCombinedBounds();
		return bounds == null ? FULL_BLOCK_AABB : bounds;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		return this.rayTrace(pos, start, end, this.getBoundingBox(state, world, pos));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntityVanillaFoodDisplay tile = tile(world, pos);
		ItemStack stack = tile == null ? null : tile.getSurfaceSetting().getCompatibilityItem();
		return stack == null ? null : stack.copy();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World world, BlockPos pos) { return NULL_AABB; }
	@Override
	public boolean isFullCube(IBlockState state) { return false; }
	@Override
	public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override
	public boolean isReplaceable(IBlockAccess world, BlockPos pos) { return false; }
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) { return EnumPushReaction.DESTROY; }
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.INVISIBLE; }
	@Override
	public int quantityDropped(Random random) { return 0; }
	@Override
	public boolean hasTileEntity(IBlockState state) { return true; }
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) { return new TileEntityVanillaFoodDisplay(); }
}
