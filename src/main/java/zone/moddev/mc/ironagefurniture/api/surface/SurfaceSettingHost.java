package zone.moddev.mc.ironagefurniture.api.surface;

public interface SurfaceSettingHost {
	SurfaceSetting getSurfaceSetting();

	void markSurfaceSettingChanged();

	default double getSurfaceHeight() { return 1.0D; }
	default double getSurfaceOffsetX() { return 0.0D; }
	default double getSurfaceOffsetZ() { return 0.0D; }
	default net.minecraft.util.EnumFacing.Axis getSurfaceAxis() { return null; }
}
