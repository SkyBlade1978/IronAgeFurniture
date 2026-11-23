package zone.moddev.mc.ironagefurniture.api;

import java.util.function.Function;

import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageCrate;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBulkBarrel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerCrate;
import zone.moddev.mc.ironagefurniture.api.container.ContainerFurnitureLabel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerShelfLabel;
import zone.moddev.mc.ironagefurniture.api.container.ContainerShelfMedia;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCrate;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

	private static final int LOWER_LABEL = 7;
	private static final int UPPER_OFFHAND_LABEL = LOWER_LABEL + 3;
	public static final int BULK_BARREL = UPPER_OFFHAND_LABEL + 1;
	private static final int BARREL_LABEL = BULK_BARREL + 1;
	private static final int LAST_BARREL_LABEL = BARREL_LABEL + 7;
	public static final int CRATE = LAST_BARREL_LABEL + 1;
	private static final int CRATE_LABEL = CRATE + 1;
	private static final int CRATE_OFFHAND_LABEL = CRATE_LABEL + 1;

	private static Function<TileEntityBarrel, Object> barrelScreen;
	private static Function<ContainerBulkBarrel, Object> bulkBarrelScreen;
	private static Function<ContainerShelfMedia, Object> mediaScreen;
	private static Function<ContainerFurnitureLabel, Object> labelScreen;
	private static Function<ContainerCrate, Object> crateScreen;

	public static void setClientFactories(Function<TileEntityBarrel, Object> barrelFactory,
			Function<ContainerShelfMedia, Object> mediaFactory,
			Function<ContainerFurnitureLabel, Object> labelFactory) {
		barrelScreen = barrelFactory;
		mediaScreen = mediaFactory;
		labelScreen = labelFactory;
	}

	public static void setBulkBarrelScreen(Function<ContainerBulkBarrel, Object> factory) {
		bulkBarrelScreen = factory;
	}

	public static void setCrateScreen(Function<ContainerCrate, Object> factory) {
		crateScreen = factory;
	}

	public static int shelvingLabelId(boolean upper, EnumHand hand) {
		return LOWER_LABEL + (upper ? 1 : 0) + (hand == EnumHand.OFF_HAND ? 2 : 0);
	}

	private static boolean isLabel(int id) {
		return id >= LOWER_LABEL && id <= UPPER_OFFHAND_LABEL || id >= BARREL_LABEL && id <= LAST_BARREL_LABEL
			|| id == CRATE_LABEL || id == CRATE_OFFHAND_LABEL;
	}

	public static int crateLabelId(EnumHand hand) {
		return hand == EnumHand.OFF_HAND ? CRATE_OFFHAND_LABEL : CRATE_LABEL;
	}

	public static int barrelLabelId(EnumFacing face, EnumHand hand) {
		return BARREL_LABEL + 2 * face.getHorizontalIndex() + (hand == EnumHand.OFF_HAND ? 1 : 0);
	}

	private ContainerFurnitureLabel findLabel(int id, World world, BlockPos pos) {
		if (!world.isBlockLoaded(pos)) {
			return null;
		}

		Block block = world.getBlockState(pos).getBlock();
		TileEntity tile = world.getTileEntity(pos);
		if (id == CRATE_LABEL || id == CRATE_OFFHAND_LABEL) {
			if (!(block instanceof StorageCrate) || !(tile instanceof TileEntityCrate)) {
				return null;
			}

			TileEntityCrate crate = (TileEntityCrate)tile;
			EnumHand hand = id == CRATE_LABEL ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			return new ContainerFurnitureLabel(world, pos, block, tile, crate.getCrateLabel(), hand, crate::writeLabel);
		}
		if (id >= BARREL_LABEL) {
			if (!(block instanceof StorageBarrel) || !(tile instanceof TileEntityBarrel)) {
				return null;
			}

			TileEntityBarrel barrel = (TileEntityBarrel)tile;
			int offset = id - BARREL_LABEL;
			EnumFacing face = EnumFacing.getHorizontal(offset / 2);
			EnumHand hand = (offset & 1) == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			return new ContainerFurnitureLabel(world, pos, block, tile, barrel.getBarrelLabel(), hand,
				(sign, text) -> barrel.writeLabel(face, sign, text));
		}

		if (!(block instanceof ShelvingUnit)) {
			return null;
		}

		int offset = id - LOWER_LABEL;
		return new ContainerShelfLabel(world, pos, (ShelvingUnit)block,
			tile instanceof TileEntityShelvingUnit ? (TileEntityShelvingUnit)tile : null,
			(offset & 1) != 0, offset >= 2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
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
		if (id == CRATE) {
			ContainerCrate menu = this.findCrateMenu(player, world, pos);
			return menu != null && menu.canInteractWith(player) ? menu : null;
		}
		if (isLabel(id)) {
			ContainerFurnitureLabel menu = this.findLabel(id, world, pos);
			return menu != null && menu.canInteractWith(player) ? menu : null;
		}

		if (id == BARREL) {
			TileEntityBarrel barrel = this.findBarrel(world, pos);
			return barrel != null && barrel.isUsableByPlayer(player) ? new ContainerBarrel(barrel) : null;
		}

		if (id == BULK_BARREL) {
			TileEntityBarrel barrel = this.findBarrel(world, pos);
			ContainerBulkBarrel menu = barrel == null ? null : new ContainerBulkBarrel(barrel);
			return menu != null && menu.canInteractWith(player) ? menu : null;
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
		if (id == CRATE && crateScreen != null) {
			ContainerCrate menu = this.findCrateMenu(player, world, new BlockPos(x, y, z));
			return menu == null ? null : crateScreen.apply(menu);
		}
		if (isLabel(id) && labelScreen != null) {
			ContainerFurnitureLabel menu = this.findLabel(id, world, new BlockPos(x, y, z));
			return menu == null ? null : labelScreen.apply(menu);
		}

		if (id == BARREL && barrelScreen != null) {
			TileEntityBarrel barrel = this.findBarrel(world, new BlockPos(x, y, z));
			return barrel == null ? null : barrelScreen.apply(barrel);
		}

		if (id == BULK_BARREL && bulkBarrelScreen != null) {
			TileEntityBarrel barrel = this.findBarrel(world, new BlockPos(x, y, z));
			return barrel == null ? null : bulkBarrelScreen.apply(new ContainerBulkBarrel(barrel));
		}

		ShelfContentKind kind = mediaKind(id);
		if (kind != ShelfContentKind.NONE && mediaScreen != null) {
			return mediaScreen.apply(new ContainerShelfMedia(kind));
		}
		return null;
	}

	private ContainerCrate findCrateMenu(EntityPlayer player, World world, BlockPos pos) {
		if (!world.isBlockLoaded(pos) || !(world.getBlockState(pos).getBlock() instanceof StorageCrate)) {
			return null;
		}
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityCrate ? new ContainerCrate((TileEntityCrate)tile, player) : null;
	}
}
