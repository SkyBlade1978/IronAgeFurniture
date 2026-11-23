package zone.moddev.mc.ironagefurniture.api.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/** A feed accepts one displayed item per insertion. Players remove the displayed items. */
abstract class TileEntityDisplayStorage extends TileEntity {
	private final IItemHandler feed = new IItemHandler() {
		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return null;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != 0 || stack == null || stack.stackSize <= 0 || world == null || world.isRemote
					|| isInvalid() || !world.isBlockLoaded(pos) || world.getTileEntity(pos) != TileEntityDisplayStorage.this
					|| !tryFeed(stack, simulate)) {
				return stack;
			}

			return ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - 1);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return null;
		}
	};

	protected abstract boolean tryFeed(ItemStack stack, boolean simulate);

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
			? side != EnumFacing.DOWN : super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return side == EnumFacing.DOWN ? null : CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.feed);
		}

		return super.getCapability(capability, side);
	}
}
