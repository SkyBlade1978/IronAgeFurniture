package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.api.BedFactory;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

final class BedInitialiser {
	private BedInitialiser() {
	}

	static void registerBlocks(String suffix) {
		if (IronAgeFurnitureConfiguration.GENERATE_CANOPY_BEDS) {
			BlockObjectHolder.bed_canopy_single.put(suffix, BedFactory.CreateSingleCanopyBed(suffix));

			Block[] doubleBed = BedFactory.CreateDoubleCanopyBed(suffix);
			BlockObjectHolder.bed_canopy_double_left.put(suffix, doubleBed[0]);
			BlockObjectHolder.bed_canopy_double_right.put(suffix, doubleBed[1]);
		}

		if (IronAgeFurnitureConfiguration.GENERATE_WOOD_BEDS) {
			BlockObjectHolder.bed_wood_single.put(suffix, BedFactory.CreateSingleWoodBed(suffix));
			BlockObjectHolder.bed_wood_double.put(suffix, BedFactory.CreateDoubleWoodBed(suffix));
		}
	}

	static void registerSingleBedRecipes(String suffix, ItemStack planks) {
		Block woodenBed = BlockObjectHolder.bed_wood_single.get(suffix);

		if (woodenBed != null) {
			BedFactory.AddSingleWoodBedRecipe(planks, woodenBed);
			BedFactory.AddBedRecolourRecipe(woodenBed);
		}

		Block canopyBed = BlockObjectHolder.bed_canopy_single.get(suffix);

		if (canopyBed != null) {
			BedFactory.AddSingleCanopyBedRecipe(planks, woodenBed, canopyBed);
			BedFactory.AddBedRecolourRecipe(canopyBed);
		}
	}

	static void registerDoubleBedRecipes(String suffix) {
		Block woodSingle = BlockObjectHolder.bed_wood_single.get(suffix);
		Block woodDouble = BlockObjectHolder.bed_wood_double.get(suffix);

		if (woodSingle != null && woodDouble != null) {
			BedFactory.AddDoubleWoodBedRecipe(woodSingle, woodDouble);
			BedFactory.AddBedRecolourRecipe(woodDouble);
		}

		Block canopySingle = BlockObjectHolder.bed_canopy_single.get(suffix);
		Block canopyDouble = BlockObjectHolder.bed_canopy_double_left.get(suffix);

		if (canopySingle != null && canopyDouble != null) {
			BedFactory.AddDoubleCanopyBedRecipe(canopySingle, canopyDouble);
			BedFactory.AddBedRecolourRecipe(canopyDouble);
		}
	}
}
