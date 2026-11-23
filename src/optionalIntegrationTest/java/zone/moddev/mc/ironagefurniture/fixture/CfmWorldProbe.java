package zone.moddev.mc.ironagefurniture.fixture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

/** Loads only the audited chunks from a disposable CFM migration world. */
final class CfmWorldProbe {
	private CfmWorldProbe() { }

	static boolean run(MinecraftServer server) {
		String path = System.getProperty("iaf.probe.cfmAuditChunks");
		if (path == null) return false;

		try {
			int loaded = 0;
			for (String line : Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8)) {
				String[] coordinates = line.split(",");
				WorldServer world = server.worldServerForDimension(Integer.parseInt(coordinates[0]));
				if (world == null) throw new IllegalStateException("Missing CFM audit dimension: " + line);
				world.getChunkProvider().provideChunk(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
				loaded++;
			}

			LogManager.getLogger().info("IAF CFM AUDIT SAVING: {} inventoried chunks", loaded);
			return true;
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read the disposable CFM audit positions", exception);
		} finally {
			server.initiateShutdown();
		}
	}
}
