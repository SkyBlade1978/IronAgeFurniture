package zone.moddev.mc.ironagefurniture.client.render;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageCrate;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TileEntityCrateRenderer extends TileEntitySpecialRenderer<TileEntityCrate> {
	@Override
	public void renderTileEntityAt(TileEntityCrate tile, double x, double y, double z, float ticks, int destroyStage) {
		IBlockState state = tile.getWorld().getBlockState(tile.getPos());
		if (state.getBlock() instanceof StorageCrate && tile.getCrateLabel().getSign() != null) {
			EnumFacing face = state.getValue(StorageCrate.FACING);
			Vec3d anchor = StorageCrate.labelAnchor(face);
			FurnitureLabelRenderer.render(tile.getCrateLabel(), x + anchor.xCoord, y + anchor.yCoord, z + anchor.zCoord,
				-face.getOpposite().getHorizontalAngle(), StorageCrate.LABEL_SCALE);
		}
	}
}
