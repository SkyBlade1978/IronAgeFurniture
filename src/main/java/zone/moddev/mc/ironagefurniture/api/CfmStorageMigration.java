package zone.moddev.mc.ironagefurniture.api;

import java.util.HashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.api.Blocks.BedsideCabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageCrate;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBedsideCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/** Translates CFM 4.1.2 storage without invoking the old block's drop logic. */
final class CfmStorageMigration {
	static final String CRATE_ITEM = "cfm:crate";
	static final String CRATE_TARGET = "crate_wood_ironage_big_oak";

	private CfmStorageMigration() { }

	static Map<BlockPos, NBTTagCompound> sourceTiles(NBTTagCompound rawChunk) {
		Map<BlockPos, NBTTagCompound> storage = new HashMap<>();
		NBTTagList tiles = rawChunk.getCompoundTag("Level").getTagList("TileEntities", 10);
		for (int index = 0; index < tiles.tagCount(); index++) {
			NBTTagCompound tile = tiles.getCompoundTagAt(index);
			if (isStorageTile(tile)) {
				storage.put(new BlockPos(tile.getInteger("x"), tile.getInteger("y"), tile.getInteger("z")), tile);
			}
		}
		return storage;
	}

	static boolean isStorageTile(NBTTagCompound tile) {
		String id = tile.getString("id");
		return "cfmCabinet".equals(id) || "cfmBedsideCabinet".equals(id) || "cfmCrate".equals(id);
	}

	static void saveRestoredInventories(Chunk chunk) {
		for (TileEntity tile : chunk.getTileEntityMap().values()) {
			if (tile instanceof TileEntityCabinet && ((TileEntityCabinet)tile).hasRestoredItems()
					|| tile instanceof TileEntityBedsideCabinet && ((TileEntityBedsideCabinet)tile).hasRestoredItems()
					|| tile instanceof TileEntityCrate && ((TileEntityCrate)tile).hasRestoredItems()) {
				// TileEntity.markDirty would recursively load the chunk at this point.
				chunk.setModified(true);
				return;
			}
		}
	}

	static TileEntity prepareTile(Chunk chunk, BlockPos pos, IBlockState target, NBTTagCompound raw,
			Map<String, String> itemIds) {
		boolean bedside = target.getBlock() instanceof BedsideCabinet;
		boolean crate = target.getBlock() instanceof StorageCrate;
		if (!bedside && !crate && !(target.getBlock() instanceof Cabinet)) {
			return null;
		}

		NBTTagCompound source = raw;
		if (source == null) {
			TileEntity current = chunk.getTileEntityMap().get(pos);
			if (current != null) {
				source = current.writeToNBT(new NBTTagCompound());
			}
		}

		TileEntity destination = target.getBlock().createTileEntity(chunk.getWorld(), target);
		if (source != null) {
			NBTTagCompound data = crate ? transferCrate(source, itemIds) : transferInventory(source, bedside, itemIds);
			destination.readFromNBT(data);
		}

		return destination;
	}

	static NBTTagCompound transferCrate(NBTTagCompound source, Map<String, String> itemIds) {
		requireTile(source, "cfmCrate");
		NBTTagCompound data = source.copy();
		CfmChairMigration.rewriteItemStacks(data, itemIds);
		return data;
	}

	static void migrateCrateItemTag(NBTTagCompound stack) {
		NBTTagCompound tag = stack.getCompoundTag("tag");
		if (!tag.hasKey("Items", 9) && !tag.hasKey("Sealed", 1)) {
			return;
		}

		// Packed CFM crates stored the inventory and seal directly on the item tag.
		NBTTagCompound tile = tag.getCompoundTag("BlockEntityTag");
		if (tag.hasKey("Items", 9)) {
			tile.setTag("Items", tag.getTag("Items"));
			tag.removeTag("Items");
		}
		if (tag.hasKey("Sealed", 1)) {
			tile.setBoolean("Sealed", tag.getBoolean("Sealed"));
			tag.removeTag("Sealed");
		}
		tag.setTag("BlockEntityTag", tile);
	}

	static NBTTagCompound transferInventory(NBTTagCompound source, boolean bedside, Map<String, String> itemIds) {
		requireTile(source, bedside ? "cfmBedsideCabinet" : "cfmCabinet");

		NBTTagCompound data = source.copy();
		NBTTagList oldItems = data.getTagList("cabinetItems", 10);
		NBTTagList newItems = new NBTTagList();
		for (int index = 0; index < oldItems.tagCount(); index++) {
			NBTTagCompound stack = oldItems.getCompoundTagAt(index).copy();
			stack.setByte("Slot", stack.getByte("cabinetSlot"));
			stack.removeTag("cabinetSlot");
			CfmChairMigration.rewriteItemStacks(stack, itemIds);
			newItems.appendTag(stack);
		}

		data.removeTag("cabinetItems");
		// The CFM bedside cabinet had one nine-slot inventory. Keep its slot numbers in the upper drawer.
		data.setTag(bedside ? "UpperItems" : "Items", newItems);
		return data;
	}

	private static void requireTile(NBTTagCompound source, String expected) {
		if (!expected.equals(source.getString("id"))) {
			throw new IllegalStateException("Unexpected CFM storage tile " + source.getString("id")
				+ " at " + source.getInteger("x") + "," + source.getInteger("y") + "," + source.getInteger("z"));
		}
	}
}
