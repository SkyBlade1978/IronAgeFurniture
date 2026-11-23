package zone.moddev.mc.ironagefurniture.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/** Migrates the supported CFM 4.1.2 wooden furniture and saved items. */
public final class CfmChairMigration {
    private static final String CFM = "cfm";
    private static final String CHUNK_MIGRATED = "IAFCfmChairMigrated";
    private static final String FURNITURE_MIGRATED = "IAFCfmFurnitureMigrated";
    private static final String CRATES_MIGRATED = "IAFCfmCratesMigrated";
    private static final Map<String, String> CHAIRS;
    private static final Map<String, String> FURNITURE;
    private static volatile Map<Integer, String> missingFurnitureBlockIds = Collections.emptyMap();

    static {
        Map<String, String> chairs = new LinkedHashMap<>();
        chairs.put("cfm:chair_oak", "chair_wood_ironage_classic_oak");
        chairs.put("cfm:chair_spruce", "chair_wood_ironage_classic_spruce");
        chairs.put("cfm:chair_birch", "chair_wood_ironage_classic_birch");
        chairs.put("cfm:chair_jungle", "chair_wood_ironage_classic_jungle");
        chairs.put("cfm:chair_acacia", "chair_wood_ironage_classic_acacia");
        chairs.put("cfm:chair_dark_oak", "chair_wood_ironage_classic_big_oak");
        CHAIRS = Collections.unmodifiableMap(chairs);

        Map<String, String> furniture = new LinkedHashMap<>(chairs);
        for (String wood : new String[] { "oak", "spruce", "birch", "jungle", "acacia", "dark_oak" }) {
            String targetWood = "dark_oak".equals(wood) ? "big_oak" : wood;
            furniture.put("cfm:cabinet_" + wood, "cabinet_wood_ironage_" + targetWood);
            furniture.put("cfm:bedside_cabinet_" + wood, "bedside_cabinet_wood_ironage_" + targetWood);
            furniture.put("cfm:table_" + wood, "table_dining_" + targetWood);
            furniture.put("cfm:coffee_table_" + wood, "table_low_" + targetWood);
        }
        furniture.put(CfmStorageMigration.CRATE_ITEM, CfmStorageMigration.CRATE_TARGET);
        FURNITURE = Collections.unmodifiableMap(furniture);
    }

    public static Map<String, String> chairMappings() {
        return CHAIRS;
    }

