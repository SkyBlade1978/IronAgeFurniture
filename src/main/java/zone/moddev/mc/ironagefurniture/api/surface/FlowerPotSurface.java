package zone.moddev.mc.ironagefurniture.api.surface;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import zone.moddev.mc.ironagefurniture.api.VasePlantHelper;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

public final class FlowerPotSurface implements SurfaceInteractionRegistry.Handler {
	private static final String PLANT_TAG = "ShelfPlant";
	public static final double MODEL_SCALE = 0.56D;

	private FlowerPotSurface() {
	}

	public static void register() {
		SurfaceContentRegistry.register(Items.FLOWER_POT, stack -> {
			boolean planted = getPlant(stack) != null;
			double width = planted ? 10.8D : 6.0D;
			double height = planted ? 16.0D : 6.0D;
			return new SurfaceContentRegistry.Descriptor(SurfaceSetting.Category.SINGLETON,
				width / 16.0D * MODEL_SCALE, height / 16.0D * MODEL_SCALE);
		});
		SurfaceInteractionRegistry.register(Items.FLOWER_POT, new FlowerPotSurface());
	}

	public static ItemStack getPlant(ItemStack pot) {
		if (pot == null || !pot.hasTagCompound()) {
			return null;
		}

		ItemStack plant = ItemStack.loadItemStackFromNBT(pot.getTagCompound().getCompoundTag(PLANT_TAG));
		return VasePlantHelper.isPlantItem(plant) ? plant : null;
	}

	private static void clearPlant(ItemStack pot) {
		NBTTagCompound tag = pot.getTagCompound();
		tag.removeTag(PLANT_TAG);

		if (tag.hasNoTags()) {
			pot.setTagCompound(null);
		}
	}

	@Override
	public boolean canHandle(ItemStack displayed, ItemStack held) {
		return getPlant(displayed) != null ? held == null : VasePlantHelper.isPlantItem(held);
	}

	@Override
	public void handle(SurfaceSettingHost host, Slot slot, EntityPlayer player, EnumHand hand, ItemStack held) {
		ItemStack pot = host.getSurfaceSetting().getItem(slot);
		ItemStack plant = getPlant(pot);

		if (plant != null) {
			if (!player.inventory.addItemStackToInventory(plant)) {
				return;
			}
			clearPlant(pot);
		} else {
			NBTTagCompound tag = pot.hasTagCompound() ? pot.getTagCompound() : new NBTTagCompound();
			tag.setTag(PLANT_TAG, VasePlantHelper.copyOne(held).writeToNBT(new NBTTagCompound()));
			pot.setTagCompound(tag);

			if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) {
				player.setHeldItem(hand, null);
			}
		}

		host.markSurfaceSettingChanged();
	}

	@Override
	public List<ItemStack> getDrops(ItemStack displayed) {
		ItemStack plant = getPlant(displayed);
		if (plant == null) {
			return Collections.singletonList(displayed);
		}

		clearPlant(displayed);
		return Arrays.asList(displayed, plant);
	}
}
