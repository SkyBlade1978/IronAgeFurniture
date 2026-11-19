package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TileEntityShelvingUnitRenderer extends TileEntitySpecialRenderer<TileEntityShelvingUnit> {
	private static final ShelfContentKind[] MEDIA_KINDS = { ShelfContentKind.BOOKS, ShelfContentKind.RECORDS };
	private final ItemStack modelStack = new ItemStack(Items.BOOK);

	public static void register() {
		TileEntityShelvingUnitRenderer renderer = new TileEntityShelvingUnitRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShelvingUnit.class, renderer);
		MinecraftForge.EVENT_BUS.register(renderer);
	}

	@SubscribeEvent
	public void stitch(TextureStitchEvent.Pre event) {
		for (ShelfContentKind kind : MEDIA_KINDS) {
			for (int count = 1; count <= ShelvingUnit.getMediaCapacity(kind); count++) {
				for (ResourceLocation texture : loadMediaModel(kind, count).getTextures()) {
					event.getMap().registerSprite(texture);
				}
			}
		}
	}

	@SubscribeEvent
	public void bake(ModelBakeEvent event) {
		for (ShelfContentKind kind : MEDIA_KINDS) {
			for (int count = 1; count <= ShelvingUnit.getMediaCapacity(kind); count++) {
				IModel model = loadMediaModel(kind, count);
				IBakedModel baked = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
					texture -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString()));
				event.getModelRegistry().putObject(mediaModelKey(kind, count), baked);
			}
		}
	}

	private static IModel loadMediaModel(ShelfContentKind kind, int count) {
		ResourceLocation path = new ResourceLocation(Ironagefurniture.MODID, "block/" + mediaModelPath(kind, count));
		try {
			return ModelLoaderRegistry.getModel(path);
		} catch (Exception failure) {
			throw new IllegalStateException("Cannot load shelving arrangement " + path, failure);
		}
	}

	private static String mediaModelPath(ShelfContentKind kind, int count) {
		return "shelving_media/" + kind.getName() + "_" + count;
	}

	private static ModelResourceLocation mediaModelKey(ShelfContentKind kind, int count) {
		return new ModelResourceLocation(Ironagefurniture.MODID + ":" + mediaModelPath(kind, count), "inventory");
	}

	@Override
	public void renderTileEntityAt(TileEntityShelvingUnit tile, double x, double y, double z,
			float partialTicks, int destroyStage) {
		float shelfYaw = tile.getFacing().getOpposite().getHorizontalAngle();
		this.renderLevel(tile.getLevel(false), x, y, z, shelfYaw);
		this.renderLevel(tile.getLevel(true), x, y, z, shelfYaw);
	}

	private void renderLevel(ShelfLevel level, double x, double y, double z, float shelfYaw) {
		if (level.isEmpty()) {
			return;
		}

		SurfaceSetting setting = level.getSurfaceSetting();
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x + 0.5D + level.getSurfaceOffsetX(), y + level.getSurfaceHeight(),
				z + 0.5D + level.getSurfaceOffsetZ());
			if (level.getMediaKind() == ShelfContentKind.BOTTLES) {
				this.renderBottles(level, shelfYaw);
				return;
			}

			if (level.getMediaCount() > 0) {
				this.renderMedia(level, -shelfYaw);
				return;
			}

			double scale = level.getScale();
			GlStateManager.scale(scale, scale, scale);
			for (Slot slot : Slot.values()) {
				ItemStack stack = setting.getItem(slot);
				if (stack != null) {
					float yaw = setting.getFacing(slot).getOpposite().getHorizontalAngle();
					SurfaceDisplayRenderHelper.renderSurfaceItem(stack,
						new SurfaceRenderContext(0, 0, 0, setting.getX(slot) - 0.5D, 0.04D,
							setting.getZ(slot) - 0.5D, 0, yaw));
				}
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private void renderBottles(ShelfLevel level, float yaw) {
		for (int index = 0; index < level.getMediaCount(); index++) {
			ItemStack bottle = level.getMediaItem(index);
			Vec3d offset = level.getBottleOffset(index);
			double scale = level.getBottleScale(bottle);
			GlStateManager.pushMatrix();
			try {
				GlStateManager.translate(offset.xCoord, 0, offset.zCoord);
				GlStateManager.scale(scale, scale, scale);
				SurfaceDisplayRenderHelper.renderSurfaceItem(bottle, new SurfaceRenderContext(0, 0, 0, 0, 0.04D, 0, 0, yaw));
			} finally {
				GlStateManager.popMatrix();
			}
		}
	}

	private void renderMedia(ShelfLevel level, float yaw) {
		Minecraft client = Minecraft.getMinecraft();
		IBakedModel model = client.getRenderItem().getItemModelMesher().getModelManager()
			.getModel(mediaModelKey(level.getMediaKind(), level.getMediaCount()));
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(ShelvingUnit.MEDIA_SCALE, ShelvingUnit.MEDIA_SCALE, ShelvingUnit.MEDIA_SCALE);
		GlStateManager.translate(0.0D, 0.5D, 0.0D);
		client.getRenderItem().renderItem(this.modelStack, model);
	}
}
