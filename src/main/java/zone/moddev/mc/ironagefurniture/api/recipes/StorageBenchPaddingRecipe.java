package zone.moddev.mc.ironagefurniture.api.recipes;

import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockStorageBench;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class StorageBenchPaddingRecipe extends ShapelessOreRecipe {
	private final Item bench;

	public StorageBenchPaddingRecipe(Block bench, PaddedBenchColour colour) {
		super(ItemBlockStorageBench.paddedStack(bench, colour), new ItemStack(bench, 1, OreDictionary.WILDCARD_VALUE),
			new ItemStack(Blocks.CARPET, 1, colour.getCarpetMetadata()));
		this.bench = Item.getItemFromBlock(bench);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		ItemStack result = super.getCraftingResult(inventory);

		for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
			ItemStack source = inventory.getStackInSlot(slot);
			if (source != null && source.getItem() == this.bench && source.hasTagCompound()) {
				result.setTagCompound(source.getTagCompound().copy());
				break;
			}
		}
		return result;
	}
}
