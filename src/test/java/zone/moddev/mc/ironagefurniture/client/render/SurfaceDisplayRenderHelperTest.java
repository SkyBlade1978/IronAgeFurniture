package zone.moddev.mc.ironagefurniture.client.render;

import javax.vecmath.Matrix4f;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SurfaceDisplayRenderHelperTest {
	@Test
	public void liftsVanillaBlockByHalfItsDisplayedHeight() {
		Matrix4f fixed = new Matrix4f();
		fixed.set(0.5F);
		assertEquals(0.1375D, SurfaceDisplayRenderHelper.blockItemLift(fixed), 0.000001D);
	}

	@Test
	public void accountsForVerticalTranslationAndTilt() {
		Matrix4f fixed = new Matrix4f();
		fixed.rotX((float)(Math.PI / 4.0D));
		fixed.m13 = 0.125F;
		double expected = 0.55D * (Math.sqrt(0.5D) - 0.125D);
		assertEquals(expected, SurfaceDisplayRenderHelper.blockItemLift(fixed), 0.000001D);
	}

	@Test
	public void groundsBlockWithNoPerspectiveTransform() {
		assertEquals(0.275D, SurfaceDisplayRenderHelper.blockItemLift(null), 0.000001D);
	}
}
