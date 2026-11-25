package zone.moddev.mc.ironagefurniture.api.surface;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Original add-on registrations use the same registry as Core content. */
@Deprecated
public final class SurfaceContentRegistry {
	public interface Provider {
		Descriptor describe(ItemStack stack);
	}

	public static final class Descriptor {
		private final zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.Descriptor core;

		public Descriptor(SurfaceSetting.Category category, double width, double height) {
			this(new zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.Descriptor(
				zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Category.valueOf(category.name()), width, height));
		}

		public Descriptor(SurfaceSetting.Category category, AxisAlignedBB bounds) {
			this(new zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.Descriptor(
				zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Category.valueOf(category.name()), bounds));
		}

		private Descriptor(zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.Descriptor core) {
			this.core = core;
		}

		public SurfaceSetting.Category getCategory() { return SurfaceSetting.Category.valueOf(this.core.getCategory().name()); }
		public double getWidth() { return this.core.getWidth(); }
		public double getHeight() { return this.core.getHeight(); }
		public AxisAlignedBB getBounds(EnumFacing facing) { return this.core.getBounds(facing); }
		public ShelfLayout getShelfLayout() { return ShelfLayout.valueOf(this.core.getShelfLayout().name()); }
		public boolean acceptsProduceTray() { return this.core.acceptsProduceTray(); }

		public Descriptor withShelfLayout(ShelfLayout layout) {
			return new Descriptor(this.core.withShelfLayout(layout.core()));
		}

		public Descriptor withProduceTray() { return new Descriptor(this.core.withProduceTray()); }
	}

	private SurfaceContentRegistry() { }

	public static void register(Item item, Provider provider) {
		if (provider == null) throw new IllegalArgumentException("Missing surface provider");
		zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.register(item, stack -> {
			Descriptor descriptor = provider.describe(stack);
			return descriptor == null ? null : descriptor.core;
		});
	}

	public static void freeze() { zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.freeze(); }

	public static Descriptor describe(ItemStack stack) {
		return new Descriptor(zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.describe(stack));
	}
}
