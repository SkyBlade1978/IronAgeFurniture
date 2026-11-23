package zone.moddev.mc.ironagefurniture.api.tile;

import java.util.Arrays;

import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class TileEntityProduceFurniture extends TileEntityDisplayStorage {
	private final ProduceTray[] trays = new ProduceTray[3];
	private String wood;

	public String getWood() {
		return this.wood == null ? ProduceTray.DEFAULT_WOOD : this.wood;
	}

	public boolean isRackReady() {
		return this.wood != null;
	}

	public void setWood(String wood) {
		this.wood = wood;
		this.changed();
	}

	public ProduceTray getTray(int slot) {
		return this.trays[slot];
	}

	public void setTray(int slot, ProduceTray tray) {
		this.trays[slot] = tray;
		this.changed();
	}

	public void changed() {
		this.markDirty();
		if (this.world != null && !this.world.isRemote) {
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 2);
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		// A floor tray changes height when stacked, but both halves belong to the same tile.
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	protected boolean tryFeed(ItemStack stack, boolean simulate) {
		IBlockState state = this.world.getBlockState(this.pos);
		if (!(state.getBlock() instanceof ProduceFurniture)) {
			return false;
		}

		TileEntityProduceFurniture owner = ((ProduceFurniture)state.getBlock()).getOwner(this.world, this.pos, state);
		if (owner == null) {
			return false;
		}
		for (int slot = owner.trays.length - 1; slot >= 0; slot--) {
			ProduceTray tray = owner.trays[slot];
			if (tray != null && tray.offer(stack, simulate)) {
				if (!simulate) {
					owner.changed();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.wood = tag.hasKey(ProduceTray.WOOD_TAG, 8) ? tag.getString(ProduceTray.WOOD_TAG) : null;
		Arrays.fill(this.trays, null);
		NBTTagList entries = tag.getTagList("Trays", 10);
		for (int i = 0; i < entries.tagCount(); i++) {
			NBTTagCompound entry = entries.getCompoundTagAt(i);
			int slot = entry.getByte("Slot") & 255;
			if (slot < this.trays.length) {
				this.trays[slot] = ProduceTray.read(entry);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (this.wood != null) {
			tag.setString(ProduceTray.WOOD_TAG, this.wood);
		}
		NBTTagList entries = new NBTTagList();
		for (int slot = 0; slot < this.trays.length; slot++) {
			if (this.trays[slot] != null) {
				NBTTagCompound entry = this.trays[slot].write();
				entry.setByte("Slot", (byte)slot);
				entries.appendTag(entry);
			}
		}
		tag.setTag("Trays", entries);
		return tag;
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
		this.handleUpdateTag(packet.getNbtCompound());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(this.pos, this.pos.add(1, 2, 1));
	}
}
