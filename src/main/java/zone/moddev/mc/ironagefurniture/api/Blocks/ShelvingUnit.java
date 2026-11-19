package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.List;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceInteractionRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingInteraction;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingInteraction.Result;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;
import zone.moddev.mc.ironagefurniture.client.render.SurfaceDisplayRenderHelper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

public final class ShelvingUnit extends RackBlock {
	public static final double BOARD_THICKNESS = 1.0D / 16.0D;
	public static final double UPPER_HEIGHT = 8.5D / 16.0D;
	public static final double MEDIA_SCALE = 0.84D;
	public static final int BOTTLE_CAPACITY = 8;
	private static final int ITEM_HIT = 100;
	private static final int MEDIA_HIT = ITEM_HIT + 2 * Slot.values().length;
	private static final int BOTTLE_HIT = MEDIA_HIT + 2;
	private static final double[] BOOK_ROW_WIDTHS = { 3.7D, 6.45D, 8.79D, 10.75D, 15.05D };

	public ShelvingUnit(Material material, float resistance, float hardness) {
		super(material, resistance, hardness);
	}

	public static int getMediaCapacity(ShelfContentKind kind) {
		if (kind == ShelfContentKind.BOTTLES) {
			return BOTTLE_CAPACITY;
		}
		return kind == ShelfContentKind.RECORDS ? 10 : kind.getCapacity();
	}

	public static ShelfContentKind classifyMedia(ItemStack stack) {
		ShelfContentKind kind = ShelfContentKind.forMedia(stack);
		if (kind != ShelfContentKind.NONE) {
			return kind;
		}
		return SurfaceSetting.classify(stack) == SurfaceSetting.Category.BOTTLE
			? ShelfContentKind.BOTTLES : ShelfContentKind.NONE;
	}

	@Override
	public boolean isRackFamily(Block block) {
		return block instanceof ShelvingUnit;
	}

	@Override
	protected double getRackDepth() {
		return 0.5D;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return state.getValue(DATA) ? new TileEntityShelvingUnit() : null;
	}

	@Override
	public void removeEmptyRackTile(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);

		if (!world.isRemote && tile instanceof TileEntityShelvingUnit && ((TileEntityShelvingUnit)tile).isEmpty()) {
			this.clearRackTile(world, pos);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (this.isRackItem(held)) {
			return this.tryPlaceRackFromRackClick(world, pos, state, player, hand, held, side, hitX, hitY, hitZ);
		}

		if (player.isSneaking()) {
			return false;
		}

		boolean hasItem = held != null && held.stackSize > 0;
		TileEntityShelvingUnit tile = this.getRackTile(world, pos, hasItem, TileEntityShelvingUnit.class);

		if (tile == null) {
			return world.isRemote && hasItem;
		}

		boolean upper = hitY >= UPPER_HEIGHT - BOARD_THICKNESS;
		ShelfLevel level = tile.getLevel(upper);
		ShelfContentKind mediaKind = level.getMediaKind();
		if (!hasItem && mediaKind != ShelfContentKind.NONE) {
			if (!world.isRemote) {
				player.openGui(Ironagefurniture.instance, StorageGuiHandler.shelvingMediaId(mediaKind, upper), world,
					pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}

		ShelfContentKind heldKind = classifyMedia(held);
		boolean hasSurfaceItems = level.getSurfaceSetting().hasAnyItem();
		if (hasSurfaceItems && (heldKind == ShelfContentKind.BOOKS || heldKind == ShelfContentKind.RECORDS)) {
			return false;
		}

		if (mediaKind != ShelfContentKind.NONE || (heldKind != ShelfContentKind.NONE && !hasSurfaceItems)) {
			if (!world.isRemote && level.addMedia(held)) {
				if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) {
					player.setHeldItem(hand, null);
				}
				level.markSurfaceSettingChanged();
			}
			return true;
		}

		double scale = level.getScale();
		float x = (float)(0.5D + (hitX - 0.5D - level.getSurfaceOffsetX()) / scale);
		float z = (float)(0.5D + (hitZ - 0.5D - level.getSurfaceOffsetZ()) / scale);
		Result result = SurfaceSettingInteraction.handle(level, player, hand, held, x, z, level.getSurfaceAxis(), true);
		this.removeEmptyRackTile(world, pos);
		return result != Result.NOT_HANDLED;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);

		if (!world.isRemote && tile instanceof TileEntityShelvingUnit) {
			TileEntityShelvingUnit shelves = (TileEntityShelvingUnit)tile;
			for (int index = 0; index < 2; index++) {
				ShelfLevel level = shelves.getLevel(index == 1);
				SurfaceDisplayBlocker.dropItems(world, pos, level);
				for (ItemStack stack = level.removeLastMedia(); stack != null; stack = level.removeLastMedia()) {
					SurfaceInteractionRegistry.drop(world, pos, stack);
				}
			}
		}

		super.breakBlock(world, pos, state);
		this.notifyRackNeighbors(world, pos);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityShelvingUnit ? ((TileEntityShelvingUnit)tile).getDisplayLight() : 0;
	}

