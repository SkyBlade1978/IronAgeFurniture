package zone.moddev.mc.ironagefurniture.api;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Optional ageing policy for a generic, non-craftable fluid-vessel base. */
public final class FluidAgingRegistry {
	public interface Policy {
		boolean age(FluidStack stack, int ticks);
		void resetToCurrentLevel(FluidStack stack);
		int progress(FluidStack stack);
		int progressTotal(FluidStack stack);
		String nextLevelName(FluidStack stack);
		boolean canAgeFurther(FluidStack stack);
	}

	private static final Map<Fluid, Policy> POLICIES = new IdentityHashMap<Fluid, Policy>();
	private static boolean frozen;

	private FluidAgingRegistry() { }

	public static synchronized void register(Fluid fluid, Policy policy) {
		if (frozen || fluid == null || policy == null || POLICIES.containsKey(fluid)) {
			throw new IllegalStateException("Invalid or duplicate fluid ageing policy");
		}
		POLICIES.put(fluid, policy);
	}

	public static synchronized void freeze() { frozen = true; }

	public static synchronized Policy find(FluidStack stack) {
		return stack == null || stack.getFluid() == null ? null : POLICIES.get(stack.getFluid());
	}

	public static FluidStack copyAtCurrentLevel(FluidStack stack) {
		if (stack == null) return null;
		FluidStack copy = stack.copy();
		Policy policy = find(copy);
		if (policy != null) policy.resetToCurrentLevel(copy);
		return copy;
	}
}
