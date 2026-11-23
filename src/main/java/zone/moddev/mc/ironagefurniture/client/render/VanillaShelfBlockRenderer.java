package zone.moddev.mc.ironagefurniture.client.render;

import java.util.IdentityHashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Fits vanilla block geometry to a shelf without placing a live block. */
@SideOnly(Side.CLIENT)
final class VanillaShelfBlockRenderer {
	private final Map<IBlockState, AxisAlignedBB> boundsByState = new IdentityHashMap<>();
	private final CuttingShelfRenderer cuttings = new CuttingShelfRenderer();

	void clear() {
		this.boundsByState.clear();
		this.cuttings.clear();
	}

	static IBlockState displayState(VanillaShelfDisplay display, ItemStack stack) {
		Block block = Block.getBlockFromItem(stack.getItem());
		IBlockState state = block.getStateFromMeta(stack.getMetadata());

		switch (display) {
		case GLASS_PANE:
		case STAINED_GLASS_PANE:
		case IRON_BARS:
			return state.withProperty(BlockPane.EAST, true).withProperty(BlockPane.WEST, true);
		case LEVER:
			return block.getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.UP_Z);
		case TRIPWIRE_HOOK:
			return block.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.NORTH);
		case VINE:
			return block.getDefaultState().withProperty(BlockVine.WEST, true);
		case DOUBLE_PLANT:
			return state.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);
		default:
			return state;
		}
	}

	void render(VanillaShelfDisplay display, ItemStack stack, AxisAlignedBB target, BlockPos pos) {
		IBlockState state = displayState(display, stack);
		if (isLoosePlant(display)) {
			this.cuttings.render(state, target, pos);
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		BlockRendererDispatcher blocks = client.getBlockRendererDispatcher();
		AxisAlignedBB bounds = this.bounds(state, blocks);
		double scale = Math.min(SurfaceDisplayRenderHelper.fitAxis(target.minX, target.maxX, bounds.minX, bounds.maxX),
			Math.min(SurfaceDisplayRenderHelper.fitAxis(target.minY, target.maxY, bounds.minY, bounds.maxY),
				SurfaceDisplayRenderHelper.fitAxis(target.minZ, target.maxZ, bounds.minZ, bounds.maxZ)));
		double x = (target.minX + target.maxX - scale * (bounds.minX + bounds.maxX)) / 2;
		double z = (target.minZ + target.maxZ - scale * (bounds.minZ + bounds.maxZ)) / 2;
		boolean enableBlend = display == VanillaShelfDisplay.STAINED_GLASS_PANE && !GL11.glIsEnabled(GL11.GL_BLEND);

		client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		try {
			if (enableBlend) {
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}

			GlStateManager.translate(x, target.minY - scale * bounds.minY, z);
			GlStateManager.scale(scale, scale, scale);
			renderBlock(client, blocks, state, pos);
		} finally {
			GlStateManager.popMatrix();
			if (enableBlend) {
				GlStateManager.disableBlend();
			}
		}
	}

	private static boolean isLoosePlant(VanillaShelfDisplay display) {
		switch (display) {
		case DEADBUSH:
		case DOUBLE_PLANT:
		case RED_FLOWER:
		case YELLOW_FLOWER:
		case SAPLING:
		case TALLGRASS:
		case VINE:
			return true;
		default:
			return false;
		}
	}

	private AxisAlignedBB bounds(IBlockState state, BlockRendererDispatcher blocks) {
		return this.boundsByState.computeIfAbsent(state,
			key -> SurfaceDisplayRenderHelper.modelBounds(blocks.getModelForState(key), key, null));
	}

	private static void renderBlock(Minecraft client, BlockRendererDispatcher blocks, IBlockState state, BlockPos pos) {
		IBakedModel model = blocks.getModelForState(state);
		int tint = client.getBlockColors().colorMultiplier(state, client.world, pos, 0);
		blocks.getBlockModelRenderer().renderModelBrightnessColor(state, model, 1.0F,
			(tint >> 16 & 255) / 255.0F, (tint >> 8 & 255) / 255.0F, (tint & 255) / 255.0F);
	}
}
