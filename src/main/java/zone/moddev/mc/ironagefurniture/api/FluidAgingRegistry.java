package zone.moddev.mc.ironagefurniture.api;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Compatibility entry point for staged beverage ageing. */
@Deprecated
public final class FluidAgingRegistry {
	public interface Policy extends zone.moddev.mc.skysfurniturecore.api.vessel.FluidAgingRegistry.Policy { }

	private FluidAgingRegistry() { }

	public static void register(Fluid fluid, Policy policy) {
		zone.moddev.mc.skysfurniturecore.api.vessel.FluidAgingRegistry.register(fluid, policy);
	}

	public static void freeze() {
		zone.moddev.mc.skysfurniturecore.api.vessel.FluidAgingRegistry.freeze();
	}

	public static Policy find(FluidStack stack) {
		zone.moddev.mc.skysfurniturecore.api.vessel.FluidAgingRegistry.Policy policy =
			zone.moddev.mc.skysfurniturecore.api.vessel.FluidAgingRegistry.find(stack);
		return policy instanceof Policy ? (Policy)policy : null;
	}

	public static FluidStack copyAtCurrentLevel(FluidStack stack) {
		return zone.moddev.mc.skysfurniturecore.api.vessel.FluidAgingRegistry.copyAtCurrentLevel(stack);
	}
}
