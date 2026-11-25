package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.skysfurniturecore.api.storage.DrawerDefinition;
import zone.moddev.mc.skysfurniturecore.api.storage.DrawerTile;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.ILockableContainer;

/** Retains the two original drawer keys and packet tags. */
public abstract class TileEntityDrawerStorage extends DrawerTile {
	protected TileEntityDrawerStorage() {
		super(new DrawerDefinition("upper", "container.ironagefurniture.upper_drawer", 9,
				new AxisAlignedBB(0, 0.5D, 0, 1, 1, 1), "UpperItems", "UpperOpen"),
			new DrawerDefinition("lower", "container.ironagefurniture.lower_drawer", 9,
				new AxisAlignedBB(0, 0, 0, 1, 0.5D, 1), "LowerItems", "LowerOpen"));
	}

	public IInventory getDrawer(boolean upper) {
		return this.getDrawerContainer(upper);
	}

	protected ILockableContainer getDrawerContainer(boolean upper) {
		return this.getDrawerContainer(upper ? "upper" : "lower");
	}

	public boolean isDrawerOpen(boolean upper) {
		return this.isDrawerOpen(upper ? "upper" : "lower");
	}
}
