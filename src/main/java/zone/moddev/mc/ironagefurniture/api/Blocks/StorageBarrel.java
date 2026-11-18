package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.FluidVesselInteractions;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;

/** Craftable ordinary fluid barrel; Brewing continues to own its specialised vessels. */
public class StorageBarrel extends Barrel {
	public StorageBarrel(Material material, String name, float resistance, float hardness) {
		super(material, name, resistance, hardness);
	}

	@Override
	protected boolean openVesselGui(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!world.isRemote) {
			player.openGui(Ironagefurniture.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	@Override
	protected boolean isSpecialFluidItem(ItemStack stack) {
		return FluidVesselInteractions.find(stack) != null || (stack != null && FluidUtil.getFluidHandler(stack) != null);
	}

	@Override
	protected boolean trySpecialFluidInteraction(TileEntityBarrel barrel, EntityPlayer player, EnumHand hand, ItemStack stack) {
		FluidVesselInteractions.Handler handler = FluidVesselInteractions.find(stack);

		if (handler != null) {
			handler.interact(barrel, player, hand, stack);
			return true;
		}

		if (hand == EnumHand.MAIN_HAND && FluidUtil.getFluidHandler(stack) != null) {
			FluidUtil.interactWithFluidHandler(stack, barrel.getFluidHandler(), player);
			barrel.markForFluidUpdate();
			return true;
		}

		return false;
	}
}
