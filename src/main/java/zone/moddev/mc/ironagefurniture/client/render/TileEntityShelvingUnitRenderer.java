package zone.moddev.mc.ironagefurniture.client.render;

import java.util.EnumMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.surface.MaterialShelfDisplay;
import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TileEntityShelvingUnitRenderer extends TileEntitySpecialRenderer<TileEntityShelvingUnit> {
	private static final ShelfContentKind[] MEDIA_KINDS = { ShelfContentKind.BOOKS, ShelfContentKind.RECORDS };
	private static final String TRAY_MODEL = "shelving_media/produce_tray";
	private static final String GEM_PLATTER_MODEL = "shelving_media/gems/platter";
	private static final String TALLOW_MODEL = "shelving_media/furniture/tallow";
	private static final String ARROW_MODEL = "shelving_media/arrow";
	private static final String SPECTRAL_ARROW_MODEL = "shelving_media/spectral_arrow";
	private static final String FLINT_AND_STEEL_MODEL = "shelving_media/flint_and_steel";
	private static final ModelResourceLocation TRAY_KEY = modelKey(TRAY_MODEL);
	private static final ModelResourceLocation GEM_PLATTER_KEY = modelKey(GEM_PLATTER_MODEL);
	private static final ModelResourceLocation TALLOW_KEY = modelKey(TALLOW_MODEL);
	private static final ModelResourceLocation ARROW_KEY = modelKey(ARROW_MODEL);
	private static final ModelResourceLocation SPECTRAL_ARROW_KEY = modelKey(SPECTRAL_ARROW_MODEL);
	private static final ModelResourceLocation FLINT_AND_STEEL_KEY = modelKey(FLINT_AND_STEEL_MODEL);
	private final ItemStack modelStack = new ItemStack(Items.BOOK);
	private final ShieldPlateRenderer shieldPlate = new ShieldPlateRenderer();
	private final TrophyShelfRenderer trophies = new TrophyShelfRenderer();
	private final MaterialShelfRenderer materials = new MaterialShelfRenderer();
	private final GemShelfRenderer gems = new GemShelfRenderer();
	private final VanillaShelfBlockRenderer vanillaBlocks = new VanillaShelfBlockRenderer();
	private final Map<VanillaShelfDisplay, ModelResourceLocation> displayModels =
		new EnumMap<>(VanillaShelfDisplay.class);

	public static void register() {
		TileEntityShelvingUnitRenderer renderer = new TileEntityShelvingUnitRenderer();
		FurnitureSurfaceDisplayRenderer.register(TALLOW_KEY);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShelvingUnit.class, renderer);
		MinecraftForge.EVENT_BUS.register(renderer);
		TileEntityProduceFurnitureRenderer.register(renderer);
	}

	@SubscribeEvent
	public void clearArmourPreview(WorldEvent.Unload event) {
		if (event.getWorld().isRemote) {
			this.trophies.clearPreview();
		}
	}

	@SubscribeEvent
	public void stitch(TextureStitchEvent.Pre event) {
		this.materials.stitch(event);
		for (ShelfContentKind kind : MEDIA_KINDS) {
			for (int count = 1; count <= ShelvingUnit.getMediaCapacity(kind); count++) {
				stitchModel(event, mediaModelPath(kind, count));
			}
		}
		stitchModel(event, TRAY_MODEL);
		stitchModel(event, GEM_PLATTER_MODEL);
		stitchModel(event, TALLOW_MODEL);
		stitchModel(event, ARROW_MODEL);
		stitchModel(event, SPECTRAL_ARROW_MODEL);
		stitchModel(event, FLINT_AND_STEEL_MODEL);
		for (VanillaShelfDisplay display : VanillaShelfDisplay.values()) {
			if (!display.usesBlockModel()) {
				stitchModel(event, display.getModelPath());
			}
		}
	}

	private static void stitchModel(TextureStitchEvent.Pre event, String path) {
		for (ResourceLocation texture : loadModel(path).getTextures()) {
			event.getMap().registerSprite(texture);
		}
	}

	@SubscribeEvent
	public void bake(ModelBakeEvent event) {
		this.materials.bake();
		this.gems.loadPalettes();
		for (ShelfContentKind kind : MEDIA_KINDS) {
			for (int count = 1; count <= ShelvingUnit.getMediaCapacity(kind); count++) {
				String path = mediaModelPath(kind, count);
				bakeModel(event, path, modelKey(path));
			}
		}
		bakeModel(event, TRAY_MODEL, TRAY_KEY);
		bakeModel(event, GEM_PLATTER_MODEL, GEM_PLATTER_KEY);
		bakeModel(event, TALLOW_MODEL, TALLOW_KEY);
		bakeModel(event, ARROW_MODEL, ARROW_KEY);
		bakeModel(event, SPECTRAL_ARROW_MODEL, SPECTRAL_ARROW_KEY);
		bakeModel(event, FLINT_AND_STEEL_MODEL, FLINT_AND_STEEL_KEY);
		for (VanillaShelfDisplay display : VanillaShelfDisplay.values()) {
			if (display.usesBlockModel()) {
				continue;
			}

			String path = display.getModelPath();
			ModelResourceLocation key = modelKey(path);
			bakeModel(event, path, key);
			this.displayModels.put(display, key);
		}
		this.vanillaBlocks.clear();
		SurfaceDisplayRenderHelper.clearShelfModelBounds();
	}

	private static void bakeModel(ModelBakeEvent event, String path, ModelResourceLocation key) {
		event.getModelRegistry().putObject(key, bakeModel(loadModel(path)));
	}

	static IBakedModel bakeModel(IModel model) {
		return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
			texture -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString()));
	}

	static IModel loadModel(String model) {
		ResourceLocation path = new ResourceLocation(Ironagefurniture.MODID, "block/" + model);
		try {
			return ModelLoaderRegistry.getModel(path);
		} catch (Exception failure) {
			throw new IllegalStateException("Cannot load shelving arrangement " + path, failure);
		}
	}

	private static String mediaModelPath(ShelfContentKind kind, int count) {
		return "shelving_media/" + kind.getName() + "_" + count;
	}

	private static ModelResourceLocation modelKey(String name) {
		return new ModelResourceLocation(Ironagefurniture.MODID + ":" + name, "inventory");
	}

	@Override
	public void renderTileEntityAt(TileEntityShelvingUnit tile, double x, double y, double z,
			float partialTicks, int destroyStage) {
		float shelfYaw = tile.getFacing().getOpposite().getHorizontalAngle();
		this.renderLevel(tile, tile.getLevel(false), x, y, z, shelfYaw, partialTicks);
		this.renderLevel(tile, tile.getLevel(true), x, y, z, shelfYaw, partialTicks);

		this.renderLabel(tile, false, x, y, z, -shelfYaw);
		this.renderLabel(tile, true, x, y, z, -shelfYaw);
	}

	private void renderLabel(TileEntityShelvingUnit tile, boolean upper, double x, double y, double z, float yaw) {
		ShelfLevel level = tile.getLevel(upper);
		FurnitureLabelRenderer.render(level.getLabel(),
			x + 0.5D + level.getSurfaceOffsetX() + 0.25D * tile.getFacing().getFrontOffsetX(),
			y + ShelvingUnit.getLabelHeight(upper),
			z + 0.5D + level.getSurfaceOffsetZ() + 0.25D * tile.getFacing().getFrontOffsetZ(), yaw, 1);
	}

	private void renderLevel(TileEntityShelvingUnit tile, ShelfLevel level,
			double x, double y, double z, float shelfYaw, float partialTicks) {
		if (level.isEmpty()) {
			return;
		}

		SurfaceSetting setting = level.getSurfaceSetting();
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x + 0.5D + level.getSurfaceOffsetX(), y + level.getSurfaceHeight(),
				z + 0.5D + level.getSurfaceOffsetZ());

			if (level.getShelfLayout() != ShelfLayout.NONE) {
				this.renderArrangement(tile, level, -shelfYaw, partialTicks);
				return;
			}

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
					double itemX = setting.getX(slot) - 0.5D;
					double itemZ = setting.getZ(slot) - 0.5D;
					float yaw = setting.getFacing(slot).getOpposite().getHorizontalAngle();
					VanillaShelfDisplay display = VanillaShelfDisplay.find(stack);
					if (display != null && display.isShelfObject() && SurfaceRenderRegistry.get(stack) == null) {
						GlStateManager.pushMatrix();
						try {
							GlStateManager.translate(itemX, 0, itemZ);
							GlStateManager.rotate(yaw, 0, 1, 0);
							this.renderShelfObject(stack, display, ShelfLayout.CENTRED_FOOD.place(0, display.getShelfModel()), tile.getPos());
						} finally {
							GlStateManager.popMatrix();
						}
						continue;
					}

					SurfaceDisplayRenderHelper.renderSurfaceItem(stack,
						new SurfaceRenderContext(0, 0, 0, itemX, 0.04D, itemZ, 0, yaw));
				}
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private void renderArrangement(TileEntityShelvingUnit tile, ShelfLevel level, float yaw, float partialTicks) {
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		ShelfLayout layout = level.getShelfLayout();

		if (layout.hasTray()) {
			ModelResourceLocation tray = layout == ShelfLayout.GEM_PLATTER ? GEM_PLATTER_KEY : TRAY_KEY;
			renderBoardModel(this.modelStack, tray, 0, 0, 0, 1, 0);
		}

		int visibleItems = level.getVisibleArrangementCount();
		for (int index = 0; index < visibleItems; index++) {
			ItemStack stack = level.getMediaItem(index);
			ShelfLayout.Placement placement = level.getShelfPlacement(index);
			if (SurfaceRenderRegistry.get(stack) == null) {
				VanillaShelfDisplay display = VanillaShelfDisplay.find(stack);
				MaterialShelfDisplay material = MaterialShelfDisplay.find(stack);
				if (material == MaterialShelfDisplay.GEM) {
					this.gems.render(stack, placement, tile.getWorld().getLightBrightness(tile.getPos()));
					continue;
				}

				if (material != null) {
					display = material.getArmourDisplay();
					if (display == null) {
						this.materials.render(stack, material, placement);
						continue;
					}
				}

				if (display != null) {
					if (display.isShelfObject()) {
						this.renderShelfObject(stack, display, placement, tile.getPos());
						continue;
					}

					boolean hasSeparateModel = TrophyShelfRenderer.usesArmourLayer(display)
						|| display == VanillaShelfDisplay.ELYTRA || display == VanillaShelfDisplay.BANNER;
					ItemStack base = hasSeparateModel ? this.modelStack : stack;
					renderBoardModel(base, this.displayModels.get(display), placement);

					TravelShelfRenderer.render(display, tile, level, stack, placement, yaw, partialTicks);
					this.trophies.render(display, tile.getWorld(), stack, placement, partialTicks);
					continue;
				}

				switch (layout) {
				case TOOL:
				case COMPACT_TOOL:
					SurfaceDisplayRenderHelper.renderShelfTool(stack, placement.bounds);
					continue;
				case ARROW_STACKS:
					ModelResourceLocation arrow = stack.getItem() == Items.SPECTRAL_ARROW ? SPECTRAL_ARROW_KEY : ARROW_KEY;
					renderBoardModel(stack, arrow, placement);
					continue;
				case FLINT_AND_STEEL_ROW:
					renderBoardModel(stack, FLINT_AND_STEEL_KEY, placement);
					continue;
				case SHIELD:
					this.renderShelfShield(stack, placement);
					continue;
				default:
					break;
				}
			}

			renderArrangementStack(stack, placement);
		}
	}

	private void renderShelfObject(ItemStack stack, VanillaShelfDisplay display, ShelfLayout.Placement placement, BlockPos pos) {
		if (display.usesBlockModel()) {
			this.vanillaBlocks.render(display, stack, placement.bounds, pos);
		} else {
			renderBoardModel(stack, this.displayModels.get(display), placement);
		}
	}

	void renderProduce(ItemStack stack, ShelfLayout.Placement placement) {
		VanillaShelfDisplay display = VanillaShelfDisplay.find(stack);
		if (display != null && SurfaceRenderRegistry.get(stack) == null) {
			renderBoardModel(stack, this.displayModels.get(display), placement);
		} else {
			renderArrangementStack(stack, placement);
		}
	}

	private static void renderArrangementStack(ItemStack stack, ShelfLayout.Placement placement) {
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(placement.x, placement.y, placement.z);
			GlStateManager.scale(placement.scale, placement.scaleY, placement.scale);
			SurfaceDisplayRenderHelper.renderSurfaceItem(stack,
				new SurfaceRenderContext(0, 0, 0, 0, 0.04D, 0, 0, placement.yaw));
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static void renderBoardModel(ItemStack stack, ModelResourceLocation key, ShelfLayout.Placement placement) {
		renderBoardModel(stack, key, placement.x, placement.y, placement.z, placement.scale, placement.yaw);
	}

	private static void renderBoardModel(ItemStack stack, ModelResourceLocation key,
			double x, double y, double z, double scale, float yaw) {
		Minecraft client = Minecraft.getMinecraft();
		IBakedModel model = client.getRenderItem().getItemModelMesher().getModelManager().getModel(key);
		renderBoardModel(stack, model, x, y, z, scale, yaw);
	}

	static void renderBoardModel(ItemStack stack, IBakedModel model, ShelfLayout.Placement placement) {
		renderBoardModel(stack, model, placement.x, placement.y, placement.z, placement.scale, placement.yaw);
	}

	static void renderBoardModel(ItemStack stack, IBakedModel model,
			double x, double y, double z, double scale, float yaw) {
		Minecraft client = Minecraft.getMinecraft();
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.translate(0, 0.5D, 0);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			SurfaceDisplayRenderHelper.renderBakedItem(client.getRenderItem(), stack, model);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private void renderShelfShield(ItemStack stack, ShelfLayout.Placement placement) {
		GlStateManager.pushMatrix();
		try {
			// The back of the plate is one pixel from the model origin. Put it on the board.
			GlStateManager.translate(placement.x, placement.y - placement.scale / 16, placement.z);
			GlStateManager.scale(placement.scale, placement.scale, placement.scale);
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.shieldPlate.render(stack);
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
			.getModel(modelKey(mediaModelPath(level.getMediaKind(), level.getMediaCount())));
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(ShelvingUnit.MEDIA_SCALE, ShelvingUnit.MEDIA_SCALE, ShelvingUnit.MEDIA_SCALE);
		GlStateManager.translate(0.0D, 0.5D, 0.0D);
		client.getRenderItem().renderItem(this.modelStack, model);
	}
}
