package zone.moddev.mc.ironagefurniture.client.render;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Client-only registrations for addon-provided surface item visuals. */
public final class SurfaceRenderRegistry {
	public interface Renderer {
		void render(ItemStack stack, SurfaceRenderContext context);
	}

	private static final Map<Item, Renderer> RENDERERS = new IdentityHashMap<Item, Renderer>();
	private static boolean frozen;

	private SurfaceRenderRegistry() { }

	public static synchronized void register(Item item, Renderer renderer) {
		if (frozen || item == null || renderer == null || RENDERERS.containsKey(item)) {
			throw new IllegalStateException("Invalid or duplicate surface renderer registration");
		}
		RENDERERS.put(item, renderer);
	}

	public static synchronized void freeze() { frozen = true; }

	public static synchronized Renderer get(ItemStack stack) {
		return stack == null || stack.stackSize <= 0 ? null : RENDERERS.get(stack.getItem());
	}
}
