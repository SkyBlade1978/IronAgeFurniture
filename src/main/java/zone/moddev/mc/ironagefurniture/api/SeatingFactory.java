package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.api.Blocks.BackBench;
import zone.moddev.mc.ironagefurniture.api.Blocks.Bench;
import zone.moddev.mc.ironagefurniture.api.Blocks.Chair;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningChair;
import zone.moddev.mc.ironagefurniture.api.Blocks.PaddedBackBench;
import zone.moddev.mc.ironagefurniture.api.Blocks.PaddedBench;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShieldChair;
import zone.moddev.mc.ironagefurniture.api.Blocks.Stool;
import zone.moddev.mc.ironagefurniture.api.Blocks.ThroneChair;
import zone.moddev.mc.ironagefurniture.api.Blocks.WingbackChair;
import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.Enumerations.UpholsteryColour;
import zone.moddev.mc.ironagefurniture.api.recipes.ShieldChairRecipe;
import zone.moddev.mc.ironagefurniture.api.recipes.UpholsteryUpgradeRecipe;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class SeatingFactory {

	private SeatingFactory() {
	}

	public static Block CreateWoodChair(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerBlock(new Chair(Material.WOOD, name, resistance, hardness), name);
	}

	public static Block CreateWoodChair(String name) {
		return CreateWoodChair(name, 10, 1);
	}

	public static Block CreateWoodDiningChair(String name) {
		return FurnitureRegistration.registerBlock(new DiningChair(Material.WOOD, name, 10, 1), name);
	}

	public static Block CreateWoodShieldChair(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerBlock(new ShieldChair(Material.WOOD, name, resistance, hardness), name);
	}

	public static Block CreateWoodShieldChair(String name) {
		return CreateWoodShieldChair(name, 10, 1);
	}

	public static Block CreateWoodWingbackChair(String name) {
		return FurnitureRegistration.registerBlock(new WingbackChair(Material.WOOD, name, 10, 2), name);
	}

	public static Block CreateWoodThroneChair(String name) {
		return FurnitureRegistration.registerBlock(new ThroneChair(Material.WOOD, name, 10, 3), name);
	}

	public static Block CreateWoodShortStool(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerBlock(new Stool(Material.WOOD, name, resistance, false, 0.25, hardness), name);
	}

	public static Block CreateWoodShortStool(String name) {
		return CreateWoodShortStool(name, 10, 1);
	}

	public static Block CreateWoodTallStool(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerBlock(new Stool(Material.WOOD, name, resistance, true, 0.6, hardness), name);
	}

	public static Block CreateWoodTallStool(String name) {
		return FurnitureRegistration.registerBlock(new Stool(Material.WOOD, name, 10, true, 0.6, 1), name);
	}

	public static Block CreateWoodBench(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerBlock(new Bench(Material.WOOD, name, resistance, false, 0.25, hardness), name);
	}

	public static Block CreateWoodBench(String name) {
		return CreateWoodBench(name, 10, 1);
	}

	public static Block CreateWoodBackBench(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerBlock(new BackBench(Material.WOOD, name, resistance, false, 0.25, hardness), name);
	}

	public static Block CreateWoodBackBench(String name) {
		return CreateWoodBackBench(name, 10, 1);
	}

	public static Block CreateWoodPaddedBench(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerPaddedBlock(
				new PaddedBench(Material.WOOD, name, resistance, false, 0.25, hardness), name);
	}

	public static Block CreateWoodPaddedBench(String name) {
		return CreateWoodPaddedBench(name, 10, 1);
	}

	public static Block CreateWoodPaddedBackBench(String name, float resistance, float hardness) {
		return FurnitureRegistration.registerPaddedBlock(
				new PaddedBackBench(Material.WOOD, name, resistance, false, 0.25, hardness), name);
	}

	public static Block CreateWoodPaddedBackBench(String name) {
		return CreateWoodPaddedBackBench(name, 10, 1);
	}

	public static void AddClassicChairRecipe(ItemStack planks, Block chair) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(chair, 1),
				"x  ", "xxx", "y y", 'x', planks, 'y', "stickWood"));
	}

	public static void AddDiningChairRecipe(ItemStack slab, Block chair) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(chair, 1),
				"s  ", "sss", "t t", 's', slab, 't', "stickWood"));
	}

	public static void AddWingbackChairRecipe(ItemStack planks, Block chairIn, Block chairOut) {
		for (UpholsteryColour colour : UpholsteryColour.values()) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(UpholsteryColourHelper.createStack(chairOut, 1, colour),
					new ItemStack(Blocks.CARPET, 1, colour.getCarpetMetadata()), planks, chairIn));
		}
	}

	public static void AddThroneChairRecipe(ItemStack planks, Block chairIn, Block chairOut) {
		for (UpholsteryColour colour : UpholsteryColour.values()) {
			GameRegistry.addRecipe(new UpholsteryUpgradeRecipe(chairIn, chairOut, planks, colour));
		}
	}

	public static void AddShortStoolRecipe(ItemStack planks, Block stool) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(stool, 1), " x ", "yyy", 'x', planks, 'y', "stickWood"));
	}

	public static void AddBenchRecipe(ItemStack planks, Block bench) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(bench, 1), "xx", "yy", 'x', planks, 'y', "stickWood"));
	}

	public static void AddBackBenchRecipe(ItemStack planks, Block bench) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(bench, 1), "x ", "xx", "yy", 'x', planks, 'y', "stickWood"));
	}

	public static void AddLogBenchRecipe(ItemStack log, Block bench) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(bench, 6), "xxx", 'x', log));
	}

	public static void AddPaddedBenchRecipe(Block chairIn, Block chairOut) {
		for (PaddedBenchColour colour : PaddedBenchColour.values()) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(
					PaddedBenchColourHelper.createStack(chairOut, 1, colour),
					new ItemStack(chairIn, 1),
					new ItemStack(Blocks.CARPET, 1, colour.getCarpetMetadata())));
		}
	}

	public static void AddTallStoolRecipe(ItemStack planks, Block stool) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(stool, 1),
				" x ", "yyy", "yyy", 'x', planks, 'y', "stickWood"));
	}

	public static void AddShieldChairRecipe(Block chairIn, Block chairOut) {
		GameRegistry.addRecipe(new ShieldChairRecipe(chairIn, chairOut));
	}

	public static void AddChairConversionRecipe(Block chairIn, Block chairOut) {
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(chairOut, 1), new ItemStack(chairIn, 1)));
	}
}
