package zone.moddev.mc.ironagefurniture.fixture;

import java.util.List;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.tile.ProduceTray;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityProduceFurniture;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityHopper;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

final class ProduceFurnitureProbe {
	private static final BlockPos SAVE = new BlockPos(128, 120, 416);

	private ProduceFurnitureProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		require(ProduceTray.capacity(new ItemStack(Items.APPLE)) == 9 && ProduceTray.capacity(new ItemStack(Items.CARROT)) == 15,
			"Produce capacity diverged from the shelf layouts");
		require(ProduceTray.capacity(new ItemStack(Items.REDSTONE)) == 0 && ProduceTray.capacity(new ItemStack(Items.DIAMOND_AXE)) == 0,
			"Non-food items can be stocked in produce trays");
		require(ProduceTray.capacity(new ItemStack(OptionalIntegrationRecipeProbe.SHELF_LAYOUT_SAMPLE)) == 6,
			"Add-on opt-in did not enable the produce tray");
		verifyMissingItem();
		for (EnumFacing front : EnumFacing.Plane.HORIZONTAL) {
			BlockPos pos = origin.east(8 * front.getHorizontalIndex());
			verifyStack(world, player, pos, front);
			verifyRack(world, player, pos.south(8), front);
		}
		verifyCreative(world, player, origin.south(16));

