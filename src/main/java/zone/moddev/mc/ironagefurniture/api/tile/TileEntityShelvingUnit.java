package zone.moddev.mc.ironagefurniture.api.tile;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.network.ShelfMapSync;
import zone.moddev.mc.ironagefurniture.api.surface.FurnitureSurfaceDisplay;
import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class TileEntityShelvingUnit extends TileEntityDisplayStorage {
	public static final int LABEL_MAX_LENGTH = FurnitureLabel.MAX_LENGTH;

	private final ShelfLevel lower = new ShelfLevel(false);
	private final ShelfLevel upper = new ShelfLevel(true);

	public ShelfLevel getLevel(boolean upper) {
		return upper ? this.upper : this.lower;
	}

	@Override
	protected boolean tryFeed(ItemStack stack, boolean simulate) {
		IBlockState state = this.getShelvingState();
		if (!(state.getBlock() instanceof ShelvingUnit) || !ShelvingUnit.canFeedDisplay(stack)) {
			return false;
		}

		return this.upper.tryFeed(stack, simulate) || this.lower.tryFeed(stack, simulate);
	}

	public boolean isEmpty() {
		return this.lower.isEmpty() && this.upper.isEmpty();
	}

	public int getDisplayLight() {
		return Math.max(this.lower.getLight(), this.upper.getLight());
	}

	@Override
	public void onLoad() {
		super.onLoad();
		ShelfMapSync.INSTANCE.update(this);
	}

	@Override
	public void invalidate() {
		ShelfMapSync.INSTANCE.remove(this);
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		ShelfMapSync.INSTANCE.remove(this);
		super.onChunkUnload();
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
		private final FurnitureLabel label = new FurnitureLabel();

		private ShelfLevel(boolean upper) {
			this.upper = upper;
		}

		@Override
		public SurfaceSetting getSurfaceSetting() {
			return this.setting;
		}

		public boolean isEmpty() {
			return this.label.getSign() == null && this.media.isEmpty() && !this.setting.hasAnyItem();
		}

		public FurnitureLabel getLabel() {
			return this.label;
		}

		public ItemStack getLabelSign() {
			return this.label.getSign();
		}

		public String getLabelText() {
			return this.label.getText();
		}

		public void setLabel(ItemStack sign, String text) {
			this.label.set(sign, text);
		}

		public ItemStack removeLabel() {
			return this.label.remove();
		}

		public int getMediaCount() {
			return this.media.size();
		}

		public ShelfContentKind getMediaKind() {
			return ShelvingUnit.classifyMedia(this.getMediaItem(0));
		}

		public ShelfLayout getShelfLayout() {
			return this.getMediaKind() == ShelfContentKind.NONE
				? SurfaceContentRegistry.describe(this.getMediaItem(0)).getShelfLayout()
				: ShelfLayout.NONE;
		}

		public ShelfLayout.Placement getShelfPlacement(int index) {
			ShelfLayout layout = this.getShelfLayout();
			ItemStack stack = this.getMediaItem(index);
			if (stack == null || index >= layout.getCapacity()) {
				return null;
			}
			VanillaShelfDisplay display = VanillaShelfDisplay.find(stack);
			SurfaceContentRegistry.Descriptor descriptor = display == null
				? SurfaceContentRegistry.describe(stack) : display.getShelfModel();
			return layout.place(index, descriptor);
		}

		public int getVisibleArrangementCount() {
			return Math.min(this.getMediaCount(), this.getShelfLayout().getCapacity());
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
			return this.addMedia(stack, false);
		}

		private boolean addMedia(ItemStack stack, boolean loading) {
			if (!this.canAddMedia(stack, loading)) {
				return false;
			}

			ItemStack stored = stack.copy();
			stored.stackSize = 1;
			this.media.add(stored);
			this.mediaRevision++;
			return true;
		}

		private boolean canAddMedia(ItemStack stack, boolean loading) {
			return !this.setting.hasAnyItem() && this.media.size() < shelfCapacity(stack, loading)
				&& (this.media.isEmpty() || matchesDisplay(this.media.get(0), stack));
		}

		private boolean tryFeed(ItemStack stack, boolean simulate) {
			ShelfContentKind kind = ShelvingUnit.classifyMedia(stack);
			boolean hasSurfaceItems = this.setting.hasAnyItem();
			if (hasSurfaceItems && (kind == ShelfContentKind.BOOKS || kind == ShelfContentKind.RECORDS)) {
				return false;
			}

			boolean structured = kind != ShelfContentKind.NONE
				|| SurfaceContentRegistry.describe(stack).getShelfLayout() != ShelfLayout.NONE;
			if (!this.media.isEmpty() || structured && !hasSurfaceItems) {
				if (simulate) {
					return this.canAddMedia(stack, false);
				}
				if (!this.addMedia(stack)) {
					return false;
				}
			} else {
				EnumFacing facing = getFacing();
				EnumFacing.Axis axis = this.getSurfaceAxis();
				if (!this.setting.canInsert(stack, facing, 0.5F, 0.5F, axis)) {
					return false;
				}
				if (simulate) {
					return true;
				}
				this.setting.insert(stack, facing, 0.5F, 0.5F, axis);
			}

			this.markSurfaceSettingChanged();
			return true;
		}

		private int shelfCapacity(ItemStack stack, boolean loading) {
			ShelfContentKind kind = ShelvingUnit.classifyMedia(stack);
			if (kind != ShelfContentKind.NONE) {
				return ShelvingUnit.getMediaCapacity(kind);
			}

			ShelfLayout layout = SurfaceContentRegistry.describe(stack).getShelfLayout();
			// Older carrot trays held 18. Keep the extras available for pickup and block drops.
			return loading && layout == ShelfLayout.CARROT_TRAY ? 18 : layout.getCapacity();
		}

		private boolean matchesDisplay(ItemStack first, ItemStack incoming) {
			ShelfContentKind kind = ShelvingUnit.classifyMedia(first);
			if (kind != ShelfContentKind.NONE) {
				return kind == ShelvingUnit.classifyMedia(incoming);
			}

			if (first.getItem() == Items.FLINT_AND_STEEL) {
				return incoming != null && incoming.getItem() == Items.FLINT_AND_STEEL;
			}

			if (SurfaceContentRegistry.describe(first).getShelfLayout() == ShelfLayout.GEM_PLATTER) {
				return SurfaceContentRegistry.describe(incoming).getShelfLayout() == ShelfLayout.GEM_PLATTER;
			}

			return ItemStack.areItemsEqual(first, incoming) && ItemStack.areItemStackTagsEqual(first, incoming);
		}

		public boolean takeShelfItem(EntityPlayer player) {
			return this.takeShelfItem(player, this.media.size() - 1);
		}

		public boolean takeShelfItem(EntityPlayer player, int index) {
			ItemStack stack = this.getMediaItem(index);
			if (stack == null) {
				return false;
			}

			InventoryPlayer inventory = player.inventory;
			if (stack.isStackable()) {
				for (ItemStack existing : inventory.mainInventory) {
					if (existing != null && existing.isStackable() && ItemStack.areItemsEqual(existing, stack)
							&& ItemStack.areItemStackTagsEqual(existing, stack)
							&& existing.stackSize < Math.min(existing.getMaxStackSize(), inventory.getInventoryStackLimit())) {
						existing.stackSize++;
						this.removeMedia(index);
						inventory.markDirty();
						this.markSurfaceSettingChanged();
						return true;
					}
				}
			}

			int slot = inventory.getFirstEmptyStack();
			if (slot < 0) {
				return false;
			}
			inventory.setInventorySlotContents(slot, this.removeMedia(index));
			inventory.markDirty();
			this.markSurfaceSettingChanged();
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
			this.label.readFromNBT(compound);

			this.media.clear();
			this.mediaRevision++;
			NBTTagList items = compound.getTagList("MediaItems", 10);
			for (int index = 0; index < items.tagCount(); index++) {
				this.addMedia(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(index)), true);
			}

			this.migrateSurfaceMedia();
		}

		private void migrateSurfaceMedia() {
			ItemStack first = this.setting.getCompatibilityItem();
			if (!this.media.isEmpty() || first == null || this.setting.getItemCount() > shelfCapacity(first, false)) {
				return;
			}

			// Migrate complete rows and keep mixed surface settings intact.
			List<ItemStack> items = new ArrayList<>();
			for (Slot slot : Slot.values()) {
				ItemStack item = this.setting.getItem(slot);
				if (item != null) {
					if (!matchesDisplay(first, item)) {
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
			this.label.writeToNBT(compound);

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
			boolean fittedModel = this.setting.getItemCount() == 1
				&& FurnitureSurfaceDisplay.find(this.setting.getCompatibilityItem()) != null;
			return Math.min(fittedModel ? 1.0D : 0.65D, Math.min(0.375D / bounds.maxY,
				Math.min(0.24D / halfDepth, 0.425D / halfWidth)));
		}

		@Override
		public void markSurfaceSettingChanged() {
			if (world != null && !world.isRemote) {
				this.migrateSurfaceMedia();
				ShelfMapSync.INSTANCE.update(TileEntityShelvingUnit.this);
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
				light = Math.max(light, FurnitureSurfaceDisplay.getLight(this.setting.getItem(slot)));
			}

			for (ItemStack stack : this.media) {
				light = Math.max(light, FurnitureSurfaceDisplay.getLight(stack));
			}

			return light;
		}
	}
}
