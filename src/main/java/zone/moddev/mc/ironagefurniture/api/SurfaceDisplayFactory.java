package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.api.Blocks.GlassVaseBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceDisplayBlocker;
import zone.moddev.mc.ironagefurniture.api.Blocks.VanillaFoodDisplay;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public final class SurfaceDisplayFactory {

	private SurfaceDisplayFactory() {
	}

	public static Block createGlassVase() {
		return FurnitureRegistration.registerBlock(
				new GlassVaseBlock(Material.GLASS, "ornament_glass_vase", 1, 0.3F), "ornament_glass_vase", 16);
	}

	public static void createSurfaceDisplayBlocker(String name) {
		FurnitureRegistration.registerBlockWithoutItem(new SurfaceDisplayBlocker(), name);
	}

	public static void createVanillaFoodDisplay(String name) {
		FurnitureRegistration.registerBlockWithoutItem(new VanillaFoodDisplay(), name);
	}
}
