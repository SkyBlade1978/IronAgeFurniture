package zone.moddev.mc.ironagefurniture.client.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL11;

import zone.moddev.mc.ironagefurniture.api.surface.MaterialShelfDisplay;
import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout.Placement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class GemShelfRenderer {
	private static final int[] DEFAULT_PALETTE = { 0x3B3047, 0x826F99, 0xC8B8DA };
	private static final double CORNER = Math.sqrt(0.5D);
	private static final double[] BRILLIANT_X = { 1, CORNER, 0, -CORNER, -1, -CORNER, 0, CORNER };
	private static final double[] BRILLIANT_Z = { 0, -CORNER, -1, -CORNER, 0, CORNER, 1, CORNER };
	private static final double[] STEP_X = { 1, 0.66D, -0.66D, -1, -1, -0.66D, 0.66D, 1 };
	private static final double[] STEP_Z = { -0.58D, -1, -1, -0.58D, 0.58D, 1, 1, 0.58D };
	private static final double[] FACET_LIGHT = { 0.44D, 0.90D, 0.63D, 0.83D, 0.31D, 0.51D, 0.18D, 0.58D };

	private final Map<ResourceLocation, int[]> palettes = new HashMap<>();

	void loadPalettes() {
		this.palettes.clear();
		if (!Loader.isModLoaded("basegems")) {
			return;
		}

		for (Item item : Item.REGISTRY) {
			ResourceLocation name = item.getRegistryName();
			if (name != null && "basegems".equals(name.getResourceDomain())
					&& MaterialShelfDisplay.find(new ItemStack(item)) == MaterialShelfDisplay.GEM) {
				this.palettes.put(name, paletteFromSprite(name));
			}
		}
	}

	private static int[] paletteFromSprite(ResourceLocation name) {
		ResourceLocation path = new ResourceLocation(name.getResourceDomain(), "textures/items/" + name.getResourcePath() + ".png");
		try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(path)) {
			BufferedImage sprite = TextureUtil.readBufferedImage(resource.getInputStream());
			int[] pixels = sprite.getRGB(0, 0, sprite.getWidth(), sprite.getHeight(), null, 0, sprite.getWidth());
			Integer[] opaque = IntStream.of(pixels).filter(pixel -> (pixel >>> 24) > 192).boxed()
				.sorted(Comparator.comparingInt(GemShelfRenderer::luminance)).toArray(Integer[]::new);
			if (opaque.length > 0) {
				return new int[] { opaque[opaque.length / 4], opaque[opaque.length * 3 / 5], opaque[opaque.length * 9 / 10] };
			}
		} catch (IOException missing) {
			// A resource pack may replace the item model and omit the original sprite.
		}
		return DEFAULT_PALETTE;
	}

	private static int luminance(int colour) {
		return 3 * (colour >> 16 & 255) + 6 * (colour >> 8 & 255) + (colour & 255);
	}

	void render(ItemStack stack, Placement placement, float light) {
		ResourceLocation name = stack.getItem().getRegistryName();
		int[] palette = this.palettes.getOrDefault(name, DEFAULT_PALETTE);
		String material = name.getResourcePath();
		material = material.substring(0, material.indexOf('_'));
		Cut cut = Cut.forMaterial(material);

		GlStateManager.pushMatrix();
		GlStateManager.translate(placement.x, placement.y, placement.z);
		GlStateManager.rotate(ShelfLayout.GEM_PLATTER_ANGLE, 1, 0, 0);
		GlStateManager.disableTexture2D();
		boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
		GlStateManager.disableLighting();
		try {
			renderFacets(cut, palette, light);
		} finally {
			if (lighting) {
				GlStateManager.enableLighting();
			}
			GlStateManager.enableTexture2D();
			GlStateManager.popMatrix();
		}
	}

	private static void renderFacets(Cut cut, int[] palette, float light) {
		Vec3d[] base = cut.base;
		Vec3d[] girdle = cut.girdle;
		Vec3d[] crown = cut.crown;

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		for (int side = 0; side < girdle.length; side++) {
			int next = (side + 1) % girdle.length;
			double glint = FACET_LIGHT[side];
			int skirt = blend(palette[0], palette[1], glint * 0.72D);
			int facet = blend(palette[1], palette[2], glint);
			int edge = blend(facet, 0xFFFFFF, glint > 0.80D ? 0.36D : 0.05D);
			triangle(buffer, base[side], base[next], girdle[next], skirt, light);
			triangle(buffer, base[side], girdle[next], girdle[side], skirt, light);
			triangle(buffer, girdle[side], girdle[next], crown[next], facet, light);
			triangle(buffer, girdle[side], crown[next], crown[side], edge, light);
			int top = blend(palette[2], 0xFFFFFF, cut.cabochon ? glint * 0.24D : 0.12D);
			triangle(buffer, cut.table, crown[side], crown[next], top, light);
		}
		tessellator.draw();
	}

	private static int blend(int from, int to, double weight) {
		int red = mix(from, to, weight, 16);
		int green = mix(from, to, weight, 8);
		int blue = mix(from, to, weight, 0);
		return red << 16 | green << 8 | blue;
	}

	private static int mix(int from, int to, double weight, int shift) {
		int start = from >> shift & 255;
		return (int)Math.round(start + ((to >> shift & 255) - start) * weight);
	}

	private static void triangle(VertexBuffer buffer, Vec3d first, Vec3d second, Vec3d third, int colour, float light) {
		int red = (int)((colour >> 16 & 255) * light);
		int green = (int)((colour >> 8 & 255) * light);
		int blue = (int)((colour & 255) * light);
		buffer.pos(first.xCoord, first.yCoord, first.zCoord).color(red, green, blue, 255).endVertex();
		buffer.pos(second.xCoord, second.yCoord, second.zCoord).color(red, green, blue, 255).endVertex();
		buffer.pos(third.xCoord, third.yCoord, third.zCoord).color(red, green, blue, 255).endVertex();
	}

	private enum Cut {
		BRILLIANT(BRILLIANT_X, BRILLIANT_Z, false),
		STEP(STEP_X, STEP_Z, false),
		CABOCHON(BRILLIANT_X, BRILLIANT_Z, true);

		private final Vec3d[] base;
		private final Vec3d[] girdle;
		private final Vec3d[] crown;
		private final Vec3d table;
		private final boolean cabochon;

		Cut(double[] x, double[] z, boolean cabochon) {
			this.base = ring(x, z, 0.62D, 0);
			this.girdle = ring(x, z, 1, cabochon ? 0.48D : 0.42D);
			this.crown = ring(x, z, cabochon ? 0.69D : 0.57D, cabochon ? 1.04D : 1.30D);
			this.table = new Vec3d(0, (cabochon ? 1.35D : 1.30D) / 16, 0);
			this.cabochon = cabochon;
		}

		private static Vec3d[] ring(double[] x, double[] z, double radius, double height) {
			Vec3d[] ring = new Vec3d[x.length];
			for (int index = 0; index < ring.length; index++) {
				ring[index] = new Vec3d(x[index] * 1.60D * radius / 16, height / 16, z[index] * 1.30D * radius / 16);
			}
			return ring;
		}

		private static Cut forMaterial(String material) {
			switch (material) {
			case "agate":
			case "amber":
			case "jade":
			case "malachite":
			case "moonstone":
			case "onyx":
			case "opal":
			case "turquoise":
				return CABOCHON;
			case "aquamarine":
			case "beryl":
			case "bluetopaz":
			case "goldenberyl":
			case "sapphire":
			case "topaz":
			case "violetsapphire":
				return STEP;
			default:
				return BRILLIANT;
			}
		}
	}
}
