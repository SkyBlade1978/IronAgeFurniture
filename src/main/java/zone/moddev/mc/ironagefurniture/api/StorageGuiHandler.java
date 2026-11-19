package zone.moddev.mc.ironagefurniture.api;

import java.util.function.Function;

import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerShelfMedia;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/** Storage screens share Forge's single GUI handler for the mod. */
public final class StorageGuiHandler implements IGuiHandler {
	public static final int BARREL = 0;
	private static final int LOWER_DISCS = 1;
	private static final int UPPER_DISCS = 2;
	private static final int LOWER_BOOKS = 3;
	private static final int UPPER_BOOKS = 4;
	private static final int LOWER_BOTTLES = 5;
	private static final int UPPER_BOTTLES = 6;

	private static Function<TileEntityBarrel, Object> barrelScreen;
	private static Function<ContainerShelfMedia, Object> mediaScreen;

	public static void setClientFactories(Function<TileEntityBarrel, Object> barrelFactory,
			Function<ContainerShelfMedia, Object> mediaFactory) {
		barrelScreen = barrelFactory;
		mediaScreen = mediaFactory;
	}

	public static int shelvingMediaId(ShelfContentKind kind, boolean upper) {
		if (kind == ShelfContentKind.BOTTLES) {
			return upper ? UPPER_BOTTLES : LOWER_BOTTLES;
		}

		if (kind == ShelfContentKind.BOOKS) {
			return upper ? UPPER_BOOKS : LOWER_BOOKS;
		}

		return upper ? UPPER_DISCS : LOWER_DISCS;
	}

	private static ShelfContentKind mediaKind(int id) {
		if (id == LOWER_BOTTLES || id == UPPER_BOTTLES) {
			return ShelfContentKind.BOTTLES;
		}

		if (id == LOWER_BOOKS || id == UPPER_BOOKS) {
			return ShelfContentKind.BOOKS;
		}

		return id == LOWER_DISCS || id == UPPER_DISCS ? ShelfContentKind.RECORDS : ShelfContentKind.NONE;
	}

	private TileEntityBarrel findBarrel(World world, BlockPos pos) {
		if (!world.isBlockLoaded(pos) || !(world.getBlockState(pos).getBlock() instanceof StorageBarrel)) {
			return null;
		}
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityBarrel ? (TileEntityBarrel)tile : null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if (id == BARREL) {
			TileEntityBarrel barrel = this.findBarrel(world, pos);
			return barrel != null && barrel.isUsableByPlayer(player) ? new ContainerBarrel(barrel) : null;
		}

		ShelfContentKind kind = mediaKind(id);
		if (kind != ShelfContentKind.NONE && world.isBlockLoaded(pos)
				&& world.getBlockState(pos).getBlock() instanceof ShelvingUnit) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityShelvingUnit) {
				boolean upper = id == UPPER_DISCS || id == UPPER_BOOKS || id == UPPER_BOTTLES;
				ContainerShelfMedia menu = new ContainerShelfMedia(world, pos, (TileEntityShelvingUnit)tile, upper, kind);
				return menu.canInteractWith(player) ? menu : null;
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == BARREL && barrelScreen != null) {
			TileEntityBarrel barrel = this.findBarrel(world, new BlockPos(x, y, z));
			return barrel == null ? null : barrelScreen.apply(barrel);
		}

		ShelfContentKind kind = mediaKind(id);
		if (kind != ShelfContentKind.NONE && mediaScreen != null) {
			return mediaScreen.apply(new ContainerShelfMedia(kind));
		}
		return null;
	}
}
