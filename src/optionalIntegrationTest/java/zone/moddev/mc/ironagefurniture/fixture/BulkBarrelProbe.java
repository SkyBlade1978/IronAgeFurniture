package zone.moddev.mc.ironagefurniture.fixture;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBulkBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.BarrelBulkStorage;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

final class BulkBarrelProbe {
	private BulkBarrelProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos pos) {
		Block block = Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak");
		IBlockState state = block.getDefaultState();
		world.setBlockState(pos, state);
		player.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 2);
		player.inventory.clear();
		TileEntityBarrel tile = (TileEntityBarrel)world.getTileEntity(pos);
		BarrelBulkStorage storage = tile.getBulkStorage();
		IFluidHandler fluid = tile.getFluidHandler();
		IItemHandler input = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
		IItemHandler output = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
		FluidStack water = new FluidStack(FluidRegistry.WATER, 1000);
		ItemStack fish = new ItemStack(Items.FISH, 64, 1).setStackDisplayName("Market salmon");

		require(input != null && output != null && tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
			EnumFacing.NORTH) == null, "Bulk ports must follow hopper direction");
		require(BarrelBulkStorage.capacityFor(new ItemStack(Items.WHEAT_SEEDS)) == 512
			&& BarrelBulkStorage.capacityFor(new ItemStack(Items.SUGAR)) == 256
			&& BarrelBulkStorage.capacityFor(new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage())) == 256
			&& BarrelBulkStorage.capacityFor(new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage())) == 0,
			"Starter pantry limits did not match item variants");
		require(input.insertItem(0, fish, true) == null && storage.getCount() == 0
			&& !block.getActualState(state, world, pos).getValue(StorageBarrel.OPEN), "Simulation opened the lid");

		require(fluid.fill(water, true) == 1000 && input.insertItem(0, fish, false) == fish,
			"A fluid-filled barrel accepted solids");
		click(world, player, pos, fish);
		require(fish.stackSize == 64 && storage.getCount() == 0, "A hand deposit consumed items with fluid present");
		require(fluid.drain(1000, true).amount == 1000, "Could not drain the starter fluid");
		click(world, player, pos, fish);
		require(player.getHeldItemMainhand() == null && storage.getCount() == 64 && storage.getCapacity() == 128
			&& block.getActualState(state, world, pos).getValue(StorageBarrel.OPEN)
			&& block.getComparatorInputOverride(state, world, pos) == 8, "First hand deposit did not open and update the barrel");
		require(fluid.fill(water, false) == 0 && fluid.fill(water, true) == 0 && tile.fill(EnumFacing.NORTH, water, true) == 0,
			"A cached or legacy fluid handler accepted fluid while open");
		require(storage.deposit(new ItemStack(Items.FISH, 1, 0), false) == 0
			&& storage.deposit(new ItemStack(Items.FISH, 1, 1), false) == 0, "The barrel mixed fish variants or item tags");

		verifyHoppers(world, pos, tile, fish);
		ItemStack extra = new ItemStack(Items.FISH, 64, 1).setStackDisplayName("Market salmon");
		ItemStack part = extra.copy();
		part.stackSize = 60;
		require(input.insertItem(0, part, false) == null && storage.getCount() == 124, "Hopper port did not add a bulk deposit");
		part = extra.copy();
		click(world, player, pos, part);
		require(part.stackSize == 60 && storage.getCount() == 128, "Top deposit did not leave the exact overflow");
		require(block.getComparatorInputOverride(state, world, pos) == 15 && input.insertItem(0, extra, false) == extra,
			"A full barrel accepted extra items");
		require(output.extractItem(0, 200, true).stackSize == 64 && storage.getCount() == 128,
			"A simulated withdrawal changed storage or returned an oversized stack");

		NBTTagCompound saved = ((TileEntity)tile).writeToNBT(new NBTTagCompound());
		TileEntity restored = TileEntity.create(world, saved);
		require(restored != null && saved.equals(restored.writeToNBT(new NBTTagCompound())),
			"Barrel reload changed the batch contents or amount");
		world.setTileEntity(pos, restored);
		require(output.extractItem(0, 1, false) == null, "A stale port extracted from the replaced tile");
		TileEntityBarrel restoredBarrel = (TileEntityBarrel)restored;
		storage = restoredBarrel.getBulkStorage();
		Container menu = (Container)new StorageGuiHandler().getServerGuiElement(
			StorageGuiHandler.BULK_BARREL, player, world, pos.getX(), pos.getY(), pos.getZ());
		require(menu instanceof ContainerBulkBarrel, "The filled barrel did not supply a bulk screen");
		player.openContainer = menu;
		for (int slot = 0; slot < player.inventory.mainInventory.length; slot++) {
			player.inventory.setInventorySlotContents(slot, new ItemStack(Blocks.STONE, 64));
		}
		require(!menu.enchantItem(player, ContainerBulkBarrel.TAKE_STACK) && storage.getCount() == 128,
			"A full player inventory lost stored fish");
		ItemStack existing = new ItemStack(Items.FISH, 62, 1).setStackDisplayName("Market salmon");
		player.inventory.setInventorySlotContents(12, existing);
		require(menu.enchantItem(player, ContainerBulkBarrel.TAKE_STACK) && storage.getCount() == 126
			&& player.inventory.getStackInSlot(12).stackSize == 64, "Partial withdrawal did not move exactly what fit");
		player.inventory.clear();
		require(menu.enchantItem(player, ContainerBulkBarrel.TAKE_ONE) && storage.getCount() == 125
			&& ItemStack.areItemStacksEqual(storage.getStack(), player.inventory.getStackInSlot(0)),
			"Take One lost exact fish data");

		require(storage.withdraw(64, false).stackSize == 64 && storage.withdraw(64, false).stackSize == 61
			&& !block.getActualState(state, world, pos).getValue(StorageBarrel.OPEN), "The last withdrawal did not close the lid");
		require(restoredBarrel.getFluidHandler().fill(water, true) == 1000
			&& restoredBarrel.getFluidHandler().drain(1000, true).amount == 1000,
			"An emptied solid barrel was not available for fluids");
		require(storage.deposit(extra, false) == 64 && !menu.enchantItem(player, ContainerBulkBarrel.TAKE_ONE),
			"A window from the previous batch could withdraw from a new batch of the same item");
		player.closeScreen();
		require(storage.deposit(extra, false) == 64, "Could not prepare a full break test");
		player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
		require(player.interactionManager.tryHarvestBlock(pos), "Native bulk barrel mining failed");
		int fishDrops = 0;
		int barrelDrops = 0;
		for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
			ItemStack stack = drop.getEntityItem();
			if (ItemStack.areItemsEqual(stack, extra) && ItemStack.areItemStackTagsEqual(stack, extra)) {
				require(stack.stackSize <= stack.getMaxStackSize(), "A barrel dropped an oversized stack");
				fishDrops += stack.stackSize;
			} else if (stack.getItem() == net.minecraft.item.Item.getItemFromBlock(block)) {
				require(!stack.hasTagCompound(), "Solid contents travelled in the barrel item");
				barrelDrops += stack.stackSize;
			}
			drop.setDead();
		}
		require(fishDrops == 128 && barrelDrops == 1, "Barrel mining lost or duplicated its contents");

		Block side = Block.getBlockFromName("ironagefurniture:side_barrel_wood_ironage_oak");
		world.setBlockState(pos, side.getDefaultState());
		TileEntityBarrel sideTile = (TileEntityBarrel)world.getTileEntity(pos);
		require(!sideTile.isBulkBarrel() && sideTile.getBulkStorage().deposit(extra, false) == 0
			&& sideTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP) == null
			&& sideTile.getFluidHandler().fill(water, true) == 1000, "A side barrel exposed the upright bulk mode");
		world.setBlockToAir(pos);
		player.inventory.clear();
		LogManager.getLogger().info("IAF BULK BARREL PROBE PASSED: hand, live hoppers, fluid exclusion, NBT, withdrawals and drops");
	}

	private static void verifyHoppers(WorldServer world, BlockPos pos, TileEntityBarrel tile, ItemStack fish) {
		// Exercise Forge's vanilla hopper hooks, in addition to direct capability calls.
		world.setBlockState(pos.up(), Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN));
		TileEntityHopper above = (TileEntityHopper)world.getTileEntity(pos.up());
		ItemStack one = fish.copy();
		one.stackSize = 1;
		above.setInventorySlotContents(0, one.copy());
		for (int tick = 0; tick < 9 && tile.getBulkStorage().getCount() != 65; tick++) {
			above.update();
		}
		require(tile.getBulkStorage().getCount() == 65 && above.getStackInSlot(0) == null,
			"Vanilla hopper did not insert through the top port");
		world.setBlockToAir(pos.up());
		world.setBlockState(pos.down(), Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN));
		TileEntityHopper below = (TileEntityHopper)world.getTileEntity(pos.down());
		for (int tick = 0; tick < 9 && tile.getBulkStorage().getCount() != 64; tick++) {
			below.update();
		}
		require(tile.getBulkStorage().getCount() == 64 && ItemStack.areItemStacksEqual(below.getStackInSlot(0), one),
			"Vanilla hopper did not extract the exact stack through the bottom port");
		below.clear();
		world.setBlockToAir(pos.down());
	}

	private static void click(WorldServer world, FakePlayer player, BlockPos pos, ItemStack stack) {
		player.setHeldItem(EnumHand.MAIN_HAND, stack);
		IBlockState state = world.getBlockState(pos);
		require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, stack,
			EnumFacing.UP, 0.5F, 1.0F, 0.5F), "Top click did not handle eligible contents");
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
