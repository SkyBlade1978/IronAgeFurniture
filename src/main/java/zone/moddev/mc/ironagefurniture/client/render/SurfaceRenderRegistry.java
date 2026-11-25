package zone.moddev.mc.ironagefurniture.client.render;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Original renderer registration backed by the shared client registry. */
@Deprecated
public final class SurfaceRenderRegistry {
	public interface Renderer {
		void render(ItemStack stack, SurfaceRenderContext context);
	}

	private SurfaceRenderRegistry() { }

	public static void register(Item item, Renderer renderer) {
		if (renderer == null) throw new IllegalArgumentException("Missing renderer");
		zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry.register(item, (stack, context) ->
			renderer.render(stack, new SurfaceRenderContext(context.x, context.y, context.z, context.itemX,
				context.itemY, context.itemZ, context.blockSurfaceY, context.yaw)));
	}

	public static void freeze() { zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry.freeze(); }

	public static Renderer get(ItemStack stack) {
		zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry.Renderer renderer =
			zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry.get(stack);
		return renderer == null ? null : (item, context) -> renderer.render(item, context.core());
	}
}
