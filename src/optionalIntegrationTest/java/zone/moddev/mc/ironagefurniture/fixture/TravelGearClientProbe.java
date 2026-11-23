package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.client.render.TravelInstrumentProbe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Follows a real client's first view, map update and pickup without using a held map for sync. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class TravelGearClientProbe {
	private static final int CHANGED_PIXEL = 35;
	private static final byte UPDATED_COLOUR = 18;
	private static BlockPos shelfPos;
	private static ItemStack map;
	private static Future<?> pending;
	private static int phase;
	private static int ticks;

	private TravelGearClientProbe() { }

	static void start(Minecraft client) {
		TravelInstrumentProbe.verify();
		shelfPos = client.player.getPosition().south(512);
		pending = client.getIntegratedServer().addScheduledTask(() -> prepare(client));
	}

	static ItemStack galleryMap() {
		return map.copy();
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (shelfPos == null || event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		try {
			require(++ticks < 300, "Travel gear probe timed out in phase " + phase
				+ " with held item " + client.player.getHeldItemMainhand());
			if (pending != null) {
				if (!pending.isDone()) return;
				pending.get();
				pending = null;
			}

			MapData data = (MapData)client.world.loadData(MapData.class, mapName());
			ItemStack held = client.player.getHeldItemMainhand();
			switch (phase) {
			case 0:
				if (ticks < 30) return;
				require(data == null, "A shelf map was sent before the player watched the chunk");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = player(client);
					WorldServer world = player.getServerWorld();
					require(!world.getPlayerChunkMap().isPlayerWatchingChunk(player, shelfPos.getX() >> 4, shelfPos.getZ() >> 4),
						"Map fixture was already in a watched chunk");
					BlockPos stand = shelfPos.north(2);
					world.setBlockState(stand.down(), Blocks.STONE.getDefaultState());
					player.connection.setPlayerLocation(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D, 0, 0);
				});
				break;
			case 1:
				if (data == null || !ItemStack.areItemStacksEqual(displayed(client), map)) return;
				require(held == null && data.colors[CHANGED_PIXEL] == colour(CHANGED_PIXEL)
					&& data.colors[16383] == colour(16383), "First view failed to receive the full map without holding it");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					MapData serverMap = (MapData)player(client).getServerWorld().loadData(MapData.class, mapName());
					serverMap.colors[CHANGED_PIXEL] = UPDATED_COLOUR;
					serverMap.updateMapData(CHANGED_PIXEL, 0);
				});
				break;
			case 2:
				if (data == null || data.colors[CHANGED_PIXEL] != UPDATED_COLOUR) return;
				require(data.colors[16383] == colour(16383), "A map update broke an unchanged pixel");
				require(client.playerController.processRightClickBlock(client.player, client.world, null,
					shelfPos, EnumFacing.NORTH, new Vec3d(shelfPos).addVector(0.5D, 0.22D, 0.25D), EnumHand.MAIN_HAND)
					== EnumActionResult.SUCCESS, "The displayed map could not be picked up");
				break;
			case 3:
				if (!ItemStack.areItemStacksEqual(held, map) || displayed(client) != null) return;
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = player(client);
					player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.MAP));
					player.sendContainerToPlayer(player.inventoryContainer);
				});
				break;
			case 4:
				if (held == null || held.getItem() != Items.MAP) return;
				client.playerController.processRightClick(client.player, client.world, held, EnumHand.MAIN_HAND);
				break;
			case 5:
				if (held == null || held.getItem() != Items.FILLED_MAP || held.getMetadata() == map.getMetadata()) return;
				shelfPos = null;
				LogManager.getLogger().info("IAF TRAVEL GEAR NETWORK PASSED: first map view, live pixel update, exact pickup and normal blank map use");
				TrophyShelfClientProbe.start(client);
				return;
			default:
				throw new IllegalStateException("Unexpected travel gear phase " + phase);
			}
			phase++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static void prepare(Minecraft client) {
		EntityPlayerMP player = player(client);
		WorldServer world = player.getServerWorld();
		int id = world.getUniqueDataId("map");
		map = new ItemStack(Items.FILLED_MAP, 1, id).setStackDisplayName("Coastal survey");
		MapData data = new MapData(mapName());
		data.dimension = (byte)world.provider.getDimension();
		for (int index = 0; index < data.colors.length; index++) {
			data.colors[index] = colour(index);
		}
		world.setData(mapName(), data);
		data.markDirty();

		Block block = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
		IBlockState state = block.getDefaultState().withProperty(RackBlock.STANDING, true).withProperty(RackBlock.DATA, true);
		world.setBlockState(shelfPos.down(), Blocks.STONE.getDefaultState());
		world.setBlockState(shelfPos, state);
		TileEntityShelvingUnit shelf = (TileEntityShelvingUnit)world.getTileEntity(shelfPos);
		require(shelf.getLevel(false).addMedia(map), "Could not display a filled map in the fixture");
		shelf.getLevel(false).markSurfaceSettingChanged();

		player.inventory.clear();
		player.sendContainerToPlayer(player.inventoryContainer);
	}

	private static byte colour(int index) {
		int x = index % 128;
		int z = index / 128;
		int shore = 36 + (z / 9 % 5) * 6 + (int)(Math.sin(z / 18.0D) * 13);
		if (x < shore) return (byte)(48 + (x + z) % 2);
		if (x < shore + 5) return 10;
		if (x > 88 && z > 18 && z < 67) return (byte)(34 + (x / 5 + z / 7) % 2);
		return (byte)(5 + (x / 6 + z / 5) % 2);
	}

	private static String mapName() {
		return "map_" + map.getMetadata();
	}

	private static ItemStack displayed(Minecraft client) {
		TileEntity tile = client.world.getTileEntity(shelfPos);
		return tile instanceof TileEntityShelvingUnit ? ((TileEntityShelvingUnit)tile).getLevel(false).getMediaItem(0) : null;
	}

	private static EntityPlayerMP player(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new IllegalStateException(message);
	}
}
