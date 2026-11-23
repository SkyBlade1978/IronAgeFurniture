package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.ironagefurniture.api.Blocks.ToolRack;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityToolRack;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public final class TileEntityToolRackRenderer extends TileEntitySpecialRenderer<TileEntityToolRack> {
	private final ShieldPlateRenderer shieldPlate = new ShieldPlateRenderer();

	@Override
	public void renderTileEntityAt(TileEntityToolRack rack, double x, double y, double z, float partialTicks, int destroyStage) {
		IBlockState state = rack.getWorld().getBlockState(rack.getPos());
		if (!(state.getBlock() instanceof ToolRack)) {
			return;
		}

		EnumFacing front = state.getValue(BlockHorizontal.FACING);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x + 0.5D, y, z + 0.5D);
			GlStateManager.rotate(180 - front.getHorizontalAngle(), 0, 1, 0);
			GlStateManager.translate(-0.5D, 0, -0.5D);

			for (int slot = 0; slot < TileEntityToolRack.SLOTS; slot++) {
				ItemStack tool = rack.getTool(slot);
				if (tool != null) {
					AxisAlignedBB bounds = ToolRack.getToolBounds(slot, tool);
					if (tool.getItem() == Items.SHIELD) {
						this.renderShield(tool, bounds);
					} else {
						SurfaceDisplayRenderHelper.renderRackTool(tool, bounds);
					}
				}
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private void renderShield(ItemStack shield, AxisAlignedBB bounds) {
		double scale = (bounds.maxX - bounds.minX) * 16 / 12;
		GlStateManager.pushMatrix();
		try {
			// Vanilla's plate has its back one model pixel behind the origin.
			GlStateManager.translate((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2,
				bounds.maxZ + scale / 16);
			GlStateManager.rotate(180.0F, 0, 0, 1);
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.color(1, 1, 1, 1);
			this.shieldPlate.render(shield);
		} finally {
			GlStateManager.popMatrix();
		}
	}
}
