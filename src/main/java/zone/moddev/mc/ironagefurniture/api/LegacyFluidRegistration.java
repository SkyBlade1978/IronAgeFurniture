package zone.moddev.mc.ironagefurniture.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes addon fluid registration during IAF pre-initialisation. Forge 1.10
 * embeds the active mod ID in a fluid's saved default identity; this narrow
 * hook lets an addon keep a legacy IAF-owned fluid identity without loading
 * addon classes when that addon is absent. The addon registers its callback
 * during construction, before any pre-initialisation event is dispatched.
 */
public final class LegacyFluidRegistration {
	private static final List<Runnable> callbacks = new ArrayList<Runnable>();
	private static boolean executed;

	private LegacyFluidRegistration() { }

	public static synchronized void register(Runnable callback) {
		if (callback == null || executed) throw new IllegalStateException("Fluid registration window closed");
		callbacks.add(callback);
	}

	public static synchronized void runPending() {
		if (executed) return;
		executed = true;
		for (Runnable callback : callbacks) callback.run();
		callbacks.clear();
	}
}
