package zone.moddev.mc.ironagefurniture.api.container;

import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;

import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ContainerShelfLabel extends ContainerFurnitureLabel {
	public ContainerShelfLabel(World world, BlockPos pos, ShelvingUnit shelf, TileEntityShelvingUnit tile,
			boolean upper, EnumHand hand) {
		super(world, pos, shelf, tile, tile == null ? null : tile.getLevel(upper).getLabel(), hand,
			(sign, text) -> shelf.writeLabel(world, pos, upper, sign, text));
	}
}
