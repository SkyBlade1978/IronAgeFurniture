package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.LightingFactory;
import zone.moddev.mc.ironagefurniture.api.MineralogyCompat;
import zone.moddev.mc.ironagefurniture.api.SeatingFactory;
import zone.moddev.mc.ironagefurniture.api.SurfaceDisplayFactory;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceBottles;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBottleRack;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDiningTable;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityHalfCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityVanillaFoodDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Registers the configured furniture and lighting blocks.
 *
 * @author SkyBlade1978
 */
public class BlockInitialiser {


	protected BlockInitialiser() {
		throw new IllegalAccessError("This class cannot be instansiated");
	}

	public static void init() {
		generateChairs();
		generateLights();
		generateBedsAndChairUpgrades();
		generateSurfaceFurniture();
		DiningChairInitialiser.registerBlocks();
	}

	private static void generateBedsAndChairUpgrades() {
		for (String suffix : WoodVariantHelper.getEnabledWoodSuffixes()) {
			BedInitialiser.registerBlocks(suffix);
			ChairUpgradeInitialiser.registerBlocks(suffix);
		}
	}

	private static void generateSurfaceFurniture() {
		VanillaSurfaceFoods.register();
		VanillaSurfaceBottles.register();

		GameRegistry.registerTileEntity(TileEntityDiningTable.class, Ironagefurniture.MODID + ":table_dining");
		GameRegistry.registerTileEntity(TileEntityCabinet.class, Ironagefurniture.MODID + ":cabinet_wood_ironage");
		GameRegistry.registerTileEntity(TileEntityVanillaFoodDisplay.class, Ironagefurniture.MODID + ":vanilla_food_display");
		GameRegistry.registerTileEntity(TileEntityWallShelf.class, Ironagefurniture.MODID + ":shelf_wall");
		GameRegistry.registerTileEntity(TileEntityHalfCabinet.class, Ironagefurniture.MODID + ":half_cabinet_wood_ironage");
		GameRegistry.registerTileEntity(TileEntityBarrel.class, Ironagefurniture.MODID + ":barrel_wood_ironage");
		GameRegistry.registerTileEntity(TileEntityBottleRack.class, Ironagefurniture.MODID + ":bottle_rack_wood_ironage");

		GlassVaseInitialiser.registerBlock();
		SurfaceDisplayFactory.createSurfaceDisplayBlocker("surface_display_blocker");
		SurfaceDisplayFactory.createVanillaFoodDisplay("vanilla_food_display");

		for (String suffix : WoodVariantHelper.getEnabledWoodSuffixes()) {
			TableAndShelfInitialiser.registerBlocks(suffix);
			StorageInitialiser.registerBlocks(suffix);
		}
	}

	private static void generateChairs() {
		if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
			BlockObjectHolder.chair_wood_ironage_classic_oak = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_oak");
			BlockObjectHolder.chair_wood_ironage_classic_acacia = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_acacia");
			BlockObjectHolder.chair_wood_ironage_classic_big_oak = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_big_oak");
			BlockObjectHolder.chair_wood_ironage_classic_birch = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_birch");
			BlockObjectHolder.chair_wood_ironage_classic_jungle = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_jungle");
			BlockObjectHolder.chair_wood_ironage_classic_spruce = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_spruce");
		}

