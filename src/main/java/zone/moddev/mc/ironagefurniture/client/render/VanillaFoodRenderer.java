package zone.moddev.mc.ironagefurniture.client.render;

import java.util.HashSet;
import java.util.Set;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods.Food;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Baked food models used only on furniture surfaces. */
@SideOnly(Side.CLIENT)
public final class VanillaFoodRenderer implements SurfaceRenderRegistry.Renderer {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Ironagefurniture.MODID, "blocks/surface_foods");

	public static void register() {
		VanillaFoodRenderer renderer = new VanillaFoodRenderer();
		Set<Item> registered = new HashSet<Item>();
		for (Food food : VanillaSurfaceFoods.all()) {
			if (registered.add(food.item)) SurfaceRenderRegistry.register(food.item, renderer);
		}
		MinecraftForge.EVENT_BUS.register(renderer);
	}

	@SubscribeEvent
	public void stitch(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(TEXTURE);
	}

	@SubscribeEvent
	public void bake(ModelBakeEvent event) {
		for (Food food : VanillaSurfaceFoods.all()) {
			try {
				IModel model = ModelLoaderRegistry.getModel(
					new ResourceLocation(Ironagefurniture.MODID, "block/surface_food/" + food.model));
				IBakedModel baked = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
					texture -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString()));
				event.getModelRegistry().putObject(modelKey(food), baked);
			} catch (Exception failure) {
				throw new IllegalStateException("Cannot bake surface food " + food.model, failure);
			}
		}
	}

	@Override
	public void render(ItemStack stack, SurfaceRenderContext context) {
		Food food = VanillaSurfaceFoods.find(stack);
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
			.getModelManager().getModel(modelKey(food));
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(context.x + context.itemX, context.y + context.blockSurfaceY + 0.5D,
				context.z + context.itemZ);
			GlStateManager.rotate(context.yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static ModelResourceLocation modelKey(Food food) {
		return new ModelResourceLocation(Ironagefurniture.MODID + ":surface_food/" + food.model, "inventory");
	}
}
