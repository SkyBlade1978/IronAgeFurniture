package zone.moddev.mc.ironagefurniture.api.tile;

import zone.moddev.mc.ironagefurniture.api.Blocks.BedsideCabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceReservationRepair;
import zone.moddev.mc.ironagefurniture.api.surface.FurnitureSurfaceDisplay;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityBedsideCabinet extends TileEntityDrawerStorage implements SurfaceSettingHost {
	private final SurfaceSetting surface = new SurfaceSetting();

	@Override
	public void onLoad() {
		super.onLoad();
		SurfaceReservationRepair.enqueue(this.world, this.pos);
	}

	@Override
	protected boolean isFront(EnumFacing facing) {
		IBlockState state = this.world == null ? null : this.world.getBlockState(this.pos);
		return facing != null && state != null && state.getBlock() instanceof BedsideCabinet
			&& state.getValue(BedsideCabinet.FACING) == facing;
	}

	@Override
	public SurfaceSetting getSurfaceSetting() {
		return this.surface;
	}

	@Override
	public double getSurfaceHeight() {
		return BedsideCabinet.TOP_HEIGHT;
	}

	public int getDisplayLight() {
		int light = 0;

		for (Slot slot : Slot.values()) {
			light = Math.max(light, FurnitureSurfaceDisplay.getLight(this.surface.getItem(slot)));
		}

		return light;
	}

	@Override
	public void markSurfaceSettingChanged() {
		this.markDirty();
		this.syncDisplay();

		if (this.world != null) {
			this.world.checkLight(this.pos);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		this.surface.writeToNBT(compound);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.surface.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		this.surface.writeToNBT(tag);
		return tag;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		this.surface.readFromNBT(tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		super.onDataPacket(net, packet);
		this.world.checkLight(this.pos);
	}
}
