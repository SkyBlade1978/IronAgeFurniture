package zone.moddev.mc.ironagefurniture.init;

import java.util.LinkedHashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.StorageFactory;
import zone.moddev.mc.ironagefurniture.api.recipes.SideBarrelRecipe;
import zone.moddev.mc.ironagefurniture.api.recipes.StorageBenchPaddingRecipe;
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
	private static final Map<String, Block> BEDSIDE_CABINETS = new LinkedHashMap<>();
	private static final Map<String, Block> DRAWER_UNITS = new LinkedHashMap<>();
	private static final Map<String, Block> HALF_DRAWER_UNITS = new LinkedHashMap<>();
	private static final Map<String, Block> STORAGE_BENCHES = new LinkedHashMap<>();
	private static final Map<String, Block> CRATES = new LinkedHashMap<>();
	private static final Map<String, Block> BARRELS = new LinkedHashMap<>();
	private static final Map<String, Block> SIDE_BARRELS = new LinkedHashMap<>();
	private static final Map<String, Block> TOOL_RACKS = new LinkedHashMap<>();
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

		if (IronAgeFurnitureConfiguration.GENERATE_BEDSIDE_CABINETS) {
			BEDSIDE_CABINETS.put(suffix, StorageFactory.createBedsideCabinet("bedside_cabinet_wood_ironage_" + suffix));
		}

		if (IronAgeFurnitureConfiguration.GENERATE_DRAWER_UNITS) {
			DRAWER_UNITS.put(suffix, StorageFactory.createDrawerUnit("drawer_unit_wood_ironage_" + suffix, false));
			HALF_DRAWER_UNITS.put(suffix, StorageFactory.createDrawerUnit("half_drawer_unit_wood_ironage_" + suffix, true));
		}

		if (IronAgeFurnitureConfiguration.GENERATE_STORAGE_BENCHES && getWoodBench(suffix) != null) {
			STORAGE_BENCHES.put(suffix, StorageFactory.createStorageBench("storage_bench_wood_ironage_" + suffix));
		}

		if (IronAgeFurnitureConfiguration.GENERATE_CRATES) {
			CRATES.put(suffix, StorageFactory.createCrate("crate_wood_ironage_" + suffix));
		}

		if (IronAgeFurnitureConfiguration.GENERATE_TOOL_RACKS) {
			TOOL_RACKS.put(suffix, StorageFactory.createToolRack("tool_rack_wood_ironage_" + suffix));
		}

		if (IronAgeFurnitureConfiguration.GENERATE_BOTTLE_RACKS) {
			BOTTLE_RACKS.put(suffix, StorageFactory.createBottleRack("bottle_rack_wood_ironage_" + suffix));
		}
	}

	private static Block getWoodBench(String suffix) {
		return Ironagefurniture.BlockRegistry.get("chair_wood_ironage_bench_single_" + suffix);
	}

	static void registerStorageBenchRecipes() {
		for (Map.Entry<String, Block> entry : STORAGE_BENCHES.entrySet()) {
			Block storageBench = entry.getValue();
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(storageBench), getWoodBench(entry.getKey()), Blocks.CHEST));

			for (PaddedBenchColour colour : PaddedBenchColour.values()) {
				GameRegistry.addRecipe(new StorageBenchPaddingRecipe(storageBench, colour));
			}
		}
	}

	static void registerRecipeTypes() {
		RecipeSorter.register("ironagefurniture:storage_bench_padding", StorageBenchPaddingRecipe.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
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

		Block bedsideCabinet = BEDSIDE_CABINETS.get(suffix);

		if (bedsideCabinet != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(bedsideCabinet), "ppp", "psp", "psp",
				'p', plank, 's', "stickWood"));
		}

		Block drawers = DRAWER_UNITS.get(suffix);

		if (drawers != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(drawers), "ppp", "psp", "ppp",
				'p', plank, 's', "stickWood"));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HALF_DRAWER_UNITS.get(suffix)), "ppp", " s ", "ppp",
				'p', plank, 's', "stickWood"));
		}

		Block toolRack = TOOL_RACKS.get(suffix);
		if (toolRack != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(toolRack, 2), "ppp", "s s",
				'p', plank, 's', "stickWood"));
		}

		Block crate = CRATES.get(suffix);
		if (crate != null) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crate), "ppp", "s s", "ppp",
				'p', plank, 's', "stickWood"));
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
