package zone.moddev.mc.ironagefurniture.api.surface;

import java.util.ArrayList;
import java.util.List;
import zone.moddev.mc.ironagefurniture.api.VasePlantHelper;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/** Vases extend the same common placement/interaction contract as addon items. */
public final class GlassVaseSurface implements SurfaceInteractionRegistry.Handler {
	public static final float SCALE = 0.83F;
	private GlassVaseSurface() { }
	public static void register(Item item) {
		SurfaceContentRegistry.register(item, stack -> new SurfaceContentRegistry.Descriptor(SurfaceSetting.Category.SINGLETON,
			VasePlantHelper.hasPlant(stack) ? 0.56D * SCALE : 4.5D / 16.0D * SCALE,
			VasePlantHelper.hasPlant(stack) ? 0.68D * SCALE : 6.0D / 16.0D * SCALE));
		SurfaceInteractionRegistry.register(item, new GlassVaseSurface());
	}
	@Override public boolean canHandle(ItemStack displayed, ItemStack held) {
		return VasePlantHelper.canHandleVaseClick(displayed, held);
	}
	@Override public void handle(SurfaceSettingHost host, Slot slot, EntityPlayer player, EnumHand hand, ItemStack held) {
		ItemStack vase = host.getSurfaceSetting().getItem(slot);
		if (VasePlantHelper.hasPlant(vase)) {
			ItemStack plant = VasePlantHelper.removePlant(vase);
			if (!player.inventory.addItemStackToInventory(plant)) player.dropItem(plant, false);
		} else if (VasePlantHelper.addPlant(vase, held)) {
			if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) player.setHeldItem(hand, null);
		}
		host.markSurfaceSettingChanged();
	}
	@Override public List<ItemStack> getDrops(ItemStack displayed) {
		List<ItemStack> result = new ArrayList<ItemStack>();
		ItemStack plant = VasePlantHelper.removePlant(displayed);
		result.add(displayed);
		if (plant != null) result.add(plant);
		return result;
	}
}
