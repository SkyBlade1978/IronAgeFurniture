package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Enumerations.Rotation;
import zone.moddev.mc.ironagefurniture.api.entity.Seat;
import zone.moddev.mc.skysfurniturecore.api.seating.ChairBlock;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Chair extends ChairBlock {
	public Chair(Material material, String name, float resistance, double seatHeight, float hardness) {
		super(material, name, resistance, seatHeight, hardness);
		this.setCreativeTab(Ironagefurniture.ironagefurnitureTab);
	}

	public Chair(Material material, String name, float resistance, float hardness) {
		this(material, name, resistance, 0.3D, hardness);
	}

	@Override
	protected Seat createSeat(World world, BlockPos pos, double seatHeight) {
		return new Seat(world, pos.getX(), pos.getY(), pos.getZ(), seatHeight);
	}

	public static AxisAlignedBB RotateBB(Rotation rotation, AxisAlignedBB box) {
		return BlockHBase.RotateBB(rotation, box);
	}
}
