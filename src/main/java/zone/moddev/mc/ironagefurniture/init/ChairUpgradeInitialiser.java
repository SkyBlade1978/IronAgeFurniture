package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.SeatingFactory;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

final class ChairUpgradeInitialiser {
	private ChairUpgradeInitialiser() {
	}

	static void registerBlocks(String suffix) {
		if (!upgradesEnabled() || !Ironagefurniture.BlockRegistry.containsKey("chair_wood_ironage_classic_" + suffix)) {
			return;
		}

		BlockObjectHolder.chair_wood_ironage_wingback.put(suffix,
			SeatingFactory.CreateWoodWingbackChair("chair_wood_ironage_wingback_" + suffix));

		if (IronAgeFurnitureConfiguration.GENERATE_THRONES) {
			BlockObjectHolder.chair_wood_ironage_throne.put(suffix,
				SeatingFactory.CreateWoodThroneChair("chair_wood_ironage_throne_" + suffix));
		}
	}

	static void registerRecipes(String suffix, ItemStack planks) {
		if (!upgradesEnabled()) {
			return;
		}

		Block classic = Ironagefurniture.BlockRegistry.get("chair_wood_ironage_classic_" + suffix);
		Block wingback = BlockObjectHolder.chair_wood_ironage_wingback.get(suffix);

		if (classic == null || wingback == null) {
			return;
		}

		SeatingFactory.AddWingbackChairRecipe(planks, classic, wingback);

		Block throne = BlockObjectHolder.chair_wood_ironage_throne.get(suffix);

		if (IronAgeFurnitureConfiguration.GENERATE_THRONES && throne != null) {
			SeatingFactory.AddThroneChairRecipe(planks, wingback, throne);
		}
	}

	private static boolean upgradesEnabled() {
		return IronAgeFurnitureConfiguration.GENERATE_CLASSIC_CHAIRS
			&& IronAgeFurnitureConfiguration.GENERATE_WINGBACK_CHAIRS;
	}
}
