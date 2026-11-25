package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderContext;

import zone.moddev.mc.ironagefurniture.api.surface.FlowerPotSurface;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockFlowerPot.EnumFlowerType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ColorizerGrass;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FlowerPotSurfaceRenderer implements SurfaceRenderRegistry.Renderer {
	private static final EnumFlowerType[] FLOWERS = {
		EnumFlowerType.POPPY, EnumFlowerType.BLUE_ORCHID, EnumFlowerType.ALLIUM, EnumFlowerType.HOUSTONIA,
		EnumFlowerType.RED_TULIP, EnumFlowerType.ORANGE_TULIP, EnumFlowerType.WHITE_TULIP,
		EnumFlowerType.PINK_TULIP, EnumFlowerType.OXEYE_DAISY
	};
	private static final EnumFlowerType[] SAPLINGS = {
		EnumFlowerType.OAK_SAPLING, EnumFlowerType.SPRUCE_SAPLING, EnumFlowerType.BIRCH_SAPLING,
		EnumFlowerType.JUNGLE_SAPLING, EnumFlowerType.ACACIA_SAPLING, EnumFlowerType.DARK_OAK_SAPLING
	};

	public static void register() {
		SurfaceRenderRegistry.register(Items.FLOWER_POT, new FlowerPotSurfaceRenderer());
	}

	@Override
	public void render(ItemStack stack, SurfaceRenderContext context) {
		Minecraft client = Minecraft.getMinecraft();
		EnumFlowerType contents = getContents(FlowerPotSurface.getPlant(stack));
		IBlockState state = Blocks.FLOWER_POT.getDefaultState().withProperty(BlockFlowerPot.CONTENTS, contents);
		BlockRendererDispatcher blocks = client.getBlockRendererDispatcher();
		int tint = contents == EnumFlowerType.FERN ? ColorizerGrass.getGrassColor(0.5D, 1.0D) : 0xFFFFFF;
		float red = (tint >> 16 & 255) / 255.0F;
		float green = (tint >> 8 & 255) / 255.0F;
		float blue = (tint & 255) / 255.0F;

		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(context.x + context.itemX, context.y + context.blockSurfaceY, context.z + context.itemZ);
			GlStateManager.rotate(context.yaw, 0, 1, 0);
			double scale = FlowerPotSurface.MODEL_SCALE;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.translate(-0.5D, 0, -0.5D);
			blocks.getBlockModelRenderer().renderModelBrightnessColor(state, blocks.getModelForState(state),
				1.0F, red, green, blue);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static EnumFlowerType getContents(ItemStack plant) {
		if (plant == null) {
			return EnumFlowerType.EMPTY;
		}

		Block block = ((ItemBlock)plant.getItem()).getBlock();
		int variant = plant.getMetadata();

		if (block == Blocks.RED_FLOWER) {
			return variant >= 0 && variant < FLOWERS.length ? FLOWERS[variant] : EnumFlowerType.POPPY;
		}
		if (block == Blocks.SAPLING) {
			return variant >= 0 && variant < SAPLINGS.length ? SAPLINGS[variant] : EnumFlowerType.OAK_SAPLING;
		}
		if (block == Blocks.YELLOW_FLOWER) {
			return EnumFlowerType.DANDELION;
		}
		if (block == Blocks.RED_MUSHROOM) {
			return EnumFlowerType.MUSHROOM_RED;
		}
		if (block == Blocks.BROWN_MUSHROOM) {
			return EnumFlowerType.MUSHROOM_BROWN;
		}
		if (block == Blocks.DEADBUSH) {
			return EnumFlowerType.DEAD_BUSH;
		}
		if (block == Blocks.TALLGRASS) {
			return EnumFlowerType.FERN;
		}
		return EnumFlowerType.CACTUS;
	}
}
