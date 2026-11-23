package zone.moddev.mc.ironagefurniture.api.Items;

import java.util.List;

import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ItemBlockCrate extends ItemBlock {
	public ItemBlockCrate(Block block) {
		super(block);
	}

	public static boolean isPacked(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemBlockCrate) || !stack.hasTagCompound()) {
			return false;
		}

		NBTTagCompound data = stack.getTagCompound().getCompoundTag("BlockEntityTag");
		return data.getBoolean(TileEntityCrate.SEALED_TAG) || data.getTagList("Items", 10).tagCount() > 0
			|| data.getTagList("ItemsUnresolved", 10).tagCount() > 0;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return isPacked(stack) ? 1 : super.getItemStackLimit(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if (isPacked(stack)) {
			tooltip.add(I18n.format("item.ironagefurniture.crate.packed"));
			NBTTagCompound data = stack.getTagCompound().getCompoundTag("BlockEntityTag");
			String label = data.getString("LabelText");
			if (!label.isEmpty()) {
				tooltip.add(label);
			}
		}
	}
}
