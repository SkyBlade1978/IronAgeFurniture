package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.tile.FurnitureLabel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StorageCrate extends Block {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool SEALED = PropertyBool.create("sealed");
	public static final double LABEL_SCALE = 0.78D;
	private static final int LABEL_HIT = 1;
	private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(1.0D / 16, 0, 1.0D / 16, 15.0D / 16, 1, 15.0D / 16);

	public StorageCrate() {
		super(Material.WOOD);
		this.setHardness(1.5F);
		this.setResistance(10);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH).withProperty(SEALED, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, SEALED);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing face, float x, float y,
			float z, int meta, EntityLivingBase placer, ItemStack stack) {
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
		TileEntityCrate crate = findCrate(world, pos);
		return state.withProperty(SEALED, crate != null && crate.isSealed());
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityCrate();
	}

	private static TileEntityCrate findCrate(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityCrate ? (TileEntityCrate)tile : null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntityCrate crate = findCrate(world, pos);
		if (side != state.getValue(FACING) || crate == null) {
			return false;
		}

		boolean canEdit = !player.isSpectator() && player.capabilities.allowEdit && world.isBlockModifiable(player, pos);
		if (player.isSneaking()) {
			float width = side.getAxis() == EnumFacing.Axis.X ? hitZ : hitX;
			if (held != null || width < 5.0F / 16 || width > 11.0F / 16 || hitY < 11.0F / 16 || hitY > 15.75F / 16) {
				return false;
			}
			if (!world.isRemote && canEdit) {
				crate.setSealed(!crate.isSealed());
				world.playSound(null, pos, crate.isSealed() ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE
					: SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, 1.1F);
			}
			return true;
		}

		if (FurnitureLabel.isSign(held) || held == null && isLabelHit(state, world, pos, player)) {
			if (!world.isRemote && canEdit) {
				player.openGui(Ironagefurniture.instance, StorageGuiHandler.crateLabelId(hand), world,
					pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}

		if (held == null && hand == EnumHand.MAIN_HAND && FurnitureLabel.isSign(player.getHeldItemOffhand())) {
			return false;
		}
		if (!world.isRemote && crate.canOpen(player)) {
			player.openGui(Ironagefurniture.instance, StorageGuiHandler.CRATE, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	public static Vec3d labelAnchor(EnumFacing face) {
		double offset = 6.75D / 16;
		return new Vec3d(0.5D + face.getFrontOffsetX() * offset, 6.5D / 16,
			0.5D + face.getFrontOffsetZ() * offset);
	}

	private static boolean isLabelHit(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
		RayTraceResult hit = SurfaceItemHit.traceFromPlayer(state, world, pos, player);
		return hit != null && hit.subHit == LABEL_HIT;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		RayTraceResult body = super.collisionRayTrace(state, world, pos, start, end);
		TileEntityCrate crate = findCrate(world, pos);
		if (crate == null || crate.getCrateLabel().getSign() == null) {
			return body;
		}

		EnumFacing face = state.getValue(FACING);
		if ((end.xCoord - start.xCoord) * face.getFrontOffsetX() + (end.zCoord - start.zCoord) * face.getFrontOffsetZ() >= 0) {
			return body;
		}
		RayTraceResult hit = FurnitureLabel.bounds(labelAnchor(face), face, LABEL_SCALE).offset(pos).calculateIntercept(start, end);
		if (hit == null || (body != null && body.sideHit != face
				&& start.squareDistanceTo(body.hitVec) < start.squareDistanceTo(hit.hitVec))) {
			return body;
		}
		RayTraceResult label = new RayTraceResult(hit.hitVec, face, pos);
		label.subHit = LABEL_HIT;
		return label;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
		if (hit != null && hit.subHit == LABEL_HIT && pos.equals(hit.getBlockPos())) {
			return FurnitureLabel.bounds(labelAnchor(state.getValue(FACING)), state.getValue(FACING), LABEL_SCALE).offset(pos);
		}
		return super.getSelectedBoundingBox(state, world, pos);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		TileEntityCrate crate = findCrate(world, pos);
		if (crate != null && isLabelHit(state, world, pos, player)) {
			if (!world.isRemote && player.capabilities.allowEdit && world.isBlockModifiable(player, pos)) {
				ItemStack sign = crate.removeLabel();
				if (sign != null && !player.capabilities.isCreativeMode) {
					spawnAsEntity(world, pos, sign);
				}
			}
			return false;
		}
		if (willHarvest) {
			return true; // Keep the tile until the packed crate item has been created.
		}
		if (!world.isRemote && crate != null && !crate.writeDropData().hasNoTags()) {
			spawnAsEntity(world, pos, this.makeDrop(crate));
		}
		return super.removedByPlayer(state, world, pos, player, false);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack tool) {
		super.harvestBlock(world, player, pos, state, tile, tool);
		if (world.getBlockState(pos).getBlock() == this) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> drops = new ArrayList<>();
		drops.add(this.makeDrop(findCrate(world, pos)));
		return drops;
	}

	private ItemStack makeDrop(TileEntityCrate crate) {
		ItemStack stack = new ItemStack(this);
		if (crate != null) {
			NBTTagCompound packed = crate.writeDropData();
			if (!packed.hasNoTags()) {
				stack.setTagInfo("BlockEntityTag", packed);
			}
		}
		return stack;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityCrate crate = findCrate(world, pos);
		if (!world.isRemote && crate != null && !crate.isSealed() && world.getGameRules().getBoolean("doTileDrops")) {
			InventoryHelper.dropInventoryItems(world, pos, crate.getInventory());
			ItemStack sign = crate.getCrateLabel().remove();
			if (sign != null) {
				spawnAsEntity(world, pos, sign);
			}
		}
		super.breakBlock(world, pos, state);
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
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BOUNDS;
	}
}
