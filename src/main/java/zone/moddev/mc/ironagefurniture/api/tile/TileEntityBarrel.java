package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.skysfurniturecore.api.vessel.BarrelTile;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

/** Keeps the original Iron Age tile identity and bulk-storage rules. */
public class TileEntityBarrel extends BarrelTile {
	public TileEntityBarrel() { }

	protected TileEntityBarrel(int capacity) {
		super(capacity);
	}

	@Override
	public boolean isBulkBarrel() {
		if (this.isInvalid() || this.world == null || !this.world.isBlockLoaded(this.pos)
				|| this.world.getTileEntity(this.pos) != this) {
			return false;
		}

		Block block = this.world.getBlockState(this.pos).getBlock();
		return block instanceof StorageBarrel && ((StorageBarrel)block).supportsBulkStorage();
	}

	@Override
	public String getContainerNameKey() {
		return "container.ironagefurniture.barrel";
	}

	@Override
	public ResourceLocation getAgingVesselType() {
		return this.getClass() == TileEntityBarrel.class ? super.getAgingVesselType() : null;
	}
}
