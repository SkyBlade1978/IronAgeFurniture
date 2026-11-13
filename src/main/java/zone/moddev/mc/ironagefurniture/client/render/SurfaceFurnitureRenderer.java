package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SurfaceFurnitureRenderer<T extends TileEntity & SurfaceSettingHost>
		extends TileEntitySpecialRenderer<T> {
	private final double surfaceY;

	public SurfaceFurnitureRenderer() { this(1.0D); }

	public SurfaceFurnitureRenderer(double surfaceY) { this.surfaceY = surfaceY; }

	@Override
	public void renderTileEntityAt(T tile, double x, double y, double z, float partialTicks, int destroyStage) {
		SurfaceSetting setting = tile.getSurfaceSetting();
		for (Slot slot : Slot.values()) {
			ItemStack stack = setting.getItem(slot);
			if (stack == null) continue;
			float yaw = setting.getFacing(slot).getOpposite().getHorizontalAngle();
			SurfaceDisplayRenderHelper.renderSurfaceItem(stack,
				new SurfaceRenderContext(x, y, z, setting.getX(slot), this.surfaceY + 0.04D,
					setting.getZ(slot), this.surfaceY, yaw));
		}
	}
}
