package zone.moddev.mc.ironagefurniture.api.Items;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockRack extends ItemBlock {
	public ItemBlockRack(Block block) {
		super(block);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
			EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ) {
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock() instanceof RackBlock && ((RackBlock)state.getBlock()).isRackFamily(this.block)
				&& ((RackBlock)state.getBlock()).tryPlaceRackFromRackClick(world, pos, state, player, hand, stack,
					face, hitX, hitY, hitZ)) {
			return EnumActionResult.SUCCESS;
		}

		return super.onItemUse(stack, player, world, pos, hand, face, hitX, hitY, hitZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player,
			ItemStack stack) {
		Block clicked = world.getBlockState(pos).getBlock();

		if (clicked instanceof RackBlock && ((RackBlock)clicked).isRackFamily(this.block)) {
			return true;
		}

		return super.canPlaceBlockOnSide(world, pos, side, player, stack);
	}
}
