package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category;

import net.minecraft.init.Items;
import net.minecraft.item.Item;

final class VanillaShelfTools {
	private VanillaShelfTools() { }

	static void register() {
		register(ShelfLayout.TOOL,
			Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE,
			Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE,
			Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL,
			Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE,
			Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD,
			Items.BOW, Items.FISHING_ROD, Items.CARROT_ON_A_STICK);
		register(ShelfLayout.COMPACT_TOOL, Items.SHEARS);
		register(ShelfLayout.FLINT_AND_STEEL_ROW, Items.FLINT_AND_STEEL);
		register(ShelfLayout.SHIELD, Items.SHIELD);
		register(ShelfLayout.ARROW_STACKS, Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW);
	}

	private static void register(ShelfLayout layout, Item... items) {
		SurfaceContentRegistry.Descriptor descriptor = new SurfaceContentRegistry.Descriptor(Category.SINGLETON, 0.5D, 0.75D)
			.withShelfLayout(layout);

		for (Item item : items) {
			SurfaceContentRegistry.register(item, stack -> descriptor);
		}
	}
}
