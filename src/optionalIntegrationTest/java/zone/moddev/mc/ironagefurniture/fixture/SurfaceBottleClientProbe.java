package zone.moddev.mc.ironagefurniture.fixture;

import zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.client.render.SurfaceDisplayRenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;

final class SurfaceBottleClientProbe {
	static int verify(Minecraft client, BlockPos pos) {
		int cases=0;
		for(String id:Phase5BottleProbe.HOSTS) for(EnumFacing facing:EnumFacing.Plane.HORIZONTAL) for(ItemStack bottle:Phase5BottleProbe.samples()) {
			client.world.setBlockToAir(pos.up()); client.world.setBlockToAir(pos);
			Block block=Block.getBlockFromName("ironagefurniture:"+id);
			IBlockState state=block.getDefaultState();
			if(block instanceof Cabinet || block instanceof WallShelf) state=state.withProperty(BlockHorizontal.FACING,facing);
			if(block instanceof DiningTable) state=state.withProperty(DiningTable.DATA,true);
			if(block instanceof WallShelf) {
				client.world.setBlockState(pos.offset(facing.getOpposite()),Blocks.STONE.getDefaultState(),2);
				state=state.withProperty(WallShelf.DATA,true);
			}
			client.world.setBlockState(pos,state,2);
			SurfaceSettingHost host=(SurfaceSettingHost)client.world.getTileEntity(pos);
			host.getSurfaceSetting().setSingleItem(bottle,facing);
			client.world.setBlockState(pos.up(),Block.getBlockFromName("ironagefurniture:surface_display_blocker").getDefaultState(),2);
			AxisAlignedBB local=SurfaceDisplayRenderHelper.getItemBounds(host.getSurfaceSetting(),SurfaceSetting.Slot.CENTER);
			AxisAlignedBB expected=SurfaceContentRegistry.describe(bottle).getBounds(facing).offset(0.5D,0,0.5D);
			Phase5BottleProbe.require(Math.abs(local.minY-expected.minY)<0.000001D && Math.abs(local.maxY-expected.maxY)<0.000001D
				&& Math.abs(local.minX-expected.minX)<0.000001D && Math.abs(local.maxZ-expected.maxZ)<0.000001D
				&& local.maxY-local.minY>0.4D,"Bottle still uses flat-sprite bounds");
			AxisAlignedBB bounds=local.offset(pos.getX()+host.getSurfaceOffsetX(),pos.getY()+host.getSurfaceHeight(),pos.getZ()+host.getSurfaceOffsetZ());
			Vec3d centre=new Vec3d((bounds.minX+bounds.maxX)/2,(bounds.minY+bounds.maxY)/2,(bounds.minZ+bounds.maxZ)/2);
			for(EnumFacing side:new EnumFacing[]{EnumFacing.NORTH,EnumFacing.EAST,EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.UP}) {
				double reach=(side.getAxis()==EnumFacing.Axis.X?bounds.maxX-bounds.minX:side.getAxis()==EnumFacing.Axis.Y?bounds.maxY-bounds.minY:bounds.maxZ-bounds.minZ)/2+0.01D;
				RayTraceResult hit=client.world.rayTraceBlocks(centre.addVector(side.getFrontOffsetX()*reach,side.getFrontOffsetY()*reach,side.getFrontOffsetZ()*reach),centre);
				Phase5BottleProbe.require(hit!=null && (hit.getBlockPos().equals(pos)||hit.getBlockPos().equals(pos.up())) && hit.sideHit==side,"Bottle model cannot be targeted: "+id+"/"+side);
			}
			cases++;
		}
		client.world.setBlockToAir(pos.up()); client.world.setBlockToAir(pos);
		LogManager.getLogger().info("IAF SURFACE BOTTLE CLIENT PROBE PASSED: {} host/facing/bottle bounds and visible-face ray cases",cases);
		return cases;
	}
	static BlockPos scene(Minecraft client,BlockPos origin) {
		java.util.List<ItemStack> bottles=Phase5BottleProbe.samples();
		for(int x=-1;x<bottles.size()+1;x++) for(int z=-1;z<5;z++) {
			client.world.setBlockState(origin.add(x,-1,z),Blocks.STONEBRICK.getDefaultState(),2);
			for(int y=0;y<4;y++) client.world.setBlockToAir(origin.add(x,y,z));
			if(z==4) for(int y=0;y<3;y++) client.world.setBlockState(origin.add(x,y,z),Blocks.STONEBRICK.getDefaultState(),2);
		}
		for(int i=0;i<bottles.size();i++) for(boolean lower:new boolean[]{false,true}) {
			Block table=Block.getBlockFromName("ironagefurniture:"+(lower?"table_low_oak":"table_dining_oak"));
			BlockPos pos=origin.add(i,0,lower?0:2);
			client.world.setBlockState(pos,table.getDefaultState().withProperty(DiningTable.DATA,true),2);
			((SurfaceSettingHost)client.world.getTileEntity(pos)).getSurfaceSetting().setSingleItem(bottles.get(i),EnumFacing.NORTH);
		}
		for(int i=0;i<bottles.size();i++) {
			Block shelf=Block.getBlockFromName("ironagefurniture:shelf_wall_oak");
			BlockPos pos=origin.add(i,1,3);
			client.world.setBlockState(pos,shelf.getDefaultState().withProperty(WallShelf.DATA,true),2);
			((zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf)client.world.getTileEntity(pos))
				.addEmbeddedContent(WallShelf.ShelfContentKind.BOTTLES,bottles.get(i));
		}
		return origin;
	}
}
