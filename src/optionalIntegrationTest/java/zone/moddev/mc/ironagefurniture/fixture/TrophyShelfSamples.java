package zone.moddev.mc.ironagefurniture.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;

final class TrophyShelfSamples {
	private TrophyShelfSamples() { }

	static List<ItemStack> all() {
		Item[] armour = {
			Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
			Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
			Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
			Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
			Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS
		};
		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : armour) {
			stacks.add(armour(item));
		}
		for (int type = 0; type < 6; type++) {
			stacks.add(head(type));
		}
		stacks.add(stand());
		stacks.add(new ItemStack(Items.IRON_HORSE_ARMOR).setStackDisplayName("Stable guard"));
		stacks.add(new ItemStack(Items.GOLDEN_HORSE_ARMOR));
		stacks.add(new ItemStack(Items.DIAMOND_HORSE_ARMOR));
		stacks.add(elytra());

		for (EnumDyeColor colour : EnumDyeColor.values()) {
			stacks.add(banner(colour));
		}

		stacks.add(patternedBanner(EnumDyeColor.BLUE));
		return stacks;
	}

	static ItemStack elytra() {
		ItemStack stack = new ItemStack(Items.ELYTRA, 1, 83).setStackDisplayName("Weathered wings");
		stack.addEnchantment(Enchantments.UNBREAKING, 3);
		return stack;
	}

	static ItemStack banner(EnumDyeColor colour) {
		return new ItemStack(Items.BANNER, 1, colour.getDyeDamage());
	}

	static ItemStack patternedBanner(EnumDyeColor colour) {
		ItemStack stack = banner(colour).setStackDisplayName("Stable standard");
		NBTTagList patterns = new NBTTagList();
		patterns.appendTag(pattern("ls", EnumDyeColor.CYAN));
		patterns.appendTag(pattern("cr", EnumDyeColor.WHITE));
		patterns.appendTag(pattern("bo", EnumDyeColor.RED));
		stack.getSubCompound("BlockEntityTag", true).setTag("Patterns", patterns);
		return stack;
	}

	private static NBTTagCompound pattern(String id, EnumDyeColor colour) {
		NBTTagCompound pattern = new NBTTagCompound();
		pattern.setString("Pattern", id);
		pattern.setInteger("Color", colour.getDyeDamage());
		return pattern;
	}

	static ItemStack armour(Item item) {
		ItemStack stack = new ItemStack(item, 1, 7).setStackDisplayName("Keeper's armour");
		ItemArmor armour = (ItemArmor)item;
		if (armour.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
			armour.setColor(stack, 0x247DA4);
		}
		if (item == Items.DIAMOND_CHESTPLATE || item == Items.DIAMOND_BOOTS) {
			stack.addEnchantment(Enchantments.PROTECTION, 2);
		}
		return stack;
	}

	static ItemStack head(int type) {
		ItemStack stack = new ItemStack(Items.SKULL, 1, type).setStackDisplayName("Collected head " + type);
		if (type == 3) {
			GameProfile owner = new GameProfile(UUID.fromString("0e281a6f-4d7b-42d5-b75c-b69ca4e89d95"), "ShelfTester");
			stack.setTagInfo("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), owner));
		}
		return stack;
	}

	static ItemStack stand() {
		ItemStack stack = new ItemStack(Items.ARMOR_STAND).setStackDisplayName("Keeper's stand");
		NBTTagCompound data = new NBTTagCompound();
		data.setBoolean("ShowArms", true);
		data.setBoolean("Small", true);
		stack.setTagInfo("EntityTag", data);
		return stack;
	}
}
