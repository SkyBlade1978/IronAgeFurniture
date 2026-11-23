package zone.moddev.mc.ironagefurniture.fixture;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageCrate;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockCrate;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.container.ContainerCrate;
import zone.moddev.mc.ironagefurniture.api.container.ContainerFurnitureLabel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

final class CrateProbe {
	private CrateProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block block = Block.getBlockFromName("ironagefurniture:crate_wood_ironage_oak");
		for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
			BlockPos pos = origin.east(5 * face.getHorizontalIndex());
			IBlockState state = block.getDefaultState().withProperty(StorageCrate.FACING, face);
			world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 3);
			world.setBlockState(pos, state, 3);
			TileEntity tile = world.getTileEntity(pos);
			TileEntityCrate crate = (TileEntityCrate)tile;
			testCrate(world, player, pos, state, crate, tile, face);
		}
		player.inventory.clear();
		LogManager.getLogger().info("IAF CRATE PROBE PASSED: all facings, hoppers, menus, label, packing, placement and exact drops");
	}

	private static void testCrate(WorldServer world, FakePlayer player, BlockPos pos, IBlockState state,
			TileEntityCrate crate, TileEntity tile, EnumFacing face) {
		ItemStack sample = new ItemStack(Items.BOOK, 3).setStackDisplayName("Seafreight inventory");
		ItemStack sign = new ItemStack(Items.SIGN).setStackDisplayName("Market sign");
		IItemHandler openPort = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
		require(openPort != null && openPort.getSlots() == 16, "Crate has the wrong slot count");
		verifyHopper(world, pos, openPort);
		crate.getInventory().setInventorySlotContents(15, sample.copy());

		Vec3d centre = new Vec3d(pos).add(StorageCrate.labelAnchor(face)).addVector(0, 0.04D, 0);
		Vec3d eye = centre.addVector(face.getFrontOffsetX() * 1.5D, 0, face.getFrontOffsetZ() * 1.5D);
		float yaw = (float)Math.toDegrees(Math.atan2(face.getFrontOffsetX(), -face.getFrontOffsetZ()));
		player.setPositionAndRotation(eye.xCoord, eye.yCoord - player.getEyeHeight(), eye.zCoord, yaw, 0);
		player.inventory.clear();
		player.setHeldItem(EnumHand.MAIN_HAND, sign.copy());
		StorageGuiHandler handler = new StorageGuiHandler();
		ContainerFurnitureLabel label = (ContainerFurnitureLabel)handler.getServerGuiElement(
			StorageGuiHandler.crateLabelId(EnumHand.MAIN_HAND), player, world, pos.getX(), pos.getY(), pos.getZ());
		require(label != null && label.save(player, "Market stock") && player.getHeldItemMainhand() == null,
			"Crate label did not use one exact sign");
		RayTraceResult labelHit = state.collisionRayTrace(world, pos, eye, centre.addVector(
			-face.getFrontOffsetX(), 0, -face.getFrontOffsetZ()));
		require(labelHit != null && labelHit.sideHit == face && labelHit.subHit >= 0, "Crate sign is not targetable");

		Container menu = new ContainerCrate(crate, player);
		require(menu.canInteractWith(player) && menu.inventorySlots.size() == 52, "Crate has no usable 4x4 GUI");
		player.setPosition(eye.xCoord, eye.yCoord - player.getEyeHeight() + 0.35D, eye.zCoord);
		latch(world, player, pos, state, face);
		require(crate.isSealed() && !menu.canInteractWith(player) && menu.transferStackInSlot(player, 15) == null
			&& tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP) == null
			&& openPort.extractItem(15, 1, false) == null && openPort.insertItem(1, sample, false) == sample,
			"Sealed crate permits GUI or automation access");
		NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
		TileEntity copy = TileEntity.create(world, saved);
		require(copy != null && saved.equals(copy.writeToNBT(new NBTTagCompound()))
			&& !tile.getUpdateTag().hasKey("Items"), "Crate failed exact NBT round trip or exposed its inventory");

		// Mine the latch area so the front label does not intercept the break.
		player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
		require(player.interactionManager.tryHarvestBlock(pos) && world.isAirBlock(pos), "Could not pack by mining");
		ItemStack packed = null;
		int drops = 0;
		for (EntityItem entity : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
			if (!entity.isDead) {
				drops++;
				packed = entity.getEntityItem().copy();
				entity.setDead();
			}
		}
		require(drops == 1 && packed != null && ItemBlockCrate.isPacked(packed) && packed.getMaxStackSize() == 1,
			"Sealed crate did not drop exactly one non-stackable packed item");
		player.setHeldItem(EnumHand.MAIN_HAND, packed);
		require(packed.onItemUse(player, world, pos.down(), EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 1, 0.5F)
			== EnumActionResult.SUCCESS, "Could not place packed crate");
		TileEntity restored = world.getTileEntity(pos);
		TileEntityCrate unpacked = (TileEntityCrate)restored;
		require(unpacked.isSealed() && ItemStack.areItemStacksEqual(sample, unpacked.getInventory().getStackInSlot(15))
			&& ItemStack.areItemStacksEqual(sign, unpacked.getCrateLabel().getSign())
			&& "Market stock".equals(unpacked.getCrateLabel().getText()), "Placement lost crate contents or label");

		IBlockState placed = world.getBlockState(pos);
		player.setHeldItem(EnumHand.MAIN_HAND, null);
		latch(world, player, pos, placed, placed.getValue(StorageCrate.FACING));
		require(!unpacked.isSealed(), "Latch did not reopen the crate");
		IItemHandler unpackedPort = restored.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		packed.stackSize = 1;
		require(unpackedPort.insertItem(0, packed, false) == packed && unpacked.getInventory().getStackInSlot(0) == null
			&& !unpacked.getInventory().isItemValidForSlot(0, packed), "Packed crates can be nested");

		player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
		require(player.interactionManager.tryHarvestBlock(pos), "Could not break the unlatched crate");
		int books = 0, signs = 0, shells = 0;
		for (EntityItem entity : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
			if (entity.isDead) continue;
			ItemStack stack = entity.getEntityItem();
			if (ItemStack.areItemsEqual(stack, sample) && ItemStack.areItemStackTagsEqual(stack, sample)) books += stack.stackSize;
			else if (ItemStack.areItemStacksEqual(stack, sign)) signs++;
			else if (stack.getItem() == packed.getItem() && !ItemBlockCrate.isPacked(stack)) shells++;
			entity.setDead();
		}
		require(books == 3 && signs == 1 && shells == 1, "Unlatched crate did not spill the exact inventory once");
	}

	private static void verifyHopper(WorldServer world, BlockPos pos, IItemHandler port) {
		world.setBlockState(pos.up(), Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN));
		TileEntityHopper hopper = (TileEntityHopper)world.getTileEntity(pos.up());
		hopper.setInventorySlotContents(0, new ItemStack(Items.STICK));
		hopper.update();
		require(hopper.getStackInSlot(0) == null && port.getStackInSlot(0).getItem() == Items.STICK,
			"Vanilla hopper could not insert into unlatched crate");
		port.extractItem(0, 1, false);
		world.setBlockToAir(pos.up());
	}

	private static void latch(WorldServer world, FakePlayer player, BlockPos pos, IBlockState state, EnumFacing face) {
		player.setSneaking(true);
		require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, null, face,
			0.5F, 0.82F, 0.5F), "Sneak-right-click did not reach the front latch");
		player.setSneaking(false);
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new IllegalStateException(message);
	}
}
