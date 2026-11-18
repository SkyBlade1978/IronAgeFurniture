package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.api.Blocks.MultiBlockBed;
import zone.moddev.mc.ironagefurniture.api.Blocks.MultiBlockWoodBed;
import zone.moddev.mc.ironagefurniture.api.Enumerations.UpholsteryColour;
import zone.moddev.mc.ironagefurniture.api.recipes.BedRecolourRecipe;
import zone.moddev.mc.ironagefurniture.api.recipes.MatchingUpholsteryRecipe;
import zone.moddev.mc.ironagefurniture.api.recipes.UpholsteryUpgradeRecipe;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class BedFactory {

	private BedFactory() {
	}

	public static Block CreateSingleCanopyBed(String suffix) {
		String name = "bed_canopy_foot_lower_" + suffix;
		MultiBlockBed bed = new MultiBlockBed(Material.WOOD, name, 10, 3, MultiBlockBed.SINGLE_SIDE);

		Block registeredBed = FurnitureRegistration.registerBlock(bed, name, 1);
		bed.setSingleBlock(registeredBed);
		return registeredBed;
	}

	public static Block[] CreateDoubleCanopyBed(String suffix) {
		String leftName = "bed_canopy_foot_left_lower_" + suffix;
		String rightName = "bed_canopy_foot_right_lower_" + suffix;

		MultiBlockBed leftBed = new MultiBlockBed(Material.WOOD, leftName, 10, 6, MultiBlockBed.LEFT_SIDE);
		MultiBlockBed rightBed = new MultiBlockBed(Material.WOOD, rightName, 10, 6, MultiBlockBed.RIGHT_SIDE);

		Block leftBlock = FurnitureRegistration.registerBlock(leftBed, leftName, 1);
		Block rightBlock = FurnitureRegistration.registerBlockWithoutItem(rightBed, rightName);

		leftBed.setDoubleBlocks(leftBlock, rightBlock);
		rightBed.setDoubleBlocks(leftBlock, rightBlock);
		return new Block[] { leftBlock, rightBlock };
	}

	public static Block CreateSingleWoodBed(String suffix) {
		String name = "bed_wood_foot_" + suffix;
		return FurnitureRegistration.registerBlock(new MultiBlockWoodBed(Material.WOOD, name, 10, 3, false), name, 1);
	}

	public static Block CreateDoubleWoodBed(String suffix) {
		String name = "bed_wood_foot_left_" + suffix;
		return FurnitureRegistration.registerBlock(new MultiBlockWoodBed(Material.WOOD, name, 10, 6, true), name, 1);
	}

	public static void AddSingleCanopyBedRecipe(ItemStack planks, Block bed) {
		String wood = bed.getRegistryName().getResourcePath().substring("bed_canopy_foot_lower_".length());
		AddSingleCanopyBedRecipe(planks, BlockObjectHolder.bed_wood_single.get(wood), bed);
	}

	public static void AddSingleCanopyBedRecipe(ItemStack planks, Block woodenBed, Block bed) {
		for (UpholsteryColour colour : UpholsteryColour.values()) {
			GameRegistry.addRecipe(new UpholsteryUpgradeRecipe(woodenBed, bed, planks, colour));
		}
	}

	public static void AddDoubleCanopyBedRecipe(Block singleBed, Block doubleBed) {
		GameRegistry.addRecipe(new MatchingUpholsteryRecipe(singleBed, doubleBed));
	}

	public static void AddSingleWoodBedRecipe(ItemStack planks, Block bed) {
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(bed, 1), Items.BED, planks));
	}

	public static void AddDoubleWoodBedRecipe(Block singleBed, Block doubleBed) {
		GameRegistry.addRecipe(new MatchingUpholsteryRecipe(singleBed, doubleBed));
	}

	public static void AddBedRecolourRecipe(Block bed) {
		GameRegistry.addRecipe(new BedRecolourRecipe(bed));
	}
}
