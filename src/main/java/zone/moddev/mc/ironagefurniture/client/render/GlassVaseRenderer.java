package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.VasePlantHelper;
import zone.moddev.mc.ironagefurniture.api.surface.GlassVaseSurface;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class GlassVaseRenderer implements SurfaceRenderRegistry.Renderer {
	public static void register() {
		Item item = Ironagefurniture.ItemRegistry.get("ornament_glass_vase");

		if (item != null) {
			SurfaceRenderRegistry.register(item, new GlassVaseRenderer());
		}
	}

	@Override
	public void render(ItemStack stack, SurfaceRenderContext context) {
		GlStateManager.pushMatrix();

		try {
			GlStateManager.translate(context.x + context.itemX, context.y + context.blockSurfaceY, context.z + context.itemZ);
			GlStateManager.rotate(context.yaw, 0, 1, 0);
			GlStateManager.scale(GlassVaseSurface.SCALE, GlassVaseSurface.SCALE, GlassVaseSurface.SCALE);

			// Draw the plant first because the glass writes to the depth buffer.
			renderPlant(VasePlantHelper.getPlant(stack), 0, 0, 0, 0);

			GlStateManager.pushMatrix();
			try {
				GlStateManager.translate(0, 0.5D, 0);
				Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
			} finally {
				GlStateManager.popMatrix();
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	public static void renderPlant(ItemStack plant, double x, double y, double z, float yaw) {
		if (plant == null || plant.stackSize <= 0) {
			return;
		}

		boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
		GlStateManager.pushMatrix();

		try {
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(yaw, 0, 1, 0);
			GlStateManager.disableTexture2D();

			// These coloured cuboids have no normals for Minecraft's item lighting.
			GlStateManager.disableLighting();
			stem(-0.012D, 0.14D, -0.012D, 0.012D, 0.40D, 0.012D);
			stem(-0.075D, 0.23D, -0.008D, -0.012D, 0.29D, 0.008D);
			stem(0.012D, 0.31D, -0.008D, 0.070D, 0.37D, 0.008D);

			if (lightingEnabled) {
				GlStateManager.enableLighting();
			}

			GlStateManager.enableTexture2D();
			GlStateManager.translate(0, 0.40D, 0);
			GlStateManager.scale(0.56F, 0.56F, 0.56F);
			Minecraft.getMinecraft().getRenderItem().renderItem(plant, ItemCameraTransforms.TransformType.FIXED);
		} finally {
			if (lightingEnabled) {
				GlStateManager.enableLighting();
			} else {
				GlStateManager.disableLighting();
			}

			GlStateManager.enableTexture2D();
			GlStateManager.popMatrix();
		}
	}

	private static void stem(double x0, double y0, double z0, double x1, double y1, double z1) {
		double[][] vertices = {
			{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0},
			{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}
		};
		int[][] faces = {
			{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 4, 7, 3},
			{1, 2, 6, 5}, {0, 1, 5, 4}, {3, 7, 6, 2}
		};

		VertexBuffer buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

		for (int[] face : faces) {
			for (int index : face) {
				double[] vertex = vertices[index];
				buffer.pos(vertex[0], vertex[1], vertex[2]).color(0.08F, 0.34F, 0.08F, 1.0F).endVertex();
			}
		}

		Tessellator.getInstance().draw();
	}
}
