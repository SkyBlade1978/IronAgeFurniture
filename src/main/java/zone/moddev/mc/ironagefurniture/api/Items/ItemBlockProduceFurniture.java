package zone.moddev.mc.ironagefurniture.api.Items;

import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.tile.ProduceTray;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityProduceFurniture;
import zone.moddev.mc.ironagefurniture.init.WoodVariantHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ItemBlockProduceFurniture extends ItemBlock {
	public ItemBlockProduceFurniture(ProduceFurniture block) {
		super(block);
	}

	public static boolean isTray(ItemStack stack) {
		return stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemBlockProduceFurniture
			&& !((ItemBlockProduceFurniture)stack.getItem()).isRack();
	}

	public boolean isRack() {
		return ((ProduceFurniture)this.block).isRack();
	}

	public static ProduceTray unpack(ItemStack stack) {
		return ProduceTray.read(stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound());
	}

	public static ItemStack pack(Block block, ProduceTray tray) {
		return variant(block, tray.write());
	}

	public static ItemStack empty(Block block, String wood, int count) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString(ProduceTray.WOOD_TAG, wood);
		ItemStack stack = variant(block, tag);
		stack.stackSize = count;
		return stack;
	}

	private static ItemStack variant(Block block, NBTTagCompound tag) {
		ItemStack stack = new ItemStack(block);
		stack.setTagCompound(tag);
		return stack;
	}

	public static String wood(ItemStack stack) {
		String wood = stack.hasTagCompound() ? stack.getTagCompound().getString(ProduceTray.WOOD_TAG) : "";
		return wood.isEmpty() ? ProduceTray.DEFAULT_WOOD : wood;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey(ProduceTray.CONTENTS_TAG, 10)
			? 1 : super.getItemStackLimit(stack);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String wood = wood(stack);
		return super.getUnlocalizedName(stack) + "." + (WoodVariantHelper.getEnabledWoodSuffixes().contains(wood)
			? wood : ProduceTray.DEFAULT_WOOD);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> stacks) {
		for (String wood : WoodVariantHelper.getEnabledWoodSuffixes()) {
			stacks.add(empty(this.block, wood, 1));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		ProduceTray tray = unpack(stack);
		ItemStack contents = tray.getContents();
		if (contents != null) {
			tooltip.add(contents.stackSize + " × " + contents.getDisplayName());
		} else if (tray.isPacked()) {
			tooltip.add(net.minecraft.client.resources.I18n.format("item.ironagefurniture.produce_tray.unavailable"));
		}
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing side, float x, float y, float z) {
		EnumActionResult result = super.onItemUse(stack, player, world, pos, hand, side, x, y, z);
		if (result == EnumActionResult.SUCCESS && player.capabilities.isCreativeMode && !this.isRack()
				&& stack.hasTagCompound()) {
			// The Creative interaction restores stack count after placement. Transfer the saved contents only once.
			stack.getTagCompound().removeTag(ProduceTray.CONTENTS_TAG);
		}
		return result;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
			float x, float y, float z, IBlockState state) {
		ProduceFurniture furniture = (ProduceFurniture)this.block;
		BlockPos upper = pos.up();
		if (!furniture.canPlaceBlockAt(world, pos) || this.isRack() && (!world.isBlockModifiable(player, upper)
				|| !player.canPlayerEdit(upper, side, stack))) {
			return false;
		}
		if (!super.placeBlockAt(stack, player, world, pos, side, x, y, z, state)) {
			return false;
		}

		TileEntityProduceFurniture owner = furniture.getOwner(world, pos, state);
		if (owner == null) {
			return false;
		}
		if (this.isRack()) {
			if (!world.setBlockState(upper, state.withProperty(ProduceFurniture.SECOND, true), 3)) {
				world.setBlockToAir(pos);
				return false;
			}
			owner.setWood(wood(stack));
		} else {
			owner.setTray(0, unpack(stack));
		}
		return true;
	}
}
