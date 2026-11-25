package zone.moddev.mc.ironagefurniture.client.render;

import org.junit.Test;
import static org.junit.Assert.*;

import net.minecraft.util.math.AxisAlignedBB;

public class GranularBarrelRendererTest {
	@Test
	public void kernelsCoverTheOpeningWithoutFloatingOrIntersectingTheBarrel() {
		assertTrue(GranularBarrelRenderer.KERNELS.size() > 200);
		for (AxisAlignedBB kernel : GranularBarrelRenderer.KERNELS) {
			assertTrue(GranularBarrelRenderer.insideOpening(kernel));
			assertEquals(0, kernel.minY, 0);
			assertTrue(kernel.maxY > 0);
			assertTrue(GranularBarrelRenderer.fillHeight(1) + kernel.maxY < 15.8D / 16);
		}

		for (double fraction : new double[] {1.0D / 512, 0.125D, 0.5D, 1}) {
			assertEquals((2 + 12 * fraction) / 16, GranularBarrelRenderer.fillHeight(fraction), 0);
		}
		assertEquals(GranularBarrelRenderer.fillHeight(0), GranularBarrelRenderer.fillHeight(-1), 0);
		assertEquals(GranularBarrelRenderer.fillHeight(1), GranularBarrelRenderer.fillHeight(2), 0);
	}
}
