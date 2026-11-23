package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBench;
import zone.moddev.mc.ironagefurniture.api.Enumerations.BenchType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public final class TileEntityStorageBench extends TileEntityPaddedBench {
	private final StoredInventoryData inventoryData = new StoredInventoryData();
	private final BenchInventory inventory = new BenchInventory();
	private BenchType pair = BenchType.SINGLE;
	private TileEntityStorageBench combinedWith;
	private IInventory doubleInventory;
	private boolean loading;

	private final InvWrapper handler = new InvWrapper(this.inventory) {
		@Override
		public IInventory getInv() {
			return TileEntityStorageBench.this.getAccessibleInventory();
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return this.isAvailable(slot) ? super.insertItem(slot, stack, simulate) : stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return this.isAvailable(slot) ? super.extractItem(slot, amount, simulate) : null;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (this.isAvailable(slot)) {
				super.setStackInSlot(slot, stack);
			}
		}

		private boolean isAvailable(int slot) {
			return TileEntityStorageBench.this.isLoaded() && slot >= 0 && slot < this.getSlots();
		}
	};

	private boolean isLoaded() {
		return this.world != null && !this.isInvalid() && this.world.isBlockLoaded(this.pos)
			&& this.world.getTileEntity(this.pos) == this;
	}

	public IInventory getInventory() {
		return this.inventory;
	}

	public BenchType getPair() {
		return this.pair;
	}

	public void setPair(BenchType pair) {
		if (this.pair == pair) {
			return;
		}

		this.pair = pair;
		this.clearDoubleInventory();
		this.markDirty();
		IBlockState state = this.world.getBlockState(this.pos);
		this.world.notifyBlockUpdate(this.pos, state, state, 2);
	}

	private TileEntityStorageBench getPartner() {
		return this.pair == BenchType.SINGLE || !this.isLoaded() ? null
			: StorageBench.getPartner(this.world, this.pos, this.world.getBlockState(this.pos), this);
	}

	public IInventory getAccessibleInventory() {
		TileEntityStorageBench partner = this.getPartner();
		if (partner == null) {
			this.clearDoubleInventory();
			return this.inventory;
		}

		return this.pair == BenchType.LEFT ? this.doubleWith(partner) : partner.doubleWith(this);
	}

	private IInventory doubleWith(TileEntityStorageBench right) {
		if (this.combinedWith != right) {
			this.combinedWith = right;
			this.doubleInventory = new InventoryLargeChest("container.ironagefurniture.double_storage_bench", this.inventory, right.inventory) {
				@Override
				public boolean isUsableByPlayer(EntityPlayer player) {
					return super.isUsableByPlayer(player) && TileEntityStorageBench.this.getPartner() == right;
				}
			};
		}
		return this.doubleInventory;
	}

	private void clearDoubleInventory() {
		this.combinedWith = null;
		this.doubleInventory = null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.handler);
		}
		return super.getCapability(capability, side);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		this.writeAppearance(tag);
		this.inventoryData.write(tag, "Items", this.inventory.getSizeInventory(), this.inventory::getStackInSlot);
		return tag;
	}

	private NBTTagCompound writeAppearance(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setString("Pair", this.pair.getName());
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		String savedPair = tag.getString("Pair");
		this.pair = "left".equals(savedPair) ? BenchType.LEFT : "right".equals(savedPair) ? BenchType.RIGHT : BenchType.SINGLE;
		this.clearDoubleInventory();

		this.loading = true;
		try {
			this.inventory.clear();
			this.inventoryData.read(tag, "Items", this.inventory.getSizeInventory(),
				this.inventory::getStackInSlot, this.inventory::setInventorySlotContents);
		} finally {
			this.loading = false;
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeAppearance(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
		super.onDataPacket(networkManager, packet);
		if (this.world != null) {
			// Either half's packet can complete the client's view of the join.
			this.world.markBlockRangeForRenderUpdate(this.pos.add(-1, 0, -1), this.pos.add(1, 0, 1));
		}
	}

	public NBTTagCompound getUnresolvedItems() {
		NBTTagCompound tag = new NBTTagCompound();
		this.inventoryData.writeUnresolved(tag, "Items");
		return tag;
	}

	private final class BenchInventory extends InventoryBasic implements ILockableContainer {
		BenchInventory() {
			super("container.ironagefurniture.storage_bench", false, 27);
		}

		@Override
		public void markDirty() {
			super.markDirty();
			if (!TileEntityStorageBench.this.loading) {
				TileEntityStorageBench.this.markDirty();
			}
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			return TileEntityStorageBench.this.isLoaded() && player.getEntityWorld() == TileEntityStorageBench.this.world
				&& player.getDistanceSqToCenter(TileEntityStorageBench.this.pos) <= 64;
		}

		@Override
		public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
			return new ContainerChest(inventory, this, player);
		}

		@Override
		public String getGuiID() {
			return "minecraft:chest";
		}

		@Override
		public boolean isLocked() {
			return false;
		}

		@Override
		public LockCode getLockCode() {
			return LockCode.EMPTY_CODE;
		}

		@Override
		public void setLockCode(LockCode code) {
			// The storage bench has no lock mechanism.
		}
	}
}
