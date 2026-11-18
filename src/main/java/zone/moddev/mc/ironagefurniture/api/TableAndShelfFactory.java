package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.LowTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public final class TableAndShelfFactory {

	private TableAndShelfFactory() {
	}

	public static Block createDiningTable(String name) {
		return FurnitureRegistration.registerBlock(new DiningTable(Material.WOOD, 10, 1), name, 16);
	}

	public static Block createLowTable(String name) {
		return FurnitureRegistration.registerBlock(new LowTable(Material.WOOD, name, 10, 1), name, 16);
	}

	public static Block createWallShelf(String name) {
		return FurnitureRegistration.registerBlock(new WallShelf(Material.WOOD, name, 10, 1), name, 16);
	}
}
