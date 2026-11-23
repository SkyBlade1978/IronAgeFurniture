package zone.moddev.mc.ironagefurniture.client.render;

import org.lwjgl.opengl.GL11;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.BarrelBulkStorage;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TileEntityBarrelRenderer extends TileEntitySpecialRenderer<TileEntityBarrel> {
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
		boolean showProduce = storage.getCapacity() == 128;
		double fraction = Math.min(1, (double)storage.getCount() / storage.getCapacity());
		double height = (2 + (showProduce ? 10 : 12) * fraction) / 16;
		Minecraft client = Minecraft.getMinecraft();
		TextureAtlasSprite texture = client.getTextureMapBlocks().getAtlasSprite(fillTexture(stack.getItem()));
		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, y + height, z);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
			// The wooden walls leave three connected strips across the barrel opening.
			drawFill(buffer, texture, 4, 2.14D, 12, 4);
			drawFill(buffer, texture, 2.14D, 4, 13.86D, 12);
			drawFill(buffer, texture, 4, 12, 12, 13.86D);
			tessellator.draw();

			if (showProduce) {
				renderSample(stack, 0.38D, 0.36D, 25);
				if (storage.getCount() >= 2) {
					renderSample(stack, 0.55D, 0.63D, -26);
				}
				if (storage.getCount() >= 3) {
					renderSample(stack, 0.64D, 0.39D, 14);
				}
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static void renderSample(ItemStack stack, double x, double z, float yaw) {
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, 0.004D, z);
			GlStateManager.scale(0.47D, 0.47D, 0.47D);
			SurfaceDisplayRenderHelper.renderSurfaceItem(stack, new SurfaceRenderContext(0, 0, 0, 0, 0.035D, 0, 0, yaw));
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
		if (item == Items.NETHER_WART) return "minecraft:blocks/nether_wart_block";
		if (item == Items.FISH) return "minecraft:blocks/wool_colored_gray";
		if (item == Items.CARROT) return "minecraft:blocks/hardened_clay_stained_orange";
		if (item == Items.BEETROOT) return "minecraft:blocks/hardened_clay_stained_red";
		if (item == Item.getItemFromBlock(Blocks.RED_MUSHROOM)) return "minecraft:blocks/mushroom_block_skin_red";
		if (item == Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)) return "minecraft:blocks/mushroom_block_skin_brown";
		if (item == Items.POTATO) return "minecraft:blocks/hardened_clay_stained_yellow";
		return "minecraft:blocks/hay_block_top";
	}
}
