package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockCrate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class TileEntityCrate extends TileEntity {
	public static final int SLOTS = 16;
	public static final String SEALED_TAG = "Sealed";

	private final FurnitureLabel label = new FurnitureLabel();
	private final StoredInventoryData inventoryData = new StoredInventoryData();
	private final InventoryBasic inventory = new InventoryBasic("container.ironagefurniture.crate", false, SLOTS) {
		@Override
		public void markDirty() {
			super.markDirty();
			if (!TileEntityCrate.this.reading) {
				TileEntityCrate.this.markDirty();
			}
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			return !ItemBlockCrate.isPacked(stack);
		}
	};
	private final InvWrapper port = new InvWrapper(this.inventory) {
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (TileEntityCrate.this.isSealed() || TileEntityCrate.this.isInvalid() || ItemBlockCrate.isPacked(stack)) {
				return stack;
			}
			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return TileEntityCrate.this.isSealed() || TileEntityCrate.this.isInvalid()
				? null : super.extractItem(slot, amount, simulate);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return TileEntityCrate.this.isSealed() || TileEntityCrate.this.isInvalid()
				? null : super.getStackInSlot(slot);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (!TileEntityCrate.this.isSealed() && !TileEntityCrate.this.isInvalid() && !ItemBlockCrate.isPacked(stack)) {
				super.setStackInSlot(slot, stack);
			}
		}
	};
	private boolean sealed;
	private boolean reading;

	public IInventory getInventory() {
		return this.inventory;
	}

	public boolean isSealed() {
		return this.sealed;
	}

	public void setSealed(boolean sealed) {
		if (this.sealed != sealed) {
			this.sealed = sealed;
			this.sync();
		}
	}

	public boolean canOpen(EntityPlayer player) {
		return !this.sealed && !this.isInvalid() && this.world != null && player.getEntityWorld() == this.world
			&& this.world.getTileEntity(this.pos) == this && player.getDistanceSqToCenter(this.pos) <= 64;
	}

	public FurnitureLabel getCrateLabel() {
		return this.label;
	}

	public boolean writeLabel(ItemStack sign, String text) {
		if (!FurnitureLabel.isSign(sign)) {
			return false;
		}
		this.label.set(sign, text);
		this.sync();
		return true;
	}

	public ItemStack removeLabel() {
		ItemStack sign = this.label.remove();
		this.sync();
		return sign;
	}

	public boolean hasRestoredItems() {
		return this.inventoryData.hasRestoredItems();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
			? !this.sealed : super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return this.sealed ? null : CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.port);
		}
		return super.getCapability(capability, facing);
	}

	public NBTTagCompound writeDropData() {
		NBTTagCompound data = new NBTTagCompound();
		if (this.sealed) {
			this.writeContents(data);
			this.writeAppearance(data);
		} else {
			this.inventoryData.writeUnresolved(data, "Items");
		}
		return data;
	}

	private void writeContents(NBTTagCompound data) {
		this.inventoryData.write(data, "Items", SLOTS, this.inventory::getStackInSlot);
	}

	private void writeAppearance(NBTTagCompound data) {
		data.setBoolean(SEALED_TAG, this.sealed);
		this.label.writeToNBT(data);
	}

	private void readAppearance(NBTTagCompound data) {
		this.sealed = data.getBoolean(SEALED_TAG);
		this.label.readFromNBT(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.reading = true;
		try {
			this.inventory.clear();
			this.inventoryData.read(data, "Items", SLOTS, this.inventory::getStackInSlot,
				this.inventory::setInventorySlotContents);
		} finally {
			this.reading = false;
		}
		this.readAppearance(data);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		this.writeContents(data);
		this.writeAppearance(data);
		return data;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	private void sync() {
		this.markDirty();
		if (this.world != null && !this.world.isRemote) {
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 2);
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.writeToNBT(new NBTTagCompound());
		this.writeAppearance(tag);
		return tag;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.readAppearance(tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		this.handleUpdateTag(packet.getNbtCompound());
		this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
	}
}
