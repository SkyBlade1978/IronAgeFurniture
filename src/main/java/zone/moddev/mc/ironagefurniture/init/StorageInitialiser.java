package zone.moddev.mc.ironagefurniture.init;

import java.util.LinkedHashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.api.StorageFactory;
import zone.moddev.mc.ironagefurniture.api.recipes.SideBarrelRecipe;
import zone.moddev.mc.ironagefurniture.api.recipes.UprightBarrelRecipe;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

final class StorageInitialiser {
	private static final Map<String, Block> CABINETS = new LinkedHashMap<>();
	private static final Map<String, Block> HALF_CABINETS = new LinkedHashMap<>();
	private static final Map<String, Block> BARRELS = new LinkedHashMap<>();
	private static final Map<String, Block> SIDE_BARRELS = new LinkedHashMap<>();
	private static final Map<String, Block> BOTTLE_RACKS = new LinkedHashMap<>();
	private static final Map<String, Block> SHELVING_UNITS = new LinkedHashMap<>();

	private StorageInitialiser() {
	}

	static void registerBlocks(String suffix) {
		if (IronAgeFurnitureConfiguration.GENERATE_CABINETS) {
			CABINETS.put(suffix, StorageFactory.createCabinet("cabinet_wood_ironage_" + suffix));

			if (IronAgeFurnitureConfiguration.GENERATE_HALF_CABINETS) {
				HALF_CABINETS.put(suffix, StorageFactory.createHalfCabinet("half_cabinet_wood_ironage_" + suffix));
			}
		}

		if (IronAgeFurnitureConfiguration.GENERATE_BARRELS) {
			BARRELS.put(suffix, StorageFactory.createBarrel("barrel_wood_ironage_" + suffix, false));

			if (IronAgeFurnitureConfiguration.GENERATE_SIDE_BARRELS) {
				SIDE_BARRELS.put(suffix, StorageFactory.createBarrel("side_barrel_wood_ironage_" + suffix, true));
			}
		}

		if (IronAgeFurnitureConfiguration.GENERATE_BOTTLE_RACKS) {
			BOTTLE_RACKS.put(suffix, StorageFactory.createBottleRack("bottle_rack_wood_ironage_" + suffix));
		}
	}

	static void registerRecipeTypes() {
		RecipeSorter.register("ironagefurniture:side_barrel", SideBarrelRecipe.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("ironagefurniture:upright_barrel", UprightBarrelRecipe.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
	}

	static void registerShelvingUnits() {
		if (!IronAgeFurnitureConfiguration.GENERATE_SHELVING_UNITS) {
			return;
		}

		for (String wood : WoodVariantHelper.getEnabledWoodSuffixes()) {
			SHELVING_UNITS.put(wood, StorageFactory.createShelvingUnit("shelving_unit_wood_ironage_" + wood));
		}
	}

	static void registerShelvingUnitRecipe(String wood, ItemStack slab) {
		Block unit = SHELVING_UNITS.get(wood);

		if (unit != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(unit), "t t", "sss", "t t",
				's', slab, 't', "stickWood"));
		}
	}

	static void registerBottleRackRecipe(String suffix, ItemStack slab) {
		Block rack = BOTTLE_RACKS.get(suffix);

		if (rack != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rack, 2), "s s", " t ", "s s",
				's', slab, 't', "stickWood"));
		}
	}

	static void registerCabinetAndBarrelRecipes(String suffix, ItemStack plank) {
		Block cabinet = CABINETS.get(suffix);
		Block halfCabinet = HALF_CABINETS.get(suffix);

		if (cabinet != null) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(cabinet), Blocks.CHEST, plank));
		}

		if (halfCabinet != null) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(halfCabinet, 2), cabinet));
		}

		Block barrel = BARRELS.get(suffix);
		Block sideBarrel = SIDE_BARRELS.get(suffix);

		if (barrel != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(barrel, 3), "ppp", " i ", "ppp",
				'p', plank, 'i', Items.IRON_INGOT));
		}

		if (sideBarrel != null) {
			GameRegistry.addRecipe(new SideBarrelRecipe(barrel, sideBarrel));
			GameRegistry.addRecipe(new UprightBarrelRecipe(sideBarrel, barrel));
		}
	}
}
