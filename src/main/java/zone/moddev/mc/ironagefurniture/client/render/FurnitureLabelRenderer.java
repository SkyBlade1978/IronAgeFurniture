package zone.moddev.mc.ironagefurniture.client.render;

import org.lwjgl.opengl.GL11;

import zone.moddev.mc.ironagefurniture.api.tile.FurnitureLabel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FurnitureLabelRenderer {
	private static final String MODEL = "shelving_media/decor/shelf_label";
	private static final ItemStack MODEL_STACK = new ItemStack(Items.BOOK);
	private static IBakedModel board;

	public static void register() {
		MinecraftForge.EVENT_BUS.register(new FurnitureLabelRenderer());
	}

	@SubscribeEvent
	public void stitch(TextureStitchEvent.Pre event) {
		for (ResourceLocation texture : TileEntityShelvingUnitRenderer.loadModel(MODEL).getTextures()) {
			event.getMap().registerSprite(texture);
		}
	}

	@SubscribeEvent
	public void bake(ModelBakeEvent event) {
		board = TileEntityShelvingUnitRenderer.bakeModel(TileEntityShelvingUnitRenderer.loadModel(MODEL));
	}

	static void render(FurnitureLabel label, double x, double y, double z, float yaw, double scale) {
		if (label.getSign() == null || board == null) {
			return;
		}

		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(yaw, 0, 1, 0);
			GlStateManager.scale(scale, scale, scale);
			TileEntityShelvingUnitRenderer.renderBoardModel(MODEL_STACK, board, 0, 0, 0, 1, 0);

			String text = label.getText();
			if (!text.isEmpty()) {
				renderText(text);
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private static void renderText(String line) {
		FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
		int width = font.getStringWidth(line);
		double scale = Math.min(0.009D, 0.62D / Math.max(width, 1));
		boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);

		GlStateManager.translate(0, FurnitureLabel.BOARD.maxY / 2, FurnitureLabel.BOARD.minZ - 0.001D);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.scale(scale, -scale, scale);
		GlStateManager.disableLighting();
		try {
			font.drawString(line, -width / 2, -4, 0xFF302015);
		} finally {
			if (lightingEnabled) {
				GlStateManager.enableLighting();
			}
			GlStateManager.color(1, 1, 1, 1);
		}
	}
}