	public double getBottomShelfHeight(IBlockState state, IBlockAccess world, BlockPos pos) {
		return (this.getActualState(state, world, pos).getValue(CONNECTIONS) & DOWN) != 0
			? BOARD_THICKNESS / 2.0D : BOARD_THICKNESS;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB filter,
			List<AxisAlignedBB> boxes, Entity entity) {
		for (AxisAlignedBB board : this.getBoards(state, world, pos)) {
			addCollisionBoxToList(pos, filter, boxes, board);
		}
	}

	private AxisAlignedBB[] getBoards(IBlockState state, IBlockAccess world, BlockPos pos) {
		int connections = this.getActualState(state, world, pos).getValue(CONNECTIONS);
		double left = (connections & LEFT) != 0 ? BOARD_THICKNESS / 2.0D : BOARD_THICKNESS;
		double right = 1.0D - ((connections & RIGHT) != 0 ? BOARD_THICKNESS / 2.0D : BOARD_THICKNESS);
		double bottom = (connections & DOWN) != 0 ? BOARD_THICKNESS / 2.0D : BOARD_THICKNESS;
		double top = 1.0D - ((connections & UP) != 0 ? BOARD_THICKNESS / 2.0D : BOARD_THICKNESS);
		double front = state.getValue(STANDING) ? 0.25D : 0.5D;
		double back = front + this.getRackDepth();
		AxisAlignedBB[] boards = {
			new AxisAlignedBB(0, 0, front, 1, bottom, back),
			new AxisAlignedBB(0, top, front, 1, 1, back),
			new AxisAlignedBB(0, bottom, front, left, top, back),
			new AxisAlignedBB(right, bottom, front, 1, top, back),
			new AxisAlignedBB(left, UPPER_HEIGHT - BOARD_THICKNESS, front, right, UPPER_HEIGHT, back)
		};

		for (int index = 0; index < boards.length; index++) {
			boards[index] = this.rotateToFacing(boards[index], state.getValue(FACING));
		}

		return boards;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		Vec3d origin = new Vec3d(pos);
		Vec3d localStart = start.subtract(origin);
		Vec3d localEnd = end.subtract(origin);
		RayTraceResult nearest = null;

		for (AxisAlignedBB board : this.getBoards(state, world, pos)) {
			nearest = nearerHit(nearest, board, pos, origin, localStart, localEnd, -1);
		}

		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEntityShelvingUnit) {
			TileEntityShelvingUnit shelves = (TileEntityShelvingUnit)tile;

			for (int index = 0; index < 2; index++) {
				ShelfLevel level = shelves.getLevel(index == 1);
				if (level.getMediaKind() == ShelfContentKind.BOTTLES) {
					for (int bottle = 0; bottle < level.getMediaCount(); bottle++) {
						int hit = BOTTLE_HIT + index * BOTTLE_CAPACITY + bottle;
						nearest = nearerHit(nearest, level.getBottleBounds(bottle), pos, origin, localStart, localEnd, hit);
					}
					continue;
				}

				nearest = nearerHit(nearest, this.getMediaBounds(state, level), pos, origin, localStart, localEnd,
					MEDIA_HIT + index);

				for (Slot slot : Slot.values()) {
					AxisAlignedBB bounds = getItemBounds(level, slot, world.isRemote);
					nearest = nearerHit(nearest, bounds, pos, origin, localStart, localEnd, itemHit(index, slot));
				}
			}
		}

		return nearest;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
		TileEntity tile = world.getTileEntity(pos);

		if (hit != null && pos.equals(hit.getBlockPos()) && hit.subHit >= ITEM_HIT
				&& tile instanceof TileEntityShelvingUnit) {
			ShelfLevel level = getHitLevel((TileEntityShelvingUnit)tile, hit.subHit);
			AxisAlignedBB bounds;
			if (hit.subHit >= BOTTLE_HIT) {
				bounds = level.getBottleBounds(getHitBottle(hit.subHit));
			} else {
				bounds = hit.subHit >= MEDIA_HIT ? this.getMediaBounds(state, level)
					: getItemBounds(level, getHitSlot(hit.subHit), true);
			}

			if (bounds != null) {
				return bounds.offset(pos);
			}
		}

