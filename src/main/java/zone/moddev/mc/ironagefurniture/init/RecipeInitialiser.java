package zone.moddev.mc.ironagefurniture.init;

import java.util.Map;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.CfmChairMigration;
import zone.moddev.mc.ironagefurniture.api.LightingFactory;
import zone.moddev.mc.ironagefurniture.api.SeatingFactory;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeInitialiser {
	protected RecipeInitialiser() {
		throw new IllegalAccessError("This class cannot be instansiated");
	}

	public static void init() {
		registerCustomRecipeTypes();
		generateChairRecipes();
		generateLightRecipes();
		generateBedAndChairUpgradeRecipes();
		generateSurfaceFurnitureRecipes();
		SconceAndCandleRecipes.register();
		WoodVariantHelper.forEachEnabledSlabVariant(DiningChairInitialiser::registerRecipe);
	}

	private static void generateBedAndChairUpgradeRecipes() {
		WoodVariantHelper.forEachEnabledPlankVariant((suffix, planks) -> {
			ChairUpgradeInitialiser.registerRecipes(suffix, planks);
			BedInitialiser.registerSingleBedRecipes(suffix, planks);
		});

		for (String suffix : WoodVariantHelper.getEnabledWoodSuffixes()) {
			BedInitialiser.registerDoubleBedRecipes(suffix);
		}
	}

	private static void generateSurfaceFurnitureRecipes() {
		StorageInitialiser.registerRecipeTypes();
		GlassVaseInitialiser.registerRecipes();

		WoodVariantHelper.forEachEnabledSlabVariant((suffix, slab) -> {
			StorageInitialiser.registerBottleRackRecipe(suffix, slab);
			TableAndShelfInitialiser.registerRecipes(suffix, slab);
		});

		WoodVariantHelper.forEachEnabledPlankVariant(StorageInitialiser::registerCabinetAndBarrelRecipes);
	}

	private static void registerCustomRecipeTypes() {
		RecipeSorter.register("ironagefurniture:shield_chair",
			zone.moddev.mc.ironagefurniture.api.recipes.ShieldChairRecipe.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("ironagefurniture:matching_upholstery",
			zone.moddev.mc.ironagefurniture.api.recipes.MatchingUpholsteryRecipe.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("ironagefurniture:bed_recolour",
			zone.moddev.mc.ironagefurniture.api.recipes.BedRecolourRecipe.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("ironagefurniture:upholstery_upgrade",
			zone.moddev.mc.ironagefurniture.api.recipes.UpholsteryUpgradeRecipe.class,
			RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
	}

	private static void generateLightRecipes() {
		if (!IronAgeFurnitureConfiguration.GENERATE_LIGHTS) {
			return;
		}
		if (IronAgeFurnitureConfiguration.GENERATE_SCONCES) {
			LightingFactory.AddIronSconceRecipe(
					BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron);
		}
		if (IronAgeFurnitureConfiguration.GENERATE_GLOW_LAMPS) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(
					BlockObjectHolder.light_metal_ironage_block_floor_glow_clear),
					Items.GLOWSTONE_DUST, Items.GLASS_BOTTLE));
		}
		if (IronAgeFurnitureConfiguration.GENERATE_REDSTONE_LAMPS) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(
					BlockObjectHolder.light_metal_ironage_block_floor_red_clear),
					Items.REDSTONE, Items.GLASS_BOTTLE));
		}
		if (IronAgeFurnitureConfiguration.GENERATE_LAVA_LAMPS) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(
					BlockObjectHolder.light_metal_ironage_block_floor_lava_clear, 8),
					"yyy", "yxy", "yyy", 'y', Items.GLASS_BOTTLE, 'x', Items.LAVA_BUCKET));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.OBSIDIAN),
					"yyy", "yyy", "yyy", 'y', BlockObjectHolder.obsidian_chunk));
		}
	}


	private static void generateChairRecipes() {

		if (IronAgeFurnitureConfiguration.CFM_CONVERSION_RECIPES && Loader.isModLoaded("cfm")) {
			for (Map.Entry<String, String> chair : CfmChairMigration.chairMappings().entrySet()) {
				Block oldChair = Block.getBlockFromName(chair.getKey());
				Block newChair = Ironagefurniture.BlockRegistry.get(chair.getValue());
				if (oldChair != null && newChair != null) {
					SeatingFactory.AddChairConversionRecipe(oldChair, newChair);
				}
			}
		}


		if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
			SeatingFactory.AddClassicChairRecipe(new ItemStack(Blocks.PLANKS, 1, 0), BlockObjectHolder.chair_wood_ironage_classic_oak);
			SeatingFactory.AddClassicChairRecipe(new ItemStack(Blocks.PLANKS, 1, 1), BlockObjectHolder.chair_wood_ironage_classic_spruce);
			SeatingFactory.AddClassicChairRecipe(new ItemStack(Blocks.PLANKS, 1, 2), BlockObjectHolder.chair_wood_ironage_classic_birch);
			SeatingFactory.AddClassicChairRecipe(new ItemStack(Blocks.PLANKS, 1, 3), BlockObjectHolder.chair_wood_ironage_classic_jungle);
			SeatingFactory.AddClassicChairRecipe(new ItemStack(Blocks.PLANKS, 1, 4), BlockObjectHolder.chair_wood_ironage_classic_acacia);
			SeatingFactory.AddClassicChairRecipe(new ItemStack(Blocks.PLANKS, 1, 5), BlockObjectHolder.chair_wood_ironage_classic_big_oak);
		}

		if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
			SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_oak, BlockObjectHolder.chair_wood_ironage_shield_oak);
			SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_spruce, BlockObjectHolder.chair_wood_ironage_shield_spruce);
			SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_birch, BlockObjectHolder.chair_wood_ironage_shield_birch);
			SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_jungle, BlockObjectHolder.chair_wood_ironage_shield_jungle);
			SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_acacia, BlockObjectHolder.chair_wood_ironage_shield_acacia);
			SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_big_oak, BlockObjectHolder.chair_wood_ironage_shield_big_oak);
		}

		if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
			SeatingFactory.AddShortStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 0), BlockObjectHolder.chair_wood_ironage_stool_short_oak);
			SeatingFactory.AddShortStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 1), BlockObjectHolder.chair_wood_ironage_stool_short_spruce);
			SeatingFactory.AddShortStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 2), BlockObjectHolder.chair_wood_ironage_stool_short_birch);
			SeatingFactory.AddShortStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 3), BlockObjectHolder.chair_wood_ironage_stool_short_jungle);
			SeatingFactory.AddShortStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 4), BlockObjectHolder.chair_wood_ironage_stool_short_acacia);
			SeatingFactory.AddShortStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 5), BlockObjectHolder.chair_wood_ironage_stool_short_big_oak);
		}

		if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
			SeatingFactory.AddTallStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 0), BlockObjectHolder.chair_wood_ironage_stool_tall_oak);
			SeatingFactory.AddTallStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 1), BlockObjectHolder.chair_wood_ironage_stool_tall_spruce);
			SeatingFactory.AddTallStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 2), BlockObjectHolder.chair_wood_ironage_stool_tall_birch);
			SeatingFactory.AddTallStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 3), BlockObjectHolder.chair_wood_ironage_stool_tall_jungle);
			SeatingFactory.AddTallStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 4), BlockObjectHolder.chair_wood_ironage_stool_tall_acacia);
			SeatingFactory.AddTallStoolRecipe(new ItemStack(Blocks.PLANKS, 1, 5), BlockObjectHolder.chair_wood_ironage_stool_tall_big_oak);
		}

		if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
			SeatingFactory.AddBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 0), BlockObjectHolder.chair_wood_ironage_bench_single_oak);
			SeatingFactory.AddBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 1), BlockObjectHolder.chair_wood_ironage_bench_single_spruce);
			SeatingFactory.AddBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 2), BlockObjectHolder.chair_wood_ironage_bench_single_birch);
			SeatingFactory.AddBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 3), BlockObjectHolder.chair_wood_ironage_bench_single_jungle);
			SeatingFactory.AddBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 4), BlockObjectHolder.chair_wood_ironage_bench_single_acacia);
			SeatingFactory.AddBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 5), BlockObjectHolder.chair_wood_ironage_bench_single_big_oak);

			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_oak, BlockObjectHolder.chair_wood_ironage_bench_padded_single_oak);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_spruce, BlockObjectHolder.chair_wood_ironage_bench_padded_single_spruce);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_birch, BlockObjectHolder.chair_wood_ironage_bench_padded_single_birch);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_jungle, BlockObjectHolder.chair_wood_ironage_bench_padded_single_jungle);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_acacia, BlockObjectHolder.chair_wood_ironage_bench_padded_single_acacia);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_big_oak, BlockObjectHolder.chair_wood_ironage_bench_padded_single_oak);

			SeatingFactory.AddLogBenchRecipe(new ItemStack(Blocks.LOG, 1, 0), BlockObjectHolder.chair_wood_ironage_bench_log_single_oak);
			SeatingFactory.AddLogBenchRecipe(new ItemStack(Blocks.LOG, 1, 1), BlockObjectHolder.chair_wood_ironage_bench_log_single_spruce);
			SeatingFactory.AddLogBenchRecipe(new ItemStack(Blocks.LOG, 1, 2), BlockObjectHolder.chair_wood_ironage_bench_log_single_birch);
			SeatingFactory.AddLogBenchRecipe(new ItemStack(Blocks.LOG, 1, 3), BlockObjectHolder.chair_wood_ironage_bench_log_single_jungle);
			SeatingFactory.AddLogBenchRecipe(new ItemStack(Blocks.LOG2, 1, 0), BlockObjectHolder.chair_wood_ironage_bench_log_single_acacia);
			SeatingFactory.AddLogBenchRecipe(new ItemStack(Blocks.LOG2, 1, 1), BlockObjectHolder.chair_wood_ironage_bench_log_single_big_oak);

			SeatingFactory.AddBackBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 0), BlockObjectHolder.chair_wood_ironage_bench_back_single_oak);
			SeatingFactory.AddBackBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 1), BlockObjectHolder.chair_wood_ironage_bench_back_single_spruce);
			SeatingFactory.AddBackBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 2), BlockObjectHolder.chair_wood_ironage_bench_back_single_birch);
			SeatingFactory.AddBackBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 3), BlockObjectHolder.chair_wood_ironage_bench_back_single_jungle);
			SeatingFactory.AddBackBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 4), BlockObjectHolder.chair_wood_ironage_bench_back_single_acacia);
			SeatingFactory.AddBackBenchRecipe(new ItemStack(Blocks.PLANKS, 1, 5), BlockObjectHolder.chair_wood_ironage_bench_back_single_big_oak);

			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_oak, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_oak);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_spruce, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_spruce);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_birch, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_birch);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_jungle, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_jungle);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_acacia, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_acacia);
			SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_big_oak, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_big_oak);

		}

		if (IronAgeFurnitureConfiguration.INTEGRATION_BIOMESOPLENTY && Loader.isModLoaded("BiomesOPlenty")) {
			Block planks = Block.getBlockFromName("BiomesOPlenty:planks_0");

			ItemStack SACRED_OAK = new ItemStack(planks, 1, 0);
			ItemStack CHERRY = new ItemStack(planks, 1, 1);
			ItemStack UMBRAN = new ItemStack(planks, 1, 2);
			ItemStack FIR = new ItemStack(planks, 1, 3);
			ItemStack ETHEREAL = new ItemStack(planks, 1, 4);
			ItemStack MAGIC = new ItemStack(planks, 1, 5);
			ItemStack MANGROVE = new ItemStack(planks, 1, 6);
			ItemStack PALM = new ItemStack(planks, 1, 7);
			ItemStack REDWOOD = new ItemStack(planks, 1, 8);
			ItemStack WILLOW = new ItemStack(planks, 1, 9);
			ItemStack PINE = new ItemStack(planks, 1, 10);
			ItemStack HELLBARK = new ItemStack(planks, 1, 11);
			ItemStack JACARANDA = new ItemStack(planks, 1, 12);
			ItemStack MAHOGANY = new ItemStack(planks, 1, 13);
			ItemStack EBONY = new ItemStack(planks, 1, 14);
			ItemStack EUCALYPTUS = new ItemStack(planks, 1, 15);

			Block logs0 = Block.getBlockFromName("BiomesOPlenty:log_0");

			ItemStack SACRED_OAK_LOG = new ItemStack(logs0, 1, 4);
			ItemStack CHERRY_LOG = new ItemStack(logs0, 1, 5);
			ItemStack UMBRAN_LOG = new ItemStack(logs0, 1, 6);
			ItemStack FIR_LOG = new ItemStack(logs0, 1, 7);

			Block logs1 = Block.getBlockFromName("BiomesOPlenty:log_1");

			ItemStack ETHEREAL_LOG = new ItemStack(logs1, 1, 4);
			ItemStack MAGIC_LOG = new ItemStack(logs1, 1, 5);
			ItemStack MANGROVE_LOG = new ItemStack(logs1, 1, 6);
			ItemStack PALM_LOG = new ItemStack(logs1, 1, 7);

			Block logs2 = Block.getBlockFromName("BiomesOPlenty:log_2");

			ItemStack REDWOOD_LOG = new ItemStack(logs2, 1, 4);
			ItemStack WILLOW_LOG = new ItemStack(logs2, 1, 5);
			ItemStack PINE_LOG = new ItemStack(logs2, 1, 6);
			ItemStack HELLBARK_LOG = new ItemStack(logs2, 1, 7);

			Block logs3 = Block.getBlockFromName("BiomesOPlenty:log_3");

			ItemStack JACARANDA_LOG = new ItemStack(logs3, 1, 4);
			ItemStack MAHOGANY_LOG = new ItemStack(logs3, 1, 5);
			ItemStack EBONY_LOG = new ItemStack(logs3, 1, 6);
			ItemStack EUCALYPTUS_LOG = new ItemStack(logs3, 1, 7);

			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				SeatingFactory.AddClassicChairRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_cherry);
				SeatingFactory.AddClassicChairRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_ebony);
				SeatingFactory.AddClassicChairRecipe(ETHEREAL, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_ethereal);
				SeatingFactory.AddClassicChairRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_eucalyptus);
				SeatingFactory.AddClassicChairRecipe(FIR, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_fir);
				SeatingFactory.AddClassicChairRecipe(HELLBARK, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_hellbark);
				SeatingFactory.AddClassicChairRecipe(JACARANDA, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_jacaranda);
				SeatingFactory.AddClassicChairRecipe(MAGIC, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_magic);
				SeatingFactory.AddClassicChairRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_mahogany);
				SeatingFactory.AddClassicChairRecipe(MANGROVE, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_mangrove);
				SeatingFactory.AddClassicChairRecipe(PALM, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_palm);
				SeatingFactory.AddClassicChairRecipe(PINE, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_pine);
				SeatingFactory.AddClassicChairRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_redwood);
				SeatingFactory.AddClassicChairRecipe(SACRED_OAK, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_sacred_oak);
				SeatingFactory.AddClassicChairRecipe(UMBRAN, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_umbran);
				SeatingFactory.AddClassicChairRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_willow);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_cherry, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_cherry);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_ebony, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_ebony);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_ethereal, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_ethereal);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_eucalyptus, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_eucalyptus);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_fir, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_fir);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_hellbark, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_hellbark);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_jacaranda, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_jacaranda);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_magic, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_magic);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_mahogany, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_mahogany);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_mangrove, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_mangrove);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_palm, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_palm);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_pine, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_pine);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_redwood, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_redwood);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_sacred_oak, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_sacred_oak);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_umbran, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_umbran);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_willow, BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_willow);

			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				SeatingFactory.AddShortStoolRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_cherry);
				SeatingFactory.AddShortStoolRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_ebony);
				SeatingFactory.AddShortStoolRecipe(ETHEREAL, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_ethereal);
				SeatingFactory.AddShortStoolRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_eucalyptus);
				SeatingFactory.AddShortStoolRecipe(FIR, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_fir);
				SeatingFactory.AddShortStoolRecipe(HELLBARK, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_hellbark);
				SeatingFactory.AddShortStoolRecipe(JACARANDA, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_jacaranda);
				SeatingFactory.AddShortStoolRecipe(MAGIC, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_magic);
				SeatingFactory.AddShortStoolRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_mahogany);
				SeatingFactory.AddShortStoolRecipe(MANGROVE, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_mangrove);
				SeatingFactory.AddShortStoolRecipe(PALM, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_palm);
				SeatingFactory.AddShortStoolRecipe(PINE, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_pine);
				SeatingFactory.AddShortStoolRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_redwood);
				SeatingFactory.AddShortStoolRecipe(SACRED_OAK, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_sacred_oak);
				SeatingFactory.AddShortStoolRecipe(UMBRAN, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_umbran);
				SeatingFactory.AddShortStoolRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_willow);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				SeatingFactory.AddTallStoolRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_cherry);
				SeatingFactory.AddTallStoolRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_ebony);
				SeatingFactory.AddTallStoolRecipe(ETHEREAL, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_ethereal);
				SeatingFactory.AddTallStoolRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_eucalyptus);
				SeatingFactory.AddTallStoolRecipe(FIR, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_fir);
				SeatingFactory.AddTallStoolRecipe(HELLBARK, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_hellbark);
				SeatingFactory.AddTallStoolRecipe(JACARANDA, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_jacaranda);
				SeatingFactory.AddTallStoolRecipe(MAGIC, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_magic);
				SeatingFactory.AddTallStoolRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_mahogany);
				SeatingFactory.AddTallStoolRecipe(MANGROVE, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_mangrove);
				SeatingFactory.AddTallStoolRecipe(PALM, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_palm);
				SeatingFactory.AddTallStoolRecipe(PINE, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_pine);
				SeatingFactory.AddTallStoolRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_redwood);
				SeatingFactory.AddTallStoolRecipe(SACRED_OAK, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_sacred_oak);
				SeatingFactory.AddTallStoolRecipe(UMBRAN, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_umbran);
				SeatingFactory.AddTallStoolRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_willow);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				SeatingFactory.AddBenchRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_cherry);
				SeatingFactory.AddBenchRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_ebony);
				SeatingFactory.AddBenchRecipe(ETHEREAL, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_ethereal);
				SeatingFactory.AddBenchRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_eucalyptus);
				SeatingFactory.AddBenchRecipe(FIR, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_fir);
				SeatingFactory.AddBenchRecipe(HELLBARK, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_hellbark);
				SeatingFactory.AddBenchRecipe(JACARANDA, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_jacaranda);
				SeatingFactory.AddBenchRecipe(MAGIC, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_magic);
				SeatingFactory.AddBenchRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_mahogany);
				SeatingFactory.AddBenchRecipe(MANGROVE, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_mangrove);
				SeatingFactory.AddBenchRecipe(PALM, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_palm);
				SeatingFactory.AddBenchRecipe(PINE, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_pine);
				SeatingFactory.AddBenchRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_redwood);
				SeatingFactory.AddBenchRecipe(SACRED_OAK, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_sacred_oak);
				SeatingFactory.AddBenchRecipe(UMBRAN, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_umbran);
				SeatingFactory.AddBenchRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_willow);

				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_cherry, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_cherry);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_ebony, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_ebony);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_ethereal, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_ethereal);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_eucalyptus, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_eucalyptus);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_fir, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_fir);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_hellbark, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_hellbark);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_jacaranda, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_jacaranda);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_magic, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_magic);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_mahogany, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_mahogany);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_mangrove, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_mangrove);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_palm, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_palm);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_pine, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_pine);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_redwood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_redwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_sacred_oak, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_sacred_oak);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_umbran, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_umbran);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_willow, BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_willow);

				SeatingFactory.AddLogBenchRecipe(CHERRY_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_cherry);
				SeatingFactory.AddLogBenchRecipe(EBONY_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_ebony);
				SeatingFactory.AddLogBenchRecipe(ETHEREAL_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_ethereal);
				SeatingFactory.AddLogBenchRecipe(EUCALYPTUS_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_eucalyptus);
				SeatingFactory.AddLogBenchRecipe(FIR_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_fir);
				SeatingFactory.AddLogBenchRecipe(HELLBARK_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_hellbark);
				SeatingFactory.AddLogBenchRecipe(JACARANDA_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_jacaranda);
				SeatingFactory.AddLogBenchRecipe(MAGIC_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_magic);
				SeatingFactory.AddLogBenchRecipe(MAHOGANY_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_mahogany);
				SeatingFactory.AddLogBenchRecipe(MANGROVE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_mangrove);
				SeatingFactory.AddLogBenchRecipe(PALM_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_palm);
				SeatingFactory.AddLogBenchRecipe(PINE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_pine);
				SeatingFactory.AddLogBenchRecipe(REDWOOD_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_redwood);
				SeatingFactory.AddLogBenchRecipe(SACRED_OAK_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_sacred_oak);
				SeatingFactory.AddLogBenchRecipe(UMBRAN_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_umbran);
				SeatingFactory.AddLogBenchRecipe(WILLOW_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_willow);

				SeatingFactory.AddBackBenchRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_cherry);
				SeatingFactory.AddBackBenchRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_ebony);
				SeatingFactory.AddBackBenchRecipe(ETHEREAL, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_ethereal);
				SeatingFactory.AddBackBenchRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_eucalyptus);
				SeatingFactory.AddBackBenchRecipe(FIR, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_fir);
				SeatingFactory.AddBackBenchRecipe(HELLBARK, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_hellbark);
				SeatingFactory.AddBackBenchRecipe(JACARANDA, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_jacaranda);
				SeatingFactory.AddBackBenchRecipe(MAGIC, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_magic);
				SeatingFactory.AddBackBenchRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_mahogany);
				SeatingFactory.AddBackBenchRecipe(MANGROVE, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_mangrove);
				SeatingFactory.AddBackBenchRecipe(PALM, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_palm);
				SeatingFactory.AddBackBenchRecipe(PINE, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_pine);
				SeatingFactory.AddBackBenchRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_redwood);
				SeatingFactory.AddBackBenchRecipe(SACRED_OAK, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_sacred_oak);
				SeatingFactory.AddBackBenchRecipe(UMBRAN, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_umbran);
				SeatingFactory.AddBackBenchRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_willow);

				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_cherry, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_cherry);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_ebony, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_ebony);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_ethereal, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_ethereal);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_eucalyptus, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_eucalyptus);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_fir, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_fir);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_hellbark, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_hellbark);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_jacaranda, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_jacaranda);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_magic, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_magic);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_mahogany, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_mahogany);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_mangrove, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_mangrove);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_palm, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_palm);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_pine, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_pine);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_redwood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_redwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_sacred_oak, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_sacred_oak);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_umbran, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_umbran);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_willow, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_willow);
			}
		}

		if (IronAgeFurnitureConfiguration.INTEGRATION_NATURA && Loader.isModLoaded("natura")) {
			Block planks = Block.getBlockFromName("natura:overworld_planks");

			ItemStack MAPLE = new ItemStack(planks, 1, 0);
			ItemStack SILVERBELL = new ItemStack(planks, 1, 1);
			ItemStack AMARANTH = new ItemStack(planks, 1, 2);
			ItemStack TIGER = new ItemStack(planks, 1, 3);
			ItemStack WILLOW = new ItemStack(planks, 1, 4);
			ItemStack EUCALYPTUS = new ItemStack(planks, 1, 5);
			ItemStack HOPSEED = new ItemStack(planks, 1, 6);
			ItemStack SAKURA = new ItemStack(planks, 1, 7);
			ItemStack REDWOOD = new ItemStack(planks, 1, 8);

			Block netherPlanks = Block.getBlockFromName("natura:nether_planks");

			ItemStack GHOSTWOOD = new ItemStack(netherPlanks, 1, 0);
			ItemStack BLOODWOOD = new ItemStack(netherPlanks, 1, 1);
			ItemStack FUSEWOOD = new ItemStack(netherPlanks, 1, 3);
			ItemStack DARKWOOD = new ItemStack(netherPlanks, 1, 2);

			Block logs = Block.getBlockFromName("natura:overworld_logs");

			ItemStack MAPLE_LOG = new ItemStack(logs, 1, 0);
			ItemStack SILVERBELL_LOG = new ItemStack(logs, 1, 1);
			ItemStack AMARANTH_LOG = new ItemStack(logs, 1, 2);
			ItemStack TIGER_LOG = new ItemStack(logs, 1, 3);

			Block logs2 = Block.getBlockFromName("natura:overworld_logs2");

			ItemStack WILLOW_LOG = new ItemStack(logs2, 1, 0);
			ItemStack EUCALYPTUS_LOG = new ItemStack(logs2, 1, 1);
			ItemStack HOPSEED_LOG = new ItemStack(logs2, 1, 2);
			ItemStack SAKURA_LOG = new ItemStack(logs2, 1, 3);

			Block logs3 = Block.getBlockFromName("natura:nether_logs");

			ItemStack GHOSTWOOD_LOG = new ItemStack(logs3, 1, 0);
			ItemStack FUSEWOOD_LOG = new ItemStack(logs3, 1, 2);
			ItemStack DARKWOOD_LOG = new ItemStack(logs3, 1, 1);

			Block logs4 = Block.getBlockFromName("natura:nether_logs2");

			ItemStack BLOODWOOD_LOG = new ItemStack(logs4, 1, 0);

			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				SeatingFactory.AddClassicChairRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_classic_natura_maple);
				SeatingFactory.AddClassicChairRecipe(SILVERBELL, BlockObjectHolder.chair_wood_ironage_classic_natura_silverbell);
				SeatingFactory.AddClassicChairRecipe(AMARANTH, BlockObjectHolder.chair_wood_ironage_classic_natura_amaranth);
				SeatingFactory.AddClassicChairRecipe(TIGER, BlockObjectHolder.chair_wood_ironage_classic_natura_tiger);
				SeatingFactory.AddClassicChairRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_classic_natura_willow);
				SeatingFactory.AddClassicChairRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_classic_natura_eucalyptus);
				SeatingFactory.AddClassicChairRecipe(HOPSEED, BlockObjectHolder.chair_wood_ironage_classic_natura_hopseed);
				SeatingFactory.AddClassicChairRecipe(SAKURA, BlockObjectHolder.chair_wood_ironage_classic_natura_sakura);
				SeatingFactory.AddClassicChairRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_classic_natura_redwood);
				SeatingFactory.AddClassicChairRecipe(GHOSTWOOD, BlockObjectHolder.chair_wood_ironage_classic_natura_ghostwood);
				SeatingFactory.AddClassicChairRecipe(BLOODWOOD, BlockObjectHolder.chair_wood_ironage_classic_natura_bloodwood);
				SeatingFactory.AddClassicChairRecipe(FUSEWOOD, BlockObjectHolder.chair_wood_ironage_classic_natura_fusewood);
				SeatingFactory.AddClassicChairRecipe(DARKWOOD, BlockObjectHolder.chair_wood_ironage_classic_natura_darkwood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_maple, BlockObjectHolder.chair_wood_ironage_shield_natura_maple);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_silverbell, BlockObjectHolder.chair_wood_ironage_shield_natura_silverbell);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_amaranth, BlockObjectHolder.chair_wood_ironage_shield_natura_amaranth);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_tiger, BlockObjectHolder.chair_wood_ironage_shield_natura_tiger);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_willow, BlockObjectHolder.chair_wood_ironage_shield_natura_willow);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_eucalyptus, BlockObjectHolder.chair_wood_ironage_shield_natura_eucalyptus);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_hopseed, BlockObjectHolder.chair_wood_ironage_shield_natura_hopseed);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_sakura, BlockObjectHolder.chair_wood_ironage_shield_natura_sakura);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_redwood, BlockObjectHolder.chair_wood_ironage_shield_natura_redwood);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_ghostwood, BlockObjectHolder.chair_wood_ironage_shield_natura_ghostwood);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_bloodwood, BlockObjectHolder.chair_wood_ironage_shield_natura_bloodwood );
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_fusewood, BlockObjectHolder.chair_wood_ironage_shield_natura_fusewood);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_natura_darkwood, BlockObjectHolder.chair_wood_ironage_shield_natura_darkwood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				SeatingFactory.AddShortStoolRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_stool_short_natura_maple);
				SeatingFactory.AddShortStoolRecipe(SILVERBELL, BlockObjectHolder.chair_wood_ironage_stool_short_natura_silverbell);
				SeatingFactory.AddShortStoolRecipe(AMARANTH, BlockObjectHolder.chair_wood_ironage_stool_short_natura_amaranth);
				SeatingFactory.AddShortStoolRecipe(TIGER, BlockObjectHolder.chair_wood_ironage_stool_short_natura_tiger);
				SeatingFactory.AddShortStoolRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_stool_short_natura_willow);
				SeatingFactory.AddShortStoolRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_stool_short_natura_eucalyptus);
				SeatingFactory.AddShortStoolRecipe(HOPSEED, BlockObjectHolder.chair_wood_ironage_stool_short_natura_hopseed);
				SeatingFactory.AddShortStoolRecipe(SAKURA, BlockObjectHolder.chair_wood_ironage_stool_short_natura_sakura);
				SeatingFactory.AddShortStoolRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_stool_short_natura_redwood);
				SeatingFactory.AddShortStoolRecipe(GHOSTWOOD, BlockObjectHolder.chair_wood_ironage_stool_short_natura_ghostwood);
				SeatingFactory.AddShortStoolRecipe(BLOODWOOD, BlockObjectHolder.chair_wood_ironage_stool_short_natura_bloodwood);
				SeatingFactory.AddShortStoolRecipe(FUSEWOOD, BlockObjectHolder.chair_wood_ironage_stool_short_natura_fusewood);
				SeatingFactory.AddShortStoolRecipe(DARKWOOD, BlockObjectHolder.chair_wood_ironage_stool_short_natura_darkwood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				SeatingFactory.AddTallStoolRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_maple);
				SeatingFactory.AddTallStoolRecipe(SILVERBELL, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_silverbell);
				SeatingFactory.AddTallStoolRecipe(AMARANTH, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_amaranth);
				SeatingFactory.AddTallStoolRecipe(TIGER, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_tiger);
				SeatingFactory.AddTallStoolRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_willow);
				SeatingFactory.AddTallStoolRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_eucalyptus);
				SeatingFactory.AddTallStoolRecipe(HOPSEED, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_hopseed);
				SeatingFactory.AddTallStoolRecipe(SAKURA, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_sakura);
				SeatingFactory.AddTallStoolRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_redwood);
				SeatingFactory.AddTallStoolRecipe(GHOSTWOOD, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_ghostwood);
				SeatingFactory.AddTallStoolRecipe(BLOODWOOD, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_bloodwood);
				SeatingFactory.AddTallStoolRecipe(FUSEWOOD, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_fusewood);
				SeatingFactory.AddTallStoolRecipe(DARKWOOD, BlockObjectHolder.chair_wood_ironage_stool_tall_natura_darkwood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				SeatingFactory.AddBenchRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_bench_single_natura_maple);
				SeatingFactory.AddBenchRecipe(SILVERBELL, BlockObjectHolder.chair_wood_ironage_bench_single_natura_silverbell);
				SeatingFactory.AddBenchRecipe(AMARANTH, BlockObjectHolder.chair_wood_ironage_bench_single_natura_amaranth);
				SeatingFactory.AddBenchRecipe(TIGER, BlockObjectHolder.chair_wood_ironage_bench_single_natura_tiger);
				SeatingFactory.AddBenchRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_bench_single_natura_willow);
				SeatingFactory.AddBenchRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_bench_single_natura_eucalyptus);
				SeatingFactory.AddBenchRecipe(HOPSEED, BlockObjectHolder.chair_wood_ironage_bench_single_natura_hopseed);
				SeatingFactory.AddBenchRecipe(SAKURA, BlockObjectHolder.chair_wood_ironage_bench_single_natura_sakura);
				SeatingFactory.AddBenchRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_bench_single_natura_redwood);
				SeatingFactory.AddBenchRecipe(GHOSTWOOD, BlockObjectHolder.chair_wood_ironage_bench_single_natura_ghostwood);
				SeatingFactory.AddBenchRecipe(BLOODWOOD, BlockObjectHolder.chair_wood_ironage_bench_single_natura_bloodwood);
				SeatingFactory.AddBenchRecipe(FUSEWOOD, BlockObjectHolder.chair_wood_ironage_bench_single_natura_fusewood);
				SeatingFactory.AddBenchRecipe(DARKWOOD, BlockObjectHolder.chair_wood_ironage_bench_single_natura_darkwood);

				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_eucalyptus, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_eucalyptus);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_amaranth, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_amaranth);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_hopseed, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_hopseed);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_maple, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_maple);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_redwood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_redwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_sakura, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_sakura);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_silverbell, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_silverbell);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_tiger, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_tiger);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_willow, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_willow);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_bloodwood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_bloodwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_darkwood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_darkwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_fusewood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_fusewood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_natura_ghostwood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_ghostwood);

				SeatingFactory.AddLogBenchRecipe(MAPLE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_maple);
				SeatingFactory.AddLogBenchRecipe(SILVERBELL_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_silverbell);
				SeatingFactory.AddLogBenchRecipe(AMARANTH_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_amaranth);
				SeatingFactory.AddLogBenchRecipe(TIGER_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_tiger);
				SeatingFactory.AddLogBenchRecipe(WILLOW_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_willow);
				SeatingFactory.AddLogBenchRecipe(EUCALYPTUS_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_eucalyptus);
				SeatingFactory.AddLogBenchRecipe(HOPSEED_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_hopseed);
				SeatingFactory.AddLogBenchRecipe(SAKURA_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_sakura);
				SeatingFactory.AddLogBenchRecipe(GHOSTWOOD_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_ghostwood);
				SeatingFactory.AddLogBenchRecipe(BLOODWOOD_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_bloodwood);
				SeatingFactory.AddLogBenchRecipe(FUSEWOOD_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_fusewood);
				SeatingFactory.AddLogBenchRecipe(DARKWOOD_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_darkwood);

				SeatingFactory.AddBackBenchRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_maple);
				SeatingFactory.AddBackBenchRecipe(SILVERBELL, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_silverbell);
				SeatingFactory.AddBackBenchRecipe(AMARANTH, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_amaranth);
				SeatingFactory.AddBackBenchRecipe(TIGER, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_tiger);
				SeatingFactory.AddBackBenchRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_willow);
				SeatingFactory.AddBackBenchRecipe(EUCALYPTUS, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_eucalyptus);
				SeatingFactory.AddBackBenchRecipe(HOPSEED, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_hopseed);
				SeatingFactory.AddBackBenchRecipe(SAKURA, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_sakura);
				SeatingFactory.AddBackBenchRecipe(REDWOOD, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_redwood);
				SeatingFactory.AddBackBenchRecipe(GHOSTWOOD, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_ghostwood);
				SeatingFactory.AddBackBenchRecipe(BLOODWOOD, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_bloodwood);
				SeatingFactory.AddBackBenchRecipe(FUSEWOOD, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_fusewood);
				SeatingFactory.AddBackBenchRecipe(DARKWOOD, BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_darkwood);

				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_eucalyptus, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_eucalyptus);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_amaranth, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_amaranth);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_hopseed, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_hopseed);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_maple, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_maple);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_redwood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_redwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_sakura, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_sakura);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_silverbell, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_silverbell);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_tiger, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_tiger);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_willow, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_willow);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_bloodwood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_bloodwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_darkwood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_darkwood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_fusewood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_fusewood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_ghostwood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_ghostwood);
			}
		}

		if (IronAgeFurnitureConfiguration.INTEGRATION_IMMERSIVEENGINEERING && Loader.isModLoaded("immersiveengineering")) {
			Block wood = Block.getBlockFromName("immersiveengineering:treatedWood");


			ItemStack HORIZONTAL = new ItemStack(wood, 1, 0);
			ItemStack VERTICAL = new ItemStack(wood, 1, 1);
			ItemStack PACKAGED = new ItemStack(wood, 1, 2);

			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				SeatingFactory.AddClassicChairRecipe(HORIZONTAL, BlockObjectHolder.chair_wood_ironage_classic_immersiveengineering_treatedWood);
				SeatingFactory.AddClassicChairRecipe(VERTICAL, BlockObjectHolder.chair_wood_ironage_classic_immersiveengineering_treatedWood);
				SeatingFactory.AddClassicChairRecipe(PACKAGED, BlockObjectHolder.chair_wood_ironage_classic_immersiveengineering_treatedWood);

			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_immersiveengineering_treatedWood, BlockObjectHolder.chair_wood_ironage_shield_immersiveengineering_treatedWood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				SeatingFactory.AddShortStoolRecipe(HORIZONTAL, BlockObjectHolder.chair_wood_ironage_stool_short_immersiveengineering_treatedWood);
				SeatingFactory.AddShortStoolRecipe(VERTICAL, BlockObjectHolder.chair_wood_ironage_stool_short_immersiveengineering_treatedWood);
				SeatingFactory.AddShortStoolRecipe(PACKAGED, BlockObjectHolder.chair_wood_ironage_stool_short_immersiveengineering_treatedWood);

			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				SeatingFactory.AddTallStoolRecipe(HORIZONTAL, BlockObjectHolder.chair_wood_ironage_stool_tall_immersiveengineering_treatedWood);
				SeatingFactory.AddTallStoolRecipe(VERTICAL, BlockObjectHolder.chair_wood_ironage_stool_tall_immersiveengineering_treatedWood);
				SeatingFactory.AddTallStoolRecipe(PACKAGED, BlockObjectHolder.chair_wood_ironage_stool_tall_immersiveengineering_treatedWood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				SeatingFactory.AddBenchRecipe(HORIZONTAL, BlockObjectHolder.chair_wood_ironage_bench_single_immersiveengineering_treatedWood);
				SeatingFactory.AddBenchRecipe(VERTICAL, BlockObjectHolder.chair_wood_ironage_bench_single_immersiveengineering_treatedWood);
				SeatingFactory.AddBenchRecipe(PACKAGED, BlockObjectHolder.chair_wood_ironage_bench_single_immersiveengineering_treatedWood);

				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_immersiveengineering_treatedWood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_immersiveengineering_treatedWood);
				SeatingFactory.AddBackBenchRecipe(PACKAGED, BlockObjectHolder.chair_wood_ironage_bench_back_single_immersiveengineering_treatedWood);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_immersiveengineering_treatedWood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_immersiveengineering_treatedWood);
			}
		}

		if (IronAgeFurnitureConfiguration.INTEGRATION_FORESTRY && Loader.isModLoaded("forestry")) {
			Block wood = Block.getBlockFromName("forestry:planks.0");
			Block wood1 = Block.getBlockFromName("forestry:planks.1");

			ItemStack LARCH = new ItemStack(wood, 1, 0);
			ItemStack TEAK = new ItemStack(wood, 1, 1);
			ItemStack ACACIA = new ItemStack(wood, 1, 2);
			ItemStack LIME = new ItemStack(wood, 1, 3);
			ItemStack CHESTNUT = new ItemStack(wood, 1, 4);
			ItemStack WENGE = new ItemStack(wood, 1, 5);
			ItemStack BAOBAB = new ItemStack(wood, 1, 6);
			ItemStack SEQUOIA = new ItemStack(wood, 1, 7);
			ItemStack KAPOK = new ItemStack(wood, 1, 8);
			ItemStack EBONY = new ItemStack(wood, 1, 9);
			ItemStack MAHOGANY = new ItemStack(wood, 1, 10);
			ItemStack BALSA = new ItemStack(wood, 1, 11);
			ItemStack WILLOW = new ItemStack(wood, 1, 12);
			ItemStack WALNUT = new ItemStack(wood, 1, 13);
			ItemStack GREENHEART = new ItemStack(wood, 1, 14);
			ItemStack CHERRY = new ItemStack(wood, 1, 15);
			ItemStack MAHOE = new ItemStack(wood1, 1, 0);
			ItemStack POPLAR = new ItemStack(wood1, 1, 1);
			ItemStack PALM = new ItemStack(wood1, 1, 2);
			ItemStack PAPAYA = new ItemStack(wood1, 1, 3);
			ItemStack PINE = new ItemStack(wood1, 1, 4);
			ItemStack PLUM = new ItemStack(wood1, 1, 5);
			ItemStack MAPLE = new ItemStack(wood1, 1, 6);
			ItemStack CITRUS = new ItemStack(wood1, 1, 7);
			ItemStack GIGANTEUM = new ItemStack(wood1, 1, 8);
			ItemStack IPE = new ItemStack(wood1, 1, 9);
			ItemStack PADAUK = new ItemStack(wood1, 1, 10);
			ItemStack COCOBOLO = new ItemStack(wood1, 1, 11);
			ItemStack ZEBRAWOOD = new ItemStack(wood1, 1, 12);

			Block logs = Block.getBlockFromName("forestry:logs.0");

			ItemStack LARCH_LOG = new ItemStack(logs, 1, 0);
			ItemStack TEAK_LOG = new ItemStack(logs, 1, 1);
			ItemStack ACACIA_LOG = new ItemStack(logs, 1, 2);
			ItemStack LIME_LOG = new ItemStack(logs, 1, 3);

			Block logs1 = Block.getBlockFromName("forestry:logs.1");

			ItemStack CHESTNUT_LOG = new ItemStack(logs1, 1, 0);
			ItemStack WENGE_LOG = new ItemStack(logs1, 1, 1);
			ItemStack BAOBAB_LOG = new ItemStack(logs1, 1, 2);
			ItemStack SEQUOIA_LOG = new ItemStack(logs1, 1, 3);

			Block logs2 = Block.getBlockFromName("forestry:logs.2");

			ItemStack KAPOK_LOG = new ItemStack(logs2, 1, 0);
			ItemStack EBONY_LOG = new ItemStack(logs2, 1, 1);
			ItemStack MAHOGANY_LOG = new ItemStack(logs2, 1, 2);
			ItemStack BALSA_LOG = new ItemStack(logs2, 1, 3);

			Block logs3 = Block.getBlockFromName("forestry:logs.3");

			ItemStack WILLOW_LOG = new ItemStack(logs3, 1, 0);
			ItemStack WALNUT_LOG = new ItemStack(logs3, 1, 1);
			ItemStack GREENHEART_LOG = new ItemStack(logs3, 1, 2);
			ItemStack CHERRY_LOG = new ItemStack(logs3, 1, 3);

			Block logs4 = Block.getBlockFromName("forestry:logs.4");

			ItemStack MAHOE_LOG = new ItemStack(logs4, 1, 0);
			ItemStack POPLAR_LOG = new ItemStack(logs4, 1, 1);
			ItemStack PALM_LOG = new ItemStack(logs4, 1, 2);
			ItemStack PAPAYA_LOG = new ItemStack(logs4, 1, 3);

			Block logs5 = Block.getBlockFromName("forestry:logs.5");

			ItemStack PINE_LOG = new ItemStack(logs5, 1, 0);
			ItemStack PLUM_LOG = new ItemStack(logs5, 1, 1);
			ItemStack MAPLE_LOG = new ItemStack(logs5, 1, 2);
			ItemStack CITRUS_LOG = new ItemStack(logs5, 1, 3);

			Block logs6 = Block.getBlockFromName("forestry:logs.6");

			ItemStack GIGANTEUM_LOG = new ItemStack(logs6, 1, 0);
			ItemStack IPE_LOG = new ItemStack(logs6, 1, 1);
			ItemStack PADAUK_LOG = new ItemStack(logs6, 1, 2);
			ItemStack COCOBOLO_LOG = new ItemStack(logs6, 1, 3);

			Block logs7 = Block.getBlockFromName("forestry:logs.7");

			ItemStack ZEBRAWOOD_LOG = new ItemStack(logs7, 1, 0);

			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				SeatingFactory.AddClassicChairRecipe(ACACIA, BlockObjectHolder.chair_wood_ironage_classic_forestry_acacia);
				SeatingFactory.AddClassicChairRecipe(BALSA, BlockObjectHolder.chair_wood_ironage_classic_forestry_balsa);
				SeatingFactory.AddClassicChairRecipe(BAOBAB, BlockObjectHolder.chair_wood_ironage_classic_forestry_baobab);
				SeatingFactory.AddClassicChairRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_classic_forestry_cherry);
				SeatingFactory.AddClassicChairRecipe(CHESTNUT, BlockObjectHolder.chair_wood_ironage_classic_forestry_chestnut);
				SeatingFactory.AddClassicChairRecipe(CITRUS, BlockObjectHolder.chair_wood_ironage_classic_forestry_citrus);
				SeatingFactory.AddClassicChairRecipe(COCOBOLO, BlockObjectHolder.chair_wood_ironage_classic_forestry_cocobolo);
				SeatingFactory.AddClassicChairRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_classic_forestry_ebony);
				SeatingFactory.AddClassicChairRecipe(GIGANTEUM, BlockObjectHolder.chair_wood_ironage_classic_forestry_giganteum);
				SeatingFactory.AddClassicChairRecipe(GREENHEART, BlockObjectHolder.chair_wood_ironage_classic_forestry_greenheart);
				SeatingFactory.AddClassicChairRecipe(IPE, BlockObjectHolder.chair_wood_ironage_classic_forestry_ipe);
				SeatingFactory.AddClassicChairRecipe(KAPOK, BlockObjectHolder.chair_wood_ironage_classic_forestry_kapok);
				SeatingFactory.AddClassicChairRecipe(LARCH, BlockObjectHolder.chair_wood_ironage_classic_forestry_larch);
				SeatingFactory.AddClassicChairRecipe(LIME, BlockObjectHolder.chair_wood_ironage_classic_forestry_lime);
				SeatingFactory.AddClassicChairRecipe(MAHOE, BlockObjectHolder.chair_wood_ironage_classic_forestry_mahoe);
				SeatingFactory.AddClassicChairRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_classic_forestry_mahogany);
				SeatingFactory.AddClassicChairRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_classic_forestry_maple);
				SeatingFactory.AddClassicChairRecipe(PADAUK, BlockObjectHolder.chair_wood_ironage_classic_forestry_padauk);
				SeatingFactory.AddClassicChairRecipe(PALM, BlockObjectHolder.chair_wood_ironage_classic_forestry_palm);
				SeatingFactory.AddClassicChairRecipe(PAPAYA, BlockObjectHolder.chair_wood_ironage_classic_forestry_papaya);
				SeatingFactory.AddClassicChairRecipe(PINE, BlockObjectHolder.chair_wood_ironage_classic_forestry_pine);
				SeatingFactory.AddClassicChairRecipe(PLUM, BlockObjectHolder.chair_wood_ironage_classic_forestry_plum);
				SeatingFactory.AddClassicChairRecipe(POPLAR, BlockObjectHolder.chair_wood_ironage_classic_forestry_poplar);
				SeatingFactory.AddClassicChairRecipe(SEQUOIA, BlockObjectHolder.chair_wood_ironage_classic_forestry_sequoia);
				SeatingFactory.AddClassicChairRecipe(TEAK, BlockObjectHolder.chair_wood_ironage_classic_forestry_teak);
				SeatingFactory.AddClassicChairRecipe(WALNUT, BlockObjectHolder.chair_wood_ironage_classic_forestry_walnut);
				SeatingFactory.AddClassicChairRecipe(WENGE, BlockObjectHolder.chair_wood_ironage_classic_forestry_wenge);
				SeatingFactory.AddClassicChairRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_classic_forestry_willow);
				SeatingFactory.AddClassicChairRecipe(ZEBRAWOOD, BlockObjectHolder.chair_wood_ironage_classic_forestry_zebrawood);


			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_acacia, BlockObjectHolder.chair_wood_ironage_shield_forestry_acacia);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_balsa, BlockObjectHolder.chair_wood_ironage_shield_forestry_balsa);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_baobab, BlockObjectHolder.chair_wood_ironage_shield_forestry_baobab);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_cherry, BlockObjectHolder.chair_wood_ironage_shield_forestry_cherry);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_chestnut, BlockObjectHolder.chair_wood_ironage_shield_forestry_chestnut);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_citrus, BlockObjectHolder.chair_wood_ironage_shield_forestry_citrus);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_cocobolo, BlockObjectHolder.chair_wood_ironage_shield_forestry_cocobolo);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_ebony, BlockObjectHolder.chair_wood_ironage_shield_forestry_ebony);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_giganteum, BlockObjectHolder.chair_wood_ironage_shield_forestry_giganteum);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_greenheart, BlockObjectHolder.chair_wood_ironage_shield_forestry_greenheart);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_ipe, BlockObjectHolder.chair_wood_ironage_shield_forestry_ipe);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_kapok, BlockObjectHolder.chair_wood_ironage_shield_forestry_kapok);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_larch, BlockObjectHolder.chair_wood_ironage_shield_forestry_larch);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_lime, BlockObjectHolder.chair_wood_ironage_shield_forestry_lime);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_mahoe, BlockObjectHolder.chair_wood_ironage_shield_forestry_mahoe);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_mahogany, BlockObjectHolder.chair_wood_ironage_shield_forestry_mahogany);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_maple, BlockObjectHolder.chair_wood_ironage_shield_forestry_maple);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_padauk, BlockObjectHolder.chair_wood_ironage_shield_forestry_padauk);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_palm, BlockObjectHolder.chair_wood_ironage_shield_forestry_palm);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_papaya, BlockObjectHolder.chair_wood_ironage_shield_forestry_papaya);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_pine, BlockObjectHolder.chair_wood_ironage_shield_forestry_pine);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_plum, BlockObjectHolder.chair_wood_ironage_shield_forestry_plum);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_poplar, BlockObjectHolder.chair_wood_ironage_shield_forestry_poplar);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_sequoia, BlockObjectHolder.chair_wood_ironage_shield_forestry_sequoia);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_teak, BlockObjectHolder.chair_wood_ironage_shield_forestry_teak);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_walnut, BlockObjectHolder.chair_wood_ironage_shield_forestry_walnut);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_wenge, BlockObjectHolder.chair_wood_ironage_shield_forestry_wenge);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_willow, BlockObjectHolder.chair_wood_ironage_shield_forestry_willow);
				SeatingFactory.AddShieldChairRecipe(BlockObjectHolder.chair_wood_ironage_classic_forestry_zebrawood, BlockObjectHolder.chair_wood_ironage_shield_forestry_zebrawood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				SeatingFactory.AddShortStoolRecipe(ACACIA, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_acacia);
				SeatingFactory.AddShortStoolRecipe(BALSA, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_balsa);
				SeatingFactory.AddShortStoolRecipe(BAOBAB, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_baobab);
				SeatingFactory.AddShortStoolRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_cherry);
				SeatingFactory.AddShortStoolRecipe(CHESTNUT, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_chestnut);
				SeatingFactory.AddShortStoolRecipe(CITRUS, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_citrus);
				SeatingFactory.AddShortStoolRecipe(COCOBOLO, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_cocobolo);
				SeatingFactory.AddShortStoolRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_ebony);
				SeatingFactory.AddShortStoolRecipe(GIGANTEUM, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_giganteum);
				SeatingFactory.AddShortStoolRecipe(GREENHEART, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_greenheart);
				SeatingFactory.AddShortStoolRecipe(IPE, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_ipe);
				SeatingFactory.AddShortStoolRecipe(KAPOK, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_kapok);
				SeatingFactory.AddShortStoolRecipe(LARCH, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_larch);
				SeatingFactory.AddShortStoolRecipe(LIME, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_lime);
				SeatingFactory.AddShortStoolRecipe(MAHOE, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_mahoe);
				SeatingFactory.AddShortStoolRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_mahogany);
				SeatingFactory.AddShortStoolRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_maple);
				SeatingFactory.AddShortStoolRecipe(PADAUK, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_padauk);
				SeatingFactory.AddShortStoolRecipe(PALM, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_palm);
				SeatingFactory.AddShortStoolRecipe(PAPAYA, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_papaya);
				SeatingFactory.AddShortStoolRecipe(PINE, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_pine);
				SeatingFactory.AddShortStoolRecipe(PLUM, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_plum);
				SeatingFactory.AddShortStoolRecipe(POPLAR, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_poplar);
				SeatingFactory.AddShortStoolRecipe(SEQUOIA, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_sequoia);
				SeatingFactory.AddShortStoolRecipe(TEAK, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_teak);
				SeatingFactory.AddShortStoolRecipe(WALNUT, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_walnut);
				SeatingFactory.AddShortStoolRecipe(WENGE, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_wenge);
				SeatingFactory.AddShortStoolRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_willow);
				SeatingFactory.AddShortStoolRecipe(ZEBRAWOOD, BlockObjectHolder.chair_wood_ironage_stool_short_forestry_zebrawood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				SeatingFactory.AddTallStoolRecipe(ACACIA, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_acacia);
				SeatingFactory.AddTallStoolRecipe(BALSA, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_balsa);
				SeatingFactory.AddTallStoolRecipe(BAOBAB, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_baobab);
				SeatingFactory.AddTallStoolRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_cherry);
				SeatingFactory.AddTallStoolRecipe(CHESTNUT, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_chestnut);
				SeatingFactory.AddTallStoolRecipe(CITRUS, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_citrus);
				SeatingFactory.AddTallStoolRecipe(COCOBOLO, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_cocobolo);
				SeatingFactory.AddTallStoolRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_ebony);
				SeatingFactory.AddTallStoolRecipe(GIGANTEUM, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_giganteum);
				SeatingFactory.AddTallStoolRecipe(GREENHEART, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_greenheart);
				SeatingFactory.AddTallStoolRecipe(IPE, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_ipe);
				SeatingFactory.AddTallStoolRecipe(KAPOK, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_kapok);
				SeatingFactory.AddTallStoolRecipe(LARCH, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_larch);
				SeatingFactory.AddTallStoolRecipe(LIME, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_lime);
				SeatingFactory.AddTallStoolRecipe(MAHOE, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_mahoe);
				SeatingFactory.AddTallStoolRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_mahogany);
				SeatingFactory.AddTallStoolRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_maple);
				SeatingFactory.AddTallStoolRecipe(PADAUK, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_padauk);
				SeatingFactory.AddTallStoolRecipe(PALM, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_palm);
				SeatingFactory.AddTallStoolRecipe(PAPAYA, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_papaya);
				SeatingFactory.AddTallStoolRecipe(PINE, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_pine);
				SeatingFactory.AddTallStoolRecipe(PLUM, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_plum);
				SeatingFactory.AddTallStoolRecipe(POPLAR, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_poplar);
				SeatingFactory.AddTallStoolRecipe(SEQUOIA, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_sequoia);
				SeatingFactory.AddTallStoolRecipe(TEAK, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_teak);
				SeatingFactory.AddTallStoolRecipe(WALNUT, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_walnut);
				SeatingFactory.AddTallStoolRecipe(WENGE, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_wenge);
				SeatingFactory.AddTallStoolRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_willow);
				SeatingFactory.AddTallStoolRecipe(ZEBRAWOOD, BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_zebrawood);
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				SeatingFactory.AddBenchRecipe(ACACIA, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_acacia);
				SeatingFactory.AddBenchRecipe(BALSA, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_balsa);
				SeatingFactory.AddBenchRecipe(BAOBAB, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_baobab);
				SeatingFactory.AddBenchRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_cherry);
				SeatingFactory.AddBenchRecipe(CHESTNUT, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_chestnut);
				SeatingFactory.AddBenchRecipe(CITRUS, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_citrus);
				SeatingFactory.AddBenchRecipe(COCOBOLO, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_cocobolo);
				SeatingFactory.AddBenchRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_ebony);
				SeatingFactory.AddBenchRecipe(GIGANTEUM, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_giganteum);
				SeatingFactory.AddBenchRecipe(GREENHEART, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_greenheart);
				SeatingFactory.AddBenchRecipe(IPE, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_ipe);
				SeatingFactory.AddBenchRecipe(KAPOK, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_kapok);
				SeatingFactory.AddBenchRecipe(LARCH, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_larch);
				SeatingFactory.AddBenchRecipe(LIME, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_lime);
				SeatingFactory.AddBenchRecipe(MAHOE, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_mahoe);
				SeatingFactory.AddBenchRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_mahogany);
				SeatingFactory.AddBenchRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_maple);
				SeatingFactory.AddBenchRecipe(PADAUK, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_padauk);
				SeatingFactory.AddBenchRecipe(PALM, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_palm);
				SeatingFactory.AddBenchRecipe(PAPAYA, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_papaya);
				SeatingFactory.AddBenchRecipe(PINE, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_pine);
				SeatingFactory.AddBenchRecipe(PLUM, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_plum);
				SeatingFactory.AddBenchRecipe(POPLAR, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_poplar);
				SeatingFactory.AddBenchRecipe(SEQUOIA, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_sequoia);
				SeatingFactory.AddBenchRecipe(TEAK, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_teak);
				SeatingFactory.AddBenchRecipe(WALNUT, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_walnut);
				SeatingFactory.AddBenchRecipe(WENGE, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_wenge);
				SeatingFactory.AddBenchRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_willow);
				SeatingFactory.AddBenchRecipe(ZEBRAWOOD, BlockObjectHolder.chair_wood_ironage_bench_single_forestry_zebrawood);

				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_acacia, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_acacia);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_balsa, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_balsa);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_baobab, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_baobab);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_cherry, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_cherry);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_chestnut, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_chestnut);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_citrus, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_citrus);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_cocobolo, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_cocobolo);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_ebony, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_ebony);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_giganteum, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_giganteum);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_greenheart, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_greenheart);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_ipe, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_ipe);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_kapok, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_kapok);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_larch, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_larch);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_lime, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_lime);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_mahoe, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_mahoe);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_mahogany, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_mahogany);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_maple, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_maple);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_padauk, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_padauk);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_palm, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_palm);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_papaya, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_papaya);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_pine, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_pine);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_plum, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_plum);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_poplar, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_poplar);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_sequoia, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_sequoia);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_teak, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_teak);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_walnut, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_walnut);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_wenge, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_wenge);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_willow, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_willow);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_single_forestry_zebrawood, BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_zebrawood);

				SeatingFactory.AddLogBenchRecipe(ACACIA_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_acacia);
				SeatingFactory.AddLogBenchRecipe(BALSA_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_balsa);
				SeatingFactory.AddLogBenchRecipe(BAOBAB_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_baobab);
				SeatingFactory.AddLogBenchRecipe(CHERRY_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_cherry);
				SeatingFactory.AddLogBenchRecipe(CHESTNUT_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_chestnut);
				SeatingFactory.AddLogBenchRecipe(CITRUS_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_citrus);
				SeatingFactory.AddLogBenchRecipe(COCOBOLO_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_cocobolo);
				SeatingFactory.AddLogBenchRecipe(EBONY_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_ebony);
				SeatingFactory.AddLogBenchRecipe(GIGANTEUM_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_giganteum);
				SeatingFactory.AddLogBenchRecipe(GREENHEART_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_greenheart);
				SeatingFactory.AddLogBenchRecipe(IPE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_ipe);
				SeatingFactory.AddLogBenchRecipe(KAPOK_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_kapok);
				SeatingFactory.AddLogBenchRecipe(LARCH_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_larch);
				SeatingFactory.AddLogBenchRecipe(LIME_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_lime);
				SeatingFactory.AddLogBenchRecipe(MAHOE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_mahoe);
				SeatingFactory.AddLogBenchRecipe(MAHOGANY_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_mahogany);
				SeatingFactory.AddLogBenchRecipe(MAPLE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_maple);
				SeatingFactory.AddLogBenchRecipe(PADAUK_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_padauk);
				SeatingFactory.AddLogBenchRecipe(PALM_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_palm);
				SeatingFactory.AddLogBenchRecipe(PAPAYA_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_papaya);
				SeatingFactory.AddLogBenchRecipe(PINE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_pine);
				SeatingFactory.AddLogBenchRecipe(PLUM_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_plum);
				SeatingFactory.AddLogBenchRecipe(POPLAR_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_poplar);
				SeatingFactory.AddLogBenchRecipe(SEQUOIA_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_sequoia);
				SeatingFactory.AddLogBenchRecipe(TEAK_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_teak);
				SeatingFactory.AddLogBenchRecipe(WALNUT_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_walnut);
				SeatingFactory.AddLogBenchRecipe(WENGE_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_wenge);
				SeatingFactory.AddLogBenchRecipe(WILLOW_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_willow);
				SeatingFactory.AddLogBenchRecipe(ZEBRAWOOD_LOG, BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_zebrawood);

				SeatingFactory.AddBackBenchRecipe(ACACIA, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_acacia);
				SeatingFactory.AddBackBenchRecipe(BALSA, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_balsa);
				SeatingFactory.AddBackBenchRecipe(BAOBAB, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_baobab);
				SeatingFactory.AddBackBenchRecipe(CHERRY, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_cherry);
				SeatingFactory.AddBackBenchRecipe(CHESTNUT, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_chestnut);
				SeatingFactory.AddBackBenchRecipe(CITRUS, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_citrus);
				SeatingFactory.AddBackBenchRecipe(COCOBOLO, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_cocobolo);
				SeatingFactory.AddBackBenchRecipe(EBONY, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_ebony);
				SeatingFactory.AddBackBenchRecipe(GIGANTEUM, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_giganteum);
				SeatingFactory.AddBackBenchRecipe(GREENHEART, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_greenheart);
				SeatingFactory.AddBackBenchRecipe(IPE, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_ipe);
				SeatingFactory.AddBackBenchRecipe(KAPOK, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_kapok);
				SeatingFactory.AddBackBenchRecipe(LARCH, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_larch);
				SeatingFactory.AddBackBenchRecipe(LIME, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_lime);
				SeatingFactory.AddBackBenchRecipe(MAHOE, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_mahoe);
				SeatingFactory.AddBackBenchRecipe(MAHOGANY, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_mahogany);
				SeatingFactory.AddBackBenchRecipe(MAPLE, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_maple);
				SeatingFactory.AddBackBenchRecipe(PADAUK, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_padauk);
				SeatingFactory.AddBackBenchRecipe(PALM, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_palm);
				SeatingFactory.AddBackBenchRecipe(PAPAYA, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_papaya);
				SeatingFactory.AddBackBenchRecipe(PINE, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_pine);
				SeatingFactory.AddBackBenchRecipe(PLUM, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_plum);
				SeatingFactory.AddBackBenchRecipe(POPLAR, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_poplar);
				SeatingFactory.AddBackBenchRecipe(SEQUOIA, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_sequoia);
				SeatingFactory.AddBackBenchRecipe(TEAK, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_teak);
				SeatingFactory.AddBackBenchRecipe(WALNUT, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_walnut);
				SeatingFactory.AddBackBenchRecipe(WENGE, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_wenge);
				SeatingFactory.AddBackBenchRecipe(WILLOW, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_willow);
				SeatingFactory.AddBackBenchRecipe(ZEBRAWOOD, BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_zebrawood);

				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_acacia, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_acacia);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_balsa, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_balsa);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_baobab, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_baobab);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_cherry, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_cherry);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_chestnut, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_chestnut);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_citrus, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_citrus);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_cocobolo, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_cocobolo);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_ebony, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_ebony);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_giganteum, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_giganteum);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_greenheart, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_greenheart);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_ipe, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_ipe);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_kapok, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_kapok);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_larch, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_larch);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_lime, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_lime);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_mahoe, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_mahoe);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_mahogany, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_mahogany);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_maple, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_maple);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_padauk, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_padauk);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_palm, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_palm);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_papaya, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_papaya);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_pine, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_pine);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_plum, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_plum);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_poplar, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_poplar);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_sequoia, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_sequoia);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_teak, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_teak);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_walnut, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_walnut);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_wenge, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_wenge);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_willow, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_willow);
				SeatingFactory.AddPaddedBenchRecipe(BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_zebrawood, BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_zebrawood);
			}
		}
	}
}
