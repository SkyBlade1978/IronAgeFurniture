package zone.moddev.mc.ironagefurniture.api.surface;

import static org.junit.Assert.*;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

public class VanillaFoodPlacementTest {
	@BeforeClass
	public static void bootstrap() { Bootstrap.register(); }

	@Test
	public void knownFoodIncludesFishVariantsAndExactTaggedStacks() {
		assertTrue(VanillaFoodPlacement.isKnownFood(new ItemStack(Items.BREAD).setStackDisplayName("My loaf")));
		assertTrue(VanillaFoodPlacement.isKnownFood(new ItemStack(Items.GOLDEN_APPLE, 1, 1)));
		for (int meta = 0; meta < 4; meta++) assertTrue(VanillaFoodPlacement.isKnownFood(new ItemStack(Items.FISH, 1, meta)));
		assertFalse(VanillaFoodPlacement.isKnownFood(new ItemStack(Items.FISH, 1, 4)));
		assertFalse(VanillaFoodPlacement.isKnownFood(new ItemStack(Items.COOKED_FISH, 1, 2)));
	}

	@Test
	public void generalItemsAndUnregisteredFoodsAreNotEligible() {
		for (net.minecraft.item.Item item : new net.minecraft.item.Item[]{
				Items.DIAMOND, Items.ARMOR_STAND, Items.GLASS_BOTTLE, Items.POTIONITEM,
				Items.BEETROOT_SEEDS, Items.CAKE}) {
			assertFalse(VanillaFoodPlacement.isKnownFood(new ItemStack(item)));
		}
		assertFalse(VanillaFoodPlacement.isKnownFood(new ItemStack(new ItemFood(4, false))));
		assertFalse(VanillaFoodPlacement.isKnownFood(new ItemStack(Items.BREAD, 0)));
		assertFalse(VanillaFoodPlacement.isKnownFood(null));
	}
}
