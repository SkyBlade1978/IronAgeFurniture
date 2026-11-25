package zone.moddev.mc.ironagefurniture.fixture;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBedsideCabinet;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDiningTable;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityVanillaFoodDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;
import zone.moddev.mc.ironagefurniture.api.Blocks.LowTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;

/** Verifies saved furniture after real chunk loading and deferred repairs. */
@Mod.EventBusSubscriber
public final class SurfaceReloadProbe {
    private static final List<Snapshot> SNAPSHOTS = new ArrayList<>();
    private static MinecraftServer server;
    private static int chunks;
    private static int ticks;

    private SurfaceReloadProbe() { }

    static void start(MinecraftServer loadedServer, int loadedChunks) {
        server = loadedServer;
        chunks = loadedChunks;
        for (int dimension : new int[]{0, -1, 1}) {
            for (TileEntity tile : server.worldServerForDimension(dimension).loadedTileEntityList) {
                if (tile instanceof TileEntityDiningTable || tile instanceof TileEntityCabinet || tile instanceof TileEntityBedsideCabinet
                        || tile instanceof TileEntityVanillaFoodDisplay || tile instanceof TileEntityWallShelf
                        || tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityGlassVase
                        || tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel
                        || tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityBottleRack) {
                    SNAPSHOTS.add(new Snapshot(tile));
                }
            }
        }
        require(!SNAPSHOTS.isEmpty(), "Saved-world fixture contains no Phase 5 furniture");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (server == null || event.phase != TickEvent.Phase.END || ++ticks < 2) return;
        MinecraftServer completedServer = server;
        server = null;
        try {
            int repaired = 0;
            int foods = 0;
            int shelves = 0, lowTables = 0, vases = 0, halfCabinets = 0, barrels = 0, racks = 0;
            Block blocker = Block.getBlockFromName("ironagefurniture:surface_display_blocker");
            for (Snapshot snapshot : SNAPSHOTS) {
                TileEntity tile = snapshot.tile;
                if (tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityHalfCabinet) halfCabinets++;
                if (tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel) barrels++;
                if (tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityBottleRack) racks++;
                if (tile instanceof TileEntityWallShelf) shelves++;
                if (tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityGlassVase) vases++;
                if (tile.getWorld().getBlockState(tile.getPos()).getBlock() instanceof LowTable) lowTables++;
                require(tile.getWorld().getTileEntity(tile.getPos()) == tile, "Reload removed a furniture tile");
                require(tile.writeToNBT(new NBTTagCompound()).equals(snapshot.data),
                        "Reload changed furniture inventory or surface NBT at " + tile.getPos());
                require(tile.getWorld().getBlockState(tile.getPos()).equals(snapshot.state),
                        "Reload changed furniture metadata at " + tile.getPos());
                if (tile instanceof TileEntityVanillaFoodDisplay) {
                    foods++;
                    require(tile.getWorld().getBlockState(tile.getPos().down()).getBlock() == snapshot.below,
                            "Reload changed the vanilla food support");
                } else if (tile instanceof SurfaceSettingHost && ((SurfaceSettingHost)tile).getSurfaceSetting().hasAnyItem()) {
                    Block above = tile.getWorld().getBlockState(tile.getPos().up()).getBlock();
                    if (snapshot.above == Blocks.AIR) {
                        require(above == blocker, "Missing display reservation after real chunk loading");
                        repaired++;
                    } else {
                        require(above == snapshot.above, "Reload replaced an occupied display cell");
                    }
                }
            }
            LogManager.getLogger().info(
                    "IAF SAVED SURFACE RELOAD PASSED: {} saved chunks, {} furniture, {} repaired displays",
                    chunks, SNAPSHOTS.size(), repaired);
            LogManager.getLogger().info("IAF SAVED VANILLA FOOD RELOAD PASSED: {} food displays", foods);
            LogManager.getLogger().info("IAF SAVED SHELF/LOW TABLE RELOAD PASSED: {} shelves, {} low tables", shelves, lowTables);
            LogManager.getLogger().info("IAF SAVED GLASS VASE RELOAD PASSED: {} placed vases", vases);
            LogManager.getLogger().info("IAF SAVED STORAGE RELOAD PASSED: {} half cabinets, {} barrels, {} racks", halfCabinets, barrels, racks);
            verifyBarrelFixtures(completedServer.worldServerForDimension(0));
            BedsideCabinetProbe.verifyReload(completedServer.worldServerForDimension(0));
            StorageHopperProbe.verifyReload(completedServer.worldServerForDimension(0));
            DrawerUnitProbe.verifyReload(completedServer.worldServerForDimension(0));
            StorageBenchProbe.verifyReload(completedServer.worldServerForDimension(0));
            ToolRackProbe.verifyReload(completedServer.worldServerForDimension(0));
            ProduceFurnitureProbe.verifyReload(completedServer.worldServerForDimension(0));
        } finally {
            completedServer.initiateShutdown();
        }
    }

    private static void verifyBarrelFixtures(WorldServer world) {
        BlockPos fluidPos = new BlockPos(68, 120, 305);
        TileEntity tile = world.getTileEntity(fluidPos);
        if (!(tile instanceof TileEntityBarrel)) {
            return;
        }

        TileEntityBarrel fluidBarrel = (TileEntityBarrel)tile;
        FluidStack fluid = fluidBarrel.getFluid();
        require(fluid != null && fluid.amount == 12345 && fluidBarrel.isSealed() && fluid.tag != null
            && "exact batch data".equals(fluid.tag.getString("StorageProof"))
            && fluidBarrel.getBulkStorage().getCount() == 0, "Saved sealed barrel lost fluid or gained solid contents");
        LogManager.getLogger().info("IAF SEALED BARREL RELOAD PASSED: exact 12345 mB and seal");

        BlockPos bulkPos = fluidPos.east(8);
        tile = world.getTileEntity(bulkPos);
        if (tile instanceof TileEntityBarrel) {
            TileEntityBarrel bulk = (TileEntityBarrel)tile;
            IBlockState state = world.getBlockState(bulkPos);
            require(bulk.getBulkStorage().getCount() == 37
                && "Saved grain".equals(bulk.getBulkStorage().getStack().getDisplayName())
                && bulk.getFluidAmount() == 0
                && state.getBlock().getActualState(state, world, bulkPos).getValue(StorageBarrel.OPEN),
                "Saved bulk barrel lost its exact batch or lid state");
            LogManager.getLogger().info("IAF BULK BARREL WORLD RELOAD PASSED: 37 named seeds and the open model");
        }
    }

    private static final class Snapshot {
        final TileEntity tile;
        final NBTTagCompound data;
        final IBlockState state;
        final Block above;
        final Block below;

        Snapshot(TileEntity tile) {
            this.tile = tile;
            this.data = tile.writeToNBT(new NBTTagCompound());
            this.state = tile.getWorld().getBlockState(tile.getPos());
            this.above = tile.getWorld().getBlockState(tile.getPos().up()).getBlock();
            this.below = tile.getWorld().getBlockState(tile.getPos().down()).getBlock();
        }
    }

    private static void require(boolean pass, String message) {
        if (!pass) throw new IllegalStateException(message);
    }
}
