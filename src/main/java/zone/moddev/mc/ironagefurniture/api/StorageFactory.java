package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.api.Blocks.BottleRack;
import zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.HalfCabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.SideBarrel;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public final class StorageFactory {

	private StorageFactory() {
	}

	public static Block createCabinet(String name) {
		return FurnitureRegistration.registerBlock(new Cabinet(Material.WOOD, 10, 1.5F), name, 64);
	}

	public static Block createHalfCabinet(String name) {
		return FurnitureRegistration.registerBlock(new HalfCabinet(Material.WOOD, name, 10, 1.5F), name, 64);
	}

	public static Block createBarrel(String name, boolean side) {
		return FurnitureRegistration.registerBlock(
				side ? new SideBarrel(Material.WOOD, name, 10, 1.5F)
						: new StorageBarrel(Material.WOOD, name, 10, 1.5F),
				name, 16);
	}

	public static Block createBottleRack(String name) {
		return FurnitureRegistration.registerBlock(new BottleRack(Material.WOOD, name, 10, 1), name, 16);
	}
}
