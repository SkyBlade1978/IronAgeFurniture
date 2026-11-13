package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Defers display repair until loaded chunks are available through the world. */
public final class SurfaceReservationRepair {
	private static final Map<World, Set<BlockPos>> PENDING = new WeakHashMap<World, Set<BlockPos>>();

	public static void enqueue(World world, BlockPos pos) {
		if (world == null || world.isRemote) return;
		synchronized (PENDING) {
			Set<BlockPos> positions = PENDING.get(world);
			if (positions == null) {
				positions = new HashSet<BlockPos>();
				PENDING.put(world, positions);
			}
			positions.add(pos.toImmutable());
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.world.isRemote) return;
		Set<BlockPos> positions;
		synchronized (PENDING) {
			positions = PENDING.remove(event.world);
		}
		if (positions == null) return;
		for (BlockPos pos : positions) {
			if (event.world.isBlockLoaded(pos)) SurfaceDisplayBlocker.restoreReservation(event.world, pos);
		}
	}
}