		if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
			BlockObjectHolder.chair_wood_ironage_shield_oak = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_oak");
			BlockObjectHolder.chair_wood_ironage_shield_acacia = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_acacia");
			BlockObjectHolder.chair_wood_ironage_shield_big_oak = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_big_oak");
			BlockObjectHolder.chair_wood_ironage_shield_birch = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_birch");
			BlockObjectHolder.chair_wood_ironage_shield_jungle = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_jungle");
			BlockObjectHolder.chair_wood_ironage_shield_spruce = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_spruce");
		}

		if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
			BlockObjectHolder.chair_wood_ironage_stool_short_oak =  SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_oak");
			BlockObjectHolder.chair_wood_ironage_stool_short_acacia = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_acacia");
			BlockObjectHolder.chair_wood_ironage_stool_short_big_oak = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_big_oak");
			BlockObjectHolder.chair_wood_ironage_stool_short_birch =  SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_birch");
			BlockObjectHolder.chair_wood_ironage_stool_short_jungle = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_jungle");
			BlockObjectHolder.chair_wood_ironage_stool_short_spruce = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_spruce");
		}

		if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
			BlockObjectHolder.chair_wood_ironage_stool_tall_acacia = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_acacia");
			BlockObjectHolder.chair_wood_ironage_stool_tall_big_oak = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_big_oak");
			BlockObjectHolder.chair_wood_ironage_stool_tall_birch = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_birch");
			BlockObjectHolder.chair_wood_ironage_stool_tall_jungle = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_jungle");
			BlockObjectHolder.chair_wood_ironage_stool_tall_oak = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_oak");
			BlockObjectHolder.chair_wood_ironage_stool_tall_spruce = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_spruce");
		}

		if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
			BlockObjectHolder.chair_wood_ironage_bench_single_oak = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_oak");
			BlockObjectHolder.chair_wood_ironage_bench_single_acacia = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_acacia");
			BlockObjectHolder.chair_wood_ironage_bench_single_big_oak = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_big_oak");
			BlockObjectHolder.chair_wood_ironage_bench_single_birch = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_birch");
			BlockObjectHolder.chair_wood_ironage_bench_single_jungle = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_jungle");
			BlockObjectHolder.chair_wood_ironage_bench_single_spruce = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_spruce");

			BlockObjectHolder.chair_wood_ironage_bench_padded_single_oak = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_oak");
			BlockObjectHolder.chair_wood_ironage_bench_padded_single_acacia = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_acacia");
			BlockObjectHolder.chair_wood_ironage_bench_padded_single_big_oak = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_big_oak");
			BlockObjectHolder.chair_wood_ironage_bench_padded_single_birch = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_birch");
			BlockObjectHolder.chair_wood_ironage_bench_padded_single_jungle = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_jungle");
			BlockObjectHolder.chair_wood_ironage_bench_padded_single_spruce = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_spruce");

			BlockObjectHolder.chair_wood_ironage_bench_log_single_oak = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_oak");
			BlockObjectHolder.chair_wood_ironage_bench_log_single_acacia = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_acacia");
			BlockObjectHolder.chair_wood_ironage_bench_log_single_big_oak = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_big_oak");
			BlockObjectHolder.chair_wood_ironage_bench_log_single_birch = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_birch");
			BlockObjectHolder.chair_wood_ironage_bench_log_single_jungle = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_jungle");
			BlockObjectHolder.chair_wood_ironage_bench_log_single_spruce = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_spruce");

			BlockObjectHolder.chair_wood_ironage_bench_back_single_oak = SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_oak");
			BlockObjectHolder.chair_wood_ironage_bench_back_single_acacia = SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_acacia");
			BlockObjectHolder.chair_wood_ironage_bench_back_single_big_oak = SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_big_oak");
			BlockObjectHolder.chair_wood_ironage_bench_back_single_birch = SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_birch");
			BlockObjectHolder.chair_wood_ironage_bench_back_single_jungle = SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_jungle");
			BlockObjectHolder.chair_wood_ironage_bench_back_single_spruce = SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_spruce");

			BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_oak = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_oak");
			BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_acacia = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_acacia");
			BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_big_oak = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_big_oak");
			BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_birch = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_birch");
			BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_jungle = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_jungle");
			BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_spruce = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_spruce");
		}


		if (IronAgeFurnitureConfiguration.INTEGRATION_IMMERSIVEENGINEERING && Loader.isModLoaded("immersiveengineering")) {
			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_classic_immersiveengineering_treatedWood = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_immersiveengineering_treatedWood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_shield_immersiveengineering_treatedWood = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_immersiveengineering_treatedWood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_short_immersiveengineering_treatedWood =  SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_immersiveengineering_treatedWood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_tall_immersiveengineering_treatedWood = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_immersiveengineering_treatedWood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				BlockObjectHolder.chair_wood_ironage_bench_single_immersiveengineering_treatedWood = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_immersiveengineering_treatedWood");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_immersiveengineering_treatedWood = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_immersiveengineering_treatedWood");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_immersiveengineering_treatedWood = SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_immersiveengineering_treatedWood");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_immersiveengineering_treatedWood = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_immersiveengineering_treatedWood");
			}
		}

		if (IronAgeFurnitureConfiguration.INTEGRATION_BIOMESOPLENTY && Loader.isModLoaded("BiomesOPlenty")) {
			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_cherry         = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_ebony          = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_ethereal       = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_eucalyptus     = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_fir            = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_hellbark       = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_jacaranda      = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_magic          = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_mahogany       = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_mangrove       = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_palm           = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_pine           = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_redwood        = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_sacred_oak     = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_umbran         = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_classic_biomesoplenty_willow         = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_biomesoplenty_willow");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_cherry		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_ebony			= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_ethereal		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_eucalyptus	= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_fir			= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_hellbark		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_jacaranda		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_magic			= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_mahogany		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_mangrove		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_palm			= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_pine			= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_redwood		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_sacred_oak	= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_umbran		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_shield_biomesoplenty_willow		= 	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_biomesoplenty_willow");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_cherry			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_ebony			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_ethereal			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_eucalyptus		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_fir				= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_hellbark			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_jacaranda		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_magic			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_mahogany			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_mangrove			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_palm				= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_pine				= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_redwood			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_sacred_oak		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_umbran			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_stool_short_biomesoplenty_willow			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_biomesoplenty_willow");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_cherry			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_ebony				=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_ethereal			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_eucalyptus		=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_fir				=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_hellbark			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_jacaranda			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_magic				=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_mahogany			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_mangrove			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_palm				=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_pine				=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_redwood			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_sacred_oak		=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_umbran			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_stool_tall_biomesoplenty_willow			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_biomesoplenty_willow");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_cherry				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_ebony				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_ethereal			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_eucalyptus			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_fir					=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_hellbark			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_jacaranda			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_magic				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_mahogany			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_mangrove			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_palm				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_pine				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_redwood				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_sacred_oak			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_umbran				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_bench_single_biomesoplenty_willow				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_biomesoplenty_willow");

				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_cherry 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_ebony 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_ethereal 	= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_eucalyptus 	= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_fir 			= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_hellbark 	= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_jacaranda 	= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_magic 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_mahogany 	= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_mangrove 	= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_palm 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_pine 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_redwood 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_sacred_oak 	= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_umbran 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_biomesoplenty_willow 		= 	SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_biomesoplenty_willow");

				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_cherry			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_ebony			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_ethereal		=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_eucalyptus		=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_fir				=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_hellbark		=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_jacaranda		=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_magic			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_mahogany		=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_mangrove		=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_palm			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_pine			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_redwood			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_sacred_oak		=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_umbran			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_biomesoplenty_willow			=	SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_biomesoplenty_willow");

				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_cherry			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_ebony			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_ethereal		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_eucalyptus		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_fir			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_hellbark		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_jacaranda		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_magic			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_mahogany		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_mangrove		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_palm			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_pine			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_redwood		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_sacred_oak		=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_umbran			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_biomesoplenty_willow			=	SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_biomesoplenty_willow");

				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_cherry			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_ebony			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_ethereal		=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_ethereal");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_eucalyptus		=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_fir				=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_fir");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_hellbark		=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_hellbark");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_jacaranda		=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_jacaranda");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_magic			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_magic");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_mahogany		=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_mangrove		=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_mangrove");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_palm			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_palm");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_pine			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_pine");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_redwood			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_sacred_oak		=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_sacred_oak");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_umbran			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_umbran");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_biomesoplenty_willow			=	SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_biomesoplenty_willow");
			}
		}

		if (IronAgeFurnitureConfiguration.INTEGRATION_NATURA && Loader.isModLoaded("natura")) {
			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_classic_natura_amaranth		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_classic_natura_eucalyptus		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_classic_natura_hopseed			= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_classic_natura_maple			= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_maple");
				BlockObjectHolder.chair_wood_ironage_classic_natura_sakura			= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_classic_natura_silverbell		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_classic_natura_tiger			= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_classic_natura_willow			= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_willow");
				BlockObjectHolder.chair_wood_ironage_classic_natura_redwood		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_classic_natura_bloodwood		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_classic_natura_darkwood		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_classic_natura_fusewood		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_classic_natura_ghostwood		= SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_natura_ghostwood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_shield_natura_amaranth			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_shield_natura_eucalyptus		=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_shield_natura_hopseed			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_shield_natura_maple			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_maple");
				BlockObjectHolder.chair_wood_ironage_shield_natura_sakura			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_shield_natura_silverbell		=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_shield_natura_tiger			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_shield_natura_willow			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_willow");
				BlockObjectHolder.chair_wood_ironage_shield_natura_redwood		=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_shield_natura_bloodwood		=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_shield_natura_darkwood			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_shield_natura_fusewood			=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_shield_natura_ghostwood		=	SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_natura_ghostwood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_amaranth		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_eucalyptus		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_hopseed			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_maple			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_maple");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_sakura			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_silverbell		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_tiger			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_willow			= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_willow");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_redwood		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_bloodwood		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_darkwood		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_fusewood		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_stool_short_natura_ghostwood		= SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_natura_ghostwood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_amaranth			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_eucalyptus		=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_hopseed			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_maple			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_maple");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_sakura			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_silverbell		=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_tiger			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_willow			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_willow");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_redwood		=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_bloodwood		=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_darkwood			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_fusewood			=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_stool_tall_natura_ghostwood		=	SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_natura_ghostwood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_amaranth		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_eucalyptus		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_hopseed		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_maple			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_maple");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_sakura			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_silverbell		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_tiger			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_willow			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_willow");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_redwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_bloodwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_darkwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_fusewood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_bench_single_natura_ghostwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_natura_ghostwood");

				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_eucalyptus = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_amaranth = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_hopseed = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_maple = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_maple");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_sakura = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_silverbell = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_tiger = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_willow = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_willow");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_redwood = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_bloodwood = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_darkwood = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_fusewood = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_natura_ghostwood = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_natura_ghostwood");

				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_amaranth		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_eucalyptus		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_hopseed		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_maple			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_maple");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_sakura			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_silverbell		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_tiger			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_willow			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_willow");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_redwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_bloodwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_darkwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_fusewood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_natura_ghostwood		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_natura_ghostwood");

				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_eucalyptus	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_amaranth 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_hopseed 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_maple 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_maple");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_sakura 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_silverbell 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_tiger 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_willow 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_willow");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_redwood 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_bloodwood 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_darkwood 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_fusewood 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_natura_ghostwood 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_natura_ghostwood");

				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_eucalyptus = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_eucalyptus");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_amaranth 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_amaranth");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_hopseed 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_hopseed");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_maple 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_maple");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_sakura 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_sakura");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_silverbell = SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_silverbell");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_tiger 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_tiger");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_willow 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_willow");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_redwood 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_redwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_bloodwood 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_bloodwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_darkwood 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_darkwood");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_fusewood 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_fusewood");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_natura_ghostwood 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_natura_ghostwood");
			}
		}

		if (IronAgeFurnitureConfiguration.INTEGRATION_FORESTRY && Loader.isModLoaded("forestry")) {
			if (IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_classic_forestry_acacia = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_balsa = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_baobab = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_cherry = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_chestnut = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_citrus = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_cocobolo = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_ebony = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_giganteum = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_greenheart = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_ipe = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_kapok = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_larch = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_lime = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_mahoe = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_mahogany = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_maple = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_padauk = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_palm = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_papaya = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_pine = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_plum = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_poplar = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_sequoia = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_teak = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_walnut = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_wenge = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_willow = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_classic_forestry_zebrawood = SeatingFactory.CreateWoodChair("chair_wood_ironage_classic_forestry_zebrawood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHIELD_CHAIRS) {
				BlockObjectHolder.chair_wood_ironage_shield_forestry_acacia = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_balsa = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_baobab = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_cherry = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_chestnut = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_citrus = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_cocobolo = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_ebony = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_giganteum = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_greenheart = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_ipe = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_kapok = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_larch = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_lime = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_mahoe = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_mahogany = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_maple = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_padauk = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_palm = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_papaya = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_pine = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_plum = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_poplar = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_sequoia = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_teak = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_walnut = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_wenge = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_willow = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_shield_forestry_zebrawood = SeatingFactory.CreateWoodShieldChair("chair_wood_ironage_shield_forestry_zebrawood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_SHORT_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_acacia = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_balsa = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_baobab = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_cherry = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_chestnut = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_citrus = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_cocobolo = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_ebony = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_giganteum = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_greenheart = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_ipe = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_kapok = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_larch = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_lime = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_mahoe = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_mahogany = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_maple = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_padauk = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_palm = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_papaya = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_pine = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_plum = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_poplar = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_sequoia = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_teak = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_walnut = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_wenge = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_willow = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_stool_short_forestry_zebrawood = SeatingFactory.CreateWoodShortStool("chair_wood_ironage_stool_short_forestry_zebrawood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_TALL_STOOLS) {
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_acacia = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_balsa = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_baobab = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_cherry = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_chestnut = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_citrus = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_cocobolo = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_ebony = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_giganteum = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_greenheart = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_ipe = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_kapok = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_larch = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_lime = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_mahoe = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_mahogany = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_maple = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_padauk = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_palm = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_papaya = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_pine = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_plum = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_poplar = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_sequoia = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_teak = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_walnut = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_wenge = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_willow = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_stool_tall_forestry_zebrawood = SeatingFactory.CreateWoodTallStool("chair_wood_ironage_stool_tall_forestry_zebrawood");
			}

			if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BENCHES) {
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_acacia = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_balsa = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_baobab = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_cherry = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_chestnut = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_citrus = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_cocobolo = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_ebony = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_giganteum = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_greenheart = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_ipe = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_kapok = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_larch = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_lime = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_mahoe = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_mahogany = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_maple = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_padauk = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_palm = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_papaya = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_pine = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_plum = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_poplar = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_sequoia = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_teak = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_walnut = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_wenge = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_willow = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_bench_single_forestry_zebrawood = SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_single_forestry_zebrawood");

				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_acacia = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_balsa = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_baobab = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_cherry = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_chestnut = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_citrus = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_cocobolo = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_ebony = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_giganteum = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_greenheart = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_ipe = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_kapok = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_larch = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_lime = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_mahoe = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_mahogany = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_maple = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_padauk = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_palm = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_papaya = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_pine = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_plum = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_poplar = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_sequoia = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_teak = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_walnut = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_wenge = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_willow = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_bench_padded_single_forestry_zebrawood = SeatingFactory.CreateWoodPaddedBench("chair_wood_ironage_bench_padded_single_forestry_zebrawood");

				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_acacia 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_balsa 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_baobab 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_cherry 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_chestnut 	= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_citrus 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_cocobolo 	= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_ebony 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_giganteum 	= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_greenheart 	= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_ipe 			= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_kapok 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_larch 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_lime 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_mahoe 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_mahogany 	= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_maple 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_padauk 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_palm 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_papaya 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_pine 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_plum 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_poplar 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_sequoia 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_teak 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_walnut 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_wenge 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_willow 		= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_bench_log_single_forestry_zebrawood 	= SeatingFactory.CreateWoodBench("chair_wood_ironage_bench_log_single_forestry_zebrawood");

				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_acacia 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_balsa 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_baobab 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_cherry 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_chestnut 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_citrus 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_cocobolo 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_ebony 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_giganteum 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_greenheart 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_ipe 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_kapok 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_larch 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_lime 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_mahoe 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_mahogany 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_maple 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_padauk 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_palm 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_papaya 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_pine 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_plum 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_poplar 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_sequoia 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_teak 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_walnut 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_wenge 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_willow 		= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_bench_back_single_forestry_zebrawood 	= SeatingFactory.CreateWoodBackBench("chair_wood_ironage_bench_back_single_forestry_zebrawood");

				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_acacia 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_acacia");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_balsa 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_balsa");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_baobab 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_baobab");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_cherry 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_cherry");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_chestnut 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_chestnut");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_citrus 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_citrus");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_cocobolo 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_cocobolo");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_ebony 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_ebony");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_giganteum 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_giganteum");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_greenheart 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_greenheart");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_ipe 			= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_ipe");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_kapok 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_kapok");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_larch 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_larch");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_lime 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_lime");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_mahoe 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_mahoe");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_mahogany 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_mahogany");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_maple 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_maple");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_padauk 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_padauk");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_palm 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_palm");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_papaya 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_papaya");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_pine 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_pine");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_plum 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_plum");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_poplar 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_poplar");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_sequoia 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_sequoia");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_teak 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_teak");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_walnut 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_walnut");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_wenge 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_wenge");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_willow 		= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_willow");
				BlockObjectHolder.chair_wood_ironage_bench_back_padded_single_forestry_zebrawood 	= SeatingFactory.CreateWoodPaddedBackBench("chair_wood_ironage_bench_back_padded_single_forestry_zebrawood");
			}
		}
	}

	private static void generateLights() {
		if (!IronAgeFurnitureConfiguration.GENERATE_LIGHTS) {
			return;
		}
		if (IronAgeFurnitureConfiguration.GENERATE_SCONCES) {
			generateSconces();
		}
		if (IronAgeFurnitureConfiguration.GENERATE_GLOW_LAMPS) {
			BlockObjectHolder.light_metal_ironage_block_floor_glow_clear = LightingFactory.CreateGlowdustLamp("light_metal_ironage_block_floor_glow_clear");
			if (IronAgeFurnitureConfiguration.GENERATE_SCONCES) {
				BlockObjectHolder.light_metal_ironage_sconce_floor_glow_iron = LightingFactory.CreateIronFloorGlowSconce("light_metal_ironage_sconce_floor_glow_iron");
				BlockObjectHolder.light_metal_ironage_sconce_wall_glow_iron = LightingFactory.CreateIronWallGlowSconce("light_metal_ironage_sconce_wall_glow_iron");
			}
		}
		if (IronAgeFurnitureConfiguration.GENERATE_LAVA_LAMPS) {
			BlockObjectHolder.light_metal_ironage_block_floor_lava_clear = LightingFactory.CreateLavaLamp("light_metal_ironage_block_floor_lava_clear");
			BlockObjectHolder.obsidian_chunk = LightingFactory.CreateObsidianChunk("obsidian_chunk");
			if (IronAgeFurnitureConfiguration.GENERATE_SCONCES) {
				BlockObjectHolder.light_metal_ironage_sconce_floor_lava_iron = LightingFactory.CreateIronFloorLavaSconce("light_metal_ironage_sconce_floor_lava_iron");
				BlockObjectHolder.light_metal_ironage_sconce_wall_lava_iron = LightingFactory.CreateIronWallLavaSconce("light_metal_ironage_sconce_wall_lava_iron");
			}
		}
		if (IronAgeFurnitureConfiguration.GENERATE_REDSTONE_LAMPS) {
			generateRedstoneLamps();
		}
		if (IronAgeFurnitureConfiguration.GENERATE_CANDLES) {
			generateCandles();
		}
	}

	private static void generateSconces() {
		BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron = LightingFactory.CreateIronFloorSconce("light_metal_ironage_sconce_floor_empty_iron");
		BlockObjectHolder.light_metal_ironage_sconce_wall_empty_iron = LightingFactory.CreateIronWallSconce("light_metal_ironage_sconce_wall_empty_iron");
		BlockObjectHolder.light_metal_ironage_sconce_floor_torch_iron = LightingFactory.CreateIronFloorTorchSconce("light_metal_ironage_sconce_floor_torch_iron");
		BlockObjectHolder.light_metal_ironage_sconce_floor_torch_iron_unlit = LightingFactory.CreateIronFloorTorchSconceUnlit("light_metal_ironage_sconce_floor_torch_iron_unlit");
		BlockObjectHolder.light_metal_ironage_sconce_floor_torch_iron_twin = LightingFactory.CreateIronFloorTorchSconceTwin("light_metal_ironage_sconce_floor_torch_iron_twin");
		BlockObjectHolder.light_metal_ironage_sconce_floor_torch_iron_twin_unlit = LightingFactory.CreateIronFloorTorchSconceTwinUnlit("light_metal_ironage_sconce_floor_torch_iron_twin_unlit");
		BlockObjectHolder.light_metal_ironage_sconce_wall_torch_iron = LightingFactory.CreateIronWallTorchSconce("light_metal_ironage_sconce_wall_torch_iron");
		BlockObjectHolder.light_metal_ironage_sconce_wall_torch_iron_unlit = LightingFactory.CreateIronWallTorchSconceUnlit("light_metal_ironage_sconce_wall_torch_iron_unlit");
		BlockObjectHolder.light_metal_ironage_sconce_wall_torch_iron_twin = LightingFactory.CreateIronWallTorchSconceTwin("light_metal_ironage_sconce_wall_torch_iron_twin");
		BlockObjectHolder.light_metal_ironage_sconce_wall_torch_iron_twin_unlit = LightingFactory.CreateIronWallTorchSconceTwinUnlit("light_metal_ironage_sconce_wall_torch_iron_twin_unlit");
		BlockObjectHolder.light_metal_ironage_sconce_floor_redtorch_iron = LightingFactory.CreateIronFloorRedTorchSconce("light_metal_ironage_sconce_floor_redtorch_iron");
		BlockObjectHolder.light_metal_ironage_sconce_floor_redtorch_iron_unlit = LightingFactory.CreateIronFloorRedTorchSconceUnlit("light_metal_ironage_sconce_floor_redtorch_iron_unlit");
		BlockObjectHolder.light_metal_ironage_sconce_wall_redtorch_iron = LightingFactory.CreateIronWallRedTorchSconce("light_metal_ironage_sconce_wall_redtorch_iron");
		BlockObjectHolder.light_metal_ironage_sconce_wall_redtorch_iron_unlit = LightingFactory.CreateIronWallRedTorchSconceUnlit("light_metal_ironage_sconce_wall_redtorch_iron_unlit");
		if (MineralogyCompat.isEnabled()) {
			BlockObjectHolder.light_metal_ironage_sconce_floor_rocksalt_iron = LightingFactory.CreateIronFloorRockSaltSconce("light_metal_ironage_sconce_floor_rocksalt_iron");
			BlockObjectHolder.light_metal_ironage_sconce_wall_rocksalt_iron = LightingFactory.CreateIronWallRockSaltSconce("light_metal_ironage_sconce_wall_rocksalt_iron");
		}
	}

	private static void generateCandles() {
		BlockObjectHolder.light_metal_ironage_candle_floor = LightingFactory.CreateCandleFloor("light_metal_ironage_candle_floor");
		BlockObjectHolder.light_metal_ironage_candle_wall = LightingFactory.CreateCandleWall("light_metal_ironage_candle_wall");
		BlockObjectHolder.light_metal_ironage_candle_floor_unlit = LightingFactory.CreateCandleFloorUnlit("light_metal_ironage_candle_floor_unlit");
		BlockObjectHolder.light_metal_ironage_candle_wall_unlit = LightingFactory.CreateCandleWallUnlit("light_metal_ironage_candle_wall_unlit");
		if (!IronAgeFurnitureConfiguration.GENERATE_SCONCES) return;
		String[] names = { "", "_two", "_three", "_four" };
		for (int i = 0; i < names.length; i++) {
			int count = i + 1;
			String floorName = "light_metal_ironage_sconce_floor_candle_iron" + names[i];
			String wallName = "light_metal_ironage_sconce_wall_candle_iron" + names[i];
			Block floor = LightingFactory.CreateIronFloorCandleSconce(floorName, count);
			Block wall = LightingFactory.CreateIronWallCandleSconce(wallName, count);
			Block floorUnlit = LightingFactory.CreateIronFloorCandleSconceUnlit(floorName + "_unlit", count);
			Block wallUnlit = LightingFactory.CreateIronWallCandleSconceUnlit(wallName + "_unlit", count);
			setCandleHolders(i, floor, wall, floorUnlit, wallUnlit);
		}
	}

	private static void setCandleHolders(int index, Block floor, Block wall, Block floorUnlit, Block wallUnlit) {
		switch (index) {
		case 0:
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron = floor;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron = wall;
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron_unlit = floorUnlit;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron_unlit = wallUnlit;
			break;
		case 1:
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron_two = floor;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron_two = wall;
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron_two_unlit = floorUnlit;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron_two_unlit = wallUnlit;
			break;
		case 2:
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron_three = floor;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron_three = wall;
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron_three_unlit = floorUnlit;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron_three_unlit = wallUnlit;
			break;
		default:
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron_four = floor;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron_four = wall;
			BlockObjectHolder.light_metal_ironage_sconce_floor_candle_iron_four_unlit = floorUnlit;
			BlockObjectHolder.light_metal_ironage_sconce_wall_candle_iron_four_unlit = wallUnlit;
		}
	}

	private static void generateRedstoneLamps() {
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear = LightingFactory.CreateRedLamp("light_metal_ironage_block_floor_red_clear");
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_one = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_one", 1);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_two = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_two", 2);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_three = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_three", 3);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_four = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_four", 4);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_five = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_five", 5);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_six = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_six", 6);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_seven = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_seven", 7);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_eight = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_eight", 8);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_nine = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_nine", 9);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_ten = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_ten", 10);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_eleven = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_eleven", 11);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_twelve = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_twelve", 12);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_thirteen = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_thirteen", 13);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_fourteen = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_fourteen", 14);
		BlockObjectHolder.light_metal_ironage_block_floor_red_clear_fifteen = LightingFactory.CreateRedLampVariant("light_metal_ironage_block_floor_red_clear_fifteen", 15);
		if (IronAgeFurnitureConfiguration.GENERATE_SCONCES) {
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron", 0);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_one = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_one", 1);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_two = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_two", 2);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_three = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_three", 3);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_four = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_four", 4);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_five = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_five", 5);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_six = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_six", 6);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_seven = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_seven", 7);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_eight = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_eight", 8);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_nine = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_nine", 9);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_ten = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_ten", 10);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_eleven = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_eleven", 11);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_twelve = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_twelve", 12);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_thirteen = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_thirteen", 13);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_fourteen = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_fourteen", 14);
			BlockObjectHolder.light_metal_ironage_sconce_floor_red_iron_fifteen = LightingFactory.CreateIronFloorRedSconce("light_metal_ironage_sconce_floor_red_iron_fifteen", 15);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron", 0);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_one = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_one", 1);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_two = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_two", 2);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_three = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_three", 3);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_four = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_four", 4);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_five = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_five", 5);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_six = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_six", 6);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_seven = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_seven", 7);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_eight = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_eight", 8);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_nine = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_nine", 9);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_ten = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_ten", 10);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_eleven = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_eleven", 11);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_twelve = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_twelve", 12);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_thirteen = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_thirteen", 13);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_fourteen = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_fourteen", 14);
			BlockObjectHolder.light_metal_ironage_sconce_wall_red_iron_fifteen = LightingFactory.CreateIronWallRedSconce("light_metal_ironage_sconce_wall_red_iron_fifteen", 15);
		}
	}
}
