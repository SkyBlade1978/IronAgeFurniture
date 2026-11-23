package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.ironagefurniture.client.gui.GuiFurnitureLabel;
import zone.moddev.mc.ironagefurniture.client.gui.ShelfLabelInputProbe;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class BarrelLabelClientProbe {
	private static final String TEXT = "Market potatoes";
	private static final EnumFacing FRONT = EnumFacing.SOUTH;
	private static BlockPos pos;
	private static Future<?> prepared;
	private static int step;
	private static int ticks;

	private BarrelLabelClientProbe() { }

	static void start(Minecraft client, BlockPos barrelPos) {
		pos = barrelPos;
		prepared = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			TileEntityBarrel barrel = (TileEntityBarrel)player.getServerWorld().getTileEntity(pos);
			require(barrel.getBulkStorage().deposit(new ItemStack(Items.POTATO, 42), false) == 42, "Missing bulk fixture");
			player.inventory.clear();
			player.inventory.currentItem = 0;
			player.setHeldItem(EnumHand.MAIN_HAND, sign(2));
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (pos == null || event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		try {
			require(++ticks < 240, "Barrel label probe stalled at step " + step);
			if (!prepared.isDone()) {
				return;
			}
			prepared.get();

			TileEntity tile = client.world.getTileEntity(pos);
			if (!(tile instanceof TileEntityBarrel)) return;
			TileEntityBarrel barrel = (TileEntityBarrel)tile;
			GuiFurnitureLabel editor = client.currentScreen instanceof GuiFurnitureLabel ? (GuiFurnitureLabel)client.currentScreen : null;
			ItemStack held = client.player.getHeldItemMainhand();

			switch (step) {
			case 0:
				if (!ItemStack.areItemStacksEqual(held, sign(2)) || barrel.getBulkStorage().getCount() != 42) return;
				click(client);
				break;
			case 1:
				if (editor == null) return;
				ShelfLabelInputProbe.type(editor, "Discard me", false);
				break;
			case 2:
				if (client.currentScreen != null) return;
				require(ItemStack.areItemStacksEqual(held, sign(2)) && barrel.getBarrelLabel().getSign() == null, "Cancel used a sign");
				click(client);
				break;
			case 3:
				if (editor == null) return;
				ShelfLabelInputProbe.type(editor, TEXT, true);
				break;
			case 4:
				if (client.currentScreen != null || !TEXT.equals(barrel.getBarrelLabel().getText())
						|| !ItemStack.areItemStacksEqual(held, sign(1))) return;
				require(barrel.getLabelFacing() == FRONT && ItemStack.areItemStacksEqual(barrel.getBarrelLabel().getSign(), sign(1)),
					"Sign data or face did not sync");
				client.player.inventory.currentItem = 1;
				break;
			case 5:
				if (held != null) return;
				click(client);
				break;
			case 6:
				if (editor == null) return;
				ShelfLabelInputProbe.type(editor, " 2", true);
				break;
			case 7:
				if (client.currentScreen != null || !(TEXT + " 2").equals(barrel.getBarrelLabel().getText())) return;
				require(ItemStack.areItemStacksEqual(client.player.inventory.getStackInSlot(0), sign(1))
					&& barrel.getBulkStorage().getCount() == 42, "Editing consumed a sign or barrel contents");
				BedsideCabinetClientProbe.start(client, pos.east(2));
				pos = null;
				LogManager.getLogger().info("IAF BARREL LABEL NETWORK PASSED: sign, cancel, exact placement and empty-hand editing");
				return;
			default:
				throw new IllegalStateException("Unexpected barrel label step " + step);
			}
			step++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static void click(Minecraft client) {
		Vec3d target = new Vec3d(pos).addVector(0.5D, 0.49D, 1);
		Vec3d look = target.subtract(client.player.getPositionEyes(1.0F));
		float yaw = (float)Math.toDegrees(Math.atan2(-look.xCoord, look.zCoord));
		float pitch = (float)-Math.toDegrees(Math.atan2(look.yCoord, Math.hypot(look.xCoord, look.zCoord)));
		client.player.rotationYaw = yaw;
		client.player.rotationPitch = pitch;
		client.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, client.player.onGround));
		require(client.playerController.processRightClickBlock(client.player, client.world, client.player.getHeldItemMainhand(),
			pos, FRONT, target, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS, "Barrel label click failed");
	}

	private static ItemStack sign(int count) {
		return new ItemStack(Items.SIGN, count).setStackDisplayName("Market sign");
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
