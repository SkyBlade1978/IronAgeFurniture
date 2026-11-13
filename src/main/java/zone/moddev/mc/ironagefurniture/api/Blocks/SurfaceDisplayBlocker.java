package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.api.MineralogyCompat;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingInteraction;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingInteraction.Result;
import zone.moddev.mc.ironagefurniture.client.render.SurfaceDisplayRenderHelper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Reserves the cell above furniture and lets players target the displayed stack. */
public final class SurfaceDisplayBlocker extends Block {
	private static SurfaceDisplayBlocker registeredBlock;

	public SurfaceDisplayBlocker() {
		super(Material.CIRCUITS);
		this.setHardness(0.0F);
		registeredBlock = this;
	}

	static boolean placesAsBlock(ItemStack stack) {
		if (stack == null) return false;
		if (stack.getItem() == Items.FLOWER_POT || MineralogyCompat.isRockSaltLampItem(stack)) return true;
		if (!(stack.getItem() instanceof ItemBlock)) return false;
		Block block = ((ItemBlock)stack.getItem()).getBlock();
		return block == Blocks.HOPPER || block instanceof Cabinet || block instanceof DiningTable
			|| block instanceof Barrel || block instanceof LightSourceCandleFloor
			|| block instanceof LightSourceGlowdust || block instanceof LightSourceLava
			|| block instanceof LightSourceRed;
	}

	static boolean hasRoom(World world, BlockPos pos) {
		BlockPos above = pos.up();
		return above.getY() < world.getHeight()
			&& (world.isAirBlock(above) || world.getBlockState(above).getBlock() == registeredBlock);
	}

	static void restoreReservation(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (!world.isRemote && tile instanceof SurfaceSettingHost
				&& ((SurfaceSettingHost)tile).getSurfaceSetting().hasAnyItem()
				&& world.isAirBlock(pos.up()) && hasRoom(world, pos)) {
			world.setBlockState(pos.up(), registeredBlock.getDefaultState(), 2);
		}
	}

	static boolean interact(World world, BlockPos pos, SurfaceSettingHost host, EntityPlayer player,
			EnumHand hand, ItemStack held, float hitX, float hitZ) {
		if (host == null) return false;
		SurfaceSetting setting = host.getSurfaceSetting();
		if (held != null && held.stackSize > 0 && !setting.hasAnyItem()) {
			if (!hasRoom(world, pos)) return true;
			if (!world.isRemote && world.getBlockState(pos.up()).getBlock() != registeredBlock
					&& !world.setBlockState(pos.up(), registeredBlock.getDefaultState(), 2)) {
				return true;
			}
		}

		Result result = SurfaceSettingInteraction.handle(host, player, hand, held, hitX, hitZ, null, true);
		if (!setting.hasAnyItem()) release(world, pos);
		return result != Result.NOT_HANDLED;
	}

	static void release(World world, BlockPos pos) {
		BlockPos above = pos.up();
		if (!world.isRemote && world.getBlockState(above).getBlock() == registeredBlock) {
			world.setBlockState(above, Blocks.AIR.getDefaultState(), 2);
		}
	}

	static void dropItems(World world, BlockPos pos, SurfaceSettingHost host) {
		for (Slot slot : Slot.values()) {
			ItemStack displayed = host.getSurfaceSetting().remove(slot);
			if (displayed != null) Block.spawnAsEntity(world, pos, displayed);
		}
	}

