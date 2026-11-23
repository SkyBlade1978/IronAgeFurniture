package zone.moddev.mc.ironagefurniture.fixture;

import java.util.List;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBottleRack;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

final class StorageHopperProbe {
	private StorageHopperProbe() { }

	static void verify(WorldServer world, BlockPos origin) {
		String[] storage = { "cabinet_wood_ironage_oak", "half_cabinet_wood_ironage_oak",
			"bedside_cabinet_wood_ironage_oak", "crate_wood_ironage_oak" };
		for (int index = 0; index < storage.length; index++) {
			verifyInventory(world, origin.east(6 * index), storage[index]);
		}

		verifyShelving(world, origin.south(6));
		verifyBottleRack(world, origin.south(12));
		verifyWallShelf(world, origin.south(18));
		LogManager.getLogger().info("IAF STORAGE HOPPER PROBE PASSED: cabinets, crates, shelf rows, wall shelves and bottle racks");
	}

	static void verifyReload(WorldServer world) {
		BlockPos origin = new BlockPos(64, 120, 360);
		TileEntity tile = world.getTileEntity(origin.south(6));
		if (!(tile instanceof TileEntityShelvingUnit)) {
			return;
		}

		TileEntityShelvingUnit shelf = (TileEntityShelvingUnit)tile;
		TileEntityBottleRack rack = (TileEntityBottleRack)world.getTileEntity(origin.south(12));
		TileEntityWallShelf wall = (TileEntityWallShelf)world.getTileEntity(origin.south(18));
		require(shelf.getLevel(true).getMediaCount() == 5 && shelf.getLevel(false).getMediaCount() == 5
			&& "Bakery delivery".equals(shelf.getLevel(false).getMediaItem(4).getDisplayName())
			&& rack != null && rack.hasBottle(3)
			&& wall != null && wall.hasDisplayedItem() && "Counter display".equals(wall.getDisplayedItem().getDisplayName()),
			"fed displays survive a dedicated server restart");
		input(tile, EnumFacing.UP);
		LogManager.getLogger().info("IAF STORAGE HOPPER WORLD RELOAD PASSED: shelf rows, bottle rack and wall display");
	}

	private static void verifyInventory(WorldServer world, BlockPos pos, String id) {
		world.setBlockState(pos, block(id).getDefaultState());
		TileEntity tile = world.getTileEntity(pos);
		ItemStack sample = new ItemStack(Items.BOOK, 3).setStackDisplayName("Hopper books");
		TileEntityHopper hopper = feed(world, pos, EnumFacing.UP, sample);
		IItemHandler inventory = input(tile, EnumFacing.UP);
		require(hopper.getStackInSlot(0) == null && ItemStack.areItemStacksEqual(sample, inventory.getStackInSlot(0)), id);

		if (tile instanceof TileEntityCrate) {
			TileEntityCrate crate = (TileEntityCrate)tile;
			crate.setSealed(true);
			hopper.setInventorySlotContents(0, sample.copy());
			tick(hopper, 35);
			require(ItemStack.areItemStacksEqual(sample, hopper.getStackInSlot(0))
				&& ItemStack.areItemStacksEqual(sample, crate.getInventory().getStackInSlot(0)), "sealed crate rejects hopper");
		}
	}

