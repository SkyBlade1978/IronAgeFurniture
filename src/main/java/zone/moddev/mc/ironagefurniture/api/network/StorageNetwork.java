package zone.moddev.mc.ironagefurniture.api.network;

import io.netty.buffer.ByteBuf;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.Side;

public final class StorageNetwork {
	public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel("iaf_storage");
	private StorageNetwork() { }
	public static void init() { channel.registerMessage(Handler.class, Seal.class, 0, Side.SERVER); }
	public static final class Seal implements IMessage {
		private BlockPos pos;
		private boolean sealed;
		public Seal() { }
		public Seal(BlockPos pos, boolean sealed) { this.pos = pos; this.sealed = sealed; }
		@Override public void fromBytes(ByteBuf buf) { pos = BlockPos.fromLong(buf.readLong()); sealed = buf.readBoolean(); }
		@Override public void toBytes(ByteBuf buf) { buf.writeLong(pos.toLong()); buf.writeBoolean(sealed); }
	}
	public static final class Handler implements IMessageHandler<Seal, IMessage> {
		@Override public IMessage onMessage(Seal message, MessageContext context) {
			EntityPlayerMP player = context.getServerHandler().playerEntity;
			player.getServerWorld().addScheduledTask(() -> apply(player, message.pos, message.sealed));
			return null;
		}
	}
	public static boolean apply(EntityPlayerMP player, BlockPos pos, boolean sealed) {
		if (pos == null || !player.getServerWorld().isBlockLoaded(pos)
				|| !(player.openContainer instanceof ContainerBarrel)) return false;
		TileEntity tile = player.getServerWorld().getTileEntity(pos);
		if (!(tile instanceof TileEntityBarrel) || !(player.getServerWorld().getBlockState(pos).getBlock() instanceof StorageBarrel)
				|| ((ContainerBarrel)player.openContainer).getBarrel() != tile || !((TileEntityBarrel)tile).isUsableByPlayer(player)
				|| !player.getServerWorld().isBlockModifiable(player, pos)) return false;
		((TileEntityBarrel)tile).setSealed(sealed);
		((TileEntityBarrel)tile).markForFluidUpdate();
		return true;
	}
}
