package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.skysfurniturecore.api.vessel.BarrelBlock;
import zone.moddev.mc.skysfurniturecore.api.vessel.BarrelTile;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Barrel extends BarrelBlock {
	public Barrel(Material material, String name, float resistance, float hardness) {
		super(material, name, resistance, hardness);
		this.setCreativeTab(Ironagefurniture.ironagefurnitureTab);
	}

	@Override
	protected final boolean trySpecialFluidInteraction(BarrelTile barrel, EntityPlayer player,
			EnumHand hand, ItemStack heldItem) {
		return barrel instanceof TileEntityBarrel
			&& this.trySpecialFluidInteraction((TileEntityBarrel)barrel, player, hand, heldItem);
	}

	protected boolean trySpecialFluidInteraction(TileEntityBarrel barrel, EntityPlayer player,
			EnumHand hand, ItemStack heldItem) {
		return false;
	}

	@Override
	protected TileEntityBarrel getBarrelEntity(IBlockAccess world, BlockPos pos) {
		BarrelTile barrel = super.getBarrelEntity(world, pos);
		return barrel instanceof TileEntityBarrel ? (TileEntityBarrel)barrel : null;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityBarrel();
	}
}
