package zone.moddev.mc.ironagefurniture.api.surface;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSettingHost;
import zone.moddev.mc.skysfurniturecore.api.surface.VanillaSurfaceBottles;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.Descriptor;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Category;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public class SurfaceExtensionTest {
	private static final class TestSurfaceHost implements SurfaceSettingHost {
		private final SurfaceSetting setting = new SurfaceSetting();
		private int changes;
		@Override public SurfaceSetting getSurfaceSetting() { return setting; }
		@Override public void markSurfaceSettingChanged() { changes++; }
	}

	@BeforeClass public static void bootstrap() { Bootstrap.register(); }

	@Test public void twoVasesKeepIndependentDataAndLegacyPlacement() {
		SurfaceContentRegistry.register(Items.ITEM_FRAME, stack -> new Descriptor(Category.VASE, 0.46D, 0.56D));
		ItemStack first = new ItemStack(Items.ITEM_FRAME, 1, 3).setStackDisplayName("Rose vase");
		first.getTagCompound().setString("Flower", "rose");
		ItemStack second = new ItemStack(Items.ITEM_FRAME, 1, 11).setStackDisplayName("Daisy vase");
		second.getTagCompound().setString("Flower", "daisy");

		for (EnumFacing.Axis axis : new EnumFacing.Axis[] { EnumFacing.Axis.X, EnumFacing.Axis.Z }) {
			SurfaceSetting setting = new SurfaceSetting();
			setting.setSingleItem(first, EnumFacing.NORTH);
			NBTTagCompound legacy = new NBTTagCompound();
			setting.writeToNBT(legacy);
			setting.readFromNBT(legacy);
			assertTrue(setting.canInsert(second, EnumFacing.NORTH, 0.9F, 0.9F, axis));
			assertEquals(1, setting.getItemCount());
			assertTrue(setting.insert(second, EnumFacing.NORTH, 0.9F, 0.9F, axis));
			assertEquals(2, setting.getItemCount());
			assertTrue(ItemStack.areItemStacksEqual(first, setting.getItem(Slot.GUEST_A)));
			assertTrue(ItemStack.areItemStacksEqual(second, setting.getItem(Slot.GUEST_B)));
			assertFalse(setting.insert(first, EnumFacing.NORTH, 0.5F, 0.5F, axis));
			assertFalse(setting.insert(new ItemStack(Items.GLASS_BOTTLE), EnumFacing.NORTH, 0.5F, 0.5F, axis));

			float firstX = (float)setting.getX(Slot.GUEST_A);
			float firstZ = (float)setting.getZ(Slot.GUEST_A);
			assertEquals(Slot.GUEST_A, setting.findTarget(firstX, firstZ));
			NBTTagCompound saved = new NBTTagCompound();
			setting.writeToNBT(saved);
			setting.readFromNBT(saved);
			assertTrue(ItemStack.areItemStacksEqual(first, setting.remove(Slot.GUEST_A)));
			assertTrue(ItemStack.areItemStacksEqual(second, setting.getItem(Slot.GUEST_B)));
			assertTrue(setting.insert(first, EnumFacing.SOUTH, firstX, firstZ, axis));
			assertTrue(ItemStack.areItemStacksEqual(first, setting.getItem(Slot.GUEST_A)));
		}
	}

	@Test public void rayHitEdgesRemainTargetableAfterClickPacketFloatConversion() {
		for (double width : new double[]{4.5D / 16.0D * GlassVaseSurface.SCALE, 0.56D * GlassVaseSurface.SCALE}) {
			Item item = new Item();
			SurfaceContentRegistry.register(item, stack -> new Descriptor(Category.SINGLETON, width, 0.5D));
			for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
				SurfaceSetting setting = new SurfaceSetting();
				setting.setSingleItem(new ItemStack(item), facing);
				AxisAlignedBB bounds = setting.getItemBounds(Slot.CENTER);
				for (double x : new double[]{bounds.minX, bounds.maxX})
					assertEquals("Packet rounded X outside the displayed vase", Slot.CENTER, setting.findTarget((float)x, 0.5F));
				for (double z : new double[]{bounds.minZ, bounds.maxZ})
					assertEquals("Packet rounded Z outside the displayed vase", Slot.CENTER, setting.findTarget(0.5F, (float)z));
				assertNull(setting.findTarget((float)(bounds.minX - 0.001D), 0.5F));
				assertNull(setting.findTarget(0.5F, (float)(bounds.maxZ + 0.001D)));
			}
		}
	}

	@Test public void testOnlyHostRetainsContentsAndSignalsChanges() {
		TestSurfaceHost host = new TestSurfaceHost();
		ItemStack decorated = new ItemStack(Items.BOOK);
		decorated.setStackDisplayName("Named menu");
		assertTrue(host.getSurfaceSetting().insert(decorated, EnumFacing.NORTH, 0.5F, 0.5F, null));
		host.markSurfaceSettingChanged();
		assertEquals(1, host.changes);
		NBTTagCompound data = new NBTTagCompound();
		host.getSurfaceSetting().writeToNBT(data);
		SurfaceSetting reloaded = new SurfaceSetting();
		reloaded.readFromNBT(data);
		assertTrue(ItemStack.areItemStackTagsEqual(decorated, reloaded.getItem(Slot.CENTER)));
		assertEquals("Named menu", reloaded.getItem(Slot.CENTER).getDisplayName());
	}

	@Test public void unknownItemsHaveGenericFallbackAndExactNbtRoundTrip() {
		ItemStack original = new ItemStack(Items.ARMOR_STAND, 3);
		original.setStackDisplayName("Harvest feast");
		original.getTagCompound().setInteger("CustomData", 41);
		assertEquals(Category.SINGLETON, SurfaceSetting.classify(original));
		SurfaceSetting setting = new SurfaceSetting();
		assertTrue(setting.insert(original, EnumFacing.EAST, 0.5F, 0.5F, null));
		assertEquals(3, original.stackSize);
		assertEquals(1, setting.getItem(Slot.CENTER).stackSize);
		NBTTagCompound saved = new NBTTagCompound();
		setting.writeToNBT(saved);
		SurfaceSetting reloaded = new SurfaceSetting();
		reloaded.readFromNBT(saved);
		assertTrue(ItemStack.areItemsEqual(setting.getItem(Slot.CENTER), reloaded.getItem(Slot.CENTER)));
		assertTrue(ItemStack.areItemStackTagsEqual(setting.getItem(Slot.CENTER),
			reloaded.getItem(Slot.CENTER)));
		assertEquals(EnumFacing.EAST, reloaded.getFacing(Slot.CENTER));
		assertNull(reloaded.getAxis());
	}

	@Test public void donorSingleItemTagLoadsAndRewritesToCurrentFormat() {
		ItemStack original = new ItemStack(Items.BOOK);
		original.setStackDisplayName("Old display");
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("DisplayedItem", original.writeToNBT(new NBTTagCompound()));
		tag.setInteger("DisplayedFacing", EnumFacing.SOUTH.getHorizontalIndex());
		SurfaceSetting setting = new SurfaceSetting();
		setting.readFromNBT(tag);
		assertEquals(EnumFacing.SOUTH, setting.getFacing(Slot.CENTER));
		setting.writeToNBT(tag);
		assertFalse(tag.hasKey("DisplayedItem"));
		assertTrue(tag.hasKey("SurfaceItems"));
	}

	@Test public void registeredItemControlsCategoryBoundsAndRendererLookup() {
		Item item = new Item();
		SurfaceContentRegistry.register(item,
			stack -> new Descriptor(Category.BOTTLE, 0.22D, 0.68D));
		ItemStack bottle = new ItemStack(item);
		assertEquals(Category.BOTTLE, SurfaceSetting.classify(bottle));
		assertEquals(0.22D, SurfaceContentRegistry.describe(bottle).getWidth(), 0.0001D);
		SurfaceRenderRegistry.Renderer renderer = (stack, context) -> { };
		SurfaceRenderRegistry.register(item, renderer);
		assertSame(renderer, SurfaceRenderRegistry.get(bottle));
		assertNull(SurfaceRenderRegistry.get(new ItemStack(Items.ARMOR_STAND)));
	}

	@Test public void vanillaPotionsRetainExactDataAndAllowTwoBottleSettings() {
		VanillaSurfaceBottles.register();
		for (Item item : new Item[]{Items.GLASS_BOTTLE, Items.POTIONITEM, Items.SPLASH_POTION,
				Items.LINGERING_POTION, Items.EXPERIENCE_BOTTLE}) {
			ItemStack original = new ItemStack(item).setStackDisplayName("Named bottle");
			original.getTagCompound().setInteger("TestData", 42);
			if (item == Items.POTIONITEM) {
				net.minecraft.potion.PotionUtils.addPotionToItemStack(original, net.minecraft.init.PotionTypes.HEALING);
				net.minecraft.potion.PotionUtils.appendEffects(original, java.util.Collections.singletonList(
					new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.SPEED, 600, 2)));
			}
			SurfaceSetting setting = new SurfaceSetting();
			assertEquals(Category.BOTTLE, SurfaceSetting.classify(original));
			NBTTagCompound legacy = new NBTTagCompound();
			legacy.setTag("DisplayedItem", original.writeToNBT(new NBTTagCompound()));
			setting.readFromNBT(legacy);
			assertTrue(ItemStack.areItemStacksEqual(original, setting.getCompatibilityItem()));
			setting.clear();
			assertTrue(setting.insert(original, EnumFacing.NORTH, 0.5F, 0.5F, null));
			assertTrue(setting.insert(new ItemStack(Items.GLASS_BOTTLE), EnumFacing.NORTH, 0.75F, 0.5F, null));
			assertEquals(2, setting.getItemCount());
			NBTTagCompound tag = new NBTTagCompound(); setting.writeToNBT(tag);
			SurfaceSetting reloaded = new SurfaceSetting(); reloaded.readFromNBT(tag);
			boolean found = false;
			for (Slot slot : Slot.values()) if (ItemStack.areItemStacksEqual(original, reloaded.getItem(slot))) found = true;
			assertTrue("Bottle data changed in the surface save", found);
		}
	}
}
