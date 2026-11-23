package zone.moddev.mc.ironagefurniture.fixture;

import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.surface.MaterialShelfDisplay;
import zone.moddev.mc.ironagefurniture.api.container.ContainerShelfLabel;
import zone.moddev.mc.ironagefurniture.api.surface.FlowerPotSurface;
import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceInteractionRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.IFluidBlock;

final class ShelvingPantryProbe {
	private ShelvingPantryProbe() { }

	static ItemStack enchantedAxe() {
		ItemStack axe = new ItemStack(Items.DIAMOND_AXE, 1, 37).setStackDisplayName("My display axe");
		axe.addEnchantment(Enchantments.EFFICIENCY, 3);
		return axe;
	}

	static ItemStack enchantedSword() {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD, 1, 37).setStackDisplayName("Workshop sword");
		sword.addEnchantment(Enchantments.SHARPNESS, 3);
		return sword;
	}

	static ItemStack patternedShield() {
		ItemStack shield = new ItemStack(Items.SHIELD, 1, 18).setStackDisplayName("Workshop shield");
		NBTTagCompound banner = shield.getSubCompound("BlockEntityTag", true);
		banner.setInteger("Base", EnumDyeColor.BLUE.getDyeDamage());
		NBTTagCompound cross = new NBTTagCompound();
		cross.setString("Pattern", "cr");
		cross.setInteger("Color", EnumDyeColor.WHITE.getDyeDamage());
		NBTTagList patterns = new NBTTagList();
		patterns.appendTag(cross);
		banner.setTag("Patterns", patterns);
		return shield;
	}

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		ItemStack[] samples = {
			new ItemStack(Items.BREAD).setStackDisplayName("Baker's loaf"),
			new ItemStack(Items.APPLE), new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.GOLDEN_APPLE, 1, 1),
			new ItemStack(Items.POTATO), new ItemStack(Items.BAKED_POTATO), new ItemStack(Items.BEETROOT),
			new ItemStack(Items.CARROT), new ItemStack(Items.GOLDEN_CARROT), new ItemStack(Items.COOKIE),
			enchantedAxe(), new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.GOLDEN_SHOVEL),
			new ItemStack(Items.IRON_HOE), new ItemStack(OptionalIntegrationRecipeProbe.SHELF_LAYOUT_SAMPLE),
			new ItemStack(Items.MELON), new ItemStack(Items.PUMPKIN_PIE).setStackDisplayName("Harvest pie"),
			new ItemStack(Items.MUSHROOM_STEW), new ItemStack(Items.RABBIT_STEW),
			new ItemStack(Items.BEETROOT_SOUP), new ItemStack(Items.CAKE),
			new ItemStack(Items.BEEF), new ItemStack(Items.COOKED_BEEF).setStackDisplayName("Butcher's steak"),
			new ItemStack(Items.PORKCHOP), new ItemStack(Items.COOKED_PORKCHOP),
			new ItemStack(Items.MUTTON), new ItemStack(Items.COOKED_MUTTON),
			new ItemStack(Items.CHICKEN), new ItemStack(Items.COOKED_CHICKEN),
			new ItemStack(Items.RABBIT), new ItemStack(Items.COOKED_RABBIT),
			new ItemStack(Items.FISH, 1, 0), new ItemStack(Items.FISH, 1, 1),
			new ItemStack(Items.FISH, 1, 2), new ItemStack(Items.FISH, 1, 3),
			new ItemStack(Items.COOKED_FISH, 1, 0), new ItemStack(Items.COOKED_FISH, 1, 1),
			new ItemStack(Items.WOODEN_SWORD), new ItemStack(Items.STONE_SWORD),
			new ItemStack(Items.IRON_SWORD), new ItemStack(Items.GOLDEN_SWORD), enchantedSword(),
			new ItemStack(Items.BOW), new ItemStack(Items.FISHING_ROD), new ItemStack(Items.CARROT_ON_A_STICK),
			new ItemStack(Items.SHEARS), new ItemStack(Items.FLINT_AND_STEEL),
			new ItemStack(Items.SHIELD), patternedShield(),
			new ItemStack(Items.ARROW), new ItemStack(Items.SPECTRAL_ARROW),
			PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW), PotionTypes.HEALING),
			PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW), PotionTypes.POISON),
			new ItemStack(Items.POISONOUS_POTATO), new ItemStack(Items.SPIDER_EYE),
			new ItemStack(Items.CHORUS_FRUIT), new ItemStack(Items.ROTTEN_FLESH),
			new ItemStack(Items.EGG), new ItemStack(Blocks.RED_MUSHROOM), new ItemStack(Blocks.BROWN_MUSHROOM),
			new ItemStack(Items.WHEAT), new ItemStack(Items.REEDS), new ItemStack(Items.SUGAR),
			new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage()), new ItemStack(Items.WHEAT_SEEDS),
			new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(Items.MELON_SEEDS), new ItemStack(Items.BEETROOT_SEEDS),
			new ItemStack(Items.MILK_BUCKET).setStackDisplayName("Dairy milk"),
			new ItemStack(Items.REDSTONE).setStackDisplayName("Fine redstone dust"),
			new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.GUNPOWDER), new ItemStack(Items.BLAZE_POWDER),
			new ItemStack(Items.NETHER_WART), new ItemStack(Items.FERMENTED_SPIDER_EYE),
			new ItemStack(Items.MAGMA_CREAM), new ItemStack(Items.GHAST_TEAR),
			new ItemStack(Items.SPECKLED_MELON), new ItemStack(Items.RABBIT_FOOT),
			new ItemStack(Items.IRON_INGOT).setStackDisplayName("Smith's iron"),
			new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_NUGGET),
			new ItemStack(Items.COAL, 1, 0), new ItemStack(Items.COAL, 1, 1),
			new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD), new ItemStack(Items.QUARTZ),
			new ItemStack(Items.BOWL).setStackDisplayName("Carved wooden bowl"),
			new ItemStack(Items.BUCKET), new ItemStack(Items.WATER_BUCKET).setStackDisplayName("Well water"),
			new ItemStack(Items.LAVA_BUCKET),
			new ItemStack(Items.CLOCK), new ItemStack(Items.COMPASS), new ItemStack(Items.MAP),
			new ItemStack(Items.FILLED_MAP, 1, 37).setStackDisplayName("Survey map"),
			new ItemStack(Items.LEAD), new ItemStack(Items.SADDLE),
			new ItemStack(Items.LEATHER), new ItemStack(Items.RABBIT_HIDE), new ItemStack(Items.STRING),
			new ItemStack(Items.STICK), new ItemStack(Items.FEATHER), new ItemStack(Items.PAPER),
			new ItemStack(Items.NAME_TAG).setStackDisplayName("Stable keeper"),
			new ItemStack(Items.BONE), new ItemStack(Items.BLAZE_ROD).setStackDisplayName("Forge ember"),
			new ItemStack(Items.FLINT), new ItemStack(Items.CLAY_BALL), new ItemStack(Items.SLIME_BALL),
			new ItemStack(Items.SNOWBALL), new ItemStack(Items.PRISMARINE_SHARD), new ItemStack(Items.PRISMARINE_CRYSTALS),
			new ItemStack(Items.CHORUS_FRUIT_POPPED),
			new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.END_CRYSTAL),
			new ItemStack(Items.NETHER_STAR), new ItemStack(Items.FIRE_CHARGE),
			MagicShelfSamples.rocket(1), MagicShelfSamples.rocket(2),
			MagicShelfSamples.star(MagicShelfSamples.RED), MagicShelfSamples.star(MagicShelfSamples.BLUE),
			MagicShelfSamples.egg("Creeper"), MagicShelfSamples.egg("Pig")
		};
		int[] capacities = { 5, 9, 9, 9, 6, 6, 6, 15, 15, 6, 1, 1, 1, 1, 6, 6, 2, 2, 2, 2, 1,
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 6, 6, 6, 6,
			6, 6, 6, 2, 6, 6, 6, 3, 3, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 6, 6, 6, 6, 6, 3,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 2, 2, 2,
			1, 1, 3, 1, 2, 1,
			2, 2, 3, 3, 3, 6, 1,
			3, 3, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 1, 1, 6, 3, 3, 6, 6, 6, 6 };
		require(SurfaceContentRegistry.describe(new ItemStack(Items.FISH, 1, 9)).getShelfLayout() == ShelfLayout.NONE,
			"Unsupported fish metadata was assigned a fish tray");
		EnumSet<VanillaShelfDisplay> dyeModels = EnumSet.noneOf(VanillaShelfDisplay.class);
		for (EnumDyeColor colour : EnumDyeColor.values()) {
			ItemStack dye = new ItemStack(Items.DYE, 1, colour.getDyeDamage());
			VanillaShelfDisplay model = VanillaShelfDisplay.find(dye);
			require(model != null && dyeModels.add(model)
				&& SurfaceContentRegistry.describe(dye).getShelfLayout() == expectedDyeLayout(colour),
				"Dye did not receive its own model and expected layout: " + colour);
			require((model == VanillaShelfDisplay.COCOA_BEANS) == (colour == EnumDyeColor.BROWN),
				"An unrelated dye received the cocoa model");
		}
		ItemStack unsupportedDye = new ItemStack(Items.DYE, 1, 16);
		require(VanillaShelfDisplay.find(unsupportedDye) == null
			&& SurfaceContentRegistry.describe(unsupportedDye).getShelfLayout() == ShelfLayout.NONE,
			"Unsupported dye metadata was assigned a shelf model");

		require(VanillaShelfDisplay.find(new ItemStack(Items.COAL, 1, 0)) == VanillaShelfDisplay.COAL
			&& VanillaShelfDisplay.find(new ItemStack(Items.COAL, 1, 1)) == VanillaShelfDisplay.CHARCOAL,
			"Coal and charcoal were not mapped to distinct models");
		ItemStack unsupportedCoal = new ItemStack(Items.COAL, 1, 2);
		require(VanillaShelfDisplay.find(unsupportedCoal) == null
			&& SurfaceContentRegistry.describe(unsupportedCoal).getShelfLayout() == ShelfLayout.NONE,
			"Unsupported coal metadata was assigned a fuel tray");

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			for (boolean standing : new boolean[] { false, true }) {
				BlockPos area = origin.south(6 * (2 * facing.getHorizontalIndex() + (standing ? 1 : 0)));
				IBlockState state = shelf.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
					.withProperty(RackBlock.STANDING, standing);
				for (int index = 0; index < samples.length; index++) {
					BlockPos pos = area.east(4 * index);
					world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
					world.setBlockState(pos, state);
					verifyDisplay(world, player, pos, (ShelvingUnit)shelf, state, samples[index], capacities[index]);
				}
				int index = samples.length;
				for (EnumDyeColor colour : EnumDyeColor.values()) {
					if (colour == EnumDyeColor.BROWN) {
						continue;
					}

					ItemStack dye = new ItemStack(Items.DYE, 1, colour.getDyeDamage());
					if (colour == EnumDyeColor.WHITE) {
						dye.setStackDisplayName("Ground bone meal");
					}

					BlockPos pos = area.east(4 * index++);
					world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
					world.setBlockState(pos, state);
					verifyDisplay(world, player, pos, (ShelvingUnit)shelf, state, dye, expectedDyeLayout(colour).getCapacity());
				}

				for (ItemStack trophy : TrophyShelfSamples.all()) {
					BlockPos pos = area.east(4 * index++);
					world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
					world.setBlockState(pos, state);
					verifyDisplay(world, player, pos, (ShelvingUnit)shelf, state, trophy, 1);
				}

				Item[] singleDisplays = {
					Items.BOAT, Items.SPRUCE_BOAT, Items.BIRCH_BOAT, Items.JUNGLE_BOAT, Items.ACACIA_BOAT, Items.DARK_OAK_BOAT,
					Items.MINECART, Items.CHEST_MINECART, Items.FURNACE_MINECART, Items.TNT_MINECART, Items.HOPPER_MINECART,
					Items.COMMAND_BLOCK_MINECART, Items.PAINTING, Items.ITEM_FRAME,
					Items.BREWING_STAND, Items.CAULDRON, Items.BED, Items.REPEATER, Items.COMPARATOR,
					Items.OAK_DOOR, Items.SPRUCE_DOOR, Items.BIRCH_DOOR, Items.JUNGLE_DOOR, Items.ACACIA_DOOR,
					Items.DARK_OAK_DOOR, Items.IRON_DOOR
				};
				for (Item item : singleDisplays) {
					BlockPos pos = area.east(4 * index++);
					world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
					world.setBlockState(pos, state);
					verifyDisplay(world, player, pos, (ShelvingUnit)shelf, state, new ItemStack(item).setStackDisplayName("Display sample"), 1);
				}
			}
		}

		verifyMixedStrikers(world, player, origin.south(54), shelf);
		verifyVanillaTravelUse(world, player, origin.south(59));
		verifyVanillaDyeUse(world, player, origin.south(64));
		verifyVanillaMountedGearUse(world, player, origin.south(69));
		verifyLabelsAndPots(world, player, origin.south(75), (ShelvingUnit)shelf);
		verifyLabelTargets(world, origin.south(80), shelf);
		verifyDisplayLighting(world, player, origin.south(87), shelf);
		if (net.minecraftforge.fml.common.Loader.isModLoaded("basemetals")) {
			verifyBaseMetals(world, player, origin.south(113), shelf);
		}
		if (net.minecraftforge.fml.common.Loader.isModLoaded("modernmetals")) {
			verifyModernMetals(world, player, origin.south(160), shelf);
		}
		if (net.minecraftforge.fml.common.Loader.isModLoaded("basegems")) {
			verifyBaseGems(world, player, origin.south(207), shelf);
		}
		if (net.minecraftforge.fml.common.Loader.isModLoaded("baseminerals")) {
			verifyBaseMinerals(world, player, origin.south(295), shelf);
		}
		LogManager.getLogger().info("IAF SHELVING PANTRY PROBE PASSED: exact items, levels, all mounts and facings, NBT, bounds, migration and drops");
	}

	private static void verifyLabelsAndPots(WorldServer world, FakePlayer player, BlockPos pos, ShelvingUnit shelf) {
		Block block = shelf;
		IBlockState state = block.getDefaultState().withProperty(RackBlock.STANDING, true);
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		world.setBlockState(pos, state);
		EntityPlayer editor = player;
		editor.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() - 1.0D);
		player.interactionManager.setGameType(GameType.SURVIVAL);
		player.inventory.clear();

		ItemStack pot = new ItemStack(Items.FLOWER_POT).setStackDisplayName("Named pot");
		ItemStack flower = new ItemStack(Blocks.RED_FLOWER, 1, 1).setStackDisplayName("Named orchid");
		click(world, player, pos, true, pot.copy());
		click(world, player, pos, true, flower.copy());

		TileEntityShelvingUnit tile = (TileEntityShelvingUnit)world.getTileEntity(pos);
		ShelfLevel upper = tile.getLevel(true);
		ItemStack planted = upper.getSurfaceSetting().getCompatibilityItem();
		require(ItemStack.areItemStacksEqual(FlowerPotSurface.getPlant(planted), flower), "Shelf pot lost its exact flower");
		require(editor.getHeldItemMainhand() == null, "Survival planting did not consume one flower");
		require(upper.getSurfaceHeight() + upper.getScale() * upper.getSurfaceSetting().getCombinedBounds().maxY
			< 1.0D - ShelvingUnit.BOARD_THICKNESS, "The planted pot intersects the upper frame");
		require(ItemStack.areItemStacksEqual(SurfaceInteractionRegistry.drops(planted).get(0), pot)
			&& ItemStack.areItemStacksEqual(SurfaceInteractionRegistry.drops(planted).get(1), flower),
			"Breaking a planted pot would lose its exact contents");

		ItemStack sign = new ItemStack(Items.SIGN, 2).setStackDisplayName("Named label");
		player.setHeldItem(EnumHand.OFF_HAND, sign);
		ContainerShelfLabel lowerMenu = new ContainerShelfLabel(world, pos, shelf, tile, false, EnumHand.OFF_HAND);
		ContainerShelfLabel staleMenu = new ContainerShelfLabel(world, pos, shelf, tile, false, EnumHand.OFF_HAND);
		require(lowerMenu.save(player, "Tools\n & parts") && sign.stackSize == 1, "Survival label did not consume one sign");
		require(!staleMenu.save(player, "Stale") && sign.stackSize == 1, "A stale editor overwrote an existing label");
		ContainerShelfLabel upperMenu = new ContainerShelfLabel(world, pos, shelf, tile, true, EnumHand.OFF_HAND);
		require(upperMenu.save(player, "Orchids") && editor.getHeldItemOffhand() == null, "Upper label failed");

		TileEntityShelvingUnit restored = new TileEntityShelvingUnit();
		((TileEntity)restored).readFromNBT(((TileEntity)tile).writeToNBT(new NBTTagCompound()));
		ItemStack expectedSign = sign.copy();
		expectedSign.stackSize = 1;
		require("Tools & parts".equals(restored.getLevel(false).getLabelText())
			&& "Orchids".equals(restored.getLevel(true).getLabelText())
			&& ItemStack.areItemStacksEqual(restored.getLevel(true).getLabelSign(), expectedSign),
			"Shelf label data was lost on reload");
		require(ItemStack.areItemStacksEqual(FlowerPotSurface.getPlant(
			restored.getLevel(true).getSurfaceSetting().getCompatibilityItem()), flower), "Potted flower was lost on reload");

		for (int click = 0; click < 2; click++) {
			require(block.onBlockActivated(world, pos, world.getBlockState(pos), player, EnumHand.OFF_HAND, null,
				EnumFacing.NORTH, 0.5F, 0.65F, 0.5F), "The shelf did not handle pot pickup");
		}
		require(ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(0), flower)
			&& ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(1), pot)
			&& upper.getLabelSign() != null && tile.getLevel(false).getLabelSign() != null,
			"Pot pickup lost the pot, plant, or independent labels");
		LogManager.getLogger().info("IAF SHELF DECOR PROBE PASSED: compact plants, exact pickup, independent labels and reload");
	}

	private static void verifyLabelTargets(WorldServer world, BlockPos origin, Block block) {
		ShelvingUnit shelf = (ShelvingUnit)block;
		ItemStack sign = new ItemStack(Items.SIGN);

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			for (boolean standing : new boolean[] { false, true }) {
				BlockPos pos = origin.east(4 * (2 * facing.getHorizontalIndex() + (standing ? 1 : 0)));
				IBlockState state = block.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
					.withProperty(RackBlock.STANDING, standing);
				world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
				world.setBlockState(pos, state);

				for (boolean upper : new boolean[] { false, true }) {
					require(shelf.writeLabel(world, pos, upper, sign, "Target"), "Failed to set the fixture label");
					RayTraceResult hit = traceDisplay(world, pos, world.getBlockState(pos), facing,
						shelf.getLabelBounds(state, upper));
					require(hit != null && hit.subHit >= 100 && hit.sideHit == facing,
						"Label could not be targeted: " + facing + "/" + standing + "/" + upper);
				}
			}
		}
	}

	private static void verifyDisplayLighting(WorldServer world, FakePlayer player, BlockPos origin, Block shelf) {
		ItemStack[] samples = {
			new ItemStack(Block.getBlockFromName("ironagefurniture:light_metal_ironage_block_floor_glow_clear")),
			new ItemStack(Block.getBlockFromName("ironagefurniture:light_metal_ironage_block_floor_lava_clear")),
			new ItemStack(Block.getBlockFromName("ironagefurniture:light_metal_ironage_candle_floor")),
			new ItemStack(Items.LAVA_BUCKET),
			new ItemStack(Block.getBlockFromName("ironagefurniture:light_metal_ironage_block_floor_red_clear")),
			new ItemStack(Items.WATER_BUCKET)
		};
		int[] lightLevels = { 15, 15, 12, 15, 0, 0 };

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			BlockPos area = origin.south(5 * facing.getHorizontalIndex());
			for (int index = 0; index < samples.length; index++) {
				BlockPos pos = area.east(4 * index);
				boolean standing = (index & 1) == 0;
				IBlockState state = shelf.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
					.withProperty(RackBlock.STANDING, standing);
				world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
				world.setBlockState(pos, state);

				for (boolean upper : new boolean[] { false, true }) {
					player.inventory.clear();
					float x = standing ? 0.5F : 0.5F - facing.getFrontOffsetX() * 0.25F;
					float z = standing ? 0.5F : 0.5F - facing.getFrontOffsetZ() * 0.25F;
					float y = upper ? 0.65F : 0.20F;
					ItemStack sample = samples[index].copy().setStackDisplayName("Display light");
					for (int slot = 0; slot < 2; slot++) {
						ItemStack held = sample.copy();
						player.setHeldItem(EnumHand.MAIN_HAND, held);
						require(shelf.onBlockActivated(world, pos, world.getBlockState(pos), player, EnumHand.MAIN_HAND,
							held, facing, x, y, z) && held.stackSize == 0, "Could not display " + sample);
					}

					IBlockState occupied = world.getBlockState(pos);
					ShelfLevel level = level(world, pos, upper);
					require(level.getVisibleArrangementCount() == 2, "Shelf should display two copies of " + sample);
					ItemStack extra = sample.copy();
					player.setHeldItem(EnumHand.MAIN_HAND, extra);
					shelf.onBlockActivated(world, pos, occupied, player, EnumHand.MAIN_HAND, extra, facing, x, y, z);
					require(extra.stackSize == 1 && level.getMediaCount() == 2, "A third light was accepted");
					player.setHeldItem(EnumHand.MAIN_HAND, null);

					AxisAlignedBB first = ((ShelvingUnit)shelf).getArrangementBounds(occupied, level, 0);
					AxisAlignedBB second = ((ShelvingUnit)shelf).getArrangementBounds(occupied, level, 1);
					double ceiling = (upper ? 1 : ShelvingUnit.UPPER_HEIGHT) - ShelvingUnit.BOARD_THICKNESS;
					require(!first.intersectsWith(second) && first.maxY < ceiling && second.maxY < ceiling,
						"Display lights overlap one another or the shelf above");
					NBTTagCompound saved = world.getTileEntity(pos).writeToNBT(new NBTTagCompound());
					TileEntityShelvingUnit restored = (TileEntityShelvingUnit)TileEntity.create(world, saved);
					require(restored != null && restored.getLevel(upper).getMediaCount() == 2
						&& ItemStack.areItemStacksEqual(restored.getLevel(upper).copyMediaItem(1), sample),
						"Reload did not keep both lights");

					require(occupied.getLightValue(world, pos) == lightLevels[index], "Wrong shelf light for " + sample);
					require(shelf.onBlockActivated(world, pos, occupied, player, EnumHand.MAIN_HAND, null,
						facing, x, y, z), "Could not recover " + sample);
					require(ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(0), sample),
						"Display effects changed the stored item " + sample);
					require(level.getMediaCount() == 1 && occupied.getLightValue(world, pos) == lightLevels[index],
						"Removing one light changed the remaining display");
					shelf.onBlockActivated(world, pos, occupied, player, EnumHand.MAIN_HAND, null, facing, x, y, z);
					require(world.getBlockState(pos).getLightValue(world, pos) == 0, "Removed item left behind an emitter");
				}
			}
		}
		LogManager.getLogger().info("IAF SHELF LIGHT PROBE PASSED: pairs, clearance, reload, emission and exact pickup");
	}

	private static void verifyBaseMetals(WorldServer world, FakePlayer player, BlockPos origin, Block shelf) {
		ItemStack[] samples = {
			MetalShelfSamples.item("copper_ingot"), MetalShelfSamples.item("tin_ingot"),
			MetalShelfSamples.item("silver_nugget"), MetalShelfSamples.item("bronze_powder"),
			MetalShelfSamples.item("steel_blend"), MetalShelfSamples.item("copper_plate"),
			MetalShelfSamples.item("iron_gear"), MetalShelfSamples.item("copper_rod"),
			MetalShelfSamples.item("steel_crackhammer"), MetalShelfSamples.item("copper_helmet"),
			MetalShelfSamples.item("copper_chestplate"), MetalShelfSamples.item("steel_leggings"),
			MetalShelfSamples.item("steel_boots"), MetalShelfSamples.item("copper_door"),
			MetalShelfSamples.item("copper_bars"), MetalShelfSamples.item("copper_trapdoor"),
			MetalShelfSamples.item("copper_ore"), MetalShelfSamples.item("copper_block"),
			MetalShelfSamples.item("human_detector"), MetalShelfSamples.item("mercury_powder"),
			MetalShelfSamples.mercuryBucket()
		};
		int[] capacities = { 6, 6, 6, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2 };
		samples[8].addEnchantment(Enchantments.EFFICIENCY, 2);
		verifyMetalDisplays(world, player, origin, shelf, "basemetals", samples, capacities);
	}

	private static void verifyModernMetals(WorldServer world, FakePlayer player, BlockPos origin, Block shelf) {
		String mod = "modernmetals";
		ItemStack[] samples = {
			MetalShelfSamples.item(mod, "aluminum_ingot"), MetalShelfSamples.item(mod, "titanium_nugget"),
			MetalShelfSamples.item(mod, "chromium_powder"), MetalShelfSamples.item(mod, "aluminum_smallpowder"),
			MetalShelfSamples.item(mod, "tungsten_crackhammer"), MetalShelfSamples.item(mod, "titanium_helmet"),
			MetalShelfSamples.item(mod, "titanium_shield"), MetalShelfSamples.item(mod, "titanium_horse_armor"),
			MetalShelfSamples.item(mod, "aluminum_arrow"), MetalShelfSamples.item(mod, "titanium_bolt"),
			MetalShelfSamples.item(mod, "titanium_crossbow"), MetalShelfSamples.item(mod, "aluminum_rod"),
			MetalShelfSamples.item(mod, "aluminum_door"), MetalShelfSamples.item(mod, "aluminum_trapdoor"),
			MetalShelfSamples.item(mod, "aluminum_casing"), MetalShelfSamples.item(mod, "titanium_ore"),
			MetalShelfSamples.bucket("aluminum"), MetalShelfSamples.bucket("titanium")
		};
		int[] capacities = { 6, 6, 2, 2, 1, 1, 1, 1, 6, 6, 1, 1, 1, 1, 2, 1, 2, 2 };
		samples[4].addEnchantment(Enchantments.EFFICIENCY, 2);
		verifyMetalDisplays(world, player, origin, shelf, mod, samples, capacities);
	}

	private static void verifyMetalDisplays(WorldServer world, FakePlayer player, BlockPos origin, Block shelf,
			String mod, ItemStack[] samples, int[] capacities) {
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			for (boolean standing : new boolean[] { false, true }) {
				BlockPos area = origin.south(5 * (2 * facing.getHorizontalIndex() + (standing ? 1 : 0)));
				IBlockState state = shelf.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
					.withProperty(RackBlock.STANDING, standing);
				for (int index = 0; index < samples.length; index++) {
					BlockPos pos = area.east(4 * index);
					world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
					world.setBlockState(pos, state);
					ItemStack sample = samples[index].copy().setStackDisplayName("Metal sample");
					require(MaterialShelfDisplay.find(sample) != null, "Metal item has no shelf model: " + sample);
					verifyDisplay(world, player, pos, (ShelvingUnit)shelf, state, sample, capacities[index]);
				}
			}
		}

		int supported = 0;
		for (Item item : Item.REGISTRY) {
			net.minecraft.util.ResourceLocation id = item.getRegistryName();
			if (id != null && mod.equals(id.getResourceDomain())) {
				if (MaterialShelfDisplay.find(new ItemStack(item)) != null) {
					supported++;
				} else {
					require(item instanceof ItemBlock && ((ItemBlock)item).getBlock() instanceof IFluidBlock,
						"Missing a shelf display for " + id);
				}
			}
		}
		LogManager.getLogger().info("IAF METALS SHELF PROBE PASSED: {} {} items and exact display, pickup and NBT", supported, mod);
	}

	private static void verifyBaseMinerals(WorldServer world, FakePlayer player, BlockPos origin, Block shelf) {
		String mod = "baseminerals";
		ItemStack[] samples = {
			MetalShelfSamples.item(mod, "salt_powder"), MetalShelfSamples.item(mod, "sulfur_smallpowder"),
			MetalShelfSamples.item(mod, "silicon_blend"), MetalShelfSamples.item(mod, "silicon_ingot"),
			MetalShelfSamples.item(mod, "lithium_ingot"), MetalShelfSamples.item(mod, "lithium_nugget"),
			MetalShelfSamples.item(mod, "salt_ore"), MetalShelfSamples.item(mod, "potash_block"),
			MetalShelfSamples.bucket("lithium"), MetalShelfSamples.bucket("silicon")
		};
		require(MaterialShelfDisplay.getBucketContents(samples[8]) != null
			&& MaterialShelfDisplay.getBucketContents(samples[9]) != null, "Base Minerals fluids should have fitted shelf buckets");
		verifyMetalDisplays(world, player, origin, shelf, mod, samples, new int[] { 2, 2, 2, 6, 6, 6, 1, 1, 2, 2 });
	}

	private static void verifyBaseGems(WorldServer world, FakePlayer player, BlockPos origin, Block shelf) {
		ShelvingUnit unit = (ShelvingUnit)shelf;
		String mod = "basegems";
		ItemStack[] gems = {
			MetalShelfSamples.item(mod, "ruby_ingot").setStackDisplayName("Collector's ruby"),
			MetalShelfSamples.item(mod, "sapphire_ingot"), MetalShelfSamples.item(mod, "opal_ingot"),
			MetalShelfSamples.item(mod, "peridot_ingot"), MetalShelfSamples.item(mod, "blackdiamond_ingot"),
			MetalShelfSamples.item(mod, "amethyst_ingot")
		};
		verifyMetalDisplays(world, player, origin.south(44), shelf, mod, new ItemStack[] {
			MetalShelfSamples.item(mod, "ruby_nugget"), MetalShelfSamples.item(mod, "amethyst_powder"),
			MetalShelfSamples.item(mod, "opal_pickaxe"), MetalShelfSamples.item(mod, "sapphire_ore"),
			MetalShelfSamples.item(mod, "jade_shield")
		}, new int[] { 6, 2, 1, 1, 1 });

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			for (boolean standing : new boolean[] { false, true }) {
				BlockPos pos = origin.south(4 * (2 * facing.getHorizontalIndex() + (standing ? 1 : 0)));
				IBlockState state = shelf.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
					.withProperty(RackBlock.STANDING, standing);
				world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
				world.setBlockState(pos, state);
				for (boolean upper : new boolean[] { false, true }) {
					player.inventory.clear();
					for (ItemStack gem : gems) {
						ItemStack held = gem.copy();
						click(world, player, pos, upper, held);
						require(held.stackSize == 0, "The platter did not take exactly one stone: " + gem);
					}

					ShelfLevel level = level(world, pos, upper);
					require(level.getShelfLayout() == ShelfLayout.GEM_PLATTER && level.getMediaCount() == 6,
						"Each shelf level should hold six mixed gemstones");
					ItemStack surplus = gems[0].copy();
					click(world, player, pos, upper, surplus);
					require(surplus.stackSize == 1, "The full platter consumed an extra stone");

					AxisAlignedBB frame = state.getBoundingBox(world, pos);
					for (int gem = 0; gem < gems.length; gem++) {
						AxisAlignedBB box = unit.getArrangementBounds(state, level, gem);
						require(ItemStack.areItemStacksEqual(gems[gem], level.copyMediaItem(gem))
							&& box.minX > frame.minX && box.maxX < frame.maxX && box.minZ > frame.minZ && box.maxZ < frame.maxZ
							&& box.maxY < (upper ? 1 : ShelvingUnit.UPPER_HEIGHT - ShelvingUnit.BOARD_THICKNESS),
							"The gemstone escaped its tier or changed its item data");
					}
					require(level.getShelfPlacement(3).y > level.getShelfPlacement(0).y, "The back row should be higher");

					AxisAlignedBB ruby = unit.getArrangementBounds(state, level, 0);
					RayTraceResult hit = traceDisplay(world, pos, state, facing, ruby, 0.26D);
					require(hit != null && hit.subHit >= 100, "A front-row gemstone should be selectable");
					for (int slot = 0; slot < player.inventory.mainInventory.length; slot++) {
						player.inventory.setInventorySlotContents(slot, new ItemStack(Blocks.COBBLESTONE, 64));
					}
					takeGem(world, player, pos, state, hit);
					require(level.getMediaCount() == 6, "The full inventory lost the selected stone");
					player.inventory.setInventorySlotContents(10, gems[0].copy());
					takeGem(world, player, pos, state, hit);
					ItemStack merged = gems[0].copy();
					merged.stackSize = 2;
					require(ItemStack.areItemStacksEqual(merged, player.inventory.getStackInSlot(10))
						&& level.getMediaCount() == 5 && ItemStack.areItemStacksEqual(gems[1], level.copyMediaItem(0)),
						"Pickup must return the selected stone with its exact name and data");
				}

				NBTTagCompound saved = world.getTileEntity(pos).writeToNBT(new NBTTagCompound());
				TileEntity restored = TileEntity.create(world, saved);
				require(restored != null && saved.equals(restored.writeToNBT(new NBTTagCompound())),
					"Mixed gemstones changed on reload");
				world.setTileEntity(pos, restored);
				world.setBlockToAir(pos);
				int drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos)).stream()
					.map(EntityItem::getEntityItem).filter(stack -> MaterialShelfDisplay.find(stack) == MaterialShelfDisplay.GEM)
					.mapToInt(stack -> stack.stackSize).sum();
				require(drops == 10, "Breaking both platters lost or duplicated a stone");
			}
		}
		player.inventory.clear();
		LogManager.getLogger().info("IAF GEM PLATTER PROBE PASSED: mixed stones, both tiers, all facings, targeting, reload and drops");
	}

	private static void takeGem(WorldServer world, FakePlayer player, BlockPos pos, IBlockState state, RayTraceResult hit) {
		Vec3d local = hit.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());
		EnumFacing facing = state.getValue(BlockHorizontal.FACING);
		double dx = -facing.getFrontOffsetX();
		double dz = -facing.getFrontOffsetZ();
		double drop = 0.26D;
		player.setPositionAndRotation(hit.hitVec.xCoord - dx, hit.hitVec.yCoord + drop - player.getEyeHeight(),
			hit.hitVec.zCoord - dz, (float)Math.toDegrees(Math.atan2(-dx, dz)), (float)Math.toDegrees(Math.atan(drop)));
		// Forge 1.10 sends these offsets as byte values in sixteenths of a block.
		float hitX = (int)(16 * local.xCoord) / 16.0F;
		float hitY = (int)(16 * local.yCoord) / 16.0F;
		float hitZ = (int)(16 * local.zCoord) / 16.0F;
		require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.OFF_HAND, null,
			hit.sideHit, hitX, hitY, hitZ),
			"The shelf did not handle gemstone pickup");
	}

	private static ShelfLayout expectedDyeLayout(EnumDyeColor colour) {
		return colour == EnumDyeColor.BLACK || colour == EnumDyeColor.BLUE ? ShelfLayout.PRODUCE_TRAY : ShelfLayout.FOOD_PAIR;
	}

	private static void verifyVanillaMountedGearUse(WorldServer world, FakePlayer player, BlockPos plinth) {
		world.setBlockState(plinth, Blocks.STONE.getDefaultState());
		ItemStack banner = TrophyShelfSamples.patternedBanner(EnumDyeColor.BLUE);
		NBTTagList patterns = banner.getSubCompound("BlockEntityTag", false).getTagList("Patterns", 10);
		require(banner.onItemUse(player, world, plinth, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 1, 0.5F)
				== EnumActionResult.SUCCESS && banner.stackSize == 0, "Vanilla banner placement failed");
		TileEntity tile = world.getTileEntity(plinth.up());
		require(world.getBlockState(plinth.up()).getBlock() == Blocks.STANDING_BANNER && tile instanceof TileEntityBanner
			&& ((TileEntityBanner)tile).getBaseColor() == EnumDyeColor.BLUE.getDyeDamage()
			&& patterns.equals(((TileEntityBanner)tile).getPatterns()), "Placed banner lost its base colour or layers");

		EntityHorse horse = new EntityHorse(world);
		for (Item item : new Item[] { Items.IRON_HORSE_ARMOR, Items.GOLDEN_HORSE_ARMOR, Items.DIAMOND_HORSE_ARMOR }) {
			ItemStack armour = new ItemStack(item);
			require(horse.replaceItemInInventory(401, armour)
				&& horse.getHorseArmorType() == HorseArmorType.getByItemStack(armour), "Vanilla horse armour slot rejected " + item);
		}

		player.inventory.clear();
		ItemStack elytra = TrophyShelfSamples.elytra();
		ItemStack expected = elytra.copy();
		require(elytra.useItemRightClick(world, player, EnumHand.MAIN_HAND).getType() == EnumActionResult.SUCCESS
			&& elytra.stackSize == 0 && ItemStack.areItemStacksEqual(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), expected),
			"Normal elytra equip lost damage, name or enchantments");
		player.setItemStackToSlot(EntityEquipmentSlot.CHEST, null);
	}

	private static void verifyVanillaDyeUse(WorldServer world, FakePlayer player, BlockPos crop) {
		world.setBlockState(crop.down(), Blocks.FARMLAND.getDefaultState());
		world.setBlockState(crop, Blocks.WHEAT.getDefaultState());
		ItemStack boneMeal = new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage());
		require(boneMeal.onItemUse(player, world, crop, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 1, 0.5F)
				== EnumActionResult.SUCCESS && boneMeal.stackSize == 0 && world.getBlockState(crop).getValue(BlockCrops.AGE) > 0,
			"Vanilla bone meal did not grow wheat");

		EntitySheep sheep = new EntitySheep(world);
		sheep.setFleeceColor(EnumDyeColor.WHITE);
		ItemStack dye = new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage());
		require(dye.interactWithEntity(player, sheep, EnumHand.MAIN_HAND)
			&& dye.stackSize == 0 && sheep.getFleeceColor() == EnumDyeColor.RED, "Vanilla dye did not colour sheep");
	}

	private static void verifyVanillaTravelUse(WorldServer world, FakePlayer player, BlockPos fence) {
		world.setBlockState(fence.down(), Blocks.STONE.getDefaultState());
		world.setBlockState(fence, Blocks.OAK_FENCE.getDefaultState());
		EntityPig pig = new EntityPig(world);
		pig.setPosition(fence.getX() + 1.5D, fence.getY(), fence.getZ() + 0.5D);
		require(world.spawnEntity(pig), "Could not spawn the travel gear test's pig");
		try {
			ItemStack saddle = new ItemStack(Items.SADDLE);
			require(saddle.interactWithEntity(player, pig, EnumHand.MAIN_HAND)
				&& pig.getSaddled() && saddle.stackSize == 0, "A vanilla saddle did not equip a pig");

			ItemStack lead = new ItemStack(Items.LEAD);
			pig.setLeashedToEntity(player, true);
			require(lead.onItemUse(player, world, fence, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 1, 0.5F)
					== EnumActionResult.SUCCESS && pig.getLeashedToEntity() instanceof EntityLeashKnot,
				"A vanilla lead did not tether a pig to a fence");
		} finally {
			if (pig.getLeashedToEntity() instanceof EntityLeashKnot) {
				pig.getLeashedToEntity().setDead();
			}
			pig.setDead();
		}
	}

	private static void verifyMixedStrikers(WorldServer world, FakePlayer player, BlockPos pos, Block shelf) {
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		world.setBlockState(pos, shelf.getDefaultState().withProperty(RackBlock.STANDING, true));
		player.inventory.clear();

		ItemStack[] variants = {
			new ItemStack(Items.FLINT_AND_STEEL),
			new ItemStack(Items.FLINT_AND_STEEL, 1, 12).setStackDisplayName("Worn striker"),
			new ItemStack(Items.FLINT_AND_STEEL, 1, 24).setStackDisplayName("Enchanted striker")
		};
		variants[2].addEnchantment(Enchantments.UNBREAKING, 2);

		for (int index = 0; index < variants.length; index++) {
			ItemStack held = variants[index].copy();
			click(world, player, pos, false, held);
			require(held.stackSize == 0 && ItemStack.areItemStacksEqual(level(world, pos, false).copyMediaItem(index), variants[index]),
				"Flint and steel with different wear or tags could not share a shelf");
		}

		NBTTagCompound saved = world.getTileEntity(pos).writeToNBT(new NBTTagCompound());
		TileEntity loaded = TileEntity.create(world, saved);
		require(loaded != null && saved.equals(loaded.writeToNBT(new NBTTagCompound())),
			"Reload altered one of the flint and steel items");
		world.setTileEntity(pos, loaded);

		for (int index = 0; index < variants.length; index++) {
			take(world, player, pos);
			require(ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(index), variants[variants.length - 1 - index]),
				"Pickup altered or reordered a flint and steel item");
		}

		require(world.getBlockState(pos).getBlock() == shelf
			&& ((TileEntityShelvingUnit)world.getTileEntity(pos)).isEmpty(),
			"Taking the last striker did not leave an empty shelf");
		LogManager.getLogger().info("IAF FLINT AND STEEL PROBE PASSED: distinct wear and tags, save/load and pickup");
	}

	private static void verifyDisplay(WorldServer world, FakePlayer player, BlockPos pos, ShelvingUnit shelf,
			IBlockState state, ItemStack sample, int capacity) {
		player.inventory.clear();
		EnumFacing facing = state.getValue(BlockHorizontal.FACING);
		Item sampleItem = sample.getItem();
		boolean mountedGear = sampleItem == Items.ELYTRA || sampleItem == Items.BANNER || HorseArmorType.isHorseArmor(sampleItem);

		for (boolean upper : new boolean[] { false, true }) {
			if (mountedGear) {
				verifyCreativeMountedGear(world, player, pos, upper, sample);
			}

			ItemStack held = sample.copy();
			held.stackSize = Math.min(2, held.getMaxStackSize());
			click(world, player, pos, upper, held);
			require(held.stackSize == Math.min(2, held.getMaxStackSize()) - 1, "Placement must consume exactly one item");

			ShelfLevel level = level(world, pos, upper);
			ShelfLayout layout = level.getShelfLayout();
			require(level.getMediaCount() == 1 && layout.getCapacity() == capacity,
				"Item did not opt into its expected shelf layout");
			// Aim down into trays that hold low-profile items.
			double elevation = sample.getItem() == Items.FEATHER || layout == ShelfLayout.CROSSWISE_TRAY ? 0.31D : 0;
			RayTraceResult hit = traceDisplay(world, pos, state, facing, shelf.getArrangementBounds(state, level, 0),
				elevation);
			require(hit != null && hit.subHit >= 100, "Partial arrangement could not be targeted: " + sample);

			ItemStack mismatch = sample.getItem() == Items.FLINT_AND_STEEL
				? new ItemStack(Items.SHEARS) : sample.copy().setStackDisplayName("Different exact stack");
			click(world, player, pos, upper, mismatch);
			require(mismatch.stackSize == 1 && level.getMediaCount() == 1, "Placement accepted different item data");

			ItemStack differentConfiguration = MagicShelfSamples.alternateConfiguration(sample);
			if (differentConfiguration != null) {
				click(world, player, pos, upper, differentConfiguration);
				require(differentConfiguration.stackSize == 1 && level.getMediaCount() == 1,
					"Different firework or spawn egg payloads shared a tray");
			}

			if (sample.getItem() == Items.GOLDEN_APPLE) {
				ItemStack variant = new ItemStack(Items.GOLDEN_APPLE, 1, 1 - sample.getMetadata());
				click(world, player, pos, upper, variant);
				require(variant.stackSize == 1 && level.getMediaCount() == 1, "Golden apple variants were mixed");
			}

			if (sample.getItem() == Items.COAL) {
				ItemStack variant = new ItemStack(Items.COAL, 1, 1 - sample.getMetadata());
				click(world, player, pos, upper, variant);
				require(variant.stackSize == 1 && level.getMediaCount() == 1, "Coal and charcoal shared a tray");
			}

			if (sample.getItem() == Items.DYE) {
				ItemStack different = sample.copy();
				different.setItemDamage((sample.getMetadata() + 1) % EnumDyeColor.values().length);
				click(world, player, pos, upper, different);
				require(different.stackSize == 1 && level.getMediaCount() == 1, "Different dye variants shared a level");
			}

			if (sample.getItem() == Items.BUCKET || sample.getItem() == Items.WATER_BUCKET || sample.getItem() == Items.LAVA_BUCKET) {
				ItemStack different = new ItemStack(sample.getItem() == Items.WATER_BUCKET ? Items.LAVA_BUCKET : Items.WATER_BUCKET);
				click(world, player, pos, upper, different);
				require(different.stackSize == 1 && level.getMediaCount() == 1, "Different buckets shared a shelf level");
			}

			if (sample.getItem() == Items.MUSHROOM_STEW) {
				ItemStack rabbitStew = new ItemStack(Items.RABBIT_STEW);
				click(world, player, pos, upper, rabbitStew);
				require(rabbitStew.stackSize == 1 && level.getMediaCount() == 1, "Different stews shared a level");
			}

			verifyButcherMatching(world, player, pos, upper, level, sample);
			if (sample.getItem() == Items.ARROW || sample.getItem() == Items.SPECTRAL_ARROW) {
				ItemStack different = new ItemStack(sample.getItem() == Items.ARROW ? Items.SPECTRAL_ARROW : Items.ARROW);
				click(world, player, pos, upper, different);
				require(different.stackSize == 1 && level.getMediaCount() == 1, "Arrow types were mixed");
			}
			if (sample.getItem() == Items.TIPPED_ARROW) {
				ItemStack different = PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW), PotionTypes.SWIFTNESS);
				click(world, player, pos, upper, different);
				require(different.stackSize == 1 && level.getMediaCount() == 1, "Tipped arrows with different effects were mixed");
			}

			player.interactionManager.setGameType(GameType.CREATIVE);
			ItemStack creative = sample.copy();
			click(world, player, pos, upper, creative);
			require(creative.stackSize == 1 && level.getMediaCount() == Math.min(2, capacity),
				"Creative placement changed the held stack");
			player.interactionManager.setGameType(GameType.SURVIVAL);

			while (level.getMediaCount() < capacity) {
				int previous = level.getMediaCount();
				click(world, player, pos, upper, sample.copy());
				require(level.getMediaCount() == previous + 1, "Arrangement rejected a matching stack before capacity");
			}
			ItemStack overflow = sample.copy();
			click(world, player, pos, upper, overflow);
			require(overflow.stackSize == 1 && level.getMediaCount() == capacity, "Full arrangement consumed an extra item");

			AxisAlignedBB frame = state.getBoundingBox(world, pos);
			double ceiling = (upper ? 1 : ShelvingUnit.UPPER_HEIGHT) - ShelvingUnit.BOARD_THICKNESS;
			for (int index = 0; index < capacity; index++) {
				AxisAlignedBB item = shelf.getArrangementBounds(state, level, index);
				require(ItemStack.areItemStacksEqual(level.copyMediaItem(index), sample)
					&& item.minX > frame.minX && item.maxX < frame.maxX && item.minZ > frame.minZ && item.maxZ < frame.maxZ
					&& item.minY >= level.getSurfaceHeight() && item.maxY < ceiling,
					"Display changed an item or clipped the frame");
			}

			boolean dyeTray = sample.getItem() == Items.DYE && layout == ShelfLayout.PRODUCE_TRAY;
			boolean crosswiseTray = layout == ShelfLayout.CROSSWISE_TRAY;
			if (capacity > 8 || layout == ShelfLayout.FOOD_PAIR || layout == ShelfLayout.MEAT_TRAY
					|| layout == ShelfLayout.FISH_TRAY || layout == ShelfLayout.FLINT_AND_STEEL_ROW
					|| layout == ShelfLayout.BUNDLE_TRAY || layout == ShelfLayout.COOKIE_STACKS || dyeTray || crosswiseTray) {
				AxisAlignedBB last = shelf.getArrangementBounds(state, level, capacity - 1);
				double rearElevation = crosswiseTray ? 0.49D : dyeTray ? 0.43D : elevation;
				RayTraceResult top = traceDisplay(world, pos, state, facing, last, rearElevation);
				require(top != null && top.subHit == hit.subHit + capacity - 1,
					"The last displayed item lost its own target when the shelf filled up: " + sample);
			}

			if (layout == ShelfLayout.ARROW_STACKS) {
				verifyArrowSpacing(world, pos, shelf, state, level, hit.subHit);
			} else if (layout == ShelfLayout.CARROT_TRAY) {
				verifyCarrotTaper(level);
			} else if (layout == ShelfLayout.INGOT_STACK) {
				verifyIngotStack(world, pos, shelf, state, level, hit.subHit);
			} else if (layout == ShelfLayout.BOWL_STACK) {
				verifyBowlStack(world, pos, shelf, state, level, hit.subHit);
			}
		}

		TileEntity tile = world.getTileEntity(pos);
		NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
		TileEntity restored = TileEntity.create(world, saved);
		require(restored != null && saved.equals(restored.writeToNBT(new NBTTagCompound())), "Reload changed pantry stacks");

		ShelfLayout layout = level(world, pos, false).getShelfLayout();
		TileEntityShelvingUnit mixed = verifyLegacySettings(world, saved.copy(), sample, facing, layout);
		if (sample.getItem() == Items.PUMPKIN_PIE) {
			verifyMixedPickup(world, player, pos, mixed, sample, facing);
			world.setTileEntity(pos, restored);
		}

		int lowerCapacity = capacity;
		if (sample.getItem() == Items.CARROT || sample.getItem() == Items.GOLDEN_CARROT) {
			lowerCapacity = 18;
			NBTTagList items = saved.getCompoundTag("LowerShelf").getTagList("MediaItems", 10);
			while (items.tagCount() < lowerCapacity) {
				items.appendTag(sample.writeToNBT(new NBTTagCompound()));
			}

			restored = TileEntity.create(world, saved);
			require(restored != null && saved.equals(restored.writeToNBT(new NBTTagCompound())),
				"Reload lost carrots from a tray filled before the taper");
			world.setTileEntity(pos, restored);
			ShelfLevel legacy = level(world, pos, false);
			require(legacy.getVisibleArrangementCount() == capacity && legacy.getShelfPlacement(capacity) == null,
				"Old overflow still has a display position");
			ItemStack extra = sample.copy();
			click(world, player, pos, false, extra);
			require(extra.stackSize == 1 && legacy.getMediaCount() == lowerCapacity,
				"An old full tray consumed an extra carrot");
		}

		for (int slot = 0; slot < player.inventory.mainInventory.length; slot++) {
			player.inventory.setInventorySlotContents(slot, new ItemStack(Blocks.COBBLESTONE, 64));
		}
		ShelfLevel lower = level(world, pos, false);
		ShelfLevel upper = level(world, pos, true);
		take(world, player, pos);
		require(lower.getMediaCount() == lowerCapacity, "Full inventory lost a shelf item");

		ItemStack expected = sample.copy();
		player.inventory.setInventorySlotContents(10, sample.isStackable() ? sample.copy() : null);
		take(world, player, pos);
		expected.stackSize = sample.isStackable() ? 2 : 1;
		require(ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(10), expected)
			&& lower.getMediaCount() == lowerCapacity - 1 && upper.getMediaCount() == capacity
			&& world.getBlockState(pos).getBlock() == shelf, "Pickup lost data, skipped a merge, or changed the other shelf");

		world.setBlockToAir(pos);
		int drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos)).stream()
			.map(EntityItem::getEntityItem)
			.filter(stack -> ItemStack.areItemsEqual(stack, sample) && ItemStack.areItemStackTagsEqual(stack, sample))
			.mapToInt(stack -> stack.stackSize).sum();
		require(drops == lowerCapacity + capacity - 1, "Breaking shelf lost or duplicated pantry items");
	}

	private static void verifyCreativeMountedGear(WorldServer world, FakePlayer player, BlockPos pos,
			boolean upper, ItemStack sample) {
		player.inventory.clear();
		player.interactionManager.setGameType(GameType.CREATIVE);
		try {
			ItemStack held = sample.copy();
			click(world, player, pos, upper, held);
			require(ItemStack.areItemStacksEqual(held, sample)
				&& ItemStack.areItemStacksEqual(level(world, pos, upper).copyMediaItem(0), sample),
				"Creative placement lost the held stack or changed the display " + sample);

			click(world, player, pos, upper, null);
			TileEntity tile = world.getTileEntity(pos);
			require((tile == null || ((TileEntityShelvingUnit)tile).getLevel(upper).isEmpty())
				&& ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(0), sample),
				"Creative pickup lost or duplicated a mounted display " + sample);
		} finally {
			player.interactionManager.setGameType(GameType.SURVIVAL);
			player.inventory.clear();
		}
	}

	private static void verifyArrowSpacing(WorldServer world, BlockPos pos, ShelvingUnit shelf, IBlockState state,
			ShelfLevel level, int firstHit) {
		EnumFacing facing = state.getValue(BlockHorizontal.FACING);
		double previousBack = Double.NEGATIVE_INFINITY;

		for (int index = 0; index < level.getVisibleArrangementCount(); index++) {
			AxisAlignedBB local = level.getShelfPlacement(index).bounds;
			require(local.minY == 0 && local.minZ > previousBack, "Arrows overlap or float above the shelf");
			previousBack = local.maxZ;

			RayTraceResult hit = traceDisplay(world, pos, state, facing, shelf.getArrangementBounds(state, level, index), 0.35D);
			require(hit != null && hit.subHit == firstHit + index, "An arrow could not be selected independently");
		}
	}

	private static void verifyButcherMatching(WorldServer world, FakePlayer player, BlockPos pos, boolean upper,
			ShelfLevel level, ItemStack sample) {
		ItemStack different;
		if (sample.getItem() == Items.BEEF) {
			different = new ItemStack(Items.COOKED_BEEF);
		} else if (sample.getItem() == Items.FISH) {
			different = new ItemStack(Items.FISH, 1, (sample.getMetadata() + 1) % 4);
		} else if (sample.getItem() == Items.COOKED_FISH) {
			different = new ItemStack(Items.COOKED_FISH, 1, 1 - sample.getMetadata());
		} else {
			return;
		}

		click(world, player, pos, upper, different);
		require(different.stackSize == 1 && level.getMediaCount() == 1,
			"A tray accepted a different meat or fish variant");
	}

	private static void verifyCarrotTaper(ShelfLevel level) {
		ShelfLayout.Placement bottom = level.getShelfPlacement(0);
		ShelfLayout.Placement middle = level.getShelfPlacement(6);
		ShelfLayout.Placement top = level.getShelfPlacement(11);
		require(Math.abs(bottom.x - middle.x - 0.06D) < 0.000001D
			&& Math.abs(middle.x - top.x - 0.06D) < 0.000001D
			&& Math.abs(middle.bounds.minY - bottom.bounds.maxY) < 0.000001D
			&& Math.abs(top.bounds.minY - middle.bounds.maxY) < 0.000001D
			&& Math.abs(bottom.x + level.getShelfPlacement(5).x) < 0.000001D
			&& Math.abs(middle.x + level.getShelfPlacement(10).x) < 0.000001D
			&& Math.abs(top.x + level.getShelfPlacement(14).x) < 0.000001D,
			"Carrot layers should be staggered, centred and touching the layer below");
	}

	private static void verifyIngotStack(WorldServer world, BlockPos pos, ShelvingUnit shelf,
			IBlockState state, ShelfLevel level, int firstHit) {
		double[] expectedX = { 0.25D, 0, -0.25D, 0.125D, -0.125D, 0 };
		int[] rows = { 0, 0, 0, 1, 1, 2 };
		double ingotHeight = level.getShelfPlacement(0).bounds.maxY;
		EnumFacing facing = state.getValue(BlockHorizontal.FACING);

		for (int index = 0; index < expectedX.length; index++) {
			ShelfLayout.Placement ingot = level.getShelfPlacement(index);
			require(Math.abs(ingot.x - expectedX[index]) < 0.000001D
				&& Math.abs(ingot.bounds.minY - rows[index] * ingotHeight) < 0.000001D
				&& Math.abs(ingot.bounds.maxY - (rows[index] + 1) * ingotHeight) < 0.000001D,
				"Ingot did not centre on the gap or rest on the layer below");

			AxisAlignedBB bounds = shelf.getArrangementBounds(state, level, index);
			RayTraceResult hit = traceDisplay(world, pos, state, facing, bounds);
			require(hit != null && hit.subHit == firstHit + index, "Ingot lost its individual hit target");
		}
	}

	private static void verifyBowlStack(WorldServer world, BlockPos pos, ShelvingUnit shelf,
			IBlockState state, ShelfLevel level, int firstHit) {
		ShelfLayout.Placement bottom = level.getShelfPlacement(0);
		double previousRim = bottom.bounds.maxY;
		double step = 0.72D * bottom.scale / 16;
		EnumFacing facing = state.getValue(BlockHorizontal.FACING);

		for (int index = 0; index < ShelfLayout.BOWL_STACK.getCapacity(); index++) {
			ShelfLayout.Placement bowl = level.getShelfPlacement(index);
			require(bowl.x == bottom.x && bowl.z == bottom.z && Math.abs(bowl.y - index * step) < 0.000001D,
				"Bowls did not nest concentrically");
			if (index > 0) {
				require(Math.abs(bowl.bounds.minY - previousRim) < 0.000001D
					&& Math.abs(bowl.bounds.maxY - previousRim - step) < 0.000001D,
					"Nested bowls did not expose one targetable rim per bowl");
			}
			previousRim = bowl.bounds.maxY;

			AxisAlignedBB bounds = shelf.getArrangementBounds(state, level, index);
			RayTraceResult hit = traceDisplay(world, pos, state, facing, bounds);
			require(hit != null && hit.subHit == firstHit + index, "Bowl lost its individual hit target");
		}
	}

	private static RayTraceResult traceDisplay(WorldServer world, BlockPos pos, IBlockState state, EnumFacing facing,
			AxisAlignedBB bounds) {
		return traceDisplay(world, pos, state, facing, bounds, 0);
	}

	private static RayTraceResult traceDisplay(WorldServer world, BlockPos pos, IBlockState state, EnumFacing facing,
			AxisAlignedBB bounds, double elevation) {
		AxisAlignedBB display = bounds.offset(pos);
		// Aim above shallow tray rims so the hit identifies the item itself.
		double y = display.minY + (display.maxY - display.minY) * 0.75D;
		Vec3d centre = new Vec3d((display.minX + display.maxX) / 2, y,
			(display.minZ + display.maxZ) / 2);
		Vec3d front = centre.addVector(facing.getFrontOffsetX(), elevation, facing.getFrontOffsetZ());
		return state.collisionRayTrace(world, pos, front, centre);
	}

	private static TileEntityShelvingUnit verifyLegacySettings(WorldServer world, NBTTagCompound saved, ItemStack meal,
			EnumFacing facing, ShelfLayout layout) {
		SurfaceSetting setting = new SurfaceSetting();
		setting.setSingleItem(meal, facing);
		NBTTagCompound loose = new NBTTagCompound();
		setting.writeToNBT(loose);
		saved.setTag("LowerShelf", loose);
		TileEntityShelvingUnit restored = (TileEntityShelvingUnit)TileEntity.create(world, saved);
		require(restored.getLevel(false).getShelfLayout() == layout
			&& ItemStack.areItemStacksEqual(restored.getLevel(false).copyMediaItem(0), meal),
			"Loose item did not migrate into its shelf arrangement");

		if (SurfaceSetting.classify(meal) == SurfaceSetting.Category.SINGLETON) {
			return restored;
		}

		ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
		setting.insert(bottle, facing, 0.75F, 0.75F, facing.getAxis());
		require(setting.getItemCount() == 2, "Could not create a legacy mixed display");
		loose = new NBTTagCompound();
		setting.writeToNBT(loose);
		saved.setTag("LowerShelf", loose);
		restored = (TileEntityShelvingUnit)TileEntity.create(world, saved);
		ShelfLevel mixed = restored.getLevel(false);
		require(mixed.getMediaCount() == 0 && mixed.getSurfaceSetting().getItemCount() == 2
			&& ItemStack.areItemStacksEqual(mixed.getSurfaceSetting().getItem(Slot.GUEST_A), meal)
			&& ItemStack.areItemStacksEqual(mixed.getSurfaceSetting().getItem(Slot.GUEST_B), bottle),
			"Migration altered a mixed loose display");
		return restored;
	}

	private static void verifyMixedPickup(WorldServer world, FakePlayer player, BlockPos pos,
			TileEntityShelvingUnit mixed, ItemStack pie, EnumFacing facing) {
		world.setTileEntity(pos, mixed);
		player.inventory.clear();
		ShelfLevel level = mixed.getLevel(false);
		AxisAlignedBB bottle = ShelvingUnit.getItemBounds(level, Slot.GUEST_B, false);
		IBlockState state = world.getBlockState(pos);
		require(bottle != null && state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, null,
			facing, (float)((bottle.minX + bottle.maxX) * 0.5D), 0.20F, (float)((bottle.minZ + bottle.maxZ) * 0.5D)),
			"The existing mixed display did not accept an empty-hand bottle pickup");
		require(ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(0), new ItemStack(Items.GLASS_BOTTLE))
			&& level.getShelfLayout() == ShelfLayout.FOOD_PAIR && ItemStack.areItemStacksEqual(level.copyMediaItem(0), pie),
			"Mixed display pickup lost the bottle or the named pie");
	}

	private static ShelfLevel level(WorldServer world, BlockPos pos, boolean upper) {
		return ((TileEntityShelvingUnit)world.getTileEntity(pos)).getLevel(upper);
	}

	private static void click(WorldServer world, FakePlayer player, BlockPos pos, boolean upper, ItemStack held) {
		player.setHeldItem(EnumHand.MAIN_HAND, held);
		IBlockState state = world.getBlockState(pos);
		require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, held,
			state.getValue(BlockHorizontal.FACING), 0.5F, upper ? 0.65F : 0.20F, 0.5F), "Shelf did not handle the click");
	}

	private static void take(WorldServer world, FakePlayer player, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.OFF_HAND, null,
			state.getValue(BlockHorizontal.FACING), 0.5F, 0.20F, 0.5F), "Shelf did not handle pickup");
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
