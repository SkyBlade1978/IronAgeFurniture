package zone.moddev.mc.ironagefurniture.init;

import java.util.LinkedHashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.api.TableAndShelfFactory;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

final class TableAndShelfInitialiser {
	private static final Map<String, Block> TABLES = new LinkedHashMap<>();
	private static final Map<String, Block> LOW_TABLES = new LinkedHashMap<>();
	private static final Map<String, Block> SHELVES = new LinkedHashMap<>();

	private TableAndShelfInitialiser() {
	}

	static void registerBlocks(String suffix) {
		if (IronAgeFurnitureConfiguration.GENERATE_DINING_TABLES) {
			TABLES.put(suffix, TableAndShelfFactory.createDiningTable("table_dining_" + suffix));
		}

		if (IronAgeFurnitureConfiguration.GENERATE_LOW_TABLES) {
			LOW_TABLES.put(suffix, TableAndShelfFactory.createLowTable("table_low_" + suffix));
		}

		if (IronAgeFurnitureConfiguration.GENERATE_WALL_SHELVES) {
			SHELVES.put(suffix, TableAndShelfFactory.createWallShelf("shelf_wall_" + suffix));
		}
	}

	static void registerRecipes(String suffix, ItemStack slab) {
		Block table = TABLES.get(suffix);

		if (table != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(table), "sss", "t t",
				's', slab, 't', "stickWood"));
		}

		Block lowTable = LOW_TABLES.get(suffix);

		if (lowTable != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lowTable), "sss", " s ", "t t",
				's', slab, 't', "stickWood"));
		}

		Block shelf = SHELVES.get(suffix);

		if (shelf != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(shelf, 3), "sss", " t ",
				's', slab, 't', "stickWood"));
		}
	}
}
