package zone.moddev.mc.ironagefurniture.api.surface;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.skysfurniturecore.api.surface.ShelfLayout;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.Descriptor;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;

public enum MaterialShelfDisplay {
	GEM(ShelfLayout.GEM_PLATTER, 3.2D, 2.6D, 1.45D),
	INGOT(ShelfLayout.INGOT_MODEL),
	NUGGET(ShelfLayout.NUGGET_MODEL),
	POWDER(ShelfLayout.INGREDIENT_BOWL_MODEL),
	TOOL(ShelfLayout.TOOL, 12.0D, 6.0D, 1.0D),
	SHEARS(ShelfLayout.COMPACT_TOOL, 9.0D, 6.0D, 1.0D),
	PROJECTILE(ShelfLayout.ARROW_STACKS, 12.0D, 2.0D, 1.0D),
	PART(ShelfLayout.FOOD_PAIR, 6.8D, 6.8D, 0.85D),
	HELMET(VanillaShelfDisplay.HELMET),
	CHESTPLATE(VanillaShelfDisplay.CHESTPLATE),
	LEGGINGS(VanillaShelfDisplay.LEGGINGS),
	BOOTS(VanillaShelfDisplay.BOOTS),
	HORSE_ARMOUR(ShelfLayout.HORSE_ARMOUR_MODEL),
	SHIELD(ShelfLayout.CENTRED_FOOD, 16.0D, 8.0D, 1.4D),
	DOOR(ShelfLayout.CENTRED_FOOD, 6.8D, 3.6D, 11.2D),
	BARS(ShelfLayout.CENTRED_FOOD, 11.2D, 3.0D, 8.5D),
	TRAPDOOR(ShelfLayout.CENTRED_FOOD, 7.8D, 7.8D, 2.2D),
	BLOCK(ShelfLayout.CENTRED_FOOD, 8.2D, 8.2D, 8.2D),
	BUCKET(ShelfLayout.BUCKET_MODEL);

	private final Descriptor descriptor;
	private final VanillaShelfDisplay armour;

	MaterialShelfDisplay(ShelfLayout layout, double width, double depth, double height) {
		this(ShelfLayout.model(layout, width, depth, height));
	}

	MaterialShelfDisplay(Descriptor model) {
		this.descriptor = model;
		this.armour = null;
	}

	MaterialShelfDisplay(VanillaShelfDisplay armour) {
		this.descriptor = armour.getShelfModel().withShelfLayout(ShelfLayout.CENTRED_FOOD);
		this.armour = armour;
	}

	public Descriptor getDescriptor() {
		return this.descriptor;
	}

	public VanillaShelfDisplay getArmourDisplay() {
		return this.armour;
	}

	public static MaterialShelfDisplay find(ItemStack stack) {
		if (stack == null || stack.stackSize <= 0) {
			return null;
		}

		if (getBucketContents(stack) != null) {
			return BUCKET;
		}

		ResourceLocation name = stack.getItem().getRegistryName();
		if (!isMaterialResource(name)) {
			return null;
		}

		if (stack.getItem() instanceof ItemArmor) {
			switch (((ItemArmor)stack.getItem()).armorType) {
			case HEAD: return HELMET;
			case CHEST: return CHESTPLATE;
			case LEGS: return LEGGINGS;
			case FEET: return BOOTS;
			default: return null;
			}
		}

		String path = name.getResourcePath();
		if ("human_detector".equals(path)) {
			return BLOCK;
		}

		int separator = path.indexOf('_');
		if (separator < 0) {
			return null;
		}

		switch (path.substring(separator + 1)) {
		case "gem":
		case "ingot": return isGemResource(name) ? GEM : INGOT;
		case "nugget": return NUGGET;
		case "powder":
		case "powder_alt":
		case "powder_dirty":
		case "smallpowder":
		case "smalldust":
		case "smallblend":
		case "blend": return POWDER;
		case "shard":
		case "crystal":
		case "clump":
		case "crushed":
		case "crushed_purified": return NUGGET;
		case "rod":
		case "axe":
		case "sword":
		case "pickaxe":
		case "shovel":
		case "hoe":
		case "scythe":
		case "bow":
		case "crossbow":
		case "fishing_rod":
		case "crackhammer": return TOOL;
		case "shears": return SHEARS;
		case "arrow":
		case "bolt": return PROJECTILE;
		case "shield": return SHIELD;
		case "horse_armor": return HORSE_ARMOUR;
		case "plate":
		case "dense_plate":
		case "casing":
		case "gear": return PART;
		case "door_item":
		case "door": return DOOR;
		case "bars": return BARS;
		case "pressure_plate":
		case "trapdoor": return TRAPDOOR;
		case "ore":
		case "wall":
		case "slab":
		case "stairs":
		case "lever":
		case "button":
		case "anvil":
		case "block": return BLOCK;
		default: return null;
		}
	}

	public static FluidStack getBucketContents(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof UniversalBucket)) {
			return null;
		}

		FluidStack fluid = FluidUtil.getFluidContained(stack);
		return fluid != null && isMaterialFluid(fluid.getFluid()) ? fluid : null;
	}

	public static boolean isMaterialFluid(Fluid fluid) {
		return fluid.getBlock() != null && isMaterialResource(fluid.getBlock().getRegistryName());
	}

	private static boolean isGemResource(ResourceLocation name) {
		return name != null && "basegems".equals(name.getResourceDomain());
	}

	public static boolean isMaterialResource(ResourceLocation name) {
		return name != null && ("basemetals".equals(name.getResourceDomain())
			|| "modernmetals".equals(name.getResourceDomain()) || "baseminerals".equals(name.getResourceDomain())
			|| isGemResource(name));
	}
}
