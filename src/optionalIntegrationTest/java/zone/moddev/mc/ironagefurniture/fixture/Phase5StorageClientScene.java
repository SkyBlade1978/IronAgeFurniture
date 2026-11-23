package zone.moddev.mc.ironagefurniture.fixture;

import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.tile.ProduceTray;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.*;
import zone.moddev.mc.ironagefurniture.api.tile.*;
import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.VasePlantHelper;
import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockStorageBench;
import zone.moddev.mc.ironagefurniture.api.PaddedBenchColourHelper;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.*;

final class Phase5StorageClientScene {
	static BlockPos scene(Minecraft client, BlockPos origin) {
		for(int x=-1;x<10;x++) for(int z=-1;z<5;z++) {
			client.world.setBlockState(origin.add(x,-1,z),Blocks.STONEBRICK.getDefaultState(),2);
			for(int y=0;y<4;y++) client.world.setBlockToAir(origin.add(x,y,z));
		}
		for(int x=0;x<4;x++) for(int y=0;y<2;y++) {
			String prefix=x<2?"cabinet_wood_ironage_":"half_cabinet_wood_ironage_";
			Block cabinet=Block.getBlockFromName("ironagefurniture:"+prefix+"birch");
			client.world.setBlockState(origin.add(x,y,3),cabinet.getDefaultState().withProperty(Cabinet.FACING,EnumFacing.NORTH),2);
		}
		Block rack=Block.getBlockFromName("ironagefurniture:bottle_rack_wood_ironage_oak");
		BlockPos rackPos=origin.add(5,0,3);
		client.world.setBlockState(rackPos,rack.getDefaultState().withProperty(BottleRack.STANDING,true).withProperty(BottleRack.DATA,true),2);
		TileEntityBottleRack tile=(TileEntityBottleRack)client.world.getTileEntity(rackPos);
		for(int slot=0;slot<9;slot++) tile.insertBottle(slot,new ItemStack(slot%2==0?Items.POTIONITEM:Items.GLASS_BOTTLE),EnumFacing.NORTH);
		Block upright=Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak");
		client.world.setBlockState(origin.add(7,0,3),upright.getDefaultState(),2);
		TileEntityBarrel barrel=(TileEntityBarrel)client.world.getTileEntity(origin.add(7,0,3));
		barrel.getFluidHandler().fill(new FluidStack(FluidRegistry.WATER,11000),true);
		Block side=Block.getBlockFromName("ironagefurniture:side_barrel_wood_ironage_birch");
		client.world.setBlockState(origin.add(8,0,3),side.getDefaultState().withProperty(SideBarrel.FACING,EnumFacing.NORTH),2);
		return origin;
	}
	static void gui(Minecraft client,BlockPos origin) {
		TileEntityBarrel barrel=(TileEntityBarrel)client.world.getTileEntity(origin.add(7,0,3));
		String[] woods={"oak","spruce","birch","jungle","acacia","big_oak"};
		for(int i=0;i<woods.length;i++) client.player.inventory.setInventorySlotContents(9+i,
			new ItemStack(Block.getBlockFromName("ironagefurniture:bottle_rack_wood_ironage_"+woods[i])));
		client.gameSettings.hideGUI=false;
		client.displayGuiScreen(new zone.moddev.mc.ironagefurniture.client.gui.GuiBarrel(barrel));
	}

	static BlockPos bulkBarrels(Minecraft client, BlockPos origin) {
		String[] woods = { "oak", "spruce", "birch", "jungle" };
		Block jacaranda = Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_biomesoplenty_jacaranda");
		if (jacaranda != null && jacaranda != Blocks.AIR) {
			woods[2] = "biomesoplenty_jacaranda";
		}

		ItemStack[] stacks = { null, new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.POTATO), new ItemStack(Items.FISH, 1, 1) };
		int[] counts = { 0, 450, 78, 128 };
		String[] labels = { "Water", "Wheat seeds", "Potatoes", "Salmon" };
		for (int index = 0; index < woods.length; index++) {
			Block barrel = Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_" + woods[index]);
			BlockPos pos = origin.add(index, 0, 2);
			client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
			client.world.setBlockState(pos, barrel.getDefaultState(), 2);
			TileEntity tile = client.world.getTileEntity(pos);
			if (stacks[index] != null) {
				NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
				NBTTagCompound bulk = new NBTTagCompound();
				bulk.setTag("Stack", stacks[index].writeToNBT(new NBTTagCompound()));
				bulk.setInteger("Amount", counts[index]);
				saved.setTag("BulkContents", bulk);
				tile.readFromNBT(saved);
				client.world.markBlockRangeForRenderUpdate(pos, pos);
			}
			((TileEntityBarrel)tile).writeLabel(EnumFacing.NORTH, new ItemStack(Items.SIGN), labels[index]);
		}

		BlockPos sidePos = origin.add(4, 0, 2);
		Block side = Block.getBlockFromName("ironagefurniture:side_barrel_wood_ironage_birch");
		client.world.setBlockState(sidePos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
		client.world.setBlockState(sidePos, side.getDefaultState().withProperty(SideBarrel.FACING, EnumFacing.NORTH), 2);
		((TileEntityBarrel)client.world.getTileEntity(sidePos)).writeLabel(EnumFacing.NORTH, new ItemStack(Items.SIGN), "Cider");
		return origin;
	}

