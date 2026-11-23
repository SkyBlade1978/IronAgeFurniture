package zone.moddev.mc.ironagefurniture.api.container;

import zone.moddev.mc.ironagefurniture.api.tile.BarrelBulkStorage;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public final class ContainerBulkBarrel extends Container {
	public static final int TAKE_ONE = 0;
	public static final int TAKE_STACK = 1;
	private static final int COUNT = 0;
	private static final int CAPACITY = 1;

	private final TileEntityBarrel barrel;
	private final int revision;
	private int count;
	private int capacity;

	public ContainerBulkBarrel(TileEntityBarrel barrel) {
		this.barrel = barrel;
		BarrelBulkStorage storage = barrel.getBulkStorage();
		this.revision = storage.getRevision();
		this.count = storage.getCount();
		this.capacity = storage.getCapacity();
		InventoryBasic display = new InventoryBasic("bulk_barrel", false, 1);
		display.setInventorySlotContents(0, storage.getStack());
		this.addSlotToContainer(new Slot(display, 0, 12, 28) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}

			@Override
			public boolean canTakeStack(EntityPlayer player) {
				return false;
			}
		});
	}

	public int getCount() {
		return this.count;
	}

	public int getCapacity() {
		return this.capacity;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		BarrelBulkStorage storage = this.barrel.getBulkStorage();
		return player.getEntityWorld() == this.barrel.getWorld() && this.barrel.isBulkBarrel()
			&& this.barrel.isUsableByPlayer(player) && storage.getCount() > 0 && storage.getRevision() == this.revision;
	}

	@Override
	public ItemStack slotClick(int slot, int button, ClickType click, EntityPlayer player) {
		return null;
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendProgressBarUpdate(this, COUNT, this.count);
		listener.sendProgressBarUpdate(this, CAPACITY, this.capacity);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		int current = this.barrel.getBulkStorage().getCount();
		if (current != this.count) {
			this.count = current;
			for (IContainerListener listener : this.listeners) {
				listener.sendProgressBarUpdate(this, COUNT, current);
			}
		}
	}

	@Override
	public void updateProgressBar(int id, int data) {
		if (id == COUNT) {
			this.count = data;
		} else if (id == CAPACITY) {
			this.capacity = data;
		}
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int action) {
		if (!(player instanceof EntityPlayerMP) || player.openContainer != this || !this.canInteractWith(player)
				|| !this.barrel.getWorld().isBlockModifiable(player, this.barrel.getPos())
				|| action != TAKE_ONE && action != TAKE_STACK) {
			return false;
		}

		BarrelBulkStorage storage = this.barrel.getBulkStorage();
		ItemStack requested = storage.withdraw(action == TAKE_ONE ? 1 : Integer.MAX_VALUE, true);
		ItemStack[] inventory = player.inventory.mainInventory;
		ItemStack[] before = new ItemStack[inventory.length];
		for (int slot = 0; slot < inventory.length; slot++) {
			before[slot] = ItemStack.copyItemStack(inventory[slot]);
		}

		ItemStack remainder = ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(player.inventory), requested, false);
		int taken = requested.stackSize - (remainder == null ? 0 : remainder.stackSize);
		if (taken <= 0) {
			return false;
		}

		storage.withdraw(taken, false);
		EntityPlayerMP serverPlayer = (EntityPlayerMP)player;
		for (int slot = 0; slot < inventory.length; slot++) {
			if (!ItemStack.areItemStacksEqual(before[slot], inventory[slot])) {
				serverPlayer.connection.sendPacket(new SPacketSetSlot(-2, slot, inventory[slot]));
			}
		}

		if (storage.getCount() == 0) {
			serverPlayer.closeScreen();
		} else {
			this.detectAndSendChanges();
		}

		return true;
	}
}
