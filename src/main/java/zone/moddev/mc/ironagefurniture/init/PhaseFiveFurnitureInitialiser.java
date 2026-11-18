package zone.moddev.mc.ironagefurniture.init;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import java.util.LinkedHashMap;
import java.util.Map;
import zone.moddev.mc.ironagefurniture.api.FurnitureFactory;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDiningTable;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityVanillaFoodDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class PhaseFiveFurnitureInitialiser {
	private static final Map<String, Block> tables = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> lowTables = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> shelves = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> cabinets = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> halfCabinets = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> barrels = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> sideBarrels = new LinkedHashMap<String, Block>();
	private static final Map<String, Block> racks = new LinkedHashMap<String, Block>();
	private static Block glassVase;

	private PhaseFiveFurnitureInitialiser() { }

	public static void registerBlocks() {
		VanillaSurfaceFoods.register();
		zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceBottles.register();
		GameRegistry.registerTileEntity(TileEntityDiningTable.class, Ironagefurniture.MODID + ":table_dining");
		GameRegistry.registerTileEntity(TileEntityCabinet.class, Ironagefurniture.MODID + ":cabinet_wood_ironage");
		GameRegistry.registerTileEntity(TileEntityVanillaFoodDisplay.class, Ironagefurniture.MODID + ":vanilla_food_display");
		GameRegistry.registerTileEntity(TileEntityWallShelf.class, Ironagefurniture.MODID + ":shelf_wall");
		GameRegistry.registerTileEntity(zone.moddev.mc.ironagefurniture.api.tile.TileEntityHalfCabinet.class, Ironagefurniture.MODID + ":half_cabinet_wood_ironage");
		GameRegistry.registerTileEntity(zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel.class, Ironagefurniture.MODID + ":barrel_wood_ironage");
		GameRegistry.registerTileEntity(zone.moddev.mc.ironagefurniture.api.tile.TileEntityBottleRack.class, Ironagefurniture.MODID + ":bottle_rack_wood_ironage");
		GameRegistry.registerTileEntityWithAlternatives(zone.moddev.mc.ironagefurniture.api.tile.TileEntityGlassVase.class,
			Ironagefurniture.MODID + ":ornament_glass_vase", Ironagefurniture.MODID + ":glass_vase");
		if (IronAgeFurnitureConfiguration.GENERATE_GLASS_VASES) {
			glassVase = FurnitureFactory.createGlassVase();
			zone.moddev.mc.ironagefurniture.api.surface.GlassVaseSurface.register(net.minecraft.item.Item.getItemFromBlock(glassVase));
		}
		FurnitureFactory.createSurfaceDisplayBlocker("surface_display_blocker");
		FurnitureFactory.createVanillaFoodDisplay("vanilla_food_display");
		for (String suffix : WoodVariantHelper.getEnabledWoodSuffixes()) {
			if (IronAgeFurnitureConfiguration.GENERATE_DINING_TABLES) tables.put(suffix, FurnitureFactory.createDiningTable("table_dining_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_LOW_TABLES) lowTables.put(suffix, FurnitureFactory.createLowTable("table_low_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_WALL_SHELVES) shelves.put(suffix, FurnitureFactory.createWallShelf("shelf_wall_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_CABINETS) cabinets.put(suffix, FurnitureFactory.createCabinet("cabinet_wood_ironage_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_CABINETS && IronAgeFurnitureConfiguration.GENERATE_HALF_CABINETS)
				halfCabinets.put(suffix, FurnitureFactory.createHalfCabinet("half_cabinet_wood_ironage_" + suffix));
			if (IronAgeFurnitureConfiguration.GENERATE_BARRELS) {
				barrels.put(suffix, FurnitureFactory.createBarrel("barrel_wood_ironage_" + suffix, false));
				if (IronAgeFurnitureConfiguration.GENERATE_SIDE_BARRELS)
					sideBarrels.put(suffix, FurnitureFactory.createBarrel("side_barrel_wood_ironage_" + suffix, true));
			}
			if (IronAgeFurnitureConfiguration.GENERATE_BOTTLE_RACKS)
				racks.put(suffix, FurnitureFactory.createBottleRack("bottle_rack_wood_ironage_" + suffix));
		}
	}

	public static void registerRecipes() {
		net.minecraftforge.oredict.RecipeSorter.register("ironagefurniture:side_barrel", zone.moddev.mc.ironagefurniture.api.recipes.SideBarrelRecipe.class,
			net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		net.minecraftforge.oredict.RecipeSorter.register("ironagefurniture:upright_barrel", zone.moddev.mc.ironagefurniture.api.recipes.UprightBarrelRecipe.class,
			net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		if (glassVase != null) for (net.minecraft.item.EnumDyeColor colour : net.minecraft.item.EnumDyeColor.values()) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(glassVase, 4, colour.getMetadata()),
				" x ", "x x", " x ", 'x', new ItemStack(Blocks.STAINED_GLASS, 1, colour.getMetadata())));
		}
		WoodVariantHelper.forEachEnabledSlabVariant((suffix, slab) -> {
			if (racks.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(racks.get(suffix), 2), "s s", " t ", "s s", 's', slab, 't', "stickWood"));
			if (tables.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tables.get(suffix)), "sss", "t t", 's', slab, 't', "stickWood"));
			if (lowTables.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lowTables.get(suffix)), "sss", " s ", "t t", 's', slab, 't', "stickWood"));
			if (shelves.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(shelves.get(suffix), 3), "sss", " t ", 's', slab, 't', "stickWood"));
		});
		WoodVariantHelper.forEachEnabledPlankVariant((suffix, plank) -> {
			if (cabinets.containsKey(suffix)) GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(cabinets.get(suffix)), Blocks.CHEST, plank));
			if (halfCabinets.containsKey(suffix)) GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(halfCabinets.get(suffix), 2), cabinets.get(suffix)));
			if (barrels.containsKey(suffix)) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(barrels.get(suffix), 3), "ppp", " i ", "ppp", 'p', plank, 'i', Items.IRON_INGOT));
			if (sideBarrels.containsKey(suffix)) {
				GameRegistry.addRecipe(new zone.moddev.mc.ironagefurniture.api.recipes.SideBarrelRecipe(barrels.get(suffix), sideBarrels.get(suffix)));
				GameRegistry.addRecipe(new zone.moddev.mc.ironagefurniture.api.recipes.UprightBarrelRecipe(sideBarrels.get(suffix), barrels.get(suffix)));
			}
		});
	}
}
