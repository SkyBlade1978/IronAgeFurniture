package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderContext;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Uses the rack's bottle, liquid and tint model upright, fitted to the registered bounds. */
@SideOnly(Side.CLIENT)
public final class SurfaceBottleRenderer implements SurfaceRenderRegistry.Renderer {
	static final SurfaceBottleRenderer INSTANCE = new SurfaceBottleRenderer();

	private SurfaceBottleRenderer() {
	}

	@Override
	public void render(ItemStack stack, SurfaceRenderContext context) {
		AxisAlignedBB bounds = SurfaceContentRegistry.describe(stack).getBounds(EnumFacing.NORTH);
		GlStateManager.pushMatrix();

		try {
			GlStateManager.translate(context.x + context.itemX, context.y + context.blockSurfaceY, context.z + context.itemZ);
			GlStateManager.rotate(context.yaw, 0, 1, 0);
			GlStateManager.translate((bounds.minX + bounds.maxX) / 2, bounds.minY, (bounds.minZ + bounds.maxZ) / 2);
			GlStateManager.scale((bounds.maxX - bounds.minX) / 0.148D,
				(bounds.maxY - bounds.minY) / 0.438D, (bounds.maxZ - bounds.minZ) / 0.148D);
			GlStateManager.translate(0, 0.228D, 0);
			GlStateManager.rotate(90, 1, 0, 0);
			TileEntityBottleRackRenderer.renderBottleModel(stack);
		} finally {
			GlStateManager.popMatrix();
		}
	}
}
