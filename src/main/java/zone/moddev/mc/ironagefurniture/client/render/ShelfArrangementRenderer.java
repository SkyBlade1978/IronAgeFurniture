package zone.moddev.mc.ironagefurniture.client.render;

import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Baked books/records/lights stay in the shelf model; dynamic vessels use the shared item render registry. */
@SideOnly(Side.CLIENT)
public final class ShelfArrangementRenderer {
	private ShelfArrangementRenderer() { }

	public static void render(TileEntityWallShelf tile, double x, double y, double z) {
		IBlockState state = tile.getWorld().getBlockState(tile.getPos()).getActualState(tile.getWorld(), tile.getPos());
		EnumFacing facing = state.getValue(WallShelf.FACING);
		ShelfContentKind kind = tile.getEmbeddedKind();
		List<ItemStack> items = tile.getEmbeddedItems();
		if (kind == ShelfContentKind.FLOWER_POT && items.size() > 1) {
			SurfaceDisplayRenderHelper.renderPottedPlant(items.get(1), x, y, z,
				0.5D - facing.getFrontOffsetX() / 16.0D, 0.5D - facing.getFrontOffsetZ() / 16.0D, tile.getSurfaceHeight());
		}
		if (kind != ShelfContentKind.BOTTLES && kind != ShelfContentKind.DRINKWARE_LARGE
				&& kind != ShelfContentKind.DRINKWARE_MEDIUM && kind != ShelfContentKind.DRINKWARE_SMALL) return;
		double outwardX = -facing.getFrontOffsetX(), outwardZ = -facing.getFrontOffsetZ();
		double rightX = -outwardZ, rightZ = outwardX;
		float yaw = facing.getOpposite().getHorizontalAngle();
		if (kind == ShelfContentKind.BOTTLES) {
			WallShelf.ShelfSupport support = state.getValue(WallShelf.SUPPORT);
			double[][] layout = support == WallShelf.ShelfSupport.INNER_CORNER
				? new double[][]{{-0.12D, -0.28D}, {-0.10D, 0.07D}, {0.22D, 0.12D}}
				: support == WallShelf.ShelfSupport.OUTER_CORNER
					? new double[][]{{0.32D, 0.12D}, {0.14D, 0.15D}, {0.12D, 0.36D}} : null;
			for (int i = 0; i < items.size(); i++) {
				int index = items.size() == 1 ? 1 : items.size() == 2 ? i * 2 : i;
				double lateral = layout == null ? (i - (items.size() - 1) / 2.0D) * 0.28D : layout[index][0];
				double depth = layout == null ? 0.12D : layout[index][1];
				renderItem(items.get(i), x + 0.5D + outwardX * depth + rightX * lateral,
					y + tile.getSurfaceHeight(), z + 0.5D + outwardZ * depth + rightZ * lateral, yaw, 1.0F);
			}
		} else {
			int columns = kind == ShelfContentKind.DRINKWARE_LARGE ? 2 : 3;
			double spacing = kind == ShelfContentKind.DRINKWARE_LARGE ? 0.31D
				: kind == ShelfContentKind.DRINKWARE_MEDIUM ? 0.235D : 0.205D;
			double depthSpacing = kind == ShelfContentKind.DRINKWARE_LARGE ? 0.22D : 0.19D;
			float scale = kind == ShelfContentKind.DRINKWARE_LARGE ? 0.82F
				: kind == ShelfContentKind.DRINKWARE_MEDIUM ? 0.78F : 0.74F;
			int rows = (items.size() + columns - 1) / columns;
			for (int row = 0, i = 0; row < rows; row++) {
				int rowCount = Math.min(columns, items.size() - i);
				for (int column = 0; column < rowCount; column++, i++) {
					double lateral = (column - (rowCount - 1) / 2.0D) * spacing;
					double depth = 0.12D + (row - (rows - 1) / 2.0D) * depthSpacing;
					renderItem(items.get(i), x + 0.5D + outwardX * depth + rightX * lateral,
						y + tile.getSurfaceHeight(), z + 0.5D + outwardZ * depth + rightZ * lateral, yaw, scale);
				}
			}
		}
	}

	private static void renderItem(ItemStack stack, double x, double y, double z, float yaw, float scale) {
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x, y, z);
			GlStateManager.scale(scale, scale, scale);
			SurfaceDisplayRenderHelper.renderSurfaceItem(stack, new SurfaceRenderContext(0, 0, 0, 0, 0.04D, 0, 0, yaw));
		} finally { GlStateManager.popMatrix(); }
	}
}
