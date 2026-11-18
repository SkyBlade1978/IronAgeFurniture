package zone.moddev.mc.ironagefurniture.api;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.BottleRack;
import zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.GlassVaseBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceLava;
import zone.moddev.mc.ironagefurniture.api.Blocks.MultiBlockBed;
import zone.moddev.mc.ironagefurniture.api.Blocks.MultiBlockChair;
import zone.moddev.mc.ironagefurniture.api.Blocks.MultiBlockWoodBed;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockBarrel;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockBottleRack;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockGlassVase;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockMetalVariant;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockPaddedBench;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockThrowableLavaLamp;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockUpholsteredFurniture;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockWallShelf;
import zone.moddev.mc.ironagefurniture.init.FurnitureCreativeTabs;
import zone.moddev.mc.ironagefurniture.init.ItemInitialiser;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

final class FurnitureRegistration {

	private FurnitureRegistration() {
	}

	static Block registerBlock(Block block, String name, int maxStackSize, boolean registerItem) {
		GameRegistry.register(block.setRegistryName(Ironagefurniture.MODID, name));
		block.setUnlocalizedName(Ironagefurniture.MODID + "." + name);
		block.setCreativeTab(registerItem ? FurnitureCreativeTabs.forName(name) : null);

		if (registerItem) {
			ItemBlock itemBlock = createItemBlock(block);
			itemBlock.setMaxStackSize(maxStackSize);
			ItemInitialiser.RegisterItem(itemBlock, name);
		}

		Ironagefurniture.BlockRegistry.put(name, block);

		if (block instanceof DiningTable || block instanceof Cabinet || block instanceof WallShelf
				|| block instanceof StorageBarrel || block instanceof BottleRack) {
			Blocks.FIRE.setFireInfo(block, 5, 20);
		}

		return block;
	}

	private static ItemBlock createItemBlock(Block block) {
		if (block instanceof StorageBarrel) {
			return new ItemBlockBarrel(block);
		}

		if (block instanceof BottleRack) {
			return new ItemBlockBottleRack(block);
		}

		if (block instanceof GlassVaseBlock) {
			return new ItemBlockGlassVase(block);
		}

		if (block instanceof MultiBlockBed || block instanceof MultiBlockWoodBed || block instanceof MultiBlockChair) {
			return new ItemBlockUpholsteredFurniture(block);
		}

		if (MetalVariantHelper.isMetalVariantBlock(block)) {
			return new ItemBlockMetalVariant(block);
		}

		if (block instanceof WallShelf) {
			return new ItemBlockWallShelf(block);
		}

		if (block instanceof LightSourceLava) {
			return new ItemBlockThrowableLavaLamp(block);
		}

		return new ItemBlock(block);
	}

	static Block registerPaddedBlock(Block block, String name) {
		GameRegistry.register(block.setRegistryName(Ironagefurniture.MODID, name));
		block.setUnlocalizedName(Ironagefurniture.MODID + "." + name);
		block.setCreativeTab(Ironagefurniture.ironagefurnitureTab);

		ItemBlock itemBlock = new ItemBlockPaddedBench(block);
		itemBlock.setMaxStackSize(16);
		ItemInitialiser.RegisterItem(itemBlock, name);

		Ironagefurniture.BlockRegistry.put(name, block);
		return block;
	}

	static Block registerBlockWithoutItem(Block block, String name) {
		return registerBlock(block, name, 16, false);
	}

	static Block registerBlock(Block block, String name, int maxStackSize) {
		return registerBlock(block, name, maxStackSize, true);
	}

	static Block registerBlock(Block block, String name) {
		return registerBlock(block, name, 16);
	}
}
