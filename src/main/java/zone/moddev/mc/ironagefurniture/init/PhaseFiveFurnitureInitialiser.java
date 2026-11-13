package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import java.util.LinkedHashMap;
import java.util.Map;
import zone.moddev.mc.ironagefurniture.api.FurnitureFactory;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDiningTable;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityVanillaFoodDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;
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
	private static final Map<String, Block> tables = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> lowTables = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> shelves = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> cabinets = new LinkedHashMap<String, Block>();

	private PhaseFiveFurnitureInitialiser() { }

	public static void registerBlocks() {
		VanillaSurfaceFoods.register();
		GameRegistry.registerTileEntity(TileEntityDiningTable.class, Ironagefurniture.MODID + ":table_dining");
		GameRegistry.registerTileEntity(TileEntityCabinet.class, Ironagefurniture.MODID + ":cabinet_wood_ironage");
		GameRegistry.registerTileEntity(TileEntityVanillaFoodDisplay.class, Ironagefurniture.MODID + ":vanilla_food_display");
		GameRegistry.registerTileEntity(TileEntityWallShelf.class, Ironagefurniture.MODID + ":shelf_wall");
		FurnitureFactory.createSurfaceDisplayBlocker("surface_display_blocker");
		FurnitureFactory.createVanillaFoodDisplay("vanilla_food_display");
		for (String suffix : WoodVariantHelper.getEnabledWoodSuffixes()) {
			if (IronAgeFurnitureConfiguration.GENERATE_DINING_TABLES) tables.put(suffix, FurnitureFactory.createDiningTable("table_dining_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_LOW_TABLES) lowTables.put(suffix, FurnitureFactory.createLowTable("table_low_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_WALL_SHELVES) shelves.put(suffix, FurnitureFactory.createWallShelf("shelf_wall_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_CABINETS) cabinets.put(suffix, FurnitureFactory.createCabinet("cabinet_wood_ironage_" + suffix));
		}
	}

	public static void registerRecipes() {
		WoodVariantHelper.forEachEnabledSlabVariant((suffix, slab) -> {
			if (tables.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tables.get(suffix)), "sss", "t t", 's', slab, 't', "stickWood"));
			if (lowTables.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lowTables.get(suffix)), "sss", " s ", "t t", 's', slab, 't', "stickWood"));
			if (shelves.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(shelves.get(suffix), 3), "sss", " t ", 's', slab, 't', "stickWood"));
		});
		WoodVariantHelper.forEachEnabledPlankVariant((suffix, plank) -> {
			Object iron = OreDictionary.getOres("nuggetIron", false).isEmpty() ? Items.IRON_INGOT : "nuggetIron";
			if (cabinets.containsKey(suffix)) GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(cabinets.get(suffix)), Blocks.CHEST, iron, plank));
		});
	}
}
