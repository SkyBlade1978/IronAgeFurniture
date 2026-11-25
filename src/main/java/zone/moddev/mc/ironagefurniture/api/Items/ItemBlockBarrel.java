package zone.moddev.mc.ironagefurniture.api.Items;

import java.util.List;
import zone.moddev.mc.ironagefurniture.api.Blocks.Barrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockBarrel extends ItemBlock {
	public ItemBlockBarrel(Block block) { super(block); }
	@Override public int getItemStackLimit(ItemStack stack) {
		FluidStack fluid = TileEntityBarrel.getFluidFromItemStack(stack);
		return TileEntityBarrel.hasBatchData(stack) || fluid != null && fluid.amount > 0
			? 1 : ((Barrel)block).getEmptyItemStackLimit();
	}
	@Override @SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		FluidStack fluid = TileEntityBarrel.getFluidFromItemStack(stack);
		if (fluid != null && fluid.amount > 0) {
			tooltip.add(fluid.getLocalizedName());
			tooltip.add(fluid.amount + " / " + ((Barrel)block).getBarrelCapacity() + " mB");
		}
	}
}
