package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/** Creative categories with fallback icons for disabled furniture. */
public final class FurnitureCreativeTabs {
	public static final CreativeTabs CHAIRS = tab("chairs", "chair_wood_ironage_classic_oak");
	public static final CreativeTabs BENCHES = tab("benches", "chair_wood_ironage_bench_single_oak");
	public static final CreativeTabs BEDS = tab("beds", "bed_canopy_foot_lower_oak");
	public static final CreativeTabs LIGHTS = tab("lights", "light_metal_ironage_sconce_floor_empty_iron");
	public static final CreativeTabs TABLES = tab("tables", "table_dining_oak");
	public static final CreativeTabs STORAGE = tab("storage", "cabinet_wood_ironage_oak");
	public static final CreativeTabs DECORATIONS = tab("decorations", "ornament_glass_vase");

	private FurnitureCreativeTabs() {
	}

	public static CreativeTabs forName(String name) {
		if (name.startsWith("bed_")) {
			return BEDS;
		}

		if (name.contains("_bench_")) {
			return BENCHES;
		}

		if (name.startsWith("chair_")) {
			return CHAIRS;
		}

		if (name.startsWith("table_") || name.startsWith("shelf_")) {
			return TABLES;
		}

		if (name.startsWith("cabinet_") || name.startsWith("half_cabinet_") || name.startsWith("barrel_")
				|| name.startsWith("side_barrel_") || name.startsWith("bottle_rack_") || name.startsWith("shelving_unit_")) {
			return STORAGE;
		}

		if (name.startsWith("ornament_")) {
			return DECORATIONS;
		}

		return LIGHTS;
	}

	private static CreativeTabs tab(final String category, final String icon) {
		return new CreativeTabs("ironagefurniture." + category) {
			@Override
			public Item getTabIconItem() {
				Block block = Ironagefurniture.BlockRegistry.get(icon);
				Item item = block == null ? null : Item.getItemFromBlock(block);
				return item == null ? Items.STICK : item;
			}

			@Override
			public boolean hasSearchBar() {
				return true;
			}
		};
	}
}
