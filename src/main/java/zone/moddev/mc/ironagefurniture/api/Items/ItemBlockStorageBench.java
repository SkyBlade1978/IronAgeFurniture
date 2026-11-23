package zone.moddev.mc.ironagefurniture.api.Items;

import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public final class ItemBlockStorageBench extends ItemBlock {
	public ItemBlockStorageBench(Block block) {
		super(block);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	public static boolean isPadded(ItemStack stack) {
		return stack != null && stack.getMetadata() >= 1 && stack.getMetadata() <= 16;
	}

	public static int colourMetadata(PaddedBenchColour colour) {
		return colour.getItemMetadata() + 1;
	}

	public static PaddedBenchColour getColour(ItemStack stack) {
		return PaddedBenchColour.byItemMetadata(stack.getMetadata() - 1);
	}

	public static ItemStack paddedStack(Block block, PaddedBenchColour colour) {
		return new ItemStack(block, 1, colourMetadata(colour));
	}

	@Override
	public int getMetadata(int damage) {
		// Facing and the padded flag are set by StorageBench when the block is placed.
		return 0;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String base = super.getItemStackDisplayName(stack);
		if (!isPadded(stack)) {
			return base;
		}

		String colour = I18n.translateToLocal("item.ironagefurniture.upholstery." + getColour(stack).getSerializedName());
		return I18n.translateToLocalFormatted("item.ironagefurniture.padded_storage_bench", base, colour);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(item));
		for (PaddedBenchColour colour : PaddedBenchColour.creativeOrder()) {
			subItems.add(paddedStack(this.block, colour));
		}
	}
}
