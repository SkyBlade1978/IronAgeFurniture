package zone.moddev.mc.ironagefurniture.fixture;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.BedsideCabinet;
import zone.moddev.mc.ironagefurniture.api.surface.FurnitureSurfaceDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBedsideCabinet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

final class BedsideCabinetProbe {
	private static final BlockPos SAVED_POS = new BlockPos(80, 120, 305);

	private BedsideCabinetProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block cabinet = Ironagefurniture.BlockRegistry.get("bedside_cabinet_wood_ironage_oak");
		FakePlayer other = FakePlayerFactory.get(world, new GameProfile(UUID.randomUUID(), "DrawerVisitor"));
		List<ItemStack> bottles = Phase5BottleProbe.samples();
		ItemStack[] displays = {
			new ItemStack(Ironagefurniture.ItemRegistry.get("ornament_glass_vase"), 1, 3),
			new ItemStack(BlockObjectHolder.light_metal_ironage_block_floor_glow_clear),
			new ItemStack(BlockObjectHolder.light_metal_ironage_candle_floor),
			bottles.get(bottles.size() - 1)
		};

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			int index = facing.getHorizontalIndex();
			verifyFacing(world, player, other, origin.east(6 * index), cabinet, facing, displays[index]);
		}

		world.setBlockState(SAVED_POS, cabinet.getDefaultState().withProperty(BedsideCabinet.FACING, EnumFacing.EAST));
		TileEntityBedsideCabinet saved = (TileEntityBedsideCabinet)world.getTileEntity(SAVED_POS);
		saved.getDrawer(true).setInventorySlotContents(8, savedBook());
		saved.getDrawer(false).setInventorySlotContents(0, savedApple());
		saved.getSurfaceSetting().setSingleItem(savedDisplay(), EnumFacing.EAST);
		saved.markSurfaceSettingChanged();

		player.inventory.clear();
		LogManager.getLogger().info("IAF BEDSIDE CABINET PROBE PASSED: all facings, independent drawers, hoppers, surface and exact NBT/drops");
	}

	static void verifyReload(WorldServer world) {
		TileEntity loaded = world.getTileEntity(SAVED_POS);
		if (!(loaded instanceof TileEntityBedsideCabinet)) {
			return;
		}

		TileEntityBedsideCabinet cabinet = (TileEntityBedsideCabinet)loaded;
		require(world.getBlockState(SAVED_POS).getValue(BedsideCabinet.FACING) == EnumFacing.EAST
			&& ItemStack.areItemStacksEqual(cabinet.getDrawer(true).getStackInSlot(8), savedBook())
			&& ItemStack.areItemStacksEqual(cabinet.getDrawer(false).getStackInSlot(0), savedApple())
			&& stored(cabinet, savedDisplay()) && !cabinet.isDrawerOpen(true) && !cabinet.isDrawerOpen(false),
			"Saved bedside cabinet lost its facing, independent inventories or display");
		LogManager.getLogger().info("IAF BEDSIDE CABINET WORLD RELOAD PASSED: distinct drawer inventories, tabletop and facing");
	}

	private static ItemStack savedBook() {
		return new ItemStack(Items.BOOK, 3).setStackDisplayName("Saved bedside reading");
	}

	private static ItemStack savedApple() {
		return new ItemStack(Items.APPLE, 11).setStackDisplayName("Saved bedside provisions");
	}

	private static ItemStack savedDisplay() {
		return new ItemStack(Items.POTIONITEM).setStackDisplayName("Saved bedside drink");
	}

	private static void verifyFacing(WorldServer world, FakePlayer player, FakePlayer other, BlockPos pos,
			Block block, EnumFacing face, ItemStack decoration) {
		IBlockState state = block.getDefaultState().withProperty(BedsideCabinet.FACING, face);
		world.setBlockState(pos, state);
		player.inventory.clear();
		TileEntity tile = world.getTileEntity(pos);
		TileEntityBedsideCabinet cabinet = (TileEntityBedsideCabinet)tile;
		IInventory upper = cabinet.getDrawer(true);
		IInventory lower = cabinet.getDrawer(false);
		verifyHoppers(world, pos, tile, face);

		ItemStack first = new ItemStack(Items.BOOK, 3).setStackDisplayName("Bedside reading");
		ItemStack second = new ItemStack(Items.APPLE, 7).setStackDisplayName("Bedside provisions");
		upper.setInventorySlotContents(8, first.copy());
		lower.setInventorySlotContents(8, second.copy());

		openDrawer(world, player, pos, face, true, upper);
		require(cabinet.isDrawerOpen(true) && !cabinet.isDrawerOpen(false), "Upper drawer opened the wrong model");
		Container upperView = new ContainerChest(other.inventory, upper, other);
		Container lowerView = new ContainerChest(other.inventory, lower, other);
		require(cabinet.isDrawerOpen(true) && cabinet.isDrawerOpen(false), "Two drawers could not open independently");
		Container active = player.openContainer;
		require(ItemStack.areItemStacksEqual(active.transferStackInSlot(player, 8), first)
			&& upper.getStackInSlot(8) == null && ItemStack.areItemStacksEqual(lower.getStackInSlot(8), second),
			"Shift-click touched the wrong drawer or lost NBT");
		player.closeScreen();
		require(cabinet.isDrawerOpen(true), "Closing one viewer closed another player's drawer");
		upperView.onContainerClosed(other);
		require(!cabinet.isDrawerOpen(true) && cabinet.isDrawerOpen(false), "Closing upper drawer closed the lower drawer");
		lowerView.onContainerClosed(other);
		openDrawer(world, player, pos, face, false, lower);
		require(!cabinet.isDrawerOpen(true) && cabinet.isDrawerOpen(false), "Lower drawer opened the wrong model");
		player.closeScreen();
		upper.setInventorySlotContents(8, first.copy());

		Vec3d top = new Vec3d(pos).addVector(0.5D, BedsideCabinet.TOP_HEIGHT, 0.5D);
		aim(player, top.addVector(0, 1.5D, 0), top);
		click(world, player, pos, EnumFacing.UP, top, decoration.copy());
		require(player.getHeldItemMainhand() == null && stored(cabinet, decoration), "Incorrect tabletop placement");
		require(block.getLightValue(state, world, pos) == FurnitureSurfaceDisplay.getLight(decoration), "Incorrect tabletop light level");

		NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
		TileEntity loaded = TileEntity.create(world, saved);
		require(loaded instanceof TileEntityBedsideCabinet && saved.equals(loaded.writeToNBT(new NBTTagCompound())),
			"Tabletop or exact drawer stacks changed on reload");
		world.setTileEntity(pos, loaded);
		cabinet = (TileEntityBedsideCabinet)loaded;
		NBTTagCompound update = loaded.getUpdateTag();
		require(!update.hasKey("UpperItems") && !update.hasKey("LowerItems"), "Drawer contents leaked in the visual update packet");

		click(world, player, pos, EnumFacing.UP, top, null);
		require(!cabinet.getSurfaceSetting().hasAnyItem() && world.isAirBlock(pos.up()), "Tabletop could not be retrieved");
		require(block.getLightValue(state, world, pos) == 0, "Removed lamp still emits light");
		click(world, player, pos, EnumFacing.UP, top, decoration.copy());

		Vec3d front = new Vec3d(pos).addVector(0.5D + 0.25D * face.getFrontOffsetX(), 0.29D,
			0.5D + 0.25D * face.getFrontOffsetZ());
		aim(player, front.addVector(face.getFrontOffsetX() * 1.5D, 0, face.getFrontOffsetZ() * 1.5D), front);
		player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
		require(player.interactionManager.tryHarvestBlock(pos), "Could not mine bedside cabinet");
		require(world.isAirBlock(pos) && world.isAirBlock(pos.up()), "Breaking cabinet left a ghost display");
		require(count(world, pos, first) == 3 && count(world, pos, second) == 7 && count(world, pos, decoration) == 1
			&& count(world, pos, new ItemStack(block)) == 1, "Cabinet drops did not match the two drawers and the display");
	}

	private static void verifyHoppers(WorldServer world, BlockPos pos, TileEntity cabinet, EnumFacing facing) {
		IItemHandler front = cabinet.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
		IItemHandler back = cabinet.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
		ItemStack sample = new ItemStack(Items.STICK);
		require(front.getSlots() == 18 && back.insertItem(17, sample.copy(), false) == null
			&& ItemStack.areItemStacksEqual(front.insertItem(0, sample.copy(), false), sample)
			&& ItemStack.areItemStacksEqual(front.extractItem(17, 1, false), sample), "Sided drawer access differs from cabinets");

		IBlockState hopper = Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN);
		world.setBlockState(pos.up(), hopper);
		TileEntityHopper input = (TileEntityHopper)world.getTileEntity(pos.up());
		input.setInventorySlotContents(0, sample.copy());
		input.update();
		require(input.getStackInSlot(0) == null && ItemStack.areItemStacksEqual(back.getStackInSlot(0), sample),
			"Vanilla hopper could not insert into the drawers");
		world.setBlockToAir(pos.up());

		world.setBlockState(pos.down(), hopper);
		TileEntityHopper output = (TileEntityHopper)world.getTileEntity(pos.down());
		output.update();
		require(ItemStack.areItemStacksEqual(output.getStackInSlot(0), sample) && back.getStackInSlot(0) == null,
			"Vanilla hopper could not extract from the drawers");
		output.clear();
		world.setBlockToAir(pos.down());
	}

	private static void openDrawer(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing face,
			boolean upper, IInventory expected) {
		Vec3d target = new Vec3d(pos).addVector(0.5D, upper ? 0.52D : 0.29D, 0.5D);
		Vec3d eye = target.addVector(face.getFrontOffsetX() * 1.5D, 0, face.getFrontOffsetZ() * 1.5D);
		aim(player, eye, target);
		RayTraceResult hit = world.rayTraceBlocks(eye, target);
		require(hit != null && pos.equals(hit.getBlockPos()) && hit.sideHit == face, "Drawer front ray missed " + face);
		click(world, player, pos, face, hit.hitVec, null);
		require(player.openContainer instanceof ContainerChest
			&& ((ContainerChest)player.openContainer).getLowerChestInventory() == expected
			&& expected.getSizeInventory() == 9, "Wrong drawer screen on " + face);
	}

	private static void aim(FakePlayer player, Vec3d eye, Vec3d target) {
		Vec3d direction = target.subtract(eye);
		float yaw = (float)Math.toDegrees(Math.atan2(-direction.xCoord, direction.zCoord));
		float pitch = (float)-Math.toDegrees(Math.atan2(direction.yCoord, Math.hypot(direction.xCoord, direction.zCoord)));
		player.setPositionAndRotation(eye.xCoord, eye.yCoord - player.getEyeHeight(), eye.zCoord, yaw, pitch);
	}

	private static void click(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing side, Vec3d point, ItemStack held) {
		player.setHeldItem(EnumHand.MAIN_HAND, held);
		IBlockState state = world.getBlockState(pos);
		require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, held, side,
			(float)(point.xCoord - pos.getX()), (float)(point.yCoord - pos.getY()), (float)(point.zCoord - pos.getZ())),
			"Could not activate the bedside cabinet");
	}

	private static boolean stored(TileEntityBedsideCabinet cabinet, ItemStack stack) {
		return ItemStack.areItemStacksEqual(cabinet.getSurfaceSetting().getCompatibilityItem(), stack);
	}

	private static int count(WorldServer world, BlockPos pos, ItemStack stack) {
		int count = 0;

		for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
			ItemStack actual = drop.getEntityItem();
			if (!drop.isDead && ItemStack.areItemsEqual(stack, actual) && ItemStack.areItemStackTagsEqual(stack, actual)) {
				count += actual.stackSize;
			}
		}

		return count;
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
