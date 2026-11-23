package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout.Placement;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
final class TravelShelfRenderer {
	private TravelShelfRenderer() { }

	static void render(VanillaShelfDisplay display, TileEntityShelvingUnit shelf, ShelfLevel level,
			ItemStack stack, Placement placement, float shelfYaw, float partialTicks) {
		if (display != VanillaShelfDisplay.CLOCK && display != VanillaShelfDisplay.COMPASS
				&& display != VanillaShelfDisplay.FILLED_MAP) {
			return;
		}

		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(placement.x, placement.y, placement.z);
			GlStateManager.rotate(placement.yaw, 0, 1, 0);
			GlStateManager.scale(placement.scale, placement.scale, placement.scale);
			World world = shelf.getWorld();
			if (display == VanillaShelfDisplay.FILLED_MAP) {
				renderMap(world, stack);
				return;
			}

			boolean clock = display == VanillaShelfDisplay.CLOCK;
			float angle;
			if (!world.provider.isSurfaceWorld()) {
				angle = (world.getTotalWorldTime() % 40 + partialTicks) * 9.0F;
			} else if (clock) {
				angle = world.getCelestialAngle(partialTicks) * 360.0F;
			} else {
				BlockPos pos = shelf.getPos();
				BlockPos spawn = world.getSpawnPoint();
				double dx = spawn.getX() - pos.getX() - level.getSurfaceOffsetX();
				double dz = spawn.getZ() - pos.getZ() - level.getSurfaceOffsetZ();
				angle = heading(dx, dz, shelfYaw + placement.yaw);
			}
			renderNeedle(angle, clock);
		} finally {
			GlStateManager.popMatrix();
		}
	}

	static float heading(double dx, double dz, float yaw) {
		float worldAngle = dx == 0 && dz == 0 ? 0 : (float)Math.toDegrees(Math.atan2(-dx, -dz));
		return worldAngle - yaw;
	}

	private static void renderNeedle(float angle, boolean clock) {
		if (clock) {
			// Tilt the hand around the same hinge as the clock case, with noon at the top.
			GlStateManager.translate(0, 0.55D / 16, -3.1D / 16);
			GlStateManager.rotate(-45, 1, 0, 0);
			GlStateManager.translate(0, 1.68D / 16, 4.7D / 16);
			angle += 180.0F;
		} else {
			GlStateManager.translate(0, 2.13D / 16, 0);
		}
		GlStateManager.rotate(angle, 0, 1, 0);
		GlStateManager.disableTexture2D();
		boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
		GlStateManager.disableLighting();
		try {
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
			needleHalf(buffer, -2.95D / 16, 0.58D / 16, clock ? 255 : 192, clock ? 215 : 35, 33);
			needleHalf(buffer, 2.15D / 16, -0.58D / 16, 205, 218, 220);
			tessellator.draw();
		} finally {
			if (lighting) {
				GlStateManager.enableLighting();
			}
			GlStateManager.enableTexture2D();
		}
	}

	private static void needleHalf(VertexBuffer buffer, double tip, double halfWidth, int red, int green, int blue) {
		buffer.pos(0, 0, tip).color(red, green, blue, 255).endVertex();
		buffer.pos(-halfWidth, 0, 0).color(red, green, blue, 255).endVertex();
		buffer.pos(halfWidth, 0, 0).color(red, green, blue, 255).endVertex();
	}

	private static void renderMap(World world, ItemStack stack) {
		MapData data = (MapData)world.loadData(MapData.class, "map_" + stack.getMetadata());
		if (data == null) {
			return;
		}

		// Use the backing's front hinge and keep the picture just above its parchment.
		GlStateManager.translate(0, 0.6D / 16, -3.3D / 16);
		GlStateManager.rotate(-45, 1, 0, 0);
		GlStateManager.translate(4.25D / 16, 0.56D / 16, 8.85D / 16);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.rotate(90, 1, 0, 0);
		double pixel = 8.5D / (16 * 128);
		GlStateManager.scale(pixel, pixel, pixel);
		Minecraft.getMinecraft().entityRenderer.getMapItemRenderer().renderMap(data, true);
	}
}
