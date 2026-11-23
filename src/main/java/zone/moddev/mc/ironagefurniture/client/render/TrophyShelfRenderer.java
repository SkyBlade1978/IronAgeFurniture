package zone.moddev.mc.ironagefurniture.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout.Placement;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelArmorStandArmor;
import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderArmorStand;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Fits vanilla trophy models to the shelf supports. */
@SideOnly(Side.CLIENT)
final class TrophyShelfRenderer {
	private static final ResourceLocation ELYTRA_TEXTURE = new ResourceLocation("textures/entity/elytra.png");
	private static final float MODEL_SCALE = 1.0F / 16;
	private final Cache<String, GameProfile> legacyOwners = CacheBuilder.newBuilder().maximumSize(64).build();
	private final TileEntityBanner banner = new TileEntityBanner();
	private final ModelBanner bannerModel = new ModelBanner();
	private final ModelElytra elytraModel = new ModelElytra();
	private EntityArmorStand preview;
	private RenderArmorStand previewRenderer;
	private LayerBipedArmor armourLayer;

	void clearPreview() {
		this.preview = null;
		this.legacyOwners.invalidateAll();
	}

	static boolean usesArmourLayer(VanillaShelfDisplay display) {
		return display == VanillaShelfDisplay.HELMET || display == VanillaShelfDisplay.CHESTPLATE
			|| display == VanillaShelfDisplay.LEGGINGS || display == VanillaShelfDisplay.BOOTS;
	}

