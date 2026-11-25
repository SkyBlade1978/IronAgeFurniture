package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderContext;


import java.util.IdentityHashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.apache.commons.lang3.tuple.Pair;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderer;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Slot;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Generic fallback with an optional specialised renderer per item. */
public final class SurfaceDisplayRenderHelper {
	private static final float BLOCK_ITEM_SCALE = 0.55F;
	private static final float FLAT_ITEM_SCALE = 0.5F;
	private static final double FLAT_ITEM_HEIGHT = 0.04D;
	private static final Map<IBakedModel, AxisAlignedBB> SHELF_TOOL_BOUNDS = new IdentityHashMap<>();
	private static final Map<IBakedModel, AxisAlignedBB> SHELF_OBJECT_BOUNDS = new IdentityHashMap<>();
	private static final Map<IBakedModel, AxisAlignedBB> RACK_LONG_TOOL_BOUNDS = new IdentityHashMap<>();
	private static final Map<IBakedModel, AxisAlignedBB> RACK_COMPACT_TOOL_BOUNDS = new IdentityHashMap<>();

	private SurfaceDisplayRenderHelper() { }

	public static void renderPottedPlant(ItemStack stack, double x, double y, double z,
			double itemX, double itemZ, double surfaceY) {
		if (stack == null || stack.stackSize <= 0) return;
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x + itemX, y + surfaceY + 0.46D, z + itemZ);
			GlStateManager.scale(0.52F, 0.52F, 0.52F);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		} finally { GlStateManager.popMatrix(); }
	}

	public static void renderSurfaceItem(ItemStack stack, SurfaceRenderContext context) {
		if (stack == null || stack.stackSize <= 0) return;
		SurfaceRenderRegistry.Renderer renderer = rendererFor(stack);
		if (renderer != null) {
			renderer.render(stack, context);
			return;
		}
		SurfaceRenderer.renderDefault(stack, context);
	}

	/** @deprecated Pass the Core rendering context. */
	@Deprecated
	public static void renderSurfaceItem(ItemStack stack,
			zone.moddev.mc.ironagefurniture.client.render.SurfaceRenderContext context) {
		renderSurfaceItem(stack, context.core());
	}

	private static SurfaceRenderRegistry.Renderer rendererFor(ItemStack stack) {
		SurfaceRenderRegistry.Renderer registered = SurfaceRenderRegistry.get(stack);
		if (registered != null) return registered;
		return SurfaceSetting.classify(stack) == SurfaceSetting.Category.BOTTLE ? SurfaceBottleRenderer.INSTANCE : null;
	}

	public static void renderGlassVasePlant(ItemStack stack, double x, double y, double z,
			double itemX, double itemZ, double surfaceY, float yaw) {
		GlassVaseRenderer.renderPlant(stack, x + itemX, y + surfaceY, z + itemZ, yaw);
	}

	static void clearShelfModelBounds() {
		SHELF_TOOL_BOUNDS.clear();
		SHELF_OBJECT_BOUNDS.clear();
		RACK_LONG_TOOL_BOUNDS.clear();
		RACK_COMPACT_TOOL_BOUNDS.clear();
	}

	static void renderShelfTool(ItemStack stack, AxisAlignedBB target) {
		Minecraft client = Minecraft.getMinecraft();
		RenderItem renderer = client.getRenderItem();
		IBakedModel model = renderer.getItemModelWithOverrides(stack, null, null);
		AxisAlignedBB bounds = SHELF_TOOL_BOUNDS.computeIfAbsent(model, SurfaceDisplayRenderHelper::shelfToolBounds);
		double scale = Math.min((target.maxX - target.minX) / (bounds.maxX - bounds.minX),
			(target.maxZ - target.minZ) / (bounds.maxZ - bounds.minZ));
		double thickness = Math.min(scale, (target.maxY - target.minY) / (bounds.maxY - bounds.minY));
		double x = (target.minX + target.maxX - (bounds.minX + bounds.maxX) * scale) / 2;
		double z = (target.minZ + target.maxZ - (bounds.minZ + bounds.maxZ) * scale) / 2;

		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, target.minY - bounds.minY * thickness, z);
			GlStateManager.scale(scale, thickness, scale);
			GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-45.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			renderBakedItem(renderer, stack, model);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	static void renderRackTool(ItemStack stack, AxisAlignedBB target) {
		Minecraft client = Minecraft.getMinecraft();
		RenderItem renderer = client.getRenderItem();
		IBakedModel model = renderer.getItemModelWithOverrides(stack, null, null);
		boolean longTool = target.maxY - target.minY > 0.5D;
		float toolAngle = longTool ? 45.0F : 0.0F;
		Map<IBakedModel, AxisAlignedBB> cache = longTool ? RACK_LONG_TOOL_BOUNDS : RACK_COMPACT_TOOL_BOUNDS;
		AxisAlignedBB bounds = cache.computeIfAbsent(model, item -> rackToolBounds(item, toolAngle));
		float angle = toolAngle;
		if (stack.getItem() instanceof ItemSword) {
			// Half-turn the model and its measured bounds so the sword hangs by its hilt.
			angle += 180.0F;
			bounds = new AxisAlignedBB(-bounds.maxX, -bounds.maxY, bounds.minZ, -bounds.minX, -bounds.minY, bounds.maxZ);
		}

		double scale = Math.min(fitAxis(target.minX, target.maxX, bounds.minX, bounds.maxX),
			fitAxis(target.minY, target.maxY, bounds.minY, bounds.maxY));
		double depth = Math.min(scale, fitAxis(target.minZ, target.maxZ, bounds.minZ, bounds.maxZ));
		double x = (target.minX + target.maxX - (bounds.minX + bounds.maxX) * scale) / 2;
		double z = (target.minZ + target.maxZ - (bounds.minZ + bounds.maxZ) * depth) / 2;

		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, target.maxY - bounds.maxY * scale, z);
			GlStateManager.scale(scale, scale, depth);
			GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate(angle, 0, 0, 1);
			GlStateManager.color(1, 1, 1, 1);
			renderBakedItem(renderer, stack, model);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static AxisAlignedBB rackToolBounds(IBakedModel model, float angle) {
		Matrix4f rotation = new Matrix4f();
		rotation.rotY((float)Math.PI);
		Matrix4f upright = new Matrix4f();
		upright.rotZ((float)Math.toRadians(angle));
		rotation.mul(upright);
		return modelBounds(model, rotation);
	}

	static void renderBakedItem(RenderItem renderer, ItemStack stack, IBakedModel model) {
		SurfaceRenderer.renderBakedItem(renderer, stack, model);
	}

	static void renderShelfObject(ItemStack stack, AxisAlignedBB target, boolean quarterTurn) {
		Minecraft client = Minecraft.getMinecraft();
		RenderItem renderer = client.getRenderItem();
		IBakedModel model = renderer.getItemModelWithOverrides(stack, null, null);
		AxisAlignedBB bounds = SHELF_OBJECT_BOUNDS.computeIfAbsent(model,
			itemModel -> modelBounds(itemModel, null).offset(-0.5D, -0.5D, -0.5D));
		if (quarterTurn) {
			bounds = new AxisAlignedBB(bounds.minZ, bounds.minY, -bounds.maxX, bounds.maxZ, bounds.maxY, -bounds.minX);
		}

		double scale = Math.min(fitAxis(target.minX, target.maxX, bounds.minX, bounds.maxX),
			Math.min(fitAxis(target.minY, target.maxY, bounds.minY, bounds.maxY),
				fitAxis(target.minZ, target.maxZ, bounds.minZ, bounds.maxZ)));
		double x = (target.minX + target.maxX - scale * (bounds.minX + bounds.maxX)) / 2;
		double z = (target.minZ + target.maxZ - scale * (bounds.minZ + bounds.maxZ)) / 2;

		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, target.minY - scale * bounds.minY, z);
			GlStateManager.scale(scale, scale, scale);
			if (quarterTurn) {
				GlStateManager.rotate(90, 0, 1, 0);
			}
			renderBakedItem(renderer, stack, model);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	static double fitAxis(double targetMin, double targetMax, double modelMin, double modelMax) {
		return (targetMax - targetMin) / Math.max(modelMax - modelMin, 0.001D);
	}

	private static AxisAlignedBB shelfToolBounds(IBakedModel model) {
		Matrix4f rotation = new Matrix4f();
		rotation.rotX((float)(-Math.PI / 2));
		Matrix4f acrossShelf = new Matrix4f();
		acrossShelf.rotZ((float)(-Math.PI / 4));
		rotation.mul(acrossShelf);
		return modelBounds(model, rotation);
	}

	private static Pair<? extends IBakedModel, Matrix4f> fixedPerspective(IBakedModel model) {
		return SurfaceRenderer.fixedPerspective(model);
	}

	/** Bounds relative to the reserved cell above the furniture. */
	public static AxisAlignedBB getItemBounds(SurfaceSetting setting, Slot slot) {
		ItemStack stack = setting.getItem(slot);
		if (stack == null || rendererFor(stack) != null) {
			return setting.getItemBounds(slot);
		}
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);
		Pair<? extends IBakedModel, Matrix4f> perspective = fixedPerspective(model);
		float yaw = setting.getFacing(slot).getOpposite().getHorizontalAngle();
		return itemBounds(modelBounds(perspective.getLeft(), null), perspective.getRight(), yaw,
			stack.getItem() instanceof ItemBlock)
			.offset(setting.getX(slot), 0.0D, setting.getZ(slot));
	}

	private static AxisAlignedBB modelBounds(IBakedModel model, Matrix4f rotation) {
		return modelBounds(model, null, rotation);
	}

	static AxisAlignedBB modelBounds(IBakedModel model, IBlockState state, Matrix4f rotation) {
		AxisAlignedBB bounds = null;
		Point3f point = new Point3f();
		for (int face = -1; face < 6; face++) {
			for (BakedQuad quad : model.getQuads(state, face < 0 ? null : EnumFacing.getFront(face), 0L)) {
				// The side quads follow the sprite outline. The front and back cover the transparent corners too.
				if (rotation != null && quad.getFace().getAxis() == EnumFacing.Axis.Z) {
					continue;
				}

				int positionOffset = 0;
				for (int element = 0; element < quad.getFormat().getElementCount(); element++) {
					if (quad.getFormat().getElement(element).isPositionElement()) {
						positionOffset = quad.getFormat().getOffset(element) / 4;
						break;
					}
				}
				int[] vertices = quad.getVertexData();
				int stride = quad.getFormat().getIntegerSize();
				for (int vertex = 0; vertex < 4; vertex++) {
					int start = vertex * stride + positionOffset;
					double x = Float.intBitsToFloat(vertices[start]);
					double y = Float.intBitsToFloat(vertices[start + 1]);
					double z = Float.intBitsToFloat(vertices[start + 2]);
					if (rotation != null) {
						point.set((float)(x - 0.5D), (float)(y - 0.5D), (float)(z - 0.5D));
						rotation.transform(point);
						x = point.x;
						y = point.y;
						z = point.z;
					}
					AxisAlignedBB vertexBounds = new AxisAlignedBB(x, y, z, x, y, z);
					bounds = bounds == null ? vertexBounds : bounds.union(vertexBounds);
				}
			}
		}
		// Built-in item renderers, such as chests, do not expose baked quads.
		return bounds == null ? new AxisAlignedBB(0, 0, 0, 1, 1, 1) : bounds;
	}

	private static AxisAlignedBB itemBounds(AxisAlignedBB modelBounds, Matrix4f fixedTransform,
			float yaw, boolean blockItem) {
		Matrix4f rotation = new Matrix4f();
		rotation.rotY((float)Math.toRadians(yaw));
		if (!blockItem) {
			Matrix4f flat = new Matrix4f();
			flat.rotX((float)(Math.PI / 2.0D));
			rotation.mul(flat);
		}
		if (fixedTransform != null) rotation.mul(fixedTransform);
		double scale = blockItem ? BLOCK_ITEM_SCALE : FLAT_ITEM_SCALE;
		double lift = blockItem ? blockItemLift(fixedTransform) : FLAT_ITEM_HEIGHT;
		AxisAlignedBB bounds = null;
		for (int corner = 0; corner < 8; corner++) {
			Point3f point = new Point3f(
				(float)(((corner & 1) == 0 ? modelBounds.minX : modelBounds.maxX) - 0.5D),
				(float)(((corner & 2) == 0 ? modelBounds.minY : modelBounds.maxY) - 0.5D),
				(float)(((corner & 4) == 0 ? modelBounds.minZ : modelBounds.maxZ) - 0.5D));
			rotation.transform(point);
			double x = point.x * scale;
			double y = point.y * scale + lift;
			double z = point.z * scale;
			AxisAlignedBB transformed = new AxisAlignedBB(x, y, z, x, y, z);
			bounds = bounds == null ? transformed : bounds.union(transformed);
		}
		return bounds;
	}

	static double blockItemLift(Matrix4f transform) {
		return SurfaceRenderer.blockItemLift(transform);
	}
}
