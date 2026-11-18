package zone.moddev.mc.ironagefurniture.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

/**
 * Compatibility entry points that delegate to the feature factories.
 */
public class FurnitureFactory {

	public static Block createDiningTable(String name) {
		return TableAndShelfFactory.createDiningTable(name);
	}

	public static Block createCabinet(String name) {
		return StorageFactory.createCabinet(name);
	}

	public static Block createLowTable(String name) {
		return TableAndShelfFactory.createLowTable(name);
	}

	public static Block createGlassVase() {
		return SurfaceDisplayFactory.createGlassVase();
	}

	public static Block createHalfCabinet(String name) {
		return StorageFactory.createHalfCabinet(name);
	}

	public static Block createBarrel(String name, boolean side) {
		return StorageFactory.createBarrel(name, side);
	}

	public static Block createBottleRack(String name) {
		return StorageFactory.createBottleRack(name);
	}

	public static Block createWallShelf(String name) {
		return TableAndShelfFactory.createWallShelf(name);
	}

	public static void createSurfaceDisplayBlocker(String name) {
		SurfaceDisplayFactory.createSurfaceDisplayBlocker(name);
	}

	public static void createVanillaFoodDisplay(String name) {
		SurfaceDisplayFactory.createVanillaFoodDisplay(name);
	}

	public static void AddClassicChairRecipe(ItemStack planks, Block chair) {
		SeatingFactory.AddClassicChairRecipe(planks, chair);
	}

	public static void AddWingbackChairRecipe(ItemStack planks, Block chairIn, Block chairOut) {
		SeatingFactory.AddWingbackChairRecipe(planks, chairIn, chairOut);
	}

	public static void AddThroneChairRecipe(ItemStack planks, Block chairIn, Block chairOut) {
		SeatingFactory.AddThroneChairRecipe(planks, chairIn, chairOut);
	}

	public static void AddSingleCanopyBedRecipe(ItemStack planks, Block bed) {
		BedFactory.AddSingleCanopyBedRecipe(planks, bed);
	}

	public static void AddSingleCanopyBedRecipe(ItemStack planks, Block woodenBed, Block bed) {
		BedFactory.AddSingleCanopyBedRecipe(planks, woodenBed, bed);
	}

	public static void AddDoubleCanopyBedRecipe(Block singleBed, Block doubleBed) {
		BedFactory.AddDoubleCanopyBedRecipe(singleBed, doubleBed);
	}

	public static void AddSingleWoodBedRecipe(ItemStack planks, Block bed) {
		BedFactory.AddSingleWoodBedRecipe(planks, bed);
	}

	public static void AddDoubleWoodBedRecipe(Block singleBed, Block doubleBed) {
		BedFactory.AddDoubleWoodBedRecipe(singleBed, doubleBed);
	}

	public static void AddBedRecolourRecipe(Block bed) {
		BedFactory.AddBedRecolourRecipe(bed);
	}

	public static void AddShortStoolRecipe(ItemStack planks, Block stool) {
		SeatingFactory.AddShortStoolRecipe(planks, stool);
	}

	public static void AddBenchRecipe(ItemStack planks, Block bench) {
		SeatingFactory.AddBenchRecipe(planks, bench);
	}

	public static void AddBackBenchRecipe(ItemStack planks, Block bench) {
		SeatingFactory.AddBackBenchRecipe(planks, bench);
	}

	public static void AddLogBenchRecipe(ItemStack log, Block bench) {
		SeatingFactory.AddLogBenchRecipe(log, bench);
	}

	public static void AddPaddedBenchRecipe(Block chairIn, Block chairOut) {
		SeatingFactory.AddPaddedBenchRecipe(chairIn, chairOut);
	}

	public static void AddTallStoolRecipe(ItemStack planks, Block stool) {
		SeatingFactory.AddTallStoolRecipe(planks, stool);
	}

	public static void AddShieldChairRecipe(Block chairIn, Block chairOut) {
		SeatingFactory.AddShieldChairRecipe(chairIn, chairOut);
	}

