package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBottleRack;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BottleRack extends RackBlock {

	public BottleRack(Material materialIn, String name, float resistance, float hardness) {
		super(materialIn, resistance, hardness);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			TileEntity tileEntity = worldIn.getTileEntity(pos);

			if (tileEntity instanceof TileEntityBottleRack) {
				((TileEntityBottleRack)tileEntity).dropBottles(worldIn, pos);
			}
		}

		super.breakBlock(worldIn, pos, state);
		this.notifyRackNeighbors(worldIn, pos);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (this.isRackItem(heldItem)) {
			if (this.tryPlaceRackFromRackClick(worldIn, pos, state, playerIn, hand, heldItem, side,
					hitX, hitY, hitZ)) {
				return true;
			}

			return false;
		}

		if (playerIn.isSneaking()) {
			return false;
		}

		int slot = this.getSlotForHit(state.getValue(FACING), hitX, hitY, hitZ);
		TileEntityBottleRack rack = this.getRackEntity(worldIn, pos, false);
		boolean hasHeldItem = heldItem != null && heldItem.stackSize > 0;

		if (hasHeldItem && !isValidBottleItem(heldItem)) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		}

		if (hasHeldItem) {
			if (rack != null && rack.hasBottle(slot)) {
				ItemStack stored = rack.getBottle(slot);

				if (this.isSameBottleStack(stored, heldItem)) {
					this.returnRackItem(worldIn, pos, playerIn, rack.removeBottle(slot));
					this.removeEmptyRackTile(worldIn, pos);
				}

				return true;
			}

			rack = this.getRackEntity(worldIn, pos, true);

			if (rack != null && rack.insertBottle(slot, heldItem,
					this.getBottleFacingForInsert(pos, state, playerIn))) {
				if (!playerIn.capabilities.isCreativeMode) {
					heldItem.stackSize--;

					if (heldItem.stackSize <= 0) {
						playerIn.setHeldItem(hand, null);
					}
				}
			}

			return true;
		}

		if (rack != null && rack.hasBottle(slot)) {
			this.returnRackItem(worldIn, pos, playerIn, rack.removeBottle(slot));
			this.removeEmptyRackTile(worldIn, pos);
		}

		return true;
	}

	private EnumFacing getBottleFacingForInsert(BlockPos pos, IBlockState state, EntityPlayer playerIn) {
		EnumFacing rackFacing = state.getValue(FACING);

		if (!state.getValue(STANDING).booleanValue() || playerIn == null) {
			return rackFacing;
		}

		EnumFacing playerSide = playerIn.getHorizontalFacing().getOpposite();

		if (playerSide == rackFacing || playerSide == rackFacing.getOpposite()) {
			return playerSide;
		}

		double playerOffsetX = playerIn.posX - (pos.getX() + 0.5D);
		double playerOffsetZ = playerIn.posZ - (pos.getZ() + 0.5D);
		double frontDistance = playerOffsetX * rackFacing.getFrontOffsetX()
			+ playerOffsetZ * rackFacing.getFrontOffsetZ();

		return frontDistance >= 0.0D ? rackFacing : rackFacing.getOpposite();
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return state.getValue(DATA).booleanValue() ? new TileEntityBottleRack() : null;
	}

	private int getSlotForHit(EnumFacing facing, float hitX, float hitY, float hitZ) {
		double localX = this.clamp(this.getLocalX(facing, hitX, hitZ), 0.0D, 0.999D);
		double localY = this.clamp(hitY, 0.0D, 0.999D);
		int column = (int)Math.floor(localX * 3.0D);
		int row = 2 - (int)Math.floor(localY * 3.0D);
		return row * 3 + column;
	}

	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private TileEntityBottleRack getRackEntity(World worldIn, BlockPos pos, boolean create) {
		return this.getRackTile(worldIn, pos, create, TileEntityBottleRack.class);
	}

	@Override
	protected void removeEmptyRackTile(World worldIn, BlockPos pos) {
		if (worldIn.isRemote) {
			return;
		}

		TileEntity tileEntity = worldIn.getTileEntity(pos);

		if (!(tileEntity instanceof TileEntityBottleRack)) {
			return;
		}

		if (((TileEntityBottleRack)tileEntity).hasStoredData()) {
			return;
		}

		this.clearRackTile(worldIn, pos);
	}

	private boolean isSameBottleStack(ItemStack storedItem, ItemStack heldItem) {
		return storedItem != null && heldItem != null && heldItem.stackSize > 0
			&& storedItem.isItemEqual(heldItem)
			&& ItemStack.areItemStackTagsEqual(storedItem, heldItem);
	}

	private void returnRackItem(World worldIn, BlockPos pos, EntityPlayer playerIn, ItemStack itemStack) {
		if (itemStack != null && itemStack.stackSize > 0
				&& !playerIn.inventory.addItemStackToInventory(itemStack)) {
			EntityItem entityItem = new EntityItem(worldIn, pos.getX() + 0.5D, pos.getY() + 0.5D,
				pos.getZ() + 0.5D, itemStack);
			worldIn.spawnEntity(entityItem);
		}
	}

	public static boolean isValidBottleItem(ItemStack stack) {
		if (stack == null || stack.stackSize <= 0) return false;
		Item item = stack.getItem();
		return item == Items.GLASS_BOTTLE || item == Items.POTIONITEM || item == Items.SPLASH_POTION
			|| item == Items.LINGERING_POTION || item == Items.EXPERIENCE_BOTTLE
			|| zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.classify(stack)
				== zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category.BOTTLE;
	}

	@Override
	public boolean isRackFamily(Block block) {
		return block instanceof BottleRack;
	}

	@Override
	protected double getRackDepth() {
		return 6.0D / 16.0D;
	}
}
