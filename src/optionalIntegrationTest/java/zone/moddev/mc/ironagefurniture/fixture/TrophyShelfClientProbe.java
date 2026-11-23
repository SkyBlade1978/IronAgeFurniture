package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Uses normal client packets for the placeable trophies in survival and creative. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class TrophyShelfClientProbe {
	private static BlockPos shelfPos;
	private static BlockPos plinth;
	private static ItemStack sample;
	private static Future<?> pending;
	private static int attempt;
	private static int phase;
	private static int ticks;

	private TrophyShelfClientProbe() { }

	static void start(Minecraft client) {
		BlockPos feet = client.player.getPosition();
		shelfPos = feet.west(2);
		plinth = feet.east(2).down();
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (shelfPos == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();

		try {
			require(++ticks < 360, "Trophy shelf probe timed out at " + attempt + "/" + phase);
			if (pending != null) {
				if (!pending.isDone()) return;
				pending.get();
				pending = null;
			}

			boolean skull = attempt < 2;
			boolean creative = (attempt & 1) != 0;
			ItemStack held = client.player.getHeldItemMainhand();

			switch (phase) {
			case 0:
				sample = skull ? TrophyShelfSamples.head(2) : TrophyShelfSamples.stand();
				prepare(client, creative ? GameType.CREATIVE : GameType.SURVIVAL);
				break;
			case 1:
				if (!ItemStack.areItemStacksEqual(held, sample)) return;
				click(client, held, shelfPos, EnumFacing.EAST, 0.22D);
				break;
			case 2:
				if (!ItemStack.areItemStacksEqual(displayed(client), sample)
						|| (creative ? !ItemStack.areItemStacksEqual(held, sample) : held != null)) return;
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = player(client);
					WorldServer world = player.getServerWorld();
					require(world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(shelfPos).expandXyz(1)).isEmpty(),
						"An entity spawned when storing a trophy");
					for (EnumFacing side : EnumFacing.values()) {
						require(world.getBlockState(shelfPos.offset(side)).getBlock() != Blocks.SKULL,
							"A skull block was placed when storing a trophy");
					}
					player.setHeldItem(EnumHand.MAIN_HAND, null);
					player.sendContainerToPlayer(player.inventoryContainer);
				});
				break;
			case 3:
				if (held != null) return;
				click(client, null, shelfPos, EnumFacing.EAST, 0.22D);
				break;
			case 4:
				if (!ItemStack.areItemStacksEqual(held, sample) || displayed(client) != null) return;
				click(client, held, plinth, EnumFacing.UP, 1.0D);
				break;
			case 5:
				BlockPos target = plinth.up();
				boolean placed = skull ? client.world.getBlockState(target).getBlock() == Blocks.SKULL
					: !client.world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(target).expandXyz(0.5D)).isEmpty();
				if (!placed) return;
				if (++attempt == 4) {
					shelfPos = null;
					LogManager.getLogger().info("IAF TROPHY NETWORK PASSED: exact pickup and normal skull and armour stand use in survival and creative");
					ShelfLabelClientProbe.start(client);
				} else {
					phase = 0;
				}
				return;
			default:
				throw new IllegalStateException("Unexpected trophy shelf phase " + phase);
			}
			phase++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static void prepare(Minecraft client, GameType mode) {
		client.playerController.setGameType(mode);
		pending = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = player(client);
			WorldServer world = player.getServerWorld();
			Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
			world.setBlockState(shelfPos.down(), Blocks.STONE.getDefaultState());
			world.setBlockState(shelfPos, shelf.getDefaultState().withProperty(RackBlock.STANDING, true));
			world.setBlockState(plinth, Blocks.STONE.getDefaultState());
			world.setBlockToAir(plinth.up());
			for (EntityArmorStand stand : world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(plinth.up()).expandXyz(0.5D))) {
				stand.setDead();
			}
			player.interactionManager.setGameType(mode);
			player.inventory.clear();
			player.setHeldItem(EnumHand.MAIN_HAND, sample.copy());
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}

	private static void click(Minecraft client, ItemStack held, BlockPos pos, EnumFacing side, double height) {
		require(client.playerController.processRightClickBlock(client.player, client.world, held, pos, side,
			new Vec3d(pos).addVector(0.5D, height, 0.5D), EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS,
			"Trophy click failed for " + sample + " at " + pos);
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
