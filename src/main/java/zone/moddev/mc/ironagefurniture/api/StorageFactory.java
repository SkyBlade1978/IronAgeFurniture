package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.api.Blocks.BedsideCabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.BottleRack;
import zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.DrawerUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.HalfCabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.SideBarrel;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBench;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageCrate;
import zone.moddev.mc.ironagefurniture.api.Blocks.ToolRack;

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

	public static Block createStorageBench(String name) {
		return FurnitureRegistration.registerBlock(new StorageBench(name), name, 16);
	}

	public static Block createDrawerUnit(String name, boolean halfDepth) {
		return FurnitureRegistration.registerBlock(new DrawerUnit(halfDepth), name, 64);
	}

	public static Block createBedsideCabinet(String name) {
		return FurnitureRegistration.registerBlock(new BedsideCabinet(Material.WOOD, 10, 1.5F), name, 64);
	}

	public static Block createCrate(String name) {
		return FurnitureRegistration.registerBlock(new StorageCrate(), name, 64);
	}

	public static Block createBarrel(String name, boolean side) {
		return FurnitureRegistration.registerBlock(
				side ? new SideBarrel(Material.WOOD, name, 10, 1.5F)
						: new StorageBarrel(Material.WOOD, name, 10, 1.5F),
				name, 16);
	}

	public static Block createToolRack(String name) {
		return FurnitureRegistration.registerBlock(new ToolRack(), name, 64);
	}

	public static void registerProduceFurniture() {
		FurnitureRegistration.registerBlock(new ProduceFurniture(false), ProduceFurniture.TRAY_ID, 64);
		FurnitureRegistration.registerBlock(new ProduceFurniture(true), ProduceFurniture.RACK_ID, 64);
	}

	public static Block createBottleRack(String name) {
		return FurnitureRegistration.registerBlock(new BottleRack(Material.WOOD, name, 10, 1), name, 16);
	}

	public static Block createShelvingUnit(String name) {
		return FurnitureRegistration.registerBlock(new ShelvingUnit(Material.WOOD, 10, 1), name, 16);
	}
}
