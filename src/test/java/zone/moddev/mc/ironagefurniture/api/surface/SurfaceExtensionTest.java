package zone.moddev.mc.ironagefurniture.api.surface;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry.Descriptor;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.client.render.SurfaceRenderRegistry;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class SurfaceExtensionTest {
	private static final class TestSurfaceHost implements SurfaceSettingHost {
		private final SurfaceSetting setting = new SurfaceSetting();
		private int changes;
		@Override public SurfaceSetting getSurfaceSetting() { return setting; }
		@Override public void markSurfaceSettingChanged() { changes++; }
	}

	@BeforeClass public static void bootstrap() { Bootstrap.register(); }

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
		ItemStack original = new ItemStack(Items.APPLE, 3);
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
		SurfaceContentRegistry.register(Items.GLASS_BOTTLE,
			stack -> new Descriptor(Category.BOTTLE, 0.22D, 0.68D));
		ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
		assertEquals(Category.BOTTLE, SurfaceSetting.classify(bottle));
		assertEquals(0.22D, SurfaceContentRegistry.describe(bottle).getWidth(), 0.0001D);
		SurfaceRenderRegistry.Renderer renderer = (stack, context) -> { };
		SurfaceRenderRegistry.register(Items.GLASS_BOTTLE, renderer);
		assertSame(renderer, SurfaceRenderRegistry.get(bottle));
		assertNull(SurfaceRenderRegistry.get(new ItemStack(Items.APPLE)));
	}
}
