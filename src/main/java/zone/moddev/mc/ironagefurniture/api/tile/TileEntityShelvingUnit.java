package zone.moddev.mc.ironagefurniture.api.tile;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class TileEntityShelvingUnit extends TileEntity {
	private final ShelfLevel lower = new ShelfLevel(false);
	private final ShelfLevel upper = new ShelfLevel(true);

	public ShelfLevel getLevel(boolean upper) {
		return upper ? this.upper : this.lower;
	}

	public boolean isEmpty() {
		return this.lower.isEmpty() && this.upper.isEmpty();
	}

	public int getDisplayLight() {
		return Math.max(this.lower.getLight(), this.upper.getLight());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.lower.readItems(compound.getCompoundTag("LowerShelf"));
		this.upper.readItems(compound.getCompoundTag("UpperShelf"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("LowerShelf", this.lower.writeItems());
		compound.setTag("UpperShelf", this.upper.writeItems());
		return compound;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
		this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
		this.world.checkLight(this.pos);
	}

	private IBlockState getShelvingState() {
		return this.world == null ? null : this.world.getBlockState(this.pos);
	}

	public EnumFacing getFacing() {
		IBlockState state = this.getShelvingState();
		return state != null && state.getBlock() instanceof ShelvingUnit
			? state.getValue(BlockHorizontal.FACING) : EnumFacing.NORTH;
	}

	private double getWallOffset() {
		IBlockState state = this.getShelvingState();
		return state != null && state.getBlock() instanceof ShelvingUnit && !state.getValue(RackBlock.STANDING)
			? -0.25D : 0.0D;
	}

	public final class ShelfLevel implements SurfaceSettingHost {
		private static final int BOTTLES_PER_ROW = ShelvingUnit.BOTTLE_CAPACITY / 2;

		private final boolean upper;
		private final SurfaceSetting setting = new SurfaceSetting();
		private final List<ItemStack> media = new ArrayList<>();
		private int mediaRevision;

		private ShelfLevel(boolean upper) {
			this.upper = upper;
		}

		@Override
		public SurfaceSetting getSurfaceSetting() {
			return this.setting;
		}

		public boolean isEmpty() {
			return this.media.isEmpty() && !this.setting.hasAnyItem();
		}

		public int getMediaCount() {
			return this.media.size();
		}

		public ShelfContentKind getMediaKind() {
			return ShelvingUnit.classifyMedia(this.getMediaItem(0));
		}

		public int getMediaRevision() {
			return this.mediaRevision;
		}

		/** Treat displayed stacks as read-only. */
		public ItemStack getMediaItem(int index) {
			return index >= 0 && index < this.media.size() ? this.media.get(index) : null;
		}

		public ItemStack copyMediaItem(int index) {
			ItemStack item = this.getMediaItem(index);
			return item == null ? null : item.copy();
		}

		public boolean addMedia(ItemStack stack) {
			ShelfContentKind incoming = ShelvingUnit.classifyMedia(stack);
			if (incoming == ShelfContentKind.NONE || this.setting.hasAnyItem()
					|| (!this.media.isEmpty() && this.getMediaKind() != incoming)
					|| this.media.size() >= ShelvingUnit.getMediaCapacity(incoming)) {
				return false;
			}

			ItemStack stored = stack.copy();
			stored.stackSize = 1;
			this.media.add(stored);
			this.mediaRevision++;
			return true;
		}

		public ItemStack removeLastMedia() {
			return this.removeMedia(this.media.size() - 1);
		}

		public ItemStack removeMedia(int index) {
			if (index < 0 || index >= this.media.size()) {
				return null;
			}

			this.mediaRevision++;
			return this.media.remove(index);
		}

		private void readItems(NBTTagCompound compound) {
			this.setting.readFromNBT(compound);
			this.media.clear();
			this.mediaRevision++;
			NBTTagList items = compound.getTagList("MediaItems", 10);
			for (int index = 0; index < items.tagCount(); index++) {
				this.addMedia(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(index)));
			}

			this.migrateSurfaceMedia();
		}

		private void migrateSurfaceMedia() {
			ShelfContentKind kind = ShelvingUnit.classifyMedia(this.setting.getCompatibilityItem());
			if (!this.media.isEmpty() || kind == ShelfContentKind.NONE) {
				return;
			}

			// Migrate complete rows and keep mixed surface settings intact.
			List<ItemStack> items = new ArrayList<>();
			for (Slot slot : Slot.values()) {
				ItemStack item = this.setting.getItem(slot);
				if (item != null) {
					if (ShelvingUnit.classifyMedia(item) != kind) {
						return;
					}
					items.add(item);
				}
			}

			this.setting.clear();
			for (ItemStack item : items) {
				this.addMedia(item);
			}
		}

		private NBTTagCompound writeItems() {
			NBTTagCompound compound = new NBTTagCompound();
			this.setting.writeToNBT(compound);
			if (!this.media.isEmpty()) {
				NBTTagList items = new NBTTagList();
				for (ItemStack stack : this.media) {
					items.appendTag(stack.writeToNBT(new NBTTagCompound()));
				}
				compound.setTag("MediaItems", items);
			}

			return compound;
		}

		@Override
		public double getSurfaceHeight() {
			IBlockState state = getShelvingState();

			if (!this.upper && state != null && state.getBlock() instanceof ShelvingUnit) {
				return ((ShelvingUnit)state.getBlock()).getBottomShelfHeight(state, world, pos);
			}

			return this.upper ? ShelvingUnit.UPPER_HEIGHT : ShelvingUnit.BOARD_THICKNESS;
		}

		@Override
		public double getSurfaceOffsetX() {
			return getFacing().getFrontOffsetX() * getWallOffset();
		}

		@Override
		public double getSurfaceOffsetZ() {
			return getFacing().getFrontOffsetZ() * getWallOffset();
		}

		@Override
		public EnumFacing.Axis getSurfaceAxis() {
			return getFacing().getAxis() == EnumFacing.Axis.X ? EnumFacing.Axis.Z : EnumFacing.Axis.X;
		}

		public Vec3d getBottleOffset(int index) {
			EnumFacing facing = getFacing();
			EnumFacing left = facing.rotateY();
			double lateral = 0.30D - (index % BOTTLES_PER_ROW) * 0.20D;
			double depth = index < BOTTLES_PER_ROW ? 0.11D : -0.11D;
			return new Vec3d(lateral * left.getFrontOffsetX() + depth * facing.getFrontOffsetX(), 0,
				lateral * left.getFrontOffsetZ() + depth * facing.getFrontOffsetZ());
		}

		public double getBottleScale(ItemStack bottle) {
			AxisAlignedBB bounds = SurfaceContentRegistry.describe(bottle).getBounds(EnumFacing.NORTH);
			double halfFootprint = Math.max(Math.max(-bounds.minX, bounds.maxX), Math.max(-bounds.minZ, bounds.maxZ));
			return Math.min(0.75D, Math.min(0.0875D / halfFootprint, 0.375D / bounds.maxY));
		}

		public AxisAlignedBB getBottleBounds(int index) {
			ItemStack bottle = this.getMediaItem(index);
			if (bottle == null) {
				return null;
			}

			AxisAlignedBB bounds = SurfaceContentRegistry.describe(bottle).getBounds(getFacing());
			Vec3d offset = this.getBottleOffset(index);
			double scale = this.getBottleScale(bottle);
			return new AxisAlignedBB(bounds.minX * scale, bounds.minY * scale, bounds.minZ * scale,
				bounds.maxX * scale, bounds.maxY * scale, bounds.maxZ * scale)
				.offset(0.5D + this.getSurfaceOffsetX() + offset.xCoord, this.getSurfaceHeight(),
					0.5D + this.getSurfaceOffsetZ() + offset.zCoord);
		}

		public double getScale() {
			AxisAlignedBB bounds = this.setting.getCombinedBounds();

			if (bounds == null) {
				return 0.65D;
			}

			double acrossX = Math.max(0.5D - bounds.minX, bounds.maxX - 0.5D);
			double acrossZ = Math.max(0.5D - bounds.minZ, bounds.maxZ - 0.5D);
			boolean alongX = this.getSurfaceAxis() == EnumFacing.Axis.X;
			double halfDepth = alongX ? acrossZ : acrossX;
			double halfWidth = alongX ? acrossX : acrossZ;
			return Math.min(0.65D, Math.min(0.375D / bounds.maxY,
				Math.min(0.24D / halfDepth, 0.425D / halfWidth)));
		}

		@Override
		public void markSurfaceSettingChanged() {
			if (world != null && !world.isRemote) {
				this.migrateSurfaceMedia();
			}
			markDirty();

			if (world != null && !world.isRemote) {
				IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 2);
				world.checkLight(pos);
			}
		}

		private int getLight() {
			int light = 0;

			for (SurfaceSetting.Slot slot : SurfaceSetting.Slot.values()) {
				ItemStack stack = this.setting.getItem(slot);

				if (stack != null && stack.getItem() instanceof ItemBlock) {
					IBlockState state = ((ItemBlock)stack.getItem()).getBlock().getDefaultState();
					light = Math.max(light, state.getLightValue());
				}
			}

			return light;
		}
	}
}
