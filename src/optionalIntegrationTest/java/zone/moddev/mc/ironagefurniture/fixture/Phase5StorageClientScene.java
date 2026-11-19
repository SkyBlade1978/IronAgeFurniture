package zone.moddev.mc.ironagefurniture.fixture;

import zone.moddev.mc.ironagefurniture.api.Blocks.*;
import zone.moddev.mc.ironagefurniture.api.tile.*;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.*;

final class Phase5StorageClientScene {
	static BlockPos scene(Minecraft client, BlockPos origin) {
		for(int x=-1;x<10;x++) for(int z=-1;z<5;z++) {
			client.world.setBlockState(origin.add(x,-1,z),Blocks.STONEBRICK.getDefaultState(),2);
			for(int y=0;y<4;y++) client.world.setBlockToAir(origin.add(x,y,z));
		}
		for(int x=0;x<4;x++) for(int y=0;y<2;y++) {
			String prefix=x<2?"cabinet_wood_ironage_":"half_cabinet_wood_ironage_";
			Block cabinet=Block.getBlockFromName("ironagefurniture:"+prefix+"birch");
			client.world.setBlockState(origin.add(x,y,3),cabinet.getDefaultState().withProperty(Cabinet.FACING,EnumFacing.NORTH),2);
		}
		Block rack=Block.getBlockFromName("ironagefurniture:bottle_rack_wood_ironage_oak");
		BlockPos rackPos=origin.add(5,0,3);
		client.world.setBlockState(rackPos,rack.getDefaultState().withProperty(BottleRack.STANDING,true).withProperty(BottleRack.DATA,true),2);
		TileEntityBottleRack tile=(TileEntityBottleRack)client.world.getTileEntity(rackPos);
		for(int slot=0;slot<9;slot++) tile.insertBottle(slot,new ItemStack(slot%2==0?Items.POTIONITEM:Items.GLASS_BOTTLE),EnumFacing.NORTH);
		Block upright=Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak");
		client.world.setBlockState(origin.add(7,0,3),upright.getDefaultState(),2);
		TileEntityBarrel barrel=(TileEntityBarrel)client.world.getTileEntity(origin.add(7,0,3));
		barrel.getFluidHandler().fill(new FluidStack(FluidRegistry.WATER,11000),true);
		Block side=Block.getBlockFromName("ironagefurniture:side_barrel_wood_ironage_birch");
		client.world.setBlockState(origin.add(8,0,3),side.getDefaultState().withProperty(SideBarrel.FACING,EnumFacing.NORTH),2);
		return origin;
	}
	static void gui(Minecraft client,BlockPos origin) {
		TileEntityBarrel barrel=(TileEntityBarrel)client.world.getTileEntity(origin.add(7,0,3));
		String[] woods={"oak","spruce","birch","jungle","acacia","big_oak"};
		for(int i=0;i<woods.length;i++) client.player.inventory.setInventorySlotContents(9+i,
			new ItemStack(Block.getBlockFromName("ironagefurniture:bottle_rack_wood_ironage_"+woods[i])));
		client.gameSettings.hideGUI=false;
		client.displayGuiScreen(new zone.moddev.mc.ironagefurniture.client.gui.GuiBarrel(barrel));
	}
	static BlockPos rackJoins(Minecraft client, BlockPos origin) {
		for (int x=-1; x<9; x++) for (int z=0; z<8; z++) {
			client.world.setBlockState(origin.add(x,-1,z), Blocks.STONEBRICK.getDefaultState(), 2);
			for (int y=0; y<4; y++) client.world.setBlockToAir(origin.add(x,y,z));
		}
		for (boolean standing : new boolean[]{false,true}) {
			int z=standing?5:1;
			for (int x=0; x<7; x++) for (int y=0; y<2; y++) {
				if (x==2 || x==3 || x<2 && y==1) continue;
				if (!standing) client.world.setBlockState(origin.add(x,y,z+1), Blocks.STONEBRICK.getDefaultState(), 2);
				Block rack=Block.getBlockFromName("ironagefurniture:bottle_rack_wood_ironage_"+(x<2?"oak":"birch"));
				BlockPos pos=origin.add(x,y,z);
				client.world.setBlockState(pos,rack.getDefaultState().withProperty(BottleRack.STANDING,standing),2);
			}
		}
		return origin;
	}

	static BlockPos shelvingUnits(Minecraft client, BlockPos origin) {
		List<ItemStack> bottles = Phase5BottleProbe.samples();
		for (int x = -1; x <= 5; x++) {
			for (int z = 0; z <= 3; z++) {
				client.world.setBlockState(origin.add(x, -1, z), Blocks.STONEBRICK.getDefaultState(), 2);
			}
		}

		for (int x : new int[] { 0, 1, 3, 4 }) {
			boolean standing = x >= 3;
			String wood = standing ? "oak" : "birch";
			Block unit = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_" + wood);

			for (int y = 0; y < 2; y++) {
				BlockPos pos = origin.add(x, y, 2);

				if (!standing) {
					client.world.setBlockState(pos.south(), Blocks.STONEBRICK.getDefaultState(), 2);
				}

				client.world.setBlockState(pos, unit.getDefaultState()
					.withProperty(RackBlock.STANDING, standing).withProperty(RackBlock.DATA, true), 2);
				TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(pos);
				if (x == 0 || x == 3) {
					for (int index = 0; index < 5; index++) {
						tile.getLevel(false).addMedia(new ItemStack(Items.BOOK));
					}
					int discCount = y == 0 ? 4 : 10;
					for (int index = 0; index < discCount; index++) {
						tile.getLevel(true).addMedia(new ItemStack(Items.RECORD_CAT));
					}
				} else {
					tile.getLevel(false).getSurfaceSetting().setSingleItem(new ItemStack(Items.APPLE), EnumFacing.NORTH);
					int bottleCount = y == 0 ? 4 : 8;
					for (int index = 0; index < bottleCount; index++) {
						tile.getLevel(true).addMedia(bottles.get((index + y) % bottles.size()));
					}
				}
			}
		}

		return origin;
	}
}
