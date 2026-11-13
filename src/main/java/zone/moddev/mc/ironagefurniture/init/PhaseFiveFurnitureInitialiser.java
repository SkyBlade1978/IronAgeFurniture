package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.FurnitureFactory;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDiningTable;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityVanillaFoodDisplay;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class PhaseFiveFurnitureInitialiser {
	private static final String TABLE = "table_dining_oak";
	private static final String CABINET = "cabinet_wood_ironage_oak";
	private static Block table;
	private static Block cabinet;

	private PhaseFiveFurnitureInitialiser() { }

	public static void registerBlocks() {
		VanillaSurfaceFoods.register();
		GameRegistry.registerTileEntity(TileEntityDiningTable.class, Ironagefurniture.MODID + ":table_dining");
		GameRegistry.registerTileEntity(TileEntityCabinet.class, Ironagefurniture.MODID + ":cabinet_wood_ironage");
		GameRegistry.registerTileEntity(TileEntityVanillaFoodDisplay.class, Ironagefurniture.MODID + ":vanilla_food_display");
		FurnitureFactory.createSurfaceDisplayBlocker("surface_display_blocker");
		FurnitureFactory.createVanillaFoodDisplay("vanilla_food_display");
		table = FurnitureFactory.createDiningTable(TABLE);
		cabinet = FurnitureFactory.createCabinet(CABINET);
	}

	public static void registerRecipes() {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(table),
			"sss", "t t", 's', new ItemStack(Blocks.WOODEN_SLAB, 1, 0), 't', "stickWood"));
		Object iron = OreDictionary.getOres("nuggetIron", false).isEmpty() ? Items.IRON_INGOT : "nuggetIron";
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(cabinet),
			Blocks.CHEST, iron, new ItemStack(Blocks.PLANKS, 1, 0)));
	}
}