	static BlockPos bedsideCabinets(Minecraft client, BlockPos origin) {
		ItemStack vase = new ItemStack(Ironagefurniture.ItemRegistry.get("ornament_glass_vase"), 1, 3);
		VasePlantHelper.addPlant(vase, new ItemStack(Blocks.RED_FLOWER));
		List<ItemStack> bottles = Phase5BottleProbe.samples();
		ItemStack[] items = {
			new ItemStack(BlockObjectHolder.light_metal_ironage_block_floor_glow_clear),
			vase,
			bottles.get(bottles.size() - 1),
			new ItemStack(BlockObjectHolder.light_metal_ironage_candle_floor)
		};
		String[] woods = { "oak", "birch", "spruce", "big_oak" };

		for (int i = 0; i < woods.length; i++) {
			BlockPos pos = origin.add(i, 0, 2);
			Block block = Ironagefurniture.BlockRegistry.get("bedside_cabinet_wood_ironage_" + woods[i]);
			client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
			client.world.setBlockState(pos, block.getDefaultState().withProperty(BedsideCabinet.FACING, EnumFacing.NORTH), 2);
			TileEntityBedsideCabinet cabinet = (TileEntityBedsideCabinet)client.world.getTileEntity(pos);
			cabinet.getSurfaceSetting().setSingleItem(items[i], EnumFacing.NORTH);
			NBTTagCompound tag = cabinet.getUpdateTag();
			tag.setBoolean("UpperOpen", (i & 1) != 0);
			tag.setBoolean("LowerOpen", (i & 2) != 0);
			cabinet.handleUpdateTag(tag);
			client.world.markBlockRangeForRenderUpdate(pos, pos);
		}

		return origin;
	}

	static BlockPos storageBenches(Minecraft client, BlockPos origin) {
		Block oak = Block.getBlockFromName("ironagefurniture:storage_bench_wood_ironage_oak");
		Block spruce = Block.getBlockFromName("ironagefurniture:storage_bench_wood_ironage_spruce");
		Block birch = Block.getBlockFromName("ironagefurniture:storage_bench_wood_ironage_birch");
		Block[] samples = {oak, spruce, oak, birch};
		PaddedBenchColour[] colours = {null, PaddedBenchColour.RED, PaddedBenchColour.MAGENTA, PaddedBenchColour.CYAN};

		for (BlockPos floor : BlockPos.getAllInBox(origin.add(-1, -1, -1), origin.add(5, -1, 4))) {
			client.world.setBlockState(floor, Blocks.STONE.getDefaultState(), 2);
		}

		for (int index = 0; index < samples.length; index++) {
			BlockPos pos = origin.add(index, 0, 2);
			PaddedBenchColour colour = colours[index];
			IBlockState state = samples[index].getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH)
				.withProperty(StorageBench.PADDED, colour != null);
			client.world.setBlockState(pos, state, 2);

			if (colour != null) {
				PaddedBenchColourHelper.setColour(client.world, pos, colour);
				IBlockState extended = samples[index].getExtendedState(state, client.world, pos);
				IBakedModel placed = client.getBlockRendererDispatcher().getModelForState(state);
				checkBenchCushion(placed, extended, colour);
			}
		}

		IBakedModel plain = client.getRenderItem().getItemModelWithOverrides(new ItemStack(oak), client.world, client.player);
		if (plain.getQuads(null, null, 0).stream().noneMatch(quad -> "minecraft:blocks/planks_oak".equals(quad.getSprite().getIconName()))) {
			throw new IllegalStateException("Missing plain inventory storage bench model");
		}

