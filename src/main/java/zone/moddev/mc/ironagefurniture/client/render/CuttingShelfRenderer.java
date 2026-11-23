package zone.moddev.mc.ironagefurniture.client.render;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.BlockDoublePlant.EnumPlantType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Builds a solid laid-down cutting from each vanilla plant's texture. */
@SideOnly(Side.CLIENT)
final class CuttingShelfRenderer {
	private static final int SIZE = 16;
	private static final String SUNFLOWER_HEAD = "minecraft:blocks/double_plant_sunflower_front";

	private final Map<IBlockState, List<Face>> models = new IdentityHashMap<>();

	void clear() {
		this.models.clear();
	}

	void render(IBlockState state, AxisAlignedBB bounds, BlockPos pos) {
		Minecraft client = Minecraft.getMinecraft();
		List<Face> faces = this.models.computeIfAbsent(state, key -> bake(client, key));
		int tint = client.getBlockColors().colorMultiplier(state, client.world, pos, 0);
		float light = client.world.getLightBrightness(pos);
		boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		try {
			GlStateManager.translate(bounds.minX, bounds.minY, bounds.minZ);
			GlStateManager.scale(bounds.maxX - bounds.minX, bounds.maxY - bounds.minY, bounds.maxZ - bounds.minZ);

			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			for (Face face : faces) {
				face.render(buffer, tint, light);
			}
			tessellator.draw();
		} finally {
			if (lighting) {
				GlStateManager.enableLighting();
			}
			GlStateManager.enableTexture2D();
			GlStateManager.popMatrix();
		}
	}

	private static List<Face> bake(Minecraft client, IBlockState state) {
		boolean tall = state.getBlock() == Blocks.DOUBLE_PLANT;
		int length = tall ? SIZE * 2 : SIZE;
		int[][] colours = new int[length][SIZE];
		copySprite(sprite(client, state), colours, length - SIZE);
		if (tall) {
			IBlockState top = state.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.UPPER);
			copySprite(sprite(client, top), colours, 0);
			if (state.getValue(BlockDoublePlant.VARIANT) == EnumPlantType.SUNFLOWER) {
				copySprite(client.getTextureMapBlocks().getAtlasSprite(SUNFLOWER_HEAD), colours, 0);
			}
		}

		int[][] heights = new int[length][SIZE];
		int minX = SIZE;
		int minZ = length;
		int maxX = 0;
		int maxZ = 0;
		int maxHeight = 0;
		for (int z = 0; z < length; z++) {
			for (int x = 0; x < SIZE; x++) {
				if (colours[z][x] == 0) {
					continue;
				}

				// Thin stems are shallow. Petals and dense foliage rise above them.
				int neighbours = 0;
				for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
					if (sample(colours, x + side.getFrontOffsetX(), z + side.getFrontOffsetZ()) != 0) {
						neighbours++;
					}
				}
				heights[z][x] = 1 + neighbours / 2;
				minX = Math.min(minX, x);
				minZ = Math.min(minZ, z);
				maxX = Math.max(maxX, x + 1);
				maxZ = Math.max(maxZ, z + 1);
				maxHeight = Math.max(maxHeight, heights[z][x]);
			}
		}

		List<Face> faces = new ArrayList<>();
		float width = maxX - minX;
		float depth = maxZ - minZ;
		for (int z = minZ; z < maxZ; z++) {
			for (int x = minX; x < maxX; x++) {
				int height = heights[z][x];
				if (height == 0) {
					continue;
				}

				float x0 = (x - minX) / width;
				float x1 = (x + 1 - minX) / width;
				// The top of the sprite becomes the north end of the cutting.
				float z0 = (z - minZ) / depth;
				float z1 = (z + 1 - minZ) / depth;
				float y1 = (float)height / maxHeight;
				int colour = colours[z][x];
				faces.add(new Face(EnumFacing.UP, colour, x0, y1, z0, x1, y1, z1));
				faces.add(new Face(EnumFacing.DOWN, colour, x0, 0, z0, x1, 0, z1));
				for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
					int neighbour = sample(heights, x + side.getFrontOffsetX(), z + side.getFrontOffsetZ());
					if (neighbour < height) {
						faces.add(new Face(side, colour, x0, (float)neighbour / maxHeight, z0, x1, y1, z1));
					}
				}
			}
		}
		return faces;
	}

	private static TextureAtlasSprite sprite(Minecraft client, IBlockState state) {
		return client.getBlockRendererDispatcher().getModelForState(state).getParticleTexture();
	}

	private static void copySprite(TextureAtlasSprite sprite, int[][] colours, int offset) {
		if (sprite.getFrameCount() == 0 || "missingno".equals(sprite.getIconName())) {
			return;
		}

		int width = sprite.getIconWidth();
		int height = sprite.getIconHeight();
		int[] pixels = sprite.getFrameTextureData(0)[0];
		for (int y = 0; y < SIZE; y++) {
			int sourceRow = (2 * y + 1) * height / (2 * SIZE) * width;
			for (int x = 0; x < SIZE; x++) {
				int colour = pixels[sourceRow + (2 * x + 1) * width / (2 * SIZE)];
				if ((colour >>> 24) >= 128) {
					colours[offset + y][x] = colour;
				}
			}
		}
	}

	private static int sample(int[][] grid, int x, int z) {
		return x >= 0 && x < SIZE && z >= 0 && z < grid.length ? grid[z][x] : 0;
	}

	private static final class Face {
		private final EnumFaceDirection direction;
		private final int colour;
		private final float shade;
		private final float[] bounds = new float[6];

		private Face(EnumFacing side, int colour, float x0, float y0, float z0, float x1, float y1, float z1) {
			this.direction = EnumFaceDirection.getFacing(side);
			this.colour = colour;
			this.shade = LightUtil.diffuseLight(side);
			this.bounds[EnumFacing.WEST.getIndex()] = x0;
			this.bounds[EnumFacing.DOWN.getIndex()] = y0;
			this.bounds[EnumFacing.NORTH.getIndex()] = z0;
			this.bounds[EnumFacing.EAST.getIndex()] = x1;
			this.bounds[EnumFacing.UP.getIndex()] = y1;
			this.bounds[EnumFacing.SOUTH.getIndex()] = z1;
		}

		private void render(VertexBuffer buffer, int tint, float light) {
			float brightness = this.shade * light / 255.0F;
			int red = Math.round((this.colour >> 16 & 255) * (tint >> 16 & 255) * brightness);
			int green = Math.round((this.colour >> 8 & 255) * (tint >> 8 & 255) * brightness);
			int blue = Math.round((this.colour & 255) * (tint & 255) * brightness);
			for (int vertex = 0; vertex < 4; vertex++) {
				VertexInformation corners = this.direction.getVertexInformation(vertex);
				buffer.pos(this.bounds[corners.xIndex], this.bounds[corners.yIndex], this.bounds[corners.zIndex])
					.color(red, green, blue, 255).endVertex();
			}
		}
	}
}
