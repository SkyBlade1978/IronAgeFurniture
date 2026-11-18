package zone.moddev.mc.ironagefurniture.fixture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.apache.logging.log4j.LogManager;

final class Phase5BottleProbe {
	static final String[] HOSTS = {"table_dining_oak", "table_low_oak", "shelf_wall_oak",
		"cabinet_wood_ironage_oak", "half_cabinet_wood_ironage_oak"};
	static List<ItemStack> samples() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		items.add(new ItemStack(Items.GLASS_BOTTLE));
		items.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER));
		items.add(PotionUtils.appendEffects(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.HEALING),
			Collections.singletonList(new PotionEffect(MobEffects.SPEED, 600, 2))));
		items.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), PotionTypes.POISON));
		items.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), PotionTypes.SWIFTNESS));
		items.add(new ItemStack(Items.EXPERIENCE_BOTTLE));
		Item brewing = Item.getByNameOrId("ironagebrewing:fluid_bottle");
		if (brewing != null) for (int amount : new int[]{0,125,250}) {
			ItemStack bottle = new ItemStack(brewing);
			if (amount > 0) {
				net.minecraftforge.fluids.Fluid mead = FluidRegistry.getFluid("ironagefurniture_mead");
				require(mead != null, "Brewing mead fixture is missing");
				FluidStack contents = new FluidStack(mead, 250);
				contents.tag = new NBTTagCompound(); contents.tag.setString("BottleProof", "Exact provenance");
				require(FluidUtil.getFluidHandler(bottle).fill(contents, true) == 250, "Cannot prepare addon bottle");
				// The simple bottle handler fills atomically; also exercise a saved partial stack.
				if (amount < 250) bottle.getTagCompound().getCompoundTag(
					net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple.FLUID_NBT_KEY).setInteger("Amount", amount);
				FluidStack stored = FluidUtil.getFluidHandler(bottle).getTankProperties()[0].getContents();
				require(stored != null && stored.amount == amount, "Saved partial bottle was not read");
			}
			items.add(bottle);
		}
		for (int i=0; i<items.size(); i++) items.get(i).setStackDisplayName("Surface bottle " + i);
		return items;
	}
	static void verify(WorldServer world) {
		FakePlayer player = FakePlayerFactory.getMinecraft(world);
		GameType oldMode = player.interactionManager.getGameType();
		player.interactionManager.setGameType(GameType.SURVIVAL); player.setSneaking(false);
		BlockPos pos = world.getSpawnPoint().add(180, 8, 180);
		int cases = 0;
		try {
			for (String id : HOSTS) for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) for (ItemStack bottle : samples()) {
				player.inventory.clear(); world.setBlockToAir(pos.up()); world.setBlockToAir(pos);
				Block block = Block.getBlockFromName("ironagefurniture:" + id);
				world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
				IBlockState state = block.getDefaultState();
				if (block instanceof WallShelf || block instanceof Cabinet) state = state.withProperty(BlockHorizontal.FACING, facing);
				if (block instanceof WallShelf) world.setBlockState(pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
				world.setBlockState(pos, state);
				player.setHeldItem(EnumHand.MAIN_HAND, bottle.copy());
				SurfaceSettingHost existing = world.getTileEntity(pos) instanceof SurfaceSettingHost ? (SurfaceSettingHost)world.getTileEntity(pos) : null;
				float x=(float)(0.5D+(existing==null?0:existing.getSurfaceOffsetX())), z=(float)(0.5D+(existing==null?0:existing.getSurfaceOffsetZ()));
				require(block.onBlockActivated(world,pos,state,player,EnumHand.MAIN_HAND,player.getHeldItemMainhand(),EnumFacing.UP,x,1,z), "Bottle placement failed");
				SurfaceSettingHost host=(SurfaceSettingHost)world.getTileEntity(pos);
				require(player.getHeldItemMainhand()==null && ItemStack.areItemStacksEqual(bottle, stored(host)), "Bottle placement changed exact stack");
				require(SurfaceSetting.classify(bottle)==SurfaceSetting.Category.BOTTLE, "Bottle fell through to singleton");
				NBTTagCompound data=((TileEntity)host).writeToNBT(new NBTTagCompound());
				TileEntity loaded=TileEntity.create(world,data);
				require(loaded instanceof SurfaceSettingHost && ItemStack.areItemStacksEqual(bottle,stored((SurfaceSettingHost)loaded)), "Bottle save lost potion/fluid NBT");
				if (block instanceof WallShelf) {
					require(block.onBlockActivated(world,pos,world.getBlockState(pos),player,EnumHand.MAIN_HAND,null,EnumFacing.UP,x,1,z), "Shelf arrangement pickup failed");
				} else {
				AxisAlignedBB bounds=host.getSurfaceSetting().getCombinedBounds().offset(pos.getX()+host.getSurfaceOffsetX(),pos.getY()+host.getSurfaceHeight(),pos.getZ()+host.getSurfaceOffsetZ());
				Vec3d centre=new Vec3d((bounds.minX+bounds.maxX)/2,(bounds.minY+bounds.maxY)/2,(bounds.minZ+bounds.maxZ)/2);
				RayTraceResult hit=world.rayTraceBlocks(new Vec3d(centre.xCoord,centre.yCoord,bounds.minZ-0.01D),centre);
				require(hit!=null,"Cannot target bottle side"); BlockPos target=hit.getBlockPos(); IBlockState hitState=world.getBlockState(target);
				require(hitState.getBlock().onBlockActivated(world,target,hitState,player,EnumHand.MAIN_HAND,null,hit.sideHit,
					(float)(hit.hitVec.xCoord-target.getX()),(float)(hit.hitVec.yCoord-target.getY()),(float)(hit.hitVec.zCoord-target.getZ())), "Bottle side pickup failed");
				}
				require(stored(host)==null && world.getBlockState(pos).getBlock()==block, "Pickup retained a ghost or removed furniture");
				int returned=0; for (ItemStack stack : player.inventory.mainInventory) if (ItemStack.areItemStacksEqual(bottle,stack)) returned++;
				require(returned==1,"Bottle pickup lost or duplicated exact potion/fluid stack");
				world.setBlockToAir(pos); cases++;
			}
		} finally { player.inventory.clear(); player.interactionManager.setGameType(oldMode); }
		LogManager.getLogger().info("IAF SURFACE BOTTLE PROBE PASSED: {} host/facing/bottle cases, exact placement/save/pickup NBT",cases);
	}
	private static ItemStack stored(SurfaceSettingHost host) {
		if (host instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf
				&& ((zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf)host).hasEmbeddedContent())
			return ((zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf)host).getFirstEmbeddedItem();
		return host.getSurfaceSetting().getCompatibilityItem();
	}
	static void require(boolean pass,String message) { if(!pass) throw new IllegalStateException(message); }
}