		for (PaddedBenchColour colour : PaddedBenchColour.values()) {
			ItemStack stack = ItemBlockStorageBench.paddedStack(oak, colour);
			checkBenchCushion(client.getRenderItem().getItemModelWithOverrides(stack, client.world, client.player), null, colour);
		}
		return origin;
	}

	private static void checkBenchCushion(IBakedModel model, IBlockState state, PaddedBenchColour colour) {
		String texture = "minecraft:blocks/wool_colored_" + colour.getLegacyTextureName();
		if (model.getQuads(state, null, 0).stream().noneMatch(quad -> texture.equals(quad.getSprite().getIconName()))) {
			throw new IllegalStateException("Missing storage bench cushion " + texture);
		}
	}

	static BlockPos shippingCrates(Minecraft client, BlockPos origin) {
		String[] woods = { "big_oak", "spruce", "oak", "birch" };
		String[] labels = { "Market stock", "Copper tools", "Apples", "Travel supplies" };

		for (int i = 0; i < woods.length; i++) {
			BlockPos pos = origin.add(i == 3 ? 1 : i, i == 3 ? 1 : 0, 2);
			Block block = Ironagefurniture.BlockRegistry.get("crate_wood_ironage_" + woods[i]);
			if (i < 3) client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
			client.world.setBlockState(pos, block.getDefaultState().withProperty(StorageCrate.FACING, EnumFacing.NORTH), 2);
			TileEntityCrate tile = (TileEntityCrate)client.world.getTileEntity(pos);
			tile.setSealed((i & 1) != 0);
			tile.writeLabel(new ItemStack(Items.SIGN), labels[i]);
			client.world.markBlockRangeForRenderUpdate(pos, pos);
		}

		return origin;
	}

	static BlockPos rackJoins(Minecraft client, BlockPos origin) {
		for (int x=-1; x<9; x++) for (int z=0; z<8; z++) {
			client.world.setBlockState(origin.add(x,-1,z), Blocks.STONEBRICK.getDefaultState(), 2);
			for (int y=0; y<4; y++) client.world.setBlockToAir(origin.add(x,y,z));
		}
		for (boolean standing : new boolean[]{false,true}) {
			int z=standing?5:1;
			for (int x=0; x<7; x++) for (int y=0; y<2; y++) {
				if (x==2 || x==3 || x<2 && y==1) continue;
				if (!standing) client.world.setBlockState(origin.add(x,y,z+1), Blocks.STONEBRICK.getDefaultState(), 2);
				Block rack=Block.getBlockFromName("ironagefurniture:bottle_rack_wood_ironage_"+(x<2?"oak":"birch"));
				BlockPos pos=origin.add(x,y,z);
				client.world.setBlockState(pos,rack.getDefaultState().withProperty(BottleRack.STANDING,standing),2);
			}
		}
		return origin;
	}

	static BlockPos shelvingUnits(Minecraft client, BlockPos origin) {
		List<ItemStack> bottles = Phase5BottleProbe.samples();
		for (int x = -1; x <= 5; x++) {
			for (int z = 0; z <= 3; z++) {
				client.world.setBlockState(origin.add(x, -1, z), Blocks.STONEBRICK.getDefaultState(), 2);
			}
		}

		for (int x : new int[] { 0, 1, 3, 4 }) {
			boolean standing = x >= 3;
			String wood = standing ? "oak" : "birch";
			Block unit = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_" + wood);

			for (int y = 0; y < 2; y++) {
				BlockPos pos = origin.add(x, y, 2);

				if (!standing) {
					client.world.setBlockState(pos.south(), Blocks.STONEBRICK.getDefaultState(), 2);
				}

				client.world.setBlockState(pos, unit.getDefaultState()
					.withProperty(RackBlock.STANDING, standing).withProperty(RackBlock.DATA, true), 2);
				TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(pos);
				if (x == 0 || x == 3) {
					for (int index = 0; index < 5; index++) {
						tile.getLevel(false).addMedia(new ItemStack(Items.BOOK));
					}
					int discCount = y == 0 ? 4 : 10;
					for (int index = 0; index < discCount; index++) {
						tile.getLevel(true).addMedia(new ItemStack(Items.RECORD_CAT));
					}
				} else {
					tile.getLevel(false).getSurfaceSetting().setSingleItem(new ItemStack(Items.APPLE), EnumFacing.NORTH);
					int bottleCount = y == 0 ? 4 : 8;
					for (int index = 0; index < bottleCount; index++) {
						tile.getLevel(true).addMedia(bottles.get((index + y) % bottles.size()));
					}
				}
			}
		}

		return origin;
	}

	static BlockPos pantryShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.BREAD), new ItemStack(Items.APPLE),
			new ItemStack(Items.CARROT), new ItemStack(Items.POTATO),
			new ItemStack(Items.COOKIE), ShelvingPantryProbe.enchantedAxe(),
			new ItemStack(OptionalIntegrationRecipeProbe.SHELF_LAYOUT_SAMPLE), new ItemStack(Items.GOLDEN_APPLE, 1, 1)
		};
		int[] amounts = { 5, 9, 15, 6, 6, 1, 3, 1 };
		return fillShelves(client, origin, samples, amounts);
	}

	static BlockPos kitchenShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.MELON), new ItemStack(Items.PUMPKIN_PIE),
			new ItemStack(Items.CAKE), new ItemStack(Items.MUSHROOM_STEW),
			new ItemStack(Items.BEETROOT_SOUP), new ItemStack(Items.RABBIT_STEW)
		};
		int[] amounts = { 6, 2, 1, 2, 2, 2 };
		return fillShelves(client, origin, samples, amounts);
	}

	static BlockPos butcherShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.BEEF), new ItemStack(Items.COOKED_BEEF),
			new ItemStack(Items.PORKCHOP), new ItemStack(Items.COOKED_MUTTON),
			new ItemStack(Items.CHICKEN), new ItemStack(Items.COOKED_CHICKEN),
			new ItemStack(Items.RABBIT), new ItemStack(Items.COOKED_RABBIT)
		};
		return fillShelves(client, origin, samples, new int[] { 2, 2, 2, 2, 2, 2, 2, 2 });
	}

	static BlockPos fishmongerShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.FISH, 1, 0), new ItemStack(Items.COOKED_FISH, 1, 0),
			new ItemStack(Items.FISH, 1, 1), new ItemStack(Items.COOKED_FISH, 1, 1),
			new ItemStack(Items.FISH, 1, 2), new ItemStack(Items.FISH, 1, 3)
		};
		return fillShelves(client, origin, samples, new int[] { 3, 3, 3, 3, 3, 3 });
	}

	static BlockPos workshopShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			ShelvingPantryProbe.enchantedSword(), new ItemStack(Items.BOW),
			new ItemStack(Items.FISHING_ROD), new ItemStack(Items.CARROT_ON_A_STICK),
			new ItemStack(Items.SHEARS), new ItemStack(Items.FLINT_AND_STEEL),
			new ItemStack(Items.SHIELD), ShelvingPantryProbe.patternedShield()
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 3, 1, 1 });
	}

	static BlockPos archeryShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.ARROW), new ItemStack(Items.SPECTRAL_ARROW),
			PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW), PotionTypes.HEALING),
			PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW), PotionTypes.POISON)
		};
		return fillShelves(client, origin, samples, new int[] { 6, 6, 6, 6 });
	}

	static BlockPos cookingIngredientShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.EGG), new ItemStack(Items.MILK_BUCKET),
			new ItemStack(Blocks.BROWN_MUSHROOM), new ItemStack(Blocks.RED_MUSHROOM),
			new ItemStack(Items.WHEAT), new ItemStack(Items.REEDS),
			new ItemStack(Items.SUGAR), new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage())
		};
		return fillShelves(client, origin, samples, new int[] { 6, 2, 6, 6, 3, 3, 2, 2 });
	}

	static BlockPos remainingFoodShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(Items.WHEAT_SEEDS),
			new ItemStack(Items.BEETROOT_SEEDS), new ItemStack(Items.MELON_SEEDS),
			new ItemStack(Items.POISONOUS_POTATO), new ItemStack(Items.CHORUS_FRUIT),
			new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.SPIDER_EYE)
		};
		return fillShelves(client, origin, samples, new int[] { 2, 2, 2, 2, 6, 6, 2, 6 });
	}

	static BlockPos brewingPowderShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.REDSTONE), new ItemStack(Items.GLOWSTONE_DUST),
			new ItemStack(Items.GUNPOWDER), new ItemStack(Items.BLAZE_POWDER),
			new ItemStack(Items.NETHER_WART), new ItemStack(Items.FERMENTED_SPIDER_EYE),
			new ItemStack(Items.MAGMA_CREAM), new ItemStack(Items.GHAST_TEAR)
		};
		return fillShelves(client, origin, samples, new int[] { 2, 2, 2, 2, 6, 6, 6, 6 });
	}

	static BlockPos brewingIngredientShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.SPECKLED_MELON), new ItemStack(Items.RABBIT_FOOT),
			new ItemStack(Items.DRAGON_BREATH), new ItemStack(Items.SUGAR),
			new ItemStack(Items.FISH, 1, 3), new ItemStack(Items.SPIDER_EYE),
			new ItemStack(Items.GOLDEN_CARROT), new ItemStack(Items.MELON)
		};
		return fillShelves(client, origin, samples, new int[] { 6, 3, 8, 2, 3, 6, 15, 6 });
	}

	static BlockPos smithingShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT),
			new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.QUARTZ),
			new ItemStack(Items.COAL, 1, 1), new ItemStack(Items.COAL, 1, 0),
			new ItemStack(Items.EMERALD), new ItemStack(Items.DIAMOND)
		};
		return fillShelves(client, origin, samples, new int[] { 6, 6, 6, 6, 6, 6, 6, 6 });
	}

	static BlockPos containerShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.BUCKET), new ItemStack(Items.BOWL),
			new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.WATER_BUCKET),
			new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.BOWL),
			new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)
		};
		return fillShelves(client, origin, samples, new int[] { 2, 6, 2, 2, 2, 2, 1, 1 });
	}

	static BlockPos travelShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.CLOCK), new ItemStack(Items.COMPASS),
			TravelGearClientProbe.galleryMap(), new ItemStack(Items.MAP),
			new ItemStack(Items.LEAD), new ItemStack(Items.SADDLE)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 3, 2, 1 });
	}

	static BlockPos armourShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			TrophyShelfSamples.armour(Items.GOLDEN_HELMET), TrophyShelfSamples.armour(Items.IRON_CHESTPLATE),
			TrophyShelfSamples.armour(Items.CHAINMAIL_LEGGINGS), TrophyShelfSamples.armour(Items.CHAINMAIL_BOOTS),
			TrophyShelfSamples.armour(Items.LEATHER_CHESTPLATE), TrophyShelfSamples.armour(Items.LEATHER_HELMET),
			TrophyShelfSamples.armour(Items.DIAMOND_BOOTS), TrophyShelfSamples.armour(Items.DIAMOND_CHESTPLATE),
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 1, 1 });
	}

	static BlockPos trophyShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			TrophyShelfSamples.head(0), TrophyShelfSamples.head(1),
			TrophyShelfSamples.head(2), TrophyShelfSamples.head(3),
			TrophyShelfSamples.head(4), TrophyShelfSamples.head(5),
			TrophyShelfSamples.stand(), new ItemStack(Items.ARMOR_STAND),
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 1, 1 });
	}

	static BlockPos craftingSupplyShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.RABBIT_HIDE), new ItemStack(Items.LEATHER),
			new ItemStack(Items.STICK), new ItemStack(Items.STRING),
			new ItemStack(Items.PAPER), new ItemStack(Items.FEATHER),
			new ItemStack(Items.PAPER), new ItemStack(Items.NAME_TAG).setStackDisplayName("Stable keeper")
		};
		return fillShelves(client, origin, samples, new int[] { 2, 2, 3, 3, 6, 3, 1, 1 });
	}

	static BlockPos dyeShelves(Minecraft client, BlockPos origin, int firstColour) {
		ItemStack[] samples = new ItemStack[8];
		int[] amounts = new int[samples.length];

		for (int index = 0; index < samples.length; index++) {
			// Place the colours in reading order from the gallery camera.
			int column = 3 - index / 2;
			int row = index % 2 == 1 ? 0 : 4;
			EnumDyeColor colour = EnumDyeColor.byDyeDamage(firstColour + row + column);
			samples[index] = new ItemStack(Items.DYE, 1, colour.getDyeDamage());
			amounts[index] = colour == EnumDyeColor.BLACK || colour == EnumDyeColor.BLUE ? 6 : 2;
		}

		return fillShelves(client, origin, samples, amounts);
	}

	static BlockPos mountedGearShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			TrophyShelfSamples.patternedBanner(EnumDyeColor.YELLOW), TrophyShelfSamples.patternedBanner(EnumDyeColor.BLUE),
			TrophyShelfSamples.banner(EnumDyeColor.RED), new ItemStack(Items.DIAMOND_HORSE_ARMOR),
			TrophyShelfSamples.elytra(), new ItemStack(Items.GOLDEN_HORSE_ARMOR),
			new ItemStack(Items.ELYTRA), new ItemStack(Items.IRON_HORSE_ARMOR)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 1, 1 });
	}

	static BlockPos materialDropShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.SNOWBALL), new ItemStack(Items.SLIME_BALL),
			new ItemStack(Items.CLAY_BALL), new ItemStack(Items.FLINT),
			new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.BLAZE_ROD),
			new ItemStack(Items.BONE), new ItemStack(Items.BONE)
		};
		return fillShelves(client, origin, samples, new int[] { 6, 6, 6, 6, 1, 3, 1, 3 });
	}

	static BlockPos prismarineAndChorusShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.CHORUS_FRUIT_POPPED), new ItemStack(Items.CHORUS_FRUIT_POPPED),
			new ItemStack(Items.CHORUS_FRUIT), new ItemStack(Items.CHORUS_FRUIT),
			new ItemStack(Items.PRISMARINE_CRYSTALS), new ItemStack(Items.PRISMARINE_CRYSTALS),
			new ItemStack(Items.PRISMARINE_SHARD), new ItemStack(Items.PRISMARINE_SHARD)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 6, 1, 6, 1, 6, 1, 6 });
	}

	static BlockPos magicShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.FIRE_CHARGE), new ItemStack(Items.FIRE_CHARGE),
			new ItemStack(Items.NETHER_STAR), new ItemStack(Items.END_CRYSTAL),
			new ItemStack(Items.ENDER_EYE), new ItemStack(Items.ENDER_EYE),
			new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_PEARL)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 6, 1, 1, 1, 6, 1, 6 });
	}

	static BlockPos fireworkAndEggShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			MagicShelfSamples.egg("Skeleton"), MagicShelfSamples.egg("Skeleton"),
			MagicShelfSamples.egg("Pig"), MagicShelfSamples.egg("Creeper"),
			MagicShelfSamples.star(MagicShelfSamples.BLUE), MagicShelfSamples.star(MagicShelfSamples.RED),
			MagicShelfSamples.rocket(2), MagicShelfSamples.rocket(1)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 6, 6, 6, 6, 6, 1, 3 });
	}

	static BlockPos boatShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.COMMAND_BLOCK_MINECART), new ItemStack(Items.BOAT),
			new ItemStack(Items.SPRUCE_BOAT), new ItemStack(Items.BIRCH_BOAT),
			new ItemStack(Items.JUNGLE_BOAT), new ItemStack(Items.ACACIA_BOAT),
			new ItemStack(Items.DARK_OAK_BOAT), new ItemStack(Items.MINECART)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 1, 1 });
	}

	static BlockPos cartAndDecorShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.PAINTING), new ItemStack(Items.ITEM_FRAME),
			new ItemStack(Items.MINECART), new ItemStack(Items.CHEST_MINECART),
			new ItemStack(Items.TNT_MINECART), new ItemStack(Items.FURNACE_MINECART),
			new ItemStack(Items.HOPPER_MINECART), new ItemStack(Items.COMMAND_BLOCK_MINECART)
		};
		fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 1, 1 });
		TileEntityShelvingUnit shelf = (TileEntityShelvingUnit)client.world.getTileEntity(origin.add(1, 0, 2));
		shelf.getLevel(false).setLabel(new ItemStack(Items.SIGN), "Transport");
		shelf.getLevel(true).setLabel(new ItemStack(Items.SIGN), "Chest cart");
		return origin;
	}

	static BlockPos potShelves(Minecraft client, BlockPos origin) {
		Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		for (int column = 0; column < 2; column++) {
			BlockPos pos = origin.add(1 + column, 0, 2);
			client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
			client.world.setBlockState(pos, shelf.getDefaultState()
				.withProperty(RackBlock.STANDING, true).withProperty(RackBlock.DATA, true), 2);
			TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(pos);
			tile.getLevel(false).getSurfaceSetting().setSingleItem(
				galleryPot(column == 0 ? new ItemStack(Blocks.TALLGRASS, 1, 2) : null), EnumFacing.NORTH);
			tile.getLevel(true).getSurfaceSetting().setSingleItem(
				galleryPot(new ItemStack(Blocks.RED_FLOWER, 1, column)), EnumFacing.NORTH);
			tile.getLevel(false).setLabel(new ItemStack(Items.SIGN), column == 0 ? "Fern" : "Ready to plant");
			tile.getLevel(true).setLabel(new ItemStack(Items.SIGN), column == 0 ? "Poppy" : "Blue orchid");
		}
		return origin;
	}

	private static ItemStack galleryPot(ItemStack plant) {
		ItemStack pot = new ItemStack(Items.FLOWER_POT);
		if (plant != null) {
			net.minecraft.nbt.NBTTagCompound tag = new net.minecraft.nbt.NBTTagCompound();
			tag.setTag("ShelfPlant", plant.writeToNBT(new net.minecraft.nbt.NBTTagCompound()));
			pot.setTagCompound(tag);
		}
		return pot;
	}

	static BlockPos utilityShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.REPEATER), new ItemStack(Items.COMPARATOR),
			new ItemStack(Items.BREWING_STAND), new ItemStack(Items.CAULDRON)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1 });
	}

	static BlockPos doorAndBedShelves(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			new ItemStack(Items.BED), new ItemStack(Items.OAK_DOOR),
			new ItemStack(Items.SPRUCE_DOOR), new ItemStack(Items.BIRCH_DOOR),
			new ItemStack(Items.JUNGLE_DOOR), new ItemStack(Items.ACACIA_DOOR),
			new ItemStack(Items.DARK_OAK_DOOR), new ItemStack(Items.IRON_DOOR)
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 1, 1 });
	}

	static BlockPos produceFurniture(Minecraft client, BlockPos origin) {
		Block trayBlock = Block.getBlockFromName("ironagefurniture:produce_tray");
		Block rackBlock = Block.getBlockFromName("ironagefurniture:produce_rack");
		ItemStack[] stock = {
			new ItemStack(Items.POTATO), new ItemStack(Items.CARROT), new ItemStack(Items.APPLE),
			new ItemStack(Items.BEETROOT), new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.BREAD),
			new ItemStack(Items.WHEAT), new ItemStack(Items.COOKIE), new ItemStack(OptionalIntegrationRecipeProbe.SHELF_LAYOUT_SAMPLE)
		};
		String[] woods = { "spruce", "oak", "birch" };
		if (client.getRenderItem().getItemModelWithOverrides(ItemBlockProduceFurniture.empty(trayBlock, "birch", 1), null, null)
				== client.getRenderItem().getItemModelMesher().getModelManager().getMissingModel()
				|| client.getRenderItem().getItemModelWithOverrides(ItemBlockProduceFurniture.empty(rackBlock, "spruce", 1), null, null)
				== client.getRenderItem().getItemModelMesher().getModelManager().getMissingModel()) {
			throw new IllegalStateException("Missing produce furniture inventory model");
		}
		for (int column = 0; column < 3; column++) {
			BlockPos pos = origin.add(column, 0, 2);
			client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
			client.world.setBlockState(pos, rackBlock.getDefaultState(), 2);
			client.world.setBlockState(pos.up(), rackBlock.getDefaultState().withProperty(ProduceFurniture.SECOND, true), 2);
			TileEntityProduceFurniture rack = (TileEntityProduceFurniture)client.world.getTileEntity(pos);
			rack.setWood("oak");
			for (int slot = 0; slot < 3; slot++) {
				rack.setTray(slot, produceSample(woods[slot], stock[3 * column + slot]));
			}
		}
		BlockPos pair = origin.add(4, 0, 2);
		client.world.setBlockState(pair.down(), Blocks.STONEBRICK.getDefaultState(), 2);
		client.world.setBlockState(pair, trayBlock.getDefaultState().withProperty(ProduceFurniture.SECOND, true), 2);
		TileEntityProduceFurniture stack = (TileEntityProduceFurniture)client.world.getTileEntity(pair);
		stack.setTray(0, produceSample("spruce", stock[3]));
		stack.setTray(1, produceSample("birch", stock[2]));
		client.world.setBlockState(pair.up(), trayBlock.getDefaultState(), 2);
		TileEntityProduceFurniture top = (TileEntityProduceFurniture)client.world.getTileEntity(pair.up());
		top.setTray(0, produceSample("oak", stock[1]));
		return origin;
	}

	private static ProduceTray produceSample(String wood, ItemStack stock) {
		ProduceTray tray = new ProduceTray(wood);
		for (int slot = 0; slot < ProduceTray.capacity(stock); slot++) {
			tray.offer(stock, false);
		}
		return tray;
	}

	static void showFurnitureInventory(Minecraft client) {
		Block tray = Block.getBlockFromName("ironagefurniture:produce_tray");
		Block rack = Block.getBlockFromName("ironagefurniture:produce_rack");
		Block vase = Block.getBlockFromName("ironagefurniture:ornament_glass_vase");
		String[] categories = { "Produce Tray", "Market Rack", "Tool Rack", "Dining Chair", "Glass Vase" };
		String[] woods = { "oak", "birch", "big_oak" };
		EnumDyeColor[] colours = { EnumDyeColor.WHITE, EnumDyeColor.CYAN, EnumDyeColor.PURPLE };
		ItemStack[][] samples = new ItemStack[woods.length][categories.length];
		for (int row = 0; row < woods.length; row++) {
			String wood = woods[row];
			samples[row][0] = ItemBlockProduceFurniture.empty(tray, wood, 1);
			samples[row][1] = ItemBlockProduceFurniture.empty(rack, wood, 1);
			samples[row][2] = modItem("tool_rack_wood_ironage_" + wood);
			samples[row][3] = modItem("chair_wood_ironage_dining_" + wood);
			samples[row][4] = new ItemStack(vase, 1, colours[row].getMetadata());
		}

		client.displayGuiScreen(new GuiScreen() {
			@Override
			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				this.drawDefaultBackground();
				int y = this.height / 2 - 33;
				for (int column = 0; column < categories.length; column++) {
					int x = this.width / 2 + (column - 2) * 66;
					this.drawCenteredString(this.fontRendererObj, categories[column], x, y - 13, 0xFFFFFF);
					for (int row = 0; row < samples.length; row++) {
						int top = y + 22 * row;
						drawRect(x - 9, top, x + 9, top + 18, 0xFFE0E0E0);
						drawRect(x - 8, top + 1, x + 8, top + 17, 0xFF888888);
						RenderHelper.enableGUIStandardItemLighting();
						client.getRenderItem().renderItemAndEffectIntoGUI(samples[row][column], x - 8, top + 1);
						RenderHelper.disableStandardItemLighting();
					}
				}
			}
		});
	}

	static BlockPos lightingShelves(Minecraft client, BlockPos origin) {
		Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		ItemStack[] props = {
			new ItemStack(Items.LAVA_BUCKET),
			modItem("obsidian_chunk"),
			modItem("light_metal_ironage_block_floor_lava_clear"),
			modItem("light_metal_ironage_sconce_floor_empty_iron"),
			modItem("light_metal_ironage_block_floor_glow_clear"),
			modItem("light_metal_ironage_block_floor_red_clear"),
			modItem("light_metal_ironage_candle_floor"),
			modItem("tallow")
		};

		for (int column = 0; column < 4; column++) {
			boolean standing = column >= 2;
			BlockPos pos = origin.add(column + (standing ? 1 : 0), 0, 2);
			client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
			if (!standing) {
				client.world.setBlockState(pos.south(), Blocks.STONEBRICK.getDefaultState(), 2);
			}
			client.world.setBlockState(pos, shelf.getDefaultState().withProperty(RackBlock.STANDING, standing)
				.withProperty(RackBlock.DATA, true), 2);
			TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(pos);
			int first = 2 * (3 - column);
			putLightingSample(tile.getLevel(false), props[first + 1]);
			putLightingSample(tile.getLevel(true), props[first]);
			client.world.checkLight(pos);
		}
		return origin;
	}

	private static void putLightingSample(TileEntityShelvingUnit.ShelfLevel level, ItemStack stack) {
		if (level.addMedia(stack)) {
			level.addMedia(stack);
		} else {
			level.getSurfaceSetting().setSingleItem(stack, EnumFacing.NORTH);
		}
	}

	private static ItemStack modItem(String name) {
		return new ItemStack(net.minecraft.item.Item.getByNameOrId("ironagefurniture:" + name));
	}

	static BlockPos baseMetalMaterials(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			MetalShelfSamples.item("copper_ingot"), MetalShelfSamples.item("silver_nugget"),
			MetalShelfSamples.item("bronze_powder"), MetalShelfSamples.item("steel_blend"),
			MetalShelfSamples.item("copper_plate"), MetalShelfSamples.item("iron_gear"),
			MetalShelfSamples.item("copper_rod"), MetalShelfSamples.item("steel_crackhammer")
		};
		return fillShelves(client, origin, samples, new int[] { 6, 6, 2, 2, 2, 2, 1, 1 });
	}

	static BlockPos baseMetalGear(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			MetalShelfSamples.item("steel_boots"), MetalShelfSamples.item("steel_leggings"),
			MetalShelfSamples.item("copper_chestplate"), MetalShelfSamples.item("copper_helmet"),
			MetalShelfSamples.item("copper_ore"), MetalShelfSamples.item("copper_bars"),
			MetalShelfSamples.item("copper_door"), MetalShelfSamples.mercuryBucket()
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 1, 1 });
	}

	static BlockPos baseMetalHardware(Minecraft client, BlockPos origin) {
		ItemStack[] samples = {
			MetalShelfSamples.item("copper_trapdoor"), MetalShelfSamples.item("human_detector"),
			MetalShelfSamples.item("copper_block"), MetalShelfSamples.item("tin_door"),
			MetalShelfSamples.item("mercury_ingot"), MetalShelfSamples.item("mercury_powder")
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 6, 2 });
	}

	static BlockPos modernMetalMaterials(Minecraft client, BlockPos origin) {
		String mod = "modernmetals";
		ItemStack[] samples = {
			MetalShelfSamples.item(mod, "chromium_powder"), MetalShelfSamples.item(mod, "titanium_nugget"),
			MetalShelfSamples.item(mod, "aluminum_ingot"), MetalShelfSamples.item(mod, "tungsten_ingot"),
			MetalShelfSamples.item(mod, "aluminum_casing"), MetalShelfSamples.item(mod, "titanium_ore"),
			MetalShelfSamples.bucket("aluminum"), MetalShelfSamples.bucket("aluminumbrass")
		};
		return fillShelves(client, origin, samples, new int[] { 2, 6, 6, 6, 2, 1, 1, 1 });
	}

	static BlockPos modernMetalGear(Minecraft client, BlockPos origin) {
		String mod = "modernmetals";
		ItemStack[] samples = {
			MetalShelfSamples.item(mod, "titanium_horse_armor"), MetalShelfSamples.item(mod, "titanium_shield"),
			MetalShelfSamples.item(mod, "titanium_helmet"), MetalShelfSamples.item(mod, "tungsten_crackhammer"),
			MetalShelfSamples.item(mod, "aluminum_door"), MetalShelfSamples.item(mod, "aluminum_trapdoor"),
			MetalShelfSamples.item(mod, "aluminum_arrow"), MetalShelfSamples.item(mod, "titanium_bolt")
		};
		return fillShelves(client, origin, samples, new int[] { 1, 1, 1, 1, 1, 1, 6, 6 });
	}

	static BlockPos baseMineralSamples(Minecraft client, BlockPos origin) {
		String mod = "baseminerals";
		ItemStack[] samples = {
			MetalShelfSamples.item(mod, "salt_powder"), MetalShelfSamples.item(mod, "sulfur_powder"),
			MetalShelfSamples.item(mod, "salt_ore"), MetalShelfSamples.item(mod, "potash_block"),
			MetalShelfSamples.item(mod, "lithium_ingot"), MetalShelfSamples.item(mod, "silicon_ingot"),
			MetalShelfSamples.item(mod, "lithium_nugget"), MetalShelfSamples.bucket("lithium")
		};
		return fillShelves(client, origin, samples, new int[] { 2, 2, 1, 1, 6, 6, 6, 2 });
	}

	static BlockPos baseGemPlatters(Minecraft client, BlockPos origin) {
		String[][] collections = {
			{ "amethyst", "aquamarine", "jade", "lepidolite", "indicolite", "agate" },
			{ "ruby", "sapphire", "opal", "peridot", "blackdiamond", "amber" },
			{ "spinel", "turquoise", "tanzanite", "alexandrite", "violetsapphire", "morganite" },
			{ "citrine", "goldenberyl", "heliodor", "carnelian", "topaz", "garnet" },
			{ "iolite", "moldavite", "onyx", "jasper", "bluetopaz", "beryl" },
			{ "malachite", "ametrine", "moonstone" }
		};
		Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		for (int section = 0; section < collections.length / 2; section++) {
			boolean standing = section == 2;
			BlockPos pos = origin.add(section + (standing ? 1 : 0), 0, 2);
			client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
			if (!standing) {
				client.world.setBlockState(pos.south(), Blocks.STONEBRICK.getDefaultState(), 2);
			}
			client.world.setBlockState(pos, shelf.getDefaultState()
				.withProperty(RackBlock.STANDING, standing).withProperty(RackBlock.DATA, true), 2);
			TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(pos);
			for (int tier = 0; tier < 2; tier++) {
				for (String gem : collections[section * 2 + tier]) {
					if (!tile.getLevel(tier == 1).addMedia(MetalShelfSamples.item("basegems", gem + "_ingot"))) {
						throw new IllegalStateException("Could not display gemstone " + gem);
					}
				}
			}
		}
		return origin;
	}

	private static BlockPos fillShelves(Minecraft client, BlockPos origin, ItemStack[] samples, int[] amounts) {
		Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");

		for (int x = 0; x < samples.length / 2; x++) {
			boolean standing = x >= 2;
			BlockPos pos = origin.add(x + (standing ? 1 : 0), 0, 2);
			client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);

			if (!standing) {
				client.world.setBlockState(pos.south(), Blocks.STONEBRICK.getDefaultState(), 2);
			}

			client.world.setBlockState(pos, shelf.getDefaultState()
				.withProperty(RackBlock.STANDING, standing).withProperty(RackBlock.DATA, true), 2);
			TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(pos);
			for (int tier = 0; tier < 2; tier++) {
				int sample = x * 2 + tier;
				for (int count = 0; count < amounts[sample]; count++) {
					if (!tile.getLevel(tier == 1).addMedia(samples[sample])) {
						throw new IllegalStateException("Could not fill shelving gallery with " + samples[sample]);
					}
				}
			}
		}

		return origin;
	}
}
