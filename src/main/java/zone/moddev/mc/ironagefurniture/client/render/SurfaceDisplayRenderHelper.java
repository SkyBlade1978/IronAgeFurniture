package zone.moddev.mc.ironagefurniture.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/** Generic fallback with an optional specialised renderer per item. */
public final class SurfaceDisplayRenderHelper {
	private SurfaceDisplayRenderHelper() { }

	public static void renderSurfaceItem(ItemStack stack, SurfaceRenderContext context) {
		if (stack == null || stack.stackSize <= 0) return;
		SurfaceRenderRegistry.Renderer renderer = SurfaceRenderRegistry.get(stack);
		if (renderer != null) {
			renderer.render(stack, context);
			return;
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(context.x + context.itemX, context.y + context.itemY,
			context.z + context.itemZ);
		GlStateManager.rotate(context.yaw, 0.0F, 1.0F, 0.0F);
		if (stack.getItem() instanceof ItemBlock) {
			GlStateManager.scale(0.55F, 0.55F, 0.55F);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		} else {
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		}
		GlStateManager.popMatrix();
	}
}