    public static void remapMissingMappings(FMLMissingMappingsEvent event) {
        // Forge 1.10 cannot remap a missing ID to a target which was already in the
        // same world snapshot: it registers the target twice and falls back to
        // level.dat_old, mixing up unrelated block IDs. The old numeric IDs
        // are retained for raw chunk migration rather than remapped here.
        Map<Integer, String> blockIds = new HashMap<>();
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            String targetPath = FURNITURE.get(mapping.resourceLocation.toString());
            if (targetPath == null) {
                continue;
            }

            Block target = registeredTarget(targetPath);
            if (target == null) {
                FMLLog.warning("[%s] Cannot migrate missing %s: its equivalent is not registered",
                    Ironagefurniture.MODID, mapping.resourceLocation);
                continue;
            }

            if (mapping.type == GameRegistry.Type.BLOCK) {
                mapping.ignore();
                blockIds.put(mapping.id, targetPath);
                FMLLog.info("[%s] Will migrate missing CFM furniture block %s from numeric ID %d to %s",
                    Ironagefurniture.MODID, mapping.resourceLocation, mapping.id, target.getRegistryName());
            } else if (mapping.type == GameRegistry.Type.ITEM) {
                Item item = Item.getItemFromBlock(target);
                if (item != null) {
                    mapping.ignore();
                    FMLLog.info("[%s] Will migrate missing CFM furniture item %s to %s from saved NBT",
                        Ironagefurniture.MODID, mapping.resourceLocation, item.getRegistryName());
                }
            }
        }
        missingFurnitureBlockIds = Collections.unmodifiableMap(blockIds);
    }

    @SubscribeEvent
    public void loadChunk(ChunkDataEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        Chunk chunk = event.getChunk();
        CfmStorageMigration.saveRestoredInventories(chunk);
        NBTTagCompound raw = event.getData();
        if (!Loader.isModLoaded(CFM)) {
            NBTTagCompound level = raw.getCompoundTag("Level");
            Map<String, String> items = registeredItemIds(FURNITURE);
            int blocks = migrateMissingBlocks(chunk, raw, missingBlockTargets(world, level), items);
            int stacks = restoreMissingChunkItems(chunk, raw, items);
            report(chunk, blocks, stacks);
            return;
        }

        Map<String, String> forced = forcedMappings();
        if (forced.isEmpty()) {
            return;
        }

        Map<String, String> items = registeredItemIds(forced);
        int blocks = migrateLoadedBlocks(chunk, raw, registeredBlockReplacements(forced), items);
        int stacks = migrateChunkInventories(chunk, items);
        report(chunk, blocks, stacks);
    }

    private static void report(Chunk chunk, int blocks, int items) {
        if (blocks + items > 0) {
            chunk.setModified(true);
            FMLLog.info("[%s] Converted %d CFM furniture blocks and %d saved stacks in chunk %d,%d",
                Ironagefurniture.MODID, blocks, items, chunk.xPosition, chunk.zPosition);
        }
    }

    @SubscribeEvent
    public void saveChunk(ChunkDataEvent.Save event) {
        if (!event.getWorld().isRemote && !Loader.isModLoaded(CFM)) {
            Map<Integer, String> ids = migrationData(event.getWorld()).getBlockIds();
            if (!ids.isEmpty()) {
                NBTTagCompound level = event.getData().getCompoundTag("Level");
                level.setBoolean(CHUNK_MIGRATED, true);
                level.setBoolean(FURNITURE_MIGRATED, true);
                if (ids.containsValue(CfmStorageMigration.CRATE_TARGET)) {
                    level.setBoolean(CRATES_MIGRATED, true);
                }
            }
        }
    }

    @SubscribeEvent
    public void unloadWorld(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            missingFurnitureBlockIds = Collections.emptyMap();
        }
    }

    private static CfmChairMigrationData migrationData(World world) {
        CfmChairMigrationData data = (CfmChairMigrationData)world.getMapStorage()
                .getOrLoadData(CfmChairMigrationData.class, CfmChairMigrationData.NAME);
        if (data == null) {
            data = new CfmChairMigrationData(CfmChairMigrationData.NAME);
            world.getMapStorage().setData(CfmChairMigrationData.NAME, data);
        }
        if (!data.getBlockIds().entrySet().containsAll(missingFurnitureBlockIds.entrySet())) {
            Map<Integer, String> observed = new HashMap<>(data.getBlockIds());
            observed.putAll(missingFurnitureBlockIds);
            data.setBlockIds(observed);
            // Persist the numeric mapping before any converted chunk can be saved.
            world.getMapStorage().saveAllData();
        }
        return data;
    }

    private static Map<Integer, Block> missingBlockTargets(World world, NBTTagCompound level) {
        Map<Integer, Block> targets = new HashMap<>();
        for (Map.Entry<Integer, String> entry : migrationData(world).getBlockIds().entrySet()) {
            String path = entry.getValue();
            String marker = CfmStorageMigration.CRATE_TARGET.equals(path) ? CRATES_MIGRATED
                : CHAIRS.containsValue(path) ? CHUNK_MIGRATED : FURNITURE_MIGRATED;
            if (level.getBoolean(marker)) {
                continue;
            }
            Block target = registeredTarget(path);
            if (target != null) targets.put(entry.getKey(), target);
        }
        return targets;
    }

    static int migrateMissingChairBlocks(Chunk chunk, NBTTagCompound rawChunk, Map<Integer, Block> targets) {
        return migrateMissingBlocks(chunk, rawChunk, targets, Collections.emptyMap());
    }

    private static int migrateMissingBlocks(Chunk chunk, NBTTagCompound rawChunk,
            Map<Integer, Block> targets, Map<String, String> items) {
        if (targets.isEmpty()) return 0;
        Map<BlockPos, NBTTagCompound> sourceTiles = CfmStorageMigration.sourceTiles(rawChunk);
        NBTTagList sections = rawChunk.getCompoundTag("Level").getTagList("Sections", 10);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        int converted = 0;
        for (int s = 0; s < sections.tagCount(); ++s) {
            NBTTagCompound rawSection = sections.getCompoundTagAt(s);
            int sectionY = rawSection.getByte("Y") & 255;
            if (sectionY >= storage.length || storage[sectionY] == null) continue;
            byte[] blocks = rawSection.getByteArray("Blocks");
            byte[] metadata = rawSection.getByteArray("Data");
            byte[] add = rawSection.getByteArray("Add");
            if (blocks.length != 4096 || metadata.length != 2048
                    || (add.length != 0 && add.length != 2048)) continue;
            ExtendedBlockStorage section = storage[sectionY];
            for (int index = 0; index < 4096; ++index) {
                int id = (blocks[index] & 255) | (nibble(add, index) << 8);
                Block target = targets.get(id);
                if (target == null) continue;
                int meta = nibble(metadata, index);
                replaceBlock(chunk, section, sectionY, index, replacementState(target, meta), sourceTiles, items);
                ++converted;
            }
        }
        return converted;
    }

    private static int migrateLoadedBlocks(Chunk chunk, NBTTagCompound raw, Map<Block, Block> replacements,
            Map<String, String> items) {
        if (replacements.isEmpty()) return 0;
        Map<BlockPos, NBTTagCompound> sourceTiles = CfmStorageMigration.sourceTiles(raw);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        int converted = 0;
        for (int sectionY = 0; sectionY < storage.length; sectionY++) {
            ExtendedBlockStorage section = storage[sectionY];
            if (section == null || section.isEmpty()) continue;
            for (int index = 0; index < 4096; index++) {
                IBlockState source = section.get(index & 15, index >> 8, index >> 4 & 15);
                Block oldBlock = source.getBlock();
                Block target = replacements.get(oldBlock);
                if (target != null) {
                    IBlockState state = replacementState(target, oldBlock.getMetaFromState(source));
                    replaceBlock(chunk, section, sectionY, index, state, sourceTiles, items);
                    converted++;
                }
            }
        }
        return converted;
    }

    private static IBlockState replacementState(Block target, int sourceMeta) {
        // CFM tables store facing. IAF tables connect to their neighbours and reserve metadata for their tile.
        return target instanceof DiningTable ? target.getDefaultState() : target.getStateFromMeta(sourceMeta & 3);
    }

    private static void replaceBlock(Chunk chunk, ExtendedBlockStorage section, int sectionY, int index,
            IBlockState target, Map<BlockPos, NBTTagCompound> sourceTiles, Map<String, String> items) {
        int x = index & 15;
        int y = index >> 8;
        int z = index >> 4 & 15;
        BlockPos pos = new BlockPos(chunk.xPosition * 16 + x, sectionY * 16 + y, chunk.zPosition * 16 + z);
        TileEntity tile = CfmStorageMigration.prepareTile(chunk, pos, target, sourceTiles.get(pos), items);
        section.set(x, y, z, target);
        if (tile != null) {
            // Updating the raw section and tile avoids CFM's breakBlock dropping the original inventory.
            chunk.addTileEntity(pos, tile);
        }
    }

    private static int nibble(byte[] values, int index) {
        return values.length == 0 ? 0 : (values[index >> 1] >> ((index & 1) << 2)) & 15;
    }

    private static int restoreMissingChunkItems(Chunk chunk, NBTTagCompound rawChunk,
            Map<String, String> ids) {
        NBTTagCompound level = rawChunk.getCompoundTag("Level");
        int converted = 0;
        NBTTagList tiles = level.getTagList("TileEntities", 10);
        for (int i = 0; i < tiles.tagCount(); ++i) {
            NBTTagCompound data = tiles.getCompoundTagAt(i).copy();
            if (CfmStorageMigration.isStorageTile(data)) continue;
            int count = rewriteItemStacks(data, ids);
            if (count == 0) continue;
            TileEntity tile = chunk.getTileEntityMap().get(new BlockPos(
                    data.getInteger("x"), data.getInteger("y"), data.getInteger("z")));
            if (tile == null) continue;
            tile.readFromNBT(data);
            // The chunk is not yet installed in the world. markDirty() would
            // ask the world to load this same chunk recursively here.
            converted += count;
        }
        NBTTagList entities = level.getTagList("Entities", 10);
        for (int i = 0; i < entities.tagCount(); ++i) {
            NBTTagCompound data = entities.getCompoundTagAt(i).copy();
            int count = rewriteItemStacks(data, ids);
            if (count == 0) continue;
            UUID uuid = new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast"));
            Entity oldEntity = null;
            for (ClassInheritanceMultiMap<Entity> group : chunk.getEntityLists()) {
                for (Entity entity : group) {
                    if (uuid.equals(entity.getUniqueID())) {
                        oldEntity = entity;
                        break;
                    }
                }
                if (oldEntity != null) break;
            }
            Entity replacement = EntityList.createEntityFromNBT(data, chunk.getWorld());
            if (replacement != null && !replacement.isDead) {
                if (oldEntity != null) chunk.removeEntity(oldEntity);
                chunk.addEntity(replacement);
                converted += count;
            }
        }
        return converted;
    }

    @SubscribeEvent
    public void recoverPlayerFile(net.minecraftforge.event.entity.player.PlayerEvent.LoadFromFile event) {
        if (Loader.isModLoaded(CFM) || event.getEntityPlayer().world.isRemote) return;
        File file = new File(event.getPlayerDirectory(), event.getPlayerUUID() + ".dat");
        if (!file.isFile()) return;
        try (FileInputStream input = new FileInputStream(file)) {
            NBTTagCompound data = CompressedStreamTools.readCompressed(input);
            Map<String, String> ids = registeredItemIds(FURNITURE);
            int main = recoverPlayerInventory(data.getTagList("Inventory", 10), event.getEntityPlayer(), ids);
            int ender = recoverEnderInventory(data.getTagList("EnderItems", 10), event.getEntityPlayer(), ids);
            if (main + ender > 0) {
                FMLLog.info("[%s] Recovered %d missing CFM furniture stacks for player %s",
                    Ironagefurniture.MODID, main + ender, event.getPlayerUUID());
            }
        } catch (IOException exception) {
            FMLLog.warning("[%s] Could not inspect CFM furniture items for player %s: %s",
                Ironagefurniture.MODID, event.getPlayerUUID(), exception.getMessage());
        }
    }

    private static int recoverPlayerInventory(NBTTagList saved, EntityPlayer player,
            Map<String, String> ids) {
        int count = 0;
        for (int i = 0; i < saved.tagCount(); ++i) {
            NBTTagCompound stack = saved.getCompoundTagAt(i).copy();
            if (rewriteItemStacks(stack, ids) == 0) continue;
            ItemStack replacement = ItemStack.loadItemStackFromNBT(stack);
            if (replacement == null) continue;
            int slot = stack.getByte("Slot") & 255;
            if (slot < 36) player.inventory.mainInventory[slot] = replacement;
            else if (slot >= 100 && slot < 104) player.inventory.armorInventory[slot - 100] = replacement;
            else if (slot == 150) player.inventory.offHandInventory[0] = replacement;
            else continue;
            ++count;
        }
        return count;
    }

    private static int recoverEnderInventory(NBTTagList saved, EntityPlayer player,
            Map<String, String> ids) {
        int count = 0;
        IInventory ender = player.getInventoryEnderChest();
        for (int i = 0; i < saved.tagCount(); ++i) {
            NBTTagCompound stack = saved.getCompoundTagAt(i).copy();
            if (rewriteItemStacks(stack, ids) == 0) continue;
            int slot = stack.getByte("Slot") & 255;
            if (slot >= ender.getSizeInventory()) continue;
            ItemStack replacement = ItemStack.loadItemStackFromNBT(stack);
            if (replacement != null) {
                ender.setInventorySlotContents(slot, replacement);
                ++count;
            }
        }
        return count;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void convertPlacedChair(BlockEvent.PlaceEvent event) {
        World world = event.getWorld();
        if (world.isRemote || event.isCanceled() || !Loader.isModLoaded(CFM) || forcedMappings().isEmpty()) {
            return;
        }
        IBlockState oldState = event.getPlacedBlock();
        Block oldBlock = oldState.getBlock();
        ResourceLocation oldId = oldBlock.getRegistryName();
        String targetPath = oldId == null ? null : CHAIRS.get(oldId.toString());
        Block target = targetPath == null ? null : registeredTarget(targetPath);
        if (target != null) {
            world.setBlockState(event.getPos(), target.getStateFromMeta(oldBlock.getMetaFromState(oldState)), 3);
        }
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote || !Loader.isModLoaded(CFM) || forcedMappings().isEmpty()) {
            return;
        }
        Map<String, String> itemIds = registeredItemIds(forcedMappings());
        migrateInventory(player.inventory, itemIds);
        migrateInventory(player.getInventoryEnderChest(), itemIds);
    }

    static boolean shouldForceConversion(boolean cfmLoaded, boolean optionEnabled) {
        return cfmLoaded && optionEnabled;
    }

    private static Map<String, String> forcedMappings() {
        if (IronAgeFurnitureConfiguration.FORCE_CFM_FURNITURE_CONVERSION) {
            return FURNITURE;
        }
        return IronAgeFurnitureConfiguration.FORCE_CFM_CHAIR_CONVERSION ? CHAIRS : Collections.emptyMap();
    }

    private static Block registeredTarget(String path) {
        ResourceLocation id = new ResourceLocation(Ironagefurniture.MODID, path);
        return Block.REGISTRY.containsKey(id) ? Block.REGISTRY.getObject(id) : null;
    }

    private static Map<Block, Block> registeredBlockReplacements(Map<String, String> mappings) {
        Map<Block, Block> replacements = new IdentityHashMap<>();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            Block target = registeredTarget(entry.getValue());
            if (target == null) {
                continue;
            }
            ResourceLocation sourceId = new ResourceLocation(entry.getKey());
            if (Block.REGISTRY.containsKey(sourceId)) {
                replacements.put(Block.REGISTRY.getObject(sourceId), target);
            }
        }
        return replacements;
    }

    private static Map<String, String> registeredItemIds(Map<String, String> mappings) {
        Map<String, String> ids = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            Block target = registeredTarget(entry.getValue());
            Item item = target == null ? null : Item.getItemFromBlock(target);
            if (item != null && item.getRegistryName() != null) {
                ids.put(entry.getKey(), item.getRegistryName().toString());
            }
        }
        return ids;
    }

    static int migrateChunkBlocks(Chunk chunk, Map<Block, Block> replacements) {
        int converted = 0;
        for (ExtendedBlockStorage section : chunk.getBlockStorageArray()) {
            if (section == null) {
                continue;
            }
            converted += migrateSectionBlocks(section, replacements);
        }
        return converted;
    }

    static int migrateSectionBlocks(ExtendedBlockStorage section, Map<Block, Block> replacements) {
        int converted = 0;
        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    IBlockState oldState = section.get(x, y, z);
                    Block oldBlock = oldState.getBlock();
                    Block target = replacements.get(oldBlock);
                    if (target != null) {
                        section.set(x, y, z, target.getStateFromMeta(oldBlock.getMetaFromState(oldState)));
                        ++converted;
                    }
                }
            }
        }
        return converted;
    }

    private static int migrateChunkInventories(Chunk chunk, Map<String, String> ids) {
        int converted = 0;
        for (TileEntity tile : chunk.getTileEntityMap().values()) {
            NBTTagCompound data = tile.writeToNBT(new NBTTagCompound());
            int changed = rewriteItemStacks(data, ids);
            if (changed > 0) {
                tile.readFromNBT(data);
                // ChunkDataEvent.Load fires before this chunk is installed in the world.
                // markDirty() would ask the world for this same chunk and recurse.
                // loadChunk marks the containing chunk modified after conversion.
                converted += changed;
            }
        }
        for (ClassInheritanceMultiMap<Entity> entities : chunk.getEntityLists()) {
            for (Entity entity : entities) {
                NBTTagCompound data = entity.serializeNBT();
                int changed = rewriteItemStacks(data, ids);
                if (changed > 0) {
                    entity.deserializeNBT(data);
                    converted += changed;
                }
            }
        }
        return converted;
    }

    static int migrateInventory(IInventory inventory, Map<String, String> ids) {
        int changed = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); ++slot) {
            ItemStack oldStack = inventory.getStackInSlot(slot);
            if (oldStack == null) {
                continue;
            }
            NBTTagCompound data = oldStack.writeToNBT(new NBTTagCompound());
            int converted = rewriteItemStacks(data, ids);
            if (converted == 0) {
                continue;
            }
            ItemStack replacement = ItemStack.loadItemStackFromNBT(data);
            if (replacement != null) {
                inventory.setInventorySlotContents(slot, replacement);
                changed += converted;
            }
        }
        if (changed > 0) {
            inventory.markDirty();
        }
        return changed;
    }

    static int rewriteItemStacks(NBTBase tag, Map<String, String> ids) {
        int converted = 0;
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound)tag;
            if (compound.hasKey("id", 8) && compound.hasKey("Count", 99)) {
                String source = compound.getString("id");
                String replacement = ids.get(source);
                if (replacement != null) {
                    if (CfmStorageMigration.CRATE_ITEM.equals(source)) {
                        CfmStorageMigration.migrateCrateItemTag(compound);
                    }
                    compound.setString("id", replacement);
                    compound.setShort("Damage", (short)0);
                    ++converted;
                }
            }
            for (String key : new ArrayList<>(compound.getKeySet())) {
                NBTBase child = compound.getTag(key);
                if (child != null) {
                    converted += rewriteItemStacks(child, ids);
                }
            }
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList)tag;
            for (int i = 0; i < list.tagCount(); ++i) {
                converted += rewriteItemStacks(list.get(i), ids);
            }
        }
        return converted;
    }

}