	private SurfaceSettingHost getHost(IBlockAccess world, BlockPos pos) {
		Block owner = world.getBlockState(pos.down()).getBlock();
		if (!(owner instanceof Cabinet) && !(owner instanceof DiningTable)) return null;
		TileEntity tile = world.getTileEntity(pos.down());
		return tile instanceof SurfaceSettingHost ? (SurfaceSettingHost)tile : null;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
			EntityPlayer player, boolean willHarvest) {
		SurfaceSettingHost host = this.getHost(world, pos);
		if (host == null) return super.removedByPlayer(state, world, pos, player, willHarvest);
		SurfaceSetting setting = host.getSurfaceSetting();
		Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		double yaw = Math.toRadians(player.rotationYaw);
		double pitch = Math.toRadians(player.rotationPitch);
		double horizontalReach = 6.0D * Math.cos(pitch);
		Vec3d end = eye.addVector(-Math.sin(yaw) * horizontalReach,
			-Math.sin(pitch) * 6.0D, Math.cos(yaw) * horizontalReach);
		RayTraceResult hit = this.collisionRayTrace(state, world, pos, eye, end);
		Slot target = hit != null
			? setting.findTarget((float)(hit.hitVec.xCoord - pos.getX()),
				(float)(hit.hitVec.zCoord - pos.getZ())) : null;
		if (target == null && setting.getItemCount() == 1) {
			for (Slot slot : Slot.values()) {
				if (setting.getItem(slot) != null) target = slot;
			}
		}
		if (target == null) return false;
		ItemStack removed = setting.remove(target);
		host.markSurfaceSettingChanged();
		if (!world.isRemote && !player.capabilities.isCreativeMode) Block.spawnAsEntity(world, pos, removed);
		if (setting.hasAnyItem()) {
			world.notifyBlockUpdate(pos, state, state, 2);
			return true;
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		SurfaceSettingHost host = this.getHost(world, pos);
		if (host != null) {
			if (world.isRemote) {
				host.getSurfaceSetting().clear();
			} else {
				dropItems(world, pos, host);
			}
			host.markSurfaceSettingChanged();
			Block owner = world.getBlockState(pos.down()).getBlock();
			if (owner instanceof DiningTable) ((DiningTable)owner).removeTableEntityIfEmpty(world, pos.down());
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		SurfaceSettingHost host = this.getHost(world, pos);
		AxisAlignedBB bounds = host == null ? null : host.getSurfaceSetting().getCombinedBounds();
		return bounds == null ? FULL_BLOCK_AABB : bounds;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		SurfaceSettingHost host = this.getHost(world, pos);
		AxisAlignedBB combined = null;
		if (host != null) {
			SurfaceSetting setting = host.getSurfaceSetting();
			for (Slot slot : Slot.values()) {
				if (setting.getItem(slot) == null) continue;
				AxisAlignedBB bounds = SurfaceDisplayRenderHelper.getItemBounds(setting, slot);
				combined = combined == null ? bounds : combined.union(bounds);
			}
		}
		return (combined == null ? this.getBoundingBox(state, world, pos) : combined).offset(pos);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		SurfaceSettingHost host = this.getHost(world, pos);
		if (host == null) return null;
		SurfaceSetting setting = host.getSurfaceSetting();
		RayTraceResult nearest = null;
		for (Slot slot : Slot.values()) {
			if (setting.getItem(slot) == null) continue;
			AxisAlignedBB bounds = world.isRemote
				? SurfaceDisplayRenderHelper.getItemBounds(setting, slot) : setting.getItemBounds(slot);
			RayTraceResult hit = this.rayTrace(pos, start, end, bounds);
			if (hit != null && (nearest == null
					|| start.squareDistanceTo(hit.hitVec) < start.squareDistanceTo(nearest.hitVec))) {
				nearest = hit;
			}
		}
		return nearest;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
		if (world.isRemote) return;
		SurfaceSettingHost host = this.getHost(world, pos);
		if (host == null || !host.getSurfaceSetting().hasAnyItem()) release(world, pos.down());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) return false;
		BlockPos owner = pos.down();
		boolean handled = interact(world, owner, this.getHost(world, pos), player, hand, held, hitX, hitZ);
		Block block = world.getBlockState(owner).getBlock();
		if (block instanceof DiningTable) ((DiningTable)block).removeTableEntityIfEmpty(world, owner);
		return handled;
	}

	@Override
	public int quantityDropped(java.util.Random random) {
		return 0;
	}
}
