package zone.moddev.mc.ironagefurniture.api;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class LegacyFluidRegistrationTest {
	@Test public void callbackRunsOnceDuringOwnerWindow() {
		AtomicInteger calls = new AtomicInteger();
		LegacyFluidRegistration.register(calls::incrementAndGet);
		LegacyFluidRegistration.runPending();
		LegacyFluidRegistration.runPending();
		assertEquals(1, calls.get());
		try {
			LegacyFluidRegistration.register(calls::incrementAndGet);
			throw new AssertionError("Late callback was accepted");
		} catch (IllegalStateException expected) {
			assertEquals(1, calls.get());
		}
	}
}
