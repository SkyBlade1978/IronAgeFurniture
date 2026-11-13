package zone.moddev.mc.ironagefurniture.api.surface;

import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import static org.junit.Assert.*;

public class VanillaSurfaceFoodsTest {
	@BeforeClass public static void bootstrap() { Bootstrap.register(); }

	@Test public void everyVanillaEdibleHasASurfaceModel() {
		for (Item item : Item.REGISTRY) {
			if (item instanceof ItemFood && "minecraft".equals(item.getRegistryName().getResourceDomain())) {
				assertNotNull(item.getRegistryName().toString(), VanillaSurfaceFoods.find(new ItemStack(item)));
			}
		}
		assertNotNull(VanillaSurfaceFoods.find(new ItemStack(Items.CAKE)));
		for (VanillaSurfaceFoods.Food food : VanillaSurfaceFoods.all()) {
			assertNotNull(food.model, VanillaSurfaceFoods.class.getResource(
				"/assets/ironagefurniture/models/block/surface_food/" + food.model + ".json"));
		}
	}

	@Test public void fishVariantsAndEnchantedApplesKeepTheirIdentity() {
		String[] raw = {"raw_cod", "raw_salmon", "clownfish", "pufferfish"};
		for (int metadata = 0; metadata < raw.length; metadata++) {
			assertEquals(raw[metadata], VanillaSurfaceFoods.find(new ItemStack(Items.FISH, 1, metadata)).model);
		}
		assertEquals("cooked_salmon", VanillaSurfaceFoods.find(new ItemStack(Items.COOKED_FISH, 1, 1)).model);
		assertEquals("golden_apple", VanillaSurfaceFoods.find(new ItemStack(Items.GOLDEN_APPLE, 1, 1)).model);
	}

	@Test public void loafBoundsRotateWithItsLongAxis() {
		SurfaceContentRegistry.Descriptor bread = VanillaSurfaceFoods.find(new ItemStack(Items.BREAD)).descriptor;
		AxisAlignedBB north = bread.getBounds(EnumFacing.NORTH);
		AxisAlignedBB east = bread.getBounds(EnumFacing.EAST);
		assertTrue(north.maxZ - north.minZ > north.maxX - north.minX);
		assertEquals(north.maxZ - north.minZ, east.maxX - east.minX, 0.000001D);
		assertEquals(north.maxX - north.minX, east.maxZ - east.minZ, 0.000001D);
		assertEquals(0.0D, east.minY, 0.000001D);
		assertEquals(north.maxY, east.maxY, 0.000001D);
	}
}
