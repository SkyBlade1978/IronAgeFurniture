package zone.moddev.mc.ironagefurniture.api.recipes;

import static org.junit.Assert.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.ironagefurniture.api.Enumerations.UpholsteryColour;
import zone.moddev.mc.ironagefurniture.api.UpholsteryColourHelper;

public class UpholsteryUpgradeContractTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }
    private InventoryCrafting grid(ItemStack source, ItemStack wood, ItemStack carpet, int rotation) {
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            @Override public boolean canInteractWith(EntityPlayer player) { return false; }
        }, 3, 3);
        int[][] slots = {{0,4,8},{8,0,4},{4,8,0},{8,4,0},{0,8,4},{4,0,8}};
        grid.setInventorySlotContents(slots[rotation][0], source);
        grid.setInventorySlotContents(slots[rotation][1], wood);
        grid.setInventorySlotContents(slots[rotation][2], carpet);
        return grid;
    }
    @Test public void allColoursAreShapelessAndStoredColourWinsOverDamage() {
        for (UpholsteryColour colour : UpholsteryColour.values()) {
            UpholsteryUpgradeRecipe recipe = new UpholsteryUpgradeRecipe(Blocks.BRICK_BLOCK, Blocks.STONE,
                    new ItemStack(Blocks.PLANKS, 1, 2), colour);
            for (int order = 0; order < 6; ++order) {
                ItemStack source = UpholsteryColourHelper.createStack(Blocks.BRICK_BLOCK, 1, colour);
                source.setItemDamage((colour.getItemMetadata() + 1) % 16);
                InventoryCrafting grid = grid(source, new ItemStack(Blocks.PLANKS, 1, 2),
                        new ItemStack(Blocks.CARPET, 1, colour.getCarpetMetadata()), order);
                assertTrue(recipe.matches(grid, null));
                assertSame(colour, UpholsteryColourHelper.getColour(recipe.getCraftingResult(grid)));
                assertSame(colour, UpholsteryColourHelper.getColour(recipe.getRecipeOutput()));
            }
        }
    }
    @Test public void rejectRecolouringWrongWoodAndExtraIngredients() {
        UpholsteryUpgradeRecipe recipe = new UpholsteryUpgradeRecipe(Blocks.BRICK_BLOCK, Blocks.STONE,
                new ItemStack(Blocks.PLANKS, 1, 2), UpholsteryColour.BLUE);
        ItemStack source = UpholsteryColourHelper.createStack(Blocks.BRICK_BLOCK, 1, UpholsteryColour.BLUE);
        assertFalse(recipe.matches(grid(source, new ItemStack(Blocks.PLANKS, 1, 1),
                new ItemStack(Blocks.CARPET, 1, 11), 0), null));
        assertFalse(recipe.matches(grid(source, new ItemStack(Blocks.PLANKS, 1, 2),
                new ItemStack(Blocks.CARPET, 1, 14), 0), null));
        InventoryCrafting extra = grid(source, new ItemStack(Blocks.PLANKS, 1, 2),
                new ItemStack(Blocks.CARPET, 1, 11), 0);
        extra.setInventorySlotContents(1, new ItemStack(net.minecraft.init.Items.STICK));
        assertFalse(recipe.matches(extra, null));
    }
    @Test public void legacyMetadataWorksAndInvalidColourDefaultsToRed() {
        for (UpholsteryColour colour : UpholsteryColour.values()) {
            UpholsteryUpgradeRecipe recipe = new UpholsteryUpgradeRecipe(Blocks.BRICK_BLOCK, Blocks.STONE,
                    new ItemStack(Blocks.PLANKS), colour);
            assertTrue(recipe.matches(grid(new ItemStack(Blocks.BRICK_BLOCK, 1, colour.getItemMetadata()),
                    new ItemStack(Blocks.PLANKS), new ItemStack(Blocks.CARPET, 1, colour.getCarpetMetadata()), 0), null));
        }
        ItemStack invalid = new ItemStack(Blocks.BRICK_BLOCK, 1, 11);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Color", "invalid");
        invalid.setTagCompound(tag);
        UpholsteryUpgradeRecipe recipe = new UpholsteryUpgradeRecipe(Blocks.BRICK_BLOCK, Blocks.STONE,
                new ItemStack(Blocks.PLANKS), UpholsteryColour.RED);
        assertTrue(recipe.matches(grid(invalid, new ItemStack(Blocks.PLANKS),
                new ItemStack(Blocks.CARPET, 1, 14), 0), null));
    }
}
