package zone.moddev.mc.ironagefurniture.client.render;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.skysfurniturecore.api.surface.VanillaSurfaceFoods;
import zone.moddev.mc.skysfurniturecore.api.vessel.BarrelBulkStorage;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TileEntityBarrelRenderer extends TileEntitySpecialRenderer<TileEntityBarrel> {
	private static final double INTERIOR_FLOOR = 1.23D / 16;
	private static final double INTERIOR_WALL = 5.8D / 16;
	private static final double INTERIOR_CORNER = 7.95D / 16;
	private static final double INTERIOR_RIM = 15.8D / 16;
	private static final int[] LAYER_SIZES = { 9, 9, 8, 8, 7, 5 };
	private static final int MAX_PILE_MODELS = Arrays.stream(LAYER_SIZES).sum();
	private static final SurfaceRenderContext PILE_ORIGIN = new SurfaceRenderContext(0, 0, 0, 0, 0, 0, 0, 0);
	// Offsets in model pixels and yaw. Longer pieces near the walls lie along the staves.
	private static final double[][] PILE_POSITIONS = {
		{ 0.25, -0.15, 31 },
		{ 5.8, 0.3, 8 }, { -5.8, -0.5, -174 },
		{ 0.45, 5.8, 88 }, { -0.35, -5.8, -94 },
		{ 3.55, 3.55, -44 }, { -3.55, -3.55, 137 },
		{ 3.5, -3.5, 51 }, { -3.5, 3.5, -129 }
	};

	@Override
	public void renderTileEntityAt(TileEntityBarrel tile, double x, double y, double z, float ticks, int destroyStage) {
		if (tile.getBarrelLabel().getSign() != null) {
			IBlockState state = tile.getWorld().getBlockState(tile.getPos());
			if (state.getBlock() instanceof StorageBarrel) {
				EnumFacing face = tile.getLabelFacing();
				Vec3d anchor = ((StorageBarrel)state.getBlock()).getLabelAnchor(state, face);
				FurnitureLabelRenderer.render(tile.getBarrelLabel(), x + anchor.xCoord, y + anchor.yCoord, z + anchor.zCoord,
					-face.getOpposite().getHorizontalAngle(), StorageBarrel.LABEL_SCALE);
			}
		}

		BarrelBulkStorage storage = tile.getBulkStorage();
		if (storage.getCount() == 0 || !tile.isBulkBarrel()) {
			return;
		}

		ItemStack stack = storage.getStack();
		VanillaSurfaceFoods.Food food = VanillaSurfaceFoods.find(stack);
		if (food != null) {
			renderPile(stack, storage, food.descriptor.getBounds(EnumFacing.NORTH), null, x, y, z);
			return;
		}

		VanillaShelfDisplay display = VanillaShelfDisplay.find(stack);
		if (display == VanillaShelfDisplay.RED_MUSHROOM || display == VanillaShelfDisplay.BROWN_MUSHROOM
				|| display == VanillaShelfDisplay.NETHER_WART) {
			Minecraft client = Minecraft.getMinecraft();
			IBakedModel model = client.getRenderItem().getItemModelMesher().getModelManager()
				.getModel(TileEntityShelvingUnitRenderer.modelKey(display.getModelPath()));
			renderPile(stack, storage, display.getShelfModel().getBounds(EnumFacing.NORTH), model, x, y, z);
			return;
		}

		renderLooseContents(stack, (double)storage.getCount() / storage.getCapacity(), x, y, z);
	}

	private static void renderLooseContents(ItemStack stack, double fraction, double x, double y, double z) {
		Minecraft client = Minecraft.getMinecraft();
		TextureAtlasSprite texture = client.getTextureMapBlocks().getAtlasSprite(fillTexture(stack.getItem()));
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, y + (2 + 12 * fraction) / 16, z);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
			// The wooden walls leave three connected strips across the barrel opening.
			drawFill(buffer, texture, 4, 2.14D, 12, 4);
			drawFill(buffer, texture, 2.14D, 4, 13.86D, 12);
			drawFill(buffer, texture, 4, 12, 12, 13.86D);
			tessellator.draw();
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static void renderPile(ItemStack stack, BarrelBulkStorage storage, AxisAlignedBB bounds, IBakedModel model,
			double x, double y, double z) {
		double halfWidth = Math.max(-bounds.minX, bounds.maxX);
		double halfDepth = Math.max(-bounds.minZ, bounds.maxZ);
		double scale = Math.min(0.78D, (3.35D / 16) / Math.hypot(halfWidth, halfDepth));
		double modelHeight = bounds.maxY - bounds.minY;
		double modelCenter = (bounds.minY + bounds.maxY) * 0.5D;
		double scaleY = Math.max(scale, Math.min(1.0D, (2.25D / 16) / modelHeight));
		int firstLayer = LAYER_SIZES[0];
		int visible = Math.min(storage.getCount(),
			firstLayer + (MAX_PILE_MODELS - firstLayer) * storage.getCount() / storage.getCapacity());

		for (int layer = 0, index = 0; index < visible; layer++) {
			float layerTurn = layer * 90.0F;
			double turn = Math.toRadians(layerTurn);
			double cosTurn = Math.cos(turn);
			double sinTurn = Math.sin(turn);
			double layerBottom = INTERIOR_FLOOR + layer * (2.23D / 16);

			for (int slot = 0; slot < LAYER_SIZES[layer] && index < visible; slot++, index++) {
				double[] position = PILE_POSITIONS[slot];
				double offsetX = (position[0] * cosTurn + position[1] * sinTurn) / 16;
				double offsetZ = (-position[0] * sinTurn + position[1] * cosTurn) / 16;
				double size = 0.91D + 0.03D * (index % 5);
				float yaw = (float)position[2] + layerTurn + (index * 17) % 23 - 11;
				float tilt = (index * 11) % 33 - 16;
				double pieceBottom = Math.max(INTERIOR_FLOOR, layerBottom + (index % 5 - 2) * 0.012D);
				double tiltRadians = Math.toRadians(tilt);
				double sinTilt = Math.abs(Math.sin(tiltRadians));
				double cosTilt = Math.cos(tiltRadians);
				double halfHeight = modelHeight * scaleY * size * 0.5D;
				double halfLength = halfDepth * scale * size;
				double depth = halfLength * cosTilt + halfHeight * sinTilt;
				double lift = halfHeight * cosTilt + halfLength * sinTilt;
				double fit = fitToOpening(offsetX, offsetZ, halfWidth * scale * size, depth, yaw);
				double centerY = Math.min(pieceBottom + lift, INTERIOR_RIM - lift);

				renderPiece(stack, model, x + 0.5D + offsetX * fit, y + centerY, z + 0.5D + offsetZ * fit,
					scale * size, scaleY * size, modelCenter, yaw, tilt);
			}
		}
	}

	private static double fitToOpening(double x, double z, double width, double depth, float yaw) {
		double angle = Math.toRadians(yaw);
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double xMargin = INTERIOR_WALL - Math.abs(cos) * width - Math.abs(sin) * depth;
		double zMargin = INTERIOR_WALL - Math.abs(sin) * width - Math.abs(cos) * depth;

		// The diagonal limits keep the whole rotated model clear of the corner posts.
		double sumMargin = INTERIOR_CORNER - Math.abs(cos - sin) * width - Math.abs(sin + cos) * depth;
		double differenceMargin = INTERIOR_CORNER - Math.abs(cos + sin) * width - Math.abs(sin - cos) * depth;
		return Math.min(Math.min(fitAxis(x, xMargin), fitAxis(z, zMargin)),
			Math.min(fitAxis(x + z, sumMargin), fitAxis(x - z, differenceMargin)));
	}

	private static double fitAxis(double offset, double margin) {
		double distance = Math.abs(offset);
		return distance <= margin ? 1.0D : margin / distance;
	}

	private static void renderPiece(ItemStack stack, IBakedModel model, double x, double y, double z,
			double scale, double scaleY, double center, float yaw, float tilt) {
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(tilt, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(scale, scaleY, scale);
			GlStateManager.translate(0, -center, 0);
			if (model == null) {
				SurfaceDisplayRenderHelper.renderSurfaceItem(stack, PILE_ORIGIN);
			} else {
				TileEntityShelvingUnitRenderer.renderBoardModel(stack, model, 0, 0, 0, 1, 0);
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static void drawFill(VertexBuffer buffer, TextureAtlasSprite sprite, double x1, double z1, double x2, double z2) {
		vertex(buffer, sprite, x1, z1);
		vertex(buffer, sprite, x1, z2);
		vertex(buffer, sprite, x2, z2);
		vertex(buffer, sprite, x2, z1);
	}

	private static void vertex(VertexBuffer buffer, TextureAtlasSprite sprite, double x, double z) {
		buffer.pos(x / 16, 0, z / 16).tex(sprite.getInterpolatedU(x), sprite.getInterpolatedV(z))
			.color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
	}

	private static String fillTexture(Item item) {
		if (item == Items.SUGAR) return "minecraft:blocks/snow";
		if (item == Items.DYE) return "minecraft:blocks/hardened_clay_stained_brown";
		return "minecraft:blocks/hay_block_top";
	}
}
