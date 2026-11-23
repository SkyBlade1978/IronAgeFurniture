package zone.moddev.mc.ironagefurniture.client.render;

import net.minecraft.util.EnumFacing;

public final class TravelInstrumentProbe {
	private TravelInstrumentProbe() { }

	public static void verify() {
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			float shelfYaw = -facing.getOpposite().getHorizontalAngle();
			for (EnumFacing spawn : EnumFacing.Plane.HORIZONTAL) {
				int x = spawn.getFrontOffsetX();
				int z = spawn.getFrontOffsetZ();
				float local = TravelShelfRenderer.heading(x, z, shelfYaw);
				double worldAngle = Math.toRadians(local + shelfYaw);
				if (Math.abs(-Math.sin(worldAngle) - x) > 0.00001D || Math.abs(-Math.cos(worldAngle) - z) > 0.00001D) {
					throw new IllegalStateException("Shelf compass pointed away from spawn for " + facing + " towards " + spawn);
				}
			}
		}
	}
}
