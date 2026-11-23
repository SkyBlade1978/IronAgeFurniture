package zone.moddev.mc.ironagefurniture.fixture;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

final class MagicShelfSamples {
	static final int RED = 0xE34C4C;
	static final int BLUE = 0x30BDE8;

	private MagicShelfSamples() { }

	static ItemStack egg(String entity) {
		ItemStack stack = new ItemStack(Items.SPAWN_EGG);
		stack.getSubCompound("EntityTag", true).setString("id", entity);
		return stack;
	}

	static ItemStack star(int colour) {
		ItemStack stack = new ItemStack(Items.FIREWORK_CHARGE).setStackDisplayName("Festival star");
		NBTTagCompound explosion = stack.getSubCompound("Explosion", true);
		explosion.setIntArray("Colors", new int[] { colour });
		explosion.setIntArray("FadeColors", new int[] { 0xF2FAFF });
		explosion.setBoolean("Flicker", true);
		explosion.setByte("Type", (byte)2);
		return stack;
	}

	static ItemStack rocket(int flight) {
		ItemStack stack = new ItemStack(Items.FIREWORKS).setStackDisplayName("Festival rocket");
		NBTTagCompound fireworks = stack.getSubCompound("Fireworks", true);
		fireworks.setByte("Flight", (byte)flight);
		NBTTagList explosions = new NBTTagList();
		explosions.appendTag(star(RED).getSubCompound("Explosion", false));
		explosions.appendTag(star(BLUE).getSubCompound("Explosion", false));
		fireworks.setTag("Explosions", explosions);
		return stack;
	}

	static ItemStack alternateConfiguration(ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.SPAWN_EGG) {
			String entity = stack.getSubCompound("EntityTag", false).getString("id");
			return egg("Pig".equals(entity) ? "Creeper" : "Pig");
		}
		if (item == Items.FIREWORK_CHARGE) {
			int colour = stack.getSubCompound("Explosion", false).getIntArray("Colors")[0];
			return star(colour == RED ? BLUE : RED);
		}
		if (item == Items.FIREWORKS) {
			int flight = stack.getSubCompound("Fireworks", false).getByte("Flight");
			return rocket(flight == 1 ? 2 : 1);
		}

		return null;
	}
}
