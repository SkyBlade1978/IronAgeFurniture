package zone.moddev.mc.ironagefurniture.client.render;

/** Location and orientation supplied to a client-only surface renderer. */
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
}
