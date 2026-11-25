package zone.moddev.mc.ironagefurniture.api.surface;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting;
import zone.moddev.mc.skysfurniturecore.api.surface.ShelfLayout;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry.Descriptor;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Category;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Vanilla items with dedicated models on modular shelves. */
public enum VanillaShelfDisplay {
	EGG(Items.EGG, ShelfLayout.PRODUCE_TRAY, 4.6D, 4.6D, 5.6D),
	RED_MUSHROOM(Item.getItemFromBlock(Blocks.RED_MUSHROOM), ShelfLayout.PRODUCE_TRAY, 5.6D, 5.6D, 4.8D),
	BROWN_MUSHROOM(Item.getItemFromBlock(Blocks.BROWN_MUSHROOM), ShelfLayout.PRODUCE_TRAY, 5.6D, 5.6D, 4.8D),
	WHEAT(Items.WHEAT, ShelfLayout.BUNDLE_TRAY, 5.8D, 9.2D, 3.4D),
	SUGAR_CANE(Items.REEDS, ShelfLayout.BUNDLE_TRAY, 5.8D, 9.2D, 2.5D),
	SUGAR(Items.SUGAR, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	COCOA_BEANS("pantry", EnumDyeColor.BROWN.getDyeDamage(), ShelfLayout.INGREDIENT_BOWL_MODEL, Items.DYE),
	WHEAT_SEEDS(Items.WHEAT_SEEDS, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	PUMPKIN_SEEDS(Items.PUMPKIN_SEEDS, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	MELON_SEEDS(Items.MELON_SEEDS, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	BEETROOT_SEEDS(Items.BEETROOT_SEEDS, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	MILK_BUCKET(Items.MILK_BUCKET, "pantry", ShelfLayout.BUCKET_MODEL),
	REDSTONE(Items.REDSTONE, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	GLOWSTONE_DUST(Items.GLOWSTONE_DUST, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	GUNPOWDER(Items.GUNPOWDER, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	BLAZE_POWDER(Items.BLAZE_POWDER, "pantry", ShelfLayout.INGREDIENT_BOWL_MODEL),
	NETHER_WART(Items.NETHER_WART, ShelfLayout.PRODUCE_TRAY, 5.2D, 4.8D, 3.6D),
	FERMENTED_SPIDER_EYE(Items.FERMENTED_SPIDER_EYE, ShelfLayout.PRODUCE_TRAY, 4.8D, 5.0D, 3.7D),
	MAGMA_CREAM(Items.MAGMA_CREAM, ShelfLayout.PRODUCE_TRAY, 5.2D, 5.2D, 4.2D),
	GHAST_TEAR(Items.GHAST_TEAR, ShelfLayout.PRODUCE_TRAY, 3.6D, 3.6D, 5.0D),
	SPECKLED_MELON(Items.SPECKLED_MELON, ShelfLayout.PRODUCE_TRAY, 7.4D, 5.0D, 4.32D),
	RABBIT_FOOT(Items.RABBIT_FOOT, ShelfLayout.BUNDLE_TRAY, 5.4D, 8.9D, 3.2D),
	IRON_INGOT(Items.IRON_INGOT, "materials", ShelfLayout.INGOT_MODEL),
	GOLD_INGOT(Items.GOLD_INGOT, "materials", ShelfLayout.INGOT_MODEL),
	GOLD_NUGGET(Items.GOLD_NUGGET, "materials", ShelfLayout.NUGGET_MODEL),
	COAL(Items.COAL, "materials", 0, ShelfLayout.PRODUCE_TRAY, 5.8D, 5.6D, 4.6D),
	CHARCOAL(Items.COAL, "materials", 1, ShelfLayout.PRODUCE_TRAY, 5.8D, 5.6D, 3.8D),
	DIAMOND(Items.DIAMOND, "materials", -1, ShelfLayout.PRODUCE_TRAY, 6.4D, 5.2D, 4.2D),
	EMERALD(Items.EMERALD, "materials", -1, ShelfLayout.PRODUCE_TRAY, 4.8D, 4.6D, 6.2D),
	QUARTZ(Items.QUARTZ, "materials", -1, ShelfLayout.PRODUCE_TRAY, 5.6D, 5.2D, 5.8D),
	BONE(Items.BONE, "materials", -1, ShelfLayout.CROSSWISE_TRAY, 3.56D, 12.40D, 1.98D),
	BLAZE_ROD(Items.BLAZE_ROD, "materials", -1, ShelfLayout.CROSSWISE_TRAY, 2.58D, 11.04D, 2.37D),
	FLINT(Items.FLINT, "materials", -1, ShelfLayout.PRODUCE_TRAY, 5.23D, 5.18D, 3.60D),
	CLAY_BALL(Items.CLAY_BALL, "materials", -1, ShelfLayout.PRODUCE_TRAY, 5.52D, 5.34D, 3.88D),
	SLIME_BALL(Items.SLIME_BALL, "materials", -1, ShelfLayout.PRODUCE_TRAY, 4.96D, 4.58D, 4.15D),
	SNOWBALL(Items.SNOWBALL, "materials", -1, ShelfLayout.PRODUCE_TRAY, 4.92D, 4.92D, 4.56D),
	PRISMARINE_SHARD(Items.PRISMARINE_SHARD, "materials", -1, ShelfLayout.PRODUCE_TRAY, 3.88D, 5.02D, 4.37D),
	PRISMARINE_CRYSTALS(Items.PRISMARINE_CRYSTALS, "materials", -1, ShelfLayout.PRODUCE_TRAY, 5.76D, 5.58D, 5.28D),
	POPPED_CHORUS_FRUIT(Items.CHORUS_FRUIT_POPPED, "materials", -1, ShelfLayout.PRODUCE_TRAY, 6.09D, 4.50D, 3.96D),
	ENDER_PEARL(Items.ENDER_PEARL, "magic", -1, ShelfLayout.PRODUCE_TRAY, 4.70D, 4.70D, 4.65D),
	EYE_OF_ENDER(Items.ENDER_EYE, "magic", -1, ShelfLayout.PRODUCE_TRAY, 4.68D, 5.30D, 5.12D),
	END_CRYSTAL(Items.END_CRYSTAL, "magic", -1, ShelfLayout.CENTRED_FOOD, 7.56D, 7.56D, 8.38D),
	NETHER_STAR(Items.NETHER_STAR, "magic", -1, ShelfLayout.CENTRED_FOOD, 8.78D, 2.80D, 8.88D),
	FIRE_CHARGE(Items.FIRE_CHARGE, "magic", -1, ShelfLayout.PRODUCE_TRAY, 4.72D, 4.78D, 4.27D),
	FIREWORK_ROCKET(Items.FIREWORKS, "magic", -1, ShelfLayout.CROSSWISE_TRAY, 2.90D, 13.08D, 2.81D),
	FIREWORK_STAR(Items.FIREWORK_CHARGE, "magic", -1, ShelfLayout.PRODUCE_TRAY, 4.88D, 4.74D, 4.34D),
	SPAWN_EGG(Items.SPAWN_EGG, "magic", -1, ShelfLayout.PRODUCE_TRAY, 4.52D, 4.44D, 5.88D),
	BOWL(Items.BOWL, "containers", -1, ShelfLayout.BOWL_STACK, 7.72D, 7.72D, 3.6D),
	BUCKET(Items.BUCKET, "containers", ShelfLayout.BUCKET_MODEL),
	WATER_BUCKET(Items.WATER_BUCKET, "containers", ShelfLayout.BUCKET_MODEL),
	LAVA_BUCKET(Items.LAVA_BUCKET, "containers", ShelfLayout.BUCKET_MODEL),
	LEATHER(Items.LEATHER, "crafting", -1, ShelfLayout.FOOD_PAIR, 10.9D, 10.4D, 3.06D),
	RABBIT_HIDE(Items.RABBIT_HIDE, "crafting", -1, ShelfLayout.FOOD_PAIR, 10.0D, 10.1D, 2.93D),
	STRING(Items.STRING, "crafting", -1, ShelfLayout.BUNDLE_TRAY, 5.2D, 6.1D, 6.04D),
	STICK(Items.STICK, "crafting", -1, ShelfLayout.BUNDLE_TRAY, 5.0D, 11.2D, 2.66D),
	FEATHER(Items.FEATHER, "crafting", -1, ShelfLayout.BUNDLE_TRAY, 5.19D, 10.55D, 0.48D),
	PAPER(Items.PAPER, "crafting", -1, ShelfLayout.COOKIE_STACKS, 9.31D, 9.18D, 0.61D),
	NAME_TAG(Items.NAME_TAG, "crafting", -1, ShelfLayout.CENTRED_FOOD, 12.18D, 5.2D, 4.9D),
	INK_SAC(Items.DYE, "dyes", EnumDyeColor.BLACK.getDyeDamage(), ShelfLayout.PRODUCE_TRAY, 4.64D, 4.9D, 4.15D),
	LAPIS_LAZULI(Items.DYE, "dyes", EnumDyeColor.BLUE.getDyeDamage(), ShelfLayout.PRODUCE_TRAY, 5.62D, 4.50D, 4.23D),
	ROSE_RED(EnumDyeColor.RED),
	CACTUS_GREEN(EnumDyeColor.GREEN),
	PURPLE_DYE(EnumDyeColor.PURPLE),
	CYAN_DYE(EnumDyeColor.CYAN),
	LIGHT_GRAY_DYE(EnumDyeColor.SILVER),
	GRAY_DYE(EnumDyeColor.GRAY),
	PINK_DYE(EnumDyeColor.PINK),
	LIME_DYE(EnumDyeColor.LIME),
	DANDELION_YELLOW(EnumDyeColor.YELLOW),
	LIGHT_BLUE_DYE(EnumDyeColor.LIGHT_BLUE),
	MAGENTA_DYE(EnumDyeColor.MAGENTA),
	ORANGE_DYE(EnumDyeColor.ORANGE),
	BONE_MEAL(EnumDyeColor.WHITE),
	CLOCK(Items.CLOCK, "travel", -1, ShelfLayout.CENTRED_FOOD, 9.4D, 7.4D, 7.72D),
	COMPASS(Items.COMPASS, "travel", -1, ShelfLayout.CENTRED_FOOD, 9.4D, 9.4D, 2.2D),
	MAP(Items.MAP, "travel", -1, ShelfLayout.BUNDLE_TRAY, 4.2D, 8.8D, 3.0D),
	FILLED_MAP(Items.FILLED_MAP, "travel", -1, ShelfLayout.CENTRED_FOOD, 9.4D, 7.4D, 7.5D),
	LEAD(Items.LEAD, "travel", -1, ShelfLayout.FOOD_PAIR, 7.3D, 6.7D, 1.8D),
	SADDLE(Items.SADDLE, "travel", -1, ShelfLayout.CENTRED_FOOD, 12.4D, 8.2D, 6.5D),
	HELMET("armour", -1, ShelfLayout.CENTRED_FOOD, 7.4D, 7.4D, 8.2D,
		Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET),
	CHESTPLATE("armour", -1, ShelfLayout.CENTRED_FOOD, 9.2D, 6.2D, 7.2D,
		Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE),
	LEGGINGS("armour", -1, ShelfLayout.CENTRED_FOOD, 6.8D, 8.2D, 7.6D,
		Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS),
	BOOTS("armour", -1, ShelfLayout.CENTRED_FOOD, 6.8D, 5.4D, 7.8D,
		Items.LEATHER_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS),
	CHAINMAIL_LEGGINGS(Items.CHAINMAIL_LEGGINGS, "armour", -1, ShelfLayout.CENTRED_FOOD, 9.8D, 8.2D, 8.55D),
	CHAINMAIL_BOOTS(Items.CHAINMAIL_BOOTS, "armour", -1, ShelfLayout.CENTRED_FOOD, 10.4D, 7.4D, 5.6D),
	IRON_HORSE_ARMOUR(Items.IRON_HORSE_ARMOR),
	GOLD_HORSE_ARMOUR(Items.GOLDEN_HORSE_ARMOR),
	DIAMOND_HORSE_ARMOUR(Items.DIAMOND_HORSE_ARMOR),
	ELYTRA(Items.ELYTRA, "armour", -1, ShelfLayout.CENTRED_FOOD, 8.13D, 5.4D, 9.46D),
	BANNER(Items.BANNER, "armour", -1, ShelfLayout.CENTRED_FOOD, 16.0D, 5.61D, 8.48D),
	ARMOUR_STAND(Items.ARMOR_STAND, "armour", -1, ShelfLayout.CENTRED_FOOD, 6.8D, 5.4D, 7.5D),
	DRAGON_HEAD(Items.SKULL, "armour", 5, ShelfLayout.CENTRED_FOOD, 5.8D, 9.6D, 7.8D),
	SKULL(Items.SKULL, "armour", -1, ShelfLayout.CENTRED_FOOD, 8.6D, 8.6D, 7.7D),
	OAK_BOAT(Items.BOAT, "transport", 14.0D, 8.4D, 3.4D),
	SPRUCE_BOAT(Items.SPRUCE_BOAT, "transport", 14.0D, 8.4D, 3.4D),
	BIRCH_BOAT(Items.BIRCH_BOAT, "transport", 14.0D, 8.4D, 3.4D),
	JUNGLE_BOAT(Items.JUNGLE_BOAT, "transport", 14.0D, 8.4D, 3.4D),
	ACACIA_BOAT(Items.ACACIA_BOAT, "transport", 14.0D, 8.4D, 3.4D),
	DARK_OAK_BOAT(Items.DARK_OAK_BOAT, "transport", 14.0D, 8.4D, 3.4D),
	MINECART(Items.MINECART, "transport", 10.2D, 7.6D, 4.6D),
	CHEST_MINECART(Items.CHEST_MINECART, "transport", 10.2D, 7.6D, 6.5D),
	FURNACE_MINECART(Items.FURNACE_MINECART, "transport", 10.2D, 7.6D, 6.7D),
	TNT_MINECART(Items.TNT_MINECART, "transport", 10.2D, 7.6D, 6.7D),
	HOPPER_MINECART(Items.HOPPER_MINECART, "transport", 10.2D, 7.6D, 6.5D),
	COMMAND_BLOCK_MINECART(Items.COMMAND_BLOCK_MINECART, "transport", 10.2D, 7.6D, 6.7D),
	PAINTING(Items.PAINTING, "decor", 10.4D, 5.6D, 8.6D),
	ITEM_FRAME(Items.ITEM_FRAME, "decor", 8.4D, 5.6D, 8.2D),
	BREWING_STAND(Items.BREWING_STAND, "utility", 8.8D, 6.4D, 8.1D),
	CAULDRON(Items.CAULDRON, "utility", 8.0D, 8.0D, 7.4D),
	BED(Items.BED, "utility", 13.8D, 7.64D, 4.6D),
	OAK_DOOR(Items.OAK_DOOR, "utility", 6.8D, 3.6D, 11.2D),
	SPRUCE_DOOR(Items.SPRUCE_DOOR, "utility", 6.8D, 3.6D, 11.2D),
	BIRCH_DOOR(Items.BIRCH_DOOR, "utility", 6.8D, 3.6D, 11.2D),
	JUNGLE_DOOR(Items.JUNGLE_DOOR, "utility", 6.8D, 3.6D, 11.2D),
	ACACIA_DOOR(Items.ACACIA_DOOR, "utility", 6.8D, 3.6D, 11.2D),
	DARK_OAK_DOOR(Items.DARK_OAK_DOOR, "utility", 6.8D, 3.6D, 11.2D),
	IRON_DOOR(Items.IRON_DOOR, "utility", 6.8D, 3.6D, 11.2D),
	REPEATER(Items.REPEATER, "utility", 10.2D, 6.6D, 4.1D),
	COMPARATOR(Items.COMPARATOR, "utility", 10.2D, 6.6D, 4.1D),
	BRICK(Items.BRICK, "objects", -1, ShelfLayout.INGOT_STACK, 7.4D, 4.8D, 2.4D),
	NETHERBRICK(Items.NETHERBRICK, "objects", -1, ShelfLayout.INGOT_STACK, 7.4D, 4.8D, 2.4D),
	TORCH(Blocks.TORCH, "objects", ShelfLayout.HARDWARE_ROW, 3.0D, 3.0D, 10.0D),
	REDSTONE_TORCH(Blocks.REDSTONE_TORCH, "objects", ShelfLayout.HARDWARE_ROW, 3.0D, 3.0D, 10.0D),
	LEVER(Blocks.LEVER, ShelfLayout.HARDWARE_ROW, 6.0D, 8.0D, 6.0D),
	TRIPWIRE_HOOK(Blocks.TRIPWIRE_HOOK, ShelfLayout.HARDWARE_ROW, 6.0D, 6.0D, 7.0D),
	LADDER(Blocks.LADDER, "objects", ShelfLayout.PANEL_STACK, 11.0D, 1.25D, 9.0D),
	IRON_BARS(Blocks.IRON_BARS, ShelfLayout.PANEL_STACK, 9.8D, 2.0D, 8.2D),
	HOPPER(Blocks.HOPPER, 8.0D, 8.0D, 8.0D),
	GLASS_PANE(Blocks.GLASS_PANE, ShelfLayout.PANEL_STACK, 9.8D, 2.0D, 8.2D),
	STAINED_GLASS_PANE(Blocks.STAINED_GLASS_PANE, ShelfLayout.PANEL_STACK, 9.8D, 2.0D, 8.2D),
	RAIL(Blocks.RAIL, "objects", ShelfLayout.FLAT_STACK, 12.0D, 6.4D, 1.5D),
	GOLDEN_RAIL(Blocks.GOLDEN_RAIL, "objects", ShelfLayout.FLAT_STACK, 12.0D, 6.4D, 1.5D),
	DETECTOR_RAIL(Blocks.DETECTOR_RAIL, "objects", ShelfLayout.FLAT_STACK, 12.0D, 6.4D, 1.5D),
	ACTIVATOR_RAIL(Blocks.ACTIVATOR_RAIL, "objects", ShelfLayout.FLAT_STACK, 12.0D, 6.4D, 1.5D),
	DEADBUSH(Blocks.DEADBUSH, ShelfLayout.CUTTINGS, 1.7D, 4.6D, 1.55D),
	DOUBLE_PLANT(Blocks.DOUBLE_PLANT, ShelfLayout.CUTTINGS, 1.9D, 6.0D, 1.85D),
	RED_FLOWER(Blocks.RED_FLOWER, ShelfLayout.CUTTINGS, 1.8D, 5.6D, 1.8D),
	YELLOW_FLOWER(Blocks.YELLOW_FLOWER, ShelfLayout.CUTTINGS, 1.8D, 5.6D, 1.7D),
	SAPLING(Blocks.SAPLING, ShelfLayout.CUTTINGS, 1.9D, 5.4D, 1.85D),
	TALLGRASS(Blocks.TALLGRASS, ShelfLayout.CUTTINGS, 1.85D, 5.3D, 1.6D),
	VINE(Blocks.VINE, ShelfLayout.CUTTINGS, 1.9D, 5.6D, 1.3D),
	WATERLILY(Blocks.WATERLILY, "objects", ShelfLayout.FLAT_STACK, 9.6D, 6.4D, 0.75D),
	WEB(Blocks.WEB, 8.0D, 6.4D, 8.0D),
	BARRIER(Blocks.BARRIER, "objects", 7.0D, 3.0D, 7.5D),
	STRUCTURE_VOID(Blocks.STRUCTURE_VOID, "objects", 7.0D, 3.0D, 7.5D);

	private static final Map<Item, List<VanillaShelfDisplay>> BY_ITEM = new IdentityHashMap<>();

	static {
		for (VanillaShelfDisplay display : values()) {
			for (Item item : display.items) {
				BY_ITEM.computeIfAbsent(item, key -> new ArrayList<>()).add(display);
			}
		}
	}

	private final Item[] items;
	private final int metadata;
	private final String modelPath;
	private final boolean shelfObject;
	private final Descriptor shelfModel;
	private final Descriptor surfaceDescriptor;

	VanillaShelfDisplay(Item horseArmour) {
		this(horseArmour, "armour", ShelfLayout.HORSE_ARMOUR_MODEL);
	}

	VanillaShelfDisplay(EnumDyeColor colour) {
		this("dyes", colour.getDyeDamage(), ShelfLayout.INGREDIENT_BOWL_MODEL, Items.DYE);
	}

	VanillaShelfDisplay(Item item, ShelfLayout layout, double width, double depth, double height) {
		this(item, "pantry", -1, layout, width, depth, height);
	}

	VanillaShelfDisplay(Item item, String folder, Descriptor model) {
		this(folder, -1, model, item);
	}

	VanillaShelfDisplay(Item item, String folder, double width, double depth, double height) {
		this(item, folder, -1, ShelfLayout.CENTRED_FOOD, width, depth, height);
	}

	VanillaShelfDisplay(Block block, double width, double depth, double height) {
		this(block, ShelfLayout.CENTRED_FOOD, width, depth, height);
	}

	VanillaShelfDisplay(Block block, String folder, double width, double depth, double height) {
		this(block, folder, ShelfLayout.CENTRED_FOOD, width, depth, height);
	}

	VanillaShelfDisplay(Block block, ShelfLayout layout, double width, double depth, double height) {
		this(block, null, layout, width, depth, height);
	}

	VanillaShelfDisplay(Block block, String folder, ShelfLayout layout, double width, double depth, double height) {
		this(Item.getItemFromBlock(block), folder, -1, layout, width, depth, height);
	}

	VanillaShelfDisplay(Item item, String folder, int metadata, ShelfLayout layout,
			double width, double depth, double height) {
		this(folder, metadata, layout, width, depth, height, item);
	}

	VanillaShelfDisplay(String folder, int metadata, ShelfLayout layout,
			double width, double depth, double height, Item... items) {
		this(folder, metadata, ShelfLayout.model(layout, width, depth, height), items);
	}

	VanillaShelfDisplay(String folder, int metadata, Descriptor model, Item... items) {
		this.items = items;
		this.metadata = metadata;
		this.modelPath = folder == null ? null : "shelving_media/" + folder + "/" + this.name().toLowerCase(Locale.ROOT);
		this.shelfObject = folder == null || "objects".equals(folder);
		this.shelfModel = model.withShelfLayout(ShelfLayout.NONE);
		// Keep generic furniture bounds and use the custom footprint only on modular shelves.
		Descriptor descriptor = new Descriptor(Category.SINGLETON, 0.5D, 0.75D).withShelfLayout(model.getShelfLayout());
		this.surfaceDescriptor = "pantry".equals(folder) && isProduceIngredient() ? descriptor.withProduceTray() : descriptor;
	}

	private boolean isProduceIngredient() {
		switch (this.name()) {
		case "REDSTONE":
		case "GLOWSTONE_DUST":
		case "GUNPOWDER":
		case "BLAZE_POWDER":
			return false;
		default:
			return true;
		}
	}

	public String getModelPath() {
		return this.modelPath;
	}

	public boolean usesBlockModel() {
		return this.modelPath == null;
	}

	public boolean isShelfObject() {
		return this.shelfObject;
	}

	public Descriptor getShelfModel() {
		return this.shelfModel;
	}

	public static VanillaShelfDisplay find(ItemStack stack) {
		if (stack == null) {
			return null;
		}

		if (stack.getItem() == Items.SKULL && (stack.getMetadata() < 0 || stack.getMetadata() > 5)) {
			return null;
		}

		List<VanillaShelfDisplay> variants = BY_ITEM.get(stack.getItem());
		if (variants != null) {
			for (VanillaShelfDisplay display : variants) {
				if (display.metadata < 0 || stack.getMetadata() == display.metadata) {
					return display;
				}
			}
		}

		return null;
	}

	public static void register() {
		for (Item item : BY_ITEM.keySet()) {
			SurfaceContentRegistry.register(item, stack -> {
				VanillaShelfDisplay display = find(stack);
				return display == null ? null : display.surfaceDescriptor;
			});
		}
	}
}
