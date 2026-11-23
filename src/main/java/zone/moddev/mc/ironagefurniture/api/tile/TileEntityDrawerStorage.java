package zone.moddev.mc.ironagefurniture.api.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class TileEntityDrawerStorage extends TileEntity {
	private final Drawer upper = new Drawer("upper_drawer");
	private final Drawer lower = new Drawer("lower_drawer");
	private final CombinedInvWrapper drawers = new CombinedInvWrapper(new InvWrapper(this.upper), new InvWrapper(this.lower));
	private final CombinedInvWrapper front = new CombinedInvWrapper(new InvWrapper(this.upper), new InvWrapper(this.lower)) {
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return stack;
		}
	};

	public IInventory getDrawer(boolean upper) {
		return this.getDrawerContainer(upper);
	}

	protected ILockableContainer getDrawerContainer(boolean upper) {
		return upper ? this.upper : this.lower;
	}

	public boolean isDrawerOpen(boolean upper) {
		return (upper ? this.upper : this.lower).viewers > 0;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.isFront(facing) ? this.front : this.drawers);
		}

		return super.getCapability(capability, facing);
	}

	protected abstract boolean isFront(EnumFacing facing);

	protected void syncDisplay() {
		if (this.world != null && !this.world.isRemote) {
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 2);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		this.upper.writeItems(compound, "UpperItems");
		this.lower.writeItems(compound, "LowerItems");
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.upper.readItems(compound, "UpperItems");
		this.lower.readItems(compound, "LowerItems");
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.writeToNBT(new NBTTagCompound());
		tag.setBoolean("UpperOpen", this.isDrawerOpen(true));
		tag.setBoolean("LowerOpen", this.isDrawerOpen(false));
		return tag;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.upper.viewers = tag.getBoolean("UpperOpen") ? 1 : 0;
		this.lower.viewers = tag.getBoolean("LowerOpen") ? 1 : 0;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		this.handleUpdateTag(packet.getNbtCompound());
		this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
	}

	public NBTTagCompound getUnresolvedItems() {
		NBTTagCompound data = new NBTTagCompound();
		this.upper.inventoryData.writeUnresolved(data, "UpperItems");
		this.lower.inventoryData.writeUnresolved(data, "LowerItems");
		return data;
	}

	public boolean hasRestoredItems() {
		return this.upper.inventoryData.hasRestoredItems() || this.lower.inventoryData.hasRestoredItems();
	}

	private final class Drawer extends InventoryBasic implements ILockableContainer {
		private final StoredInventoryData inventoryData = new StoredInventoryData();
		private int viewers;
		private boolean readingItems;

		Drawer(String name) {
			super("container.ironagefurniture." + name, false, 9);
		}

		@Override
		public void markDirty() {
			super.markDirty();
			if (!this.readingItems) {
				TileEntityDrawerStorage.this.markDirty();
			}
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			return TileEntityDrawerStorage.this.world != null && !TileEntityDrawerStorage.this.isInvalid()
				&& player.getEntityWorld() == TileEntityDrawerStorage.this.world
				&& TileEntityDrawerStorage.this.world.getTileEntity(TileEntityDrawerStorage.this.pos) == TileEntityDrawerStorage.this
				&& player.getDistanceSqToCenter(TileEntityDrawerStorage.this.pos) <= 64;
		}

		@Override
		public void openInventory(EntityPlayer player) {
			this.changeViewers(player, 1);
		}

		@Override
		public void closeInventory(EntityPlayer player) {
			this.changeViewers(player, -1);
		}

		private void changeViewers(EntityPlayer player, int delta) {
			World world = TileEntityDrawerStorage.this.world;
			if (world == null || world.isRemote || player.isSpectator() || TileEntityDrawerStorage.this.isInvalid()) {
				return;
			}

			boolean wasOpen = this.viewers > 0;
			this.viewers = Math.max(0, this.viewers + delta);

			if (wasOpen != (this.viewers > 0)) {
				TileEntityDrawerStorage.this.syncDisplay();
				BlockPos pos = TileEntityDrawerStorage.this.pos;
				world.playSound(null, pos, wasOpen ? SoundEvents.BLOCK_CHEST_CLOSE : SoundEvents.BLOCK_CHEST_OPEN,
					SoundCategory.BLOCKS, 0.35F, 1.25F);
			}
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
			// Drawers do not support locks.
		}

		void writeItems(NBTTagCompound data, String key) {
			this.inventoryData.write(data, key, this.getSizeInventory(), this::getStackInSlot);
		}

		void readItems(NBTTagCompound data, String key) {
			this.readingItems = true;
			try {
				this.clear();

				this.inventoryData.read(data, key, this.getSizeInventory(), this::getStackInSlot, this::setInventorySlotContents);
			} finally {
				this.readingItems = false;
			}
		}
	}
}
