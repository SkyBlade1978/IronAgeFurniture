package zone.moddev.mc.ironagefurniture.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;

/** Fine dry goods have an opaque fill with small kernels touching its surface. */
final class GranularBarrelRenderer {
	static final double EDGE = 2.14D / 16;
	static final double INNER = 4.0D / 16;
	static final List<AxisAlignedBB> KERNELS = kernels();

	private GranularBarrelRenderer() { }

	static double fillHeight(double fraction) {
		return (2 + 12 * Math.max(0, Math.min(1, fraction))) / 16;
	}

	static boolean insideOpening(AxisAlignedBB box) {
		return box.minX >= EDGE && box.maxX <= 1 - EDGE && box.minZ >= EDGE && box.maxZ <= 1 - EDGE
			&& (box.minX >= INNER && box.maxX <= 1 - INNER || box.minZ >= INNER && box.maxZ <= 1 - INNER);
	}

	private static List<AxisAlignedBB> kernels() {
		List<AxisAlignedBB> result = new ArrayList<>();
		for (int row = 0; row < 17; row++) {
			for (int column = 0; column < 17; column++) {
				int index = row * 17 + column;
				double x = (2.35D + column * 0.69D + (row % 2) * 0.28D + (index % 3) * 0.025D) / 16;
				double z = (2.35D + row * 0.69D + (index % 5) * 0.018D) / 16;
				double width = (index % 2 == 0 ? 0.49D : 0.74D) / 16;
				double depth = (index % 2 == 0 ? 0.74D : 0.49D) / 16;
				AxisAlignedBB kernel = new AxisAlignedBB(x - width / 2, 0, z - depth / 2,
					x + width / 2, (0.16D + (index % 4) * 0.035D) / 16, z + depth / 2);
				if (insideOpening(kernel)) {
					result.add(kernel);
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	static void render(double fraction, int colour, double x, double y, double z) {
		Minecraft client = Minecraft.getMinecraft();
		TextureAtlasSprite texture = client.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/wool_colored_white");
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, y + fillHeight(fraction), z);
			GlStateManager.color(1, 1, 1, 1);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
			int base = shade(colour, 0.82D);
			top(buffer, texture, INNER, EDGE, 1 - INNER, INNER, 0, base);
			top(buffer, texture, EDGE, INNER, 1 - EDGE, 1 - INNER, 0, base);
			top(buffer, texture, INNER, 1 - INNER, 1 - INNER, 1 - EDGE, 0, base);

			int index = 0;
			for (AxisAlignedBB kernel : KERNELS) {
				int tint = shade(colour, 0.90D + (index++ % 7) * 0.035D);
				top(buffer, texture, kernel.minX, kernel.minZ, kernel.maxX, kernel.maxZ, kernel.maxY, tint);
				walls(buffer, texture, kernel, shade(tint, 0.75D));
			}
			tessellator.draw();
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static void top(VertexBuffer buffer, TextureAtlasSprite texture, double x1, double z1,
			double x2, double z2, double y, int colour) {
		vertex(buffer, texture, x1, y, z1, colour, 0, 1, 0);
		vertex(buffer, texture, x1, y, z2, colour, 0, 1, 0);
		vertex(buffer, texture, x2, y, z2, colour, 0, 1, 0);
		vertex(buffer, texture, x2, y, z1, colour, 0, 1, 0);
	}

	private static void walls(VertexBuffer buffer, TextureAtlasSprite texture, AxisAlignedBB b, int colour) {
		for (int face = 0; face < 4; face++) {
			double x1 = face == 1 || face == 2 ? b.maxX : b.minX;
			double z1 = face >= 2 ? b.maxZ : b.minZ;
			double x2 = face < 2 ? b.maxX : b.minX;
			double z2 = face == 0 || face == 3 ? b.minZ : b.maxZ;
			int nx = face == 1 ? 1 : face == 3 ? -1 : 0;
			int nz = face == 0 ? -1 : face == 2 ? 1 : 0;
			vertex(buffer, texture, x1, 0, z1, colour, nx, 0, nz);
			vertex(buffer, texture, x1, b.maxY, z1, colour, nx, 0, nz);
			vertex(buffer, texture, x2, b.maxY, z2, colour, nx, 0, nz);
			vertex(buffer, texture, x2, 0, z2, colour, nx, 0, nz);
		}
	}

	private static void vertex(VertexBuffer buffer, TextureAtlasSprite texture, double x, double y, double z,
			int colour, int nx, int ny, int nz) {
		buffer.pos(x, y, z).tex(texture.getInterpolatedU(x * 16), texture.getInterpolatedV(z * 16))
			.color((colour >> 16) & 255, (colour >> 8) & 255, colour & 255, 255).normal(nx, ny, nz).endVertex();
	}

	private static int shade(int colour, double scale) {
		return (Math.min(255, (int)(((colour >> 16) & 255) * scale)) << 16)
			| (Math.min(255, (int)(((colour >> 8) & 255) * scale)) << 8)
			| Math.min(255, (int)((colour & 255) * scale));
	}
}
