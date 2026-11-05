package zone.moddev.mc.ironagefurniture.api.recipes;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import zone.moddev.mc.ironagefurniture.api.Enumerations.UpholsteryColour;
import zone.moddev.mc.ironagefurniture.api.UpholsteryColourHelper;

/** Upgrades a chair or bed without silently recolouring its upholstery. */
public final class UpholsteryUpgradeRecipe extends ShapelessOreRecipe {
    private final Item source;
    private final UpholsteryColour colour;

    public UpholsteryUpgradeRecipe(Block source, Block result, ItemStack planks, UpholsteryColour colour) {
        super(UpholsteryColourHelper.createStack(result, 1, colour),
                new ItemStack(source, 1, OreDictionary.WILDCARD_VALUE), planks,
                new ItemStack(Blocks.CARPET, 1, colour.getCarpetMetadata()));
        this.source = Item.getItemFromBlock(source);
        this.colour = colour;
    }

    @Override public boolean matches(InventoryCrafting inventory, World world) {
        if (!super.matches(inventory, world)) return false;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() == source)
                return UpholsteryColourHelper.getColour(stack) == colour;
        }
        return false;
    }

    @Override public ItemStack getCraftingResult(InventoryCrafting inventory) {
        return matches(inventory, null) ? getRecipeOutput().copy() : null;
    }
}