	public static void AddChairConversionRecipe(Block chairIn, Block chairOut) {
		SeatingFactory.AddChairConversionRecipe(chairIn, chairOut);
	}

	public static void AddIronSconceRecipe(Block sconce) {
		LightingFactory.AddIronSconceRecipe(sconce);
	}

	public static Block CreateWoodShieldChair(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodShieldChair(name, resistance, hardness);
	}

	public static Block CreateWoodShieldChair(String name) {
		return SeatingFactory.CreateWoodShieldChair(name);
	}

	public static Block CreateWoodShortStool(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodShortStool(name, resistance, hardness);
	}

	public static Block CreateWoodBench(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodBench(name, resistance, hardness);
	}

	public static Block CreateWoodBackBench(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodBackBench(name, resistance, hardness);
	}

	public static Block CreateWoodPaddedBench(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodPaddedBench(name, resistance, hardness);
	}

	public static Block CreateWoodPaddedBackBench(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodPaddedBackBench(name, resistance, hardness);
	}

	public static Block CreateWoodShortStool(String name) {
		return SeatingFactory.CreateWoodShortStool(name);
	}

	public static Block CreateWoodBench(String name) {
		return SeatingFactory.CreateWoodBench(name);
	}

	public static Block CreateWoodBackBench(String name) {
		return SeatingFactory.CreateWoodBackBench(name);
	}

	public static Block CreateWoodPaddedBench(String name) {
		return SeatingFactory.CreateWoodPaddedBench(name);
	}

	public static Block CreateWoodPaddedBackBench(String name) {
		return SeatingFactory.CreateWoodPaddedBackBench(name);
	}

	public static Block CreateWoodChair(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodChair(name, resistance, hardness);
	}

	public static Block CreateWoodChair(String name) {
		return SeatingFactory.CreateWoodChair(name);
	}

	public static Block CreateWoodWingbackChair(String name) {
		return SeatingFactory.CreateWoodWingbackChair(name);
	}

	public static Block CreateWoodThroneChair(String name) {
		return SeatingFactory.CreateWoodThroneChair(name);
	}

	public static Block CreateSingleCanopyBed(String suffix) {
		return BedFactory.CreateSingleCanopyBed(suffix);
	}

	public static Block[] CreateDoubleCanopyBed(String suffix) {
		return BedFactory.CreateDoubleCanopyBed(suffix);
	}

	public static Block CreateSingleWoodBed(String suffix) {
		return BedFactory.CreateSingleWoodBed(suffix);
	}

	public static Block CreateDoubleWoodBed(String suffix) {
		return BedFactory.CreateDoubleWoodBed(suffix);
	}

	public static Block CreateWoodTallStool(String name, float resistance, float hardness) {
		return SeatingFactory.CreateWoodTallStool(name, resistance, hardness);
	}

	public static Block CreateWoodTallStool(String name) {
		return SeatingFactory.CreateWoodTallStool(name);
	}

	public static Block CreateIronWallSconce(String name) {
		return LightingFactory.CreateIronWallSconce(name);
	}

	public static Block CreateIronFloorSconce(String name) {
		return LightingFactory.CreateIronFloorSconce(name);
	}

	public static Block CreateIronFloorTorchSconce(String name) {
		return LightingFactory.CreateIronFloorTorchSconce(name);
	}

	public static Block CreateIronFloorTorchSconceUnlit(String name) {
		return LightingFactory.CreateIronFloorTorchSconceUnlit(name);
	}

	public static Block CreateIronFloorTorchSconceTwin(String name) {
		return LightingFactory.CreateIronFloorTorchSconceTwin(name);
	}

	public static Block CreateIronFloorTorchSconceTwinUnlit(String name) {
		return LightingFactory.CreateIronFloorTorchSconceTwinUnlit(name);
	}

	public static Block CreateIronWallTorchSconce(String name) {
		return LightingFactory.CreateIronWallTorchSconce(name);
	}

