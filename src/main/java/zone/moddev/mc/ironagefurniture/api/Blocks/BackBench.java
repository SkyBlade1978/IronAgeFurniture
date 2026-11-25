package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.entity.Seat;
import zone.moddev.mc.skysfurniturecore.api.seating.BackBenchBlock;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BackBench extends BackBenchBlock {
	public BackBench(Material material, String name, float resistance, boolean tall, double seatHeight, float hardness) {
		super(material, name, resistance, tall, seatHeight, hardness);
		this.setCreativeTab(Ironagefurniture.ironagefurnitureTab);
	}

	@Override
	protected Seat createSeat(World world, BlockPos pos, double seatHeight) {
		return new Seat(world, pos.getX(), pos.getY(), pos.getZ(), seatHeight);
	}
}
