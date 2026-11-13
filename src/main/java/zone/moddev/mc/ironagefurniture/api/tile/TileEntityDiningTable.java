package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceReservationRepair;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityDiningTable extends TileEntity implements SurfaceSettingHost {
	private final SurfaceSetting surfaceSetting = new SurfaceSetting();
	private int blockedConnections;

	@Override
	public void onLoad() {
		super.onLoad();
		SurfaceReservationRepair.enqueue(this.world, this.pos);
	}

	public boolean hasDisplayedItem() {
		return this.surfaceSetting.hasAnyItem();
	}

	public boolean hasStoredData() {
		return this.hasDisplayedItem() || this.blockedConnections != 0;
	}

	@Override
	public SurfaceSetting getSurfaceSetting() {
		return this.surfaceSetting;
	}

	@Override
	public void markSurfaceSettingChanged() {
		this.markDirty();
		if (this.world != null && !this.world.isRemote) {
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 2);
		}
	}

	public boolean isConnectionBlocked(EnumFacing direction) {
		return direction.getAxis().isHorizontal()
			&& (this.blockedConnections & (1 << ((direction.getHorizontalIndex() + 2) % 4))) != 0;
	}

	public void setConnectionBlocked(EnumFacing direction, boolean blocked) {
		if (!direction.getAxis().isHorizontal() || this.isConnectionBlocked(direction) == blocked) return;
		this.blockedConnections ^= 1 << ((direction.getHorizontalIndex() + 2) % 4);
		this.markSurfaceSettingChanged();
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.surfaceSetting.readFromNBT(tag);
		this.blockedConnections = tag.getInteger("BlockedConnections") & 15;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		this.surfaceSetting.writeToNBT(tag);
		tag.setInteger("BlockedConnections", this.blockedConnections);
		return tag;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState != newState;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
		this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
	}
}
