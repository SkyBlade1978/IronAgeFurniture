package zone.moddev.mc.ironagefurniture.api.surface;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;

/** Vanilla bottles share the bottle setting; addon bottles register their own descriptors. */
public final class VanillaSurfaceBottles {
	private static final SurfaceContentRegistry.Descriptor DESCRIPTOR = new SurfaceContentRegistry.Descriptor(
		SurfaceSetting.Category.BOTTLE, new AxisAlignedBB(-0.0777D, 0.002D, -0.0777D, 0.0777D, 0.4619D, 0.0777D));
	private VanillaSurfaceBottles() { }
	public static void register() {
		for (Item item : new Item[]{Items.GLASS_BOTTLE, Items.POTIONITEM, Items.SPLASH_POTION,
				Items.LINGERING_POTION, Items.EXPERIENCE_BOTTLE, Items.DRAGON_BREATH}) {
			SurfaceContentRegistry.register(item, stack -> DESCRIPTOR);
		}
	}
}
