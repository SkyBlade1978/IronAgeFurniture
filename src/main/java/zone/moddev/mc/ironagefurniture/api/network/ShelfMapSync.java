package zone.moddev.mc.ironagefurniture.api.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Shares explored pixels with clients who can see an open map on a shelf. */
public final class ShelfMapSync {
	public static final ShelfMapSync INSTANCE = new ShelfMapSync();
	private static final int MAP_SIZE = 128;
	private final Map<WorldServer, WorldMaps> worlds = new IdentityHashMap<>();

	private ShelfMapSync() { }

	public void update(TileEntityShelvingUnit shelf) {
		if (!(shelf.getWorld() instanceof WorldServer)) {
			return;
		}

		if (getMap(shelf, false) != null || getMap(shelf, true) != null) {
			WorldServer world = (WorldServer)shelf.getWorld();
			this.worlds.computeIfAbsent(world, ignored -> new WorldMaps()).shelves.add(shelf);
		} else {
			this.remove(shelf);
		}
	}

	public void remove(TileEntityShelvingUnit shelf) {
		if (shelf.getWorld() instanceof WorldServer) {
			WorldMaps maps = this.worlds.get(shelf.getWorld());
			if (maps != null) {
				maps.shelves.remove(shelf);
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if (event.getWorld() instanceof WorldServer) {
			this.worlds.remove(event.getWorld());
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)
				|| event.world.getTotalWorldTime() % 20 != 0) {
			return;
		}

		WorldServer world = (WorldServer)event.world;
		WorldMaps maps = this.worlds.get(world);
		if (maps == null) {
			return;
		}

		maps.shelves.removeIf(shelf -> shelf.isInvalid() || !world.isBlockLoaded(shelf.getPos()));
		if (maps.shelves.isEmpty()) {
			this.worlds.remove(world);
			return;
		}

		Map<Integer, Set<EntityPlayerMP>> visible = new HashMap<>();
		for (TileEntityShelvingUnit shelf : maps.shelves) {
			BlockPos pos = shelf.getPos();
			ItemStack lower = getMap(shelf, false);
			ItemStack upper = getMap(shelf, true);

			for (EntityPlayer entity : world.playerEntities) {
				EntityPlayerMP player = (EntityPlayerMP)entity;
				if (world.getPlayerChunkMap().isPlayerWatchingChunk(player, pos.getX() >> 4, pos.getZ() >> 4)) {
					if (lower != null) {
						visible.computeIfAbsent(lower.getMetadata(), ignored -> new HashSet<>()).add(player);
					}
					if (upper != null) {
						visible.computeIfAbsent(upper.getMetadata(), ignored -> new HashSet<>()).add(player);
					}
				}
			}
		}

		maps.sent.keySet().retainAll(visible.keySet());
		for (Map.Entry<Integer, Set<EntityPlayerMP>> entry : visible.entrySet()) {
			int id = entry.getKey();
			MapData data = (MapData)world.loadData(MapData.class, "map_" + id);
			if (data == null) {
				maps.sent.remove(id);
				continue;
			}

			maps.sent.computeIfAbsent(id, ignored -> new SentMap()).send(id, data, entry.getValue());
		}
	}

	private static ItemStack getMap(TileEntityShelvingUnit shelf, boolean upper) {
		ItemStack stack = shelf.getLevel(upper).getMediaItem(0);
		return stack != null && stack.getItem() == Items.FILLED_MAP ? stack : null;
	}

	private static final class WorldMaps {
		private final Set<TileEntityShelvingUnit> shelves = new HashSet<>();
		private final Map<Integer, SentMap> sent = new HashMap<>();
	}

	private static final class SentMap {
		private byte[] pixels;
		private Set<EntityPlayerMP> viewers = new HashSet<>();

		private void send(int id, MapData data, Set<EntityPlayerMP> watching) {
			SPacketMaps changed = this.changedRegion(id, data);
			SPacketMaps full = null;
			for (EntityPlayerMP player : watching) {
				if (!this.viewers.contains(player)) {
					if (full == null) {
						full = packet(id, data, 0, 0, MAP_SIZE, MAP_SIZE);
					}
					player.connection.sendPacket(full);
				} else if (changed != null) {
					player.connection.sendPacket(changed);
				}
			}
			this.viewers = watching;
		}

		private SPacketMaps changedRegion(int id, MapData data) {
			if (this.pixels == null) {
				this.pixels = data.colors.clone();
				return null;
			}

			int left = MAP_SIZE;
			int top = MAP_SIZE;
			int right = -1;
			int bottom = -1;
			for (int index = 0; index < this.pixels.length; index++) {
				if (this.pixels[index] != data.colors[index]) {
					int x = index % MAP_SIZE;
					int z = index / MAP_SIZE;
					left = Math.min(left, x);
					top = Math.min(top, z);
					right = Math.max(right, x);
					bottom = Math.max(bottom, z);
					this.pixels[index] = data.colors[index];
				}
			}

			return right >= left ? packet(id, data, left, top, right - left + 1, bottom - top + 1) : null;
		}

		private static SPacketMaps packet(int id, MapData data, int x, int z, int width, int height) {
			return new SPacketMaps(id, data.scale, data.trackingPosition, data.mapDecorations.values(),
				data.colors, x, z, width, height);
		}
	}
}
