package zone.moddev.mc.ironagefurniture.api.surface;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

/** Server-authoritative placement and retrieval of exact displayed ItemStacks. */
public final class SurfaceSettingInteraction {
	public enum Result { NOT_HANDLED, HANDLED, HANDLED_AND_EMPTIED }

	private SurfaceSettingInteraction() { }

	public static Result handle(SurfaceSettingHost host, EntityPlayer player, EnumHand hand,
			ItemStack held, float hitX, float hitZ, EnumFacing.Axis forcedAxis, boolean allowSingleton) {
		if (host == null || player == null) return Result.NOT_HANDLED;
		SurfaceSetting setting = host.getSurfaceSetting();
		Slot target = setting.findTarget(hitX, hitZ);
		ItemStack displayed = setting.getItem(target);
		SurfaceInteractionRegistry.Handler handler = SurfaceInteractionRegistry.get(displayed);
		if (handler != null && handler.canHandle(displayed, held)) {
			if (!player.getEntityWorld().isRemote) handler.handle(host, target, player, hand, held);
			return Result.HANDLED;
		}
		if (target != null) {
			if (held == null || held.stackSize <= 0
					|| (ItemStack.areItemsEqual(displayed, held)
						&& ItemStack.areItemStackTagsEqual(displayed, held))) {
				boolean last = setting.getItemCount() == 1;
				if (!player.getEntityWorld().isRemote) {
					ItemStack removed = setting.remove(target);
					host.markSurfaceSettingChanged();
					if (removed != null && !player.inventory.addItemStackToInventory(removed)) {
						player.dropItem(removed, false);
					}
				}
				return last ? Result.HANDLED_AND_EMPTIED : Result.HANDLED;
			}
			return Result.HANDLED;
		}
		if (held == null || held.stackSize <= 0) return Result.NOT_HANDLED;
		boolean settingItem = SurfaceSetting.isSettingItem(held);
		if ((!allowSingleton && !settingItem)
				|| !setting.canInsert(held, player.getHorizontalFacing(), hitX, hitZ, forcedAxis)) {
			return settingItem ? Result.HANDLED : Result.NOT_HANDLED;
		}
		if (!player.getEntityWorld().isRemote
				&& setting.insert(held, player.getHorizontalFacing(), hitX, hitZ, forcedAxis)) {
			host.markSurfaceSettingChanged();
			if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) player.setHeldItem(hand, null);
		}
		return Result.HANDLED;
	}
}
