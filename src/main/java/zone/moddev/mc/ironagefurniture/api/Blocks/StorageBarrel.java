package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.FluidVesselInteractions;
import zone.moddev.mc.skysfurniturecore.api.vessel.BarrelBulkStorage;
import zone.moddev.mc.skysfurniturecore.api.storage.FurnitureLabel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Craftable barrel for liquids or one kind of solid ingredient. */
public class StorageBarrel extends Barrel {
	public static final PropertyBool OPEN = PropertyBool.create("open");
	public static final double LABEL_SCALE = 0.72D;
	private static final int LABEL_HIT = 1;

	public StorageBarrel(Material material, String name, float resistance, float hardness) {
		super(material, name, resistance, hardness);
		if (this.supportsBulkStorage()) {
			this.setDefaultState(this.blockState.getBaseState().withProperty(OPEN, false));
		}
	}

	public boolean supportsBulkStorage() {
		return true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, OPEN);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (!this.supportsBulkStorage()) {
			return state;
		}

		TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
		return state.withProperty(OPEN, barrel != null && barrel.getBulkStorage().getCount() > 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (FurnitureLabel.isSign(held) || (held == null && isLabelHit(state, world, pos, player))) {
			EnumFacing face = side.getAxis().isHorizontal() ? side : player.getHorizontalFacing().getOpposite();
			if (!world.isRemote) {
				player.openGui(Ironagefurniture.instance, StorageGuiHandler.barrelLabelId(face, hand),
					world, pos.getX(), pos.getY(), pos.getZ());
			}

			return true;
		}

		if (held != null && this.supportsBulkStorage()) {
			TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
			if (barrel != null && BarrelBulkStorage.capacityFor(held) > 0) {
				if (!world.isRemote && world.isBlockModifiable(player, pos)
						&& player.canPlayerEdit(pos, side, held)) {
					int accepted = barrel.getBulkStorage().deposit(held, false);
					if (accepted > 0 && !player.capabilities.isCreativeMode) {
						held.stackSize -= accepted;
						if (held.stackSize == 0) {
							player.setHeldItem(hand, null);
						}
					}
				}

				return true;
			}

			if (barrel != null && barrel.getBulkStorage().getCount() > 0) {
				return true;
			}
		}

		return super.onBlockActivated(world, pos, state, player, hand, held, side, hitX, hitY, hitZ);
	}

	public Vec3d getLabelAnchor(IBlockState state, EnumFacing face) {
		// On a side barrel the curved body sits inside the block at the flanks.
		double distance = this instanceof SideBarrel && state.getValue(SideBarrel.FACING).getAxis() != face.getAxis()
			? 0.5D - 1.92D / 16 + FurnitureLabel.BOARD.maxZ * LABEL_SCALE : 0.5D;
		return new Vec3d(0.5D + face.getFrontOffsetX() * distance, 0.45D, 0.5D + face.getFrontOffsetZ() * distance);
	}

	private AxisAlignedBB getLabelBounds(IBlockState state, EnumFacing face) {
		return FurnitureLabel.bounds(this.getLabelAnchor(state, face), face, LABEL_SCALE);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		RayTraceResult body = super.collisionRayTrace(state, world, pos, start, end);
		TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
		if (barrel == null || barrel.getBarrelLabel().getSign() == null) {
			return body;
		}

		EnumFacing face = barrel.getLabelFacing();
		if ((end.xCoord - start.xCoord) * face.getFrontOffsetX() + (end.zCoord - start.zCoord) * face.getFrontOffsetZ() >= 0) {
			return body;
		}

		RayTraceResult hit = this.getLabelBounds(state, face).offset(pos).calculateIntercept(start, end);
		if (hit == null || (body != null && body.sideHit != face
				&& start.squareDistanceTo(body.hitVec) < start.squareDistanceTo(hit.hitVec))) {
			return body;
		}

		RayTraceResult label = new RayTraceResult(hit.hitVec, face, pos);
		label.subHit = LABEL_HIT;
		return label;
	}

	private static boolean isLabelHit(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
		RayTraceResult hit = SurfaceItemHit.traceFromPlayer(state, world, pos, player);
		return hit != null && hit.subHit == LABEL_HIT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
		if (hit != null && hit.subHit == LABEL_HIT && pos.equals(hit.getBlockPos())) {
			TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
			if (barrel != null && barrel.getBarrelLabel().getSign() != null) {
				return this.getLabelBounds(state, barrel.getLabelFacing()).offset(pos);
			}
		}

		return super.getSelectedBoundingBox(state, world, pos);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (!isLabelHit(state, world, pos, player)) {
			return super.removedByPlayer(state, world, pos, player, willHarvest);
		}

		TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
		if (!world.isRemote && barrel != null && player.capabilities.allowEdit && world.isBlockModifiable(player, pos)) {
			ItemStack sign = barrel.removeLabel();
			if (sign != null && !player.capabilities.isCreativeMode) {
				spawnAsEntity(world, pos, sign);
			}
		}

		// Keep the block intact so Forge does not harvest the barrel as well as the sign.
		return false;
	}

	@Override
	protected boolean openVesselGui(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!world.isRemote) {
			TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
			int id = this.supportsBulkStorage() && barrel != null && barrel.getBulkStorage().getCount() > 0
				? StorageGuiHandler.BULK_BARREL : StorageGuiHandler.BARREL;
			player.openGui(Ironagefurniture.instance, id, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	@Override
	protected boolean isSpecialFluidItem(ItemStack stack) {
		return FluidVesselInteractions.find(stack) != null || (stack != null && FluidUtil.getFluidHandler(stack) != null);
	}

	@Override
	protected boolean trySpecialFluidInteraction(TileEntityBarrel barrel, EntityPlayer player, EnumHand hand, ItemStack stack) {
		FluidVesselInteractions.Handler handler = FluidVesselInteractions.find(stack);

		if (handler != null) {
			handler.interact(barrel, player, hand, stack);
			return true;
		}

		if (hand == EnumHand.MAIN_HAND && FluidUtil.getFluidHandler(stack) != null) {
			FluidUtil.interactWithFluidHandler(stack, barrel.getFluidHandler(), player);
			barrel.markForFluidUpdate();
			return true;
		}

		return false;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
		if (this.supportsBulkStorage() && barrel != null && barrel.getBulkStorage().getCount() > 0) {
			BarrelBulkStorage bulk = barrel.getBulkStorage();
			return MathHelper.floor(bulk.getCount() * 14.0F / bulk.getCapacity()) + 1;
		}

		return super.getComparatorInputOverride(state, world, pos);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && world.getGameRules().getBoolean("doTileDrops")) {
			TileEntityBarrel barrel = this.getBarrelEntity(world, pos);
			if (barrel != null) {
				ItemStack sign = barrel.getBarrelLabel().remove();
				if (sign != null) {
					spawnAsEntity(world, pos, sign);
				}

				BarrelBulkStorage storage = barrel.getBulkStorage();
				for (ItemStack stack = storage.takeDropStack(); stack != null; stack = storage.takeDropStack()) {
					spawnAsEntity(world, pos, stack);
				}
			}
		}

		super.breakBlock(world, pos, state);
	}
}
