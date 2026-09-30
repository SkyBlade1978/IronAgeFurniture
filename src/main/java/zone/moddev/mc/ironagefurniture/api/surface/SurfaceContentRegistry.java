package zone.moddev.mc.ironagefurniture.api.surface;

import java.util.IdentityHashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Common-side description of an item displayed on furniture. */
public final class SurfaceContentRegistry {
	public interface Provider {
		Descriptor describe(ItemStack stack);
	}

	public static final class Descriptor {
		private final Category category;
		private final double width;
		private final double height;

		public Descriptor(Category category, double width, double height) {
			if (category == null || width <= 0.0D || width > 1.0D
					|| height <= 0.0D || height > 1.0D) {
				throw new IllegalArgumentException("Invalid surface descriptor");
			}
			this.category = category;
			this.width = width;
			this.height = height;
		}

		public Category getCategory() { return this.category; }
		public double getWidth() { return this.width; }
		public double getHeight() { return this.height; }
	}

	private static final Descriptor DEFAULT = new Descriptor(Category.SINGLETON, 0.5D, 0.75D);
	private static final Map<Item, Provider> PROVIDERS = new IdentityHashMap<Item, Provider>();
	private static boolean frozen;

	private SurfaceContentRegistry() { }

	/** Register during pre-init or init; the table freezes at post-init. */
	public static synchronized void register(Item item, Provider provider) {
		if (frozen || item == null || provider == null || PROVIDERS.containsKey(item)) {
			throw new IllegalStateException("Invalid or duplicate surface provider registration");
		}
		PROVIDERS.put(item, provider);
	}

	public static synchronized void freeze() {
		frozen = true;
	}

	public static Descriptor describe(ItemStack stack) {
		if (stack == null || stack.stackSize <= 0) return DEFAULT;
		Provider provider;
		synchronized (SurfaceContentRegistry.class) {
			provider = PROVIDERS.get(stack.getItem());
		}
		if (provider == null) return DEFAULT;
		Descriptor result = provider.describe(stack);
		return result == null ? DEFAULT : result;
	}
}
