package zone.moddev.mc.ironagefurniture.fixture;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.ProduceTray;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;
import zone.moddev.mc.skysfurniturecore.api.surface.ShelfLayout;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.skysfurniturecore.api.vessel.BarrelBulkStorage;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

final class HarvestCraftDisplayProbe {
	private HarvestCraftDisplayProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos pos) {
		if (!Loader.isModLoaded("iafphcaddon")) return;
		ShelvingUnit shelf = (ShelvingUnit)Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		List<String> report = new ArrayList<>();
		report.add("item\tlayout\tshelf_capacity\ttray_capacity\tbarrel_capacity");
		int tested = 0, produce = 0;
		for (Item item : Item.REGISTRY) {
			if (!item.getRegistryName().getResourceDomain().equals("harvestcraft")) continue;
			ItemStack sample = new ItemStack(item).setStackDisplayName("Market stock");
			sample.getTagCompound().setInteger("DisplayProbe", 137);
			SurfaceContentRegistry.Descriptor descriptor = SurfaceContentRegistry.describe(sample);
			ShelfLayout layout = descriptor.getShelfLayout();
			if (item instanceof ItemFood) require(layout != ShelfLayout.NONE, "Uncovered food " + item.getRegistryName());
			if (item instanceof net.minecraft.item.ItemBlock
					&& item.getRegistryName().getResourcePath().endsWith("cake")) {
				require(layout != ShelfLayout.NONE, "Placeable HarvestCraft cake lacks a shelf layout " + item.getRegistryName());
				require(layout.getCapacity() == 2, "Whole cakes should share a two-place shelf layout");
			}
			if (layout == ShelfLayout.NONE) continue;
			tested++;
			boolean bottles = descriptor.getCategory() == zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Category.BOTTLE;
			int capacity = bottles ? ShelvingUnit.BOTTLE_CAPACITY : layout.getCapacity();
			int bulk = BarrelBulkStorage.capacityFor(sample);
			if (item.getRegistryName().getResourcePath().endsWith("seeditem")) {
				require(BarrelBulkStorage.getGranularColour(sample) != null, "Seeds still use the large-piece barrel pile " + item);
			}
			if (item.getRegistryName().getResourcePath().equals("parsnipitem")) {
				require(BarrelBulkStorage.getGranularColour(sample) == null, "Whole vegetables received a granular fill");
			}
			report.add(item.getRegistryName() + "\t" + layout + "\t" + capacity + "\t" + ProduceTray.capacity(sample) + "\t" + bulk);
			for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
				for (boolean standing : new boolean[] { false, true }) {
					IBlockState state = shelf.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
						.withProperty(RackBlock.STANDING, standing);
					world.setBlockState(standing ? pos.down() : pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
					world.setBlockState(pos, state);
					TileEntityShelvingUnit tile = new TileEntityShelvingUnit();
					world.setTileEntity(pos, tile);
					for (boolean upper : new boolean[] { false, true }) {
						ShelfLevel level = tile.getLevel(upper);
						player.inventory.clear();
						ItemStack held = sample.copy();
						held.stackSize = 3;
						player.setHeldItem(EnumHand.MAIN_HAND, held);
						shelf.onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, held, facing,
							0.5F, upper ? 0.65F : 0.20F, 0.5F);
						require(level.getMediaCount() == 1 && held.stackSize == 2, "Hand deposit must use exactly one " + item);
						ItemStack different = sample.copy().setStackDisplayName("Different batch");
						if (!bottles) require(!level.addMedia(different), "Shelf mixed different item data " + item);
						for (int i = 1; i < capacity; i++) require(level.addMedia(sample), "Early full shelf " + item);
						require(!level.addMedia(sample), "Overfilled shelf " + item);
						NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
						TileEntityShelvingUnit restored = (TileEntityShelvingUnit)TileEntity.create(world, saved);
						require(restored.getLevel(upper).getMediaCount() == capacity
							&& ItemStack.areItemStacksEqual(sample, restored.getLevel(upper).getMediaItem(capacity - 1)), "Shelf lost saved item " + item);
						player.inventory.clear();
						for (int slot = 0; slot < player.inventory.mainInventory.length; slot++)
							player.inventory.setInventorySlotContents(slot, new ItemStack(Blocks.COBBLESTONE, 64));
						require(!level.takeShelfItem(player) && level.getMediaCount() == capacity, "Full inventory lost food " + item);
						player.inventory.clear();
						require(level.takeShelfItem(player) && ItemStack.areItemStacksEqual(sample, player.inventory.getStackInSlot(0)), "Wrong retrieval " + item);
						while (level.getMediaCount() > 0) require(ItemStack.areItemStacksEqual(sample, level.removeLastMedia()), "Wrong drop " + item);
					}
				}
			}
			if (bulk == 0) {
				require(ProduceTray.capacity(sample) == 0, "Prepared food unexpectedly entered produce storage " + item);
				continue;
			}
			produce++;
			ProduceTray tray = new ProduceTray("birch");
			require(tray.offer(sample, true) && !tray.isPacked(), "Simulation changed tray");
			require(tray.offer(sample, false) && !tray.offer(sample.copy().setStackDisplayName("Other"), false), "Partial tray mixed NBT");
			for (int i = 1; i < capacity; i++) require(tray.offer(sample, false), "Early full tray " + item);
			require(!tray.offer(sample, false) && !tray.offer(sample.copy().setStackDisplayName("Other"), false), "Tray overfilled or mixed NBT");
			ProduceTray restoredTray = ProduceTray.read(tray.write());
			require(restoredTray.getContents().stackSize == capacity && ItemStack.areItemStackTagsEqual(sample, restoredTray.getContents()), "Tray lost saved stock");
			for (int i = 0; i < capacity; i++) { require(ItemStack.areItemStacksEqual(sample, tray.peekOne()), "Wrong tray pickup"); tray.removeOne(); }
			require(!tray.isPacked(), "Tray did not empty");

			BlockPos barrelPos = pos.east(3);
			world.setBlockState(barrelPos, Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak").getDefaultState());
			TileEntityBarrel barrel = new TileEntityBarrel();
			world.setTileEntity(barrelPos, barrel);
			BarrelBulkStorage storage = barrel.getBulkStorage();
			IItemHandler input = barrel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
			IItemHandler output = barrel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
			ItemStack batch = sample.copy(); batch.stackSize = 64;
			require(input.insertItem(0, batch, true) == null && storage.getCount() == 0, "Simulation changed barrel");
			require(input.insertItem(0, batch, false) == null, "Could not start barrel batch");
			require(storage.deposit(sample.copy().setStackDisplayName("Other"), false) == 0, "Partial barrel mixed NBT");
			for (int i = 1; i < bulk / 64; i++) require(input.insertItem(0, batch, false) == null, "Could not fill barrel " + item);
			require(storage.getCount() == bulk && input.insertItem(0, batch, false) == batch, "Wrong barrel capacity");
			require(storage.deposit(sample.copy().setStackDisplayName("Other"), false) == 0, "Barrel mixed data");
			TileEntityBarrel restored = (TileEntityBarrel)TileEntity.create(world, barrel.writeToNBT(new NBTTagCompound()));
			require(restored.getBulkStorage().getCount() == bulk && ItemStack.areItemStacksEqual(sample, restored.getBulkStorage().getStack()), "Barrel lost saved stock");
			int recovered = 0;
			while (storage.getCount() > 0) {
				ItemStack withdrawn = output.extractItem(0, 64, false);
				require(withdrawn != null && ItemStack.areItemsEqual(sample, withdrawn)
					&& ItemStack.areItemStackTagsEqual(sample, withdrawn), "Wrong barrel withdrawal");
				recovered += withdrawn.stackSize;
			}
			require(recovered == bulk, "Barrel lost stock");
		}
		verifyLegacyRows(world, player, pos);
		try {
			java.nio.file.Files.write(java.nio.file.Paths.get("harvestcraft-display-audit.tsv"), report, java.nio.charset.StandardCharsets.UTF_8);
		} catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
		player.inventory.clear();
		require(tested > 600 && produce > 100, "Unexpectedly small HarvestCraft coverage");
		LogManager.getLogger().info("IAF HARVESTCRAFT DISPLAYS PASSED: {} items, {} tray/barrel goods; all facings, both levels, exact NBT, capacity, retrieval, reload and hoppers", tested, produce);
	}

	private static void verifyLegacyRows(WorldServer world, FakePlayer player, BlockPos pos) {
		Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		world.setBlockState(pos, shelf.getDefaultState().withProperty(RackBlock.STANDING, true));
		TileEntityShelvingUnit tile = new TileEntityShelvingUnit();
		world.setTileEntity(pos, tile);
		ItemStack roast = new ItemStack(Item.getByNameOrId("harvestcraft:sundayroastitem")).setStackDisplayName("Old dinner");
		NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
		net.minecraft.nbt.NBTTagList oldRow = new net.minecraft.nbt.NBTTagList();
		oldRow.appendTag(roast.writeToNBT(new NBTTagCompound()));
		oldRow.appendTag(roast.writeToNBT(new NBTTagCompound()));
		saved.getCompoundTag("LowerShelf").setTag("MediaItems", oldRow);
		TileEntityShelvingUnit restored = (TileEntityShelvingUnit)TileEntity.create(world, saved);
		world.setTileEntity(pos, restored);
		ShelfLevel level = restored.getLevel(false);
		require(level.getMediaCount() == 2 && level.getVisibleArrangementCount() == 2
			&& level.getShelfLayout() == ShelfLayout.FOOD_PAIR && !level.addMedia(roast), "Refined capacity lost or hid an old dinner");
		player.inventory.clear();
		require(level.takeShelfItem(player) && ItemStack.areItemStacksEqual(roast, player.inventory.getStackInSlot(0))
			&& level.getMediaCount() == 1 && level.getShelfLayout() == ShelfLayout.CENTRED_FOOD, "Old dinner pickup did not restore the new layout");
		restored = (TileEntityShelvingUnit)TileEntity.create(world, restored.writeToNBT(new NBTTagCompound()));
		require(restored.getLevel(false).getMediaCount() == 1
			&& ItemStack.areItemStacksEqual(roast, restored.getLevel(false).getMediaItem(0)), "Refined row did not survive another reload");
		player.inventory.clear();
		int cakeForms = 0;
		for (String id : new String[] {"cheesecakeitem", "cheesecake", "cherrycheesecakeitem", "cherrycheesecake"}) {
			Item cakeItem = Item.getByNameOrId("harvestcraft:" + id);
			if (cakeItem == null) continue;
			cakeForms++;
			TileEntityShelvingUnit cakes = new TileEntityShelvingUnit();
			world.setTileEntity(pos, cakes);
			player.interactionManager.setGameType(net.minecraft.world.GameType.CREATIVE);
			ItemStack held = new ItemStack(cakeItem, 4).setStackDisplayName("Named cake");
			player.setHeldItem(EnumHand.MAIN_HAND, held);
			for (int i = 0; i < 3; i++) {
				shelf.onBlockActivated(world, pos, world.getBlockState(pos), player, EnumHand.MAIN_HAND, held,
					EnumFacing.NORTH, 0.5F, 0.2F, 0.5F);
			}
			require(cakes.getLevel(false).getMediaCount() == 2 && held.stackSize == 4,
				"Cake form did not use exactly two Creative shelf places: " + id);
			player.interactionManager.setGameType(net.minecraft.world.GameType.SURVIVAL);
			NBTTagCompound cakeSave = cakes.writeToNBT(new NBTTagCompound());
			TileEntityShelvingUnit reloaded = (TileEntityShelvingUnit)TileEntity.create(world, cakeSave);
			require(reloaded.getLevel(false).getMediaCount() == 2, "Cake form lost its row after reload: " + id);
		}
		require(cakeForms >= 2, "Expected both named cheesecake and cherry cheesecake items");
		player.inventory.clear();
	}

	private static void require(boolean valid, String message) {
		if (!valid) throw new IllegalStateException(message);
	}
}
