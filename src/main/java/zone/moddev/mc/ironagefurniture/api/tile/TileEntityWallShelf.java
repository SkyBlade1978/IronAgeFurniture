package zone.moddev.mc.ironagefurniture.api.tile;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceDisplayBlocker;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContents;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityWallShelf extends TileEntity implements zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost {
	private final zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting setting = new zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting();
	@Override
	public zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting getSurfaceSetting() { return this.setting; }
	@Override
	public void markSurfaceSettingChanged() { this.markForUpdate(); }
	@Override
	public double getSurfaceHeight() { return 0.8125D; }
	private EnumFacing getShelfFacing() {
		return this.world == null ? EnumFacing.NORTH
			: this.world.getBlockState(this.pos).getActualState(this.world, this.pos)
				.getValue(zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.FACING);
	}
	@Override
	public double getSurfaceOffsetX() { return -this.getShelfFacing().getFrontOffsetX() * 0.125D; }
	@Override
	public double getSurfaceOffsetZ() { return -this.getShelfFacing().getFrontOffsetZ() * 0.125D; }
	@Override
	public EnumFacing.Axis getSurfaceAxis() {
		return this.getShelfFacing().getAxis() == EnumFacing.Axis.X ? EnumFacing.Axis.Z : EnumFacing.Axis.X;
	}
	@Override
	public void onLoad() {
		super.onLoad();
		zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceReservationRepair.enqueue(this.world, this.pos);
	}
	private static final int MAX_EMBEDDED_SLOTS = 9;

	private ShelfContentKind embeddedKind = ShelfContentKind.NONE;
	private final ItemStack[] embeddedItems = new ItemStack[MAX_EMBEDDED_SLOTS];

	public boolean hasDisplayedItem() {
		return this.setting.hasAnyItem();
	}

	public ItemStack getDisplayedItem() {
		return this.setting.getCompatibilityItem();
	}

	public EnumFacing getDisplayedItemFacing() {
		return this.setting.getCompatibilityFacing();
	}

	public boolean hasStoredData() {
		return this.hasDisplayedItem() || this.hasEmbeddedContent();
	}

	public boolean hasEmbeddedContent() {
		return this.embeddedKind != ShelfContentKind.NONE && this.getEmbeddedCount() > 0;
	}

	public ItemStack getLastEmbeddedItem() {
		for (int i = this.embeddedItems.length - 1; i >= 0; i--) {
			ItemStack itemStack = this.embeddedItems[i];

			if (itemStack != null && itemStack.stackSize > 0) {
				return itemStack;
			}
		}

		return null;
	}

	public ItemStack getFirstEmbeddedItem() {
		for (ItemStack itemStack : this.embeddedItems) {
			if (itemStack != null && itemStack.stackSize > 0) {
				return itemStack;
			}
		}

		return null;
	}

	public List<ItemStack> getEmbeddedItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();

		for (ItemStack itemStack : this.embeddedItems) {
			if (itemStack != null && itemStack.stackSize > 0) {
				items.add(itemStack.copy());
			}
		}

		return items;
	}

	public ShelfContentKind getEmbeddedKind() {
		return this.hasEmbeddedContent() ? this.embeddedKind : ShelfContentKind.NONE;
	}

	public ShelfContents getShelfContents() {
		return ShelfContents.fromKindAndCount(this.getEmbeddedKind(), this.getEmbeddedCount());
	}

	public int getEmbeddedLightLevel() {
		return this.getShelfContents().getLightLevel();
	}

	public boolean canAddEmbeddedContent(ShelfContentKind kind) {
		return kind != null && kind != ShelfContentKind.NONE
			&& !this.hasDisplayedItem()
			&& (this.embeddedKind == ShelfContentKind.NONE || this.embeddedKind == kind)
			&& this.getEmbeddedCount() < kind.getCapacity();
	}

	public boolean addEmbeddedContent(ShelfContentKind kind, ItemStack itemStack) {
		if (!this.canAddEmbeddedContent(kind) || itemStack == null || itemStack.stackSize <= 0) {
			return false;
		}

		int oldLight = this.getEmbeddedLightLevel();

		if (this.embeddedKind == ShelfContentKind.NONE) {
			this.embeddedKind = kind;
		}

		ItemStack storedStack = itemStack.copy();
		storedStack.stackSize = 1;
		this.embeddedItems[this.getEmbeddedCount()] = storedStack;
		this.markForUpdate(oldLight != this.getEmbeddedLightLevel());
		return true;
	}

	public ItemStack removeLastEmbeddedItem() {
		int oldLight = this.getEmbeddedLightLevel();

		for (int i = this.embeddedItems.length - 1; i >= 0; i--) {
			ItemStack itemStack = this.embeddedItems[i];

			if (itemStack != null && itemStack.stackSize > 0) {
				this.embeddedItems[i] = null;

				if (this.getEmbeddedCount() == 0) {
					this.embeddedKind = ShelfContentKind.NONE;
				}

				this.markForUpdate(oldLight != this.getEmbeddedLightLevel());
				return itemStack;
			}
		}

		return null;
	}

	public List<ItemStack> removeAllEmbeddedItems() {
		int oldLight = this.getEmbeddedLightLevel();
		List<ItemStack> drops = new ArrayList<ItemStack>();

		for (int i = 0; i < this.embeddedItems.length; i++) {
			ItemStack itemStack = this.embeddedItems[i];

			if (itemStack != null && itemStack.stackSize > 0) {
				drops.add(itemStack);
				this.embeddedItems[i] = null;
			}
		}

		this.embeddedKind = ShelfContentKind.NONE;
		this.markForUpdate(oldLight != this.getEmbeddedLightLevel());
		return drops;
	}

	public void setDisplayedItem(ItemStack displayedItem) {
		this.setDisplayedItem(displayedItem, this.setting.getCompatibilityFacing());
	}

	public void setDisplayedItem(ItemStack displayedItem, EnumFacing facing) {
		this.setting.setSingleItem(displayedItem, facing);
		this.markForUpdate();
	}

	public ItemStack removeDisplayedItem() {
		ItemStack stack = this.setting.removeCompatibilityItem();
		this.markForUpdate();
		return stack;
	}

	public void dropDisplayedItem(World worldIn, BlockPos pos) {
		SurfaceDisplayBlocker.dropItems(worldIn, pos, this);
		SurfaceDisplayBlocker.release(worldIn, pos);
	}

	public void dropEmbeddedItems(World worldIn, BlockPos pos) {
		for (ItemStack itemStack : this.removeAllEmbeddedItems()) {
			Block.spawnAsEntity(worldIn, pos, itemStack);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.clearEmbeddedItems();

		this.setting.readFromNBT(compound);

		if (compound.hasKey("EmbeddedKind")) {
			this.embeddedKind = ShelfContentKind.byName(compound.getString("EmbeddedKind"));
			NBTTagList list = compound.getTagList("EmbeddedItems", 10);

			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound itemTag = list.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot") & 255;

				if (slot >= 0 && slot < this.embeddedItems.length) {
					this.embeddedItems[slot] = ItemStack.loadItemStackFromNBT(itemTag);
				}
			}

			if (this.getEmbeddedCount() == 0) {
				this.embeddedKind = ShelfContentKind.NONE;
			}
		} else if (compound.hasKey("EmbeddedLamp")) {
			this.readLegacyEmbeddedLamp(compound.getString("EmbeddedLamp"));
		} else {
			this.embeddedKind = ShelfContentKind.NONE;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);

		this.setting.writeToNBT(compound);

		if (this.hasEmbeddedContent()) {
			compound.setString("EmbeddedKind", this.embeddedKind.getName());
			NBTTagList list = new NBTTagList();

			for (int i = 0; i < this.embeddedItems.length; i++) {
				ItemStack itemStack = this.embeddedItems[i];

				if (itemStack != null && itemStack.stackSize > 0) {
					NBTTagCompound itemTag = new NBTTagCompound();
					itemTag.setByte("Slot", (byte)i);
					itemStack.writeToNBT(itemTag);
					list.appendTag(itemTag);
				}
			}

			compound.setTag("EmbeddedItems", list);
		} else {
			compound.removeTag("EmbeddedKind");
			compound.removeTag("EmbeddedItems");
		}

		compound.removeTag("EmbeddedLamp");
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		int oldLight = this.getEmbeddedLightLevel();
		this.readFromNBT(pkt.getNbtCompound());
		this.refreshRender();

		if (oldLight != this.getEmbeddedLightLevel()) {
			this.refreshLighting();
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	public int getEmbeddedCount() {
		int count = 0;

		for (ItemStack itemStack : this.embeddedItems) {
			if (itemStack != null && itemStack.stackSize > 0) {
				count++;
			}
		}

		return count;
	}

	private void clearEmbeddedItems() {
		for (int i = 0; i < this.embeddedItems.length; i++) {
			this.embeddedItems[i] = null;
		}
	}

	private void readLegacyEmbeddedLamp(String lampName) {
		if ("glow".equals(lampName)) {
			this.embeddedKind = ShelfContentKind.GLOW;
			this.embeddedItems[0] = this.createItemFromBlock(BlockObjectHolder.light_metal_ironage_block_floor_glow_clear);
		} else if ("lava".equals(lampName)) {
			this.embeddedKind = ShelfContentKind.LAVA;
			this.embeddedItems[0] = this.createItemFromBlock(BlockObjectHolder.light_metal_ironage_block_floor_lava_clear);
		}

		if (this.getEmbeddedCount() == 0) {
			this.embeddedKind = ShelfContentKind.NONE;
		}
	}

	private ItemStack createItemFromBlock(Block block) {
		return block == null ? null : new ItemStack(block, 1);
	}

	private void markForUpdate() {
		this.markForUpdate(false);
	}

	private void markForUpdate(boolean lightChanged) {
		this.markDirty();

		if (this.world != null && !this.world.isRemote) {
			this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos),
				this.world.getBlockState(this.pos), 3);
		}

		if (lightChanged) {
			this.refreshLighting();
		}
	}

	private void refreshLighting() {
		if (this.world == null || this.pos == null) {
			return;
		}

		this.world.checkLight(this.pos);

		for (EnumFacing facing : EnumFacing.values()) {
			this.world.checkLight(this.pos.offset(facing));
		}

		if (this.world.isRemote) {
			this.world.markBlockRangeForRenderUpdate(this.pos.add(-15, -15, -15), this.pos.add(15, 15, 15));
		}
	}

	private void refreshRender() {
		if (this.world != null && this.world.isRemote && this.pos != null) {
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
		}
	}

	private EnumFacing horizontalOrNorth(EnumFacing facing) {
		if (facing == EnumFacing.NORTH || facing == EnumFacing.EAST
				|| facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) {
			return facing;
		}

		return EnumFacing.NORTH;
	}
}
