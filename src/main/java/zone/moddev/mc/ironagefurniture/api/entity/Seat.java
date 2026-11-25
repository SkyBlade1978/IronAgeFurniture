package zone.moddev.mc.ironagefurniture.api.entity;

import zone.moddev.mc.ironagefurniture.api.util.Coordinates;
import zone.moddev.mc.skysfurniturecore.api.seating.FurnitureSeat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** The original registered Iron Age seat entity. */
public class Seat extends FurnitureSeat {
	public Coordinates SeatCoordinates = new Coordinates();

	public Seat(World world) {
		super(world);
	}

	public Seat(World world, double x, double y, double z, double seatHeight) {
		super(world, new BlockPos(x, y, z), seatHeight);
		this.SeatCoordinates = new Coordinates(x, y, z);
	}

	@Override
	protected BlockPos getAnchor() {
		return new BlockPos(this.SeatCoordinates.X, this.SeatCoordinates.Y, this.SeatCoordinates.Z);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		BlockPos anchor = super.getAnchor();
		this.SeatCoordinates = new Coordinates(anchor.getX(), anchor.getY(), anchor.getZ());
	}
}
