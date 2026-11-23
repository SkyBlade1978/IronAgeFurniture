package zone.moddev.mc.ironagefurniture.api.container;

import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockCrate;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public final class ContainerCrate extends Container {
	private final TileEntityCrate crate;
	private final String title;

	public ContainerCrate(TileEntityCrate crate, EntityPlayer player) {
		this.crate = crate;
		this.title = crate.getCrateLabel().getText();
		IInventory inventory = crate.getInventory();
		for (int row = 0; row < 4; row++) {
			for (int column = 0; column < 4; column++) {
				this.addSlotToContainer(new Slot(inventory, row * 4 + column, 53 + column * 18, 20 + row * 18));
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlotToContainer(new Slot(player.inventory, 9 + row * 9 + column, 8 + column * 18, 106 + row * 18));
			}
		}
		for (int column = 0; column < 9; column++) {
			this.addSlotToContainer(new Slot(player.inventory, column, 8 + column * 18, 164));
		}
	}

	public String getTitle() {
		return this.title;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.crate.canOpen(player);
	}

	@Override
	public ItemStack slotClick(int slot, int button, ClickType click, EntityPlayer player) {
		return this.canInteractWith(player) ? super.slotClick(slot, button, click, player) : null;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		if (!this.canInteractWith(player)) {
			return null;
		}
		Slot slot = this.inventorySlots.get(index);
		ItemStack stack = slot.getStack();
		if (stack == null) {
			return null;
		}

		ItemStack original = stack.copy();
		int slots = TileEntityCrate.SLOTS;
		if (index < slots) {
			if (!this.mergeItemStack(stack, slots, this.inventorySlots.size(), true)) {
				return null;
			}
		} else if (ItemBlockCrate.isPacked(stack) || !this.mergeItemStack(stack, 0, slots, false)) {
			return null;
		}

		if (stack.stackSize == 0) {
			slot.putStack(null);
		} else {
			slot.onSlotChanged();
		}
		slot.onPickupFromSlot(player, stack);
		return original;
	}
}
