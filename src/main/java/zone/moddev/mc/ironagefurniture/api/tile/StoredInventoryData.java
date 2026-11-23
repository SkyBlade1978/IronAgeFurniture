package zone.moddev.mc.ironagefurniture.api.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Keeps raw stacks when their supplying mod is absent and restores them when it returns. */
final class StoredInventoryData {
	private final List<NBTTagCompound> unresolved = new ArrayList<>();
	private boolean restoredItems;

	void read(NBTTagCompound data, String key, int size, IntFunction<ItemStack> slot,
			BiConsumer<Integer, ItemStack> setSlot) {
		this.unresolved.clear();
		this.restoredItems = false;
		this.readList(data.getTagList(key, 10), size, slot, setSlot, false);
		this.readList(data.getTagList(key + "Unresolved", 10), size, slot, setSlot, true);
	}

	private void readList(NBTTagList items, int size, IntFunction<ItemStack> slot,
			BiConsumer<Integer, ItemStack> setSlot, boolean allowOtherSlot) {
		for (int i = 0; i < items.tagCount(); i++) {
			NBTTagCompound saved = items.getCompoundTagAt(i);
			ItemStack stack = ItemStack.loadItemStackFromNBT(saved);
			int index = saved.getByte("Slot") & 255;
			if (allowOtherSlot && (index >= size || slot.apply(index) != null)) {
				for (index = 0; index < size && slot.apply(index) != null; index++) { }
			}

			if (stack != null && stack.getItem() != null && index < size && slot.apply(index) == null) {
				setSlot.accept(index, stack);
				this.restoredItems |= allowOtherSlot;
			} else {
				this.unresolved.add(saved.copy());
			}
		}
	}

	boolean hasRestoredItems() {
		return this.restoredItems;
	}

	void write(NBTTagCompound data, String key, int size, IntFunction<ItemStack> slot) {
		NBTTagList items = new NBTTagList();
		for (int i = 0; i < size; i++) {
			ItemStack stack = slot.apply(i);
			if (stack != null) {
				NBTTagCompound saved = stack.writeToNBT(new NBTTagCompound());
				saved.setByte("Slot", (byte)i);
				items.appendTag(saved);
			}
		}

		data.setTag(key, items);
		this.writeUnresolved(data, key);
	}

	void writeUnresolved(NBTTagCompound data, String key) {
		if (this.unresolved.isEmpty()) {
			data.removeTag(key + "Unresolved");
			return;
		}

		NBTTagList raw = new NBTTagList();
		for (NBTTagCompound item : this.unresolved) {
			raw.appendTag(item.copy());
		}

		data.setTag(key + "Unresolved", raw);
	}
}
