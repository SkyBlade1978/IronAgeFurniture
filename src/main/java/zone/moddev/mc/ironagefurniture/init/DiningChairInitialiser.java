package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.SeatingFactory;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

final class DiningChairInitialiser {
	private static final String PREFIX = "chair_wood_ironage_dining_";

	private DiningChairInitialiser() {
	}

	static void registerBlocks() {
		if (!IronAgeFurnitureConfiguration.GENERATE_DINING_CHAIRS) {
			return;
		}

		for (String wood : WoodVariantHelper.getEnabledWoodSuffixes()) {
			SeatingFactory.CreateWoodDiningChair(PREFIX + wood);
		}
	}

	static void registerRecipe(String wood, ItemStack slab) {
		Block chair = Ironagefurniture.BlockRegistry.get(PREFIX + wood);

		if (chair != null) {
			SeatingFactory.AddDiningChairRecipe(slab, chair);
		}
	}
}
