package zone.moddev.mc.ironagefurniture.client.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import zone.moddev.mc.ironagefurniture.api.surface.MaterialShelfDisplay;
import zone.moddev.mc.skysfurniturecore.api.surface.ShelfLayout.Placement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class MaterialShelfRenderer {
	private final Map<ResourceLocation, IModel> templates = new HashMap<>();
	private final Map<ResourceLocation, IBakedModel> models = new HashMap<>();
	private final Map<ResourceLocation, IModel> bucketTemplates = new HashMap<>();
	private final Map<ResourceLocation, IBakedModel> buckets = new HashMap<>();
	private final Table<String, ImmutableMap<String, String>, IModel> variants = HashBasedTable.create();

	void stitch(TextureStitchEvent.Pre event) {
		this.templates.clear();
		this.bucketTemplates.clear();
		this.variants.clear();
		if (!Loader.isModLoaded("basemetals") && !Loader.isModLoaded("modernmetals")
				&& !Loader.isModLoaded("basegems") && !Loader.isModLoaded("baseminerals")) {
			return;
		}

		for (Item item : Item.REGISTRY) {
			ResourceLocation name = item.getRegistryName();
			if (!MaterialShelfDisplay.isMaterialResource(name)) {
				continue;
			}

			MaterialShelfDisplay display = MaterialShelfDisplay.find(new ItemStack(item));
			IModel model = this.loadShelfModel(name, display);
			if (model != null) {
				this.templates.put(name, model);
			}
		}

		for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
			ResourceLocation still = fluid.getStill();
			if (MaterialShelfDisplay.isMaterialFluid(fluid) && still != null && !this.bucketTemplates.containsKey(still)
					&& hasTexture(still)) {
				this.bucketTemplates.put(still, this.retexture("shelving_media/pantry/milk_bucket",
					ImmutableMap.of("liquid", still.toString())));
			}
		}

		for (IModel model : this.variants.values()) {
			for (ResourceLocation texture : model.getTextures()) {
				event.getMap().registerSprite(texture);
			}
		}
		this.variants.clear();
	}

	void bake() {
		Map<IModel, IBakedModel> baked = new IdentityHashMap<>();
		this.models.clear();
		this.templates.forEach((name, model) -> this.models.put(name,
			baked.computeIfAbsent(model, TileEntityShelvingUnitRenderer::bakeModel)));
		this.buckets.clear();
		this.bucketTemplates.forEach((still, model) -> this.buckets.put(still,
			baked.computeIfAbsent(model, TileEntityShelvingUnitRenderer::bakeModel)));
	}

	void render(ItemStack stack, MaterialShelfDisplay display, Placement placement) {
		if (display == MaterialShelfDisplay.BUCKET) {
			this.renderBucket(stack, placement);
			return;
		}

		IBakedModel baked = this.models.get(stack.getItem().getRegistryName());
		if (baked != null) {
			TileEntityShelvingUnitRenderer.renderBoardModel(stack, baked, placement);
		} else if (display == MaterialShelfDisplay.BLOCK || display == MaterialShelfDisplay.DOOR
				|| display == MaterialShelfDisplay.BARS || display == MaterialShelfDisplay.TRAPDOOR
				|| (display == MaterialShelfDisplay.PART && stack.getItem() instanceof ItemBlock)) {
			SurfaceDisplayRenderHelper.renderShelfObject(stack, placement.bounds, display == MaterialShelfDisplay.DOOR);
		} else {
			SurfaceDisplayRenderHelper.renderShelfTool(stack, placement.bounds);
		}
	}

	private void renderBucket(ItemStack stack, Placement placement) {
		FluidStack fluid = MaterialShelfDisplay.getBucketContents(stack);
		IBakedModel model = fluid == null ? null : this.buckets.get(fluid.getFluid().getStill());
		if (model == null) {
			SurfaceDisplayRenderHelper.renderShelfObject(stack, placement.bounds, false);
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		int colour = fluid.getFluid().getColor(fluid);
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(placement.x, placement.y, placement.z);
			GlStateManager.rotate(placement.yaw, 0, 1, 0);
			GlStateManager.scale(placement.scale, placement.scale, placement.scale);
			GlStateManager.translate(-0.5D, 0, -0.5D);
			client.getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model, 1.0F,
				(colour >> 16 & 255) / 255.0F, (colour >> 8 & 255) / 255.0F, (colour & 255) / 255.0F);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private IModel loadShelfModel(ResourceLocation name, MaterialShelfDisplay display) {
		if (display == MaterialShelfDisplay.DOOR) {
			String path = name.getResourcePath();
			int doorSuffix = path.indexOf("_door");
			if (doorSuffix < 1) {
				return null;
			}

			String door = name.getResourceDomain() + ":blocks/" + path.substring(0, doorSuffix);
			ResourceLocation upper = new ResourceLocation(door + "_door_upper");
			ResourceLocation lower = new ResourceLocation(door + "_door_lower");
			ResourceLocation edge = new ResourceLocation(door + "_block");
			if (hasTexture(upper) && hasTexture(lower) && hasTexture(edge)) {
				return this.retexture("shelving_media/utility/oak_door",
					ImmutableMap.of("upper", upper.toString(), "lower", lower.toString(), "edge", edge.toString()));
			}
			return null;
		}

		if (display != MaterialShelfDisplay.INGOT && display != MaterialShelfDisplay.NUGGET
				&& display != MaterialShelfDisplay.POWDER && display != MaterialShelfDisplay.SHIELD
				&& display != MaterialShelfDisplay.HORSE_ARMOUR) {
			return null;
		}

		String path = name.getResourcePath();
		String metal = path.substring(0, path.indexOf('_'));
		ResourceLocation texture = materialTexture(name.getResourceDomain(), metal);
		if (!hasTexture(texture)) {
			return null;
		}

		switch (display) {
		case INGOT:
			return this.retexture("shelving_media/materials/iron_ingot", ImmutableMap.of("metal", texture.toString()));
		case NUGGET:
			return this.retexture("shelving_media/materials/gold_nugget",
				ImmutableMap.of("gold", texture.toString(), "particle", texture.toString()));
		case POWDER:
			return this.retexture("shelving_media/pantry/gunpowder",
				ImmutableMap.of("fill", texture.toString(), "grain", texture.toString()));
		case SHIELD:
			return this.retexture("shelving_media/metals/shield", ImmutableMap.of("metal", texture.toString()));
		case HORSE_ARMOUR:
			return this.retexture("shelving_media/armour/iron_horse_armour", ImmutableMap.of("metal", texture.toString()));
		default:
			return null;
		}
	}

	private static ResourceLocation materialTexture(String namespace, String metal) {
		switch (metal) {
		case "iron":
		case "gold":
			return new ResourceLocation("blocks/" + metal + "_block");
		case "carbon":
			return new ResourceLocation("blocks/coal_block");
		case "mercury":
			return new ResourceLocation("basemetals", "blocks/mercury_still");
		default:
			return new ResourceLocation(namespace, "blocks/" + metal + "_block");
		}
	}

	private IModel retexture(String path, ImmutableMap<String, String> textures) {
		// Item forms such as powder and small powder can share the same shape and textures.
		return this.variants.row(path).computeIfAbsent(textures, bindings ->
			((IRetexturableModel)TileEntityShelvingUnitRenderer.loadModel(path)).retexture(bindings));
	}

	private static boolean hasTexture(ResourceLocation texture) {
		ResourceLocation file = new ResourceLocation(texture.getResourceDomain(), "textures/" + texture.getResourcePath() + ".png");
		try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(file)) {
			return true;
		} catch (IOException missing) {
			return false;
		}
	}
}
