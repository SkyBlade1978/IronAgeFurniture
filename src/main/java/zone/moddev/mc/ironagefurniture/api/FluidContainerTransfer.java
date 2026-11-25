package zone.moddev.mc.ironagefurniture.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** Compatibility entry point for the shared vessel transfer helpers. */
@Deprecated
public final class FluidContainerTransfer {
	private FluidContainerTransfer() { }

	public static boolean isBucketInteractionItem(ItemStack stack) {
		return zone.moddev.mc.skysfurniturecore.api.vessel.FluidContainerTransfer.isBucketInteractionItem(stack);
	}

	public static boolean tryUseBucketWithTank(ItemStack stack, IFluidHandler tank, EntityPlayer player, EnumHand hand) {
		return zone.moddev.mc.skysfurniturecore.api.vessel.FluidContainerTransfer.tryUseBucketWithTank(stack, tank, player, hand);
	}
}
