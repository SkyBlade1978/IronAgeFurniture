package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderContext;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.surface.FurnitureSurfaceDisplay;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class FurnitureSurfaceDisplayRenderer {
	private FurnitureSurfaceDisplayRenderer() { }

	static void register(ModelResourceLocation tallow) {
		for (Item item : Ironagefurniture.ItemRegistry.values()) {
			FurnitureSurfaceDisplay display = FurnitureSurfaceDisplay.find(new ItemStack(item));
			if (display != null) {
				SurfaceRenderRegistry.register(item, (stack, context) -> render(stack, display, tallow, context));
			}
		}
	}

	private static void render(ItemStack stack, FurnitureSurfaceDisplay display, ModelResourceLocation tallow,
			SurfaceRenderContext context) {
		Minecraft client = Minecraft.getMinecraft();
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(context.x + context.itemX, context.y + context.blockSurfaceY, context.z + context.itemZ);
			GlStateManager.rotate(context.yaw, 0, 1, 0);

			if (display == FurnitureSurfaceDisplay.CANDLE) {
				IBlockState state = ((ItemBlock)stack.getItem()).getBlock().getDefaultState();
				BlockRendererDispatcher blocks = client.getBlockRendererDispatcher();
				GlStateManager.translate(-0.5D, 0, -0.5D);
				blocks.getBlockModelRenderer().renderModelBrightnessColor(state, blocks.getModelForState(state),
					1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				RenderItem renderer = client.getRenderItem();
				IBakedModel model = display == FurnitureSurfaceDisplay.TALLOW
					? renderer.getItemModelMesher().getModelManager().getModel(tallow)
					: renderer.getItemModelWithOverrides(stack, null, null);
				GlStateManager.translate(0, 0.5D, 0);
				SurfaceDisplayRenderHelper.renderBakedItem(renderer, stack, model);
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}
}
