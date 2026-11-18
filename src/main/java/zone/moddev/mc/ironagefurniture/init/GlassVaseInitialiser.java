package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.SurfaceDisplayFactory;
import zone.moddev.mc.ironagefurniture.api.surface.GlassVaseSurface;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityGlassVase;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

final class GlassVaseInitialiser {
	private static Block glassVase;

	private GlassVaseInitialiser() {
	}

	static void registerBlock() {
		GameRegistry.registerTileEntityWithAlternatives(TileEntityGlassVase.class,
			Ironagefurniture.MODID + ":ornament_glass_vase", Ironagefurniture.MODID + ":glass_vase");

		if (IronAgeFurnitureConfiguration.GENERATE_GLASS_VASES) {
			glassVase = SurfaceDisplayFactory.createGlassVase();
			GlassVaseSurface.register(Item.getItemFromBlock(glassVase));
		}
	}

	static void registerRecipes() {
		if (glassVase == null) {
			return;
		}

		for (EnumDyeColor colour : EnumDyeColor.values()) {
			int metadata = colour.getMetadata();
			ItemStack glass = new ItemStack(Blocks.STAINED_GLASS, 1, metadata);

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(glassVase, 4, metadata),
				" x ", "x x", " x ", 'x', glass));
		}
	}
}
