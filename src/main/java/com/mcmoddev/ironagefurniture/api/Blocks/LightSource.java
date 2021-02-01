package com.mcmoddev.ironagefurniture.api.Blocks;

import java.util.List;
import java.util.Random;

import com.mcmoddev.ironagefurniture.Ironagefurniture;
import com.mcmoddev.ironagefurniture.api.Enumerations.LightType;
import com.mcmoddev.ironagefurniture.api.Enumerations.Lit;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LightSource extends BlockHBase {
	private static final AxisAlignedBB TORCHSCONCEBB = new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.5, 0.9);
	
	public static final PropertyEnum<Lit> LIT = PropertyEnum.<Lit>create("lit", Lit.class);
	protected LightType _lightType = LightType.SCONCE;
	
	protected boolean ignite(World worldIn, BlockPos pos) {
		
		worldIn.checkLight(pos);
		return true; // todo return false if not lightable
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		// TODO Auto-generated method stub
		
		super.onBlockDestroyedByPlayer(worldIn, pos, state);
		worldIn.checkLight(pos);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (heldItem.getItem() == Items.FLINT_AND_STEEL) {
			if (this.ignite(worldIn, pos)) {
				// damage the flint and steel
			}
		}
		
		return true;
	}
	
	private void initLight(Material materialIn, String name, float resistance, float hardness, LightType lightType) {
		
		this.setTickRandomly(true);
		this._lightType = lightType;
		
		if (materialIn == Material.ROCK) {	
			this.setSoundType(SoundType.STONE);
			this.setHarvestLevel("pickaxe", 0);
		}
		
		if (materialIn == Material.WOOD) {	
			this.setSoundType(SoundType.WOOD);
			this.setHarvestLevel("axe", 0);
		}
		
		if (materialIn == Material.IRON) {	
			this.setSoundType(SoundType.METAL);
			this.setHarvestLevel("pickaxe", 1);
		}

		this.blockResistance = resistance;
		this.blockHardness = hardness;
		
		this.setCreativeTab(Ironagefurniture.ironagefurnitureTab);
	}
	
	public LightSource(Material materialIn, String name, float resistance, float hardness, LightType lightType) {
		super(materialIn);
		
		initLight(materialIn, name, resistance, hardness, lightType);
	}
	
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
		switch (this._lightType) {
		case SCONCE:
			EnumFacing enumfacing = (EnumFacing)stateIn.getValue(FACING);
	        double d0 = (double)pos.getX() + 0.5D;
	        double d1 = (double)pos.getY() + 0.7D;
	        double d2 = (double)pos.getZ() + 0.5D;
	        double d3 = 0.22D;
	        double d4 = 0.27D;

	        if (enumfacing.getAxis().isHorizontal())
	        {
	            EnumFacing enumfacing1 = enumfacing.getOpposite();
	            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - 0.1D * (double)enumfacing1.getFrontOffsetX(), d1 + 0.33D, d2 - 0.1D * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D, new int[0]);
	            worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 - 0.1D * (double)enumfacing1.getFrontOffsetX(), d1 + 0.33D, d2 - 0.1D * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D, new int[0]);
	        }
	        else
	        {
	            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
	            worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
	        }
	        
			break;

		default:
			break;
		}
    }
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {

		IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack)
				.withProperty(FACING, placer.getHorizontalFacing())
				.withProperty(LIT, Lit.LIT);
	
		world.checkLight(pos);
		
		return state;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		// TODO Auto-generated method stub
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		
		worldIn.checkLight(pos);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) 
	{
		Lit lit = Lit.LIT;
		
		if ( meta > 3 && meta < 8) {
			lit = Lit.UNLIT;
			meta -= 4;
		}
		
		return this.getDefaultState()
				.withProperty(LIT, lit)
				.withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int i = ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();

        if (state.getValue(LIT) == Lit.UNLIT)
            i += 4;
        
        return i;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { FACING, LIT });
	}
		
	@Override
	public boolean isFullCube(IBlockState bs) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState bs) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) 
	{
		//return BB;
		return TORCHSCONCEBB;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		
//		switch(state.getValue(FACING)) {
//		case NORTH:
//			super.addCollisionBoxToList(pos, entityBox, collidingBoxes, BACKNORTH);
//			break;
//		case SOUTH:
//			super.addCollisionBoxToList(pos, entityBox, collidingBoxes, BACKSOUTH);
//			break;
//		case WEST:
//			super.addCollisionBoxToList(pos, entityBox, collidingBoxes, BACKWEST);
//			break;
//		default:
//			super.addCollisionBoxToList(pos, entityBox, collidingBoxes, BACKEAST);
//			break;
//		}
//		
		super.addCollisionBoxToList(pos, entityBox, collidingBoxes, TORCHSCONCEBB);
	}
}