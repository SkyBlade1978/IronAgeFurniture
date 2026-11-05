package zone.moddev.mc.ironagefurniture.api.Blocks;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper.MetalVariant;
import zone.moddev.mc.ironagefurniture.api.entity.EntityReleasedLavaLamp;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LightSourceSconceLavaWall extends LightSourceSconceGlowWall {
    public LightSourceSconceLavaWall(Material materialIn, String name, float resistance, float hardness) {
        super(materialIn, name, resistance, hardness);
    }

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		scheduleRelease(worldIn, pos);
	}

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        EnumFacing facing = state.getValue(FACING);
        BlockPos behind = pos.offset(facing.getOpposite());

        if (!worldIn.getBlockState(behind).isSideSolid(worldIn, behind, facing)) {
            if (!worldIn.isRemote) {
				spawnAsEntity(worldIn, pos, MetalVariantHelper.getDrop(
					BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron, worldIn, pos));
                IBlockState lavaState = LightDrop().getDefaultState().withProperty(FACING, facing);
                worldIn.setBlockState(pos, lavaState, 3);
                worldIn.scheduleUpdate(pos, LightDrop(), LightDrop().tickRate(worldIn));
            }

            return;
        }

        super.neighborChanged(state, worldIn, pos, blockIn);
		scheduleRelease(worldIn, pos);
    }

	private void scheduleRelease(World world, BlockPos pos) {
		if (!world.isRemote && world.getBlockState(pos).getBlock() == this
				&& canRelease(world.isBlockPowered(pos), world.isAirBlock(pos.down()))
				&& !world.isUpdateScheduled(pos, this)) {
			world.scheduleUpdate(pos, this, tickRate(world));
		}
	}

	static boolean canRelease(boolean powered, boolean airBelow) {
		return powered && airBelow;
	}

	@Override
	public int tickRate(World worldIn) {
		return 2;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (world.isRemote || world.getBlockState(pos).getBlock() != this) return;
		EnumFacing facing = state.getValue(FACING);
		BlockPos behind = pos.offset(facing.getOpposite());
		if (!world.getBlockState(behind).isSideSolid(world, behind, facing)) {
			neighborChanged(state, world, pos, world.getBlockState(behind).getBlock());
			return;
		}
		if (!canRelease(world.isBlockPowered(pos), world.isAirBlock(pos.down()))) return;

		MetalVariant metal = MetalVariantHelper.getMetal(world, pos);
		IBlockState emptyState = BlockObjectHolder.light_metal_ironage_sconce_wall_empty_iron
			.getDefaultState().withProperty(FACING, facing);
		if (!MetalVariantHelper.replaceBlockPreservingMetal(world, pos, emptyState)) return;

		EntityPlayer nearest = world.getClosestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D,
            pos.getZ() + 0.5D, -1.0D, false);
        if (!releaseLamp(world, pos, state, nearest != null && nearest.capabilities.isCreativeMode)
                && world.getBlockState(pos).getBlock() == emptyState.getBlock()) {
            world.setBlockState(pos, MetalVariantHelper.withMetal(state, metal), 3);
			MetalVariantHelper.setMetal(world, pos, metal);
		}
	}

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Lists.newArrayList(MetalVariantHelper.getDrop(
			BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron, world, pos));
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        MetalVariant metal = MetalVariantHelper.getMetal(world, pos);
        boolean destroyed = super.removedByPlayer(state, world, pos, player, willHarvest);
        if (destroyed && !world.isRemote && (player == null || !player.capabilities.isCreativeMode)
                && (player == null || !hasSilkTouch(player.getHeldItemMainhand()))) {
            // Keep the holder intact if another mod refuses the falling vial.
            if (!releaseLamp(world, pos, state, false)) {
                world.setBlockState(pos, MetalVariantHelper.withMetal(state, metal), 3);
                MetalVariantHelper.setMetal(world, pos, metal);
                return false;
            }
            if (!willHarvest) spawnAsEntity(world, pos, MetalVariantHelper.getDrop(
                    BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron, metal, 1));
        }
        return destroyed;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
            TileEntity tile, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, tile, tool);
        if (!world.isRemote && !player.capabilities.isCreativeMode && hasSilkTouch(tool))
            spawnAsEntity(world, pos, new ItemStack(LightDrop(), 1));
    }

    private boolean releaseLamp(World world, BlockPos pos, IBlockState state, boolean preserveOnLanding) {
        EnumFacing outwards = state.getValue(FACING).getOpposite();
        Block lamp = LightDrop();
        EntityReleasedLavaLamp falling = new EntityReleasedLavaLamp(world,
            pos.getX() + 0.5D + 0.27D * outwards.getFrontOffsetX(),
            pos.getY() + 5.0D / 16.0D,
            pos.getZ() + 0.5D + 0.27D * outwards.getFrontOffsetZ(),
            lamp.getDefaultState().withProperty(FACING, state.getValue(FACING)), pos.getY(), preserveOnLanding);
        if (!world.spawnEntity(falling)) return false;
        ((LightSourceLava)lamp).onStartFalling(falling);
        return true;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        BlockPos blockpos = pos.up();

        if (world.isAirBlock(blockpos) && !world.getBlockState(blockpos).isFullBlock()) {
            if (rand.nextInt(25) == 0) {
                EnumFacing facing = state.getValue(FACING);
                EnumFacing opposite = facing.getOpposite();
                double x = (double)pos.getX() + 0.5D + 0.27D * opposite.getFrontOffsetX();
                double y = (double)pos.getY() + 0.72D;
                double z = (double)pos.getZ() + 0.5D + 0.27D * opposite.getFrontOffsetZ();

                world.spawnParticle(EnumParticleTypes.LAVA, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
                world.playSound(x, y, z, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
                    0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }

            if (rand.nextInt(200) == 0) {
                world.playSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(),
                    SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS,
                    0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }
        }
    }

    private boolean hasSilkTouch(ItemStack stack) {
        return stack != null && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
    }

    @Override
    protected Block LightDrop() {
        return BlockObjectHolder.light_metal_ironage_block_floor_lava_clear;
    }

    @Override
    protected Block GetWallVariant() {
        return BlockObjectHolder.light_metal_ironage_sconce_wall_lava_iron;
    }

    @Override
    protected Block GetLavaVariant() {
        return BlockObjectHolder.light_metal_ironage_sconce_wall_lava_iron;
    }
}
