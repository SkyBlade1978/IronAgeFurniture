package zone.moddev.mc.ironagefurniture.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelShield;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;

final class ShieldPlateRenderer {
	private final ModelShield model = new ModelShield();
	private final TileEntityBanner banner = new TileEntityBanner();

	void render(ItemStack shield) {
		ResourceLocation texture = BannerTextures.SHIELD_BASE_TEXTURE;
		if (shield.getSubCompound("BlockEntityTag", false) != null) {
			this.banner.setItemValues(shield);
			texture = BannerTextures.SHIELD_DESIGNS.getResourceLocation(this.banner.getPatternResourceLocation(),
				this.banner.getPatternList(), this.banner.getColorList());
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		this.model.plate.render(1.0F / 16.0F);
	}
}
