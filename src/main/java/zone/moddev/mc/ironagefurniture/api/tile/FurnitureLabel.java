package zone.moddev.mc.ironagefurniture.api.tile;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

/** A fitted sign retains its exact item data and a single editable line. */
public final class FurnitureLabel {
	public static final int MAX_LENGTH = 18;
	public static final AxisAlignedBB BOARD = new AxisAlignedBB(-5.5D / 16, 0, -0.5D / 16,
		5.5D / 16, 1.75D / 16, 0.25D / 16);

	private ItemStack sign;
	private String text = "";
	private int revision;

	public static boolean isSign(ItemStack stack) {
		return stack != null && stack.stackSize > 0 && stack.getItem() == Items.SIGN;
	}

	public static AxisAlignedBB bounds(Vec3d anchor, EnumFacing face, double scale) {
		double width = BOARD.maxX * scale;
		double top = anchor.yCoord + BOARD.maxY * scale;
		double front = -BOARD.minZ * scale;
		double back = -BOARD.maxZ * scale;
		int x = face.getFrontOffsetX();
		int z = face.getFrontOffsetZ();
		return x == 0
			? new AxisAlignedBB(anchor.xCoord - width, anchor.yCoord, anchor.zCoord + z * back,
				anchor.xCoord + width, top, anchor.zCoord + z * front)
			: new AxisAlignedBB(anchor.xCoord + x * back, anchor.yCoord, anchor.zCoord - width,
				anchor.xCoord + x * front, top, anchor.zCoord + width);
	}

	/** Treat the fitted sign as read-only. */
	public ItemStack getSign() {
		return this.sign;
	}

	public String getText() {
		return this.text;
	}

	public int getRevision() {
		return this.revision;
	}

	public void set(ItemStack sign, String text) {
		this.sign = isSign(sign) ? sign.copy() : null;
		if (this.sign != null) {
			this.sign.stackSize = 1;
		}

		String line = ChatAllowedCharacters.filterAllowedCharacters(text);
		this.text = this.sign == null ? "" : line.substring(0, Math.min(line.length(), MAX_LENGTH));
		this.revision++;
	}

	public ItemStack remove() {
		ItemStack removed = this.sign;
		this.set(null, "");
		return removed;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.set(ItemStack.loadItemStackFromNBT(compound.getCompoundTag("LabelSign")), compound.getString("LabelText"));
	}

	public void writeToNBT(NBTTagCompound compound) {
		if (this.sign != null) {
			compound.setTag("LabelSign", this.sign.writeToNBT(new NBTTagCompound()));
			compound.setString("LabelText", this.text);
		} else {
			compound.removeTag("LabelSign");
			compound.removeTag("LabelText");
		}
	}
}
