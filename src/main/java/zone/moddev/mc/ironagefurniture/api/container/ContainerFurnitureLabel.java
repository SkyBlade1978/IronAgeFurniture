package zone.moddev.mc.ironagefurniture.api.container;

import java.util.function.BiPredicate;

import zone.moddev.mc.ironagefurniture.api.tile.FurnitureLabel;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerFurnitureLabel extends Container {
	private final World world;
	private final BlockPos pos;
	private final Block block;
	private final TileEntity tile;
	private final FurnitureLabel label;
	private final int revision;
	private final String text;
	private final EnumHand hand;
	private final BiPredicate<ItemStack, String> writeLabel;

	public ContainerFurnitureLabel(World world, BlockPos pos, Block block, TileEntity tile, FurnitureLabel label,
			EnumHand hand, BiPredicate<ItemStack, String> writeLabel) {
		this.world = world;
		this.pos = pos;
		this.block = block;
		this.tile = tile;
		this.label = label;
		this.revision = label == null ? 0 : label.getRevision();
		this.text = label == null ? "" : label.getText();
		this.hand = hand;
		this.writeLabel = writeLabel;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return !player.isSpectator() && player.capabilities.allowEdit && player.getEntityWorld() == this.world
			&& this.world.isBlockLoaded(this.pos)
			&& player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D
			&& this.world.getBlockState(this.pos).getBlock() == this.block
			&& this.world.getTileEntity(this.pos) == this.tile && this.world.isBlockModifiable(player, this.pos);
	}

	public boolean save(EntityPlayer player, String updatedText) {
		if (this.world.isRemote || !this.canInteractWith(player)
				|| (this.label != null && this.label.getRevision() != this.revision)) {
			return false;
		}

		ItemStack fitted = this.label == null ? null : this.label.getSign();
		ItemStack held = player.getHeldItem(this.hand);
		ItemStack sign = fitted == null ? held : fitted;
		if (!FurnitureLabel.isSign(sign) || !this.writeLabel.test(sign, updatedText)) {
			return false;
		}

		if (fitted == null && !player.capabilities.isCreativeMode && --held.stackSize == 0) {
			player.setHeldItem(this.hand, null);
		}

		return true;
	}
}
