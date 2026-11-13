package zone.moddev.mc.ironagefurniture.client.render;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.apache.commons.lang3.tuple.Pair;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;

/** Generic fallback with an optional specialised renderer per item. */
public final class SurfaceDisplayRenderHelper {
	private static final float BLOCK_ITEM_SCALE = 0.55F;
	private static final float FLAT_ITEM_SCALE = 0.5F;
	private static final double FLAT_ITEM_HEIGHT = 0.04D;

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
		SurfaceRenderRegistry.Renderer renderer = SurfaceRenderRegistry.get(stack);
		if (renderer != null) {
			renderer.render(stack, context);
			return;
		}
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		boolean blockItem = stack.getItem() instanceof ItemBlock;
		double itemY = blockItem
			? context.blockSurfaceY + blockItemLift(fixedPerspective(renderItem.getItemModelWithOverrides(stack, null, null)).getRight())
			: context.itemY;
		GlStateManager.pushMatrix();
		GlStateManager.translate(context.x + context.itemX, context.y + itemY, context.z + context.itemZ);
		GlStateManager.rotate(context.yaw, 0.0F, 1.0F, 0.0F);
		if (blockItem) {
			GlStateManager.scale(BLOCK_ITEM_SCALE, BLOCK_ITEM_SCALE, BLOCK_ITEM_SCALE);
		} else {
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(FLAT_ITEM_SCALE, FLAT_ITEM_SCALE, FLAT_ITEM_SCALE);
		}
		renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		GlStateManager.popMatrix();
	}

	private static Pair<? extends IBakedModel, Matrix4f> fixedPerspective(IBakedModel model) {
		ItemCameraTransforms.TransformType fixed = ItemCameraTransforms.TransformType.FIXED;
		if (model instanceof IPerspectiveAwareModel) {
			return ((IPerspectiveAwareModel)model).handlePerspective(fixed);
		}
		return Pair.of(model, new TRSRTransformation(model.getItemCameraTransforms().getTransform(fixed)).getMatrix());
	}

	/** Bounds relative to the reserved cell above the furniture. */
	public static AxisAlignedBB getItemBounds(SurfaceSetting setting, Slot slot) {
		ItemStack stack = setting.getItem(slot);
		if (stack == null || SurfaceRenderRegistry.get(stack) != null) {
			return setting.getItemBounds(slot);
		}
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);
		Pair<? extends IBakedModel, Matrix4f> perspective = fixedPerspective(model);
		float yaw = setting.getFacing(slot).getOpposite().getHorizontalAngle();
		return itemBounds(modelBounds(perspective.getLeft()), perspective.getRight(), yaw,
			stack.getItem() instanceof ItemBlock)
			.offset(setting.getX(slot), 0.0D, setting.getZ(slot));
	}

	private static AxisAlignedBB modelBounds(IBakedModel model) {
		AxisAlignedBB bounds = null;
		for (int face = -1; face < 6; face++) {
			for (BakedQuad quad : model.getQuads(null, face < 0 ? null : EnumFacing.getFront(face), 0L)) {
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
					AxisAlignedBB point = new AxisAlignedBB(x, y, z, x, y, z);
					bounds = bounds == null ? point : bounds.union(point);
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

	static double blockItemLift(Matrix4f fixedTransform) {
		if (fixedTransform == null) return BLOCK_ITEM_SCALE * 0.5D;

		// RenderItem centres the unit block before applying its item perspective.
		// Use the transformed vertical extent to put its base on the surface.
		double halfHeight = (Math.abs(fixedTransform.m10) + Math.abs(fixedTransform.m11)
			+ Math.abs(fixedTransform.m12)) * 0.5D;
		return BLOCK_ITEM_SCALE * (halfHeight - fixedTransform.m13);
	}
}
