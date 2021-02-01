package com.mcmoddev.ironagefurniture.api.Blocks;

import java.util.List;
import com.mcmoddev.ironagefurniture.BlockObjectHolder;
import com.mcmoddev.ironagefurniture.Ironagefurniture;
import com.mcmoddev.ironagefurniture.api.Enumerations.LightType;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LightHolder extends BlockHBase {
	private static final AxisAlignedBB SCONCEBB = new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.5, 0.9);
	
	protected boolean _canFloor = false;
	protected boolean _canWall = false;
	protected boolean _canCeiling = false;
	
	protected LightType _lightType = LightType.SCONCE;
	
	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		// TODO Auto-generated method stub		
		super.onBlockDestroyedByPlayer(worldIn, pos, state);
		worldIn.checkLight(pos);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (heldItem.getUnlocalizedName().equals("tile.torch")) {
			
			switch (this._lightType) {
			case SCONCE:
				IBlockState newState = BlockObjectHolder.light_metal_ironage_sconce_wall_torch_iron.getDefaultState();
				
				worldIn.setBlockState(pos, newState);
			
				worldIn.checkLight(pos);
			
				heldItem.stackSize = heldItem.stackSize - 1;
				
				break;

			default:
				break;
			}
		}
		
		return true;
	}
	
	private void initLight(Material materialIn, String name, float resistance, float hardness) {
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
		this.lightValue = 0;
		
		this.setCreativeTab(Ironagefurniture.ironagefurnitureTab);
	}
	
	public LightHolder(Material materialIn, String name, float resistance, float hardness, LightType lightType) {
		super(materialIn);
		
		this._lightType = lightType;
		
		switch (this._lightType) {
		case SCONCE:
			this._canFloor = true;
			this._canWall = true;
			this._canCeiling = false;
			
			break;

		default:
			break;
		}
		
		initLight(materialIn, name, resistance, hardness);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {

		IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack)
				.withProperty(FACING, placer.getHorizontalFacing());
		
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
		return this.getDefaultState()
				.withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int i = ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
        
        return i;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { FACING });
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
		return SCONCEBB;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
	
		super.addCollisionBoxToList(pos, entityBox, collidingBoxes, SCONCEBB);
	}
}