		place(world, player, SAVE, EnumFacing.EAST, ItemBlockProduceFurniture.pack(tray(), sample("birch", apples(), 9)));
		stack(world, player, SAVE, ItemBlockProduceFurniture.pack(tray(), sample("spruce", new ItemStack(Items.CARROT), 15)));
		place(world, player, SAVE.east(3), EnumFacing.SOUTH, ItemBlockProduceFurniture.empty(rack(), "birch", 1));
		tile(world, SAVE.east(3)).setTray(2, sample("oak", new ItemStack(Items.GOLDEN_APPLE, 1, 1), 4));
		player.inventory.clear();
		LogManager.getLogger().info("IAF PRODUCE FURNITURE PASSED: four facings, exact stacks, three rack tiers, hopper feed, drops and Creative transfer");
	}

	private static void verifyStack(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing front) {
		player.inventory.clear();
		place(world, player, pos, front, ItemBlockProduceFurniture.empty(tray(), "oak", 1));
		require(tile(world, pos).getTray(0).getWood().equals("oak") && !world.getBlockState(pos).getValue(ProduceFurniture.SECOND),
			"Single floor tray did not initialize");
		ItemStack held = apples();
		held.stackSize = 10;
		for (int i = 0; i < 10; i++) {
			click(world, player, pos, held, front, 0.2F);
		}
		require(held.stackSize == 1 && tile(world, pos).getTray(0).getContents().stackSize == 9, "Tray overfilled or consumed the excess");
		ItemStack mismatch = new ItemStack(Items.APPLE);
		click(world, player, pos, mismatch, front, 0.2F);
		require(mismatch.stackSize == 1, "Tray mixed named and plain apples");
		stack(world, player, pos, ItemBlockProduceFurniture.pack(tray(), sample("birch", new ItemStack(Items.CARROT), 4)));
		require(world.getBlockState(pos).getValue(ProduceFurniture.SECOND)
			&& tile(world, pos).getTray(0).getContents().stackSize == 9 && tile(world, pos).getTray(1).getContents().stackSize == 4,
			"Stacking lost or mixed tray contents");

		world.setBlockState(pos.up(), Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN));
		TileEntityHopper hopper = (TileEntityHopper)world.getTileEntity(pos.up());
		hopper.setInventorySlotContents(0, new ItemStack(Items.CARROT));
		hopper.update();
		require(hopper.getStackInSlot(0) == null && tile(world, pos).getTray(1).getContents().stackSize == 5, "Hopper missed the upper tray");
		world.setBlockToAir(pos.up());

		placeOn(world, player, pos, front, ItemBlockProduceFurniture.empty(tray(), "spruce", 1));
		require(tile(world, pos.up()).getTray(0).getWood().equals("spruce"), "A full pair did not support a taller stack");
		world.setBlockToAir(pos.up());

		for (int i = 0; i < player.inventory.mainInventory.length; i++) {
			player.inventory.mainInventory[i] = new ItemStack(Blocks.COBBLESTONE, 64);
		}
		player.setSneaking(true);
		IBlockState state = world.getBlockState(pos);
		state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.OFF_HAND, null, front, 0.5F, 0.75F, 0.5F);
		require(tile(world, pos).getTray(1) != null, "Full inventory lost a packed tray");
		player.inventory.clear();
		click(world, player, pos, null, front, 0.75F);
		player.setSneaking(false);
		ItemStack lifted = player.inventory.getStackInSlot(0);
		require(ItemBlockProduceFurniture.isTray(lifted) && ItemBlockProduceFurniture.unpack(lifted).getWood().equals("birch")
			&& ItemBlockProduceFurniture.unpack(lifted).getContents().stackSize == 5 && tile(world, pos).getTray(1) == null,
			"Unstacking changed the upper tray or its contents");
		world.setBlockToAir(pos);
	}

	private static void verifyRack(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing front) {
		place(world, player, pos, front, ItemBlockProduceFurniture.empty(rack(), "oak", 1));
		BlockPos beside = pos.offset(front.rotateY());
		place(world, player, beside, front, ItemBlockProduceFurniture.empty(rack(), "oak", 1));
		require(rack().connectsTo(world, beside, world.getBlockState(pos), "oak"), "Matching columns did not connect");
		ItemStack[] stock = { apples(), new ItemStack(Items.CARROT), new ItemStack(OptionalIntegrationRecipeProbe.SHELF_LAYOUT_SAMPLE) };
		for (int i = 0; i < stock.length; i++) {
			double height = rack().tierHeight(i) + 0.02D;
			RayTraceResult slot = aim(world, pos, front, height);
			require(slot != null && slot.subHit == i, "Empty socket could not be targeted on " + front + " at " + i);
			clickHit(world, player, slot, ItemBlockProduceFurniture.empty(tray(), i == 1 ? "spruce" : "oak", 1));
			RayTraceResult occupied = aim(world, pos, front, rack().tierHeight(i) + 0.17D);
			require(occupied != null && occupied.subHit == i, "Installed tray could not be targeted on " + front + " at " + i);
			ItemStack item = stock[i].copy();
			clickHit(world, player, occupied, item);
			require(item.stackSize == 0 && ItemStack.areItemStacksEqual(tile(world, pos).getTray(i).getContents(), stock[i]),
				"Stocking used the wrong rack tier");
		}

		IItemHandler feed = tile(world, pos.up()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
		require(feed != null && feed.insertItem(0, stock[2].copy(), true) == null
			&& tile(world, pos).getTray(2).getContents().stackSize == 1, "Simulated rack transfer changed contents");
		require(feed.insertItem(0, stock[2].copy(), false) == null && tile(world, pos).getTray(2).getContents().stackSize == 2,
			"Rack could not feed an upper tray through its upper block");
		require(tile(world, beside).getTray(2) == null, "Joined rack columns shared contents");

		player.inventory.clear();
		player.setSneaking(true);
		clickHit(world, player, aim(world, pos, front, rack().tierHeight(1) + 0.17D), null);
		player.setSneaking(false);
		ItemStack lifted = player.inventory.getStackInSlot(0);
		require(ItemBlockProduceFurniture.isTray(lifted) && ItemBlockProduceFurniture.unpack(lifted).getWood().equals("spruce")
			&& ItemStack.areItemStacksEqual(stock[1], ItemBlockProduceFurniture.unpack(lifted).getContents())
			&& tile(world, pos).getTray(1) == null, "Rack lift lost the tray or its stock");
		clickHit(world, player, aim(world, pos, front, rack().tierHeight(1) + 0.02D), lifted);
		require(lifted.stackSize == 0 && ItemStack.areItemStacksEqual(stock[1], tile(world, pos).getTray(1).getContents()),
			"Reinstalling a filled tray changed its stock");

		world.destroyBlock(pos.up(), true);
		require(world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.getBlockState(beside).getBlock() == rack(),
			"Breaking a rack part did not remove exactly one column");
		List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos));
		require(drops.stream().filter(drop -> ItemBlockProduceFurniture.isTray(drop.getEntityItem())).count() == 3
			&& drops.stream().filter(drop -> drop.getEntityItem().getItem() == Item.getItemFromBlock(rack())).count() == 1,
			"Breaking a rack lost or duplicated the frame or a tray");
		for (int i = 0; i < stock.length; i++) {
			final int count = i == 2 ? 2 : 1;
			ItemStack expected = stock[i].copy();
			expected.stackSize = count;
			require(drops.stream().anyMatch(drop -> ItemBlockProduceFurniture.isTray(drop.getEntityItem())
				&& ItemStack.areItemStacksEqual(expected, ItemBlockProduceFurniture.unpack(drop.getEntityItem()).getContents())),
				"Rack drops changed the stored stack");
		}
		world.setBlockToAir(beside);
	}

	private static void verifyCreative(WorldServer world, FakePlayer player, BlockPos pos) {
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		player.interactionManager.setGameType(GameType.CREATIVE);
		ItemStack packed = ItemBlockProduceFurniture.pack(tray(), sample("oak", apples(), 2));
		player.setHeldItem(EnumHand.MAIN_HAND, packed);
		EnumActionResult result = player.interactionManager.processRightClickBlock(player, world, packed, EnumHand.MAIN_HAND,
			pos.down(), EnumFacing.UP, 0.5F, 1, 0.5F);
		require(result == EnumActionResult.SUCCESS && !ItemBlockProduceFurniture.unpack(packed).isPacked()
			&& tile(world, pos).getTray(0).getContents().stackSize == 2, "Creative placement duplicated or lost packed produce");
		player.interactionManager.setGameType(GameType.SURVIVAL);
		world.setBlockToAir(pos);
	}

	static void verifyReload(WorldServer world) {
		world.getChunkFromBlockCoords(SAVE);
		if (world.getBlockState(SAVE).getBlock() != tray()) {
			return;
		}
		TileEntityProduceFurniture stack = tile(world, SAVE);
		TileEntityProduceFurniture display = tile(world, SAVE.east(3));
		require(stack.getTray(0).write().equals(sample("birch", apples(), 9).write())
			&& stack.getTray(1).write().equals(sample("spruce", new ItemStack(Items.CARROT), 15).write())
			&& display.getWood().equals("birch")
			&& display.getTray(2).write().equals(sample("oak", new ItemStack(Items.GOLDEN_APPLE, 1, 1), 4).write()),
			"Reload changed a tray, wood, or exact contents");
		LogManager.getLogger().info("IAF PRODUCE FURNITURE RELOAD PASSED");
	}

	private static void verifyMissingItem() {
		NBTTagCompound data = new ProduceTray("forestry_balsa").write();
		NBTTagCompound missing = new NBTTagCompound();
		missing.setString("id", "iaf_missing_fixture:produce");
		missing.setByte("Count", (byte)5);
		data.setTag(ProduceTray.CONTENTS_TAG, missing);
		ProduceTray tray = ProduceTray.read(data);
		require(tray.isPacked() && tray.getContents() == null && data.equals(tray.write()) && !tray.offer(apples(), false),
			"Missing add-on data was discarded or overwritten");
	}

	private static ItemStack apples() {
		return new ItemStack(Items.APPLE).setStackDisplayName("Orchard apples");
	}

	private static ProduceTray sample(String wood, ItemStack item, int count) {
		ProduceTray tray = new ProduceTray(wood);
		for (int i = 0; i < count; i++) {
			require(tray.offer(item, false), "Could not create a stocked tray");
		}
		return tray;
	}

	private static ProduceFurniture tray() {
		return (ProduceFurniture)Ironagefurniture.BlockRegistry.get(ProduceFurniture.TRAY_ID);
	}

	private static ProduceFurniture rack() {
		return (ProduceFurniture)Ironagefurniture.BlockRegistry.get(ProduceFurniture.RACK_ID);
	}

	private static TileEntityProduceFurniture tile(WorldServer world, BlockPos pos) {
		return (TileEntityProduceFurniture)world.getTileEntity(pos);
	}

	private static void place(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing front, ItemStack stack) {
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		placeOn(world, player, pos.down(), front, stack);
		require(world.getBlockState(pos).getValue(BlockHorizontal.FACING) == front, "Produce furniture faced the wrong way");
	}

	private static void placeOn(WorldServer world, FakePlayer player, BlockPos support, EnumFacing front, ItemStack stack) {
		player.rotationYaw = front.getOpposite().getHorizontalAngle();
		player.setHeldItem(EnumHand.MAIN_HAND, stack);
		require(stack.onItemUse(player, world, support, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 1, 0.5F)
			== EnumActionResult.SUCCESS, "Produce furniture could not be placed at " + support.up());
	}

	private static void stack(WorldServer world, FakePlayer player, BlockPos pos, ItemStack stack) {
		click(world, player, pos, stack, EnumFacing.UP, 0.5F);
		require(stack.stackSize == 0, "Stacking did not transfer the held tray");
	}

	private static RayTraceResult aim(WorldServer world, BlockPos pos, EnumFacing front, double height) {
		Vec3d target = new Vec3d(pos).addVector(0.5D, height, 0.5D);
		return world.rayTraceBlocks(target.add(new Vec3d(front.getDirectionVec()).scale(1.5D)), target);
	}

	private static void clickHit(WorldServer world, FakePlayer player, RayTraceResult hit, ItemStack stack) {
		BlockPos pos = hit.getBlockPos();
		click(world, player, pos, stack, hit.sideHit, (float)(hit.hitVec.yCoord - pos.getY()));
	}

	private static void click(WorldServer world, FakePlayer player, BlockPos pos, ItemStack stack, EnumFacing side, float y) {
		IBlockState state = world.getBlockState(pos);
		player.setHeldItem(EnumHand.MAIN_HAND, stack);
		boolean handled = state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, stack, side, 0.5F, y, 0.5F);
		require(handled, "Produce furniture did not handle click at " + pos + " y=" + y);
	}

	private static void require(boolean ok, String message) {
		if (!ok) {
			throw new IllegalStateException(message);
		}
	}
}