	void render(VanillaShelfDisplay display, World world, ItemStack stack, Placement pose, float partialTicks) {
		boolean armour = usesArmourLayer(display);
		boolean bannerDisplay = display == VanillaShelfDisplay.BANNER;
		boolean elytra = display == VanillaShelfDisplay.ELYTRA;
		if (!armour && !bannerDisplay && !elytra && stack.getItem() != Items.SKULL) {
			return;
		}

		boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
		boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
		boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
		boolean rescale = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(pose.x, pose.y, pose.z);
			GlStateManager.rotate(pose.yaw, 0, 1, 0);
			GlStateManager.scale(pose.scale, pose.scale, pose.scale);
			GlStateManager.enableRescaleNormal();
			if (armour) {
				this.renderArmour(world, stack, partialTicks);
			} else if (bannerDisplay) {
				this.renderBanner(stack);
			} else if (elytra) {
				this.renderElytra(world, stack, partialTicks);
			} else {
				this.renderSkull(stack, display == VanillaShelfDisplay.DRAGON_HEAD);
			}
		} finally {
			GlStateManager.color(1, 1, 1, 1);
			if (!lighting) GlStateManager.disableLighting();
			if (alpha) GlStateManager.enableAlpha(); else GlStateManager.disableAlpha();
			if (blend) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
			if (cull) GlStateManager.enableCull(); else GlStateManager.disableCull();
			if (rescale) GlStateManager.enableRescaleNormal(); else GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}
	}

	private void renderBanner(ItemStack stack) {
		this.banner.setItemValues(stack);
		ResourceLocation texture = BannerTextures.BANNER_DESIGNS.getResourceLocation(this.banner.getPatternResourceLocation(),
			this.banner.getPatternList(), this.banner.getColorList());
		if (texture == null) {
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		// Hang the 20-by-40 vanilla cloth sideways to fill more of the shelf.
		GlStateManager.translate(-7.5D / 16, 4.30D / 16, 0);
		GlStateManager.rotate(90, 0, 0, 1);
		GlStateManager.scale(-0.375D, -0.375D, 0.375D);
		this.bannerModel.bannerSlate.render(MODEL_SCALE);
	}

	private void renderElytra(World world, ItemStack stack, float partialTicks) {
		this.preparePreview(world);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ELYTRA_TEXTURE);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.translate(0, 8.76D / 16, 0.9D / 16);
		// Face the opaque outside of the vanilla wings toward the front of the shelf.
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.scale(-0.36D, -0.36D, 0.36D);
		this.elytraModel.setRotationAngles(0, 0, 0, 0, 0, MODEL_SCALE, this.preview);
		this.elytraModel.render(this.preview, 0, 0, 0, 0, 0, MODEL_SCALE);
		if (stack.isItemEnchanted()) {
			GlStateManager.enableBlend();
			LayerArmorBase.renderEnchantedGlint(this.previewRenderer, this.preview, this.elytraModel,
				0, 0, partialTicks, 0, 0, 0, MODEL_SCALE);
		}
	}

	private void preparePreview(World world) {
		if (this.preview == null || this.preview.world != world) {
			this.preview = new EntityArmorStand(world);
			this.preview.setSilent(true);
			Rotations neutral = new Rotations(0, 0, 0);
			this.preview.setLeftArmRotation(neutral);
			this.preview.setRightArmRotation(neutral);
			this.preview.setLeftLegRotation(neutral);
			this.preview.setRightLegRotation(neutral);
		}
		if (this.previewRenderer == null) {
			this.previewRenderer = new RenderArmorStand(Minecraft.getMinecraft().getRenderManager());
			this.previewRenderer.getMainModel().isChild = false;
		}
		this.preview.ticksExisted = (int)(world.getTotalWorldTime() % 1000000);
	}

	private void renderArmour(World world, ItemStack stack, float partialTicks) {
		this.preparePreview(world);
		if (this.armourLayer == null) {
			this.armourLayer = new LayerBipedArmor(this.previewRenderer) {
				@Override
				protected void initArmor() {
					this.modelLeggings = new ModelArmorStandArmor(0.5F);
					this.modelArmor = new ModelArmorStandArmor(1.0F);
				}
			};
		}

		EntityEquipmentSlot slot = ((ItemArmor)stack.getItem()).armorType;
		switch (slot) {
		case HEAD:
			fitArmour(3.0D, 0.60D);
			break;
		case CHEST:
			fitArmour(6.65D, 0.45D);
			break;
		case LEGS:
			GlStateManager.translate(0, 0.7D / 16, -1.55D / 16);
			GlStateManager.rotate(28, 1, 0, 0);
			fitArmour(6.58D, 0.28D);
			break;
		case FEET:
			fitArmour(13.2D, 0.55D);
			break;
		default:
			return;
		}

		this.preview.setItemStackToSlot(slot, stack);
		try {
			this.armourLayer.doRenderLayer(this.preview, 0, 0, partialTicks, 0, 0, 0, MODEL_SCALE);
		} finally {
			this.preview.setItemStackToSlot(slot, null);
		}
	}

	private static void fitArmour(double y, double scale) {
		GlStateManager.translate(0, y / 16, 0);
		GlStateManager.scale(-scale, -scale, scale);
	}

	private void renderSkull(ItemStack stack, boolean dragon) {
		TileEntitySkullRenderer renderer = TileEntitySkullRenderer.instance;
		if (renderer == null) {
			return;
		}

		if (dragon) {
			GlStateManager.translate(0, 1.7D / 16, 2.7D / 16);
			GlStateManager.scale(0.4D, 0.4D, 0.4D);
		} else {
			GlStateManager.translate(0, 0.65D / 16, 0);
			GlStateManager.scale(0.88D, 0.88D, 0.88D);
		}
		boolean player = stack.getMetadata() == 3;
		renderer.renderSkull(-0.5F, 0, -0.5F, EnumFacing.UP, 0, stack.getMetadata(),
			player ? this.profile(stack) : null, -1, 0);
		if (player) {
			GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
		}
	}

	private GameProfile profile(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return null;
		}

		NBTTagCompound tags = stack.getTagCompound().copy();
		if (tags.hasKey("SkullOwner", 10)) {
			return NBTUtil.readGameProfileFromNBT(tags.getCompoundTag("SkullOwner"));
		}

		String name = tags.getString("SkullOwner");
		if (name.isEmpty()) {
			return null;
		}
		GameProfile profile = this.legacyOwners.getIfPresent(name);
		if (profile == null) {
			profile = TileEntitySkull.updateGameprofile(new GameProfile(null, name));
			this.legacyOwners.put(name, profile);
		}
		return profile;
	}
}
