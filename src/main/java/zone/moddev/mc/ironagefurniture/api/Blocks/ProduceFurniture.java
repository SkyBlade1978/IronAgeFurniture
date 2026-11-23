package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.Collections;
import java.util.List;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.tile.ProduceTray;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityProduceFurniture;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

/** Floor trays and market columns share portable tray data and interaction rules. */
public final class ProduceFurniture extends BlockHBase {
	public static final PropertyBool SECOND = PropertyBool.create("second");
	public static final String TRAY_ID = "produce_tray";
	public static final String RACK_ID = "produce_rack";

	private static final AxisAlignedBB TRAY_BOUNDS = new AxisAlignedBB(-7.0D / 16, 0, -7.0D / 16, 7.0D / 16, 0.5D, 7.0D / 16);
	private static final AxisAlignedBB RACK_BOUNDS = new AxisAlignedBB(0.125D / 16, 0, 0.25D / 16,
		15.875D / 16, 1, 15.75D / 16);
	private static final AxisAlignedBB EMPTY_SOCKET = new AxisAlignedBB(-7.0D / 16, -0.0625D, -7.0D / 16, 7.0D / 16, 0.125D, 7.0D / 16);
	private static final double[] RACK_HEIGHTS = { 2.5D / 16, 13.0D / 16, 23.5D / 16 };

	private final boolean rack;

