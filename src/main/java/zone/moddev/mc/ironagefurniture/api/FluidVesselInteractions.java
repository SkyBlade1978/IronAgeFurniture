package zone.moddev.mc.ironagefurniture.api;

import java.util.ArrayList;
import java.util.List;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/** Addons retain ownership of their container conversion, serving and label rules. */
public final class FluidVesselInteractions {
	public interface Handler {
		boolean accepts(ItemStack stack);
		boolean interact(TileEntityBarrel vessel, EntityPlayer player, EnumHand hand, ItemStack stack);
	}
	private static final List<Handler> HANDLERS = new ArrayList<Handler>();
	private static boolean frozen;
	private FluidVesselInteractions() { }
	public static void register(Handler handler) {
		if (frozen || handler == null) throw new IllegalStateException("Vessel interactions are closed");
		HANDLERS.add(handler);
	}
	public static void freeze() { frozen = true; }
	public static Handler find(ItemStack stack) {
		if (stack != null && stack.stackSize > 0)
			for (Handler handler : HANDLERS) if (handler.accepts(stack)) return handler;
		return null;
	}
}
