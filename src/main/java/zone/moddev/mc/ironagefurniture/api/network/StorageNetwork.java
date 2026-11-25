package zone.moddev.mc.ironagefurniture.api.network;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerFurnitureLabel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.skysfurniturecore.api.storage.FurnitureLabel;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class StorageNetwork {
	public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel("iaf_storage");

	private StorageNetwork() {
	}

	public static void init() {
		channel.registerMessage(Handler.class, Seal.class, 0, Side.SERVER);
		channel.registerMessage(EditLabelHandler.class, EditLabel.class, 1, Side.SERVER);
	}

	public static final class EditLabel implements IMessage {
		private int windowId;
		private String text;

		public EditLabel() {
		}

		public EditLabel(int windowId, String text) {
			this.windowId = windowId;
			this.text = text;
		}

		@Override
		public void fromBytes(ByteBuf buffer) {
			if (!buffer.isReadable()) {
				return;
			}

			this.windowId = buffer.readUnsignedByte();
			int length = buffer.readableBytes();

			if (length <= FurnitureLabel.MAX_LENGTH * 3) {
				this.text = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
			}
			buffer.skipBytes(length);
		}

		@Override
		public void toBytes(ByteBuf buffer) {
			buffer.writeByte(this.windowId);
			buffer.writeBytes(this.text.getBytes(StandardCharsets.UTF_8));
		}
	}

	public static final class EditLabelHandler implements IMessageHandler<EditLabel, IMessage> {
		@Override
		public IMessage onMessage(EditLabel message, MessageContext context) {
			EntityPlayerMP player = context.getServerHandler().playerEntity;
			player.getServerWorld().addScheduledTask(() -> {
				if (player.openContainer instanceof ContainerFurnitureLabel && player.openContainer.windowId == message.windowId) {
					if (message.text != null) {
						((ContainerFurnitureLabel)player.openContainer).save(player, message.text);
					}
					player.closeScreen();
				}
			});
			return null;
		}
	}

	public static final class Seal implements IMessage {
		private BlockPos pos;
		private boolean sealed;

		public Seal() {
		}

		public Seal(BlockPos pos, boolean sealed) {
			this.pos = pos;
			this.sealed = sealed;
		}

		@Override
		public void fromBytes(ByteBuf buffer) {
			this.pos = BlockPos.fromLong(buffer.readLong());
			this.sealed = buffer.readBoolean();
		}

		@Override
		public void toBytes(ByteBuf buffer) {
			buffer.writeLong(this.pos.toLong());
			buffer.writeBoolean(this.sealed);
		}
	}

	public static final class Handler implements IMessageHandler<Seal, IMessage> {
		@Override
		public IMessage onMessage(Seal message, MessageContext context) {
			EntityPlayerMP player = context.getServerHandler().playerEntity;
			player.getServerWorld().addScheduledTask(() -> apply(player, message.pos, message.sealed));
			return null;
		}
	}

	public static boolean apply(EntityPlayerMP player, BlockPos pos, boolean sealed) {
		WorldServer world = player.getServerWorld();

		if (pos == null || !world.isBlockLoaded(pos) || !(player.openContainer instanceof ContainerBarrel)) {
			return false;
		}

		TileEntity tile = world.getTileEntity(pos);

		if (!(tile instanceof TileEntityBarrel) || !(world.getBlockState(pos).getBlock() instanceof StorageBarrel)) {
			return false;
		}

		TileEntityBarrel barrel = (TileEntityBarrel)tile;
		ContainerBarrel container = (ContainerBarrel)player.openContainer;

		if (container.getBarrel() != barrel || !barrel.isUsableByPlayer(player) || !world.isBlockModifiable(player, pos)) {
			return false;
		}

		barrel.setSealed(sealed);
		barrel.markForFluidUpdate();
		return true;
	}
}
