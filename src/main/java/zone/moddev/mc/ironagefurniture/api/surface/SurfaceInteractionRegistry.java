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
		default java.util.List<ItemStack> getDrops(ItemStack displayed) {
			return java.util.Collections.singletonList(displayed.copy());
		}
	}

	private static final Map<Item, Handler> HANDLERS = new IdentityHashMap<>();
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

	/** Stored containers can return contents separately without changing their saved stack. */
	public static java.util.List<ItemStack> drops(ItemStack displayed) {
		if (displayed == null || displayed.stackSize <= 0) return java.util.Collections.emptyList();
		Handler handler = get(displayed);
		return handler == null ? java.util.Collections.singletonList(displayed.copy()) : handler.getDrops(displayed.copy());
	}

	public static void drop(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, ItemStack displayed) {
		for (ItemStack stack : drops(displayed)) net.minecraft.block.Block.spawnAsEntity(world, pos, stack);
	}
}
