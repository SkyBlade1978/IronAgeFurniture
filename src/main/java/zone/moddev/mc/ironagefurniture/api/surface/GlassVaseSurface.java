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

	private GlassVaseSurface() {
	}

	public static void register(Item item) {
		SurfaceContentRegistry.register(item, stack -> {
			boolean planted = VasePlantHelper.hasPlant(stack);
			double width = planted ? 0.56D : 4.5D / 16.0D;
			double height = planted ? 0.68D : 6.0D / 16.0D;

			return new SurfaceContentRegistry.Descriptor(SurfaceSetting.Category.SINGLETON, width * SCALE, height * SCALE);
		});
		SurfaceInteractionRegistry.register(item, new GlassVaseSurface());
	}

	@Override
	public boolean canHandle(ItemStack displayed, ItemStack held) {
		return VasePlantHelper.canHandleVaseClick(displayed, held);
	}

	@Override
	public void handle(SurfaceSettingHost host, Slot slot, EntityPlayer player, EnumHand hand, ItemStack held) {
		ItemStack vase = host.getSurfaceSetting().getItem(slot);

		if (VasePlantHelper.hasPlant(vase)) {
			ItemStack plant = VasePlantHelper.removePlant(vase);

			if (!player.inventory.addItemStackToInventory(plant)) {
				player.dropItem(plant, false);
			}
		} else if (VasePlantHelper.addPlant(vase, held)) {
			if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) {
				player.setHeldItem(hand, null);
			}
		}

		host.markSurfaceSettingChanged();
	}

	@Override
	public List<ItemStack> getDrops(ItemStack displayed) {
		List<ItemStack> drops = new ArrayList<>();
		ItemStack plant = VasePlantHelper.removePlant(displayed);
		drops.add(displayed);

		if (plant != null) {
			drops.add(plant);
		}

		return drops;
	}
}
