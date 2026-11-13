package zone.moddev.mc.ironagefurniture.api.Blocks;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.client.render.SurfaceDisplayRenderHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Loose items below the cell ceiling must be targetable through their owning furniture block. */
public final class SurfaceItemHit {
	private static final int ITEM_HIT = 100;
	private SurfaceItemHit() { }

	public static AxisAlignedBB bounds(SurfaceSettingHost host, Slot slot, boolean client) {
		AxisAlignedBB bounds = client ? SurfaceDisplayRenderHelper.getItemBounds(host.getSurfaceSetting(), slot)
			: host.getSurfaceSetting().getItemBounds(slot);
		return bounds == null ? null : bounds.offset(host.getSurfaceOffsetX(), host.getSurfaceHeight(), host.getSurfaceOffsetZ());
	}

	public static RayTraceResult trace(World world, BlockPos pos, Vec3d start, Vec3d end, RayTraceResult body) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof SurfaceSettingHost)) return body;
		SurfaceSettingHost host = (SurfaceSettingHost)tile;
		if (host.getSurfaceHeight() >= 1.0D) return body;
		RayTraceResult nearest = body;
		Vec3d localStart = start.subtract(new Vec3d(pos)), localEnd = end.subtract(new Vec3d(pos));
		for (Slot slot : Slot.values()) {
			if (host.getSurfaceSetting().getItem(slot) == null) continue;
			AxisAlignedBB bounds = bounds(host, slot, world.isRemote);
			RayTraceResult hit = bounds == null ? null : bounds.calculateIntercept(localStart, localEnd);
			if (hit == null) continue;
			Vec3d point = hit.hitVec.add(new Vec3d(pos));
			if (nearest == null || start.squareDistanceTo(point) < start.squareDistanceTo(nearest.hitVec)) {
				nearest = new RayTraceResult(point, hit.sideHit, pos);
				nearest.subHit = ITEM_HIT + slot.getIndex();
			}
		}
		return nearest;
	}

	public static boolean removeIfHit(World world, BlockPos pos, EntityPlayer player) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof SurfaceSettingHost) || ((SurfaceSettingHost)tile).getSurfaceHeight() >= 1.0D) return false;
		Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		double yaw = Math.toRadians(player.rotationYaw), pitch = Math.toRadians(player.rotationPitch);
		Vec3d end = eye.addVector(-Math.sin(yaw) * Math.cos(pitch) * 6.0D,
			-Math.sin(pitch) * 6.0D, Math.cos(yaw) * Math.cos(pitch) * 6.0D);
		IBlockState state = world.getBlockState(pos);
		RayTraceResult hit = state.collisionRayTrace(world, pos, eye, end);
		if (hit == null || hit.subHit < ITEM_HIT) return false;
		SurfaceSettingHost host = (SurfaceSettingHost)tile;
		ItemStack removed = host.getSurfaceSetting().remove(Slot.byIndex(hit.subHit - ITEM_HIT));
		host.markSurfaceSettingChanged();
		if (!world.isRemote && !player.capabilities.isCreativeMode && removed != null) Block.spawnAsEntity(world, pos, removed);
		if (!host.getSurfaceSetting().hasAnyItem()) SurfaceDisplayBlocker.release(world, pos);
		Block block = state.getBlock();
		if (block instanceof DiningTable) ((DiningTable)block).removeTableEntityIfEmpty(world, pos);
		if (block instanceof WallShelf) ((WallShelf)block).removeShelfEntityIfEmpty(world, pos);
		return true;
	}

	@SideOnly(Side.CLIENT)
	public static AxisAlignedBB selected(World world, BlockPos pos, AxisAlignedBB body) {
		RayTraceResult hit = net.minecraft.client.Minecraft.getMinecraft().objectMouseOver;
		TileEntity tile = world.getTileEntity(pos);
		if (hit != null && pos.equals(hit.getBlockPos()) && hit.subHit >= ITEM_HIT && tile instanceof SurfaceSettingHost) {
			AxisAlignedBB bounds = bounds((SurfaceSettingHost)tile, Slot.byIndex(hit.subHit - ITEM_HIT), true);
			if (bounds != null) return bounds.offset(pos);
		}
		return body;
	}
}
