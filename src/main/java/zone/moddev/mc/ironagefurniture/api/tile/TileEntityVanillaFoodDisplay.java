package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** One exact food stack above an unmodified vanilla block; no ticking or chunk-load lookups. */
public final class TileEntityVanillaFoodDisplay extends TileEntity implements SurfaceSettingHost {
	private final SurfaceSetting setting = new SurfaceSetting();

	@Override
	public SurfaceSetting getSurfaceSetting() { return this.setting; }

	@Override
	public void markSurfaceSettingChanged() {
		this.markDirty();
		if (this.world != null && !this.world.isRemote) {
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 2);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.setting.readFromNBT(tag);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		this.setting.writeToNBT(tag);
		return tag;
	}

	@Override
	public NBTTagCompound getUpdateTag() { return this.writeToNBT(new NBTTagCompound()); }

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
		this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
	}
}
