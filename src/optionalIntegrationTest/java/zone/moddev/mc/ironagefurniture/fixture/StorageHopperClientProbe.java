package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class StorageHopperClientProbe {
	private static BlockPos pos;
	private static Future<?> prepared;
	private static int ticks;

	private StorageHopperClientProbe() { }

	static void start(Minecraft client, BlockPos target) {
		pos = target;
		prepared = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			WorldServer world = player.getServerWorld();
			Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
			world.setBlockState(target.down(), Blocks.STONE.getDefaultState());
			world.setBlockState(target, shelf.getDefaultState().withProperty(RackBlock.STANDING, true));
			world.setBlockState(target.up(), Blocks.HOPPER.getDefaultState().withProperty(BlockHopper.FACING, EnumFacing.DOWN));
			TileEntityHopper hopper = (TileEntityHopper)world.getTileEntity(target.up());
			hopper.setInventorySlotContents(0, new ItemStack(Items.BREAD, 6).setStackDisplayName("Baker's delivery"));
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (pos == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();
		try {
			if (++ticks >= 240) throw new IllegalStateException("Hopper-fed items never synced to the visible shelf");
			if (!prepared.isDone()) return;
			prepared.get();
			TileEntity tile = client.world.getTileEntity(pos);
			if (!(tile instanceof TileEntityShelvingUnit)) return;
			TileEntityShelvingUnit shelf = (TileEntityShelvingUnit)tile;
			if (shelf.getLevel(true).getMediaCount() != 5 || shelf.getLevel(false).getMediaCount() != 1) return;
			if (!"Baker's delivery".equals(shelf.getLevel(false).getMediaItem(0).getDisplayName())) {
				throw new IllegalStateException("Hopper-fed shelf item lost its custom name");
			}
			client.playerController.setGameType(GameType.CREATIVE);
			client.getIntegratedServer().addScheduledTask(() ->
				serverPlayer(client).interactionManager.setGameType(GameType.CREATIVE));
			DrawerUnitClientProbe.start(client, pos.east(3));
			pos = null;
			LogManager.getLogger().info("IAF STORAGE HOPPER NETWORK PASSED: native hopper stocked both visible rows");
		} catch (Exception exception) {
			client.shutdown();
			throw exception;
		}
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}
}
