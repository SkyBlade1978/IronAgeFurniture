package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.ironagefurniture.api.Blocks.DrawerUnit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.util.EnumFacing;

public final class TileEntityDrawerUnit extends TileEntityDrawerStorage {
	@Override
	protected boolean isFront(EnumFacing facing) {
		IBlockState state = this.world == null ? null : this.world.getBlockState(this.pos);
		return facing != null && state != null && state.getBlock() instanceof DrawerUnit
			&& state.getValue(DrawerUnit.FACING) == facing;
	}

	public IInventory getWideDrawer(boolean upper, TileEntityDrawerUnit right) {
		String title = upper ? "container.ironagefurniture.upper_drawer" : "container.ironagefurniture.lower_drawer";
		return new InventoryLargeChest(title, this.getDrawerContainer(upper), right.getDrawerContainer(upper));
	}
}