	public static Block CreateIronWallTorchSconceUnlit(String name) {
		return LightingFactory.CreateIronWallTorchSconceUnlit(name);
	}

	public static Block CreateIronWallTorchSconceTwin(String name) {
		return LightingFactory.CreateIronWallTorchSconceTwin(name);
	}

	public static Block CreateIronWallTorchSconceTwinUnlit(String name) {
		return LightingFactory.CreateIronWallTorchSconceTwinUnlit(name);
	}

	public static Block CreateIronFloorRedTorchSconce(String name) {
		return LightingFactory.CreateIronFloorRedTorchSconce(name);
	}

	public static Block CreateIronFloorRedTorchSconceUnlit(String name) {
		return LightingFactory.CreateIronFloorRedTorchSconceUnlit(name);
	}

	public static Block CreateIronWallRedTorchSconce(String name) {
		return LightingFactory.CreateIronWallRedTorchSconce(name);
	}

	public static Block CreateIronWallRedTorchSconceUnlit(String name) {
		return LightingFactory.CreateIronWallRedTorchSconceUnlit(name);
	}

	public static Block CreateGlowdustLamp(String name) {
		return LightingFactory.CreateGlowdustLamp(name);
	}

	public static Block CreateIronFloorGlowSconce(String name) {
		return LightingFactory.CreateIronFloorGlowSconce(name);
	}

	public static Block CreateIronWallGlowSconce(String name) {
		return LightingFactory.CreateIronWallGlowSconce(name);
	}

	public static Block CreateIronFloorRockSaltSconce(String name) {
		return LightingFactory.CreateIronFloorRockSaltSconce(name);
	}

	public static Block CreateIronWallRockSaltSconce(String name) {
		return LightingFactory.CreateIronWallRockSaltSconce(name);
	}

	public static Block CreateLavaLamp(String name) {
		return LightingFactory.CreateLavaLamp(name);
	}

	public static Block CreateIronFloorLavaSconce(String name) {
		return LightingFactory.CreateIronFloorLavaSconce(name);
	}

	public static Block CreateIronWallLavaSconce(String name) {
		return LightingFactory.CreateIronWallLavaSconce(name);
	}

	public static Block CreateRedLamp(String name) {
		return LightingFactory.CreateRedLamp(name);
	}

	public static Block CreateRedLampVariant(String name, int lightLevel) {
		return LightingFactory.CreateRedLampVariant(name, lightLevel);
	}

	public static Block CreateIronFloorRedSconce(String name, int lightLevel) {
		return LightingFactory.CreateIronFloorRedSconce(name, lightLevel);
	}

	public static Block CreateIronWallRedSconce(String name, int lightLevel) {
		return LightingFactory.CreateIronWallRedSconce(name, lightLevel);
	}

	public static Block CreateObsidianChunk(String name) {
		return LightingFactory.CreateObsidianChunk(name);
	}

	public static Block CreateCandleFloor(String name) {
		return LightingFactory.CreateCandleFloor(name);
	}

	public static Block CreateCandleWall(String name) {
		return LightingFactory.CreateCandleWall(name);
	}

	public static Block CreateCandleFloorUnlit(String name) {
		return LightingFactory.CreateCandleFloorUnlit(name);
	}

	public static Block CreateCandleWallUnlit(String name) {
		return LightingFactory.CreateCandleWallUnlit(name);
	}

	public static Block CreateIronFloorCandleSconce(String name, int candleCount) {
		return LightingFactory.CreateIronFloorCandleSconce(name, candleCount);
	}

	public static Block CreateIronFloorCandleSconceUnlit(String name, int candleCount) {
		return LightingFactory.CreateIronFloorCandleSconceUnlit(name, candleCount);
	}

	public static Block CreateIronWallCandleSconce(String name, int candleCount) {
		return LightingFactory.CreateIronWallCandleSconce(name, candleCount);
	}

	public static Block CreateIronWallCandleSconceUnlit(String name, int candleCount) {
		return LightingFactory.CreateIronWallCandleSconceUnlit(name, candleCount);
	}
}
