package zone.moddev.mc.ironagefurniture.api.tile;

import java.util.Arrays;

import zone.moddev.mc.ironagefurniture.api.Blocks.ToolRack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

public final class TileEntityToolRack extends TileEntityDisplayStorage {
	public static final int SLOTS = 2;

	private final ItemStack[] tools = new ItemStack[SLOTS];
	private final StoredInventoryData inventoryData = new StoredInventoryData();

	public ItemStack getTool(int slot) {
		return this.tools[slot];
	}

	public boolean insertTool(int slot, ItemStack stack) {
		if (this.tools[slot] != null || !ToolRack.isTool(stack)) {
			return false;
		}

		this.tools[slot] = stack.copy();
		this.tools[slot].stackSize = 1;
		this.syncTools();
		return true;
	}

	public void removeTool(int slot) {
		this.tools[slot] = null;
		this.syncTools();
	}

	@Override
	protected boolean tryFeed(ItemStack stack, boolean simulate) {
		if (!ToolRack.isTool(stack)) {
			return false;
		}
		for (int slot = 0; slot < SLOTS; slot++) {
			if (this.tools[slot] == null) {
				return simulate || this.insertTool(slot, stack);
			}
		}
		return false;
	}

	private void syncTools() {
		this.markDirty();
		if (this.world != null && !this.world.isRemote) {
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 2);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		this.inventoryData.write(tag, "Items", SLOTS, this::getTool);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		Arrays.fill(this.tools, null);
		this.inventoryData.read(tag, "Items", SLOTS, this::getTool, (slot, stack) -> this.tools[slot] = stack);
	}

	public NBTTagCompound getUnresolvedTools() {
		NBTTagCompound tag = new NBTTagCompound();
		this.inventoryData.writeUnresolved(tag, "Items");
		return tag;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		this.handleUpdateTag(packet.getNbtCompound());
	}
}
