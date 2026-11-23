package zone.moddev.mc.ironagefurniture.api.container;

import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Shows a snapshot of one shelf level so a click always refers to the item the player saw. */
public final class ContainerShelfMedia extends Container {
	private static final int FIRST_ROW_Y = 21;
	private static final int ROW_HEIGHT = 19;
	private static final int PLAYER_INVENTORY_WINDOW = -2;

	private final World world;
	private final BlockPos pos;
	private final TileEntityShelvingUnit tile;
	private final ShelfLevel level;
	private final ShelfContentKind kind;
	private int mediaRevision;

	public ContainerShelfMedia(ShelfContentKind kind) {
		this(null, null, null, false, kind);
	}

	public ContainerShelfMedia(World world, BlockPos pos, TileEntityShelvingUnit tile, boolean upper, ShelfContentKind kind) {
		this.world = world;
		this.pos = pos;
		this.tile = tile;
		this.level = tile == null ? null : tile.getLevel(upper);
		this.kind = kind;
		this.mediaRevision = this.level == null ? 0 : this.level.getMediaRevision();

		int capacity = ShelvingUnit.getMediaCapacity(kind);
		InventoryBasic display = new InventoryBasic("shelvingMedia", false, capacity);
		for (int index = 0; index < capacity; index++) {
			if (this.level != null) {
				display.setInventorySlotContents(index, this.level.copyMediaItem(index));
			}
			this.addSlotToContainer(new Slot(display, index, 11, FIRST_ROW_Y + ROW_HEIGHT * index));
		}
	}

	public ShelfContentKind getMediaKind() {
		return this.kind;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.level != null && player.getEntityWorld() == this.world && this.world.isBlockLoaded(this.pos)
			&& this.world.getBlockState(this.pos).getBlock() instanceof ShelvingUnit
			&& player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D
			&& this.world.getTileEntity(this.pos) == this.tile && this.level.getMediaRevision() == this.mediaRevision
			&& (this.level.getMediaCount() == 0 || this.level.getMediaKind() == this.kind);
	}

	@Override
	public ItemStack slotClick(int index, int mouseButton, ClickType click, EntityPlayer player) {
		return null;
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int index) {
		// Forge uses the vanilla container button packet for selections with no cursor transfer.
		if (!this.canInteractWith(player) || index < 0 || index >= this.inventorySlots.size()) {
			return false;
		}

		Slot row = this.getSlot(index);
		int inventorySlot = player.inventory.getFirstEmptyStack();
		if (!row.getHasStack() || inventorySlot < 0) {
			return false;
		}

		// Keep the other rows in place so a queued click cannot select a different item.
		int shelfIndex = 0;
		for (int rowIndex = 0; rowIndex < index; rowIndex++) {
			if (this.getSlot(rowIndex).getHasStack()) {
				shelfIndex++;
			}
		}

		ItemStack selected = this.level.removeMedia(shelfIndex);
		player.inventory.setInventorySlotContents(inventorySlot, selected);
		player.inventory.markDirty();
		if (player instanceof EntityPlayerMP && ((EntityPlayerMP)player).connection != null) {
			((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(PLAYER_INVENTORY_WINDOW, inventorySlot, selected));
		}

		row.putStack(null);
		this.mediaRevision = this.level.getMediaRevision();
		this.level.markSurfaceSettingChanged();
		return true;
	}
}
