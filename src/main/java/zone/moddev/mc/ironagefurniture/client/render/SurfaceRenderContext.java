package zone.moddev.mc.ironagefurniture.client.render;

/** Compatibility context for the original client renderer registry. */
@Deprecated
public final class SurfaceRenderContext {
	public final double x, y, z, itemX, itemY, itemZ, blockSurfaceY;
	public final float yaw;

	public SurfaceRenderContext(double x, double y, double z, double itemX, double itemY,
			double itemZ, double blockSurfaceY, float yaw) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.itemX = itemX;
		this.itemY = itemY;
		this.itemZ = itemZ;
		this.blockSurfaceY = blockSurfaceY;
		this.yaw = yaw;
	}

	zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderContext core() {
		return new zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderContext(
			this.x, this.y, this.z, this.itemX, this.itemY, this.itemZ, this.blockSurfaceY, this.yaw);
	}
}
