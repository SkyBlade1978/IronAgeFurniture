package zone.moddev.mc.ironagefurniture.api.surface;

import java.util.IdentityHashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/** Optional interactions with an item already displayed on a surface. */
public final class SurfaceInteractionRegistry {
	public interface Handler {
		boolean canHandle(ItemStack displayed, ItemStack held);
		void handle(SurfaceSettingHost host, Slot slot, EntityPlayer player, EnumHand hand, ItemStack held);
	}

	private static final Map<Item, Handler> HANDLERS = new IdentityHashMap<Item, Handler>();
	private static boolean frozen;

	private SurfaceInteractionRegistry() { }

	public static synchronized void register(Item displayedItem, Handler handler) {
		if (frozen || displayedItem == null || handler == null || HANDLERS.containsKey(displayedItem)) {
			throw new IllegalStateException("Invalid or duplicate surface interaction registration");
		}
		HANDLERS.put(displayedItem, handler);
	}

	public static synchronized void freeze() { frozen = true; }

	public static synchronized Handler get(ItemStack displayed) {
		return displayed == null || displayed.stackSize <= 0 ? null : HANDLERS.get(displayed.getItem());
	}
}