		return super.getSelectedBoundingBox(state, world, pos);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		RayTraceResult hit = SurfaceItemHit.traceFromPlayer(state, world, pos, player);
		TileEntity tile = world.getTileEntity(pos);

		if (hit == null || hit.subHit < ITEM_HIT || !(tile instanceof TileEntityShelvingUnit)) {
			return super.removedByPlayer(state, world, pos, player, willHarvest);
		}

		ShelfLevel level = getHitLevel((TileEntityShelvingUnit)tile, hit.subHit);
		ItemStack removed;
		if (hit.subHit >= BOTTLE_HIT) {
			removed = level.removeMedia(getHitBottle(hit.subHit));
		} else {
			removed = hit.subHit >= MEDIA_HIT ? level.removeLastMedia()
				: level.getSurfaceSetting().remove(getHitSlot(hit.subHit));
		}
		level.markSurfaceSettingChanged();

		if (!world.isRemote && !player.capabilities.isCreativeMode) {
			SurfaceInteractionRegistry.drop(world, pos, removed);
		}

		this.removeEmptyRackTile(world, pos);
		return true;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
			TileEntity tile, ItemStack tool) {
		if (world.getBlockState(pos).getBlock() != this) {
			super.harvestBlock(world, player, pos, state, tile, tool);
		}
	}

	private AxisAlignedBB getMediaBounds(IBlockState state, ShelfLevel level) {
		int count = level.getMediaCount();
		if (count == 0) {
			return null;
		}

		// Match the shelving media models before applying their common render scale.
		boolean books = level.getMediaKind() == ShelfContentKind.BOOKS;
		double scale = MEDIA_SCALE / 16.0D;
		double halfWidth = (books ? BOOK_ROW_WIDTHS[count - 1] : 16.0D) * scale * 0.5D;
		double height = books ? (count == 1 ? 7.0D : 7.45D) : 7.05D;
		double depthCentre = state.getValue(STANDING) ? 0.5D : 0.75D;
		double front = depthCentre + ((books ? 6.92D : 7.0D) - 10.625D) * scale;
		double back = depthCentre + ((books ? 14.0D : 14.3D) - 10.625D) * scale;
		double y = level.getSurfaceHeight();
		return this.rotateToFacing(new AxisAlignedBB(0.5D - halfWidth, y, front,
			0.5D + halfWidth, y + height * scale, back), state.getValue(FACING));
	}

	public static AxisAlignedBB getItemBounds(ShelfLevel level, Slot slot, boolean client) {
		if (level.getSurfaceSetting().getItem(slot) == null) {
			return null;
		}

		AxisAlignedBB bounds = client ? SurfaceDisplayRenderHelper.getItemBounds(level.getSurfaceSetting(), slot)
			: level.getSurfaceSetting().getItemBounds(slot);

		if (bounds == null) {
			return null;
		}

		double scale = level.getScale();
		double x = 0.5D + level.getSurfaceOffsetX() - 0.5D * scale;
		double z = 0.5D + level.getSurfaceOffsetZ() - 0.5D * scale;
		double y = level.getSurfaceHeight();
		return new AxisAlignedBB(x + bounds.minX * scale, y + bounds.minY * scale, z + bounds.minZ * scale,
			x + bounds.maxX * scale, y + bounds.maxY * scale, z + bounds.maxZ * scale);
	}

	private static RayTraceResult nearerHit(RayTraceResult nearest, AxisAlignedBB box, BlockPos pos,
			Vec3d origin, Vec3d start, Vec3d end, int item) {
		RayTraceResult hit = box == null ? null : box.calculateIntercept(start, end);

		if (hit == null || (nearest != null && origin.add(start).squareDistanceTo(nearest.hitVec)
				<= start.squareDistanceTo(hit.hitVec))) {
			return nearest;
		}

		RayTraceResult result = new RayTraceResult(hit.hitVec.add(origin), hit.sideHit, pos);
		result.subHit = item;
		return result;
	}

	private static int itemHit(int level, Slot slot) {
		return ITEM_HIT + level * Slot.values().length + slot.getIndex();
	}

	private static ShelfLevel getHitLevel(TileEntityShelvingUnit tile, int hit) {
		if (hit >= BOTTLE_HIT) {
			return tile.getLevel(hit - BOTTLE_HIT >= BOTTLE_CAPACITY);
		}
		return tile.getLevel(hit >= MEDIA_HIT ? hit == MEDIA_HIT + 1 : hit - ITEM_HIT >= Slot.values().length);
	}

	private static int getHitBottle(int hit) {
		return (hit - BOTTLE_HIT) % BOTTLE_CAPACITY;
	}

	private static Slot getHitSlot(int hit) {
		return Slot.byIndex((hit - ITEM_HIT) % Slot.values().length);
	}
}