	private static void verifyShelving(WorldServer world, BlockPos pos) {
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		IBlockState state = block("shelving_unit_wood_ironage_oak").getDefaultState().withProperty(RackBlock.STANDING, true);
		world.setBlockState(pos, state);
		// Old empty racks had no tile. The first hopper still has to see the intake.
		world.removeTileEntity(pos);
		TileEntityShelvingUnit shelf = (TileEntityShelvingUnit)world.getTileEntity(pos);
		ItemStack bread = new ItemStack(Items.BREAD, 11).setStackDisplayName("Bakery delivery");
		IItemHandler port = input(shelf, EnumFacing.UP);
		require(port.insertItem(0, bread, true).stackSize == 10 && shelf.isEmpty()
			&& bread.stackSize == 11, "shelf simulation leaves no display");
		TileEntityHopper hopper = feed(world, pos, EnumFacing.UP, bread);
		require(shelf.getLevel(true).getMediaCount() == 5 && shelf.getLevel(false).getMediaCount() == 5
			&& hopper.getStackInSlot(0).stackSize == 1 && "Bakery delivery".equals(shelf.getLevel(false).getMediaItem(4).getDisplayName()),
			"shelf fills both levels and leaves excess in the hopper");
		ItemStack sign = new ItemStack(Items.SIGN);
		require(port.insertItem(0, sign, false) == sign && port.extractItem(0, 1, false) == null
			&& shelf.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN) == null,
			"display intake does not accept signs or extract arranged items");
		roundTrip(world, shelf);
	}

	private static void verifyBottleRack(WorldServer world, BlockPos pos) {
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		world.setBlockState(pos, block("bottle_rack_wood_ironage_oak").getDefaultState().withProperty(RackBlock.STANDING, true));
		TileEntityBottleRack rack = (TileEntityBottleRack)world.getTileEntity(pos);
		TileEntityHopper hopper = feed(world, pos, EnumFacing.EAST, new ItemStack(Items.GLASS_BOTTLE, 10));
		for (int index = 0; index < TileEntityBottleRack.SLOT_COUNT; index++) {
			require(rack.hasBottle(index), "rack feeds every position");
		}
		require(hopper.getStackInSlot(0).stackSize == 1, "full rack rejects the tenth bottle");
		world.setBlockToAir(pos.east());
		rack.removeBottle(3);
		IItemHandler port = input(rack, EnumFacing.EAST);
		ItemStack invalid = new ItemStack(Items.BREAD);
		require(port.insertItem(0, invalid, false) == invalid && rack.isSlotEmpty(3), "rack rejects non-bottles");
		List<ItemStack> samples = Phase5BottleProbe.samples();
		ItemStack bottle = samples.get(samples.size() - 1);
		require(port.insertItem(0, bottle, false) == null && ItemStack.areItemStacksEqual(bottle, rack.getBottle(3)),
			"rack fills a gap with exact vanilla or add-on bottle data");
		roundTrip(world, rack);
	}

	private static void verifyWallShelf(WorldServer world, BlockPos pos) {
		world.setBlockState(pos.south(), Blocks.STONE.getDefaultState());
		world.setBlockState(pos, block("shelf_wall_oak").getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH));
		TileEntityWallShelf shelf = (TileEntityWallShelf)world.getTileEntity(pos);
		ItemStack books = new ItemStack(Items.BOOK, 6).setStackDisplayName("Library delivery");
		TileEntityHopper hopper = feed(world, pos, EnumFacing.UP, books);
		require(shelf.getEmbeddedKind() == WallShelf.ShelfContentKind.BOOKS && shelf.getEmbeddedCount() == 5
			&& hopper.getStackInSlot(0).stackSize == 1 && "Library delivery".equals(shelf.getFirstEmbeddedItem().getDisplayName()),
			"wall shelf fills built-in book arrangement from above");
		shelf.removeAllEmbeddedItems();

		IItemHandler port = input(shelf, EnumFacing.EAST);
		ItemStack apple = new ItemStack(Items.APPLE).setStackDisplayName("Counter display");
		require(port.insertItem(0, apple, true) == apple && !shelf.hasDisplayedItem(), "wall shelf rejects blocked loose display");
		// Clear the leftovers so the adjacent hopper cannot pick up a dropped book.
		hopper.clear();
		world.setBlockToAir(pos.up());
		require(port.insertItem(0, apple, true) == null && world.isAirBlock(pos.up()), "wall shelf simulation does not reserve space");
		hopper = feed(world, pos, EnumFacing.EAST, apple);
		require(hopper.getStackInSlot(0) == null && ItemStack.areItemStacksEqual(apple, shelf.getDisplayedItem())
			&& world.getBlockState(pos.up()).getBlock() == block("surface_display_blocker"), "side feed reserves loose display space");
		roundTrip(world, shelf);
	}

	private static TileEntityHopper feed(WorldServer world, BlockPos pos, EnumFacing side, ItemStack input) {
		BlockPos hopperPos = pos.offset(side);
		world.setBlockState(hopperPos, Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, side.getOpposite()));
		TileEntityHopper hopper = (TileEntityHopper)world.getTileEntity(hopperPos);
		hopper.setInventorySlotContents(0, input.copy());
		tick(hopper, 110);
		return hopper;
	}

	private static void tick(TileEntityHopper hopper, int ticks) {
		for (int tick = 0; tick < ticks; tick++) {
			hopper.update();
		}
	}

	private static IItemHandler input(TileEntity tile, EnumFacing side) {
		IItemHandler port = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		require(port != null, "storage exposes a Forge item input");
		return port;
	}

	private static void roundTrip(WorldServer world, TileEntity tile) {
		NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
		TileEntity loaded = TileEntity.create(world, saved);
		require(loaded != null && saved.equals(loaded.writeToNBT(new NBTTagCompound())), "exact fed items survive NBT reload");
	}

	private static Block block(String id) {
		return Block.getBlockFromName("ironagefurniture:" + id);
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new IllegalStateException("Hopper check failed: " + message);
	}
}
