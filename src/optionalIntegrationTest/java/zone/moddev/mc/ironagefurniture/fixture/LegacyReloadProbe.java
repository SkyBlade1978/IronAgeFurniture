package zone.moddev.mc.ironagefurniture.fixture;

import java.io.File;
import java.io.RandomAccessFile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;

/** Loads every saved chunk in a disposable legacy fixture before its second save. */
public final class LegacyReloadProbe {
    public static boolean run(MinecraftServer server) {
        String path = System.getProperty("iaf.probe.legacyAuditWorld");
        if (path == null || path.isEmpty()) return false;
        int chunks = 0;
        for (int dimension : new int[] {0, -1, 1}) {
            WorldServer world = server.worldServerForDimension(dimension);
            File regionDirectory = new File(new File(path, dimension == 0 ? "" : "DIM" + dimension), "region");
            File[] regions = regionDirectory.listFiles((dir, name) -> name.matches("r\\.-?\\d+\\.-?\\d+\\.mca"));
            if (world == null || regions == null) continue;
            for (File region : regions) {
                String[] coordinates = region.getName().split("\\.");
                int regionX = Integer.parseInt(coordinates[1]), regionZ = Integer.parseInt(coordinates[2]);
                try (RandomAccessFile input = new RandomAccessFile(region, "r")) {
                    int[] locations = new int[1024];
                    for (int slot = 0; slot < 1024; ++slot) locations[slot] = input.readInt();
                    for (int slot = 0; slot < 1024; ++slot) if (locations[slot] != 0) {
                        world.getChunkProvider().provideChunk(regionX * 32 + (slot & 31), regionZ * 32 + (slot >> 5));
                        ++chunks;
                    }
                } catch (java.io.IOException ex) { throw new IllegalStateException("Cannot read disposable region " + region, ex); }
            }
        }
        if (chunks == 0) throw new IllegalStateException("The legacy fixture has no saved chunks");
        LogManager.getLogger().info("IAF LEGACY FULL-CHUNK RELOAD PASSED: {} saved chunks loaded", chunks);
        server.initiateShutdown();
        return true;
    }
}
