package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.api.Blocks.LightHolderSconceFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightHolderSconceWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceCandleFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceCandleFloorUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceCandleWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceCandleWallUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceGlowdust;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceLava;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceRed;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceCandleFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceCandleFloorUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceCandleWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceCandleWallUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceGlowFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceGlowWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceLavaFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceLavaWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRedFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRedTorchFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRedTorchFloorUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRedTorchWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRedTorchWallUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRedWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRockSaltFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceRockSaltWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchFloorTwin;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchFloorTwinUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchFloorUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchWall;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchWallTwin;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchWallTwinUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceTorchWallUnlit;
import zone.moddev.mc.ironagefurniture.api.Blocks.ObsideanLump;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public final class LightingFactory {

	private LightingFactory() {
	}

	public static Block CreateIronWallSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightHolderSconceWall(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronFloorSconce(String name) {
		return FurnitureRegistration.registerBlock(new LightHolderSconceFloor(Material.IRON, name, 10, 1), name, 64);
	}

	public static Block CreateIronFloorTorchSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchFloor(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronFloorTorchSconceUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchFloorUnlit(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronFloorTorchSconceTwin(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchFloorTwin(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronFloorTorchSconceTwinUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchFloorTwinUnlit(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallTorchSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchWall(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallTorchSconceUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchWallUnlit(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallTorchSconceTwin(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchWallTwin(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallTorchSconceTwinUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceTorchWallTwinUnlit(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronFloorRedTorchSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRedTorchFloor(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronFloorRedTorchSconceUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRedTorchFloorUnlit(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallRedTorchSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRedTorchWall(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallRedTorchSconceUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRedTorchWallUnlit(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateGlowdustLamp(String name) {
		return FurnitureRegistration.registerBlock(new LightSourceGlowdust(Material.GLASS, name, 1.5F, 0.3F), name, 64);
	}

	public static Block CreateIronFloorGlowSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceGlowFloor(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallGlowSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceGlowWall(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronFloorRockSaltSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRockSaltFloor(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallRockSaltSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRockSaltWall(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateLavaLamp(String name) {
		return FurnitureRegistration.registerBlock(new LightSourceLava(Material.GLASS, name, 1.5F, 0.3F), name, 64);
	}

	public static Block CreateIronFloorLavaSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceLavaFloor(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateIronWallLavaSconce(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceLavaWall(Material.IRON, name, 10, 1), name);
	}

	public static Block CreateRedLamp(String name) {
		return createRedLamp(name, 0, true);
	}

	public static Block CreateRedLampVariant(String name, int lightLevel) {
		return createRedLamp(name, lightLevel, false);
	}

	private static Block createRedLamp(String name, int lightLevel, boolean registerItem) {
		return FurnitureRegistration.registerBlock(new LightSourceRed(Material.GLASS, name, 1.5F, 0.3F, lightLevel),
				name, 64, registerItem);
	}

	public static Block CreateIronFloorRedSconce(String name, int lightLevel) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRedFloor(Material.IRON, name, 10, 1, lightLevel), name);
	}

	public static Block CreateIronWallRedSconce(String name, int lightLevel) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceRedWall(Material.IRON, name, 10, 1, lightLevel), name);
	}

	public static Block CreateObsidianChunk(String name) {
		return FurnitureRegistration.registerBlock(new ObsideanLump(Material.ROCK, name, 10, 1), name, 64);
	}

	public static Block CreateCandleFloor(String name) {
		return FurnitureRegistration.registerBlock(new LightSourceCandleFloor(Material.CIRCUITS, name, 1, 0.1F), name, 64);
	}

	public static Block CreateCandleWall(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceCandleWall(Material.CIRCUITS, name, 1, 0.1F), name);
	}

	public static Block CreateCandleFloorUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceCandleFloorUnlit(Material.CIRCUITS, name, 1, 0.1F), name);
	}

	public static Block CreateCandleWallUnlit(String name) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceCandleWallUnlit(Material.CIRCUITS, name, 1, 0.1F), name);
	}

	public static Block CreateIronFloorCandleSconce(String name, int candleCount) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceCandleFloor(Material.IRON, name, 10, 1, candleCount), name);
	}

	public static Block CreateIronFloorCandleSconceUnlit(String name, int candleCount) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceCandleFloorUnlit(Material.IRON, name, 10, 1, candleCount), name);
	}

	public static Block CreateIronWallCandleSconce(String name, int candleCount) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceCandleWall(Material.IRON, name, 10, 1, candleCount), name);
	}

	public static Block CreateIronWallCandleSconceUnlit(String name, int candleCount) {
		return FurnitureRegistration.registerBlockWithoutItem(new LightSourceSconceCandleWallUnlit(Material.IRON, name, 10, 1, candleCount), name);
	}

	public static void AddIronSconceRecipe(Block sconce) {
		Object ironInput = "nuggetIron";
		int outputCount = 4;

		// Vanilla 1.10.2 has no iron nuggets, so use ingots when no mod supplies them.
		if (!OreDictionary.doesOreNameExist("nuggetIron") || OreDictionary.getOres("nuggetIron").isEmpty()) {
			ironInput = "ingotIron";
			outputCount = 32;
		}

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sconce, outputCount),
				"xxx", "x  ", "x  ", 'x', ironInput));
	}
}
