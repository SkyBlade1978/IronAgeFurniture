package zone.moddev.mc.ironagefurniture.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.StorageFactory;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityProduceFurniture;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

final class ProduceInitialiser {
	private ProduceInitialiser() { }

	static void registerBlocks() {
		GameRegistry.registerTileEntity(TileEntityProduceFurniture.class, Ironagefurniture.MODID + ":produce_furniture");
		StorageFactory.registerProduceFurniture();
	}

	static void registerRecipes() {
		Block tray = Ironagefurniture.BlockRegistry.get(ProduceFurniture.TRAY_ID);
		Block rack = Ironagefurniture.BlockRegistry.get(ProduceFurniture.RACK_ID);
		Map<String, List<ItemStack>> slabs = new HashMap<>();
		WoodVariantHelper.forEachEnabledSlabVariant((wood, slab) ->
			slabs.computeIfAbsent(wood, key -> new ArrayList<>()).add(slab));
		WoodVariantHelper.forEachEnabledPlankVariant((wood, plank) -> {
			for (ItemStack slab : slabs.getOrDefault(wood, Collections.emptyList())) {
				GameRegistry.addRecipe(new ShapedOreRecipe(ItemBlockProduceFurniture.empty(tray, wood, 2),
					"P P", "SSS", 'P', plank, 'S', slab));
			}
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemBlockProduceFurniture.empty(rack, wood, 1),
				"KPK", "KPK", "KPK", 'K', Items.STICK, 'P', plank));
		});
	}
}