	public ProduceFurniture(boolean rack) {
		super(Material.WOOD);
		this.rack = rack;
		this.setHardness(1.5F);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(SECOND, false));
	}

	public boolean isRack() {
		return this.rack;
	}

	public int getTrayCount(IBlockState state) {
		return this.rack ? 3 : state.getValue(SECOND) ? 2 : 1;
	}

	public double tierHeight(int slot) {
		return this.rack ? RACK_HEIGHTS[slot] : 0.5D * slot;
	}

	public double tierPitch(int slot) {
		return this.rack && slot < 2 ? -10.0D : 0;
	}

	public static float yaw(EnumFacing facing) {
		return 180 - facing.getHorizontalAngle();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, SECOND);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(SECOND, (meta & 4) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | (state.getValue(SECOND) ? 4 : 0);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase player, ItemStack stack) {
		return this.getDefaultState().withProperty(FACING, player.getHorizontalFacing().getOpposite());
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return this.hasSupport(world, pos) && (!this.rack || pos.getY() < world.getHeight() - 1
			&& world.isBlockLoaded(pos.up()) && world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up()))
			&& super.canPlaceBlockAt(world, pos);
	}

	private boolean hasSupport(World world, BlockPos pos) {
		BlockPos below = pos.down();
		return world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed) {
		if (!world.isRemote && (!this.rack || !state.getValue(SECOND)) && !this.hasSupport(world, pos)) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return !this.rack && state.getValue(SECOND) && side == EnumFacing.UP;
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
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (this.rack) {
			return rotateNorthBounds(RACK_BOUNDS, state.getValue(FACING));
		}

		double height = state.getValue(SECOND) ? 1.0D : 0.5D;
		return new AxisAlignedBB(1.0D / 16, 0, 1.0D / 16, 15.0D / 16, height, 15.0D / 16);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityProduceFurniture();
	}

	private static TileEntityProduceFurniture tileAt(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityProduceFurniture ? (TileEntityProduceFurniture)tile : null;
	}

	public TileEntityProduceFurniture getOwner(IBlockAccess world, BlockPos pos, IBlockState state) {
		if (!this.rack || !state.getValue(SECOND)) {
			return tileAt(world, pos);
		}
		return this.isRackPart(world.getBlockState(pos.down()), state, false) ? tileAt(world, pos.down()) : null;
	}

	private boolean isRackPart(IBlockState other, IBlockState state, boolean upper) {
		return other.getBlock() == this && other.getValue(SECOND) == upper && other.getValue(FACING) == state.getValue(FACING);
	}

	public boolean connectsTo(IBlockAccess world, BlockPos pos, IBlockState state, String wood) {
		IBlockState other = world.getBlockState(pos);
		if (!this.isRackPart(other, state, false)) {
			return false;
		}
		TileEntityProduceFurniture tile = tileAt(world, pos);
		return tile != null && wood.equals(tile.getWood());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntityProduceFurniture owner = this.getOwner(world, pos, state);
		if (owner == null) {
			return false;
		}
		boolean upperRack = this.rack && state.getValue(SECOND);
		RayTraceResult hit = SurfaceItemHit.traceFromPlayer(state, world, pos, player);
		double hitHeight = hitY + (upperRack ? 1 : 0);
		int slot = hit != null && hit.subHit >= 0 ? hit.subHit : this.slotAtHeight(state, hitHeight);
		ProduceTray tray = owner.getTray(slot);
		if (ItemBlockProduceFurniture.isTray(held)) {
			if (!this.rack) {
				if (state.getValue(SECOND) || side != EnumFacing.UP || owner.getTray(0) == null) {
					return false;
				}
				slot = 1;
			} else if (tray != null) {
				return true;
			}
			if (!world.isRemote && world.isBlockModifiable(player, pos) && player.canPlayerEdit(pos, side, held)) {
				ProduceTray incoming = ItemBlockProduceFurniture.unpack(held);
				if (this.rack || world.setBlockState(pos, state.withProperty(SECOND, true), 2)) {
					owner.setTray(slot, incoming);
					if (incoming.isPacked() || !player.capabilities.isCreativeMode) {
						consume(player, hand, held);
					}
				}
			}
			return true;
		}

		if (tray == null || held != null && ProduceTray.capacity(held) == 0) {
			return false;
		}
		if (!world.isRemote && world.isBlockModifiable(player, pos)) {
			if (held != null) {
				if (tray.offer(held, false)) {
					if (!player.capabilities.isCreativeMode) {
						consume(player, hand, held);
					}
					owner.changed();
				}
			} else if (player.isSneaking()) {
				this.liftTray(world, pos, state, owner, slot, player);
			} else {
				ItemStack one = tray.peekOne();
				if (one != null && give(player, one)) {
					tray.removeOne();
					owner.changed();
				}
			}
		}
		return true;
	}

	private int slotAtHeight(IBlockState state, double height) {
		if (this.rack) {
			return height >= RACK_HEIGHTS[2] - 0.0625D ? 2 : height >= 0.75D ? 1 : 0;
		}
		return state.getValue(SECOND) && height >= 0.5D ? 1 : 0;
	}

	private void liftTray(World world, BlockPos pos, IBlockState state, TileEntityProduceFurniture owner,
			int slot, EntityPlayer player) {
		if (!this.rack) {
			if (world.getBlockState(pos.up()).getBlock() == this) {
				return;
			}
			slot = state.getValue(SECOND) ? 1 : 0;
		}
		ProduceTray tray = owner.getTray(slot);
		if (tray == null || !give(player, ItemBlockProduceFurniture.pack(Ironagefurniture.BlockRegistry.get(TRAY_ID), tray))) {
			return;
		}

		owner.setTray(slot, null);
		if (!this.rack) {
			if (slot == 1) {
				world.setBlockState(pos, state.withProperty(SECOND, false), 2);
			} else {
				world.setBlockToAir(pos);
			}
		}
	}

	private static boolean give(EntityPlayer player, ItemStack stack) {
		return ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(player.inventory), stack, false) == null;
	}

	private static void consume(EntityPlayer player, EnumHand hand, ItemStack stack) {
		if (--stack.stackSize == 0) {
			player.setHeldItem(hand, null);
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntityProduceFurniture owner = this.getOwner(world, pos, state);
		String wood = ProduceTray.DEFAULT_WOOD;
		if (owner != null) {
			if (this.rack) {
				wood = owner.getWood();
			} else {
				int slot = target.subHit == 1 && state.getValue(SECOND) ? 1 : 0;
				ProduceTray tray = owner.getTray(slot);
				if (tray != null) {
					wood = tray.getWood();
				}
			}
		}
		return ItemBlockProduceFurniture.empty(this, wood, 1);
	}

	private AxisAlignedBB getSlotBounds(TileEntityProduceFurniture owner, int slot) {
		return !this.rack || owner != null && owner.getTray(slot) != null ? TRAY_BOUNDS : EMPTY_SOCKET;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
		if (hit == null || !pos.equals(hit.getBlockPos()) || hit.subHit < 0 || hit.subHit >= this.getTrayCount(state)) {
			return super.getSelectedBoundingBox(state, world, pos);
		}

		int slot = hit.subHit;
		AxisAlignedBB bounds = this.getSlotBounds(this.getOwner(world, pos, state), slot);
		BlockPos base = this.rack && state.getValue(SECOND) ? pos.down() : pos;
		float pitch = (float)Math.toRadians(-this.tierPitch(slot));
		float yaw = (float)Math.toRadians(yaw(state.getValue(FACING)));
		AxisAlignedBB outline = null;
		for (double x : new double[] { bounds.minX, bounds.maxX }) {
			for (double y : new double[] { bounds.minY, bounds.maxY }) {
				for (double z : new double[] { bounds.minZ, bounds.maxZ }) {
					Vec3d point = new Vec3d(x, y, z).rotatePitch(pitch).rotateYaw(yaw)
						.addVector(base.getX() + 0.5D, base.getY() + this.tierHeight(slot), base.getZ() + 0.5D);
					AxisAlignedBB corner = new AxisAlignedBB(point.xCoord, point.yCoord, point.zCoord,
						point.xCoord, point.yCoord, point.zCoord);
					outline = outline == null ? corner : outline.union(corner);
				}
			}
		}
		return outline;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		TileEntityProduceFurniture owner = this.getOwner(world, pos, state);
		boolean upper = this.rack && state.getValue(SECOND);
		BlockPos base = upper ? pos.down() : pos;
		double yaw = yaw(state.getValue(FACING));
		Vec3d centre = new Vec3d(base).addVector(0.5D, 0, 0.5D);
		Vec3d localStart = start.subtract(centre).rotateYaw((float)Math.toRadians(-yaw));
		Vec3d localEnd = end.subtract(centre).rotateYaw((float)Math.toRadians(-yaw));
		RayTraceResult nearest = null;
		for (int slot = 0; slot < this.getTrayCount(state); slot++) {
			float pitch = (float)Math.toRadians(-this.tierPitch(slot));
			double height = this.tierHeight(slot);
			AxisAlignedBB bounds = this.getSlotBounds(owner, slot);
			RayTraceResult hit = bounds.calculateIntercept(localStart.addVector(0, -height, 0).rotatePitch(-pitch),
				localEnd.addVector(0, -height, 0).rotatePitch(-pitch));
			if (hit == null) {
				continue;
			}
			Vec3d point = hit.hitVec.rotatePitch(pitch).addVector(0, height, 0);
			if (point.yCoord < (upper ? 1 : 0) || point.yCoord > (upper ? 2 : 1)) {
				continue;
			}
			Vec3d worldHit = centre.add(point.rotateYaw((float)Math.toRadians(yaw)));
			if (nearest == null || start.squareDistanceTo(worldHit) < start.squareDistanceTo(nearest.hitVec)) {
				Vec3d normal = new Vec3d(hit.sideHit.getDirectionVec()).rotatePitch(pitch).rotateYaw((float)Math.toRadians(yaw));
				nearest = new RayTraceResult(worldHit, EnumFacing.getFacingFromVector((float)normal.xCoord,
					(float)normal.yCoord, (float)normal.zCoord), pos);
				nearest.subHit = slot;
			}
		}
		return nearest == null ? super.collisionRayTrace(state, world, pos, start, end) : nearest;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Collections.emptyList();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && !world.restoringBlockSnapshots) {
			if (this.rack && state.getValue(SECOND)) {
				if (this.isRackPart(world.getBlockState(pos.down()), state, false)) {
					world.setBlockToAir(pos.down());
				}
			} else {
				TileEntityProduceFurniture tile = tileAt(world, pos);
				if (tile != null && world.getGameRules().getBoolean("doTileDrops")) {
					if (this.rack && tile.isRackReady()) {
						spawnAsEntity(world, pos, ItemBlockProduceFurniture.pack(this, new ProduceTray(tile.getWood())));
					}
					for (int slot = 0; slot < this.getTrayCount(state); slot++) {
						ProduceTray tray = tile.getTray(slot);
						if (tray != null) {
							spawnAsEntity(world, pos, ItemBlockProduceFurniture.pack(Ironagefurniture.BlockRegistry.get(TRAY_ID), tray));
						}
					}
				}
				if (this.rack && this.isRackPart(world.getBlockState(pos.up()), state, true)) {
					world.setBlockToAir(pos.up());
				}
			}
		}
		super.breakBlock(world, pos, state);
	}
}
