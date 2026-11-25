package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;

/** The same portable data is used for a floor tray and a tray installed in a rack. */
public final class ProduceTray {
	public static final String WOOD_TAG = "Wood";
	public static final String CONTENTS_TAG = "Produce";
	public static final String DEFAULT_WOOD = "oak";

	private final String wood;
	private ItemStack contents;
	private NBTTagCompound unresolved;

	public ProduceTray(String wood) {
		this.wood = wood.isEmpty() ? DEFAULT_WOOD : wood;
	}

	public String getWood() {
		return this.wood;
	}

	/** Treat the returned stack as read-only. */
	public ItemStack getContents() {
		return this.contents;
	}

	public static int capacity(ItemStack stack) {
		SurfaceContentRegistry.Descriptor descriptor = SurfaceContentRegistry.describe(stack);
		return descriptor.acceptsProduceTray() ? descriptor.getShelfLayout().getCapacity() : 0;
	}

	public boolean offer(ItemStack stack, boolean simulate) {
		int capacity = capacity(stack);
		if (capacity == 0 || this.unresolved != null || this.contents != null
				&& (this.contents.stackSize >= capacity || !ItemHandlerHelper.canItemStacksStack(this.contents, stack))) {
			return false;
		}

		if (!simulate) {
			if (this.contents == null) {
				this.contents = ItemHandlerHelper.copyStackWithSize(stack, 1);
			} else {
				this.contents.stackSize++;
			}
		}
		return true;
	}

	public boolean isPacked() {
		return this.contents != null || this.unresolved != null;
	}

	public ItemStack peekOne() {
		return this.contents == null ? null : ItemHandlerHelper.copyStackWithSize(this.contents, 1);
	}

	public void removeOne() {
		if (this.contents != null && --this.contents.stackSize == 0) {
			this.contents = null;
		}
	}

	public NBTTagCompound write() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString(WOOD_TAG, this.wood);
		if (this.contents != null) {
			tag.setTag(CONTENTS_TAG, this.contents.writeToNBT(new NBTTagCompound()));
		} else if (this.unresolved != null) {
			tag.setTag(CONTENTS_TAG, this.unresolved.copy());
		}
		return tag;
	}

	public static ProduceTray read(NBTTagCompound tag) {
		ProduceTray tray = new ProduceTray(tag.getString(WOOD_TAG));
		if (tag.hasKey(CONTENTS_TAG, 10)) {
			NBTTagCompound raw = tag.getCompoundTag(CONTENTS_TAG);
			tray.contents = ItemStack.loadItemStackFromNBT(raw);
			if (tray.contents == null || tray.contents.getItem() == null || tray.contents.stackSize <= 0) {
				tray.contents = null;
				tray.unresolved = raw.copy();
			}
		}
		return tray;
	}
}
