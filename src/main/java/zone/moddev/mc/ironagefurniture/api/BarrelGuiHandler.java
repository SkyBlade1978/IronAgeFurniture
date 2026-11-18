package zone.moddev.mc.ironagefurniture.api;

import java.util.function.Function;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/** Client factory is installed only by the client initialiser. */
public final class BarrelGuiHandler implements IGuiHandler {
	private static Function<TileEntityBarrel, Object> clientFactory;
	public static void setClientFactory(Function<TileEntityBarrel, Object> factory) { clientFactory = factory; }
	private TileEntityBarrel barrel(int id, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if (id != 0 || !world.isBlockLoaded(pos) || !(world.getBlockState(pos).getBlock() instanceof StorageBarrel)) return null;
		return world.getTileEntity(pos) instanceof TileEntityBarrel ? (TileEntityBarrel)world.getTileEntity(pos) : null;
	}
	@Override public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntityBarrel barrel = barrel(id, world, x, y, z);
		return barrel != null && barrel.isUsableByPlayer(player) ? new ContainerBarrel(barrel) : null;
	}
	@Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntityBarrel barrel = barrel(id, world, x, y, z);
		return barrel == null || clientFactory == null ? null : clientFactory.apply(barrel);
	}
}
