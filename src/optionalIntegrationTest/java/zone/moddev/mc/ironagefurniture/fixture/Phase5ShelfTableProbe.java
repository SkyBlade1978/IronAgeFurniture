package zone.moddev.mc.ironagefurniture.fixture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.LowTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceItemHit;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDiningTable;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;

/** Cross-check new wood recipes against real chair/slab recipes, then exercise furniture interactions. */
final class Phase5ShelfTableProbe {
	private Phase5ShelfTableProbe() { }

	static void verify(WorldServer world, Set<String> mods) {
		List<String> woods = new ArrayList<String>();
		for (String id : Ironagefurniture.BlockRegistry.keySet()) {
			if (id.startsWith("chair_wood_ironage_classic_")) woods.add(id.substring("chair_wood_ironage_classic_".length()));
		}
		Collections.sort(woods);
		for (String wood : woods) verifyWoodRecipes(world, wood);
		FakePlayer player = FakePlayerFactory.getMinecraft(world);
		NetHandlerPlayServer oldConnection = player.connection;
		GameType oldMode = player.interactionManager.getGameType();
		player.connection = new NetHandlerPlayServer(world.getMinecraftServer(),
			new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
			@Override public void sendPacket(Packet<?> packet) { }
		};
		try {
			player.inventory.clear(); player.setSneaking(false);
			BlockPos pos = new BlockPos(32, 110, 32);
			for (String id : new String[]{"table_low_oak", "shelf_wall_oak"}) {
				for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
					verifyLoose(world, player, block(id), pos, facing, mods);
					verifyEmbedded(world, player, block(id), pos.south(6), facing);
					pos = pos.east(8);
				}
			}
			verifyShelfShapes(world, player, pos.south(80));
			verifyStackedShelves(world, player, pos.south(120));
			verifyAddonPair(world, player, mods, pos.south(160));
			persistArrangements(world, player, pos.south(200));
		} finally {
			player.inventory.clear(); player.setSneaking(false);
			player.interactionManager.setGameType(oldMode); player.connection = oldConnection;
		}
		LogManager.getLogger().info("IAF PHASE 5 SHELF/TABLE PROBE PASSED: {} chair woods, four furniture recipes per wood", woods.size());
	}

	private static void verifyWoodRecipes(WorldServer world, String wood) {
		Item chair = Item.getItemFromBlock(block("chair_wood_ironage_classic_" + wood));
		ItemStack plank = null;
		for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
			if (recipe.getRecipeOutput() == null || recipe.getRecipeOutput().getItem() != chair || !(recipe instanceof ShapedOreRecipe)) continue;
			for (Object ingredient : ((ShapedOreRecipe)recipe).getInput()) {
				if (ingredient instanceof ItemStack) { plank = ((ItemStack)ingredient).copy(); break; }
			}
			if (plank != null) break;
		}
		require(plank != null, "Missing chair plank reference for " + wood);
		InventoryCrafting grid = grid();
		for (int i = 0; i < 3; i++) grid.setInventorySlotContents(i, plank.copy());
		ItemStack slab = CraftingManager.getInstance().findMatchingRecipe(grid, world);
		require(slab != null, "No actual slab recipe for chair wood " + wood + ": " + plank);
		slab = slab.copy(); slab.stackSize = 1;
		for (String prefix : new String[]{"table_dining_", "table_low_", "shelf_wall_", "cabinet_wood_ironage_"}) {
			Block output = block(prefix + wood);
			grid.clear();
			if (prefix.startsWith("cabinet")) {
				grid.setInventorySlotContents(0, new ItemStack(Blocks.CHEST));
				grid.setInventorySlotContents(4, plank.copy());
				grid.setInventorySlotContents(8, OreDictionary.getOres("nuggetIron").isEmpty()
					? new ItemStack(Items.IRON_INGOT) : OreDictionary.getOres("nuggetIron").get(0).copy());
			} else {
				for (int i = 0; i < 3; i++) grid.setInventorySlotContents(i, slab.copy());
				if (prefix.equals("table_dining_")) {
					grid.setInventorySlotContents(3, new ItemStack(Items.STICK)); grid.setInventorySlotContents(5, new ItemStack(Items.STICK));
				} else if (prefix.equals("table_low_")) {
					grid.setInventorySlotContents(4, slab.copy()); grid.setInventorySlotContents(6, new ItemStack(Items.STICK)); grid.setInventorySlotContents(8, new ItemStack(Items.STICK));
				} else grid.setInventorySlotContents(4, new ItemStack(Items.STICK));
			}
			ItemStack crafted = CraftingManager.getInstance().findMatchingRecipe(grid, world);
			require(crafted != null && crafted.getItem() == Item.getItemFromBlock(output)
				&& crafted.stackSize == (prefix.equals("shelf_wall_") ? 3 : 1), "Wrong furniture recipe for " + prefix + wood + "; got " + crafted);
			require(Blocks.FIRE.getFlammability(output) == 20, "Wood furniture is not flammable: " + prefix + wood);
		}
	}

	private static void verifyLoose(WorldServer world, FakePlayer player, Block host, BlockPos pos, EnumFacing facing, Set<String> mods) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		items.add(new ItemStack(Items.BREAD)); items.add(new ItemStack(Items.APPLE));
		items.add(new ItemStack(Items.ARMOR_STAND)); items.add(new ItemStack(Blocks.STONE));
		if (mods.contains("harvestcraft")) items.add(new ItemStack(Item.getByNameOrId("harvestcraft:applepieitem")));
		for (ItemStack original : items) for (GameType mode : new GameType[]{GameType.CREATIVE, GameType.SURVIVAL}) {
			prepare(world, pos, host, facing); player.inventory.clear();
			player.interactionManager.setGameType(GameType.SURVIVAL);
			ItemStack item = tagged(original.copy(), "Low surface " + mode + " " + pos);
			require(click(world, player, pos, item.copy()), "Cannot place loose item on " + host);
			SurfaceSettingHost surface = (SurfaceSettingHost)world.getTileEntity(pos);
			require(ItemStack.areItemStacksEqual(item, ((SurfaceSettingHost)TileEntity.create(world,
				((TileEntity)surface).writeToNBT(new NBTTagCompound()))).getSurfaceSetting().getCompatibilityItem()), "Low surface NBT changed");
			AxisAlignedBB bounds = SurfaceItemHit.bounds(surface, zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot.CENTER, false).offset(pos);
			double x = (bounds.minX + bounds.maxX) / 2.0D, z = (bounds.minZ + bounds.maxZ) / 2.0D;
			RayTraceResult hit = host.collisionRayTrace(world.getBlockState(pos), world, pos,
				new Vec3d(x, pos.getY() + 3.0D, z), new Vec3d(x, pos.getY(), z));
			require(hit != null && hit.subHit >= 100, "Ray trace hit furniture before loose item");
			player.setHeldItem(EnumHand.MAIN_HAND, null);
			player.setPositionAndRotation(x, pos.getY() + 2.0D, z, 0.0F, 90.0F);
			player.interactionManager.setGameType(mode);
			require(player.interactionManager.tryHarvestBlock(pos), "Cannot mine loose item in owner's cell");
			require(world.getBlockState(pos).getBlock() == host && !surface.getSurfaceSetting().hasAnyItem(), "Mining loose item removed furniture or retained a ghost");
			require(countDrops(world, pos, item) == (mode == GameType.CREATIVE ? 0 : 1), "Loose item mining lost/duplicated data");
			world.setBlockToAir(pos); world.setBlockToAir(pos.up());
			pos = pos.east(4);
		}
		player.interactionManager.setGameType(GameType.SURVIVAL);
	}

	private static void verifyEmbedded(WorldServer world, FakePlayer player, Block host, BlockPos pos, EnumFacing facing) {
		for (String light : new String[]{"light_metal_ironage_candle_floor", "light_metal_ironage_block_floor_glow_clear", "light_metal_ironage_block_floor_lava_clear"}) {
			prepare(world, pos, host, facing); player.inventory.clear();
			ItemStack stack = tagged(new ItemStack(block(light)), "Exact embedded " + light);
			require(click(world, player, pos, stack.copy()), "Cannot embed light");
			require(world.isAirBlock(pos.up()) && host.getLightValue(world.getBlockState(pos), world, pos) == (light.contains("candle") ? 12 : 15),
				"Embedded light failed or reserved space above: " + host.getRegistryName() + " / " + light + " at " + pos);
			TileEntity before = world.getTileEntity(pos), after = TileEntity.create(world, before.writeToNBT(new NBTTagCompound()));
			require(after.writeToNBT(new NBTTagCompound()).equals(before.writeToNBT(new NBTTagCompound())), "Embedded light changed during save");
			world.setBlockToAir(pos);
			require(countDrops(world, pos, stack) == 1, "Embedded light drop lost/duplicated data");
			pos = pos.east(4);
		}
		prepare(world, pos, host, facing); player.inventory.clear();
		ItemStack pot = tagged(new ItemStack(Items.FLOWER_POT), "Exact pot");
		ItemStack plant = tagged(new ItemStack(Blocks.RED_FLOWER, 1, 4), "Exact plant");
		require(click(world, player, pos, pot.copy()) && click(world, player, pos, plant.copy()), "Cannot plant embedded flower pot");
		world.setBlockToAir(pos);
		require(countDrops(world, pos, pot) == 1 && countDrops(world, pos, plant) == 1, "Potted plant break lost exact contents");
		if (!(host instanceof WallShelf)) return;
		pos = pos.east(4); prepare(world, pos, host, facing);
		List<ItemStack> books = new ArrayList<ItemStack>();
		for (int i = 0; i < 5; i++) {
			ItemStack book = tagged(new ItemStack(Items.WRITTEN_BOOK), "Book " + i);
			NBTTagList pages = new NBTTagList(); pages.appendTag(new net.minecraft.nbt.NBTTagString("{\"text\":\"Original page " + i + "\"}"));
			book.getTagCompound().setTag("pages", pages); book.getTagCompound().setString("author", "IAF probe");
			books.add(book); require(click(world, player, pos, book.copy()), "Cannot add embedded book");
		}
		TileEntityWallShelf shelf = (TileEntityWallShelf)world.getTileEntity(pos);
		require(shelf.getEmbeddedCount() == 5 && shelf.getShelfContents() == WallShelf.ShelfContents.BOOKS_5, "Book rack count/model wrong");
		world.setBlockToAir(pos);
		for (ItemStack book : books) require(countDrops(world, pos, book) == 1, "Book NBT lost on drop");
		pos = pos.east(4); prepare(world, pos, host, facing);
		for (int i = 0; i < 6; i++) require(click(world, player, pos,
			tagged(new ItemStack(Items.RECORD_CAT), "Record " + i)), "Cannot add record");
		require(((TileEntityWallShelf)world.getTileEntity(pos)).getShelfContents() == WallShelf.ShelfContents.RECORDS_6, "Record rack count/model wrong");
		world.setBlockToAir(pos);
		for (int i = 0; i < 6; i++) require(countDrops(world, pos, tagged(new ItemStack(Items.RECORD_CAT), "Record " + i)) == 1, "Record data lost on drop");
	}

	private static void verifyShelfShapes(WorldServer world, FakePlayer player, BlockPos start) {
		Block shelf = block("shelf_wall_oak");
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			BlockPos pos = start.offset(facing, 12);
			prepare(world, pos, shelf, facing);
			EnumFacing right = facing.rotateY();
			world.setBlockState(pos.offset(right).offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
			ItemStack stack = new ItemStack(shelf);
			player.setHeldItem(EnumHand.MAIN_HAND, stack);
			require(shelf.onBlockActivated(world, pos, world.getBlockState(pos), player, EnumHand.MAIN_HAND,
				stack, right, 0.5F, 0.8F, 0.5F) && world.getBlockState(pos.offset(right)).getBlock() == shelf,
				"Shelf smart extension failed for " + facing);
			BlockPos filler = pos.offset(facing, 4);
			BlockPos front = filler.offset(facing);
			world.setBlockState(front.offset(right.getOpposite()), Blocks.STONE.getDefaultState());
			world.setBlockState(front, shelf.getDefaultState().withProperty(net.minecraft.block.BlockHorizontal.FACING, right));
			IBlockState state = shelf.getDefaultState().withProperty(net.minecraft.block.BlockHorizontal.FACING, facing);
			world.setBlockState(filler, state);
			require(shelf.getActualState(world.getBlockState(filler), world, filler).getValue(WallShelf.SUPPORT)
				== WallShelf.ShelfSupport.OUTER_CORNER, "Unsupported outside shelf corner not solved for " + facing);
			world.setBlockToAir(front);
			require(world.isAirBlock(filler), "Unsupported shelf corner did not fall after support loss");
		}
	}

	private static void verifyStackedShelves(WorldServer world, FakePlayer player, BlockPos pos) {
		Block shelf = block("shelf_wall_oak");
		prepare(world, pos, shelf, EnumFacing.NORTH);
		prepare(world, pos.up(), shelf, EnumFacing.NORTH);
		ItemStack apple = tagged(new ItemStack(Items.APPLE), "Stacked shelf apple");
		require(click(world, player, pos, apple.copy()) && world.getBlockState(pos.up()).getBlock() == shelf, "Stacked shelf display replaced upper shelf");
		world.setBlockToAir(pos.up());
		require(world.getBlockState(pos.up()).getBlock() == block("surface_display_blocker"), "Removing upper shelf did not restore display reservation");
		world.setBlockToAir(pos);
		require(world.isAirBlock(pos.up()) && countDrops(world, pos, apple) == 1, "Stacked shelf break lost food or retained blocker");
	}

	private static void verifyAddonPair(WorldServer world, FakePlayer player, Set<String> mods, BlockPos pos) {
		if (!mods.contains("ironagebrewing")) return;
		Item drinkware = Item.getByNameOrId("ironagebrewing:drinkware");
		for (String id : new String[]{"table_low_oak", "shelf_wall_oak"}) {
			prepare(world, pos, block(id), EnumFacing.NORTH); player.inventory.clear();
			ItemStack bread = tagged(new ItemStack(Items.BREAD), "Addon pair loaf " + id);
			ItemStack cup = tagged(new ItemStack(drinkware), "Addon pair cup " + id);
			require(click(world, player, pos, bread.copy()), "Cannot place food for addon pairing");
			SurfaceSettingHost host = (SurfaceSettingHost)world.getTileEntity(pos);
			player.setHeldItem(EnumHand.MAIN_HAND, cup.copy());
			IBlockState state = world.getBlockState(pos);
			require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND,
				player.getHeldItemMainhand(), EnumFacing.UP, (float)(0.8D + host.getSurfaceOffsetX()), 0.85F,
				(float)(0.5D + host.getSurfaceOffsetZ())), "Cannot pair food and addon drinkware");
			require(((SurfaceSettingHost)world.getTileEntity(pos)).getSurfaceSetting().getItemCount() == 2,
				"Addon drinkware was diverted from an existing food setting: " + id);
			world.setBlockToAir(pos);
			require(countDrops(world, pos, bread) == 1 && countDrops(world, pos, cup) == 1, "Addon place setting lost exact items");
			pos = pos.east(4);
		}
		prepare(world, pos, block("shelf_wall_oak"), EnumFacing.NORTH);
		for (int i = 0; i < 3; i++) require(click(world, player, pos,
			tagged(new ItemStack(drinkware), "Stored addon cup " + i)), "Cannot store addon vessel arrangement");
		require(((TileEntityWallShelf)world.getTileEntity(pos)).getEmbeddedCount() == 3, "Addon vessel arrangement did not retain its count");
		world.setBlockToAir(pos);
		for (int i = 0; i < 3; i++) require(countDrops(world, pos,
			tagged(new ItemStack(drinkware), "Stored addon cup " + i)) == 1, "Addon vessel arrangement lost item data");
	}

	private static void persistArrangements(WorldServer world, FakePlayer player, BlockPos pos) {
		for (String id : new String[]{"table_low_oak", "shelf_wall_oak"}) {
			Block host = block(id); prepare(world, pos, host, EnumFacing.NORTH);
			click(world, player, pos, tagged(new ItemStack(Items.APPLE), "Saved " + id)); pos = pos.east(4);
			prepare(world, pos, host, EnumFacing.NORTH);
			click(world, player, pos, tagged(new ItemStack(Items.FLOWER_POT), "Saved pot " + id));
			click(world, player, pos, tagged(new ItemStack(Blocks.RED_FLOWER, 1, 4), "Saved flower " + id)); pos = pos.east(4);
			prepare(world, pos, host, EnumFacing.NORTH);
			if (host instanceof WallShelf) {
				for (int i = 0; i < 5; i++) click(world, player, pos, tagged(new ItemStack(Items.ENCHANTED_BOOK), "Saved book " + i));
				pos = pos.east(4); prepare(world, pos, host, EnumFacing.NORTH);
				for (int i = 0; i < 6; i++) click(world, player, pos, tagged(new ItemStack(Items.RECORD_CAT), "Saved record " + i));
			} else click(world, player, pos, tagged(new ItemStack(block("light_metal_ironage_candle_floor")), "Saved candle"));
			pos = pos.east(4);
		}
	}

	private static void prepare(WorldServer world, BlockPos pos, Block host, EnumFacing facing) {
		world.setBlockToAir(pos); world.setBlockToAir(pos.up());
		if (!(host instanceof WallShelf)) world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		IBlockState state = host.getDefaultState();
		if (host instanceof WallShelf) {
			world.setBlockState(pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
			state = state.withProperty(net.minecraft.block.BlockHorizontal.FACING, facing);
		}
		world.setBlockState(pos, state);
	}

	private static boolean click(WorldServer world, FakePlayer player, BlockPos pos, ItemStack held) {
		player.setHeldItem(EnumHand.MAIN_HAND, held);
		IBlockState state = world.getBlockState(pos);
		SurfaceSettingHost host = world.getTileEntity(pos) instanceof SurfaceSettingHost ? (SurfaceSettingHost)world.getTileEntity(pos) : null;
		float x = 0.5F + (float)(host == null ? 0.0D : host.getSurfaceOffsetX());
		float z = 0.5F + (float)(host == null ? 0.0D : host.getSurfaceOffsetZ());
		return state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, held, EnumFacing.UP, x, 0.85F, z);
	}

	private static int countDrops(WorldServer world, BlockPos pos, ItemStack item) {
		int count = 0;
		for (EntityItem entity : world.getEntitiesWithinAABB(EntityItem.class,
			new AxisAlignedBB(pos.add(-1, -1, -1), pos.add(2, 2, 2)))) {
			if (ItemStack.areItemsEqual(item, entity.getEntityItem()) && ItemStack.areItemStackTagsEqual(item, entity.getEntityItem())) count += entity.getEntityItem().stackSize;
		}
		return count;
	}

	private static InventoryCrafting grid() {
		return new InventoryCrafting(new Container() { @Override public boolean canInteractWith(EntityPlayer player) { return true; } }, 3, 3);
	}
	private static ItemStack tagged(ItemStack stack, String name) { return stack.setStackDisplayName(name); }
	private static Block block(String name) {
		Block block = Block.getBlockFromName("ironagefurniture:" + name);
		require(block != null && block != Blocks.AIR, "Missing Phase 5 block " + name); return block;
	}
	private static void require(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
}
