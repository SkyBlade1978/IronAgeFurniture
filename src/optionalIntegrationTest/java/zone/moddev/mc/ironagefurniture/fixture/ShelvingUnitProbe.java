package zone.moddev.mc.ironagefurniture.fixture;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.logging.log4j.LogManager;

final class ShelvingUnitProbe {
	private ShelvingUnitProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block unit = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		require(unit instanceof ShelvingUnit, "Missing oak shelving unit");

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			BlockPos mounted = origin.east(8 * facing.getHorizontalIndex());
			verifyLayout(world, player, mounted, unit, facing, false);
			verifyLayout(world, player, mounted.south(14), unit, facing, true);
		}

		LogManager.getLogger().info("IAF SHELVING UNIT PROBE PASSED: placement, joins, books, discs, eight bottles, NBT and drops in four facings");
	}

	private static void verifyLayout(WorldServer world, FakePlayer player, BlockPos pos, Block unit, EnumFacing facing,
			boolean standing) {
		player.inventory.clear();
		EnumFacing right = facing.rotateY();
		BlockPos rightPos = pos.offset(right);
		BlockPos support = standing ? pos.down() : pos.offset(facing.getOpposite());
		world.setBlockState(support, Blocks.STONE.getDefaultState());
		world.setBlockState(support.offset(right), Blocks.STONE.getDefaultState());

		if (!standing) {
			world.setBlockState(support.up(), Blocks.STONE.getDefaultState());
			world.setBlockState(support.up().offset(right), Blocks.STONE.getDefaultState());
		}

		player.rotationYaw = facing.getOpposite().getHorizontalAngle();
		place(world, player, unit, support, standing ? EnumFacing.UP : facing);
		place(world, player, unit, pos, right);
		place(world, player, unit, pos, EnumFacing.UP);
		place(world, player, unit, pos.up(), right);

		IBlockState state = world.getBlockState(pos);
		require(state.getValue(BlockHorizontal.FACING) == facing && state.getValue(RackBlock.STANDING) == standing,
			"Shelving placement lost its facing or mount");
		require(unit.getActualState(state, world, pos).getValue(RackBlock.CONNECTIONS) == 3
				&& unit.getActualState(world.getBlockState(rightPos.up()), world, rightPos.up())
					.getValue(RackBlock.CONNECTIONS) == 12,
			"Shelving did not join into a 2x2 unit");

		AxisAlignedBB bounds = unit.getBoundingBox(state, world, pos);
		double depth = facing.getAxis() == EnumFacing.Axis.X ? bounds.maxX - bounds.minX : bounds.maxZ - bounds.minZ;
		double centreX = (bounds.minX + bounds.maxX) * 0.5D;
		double centreZ = (bounds.minZ + bounds.maxZ) * 0.5D;
		double offset = standing ? 0 : -0.25D;
		require(depth == 0.5D && centreX == 0.5D + offset * facing.getFrontOffsetX()
			&& centreZ == 0.5D + offset * facing.getFrontOffsetZ(), "Incorrect shelving depth or centre");

		// Exercise loose displays with items that use the generic layout.
		ItemStack lower = new ItemStack(Blocks.COBBLESTONE).setStackDisplayName("Lower shelf stone");
		ItemStack upper = new ItemStack(Items.NETHERBRICK).setStackDisplayName("Upper shelf brick");
		require(click(world, player, pos, facing, lower.copy(), centreX, 0.20D, centreZ)
				&& player.getHeldItemMainhand() == null, "Lower shelf did not consume one item");
		require(click(world, player, pos, facing, upper.copy(), centreX, 0.65D, centreZ)
				&& player.getHeldItemMainhand() == null, "Upper shelf did not consume one item");

		TileEntity tile = world.getTileEntity(pos);
		NBTTagCompound data = tile.writeToNBT(new NBTTagCompound());
		TileEntity restored = TileEntity.create(world, data);
		require(restored instanceof TileEntityShelvingUnit && data.equals(restored.writeToNBT(new NBTTagCompound())),
			"Shelving save data changed after loading");
		TileEntityShelvingUnit shelving = (TileEntityShelvingUnit)tile;
		checkLevel(world, pos, facing, shelving.getLevel(false), lower);
		checkLevel(world, pos, facing, shelving.getLevel(true), upper);
		require(ShelvingUnit.getItemBounds(shelving.getLevel(true), Slot.GUEST_A, false) == null,
			"An empty shelf slot had an item hitbox");

		require(click(world, player, pos, facing, null, centreX, 0.65D, centreZ), "Upper shelf item cannot be retrieved");
		require(ItemStack.areItemStacksEqual(player.getHeldItemMainhand(), upper)
			&& ItemStack.areItemStacksEqual(shelving.getLevel(false).getSurfaceSetting().getItem(Slot.CENTER), lower),
			"Retrieval changed the item or the other shelf level");
		require(click(world, player, pos, facing, upper.copy(), centreX, 0.65D, centreZ), "Upper shelf cannot be refilled");

		world.setBlockToAir(rightPos.up());
		require(unit.getActualState(world.getBlockState(rightPos), world, rightPos).getValue(RackBlock.CONNECTIONS) == 8,
			"Join mask did not refresh after shelf removal");
		world.setBlockToAir(pos);
		List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1));
		require(drops.stream().filter(entity -> ItemStack.areItemStacksEqual(entity.getEntityItem(), lower)).count() == 1
			&& drops.stream().filter(entity -> ItemStack.areItemStacksEqual(entity.getEntityItem(), upper)).count() == 1,
			"Breaking a unit must drop the exact contents of both levels once");

		verifyMedia(world, player, rightPos, facing, centreX, centreZ);
		world.setBlockState(rightPos, unit.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
			.withProperty(RackBlock.STANDING, standing));
		verifyBottles(world, player, rightPos, facing, centreX, centreZ);
	}

	private static void verifyBottles(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing facing,
			double centreX, double centreZ) {
		player.inventory.clear();
		List<ItemStack> samples = Phase5BottleProbe.samples();
		List<ItemStack> placed = new ArrayList<>();
		for (int index = 0; index < 8; index++) {
			int sample = index >= 6 && samples.size() > 8 ? index + 1 : index % samples.size();
			ItemStack bottle = samples.get(sample).copy().setStackDisplayName("Shelf bottle " + index);
			require(click(world, player, pos, facing, bottle.copy(), centreX, 0.65D, centreZ)
				&& player.getHeldItemMainhand() == null, "Shelf did not consume exactly one bottle");
			placed.add(bottle);
		}

		TileEntity tile = world.getTileEntity(pos);
		ShelfLevel level = ((TileEntityShelvingUnit)tile).getLevel(true);
		require(level.getMediaKind() == ShelfContentKind.BOTTLES && level.getMediaCount() == 8,
			"Shelving did not hold eight bottles");
		click(world, player, pos, facing, placed.get(0).copy(), centreX, 0.65D, centreZ);
		require(player.getHeldItemMainhand() != null && level.getMediaCount() == 8, "A full shelf swallowed a ninth bottle");

		IBlockState state = world.getBlockState(pos);
		AxisAlignedBB opening = state.getBlock().getBoundingBox(state, world, pos);
		boolean alongX = facing.getAxis() == EnumFacing.Axis.Z;
		for (int index = 0; index < 8; index++) {
			AxisAlignedBB bottle = level.getBottleBounds(index);
			require(bottle.minX > opening.minX + (alongX ? ShelvingUnit.BOARD_THICKNESS : 0)
				&& bottle.maxX < opening.maxX - (alongX ? ShelvingUnit.BOARD_THICKNESS : 0)
				&& bottle.minZ > opening.minZ + (alongX ? 0 : ShelvingUnit.BOARD_THICKNESS)
				&& bottle.maxZ < opening.maxZ - (alongX ? 0 : ShelvingUnit.BOARD_THICKNESS)
				&& bottle.minY >= level.getSurfaceHeight() && bottle.maxY < 1 - ShelvingUnit.BOARD_THICKNESS,
				"Bottle hitbox crossed the shelf frame");
			for (int previous = 0; previous < index; previous++) {
				AxisAlignedBB other = level.getBottleBounds(previous);
				require(bottle.maxX < other.minX || other.maxX < bottle.minX
					|| bottle.maxZ < other.minZ || other.maxZ < bottle.minZ, "Bottle footprints overlap");
			}
		}

		NBTTagCompound saved = tile.writeToNBT(new NBTTagCompound());
		TileEntity loaded = TileEntity.create(world, saved);
		require(loaded instanceof TileEntityShelvingUnit && saved.equals(loaded.writeToNBT(new NBTTagCompound())),
			"Shelf save lost exact potion or fluid data");

		SurfaceSetting legacy = new SurfaceSetting();
		legacy.insert(placed.get(0), facing, 0.25F, 0.25F, level.getSurfaceAxis());
		legacy.insert(placed.get(1), facing, 0.75F, 0.75F, level.getSurfaceAxis());
		NBTTagCompound legacyTag = new NBTTagCompound();
		legacy.writeToNBT(legacyTag);
		saved.setTag("UpperShelf", legacyTag);
		TileEntityShelvingUnit migrated = (TileEntityShelvingUnit)TileEntity.create(world, saved);
		ShelfLevel migratedLevel = migrated.getLevel(true);
		require(migratedLevel.getMediaCount() == 2 && !migratedLevel.getSurfaceSetting().hasAnyItem()
			&& !ItemStack.areItemStacksEqual(migratedLevel.copyMediaItem(0), migratedLevel.copyMediaItem(1))
			&& placed.subList(0, 2).stream().allMatch(bottle -> ItemStack.areItemStacksEqual(bottle, migratedLevel.copyMediaItem(0))
				|| ItemStack.areItemStacksEqual(bottle, migratedLevel.copyMediaItem(1))),
			"Legacy bottle display did not migrate with both exact stacks");

		AxisAlignedBB frontBottle = level.getBottleBounds(0).offset(pos);
		Vec3d target = new Vec3d((frontBottle.minX + frontBottle.maxX) / 2, (frontBottle.minY + frontBottle.maxY) / 2,
			(frontBottle.minZ + frontBottle.maxZ) / 2);
		Vec3d front = target.addVector(facing.getFrontOffsetX(), 0, facing.getFrontOffsetZ());
		RayTraceResult hit = state.collisionRayTrace(world, pos, front, target);
		require(hit != null && hit.subHit >= 100, "Could not target the front bottle");
		player.setPositionAndRotation(front.xCoord, front.yCoord - player.getEyeHeight(), front.zCoord,
			facing.getOpposite().getHorizontalAngle(), 0);
		require(click(world, player, pos, facing, null, hit.hitVec.xCoord - pos.getX(), hit.hitVec.yCoord - pos.getY(),
			hit.hitVec.zCoord - pos.getZ()), "A bottle hit did not open its picker");

		StorageGuiHandler gui = new StorageGuiHandler();
		int id = StorageGuiHandler.shelvingMediaId(ShelfContentKind.BOTTLES, true);
		Container picker = (Container)gui.getServerGuiElement(id, player, world, pos.getX(), pos.getY(), pos.getZ());
		require(picker != null && picker.inventorySlots.size() == 8 && picker.enchantItem(player, 6)
				&& !picker.enchantItem(player, 6) && picker.enchantItem(player, 2),
			"Bottle picker did not support multiple exact picks in one window");
		require(ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(0), placed.get(6))
				&& ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(1), placed.get(2)),
			"Picker returned a different potion or fluid stack");

		world.setBlockToAir(pos);
		List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1));
		for (int index = 0; index < placed.size(); index++) {
			ItemStack exact = placed.get(index);
			long count = drops.stream().filter(entity -> ItemStack.areItemStacksEqual(entity.getEntityItem(), exact)).count();
			require(count == (index == 2 || index == 6 ? 0 : 1), "Breaking the shelf lost or duplicated a bottle");
		}
	}

	private static void verifyMedia(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing facing,
			double centreX, double centreZ) {
		player.inventory.clear();
		List<ItemStack> placed = new ArrayList<>();
		for (int index = 0; index < 5; index++) {
			ItemStack book = new ItemStack(Items.WRITTEN_BOOK).setStackDisplayName("Shelving book " + index);
			NBTTagList pages = new NBTTagList();
			pages.appendTag(new NBTTagString("{\"text\":\"Shelf page " + index + "\"}"));
			book.getTagCompound().setTag("pages", pages);
			book.getTagCompound().setString("author", "IAF probe");
			require(click(world, player, pos, facing, book.copy(), centreX, 0.20D, centreZ)
				&& player.getHeldItemMainhand() == null, "Shelving did not consume one book");
			placed.add(book);
		}

		for (int index = 0; index < 10; index++) {
			ItemStack disc = new ItemStack(index % 2 == 0 ? Items.RECORD_CAT : Items.RECORD_13)
				.setStackDisplayName("Shelving disc " + index);
			require(click(world, player, pos, facing, disc.copy(), centreX, 0.65D, centreZ)
				&& player.getHeldItemMainhand() == null, "Shelving did not consume one disc");
			placed.add(disc);
		}

		TileEntityShelvingUnit tile = (TileEntityShelvingUnit)world.getTileEntity(pos);
		require(tile.getLevel(false).getMediaCount() == 5 && tile.getLevel(true).getMediaCount() == 10,
			"Books and discs did not fill their own levels");
		click(world, player, pos, facing, new ItemStack(Items.BOOK), centreX, 0.20D, centreZ);
		require(player.getHeldItemMainhand() != null && tile.getLevel(false).getMediaCount() == 5,
			"A full shelf swallowed an extra book");
		click(world, player, pos, facing, new ItemStack(Items.RECORD_CAT), centreX, 0.65D, centreZ);
		require(player.getHeldItemMainhand() != null && tile.getLevel(true).getMediaCount() == 10,
			"A full shelf swallowed an eleventh disc");

		NBTTagCompound data = ((TileEntity)tile).writeToNBT(new NBTTagCompound());
		TileEntity restored = TileEntity.create(world, data);
		require(restored instanceof TileEntityShelvingUnit && data.equals(restored.writeToNBT(new NBTTagCompound())),
			"Saved book and disc arrangements changed on reload");

		EnumFacing right = facing.rotateY();
		double outsideDisc = 0.354375D;
		Vec3d centre = new Vec3d(pos.getX() + centreX + outsideDisc * right.getFrontOffsetX(),
			pos.getY() + 0.70D, pos.getZ() + centreZ + outsideDisc * right.getFrontOffsetZ());
		Vec3d front = centre.addVector(facing.getFrontOffsetX(), 0, facing.getFrontOffsetZ());
		RayTraceResult hit = world.getBlockState(pos).collisionRayTrace(world, pos, front, centre);
		require(hit != null && hit.subHit >= 100, "Could not click the outer disc on a full shelf");
		player.setPositionAndRotation(front.xCoord, front.yCoord - player.getEyeHeight(), front.zCoord,
			facing.getOpposite().getHorizontalAngle(), 0);
		require(click(world, player, pos, facing, null, hit.hitVec.xCoord - pos.getX(),
				hit.hitVec.yCoord - pos.getY(), hit.hitVec.zCoord - pos.getZ())
			&& player.getHeldItemMainhand() == null, "Opening the picker removed a disc before selection");
		StorageGuiHandler gui = new StorageGuiHandler();
		int guiId = StorageGuiHandler.shelvingMediaId(ShelfContentKind.RECORDS, true);
		Container picker = (Container)gui.getServerGuiElement(guiId, player, world, pos.getX(), pos.getY(), pos.getZ());
		Container stale = (Container)gui.getServerGuiElement(guiId, player, world, pos.getX(), pos.getY(), pos.getZ());
		ItemStack chosen = placed.get(8);
		require(picker != null && stale != null && !picker.enchantItem(player, 10),
			"Disc selector did not open or accepted an invalid slot");
		require(picker.enchantItem(player, 3) && ItemStack.areItemStacksEqual(player.getHeldItemMainhand(), chosen)
				&& ItemStack.areItemStacksEqual(tile.getLevel(true).copyMediaItem(3), placed.get(9)),
			"Selecting a middle disc did not return the exact item and compact the shelf");
		require(!stale.enchantItem(player, 3) && tile.getLevel(true).getMediaCount() == 9,
			"A stale menu could remove a different disc after the row compacted");
		require(picker.canInteractWith(player) && !picker.enchantItem(player, 3)
				&& picker.enchantItem(player, 4)
				&& ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(1), placed.get(9))
				&& tile.getLevel(true).getMediaCount() == 8,
			"A repeated click took another disc or the next selection did not reach the next free slot");
		player.inventory.setInventorySlotContents(1, null);
		click(world, player, pos, facing, placed.get(9).copy(), centreX, 0.65D, centreZ);
		click(world, player, pos, facing, new ItemStack(Items.BOOK), centreX, 0.65D, centreZ);
		require(player.getHeldItemMainhand() != null && tile.getLevel(true).getMediaCount() == 9,
			"A book was mixed into the disc level");
		click(world, player, pos, facing, chosen.copy(), centreX, 0.65D, centreZ);

		require(click(world, player, pos, facing, null, centreX, 0.20D, centreZ)
				&& player.getHeldItemMainhand() == null, "Opening the book picker removed a book before selection");
		int bookGuiId = StorageGuiHandler.shelvingMediaId(ShelfContentKind.BOOKS, false);
		Container bookPicker = (Container)gui.getServerGuiElement(bookGuiId, player, world,
			pos.getX(), pos.getY(), pos.getZ());
		ItemStack chosenBook = placed.get(2);
		for (int slot = 0; slot < player.inventory.mainInventory.length; slot++) {
			player.inventory.setInventorySlotContents(slot, new ItemStack(Blocks.COBBLESTONE, 64));
		}
		require(bookPicker != null && !bookPicker.enchantItem(player, 2) && bookPicker.canInteractWith(player)
				&& ItemStack.areItemStacksEqual(tile.getLevel(false).copyMediaItem(2), chosenBook),
			"A full inventory lost the selected book or closed the picker");
		player.inventory.clear();
		require(bookPicker != null && bookPicker.inventorySlots.size() == 5 && bookPicker.enchantItem(player, 2)
				&& ItemStack.areItemStacksEqual(player.getHeldItemMainhand(), chosenBook)
				&& ItemStack.areItemStacksEqual(tile.getLevel(false).copyMediaItem(2), placed.get(3))
				&& tile.getLevel(true).getMediaCount() == 10,
			"Selecting a book did not preserve its pages and author or affected the other level");
		click(world, player, pos, facing, chosenBook.copy(), centreX, 0.20D, centreZ);

		NBTTagCompound loose = new NBTTagCompound();
		SurfaceSetting oldSetting = new SurfaceSetting();
		oldSetting.setSingleItem(placed.get(0), facing);
		oldSetting.writeToNBT(loose);
		data.setTag("LowerShelf", loose);
		TileEntityShelvingUnit converted = (TileEntityShelvingUnit)TileEntity.create(world, data);
		require(converted.getLevel(false).getMediaCount() == 1
				&& ItemStack.areItemStacksEqual(converted.getLevel(false).removeLastMedia(), placed.get(0)),
			"An old loose book was lost during conversion");

		IBlockState state = world.getBlockState(pos);
		Block unit = state.getBlock();
		AxisAlignedBB dropArea = new AxisAlignedBB(pos).expand(1, 1, 1);
		long framesBeforeMining = world.getEntitiesWithinAABB(EntityItem.class, dropArea).stream()
			.filter(entity -> entity.getEntityItem().getItem() == Item.getItemFromBlock(unit)).count();
		require(unit.removedByPlayer(state, world, pos, player, true)
				&& world.getBlockState(pos).getBlock() == unit && tile.getLevel(true).getMediaCount() == 9,
			"Mining the disc display must remove one disc and retain the shelving unit");
		unit.harvestBlock(world, player, pos, state, tile, new ItemStack(Items.IRON_AXE));
		List<EntityItem> minedDrops = world.getEntitiesWithinAABB(EntityItem.class, dropArea);
		require(minedDrops.stream().filter(entity -> entity.getEntityItem().getItem() == Item.getItemFromBlock(unit))
				.count() == framesBeforeMining,
			"Mining a disc generated a duplicate shelving unit");
		require(minedDrops.stream().filter(entity -> ItemStack.areItemStacksEqual(entity.getEntityItem(), chosen))
				.count() == 1, "Mining did not drop the exact last disc");

		world.setBlockToAir(pos);
		List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class, dropArea);
		for (ItemStack media : placed) {
			require(drops.stream().filter(entity -> ItemStack.areItemStacksEqual(entity.getEntityItem(), media)).count() == 1,
				"Broken shelving did not drop the exact book or disc once");
		}
	}

	private static void checkLevel(WorldServer world, BlockPos pos, EnumFacing facing, ShelfLevel level,
			ItemStack expected) {
		require(ItemStack.areItemStacksEqual(level.getSurfaceSetting().getItem(Slot.CENTER), expected),
			"Shelving mixed up items between levels");
		AxisAlignedBB bounds = ShelvingUnit.getItemBounds(level, Slot.CENTER, false).offset(pos);
		Vec3d centre = new Vec3d((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2,
			(bounds.minZ + bounds.maxZ) / 2);
		Vec3d front = centre.addVector(2 * facing.getFrontOffsetX(), 0, 2 * facing.getFrontOffsetZ());
		RayTraceResult hit = world.getBlockState(pos).collisionRayTrace(world, pos, front, centre);
		require(hit != null && hit.subHit >= 100, "Could not target the displayed item through the open frame");
	}

	private static void place(WorldServer world, FakePlayer player, Block unit, BlockPos pos, EnumFacing face) {
		ItemStack stack = new ItemStack(unit);
		player.setHeldItem(EnumHand.MAIN_HAND, stack);
		Item item = stack.getItem();
		require(item.onItemUse(stack, player, world, pos, EnumHand.MAIN_HAND, face, 0.5F, 0.5F, 0.5F)
			== EnumActionResult.SUCCESS, "Shelving failed placement at " + pos + " facing " + face);
	}

	private static boolean click(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing facing,
			ItemStack item, double x, double y, double z) {
		player.setHeldItem(EnumHand.MAIN_HAND, item);
		IBlockState state = world.getBlockState(pos);
		return state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, item, facing,
			(float)x, (float)y, (float)z);
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
