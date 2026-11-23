package zone.moddev.mc.ironagefurniture.api.tile;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/** A single kind of solid stored in bulk, with normal-sized stacks at the interaction boundaries. */
public final class BarrelBulkStorage {
	private static final String CONTENTS_TAG = "BulkContents";
	private static final int GRAIN_CAPACITY = 512;
	private static final int INGREDIENT_CAPACITY = 256;
	private static final int PRODUCE_CAPACITY = 128;

	private final TileEntityBarrel barrel;
	private final IItemHandler input = new HopperPort(true);
	private final IItemHandler output = new HopperPort(false);
	private ItemStack contents;
	private int count;
	private int revision;

	BarrelBulkStorage(TileEntityBarrel barrel) {
		this.barrel = barrel;
	}

	public static int capacityFor(ItemStack stack) {
		if (stack == null || stack.stackSize <= 0) {
			return 0;
		}

		Item item = stack.getItem();
		if (item == Items.WHEAT || item == Items.WHEAT_SEEDS || item == Items.PUMPKIN_SEEDS
				|| item == Items.MELON_SEEDS || item == Items.BEETROOT_SEEDS) {
			return GRAIN_CAPACITY;
		}

		if (item == Items.SUGAR || item == Items.NETHER_WART
				|| (item == Items.DYE && stack.getMetadata() == EnumDyeColor.BROWN.getDyeDamage())) {
			return INGREDIENT_CAPACITY;
		}

		if (item == Items.CARROT || item == Items.POTATO || item == Items.BEETROOT
				|| item == Item.getItemFromBlock(Blocks.RED_MUSHROOM) || item == Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)
				|| (item == Items.FISH && stack.getMetadata() >= 0 && stack.getMetadata() <= 3)) {
			return PRODUCE_CAPACITY;
		}

		return 0;
	}

	public int getCount() {
		return this.count;
	}

	public int getCapacity() {
		return capacityFor(this.contents);
	}

	/** Changes when a batch starts or empties so an old screen cannot withdraw from a new batch. */
	public int getRevision() {
		return this.revision;
	}

	public ItemStack getStack() {
		return this.contents == null ? null : this.contents.copy();
	}

	public int deposit(ItemStack stack, boolean simulate) {
		int capacity = capacityFor(stack);
		if (capacity == 0 || !this.barrel.isBulkBarrel() || !simulate && this.barrel.getWorld().isRemote
				|| this.barrel.getFluidAmount() > 0
				|| (this.contents != null && !ItemHandlerHelper.canItemStacksStack(this.contents, stack))) {
			return 0;
		}

		int amount = Math.min(stack.stackSize, capacity - this.count);
		if (!simulate && amount > 0) {
			if (this.contents == null) {
				this.contents = ItemHandlerHelper.copyStackWithSize(stack, 1);
				this.revision++;
			}

			this.count += amount;
			this.barrel.markForContentsUpdate();
		}

		return amount;
	}

	public ItemStack withdraw(int amount, boolean simulate) {
		ItemStack stack = this.barrel.isBulkBarrel() && (simulate || !this.barrel.getWorld().isRemote)
			? this.peek(amount) : null;
		if (stack != null && !simulate) {
			this.remove(stack.stackSize);
			this.barrel.markForContentsUpdate();
		}

		return stack;
	}

	private ItemStack peek(int amount) {
		return this.contents == null || amount <= 0 ? null : ItemHandlerHelper.copyStackWithSize(this.contents,
			Math.min(Math.min(amount, this.contents.getMaxStackSize()), this.count));
	}

	private void remove(int amount) {
		this.count -= amount;
		if (this.count == 0) {
			this.contents = null;
			this.revision++;
		}
	}

	/** Called by the owning block before Forge discards the tile on break. */
	public ItemStack takeDropStack() {
		ItemStack drop = this.peek(Integer.MAX_VALUE);
		if (drop != null) {
			this.remove(drop.stackSize);
		}

		return drop;
	}

	IItemHandler getPort(EnumFacing side) {
		return side == EnumFacing.UP ? this.input : side == EnumFacing.DOWN ? this.output : null;
	}

	void readFromNBT(NBTTagCompound tag) {
		NBTTagCompound bulk = tag.getCompoundTag(CONTENTS_TAG);
		ItemStack stored = ItemStack.loadItemStackFromNBT(bulk.getCompoundTag("Stack"));
		this.count = Math.max(0, Math.min(capacityFor(stored), bulk.getInteger("Amount")));
		this.contents = this.count == 0 ? null : ItemHandlerHelper.copyStackWithSize(stored, 1);
		this.revision++;
	}

	void writeToNBT(NBTTagCompound tag) {
		if (this.contents == null) {
			tag.removeTag(CONTENTS_TAG);
			return;
		}

		NBTTagCompound bulk = new NBTTagCompound();
		bulk.setTag("Stack", this.contents.writeToNBT(new NBTTagCompound()));
		bulk.setInteger("Amount", this.count);
		tag.setTag(CONTENTS_TAG, bulk);
	}

	private final class HopperPort implements IItemHandler {
		private final boolean inputPort;

		HopperPort(boolean inputPort) {
			this.inputPort = inputPort;
		}

		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return slot == 0 && barrel.isBulkBarrel() ? peek(Integer.MAX_VALUE) : null;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != 0 || !this.inputPort || stack == null) {
				return stack;
			}

			int accepted = deposit(stack, simulate);
			return accepted == 0 ? stack : ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - accepted);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return slot == 0 && !this.inputPort ? withdraw(amount, simulate) : null;
		}
	}
}
