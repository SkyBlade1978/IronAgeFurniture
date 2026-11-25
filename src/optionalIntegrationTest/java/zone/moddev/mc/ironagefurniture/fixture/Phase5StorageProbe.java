package zone.moddev.mc.ironagefurniture.fixture;

import java.util.*;
import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.Blocks.*;
import zone.moddev.mc.ironagefurniture.api.container.ContainerBarrel;
import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.network.StorageNetwork;
import zone.moddev.mc.ironagefurniture.api.tile.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.oredict.*;
import org.apache.logging.log4j.LogManager;

final class Phase5StorageProbe {
	static void verify(WorldServer world, Set<String> mods) {
		List<String> woods = new ArrayList<>();
		for (String id : Ironagefurniture.BlockRegistry.keySet()) {
			require(!id.startsWith("foudre_") && !id.startsWith("pot_still_"), "IAF duplicated Brewing content");
			if (id.startsWith("chair_wood_ironage_classic_")) woods.add(id.substring("chair_wood_ironage_classic_".length()));
		}
		Collections.sort(woods);
		for (String wood : woods) recipes(world, wood);
		FakePlayer player = FakePlayerFactory.getMinecraft(world);
		NetHandlerPlayServer oldConnection = player.connection;
		GameType oldMode = player.interactionManager.getGameType();
		player.connection = new NetHandlerPlayServer(world.getMinecraftServer(), new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
			@Override public void sendPacket(Packet<?> packet) { }
		};
		try {
			player.inventory.clear(); player.setSneaking(false); player.interactionManager.setGameType(GameType.SURVIVAL);
			BlockPos origin = new BlockPos(64, 120, 240);
			int i = 0;
			for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
				halfCabinet(world, player, origin.east(i++ * 12), facing);
				rack(world, player, origin.south(12).east(i * 12), facing, false, mods);
				rack(world, player, origin.south(24).east(i * 12), facing, true, mods);
				barrel(world, player, origin.south(40).east(i * 12), facing, mods);
			}
			fluidOnlyBarrel(world, origin.south(44), mods.contains("ironagebrewing"));
			ShelvingUnitProbe.verify(world, player, origin.east(90));
			ShelvingPantryProbe.verify(world, player, origin.east(130));
			BulkBarrelProbe.verify(world, player, origin.south(58));
			BarrelLabelProbe.verify(world, player, origin.south(74));
			BedsideCabinetProbe.verify(world, player, origin.south(90));
			CrateProbe.verify(world, player, origin.south(106));
			StorageHopperProbe.verify(world, origin.south(120));
			DrawerUnitProbe.verify(world, player, origin.south(130).east(40));
			StorageBenchProbe.verify(world, player, origin.south(130).east(96));
			ToolRackProbe.verify(world, player, origin.south(190).east(96));
			ProduceFurnitureProbe.verify(world, player, origin.south(208).east(100));
			HarvestCraftDisplayProbe.verify(world, player, origin.south(224).east(100));
			persist(world, origin.south(65));
		} finally {
			player.inventory.clear(); player.interactionManager.setGameType(oldMode); player.connection = oldConnection;
		}
		LogManager.getLogger().info("IAF STORAGE PROBE PASSED: {} woods, all storage recipes, half cabinets, nine-slot racks and 16000 mB barrels", woods.size());
	}
	private static void recipes(WorldServer world, String wood) {
		Item chair = Item.getItemFromBlock(block("chair_wood_ironage_classic_" + wood));
		ItemStack plank = null;
		for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
			if (recipe.getRecipeOutput() == null || recipe.getRecipeOutput().getItem() != chair || !(recipe instanceof ShapedOreRecipe)) continue;
			for (Object input : ((ShapedOreRecipe)recipe).getInput()) if (input instanceof ItemStack) { plank = ((ItemStack)input).copy(); break; }
			if (plank != null) break;
		}
		require(plank != null, "Missing chair plank for " + wood);
		InventoryCrafting grid = grid();
		for (int i=0;i<3;i++) grid.setInventorySlotContents(i, plank.copy());
		ItemStack slab = craft(world, grid).copy(); slab.stackSize = 1;
		grid.clear();
		for (int slot : new int[] { 0, 2 }) grid.setInventorySlotContents(slot, plank.copy());
		for (int slot : new int[] { 3, 4, 5 }) grid.setInventorySlotContents(slot, slab.copy());
		ItemStack tray = craft(world, grid);
		output(tray, ProduceFurniture.TRAY_ID, 2);
		require(zone.moddev.mc.ironagefurniture.api.Items.ItemBlockProduceFurniture.wood(tray).equals(wood),
			"Tray recipe lost its wood for " + wood);
		grid.clear();
		for (int slot = 0; slot < 9; slot++) {
			grid.setInventorySlotContents(slot, slot % 3 == 1 ? plank.copy() : new ItemStack(Items.STICK));
		}
		ItemStack marketRack = craft(world, grid);
		output(marketRack, ProduceFurniture.RACK_ID, 1);
		require(zone.moddev.mc.ironagefurniture.api.Items.ItemBlockProduceFurniture.wood(marketRack).equals(wood),
			"Market rack recipe lost its wood for " + wood);
		grid.clear();
		grid.setInventorySlotContents(4, new ItemStack(block("cabinet_wood_ironage_" + wood)));
		output(craft(world, grid), "half_cabinet_wood_ironage_" + wood, 2);
		grid.clear();
		for (int slot : new int[]{0,2,6,8}) grid.setInventorySlotContents(slot, slab.copy());
		grid.setInventorySlotContents(4, new ItemStack(Items.STICK));
		output(craft(world, grid), "bottle_rack_wood_ironage_" + wood, 2);

		grid.clear();
		for (int slot : new int[] { 0, 2, 6, 8 }) {
			grid.setInventorySlotContents(slot, new ItemStack(Items.STICK));
		}
		for (int slot : new int[] { 3, 4, 5 }) {
			grid.setInventorySlotContents(slot, slab.copy());
		}
		output(craft(world, grid), "shelving_unit_wood_ironage_" + wood, 1);

		grid.clear();
		for (int slot : new int[] { 0, 1, 2, 3, 5, 6, 8 }) {
			grid.setInventorySlotContents(slot, plank.copy());
		}
		grid.setInventorySlotContents(4, new ItemStack(Items.STICK));
		grid.setInventorySlotContents(7, new ItemStack(Items.STICK));
		output(craft(world, grid), "bedside_cabinet_wood_ironage_" + wood, 1);

		grid.clear();
		for (int slot : new int[] { 0, 1, 2, 6, 7, 8 }) {
			grid.setInventorySlotContents(slot, plank.copy());
		}
		grid.setInventorySlotContents(3, new ItemStack(Items.STICK));
		grid.setInventorySlotContents(5, new ItemStack(Items.STICK));
		output(craft(world, grid), "crate_wood_ironage_" + wood, 1);

		grid.clear();
		for (int slot : new int[] { 0, 1, 2 }) {
			grid.setInventorySlotContents(slot, plank.copy());
		}
		grid.setInventorySlotContents(3, new ItemStack(Items.STICK));
		grid.setInventorySlotContents(5, new ItemStack(Items.STICK));
		output(craft(world, grid), "tool_rack_wood_ironage_" + wood, 2);

		grid.clear();
		for (int slot : new int[] { 0, 1, 2, 3, 5, 6, 7, 8 }) {
			grid.setInventorySlotContents(slot, plank.copy());
		}
		grid.setInventorySlotContents(4, new ItemStack(Items.STICK));
		output(craft(world, grid), "drawer_unit_wood_ironage_" + wood, 1);

		grid.setInventorySlotContents(3, null);
		grid.setInventorySlotContents(5, null);
		output(craft(world, grid), "half_drawer_unit_wood_ironage_" + wood, 1);

		grid.clear();
		grid.setInventorySlotContents(3, new ItemStack(block("chair_wood_ironage_bench_single_" + wood)));
		grid.setInventorySlotContents(4, new ItemStack(Blocks.CHEST));
		ItemStack bench = craft(world, grid);
		output(bench, "storage_bench_wood_ironage_" + wood, 1);
		require(bench.getMetadata() == 0, "Bench and chest must craft the plain bench");
		bench.setStackDisplayName("Named bench");
		grid.clear();
		grid.setInventorySlotContents(3, bench);
		for (PaddedBenchColour colour : PaddedBenchColour.values()) {
			grid.setInventorySlotContents(4, new ItemStack(Blocks.CARPET, 1, colour.getCarpetMetadata()));
			ItemStack padded = craft(world, grid);
			require(padded != null && padded.getItem() == bench.getItem()
				&& padded.getMetadata() == colour.getItemMetadata() + 1 && ItemStack.areItemStackTagsEqual(padded, bench),
				"Incorrect padding recipe for " + wood + " / " + colour);
			grid.setInventorySlotContents(3, padded);
		}

		grid.clear();
		for (int slot : new int[]{0,1,2,6,7,8}) grid.setInventorySlotContents(slot, plank.copy());
		grid.setInventorySlotContents(4, new ItemStack(Items.IRON_INGOT));
		ItemStack upright = craft(world, grid); output(upright, "barrel_wood_ironage_" + wood, 3);
		upright = upright.copy(); upright.stackSize = 1;
		NBTTagCompound data = new NBTTagCompound();
		FluidStack fluid = proofFluid(4321); data.setTag("Tank", fluid.writeToNBT(new NBTTagCompound())); data.setBoolean("Sealed", true);
		upright.setTagCompound(data.copy());
		grid.clear(); grid.setInventorySlotContents(4, upright);
		grid.setInventorySlotContents(0, new ItemStack(Items.STICK)); grid.setInventorySlotContents(8, new ItemStack(Items.STICK));
		ItemStack side = craft(world, grid); output(side, "side_barrel_wood_ironage_" + wood, 1);
		require(data.equals(side.getTagCompound()), "Side barrel recipe lost exact tank/seal NBT");
		grid.clear(); grid.setInventorySlotContents(1, side);
		ItemStack result = craft(world, grid); output(result, "barrel_wood_ironage_" + wood, 1);
		require(data.equals(result.getTagCompound()), "Upright conversion lost exact NBT");
		for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) if (recipe.matches(grid, world)) {
			ItemStack[] remaining = recipe.getRemainingItems(grid);
			require(remaining[1] != null && remaining[1].getItem() == Items.STICK && remaining[1].stackSize == 2, "Barrel conversion lost its supports"); break;
		}
	}
	private static void halfCabinet(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing facing) {
		Block cabinet = block("half_cabinet_wood_ironage_oak");
		world.setBlockState(pos, cabinet.getDefaultState().withProperty(Cabinet.FACING, facing));
		TileEntityHalfCabinet tile = (TileEntityHalfCabinet)world.getTileEntity(pos);
		// Use Minecraft types for inherited calls so Renamer can map them in the standalone probe.
		IInventory inventory = tile;
		TileEntity tileData = tile;
		require(inventory.getSizeInventory() == 18, "Half cabinet must have 18 slots");
		AxisAlignedBB box = cabinet.getBoundingBox(world.getBlockState(pos), world, pos);
		require(Math.abs((box.maxX-box.minX)*(box.maxZ-box.minZ)-0.5D) < .00001, "Half cabinet collision has full depth");
		require(Math.abs(tile.getSurfaceOffsetX()+facing.getFrontOffsetX()*.25D)<.00001
			&& Math.abs(tile.getSurfaceOffsetZ()+facing.getFrontOffsetZ()*.25D)<.00001, "Half cabinet surface is not centred on physical top");
		ItemStack stored = new ItemStack(Items.DIAMOND, 7).setStackDisplayName("Half cabinet contents " + facing);
		inventory.setInventorySlotContents(17, stored.copy());
		ItemStack meal = new ItemStack(Items.BREAD).setStackDisplayName("Half cabinet display " + facing);
		click(world, player, pos, meal.copy(), EnumFacing.UP, (float)(.5+tile.getSurfaceOffsetX()),1,(float)(.5+tile.getSurfaceOffsetZ()));
		require(ItemStack.areItemStacksEqual(tile.getSurfaceSetting().getCompatibilityItem(),meal), "Half cabinet display failed");
		NBTTagCompound nbt = tileData.writeToNBT(new NBTTagCompound());
		tileData.readFromNBT(nbt);
		require(nbt.equals(tileData.writeToNBT(new NBTTagCompound())), "Half cabinet save changed contents");
		BlockPos pair = pos.offset(facing.rotateY());
		world.setBlockState(pair, cabinet.getDefaultState().withProperty(Cabinet.FACING,facing));
		cabinet.onBlockPlacedBy(world,pair,world.getBlockState(pair),player,new ItemStack(cabinet));
		player.setPosition(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
		click(world,player,pos,null,facing,.5F,.5F,.5F);
		require(player.openContainer.inventorySlots.size()==72, "Paired half cabinets must open 36 storage slots");
		player.closeScreen(); world.setBlockToAir(pair); world.setBlockToAir(pos);
		require(count(world,pos,stored)==7 && count(world,pos,meal)==1, "Half cabinet drop lost or duplicated exact contents");
		clearDrops(world,pos);
	}
	private static void rack(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing facing, boolean standing, Set<String> mods) {
		Block rack = block("bottle_rack_wood_ironage_oak");
		world.setBlockState(standing?pos.down():pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
		world.setBlockState(pos,rack.getDefaultState().withProperty(net.minecraft.block.BlockHorizontal.FACING,facing).withProperty(BottleRack.STANDING,standing));
		ItemStack[] bottles = new ItemStack[9];
		for(int slot=0;slot<9;slot++) {
			bottles[slot]=new ItemStack(Items.POTIONITEM).setStackDisplayName("Rack bottle " + slot + " " + pos);
			float local=(slot%3+.5F)/3F, y=1-(slot/3+.5F)/3F;
			float x=.5F+(local-.5F)*facing.rotateY().getFrontOffsetX();
			float z=.5F+(local-.5F)*facing.rotateY().getFrontOffsetZ();
			click(world,player,pos,bottles[slot].copy(),facing,x,y,z);
		}
		TileEntityBottleRack tile=(TileEntityBottleRack)world.getTileEntity(pos);
		for(int slot=0;slot<9;slot++) require(ItemStack.areItemStacksEqual(tile.getBottle(slot),bottles[slot]),"Rack grid stored the wrong bottle in a slot");
		TileEntity tileData = tile;
		NBTTagCompound data = tileData.writeToNBT(new NBTTagCompound());
		tileData.readFromNBT(data);
		require(data.equals(tileData.writeToNBT(new NBTTagCompound())), "Rack changed bottle NBT/orientations on load");
		require(!BottleRack.isValidBottleItem(new ItemStack(Items.APPLE)),"Rack accepted arbitrary food");
		if(mods.contains("ironagebrewing")) require(BottleRack.isValidBottleItem(new ItemStack(Item.getByNameOrId("ironagebrewing:fluid_bottle"))),"Rack did not accept addon bottle provider");
		world.setBlockToAir(standing?pos.down():pos.offset(facing.getOpposite()));
		require(world.isAirBlock(pos),"Unsupported rack survived");
		for(ItemStack bottle:bottles) require(count(world,pos,bottle)==1,"Rack support loss duplicated/lost exact bottle");
		clearDrops(world,pos);
	}
	private static void barrel(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing facing, Set<String> mods) {
		Block block=block("side_barrel_wood_ironage_oak");
		world.setBlockState(pos,block.getDefaultState().withProperty(SideBarrel.FACING,facing));
		TileEntityBarrel tile=(TileEntityBarrel)world.getTileEntity(pos);
		require(tile.getCapacity()==16000,"Ordinary barrel capacity changed");
		FluidStack original=proofFluid(4321);
		require(tile.getFluidHandler().fill(original,true)==4321,"Barrel rejected fluid");
		player.setPosition(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
		// Forge skips openGui for FakePlayer; the client probe checks real FML GUI packets.
		player.openContainer = (Container)new StorageGuiHandler().getServerGuiElement(StorageGuiHandler.BARREL,
			player, world, pos.getX(), pos.getY(), pos.getZ());
		require(player.openContainer instanceof ContainerBarrel && StorageNetwork.apply(player,pos,true),"Barrel seal GUI failed");
		require(tile.getFluidHandler().drain(1000,true)==null && tile.getFluidHandler().fill(proofFluid(1000),true)==0,"Sealed barrel accepted a transfer");
		player.setPosition(pos.getX()+20,pos.getY(),pos.getZ());
		require(!StorageNetwork.apply(player,pos,false) && tile.isSealed(),"Remote seal request accepted");
		player.setPosition(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
		require(StorageNetwork.apply(player,pos,false),"Barrel did not unseal"); player.closeScreen();
		if(mods.contains("ironagebrewing")) {
			player.inventory.clear();
			int serving = FluidUtil.getFluidHandler(new ItemStack(Item.getByNameOrId("ironagebrewing:fluid_bottle"))).getTankProperties()[0].getCapacity();
			click(world,player,pos,new ItemStack(Items.GLASS_BOTTLE),facing,.5F,.5F,.5F);
			require(tile.getFluidAmount()==4321-serving,"Brewing bottle did not drain exactly its declared capacity from ordinary barrel");
			ItemStack filled=null;
			for(ItemStack stack:player.inventory.mainInventory) if(stack!=null && stack.getItem()==Item.getByNameOrId("ironagebrewing:fluid_bottle")) filled=stack.copy();
			require(filled!=null && FluidUtil.getFluidContained(filled).tag.equals(original.tag),"Brewing bottle lost fluid provenance");
			click(world,player,pos,filled,facing,.5F,.5F,.5F);
			require(tile.getFluidAmount()==4321,"Brewing bottle did not return its exact fluid amount");
		}
		tile.setSealed(true); ItemStack expected=new ItemStack(block); tile.writeToItemStack(expected);
		player.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(Items.DIAMOND_AXE));
		require(player.interactionManager.tryHarvestBlock(pos),"Native barrel mining failed");
		require(count(world,pos,expected)==1,"Barrel mining lost fluid/seal NBT"); clearDrops(world,pos);
		require(Item.getItemFromBlock(block).getItemStackLimit(expected)==1,"Filled barrels must not stack");
	}

	private static void fluidOnlyBarrel(WorldServer world, BlockPos pos, boolean brewingLoaded) {
		Block block = block("side_barrel_wood_ironage_oak");
		world.setBlockState(pos, block.getDefaultState());
		TileEntityBarrel tile = (TileEntityBarrel)world.getTileEntity(pos);
		Fluid fluid = brewingLoaded ? FluidRegistry.getFluid("ironagefurniture_mead") : FluidRegistry.WATER;
		require(fluid != null && tile.getFluidHandler().fill(new FluidStack(fluid, 4000), true) == 4000,
			"Fluid barrel fixture fill");
		require(!tile.isBatchProcessingEnabled(), "The ordinary barrel defaults to batch processing");

		ItemStack[] savedStacks = {
			new ItemStack(Items.POTATO, 2).setStackDisplayName("Saved ingredient"),
			new ItemStack(Items.BREAD, 3).setStackDisplayName("Saved result")
		};
		NBTTagList items = new NBTTagList();

		for (int slot = 0; slot < savedStacks.length; slot++) {
			NBTTagCompound stack = savedStacks[slot].writeToNBT(new NBTTagCompound());
			stack.setByte("Slot", (byte)slot);
			items.appendTag(stack);
		}

		NBTTagCompound batch = new NBTTagCompound();
		batch.setString("Recipe", "missingaddon:saved_batch");
		batch.setInteger("Elapsed", 12);
		batch.setTag("Items", items);
		NBTTagCompound saved = tile.serializeNBT();
		saved.setTag("BarrelBatch", batch);
		saved.setBoolean("Sealed", true);
		tile.readFromNBT(saved);
		NBTTagCompound savedBatch = tile.serializeNBT().getCompoundTag("BarrelBatch");

		require(tile.isSealed() && tile.getBatchStatus().isEmpty()
			&& !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)
			&& !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN),
			"Inactive saved batch exposed processing or item ports");
		int previousProgress = tile.getAgeProgress();
		tile.update();
		require(tile.getAgeProgress() == previousProgress + (brewingLoaded ? 1 : 0)
			&& (tile.getAgeProgressTotal() > 0) == brewingLoaded && tile.getFluidAmount() == 4000,
			"Ordinary barrel did not preserve its fluid ageing behavior");
		require(savedBatch.getInteger("Elapsed") == 12 && items.equals(savedBatch.getTagList("Items", 10))
			&& savedBatch.equals(tile.serializeNBT().getCompoundTag("BarrelBatch")), "Inactive batch changed saved data");

		tile.setSealed(false);
		ItemStack dropped = new ItemStack(block);
		tile.writeToItemStack(dropped);
		require(items.equals(dropped.getTagCompound().getCompoundTag("BarrelBatch").getTagList("Items", 10)),
			"Opening or packing an inactive batch lost its saved items");
	}

	private static void persist(WorldServer world,BlockPos pos) {
		Block half=block("half_cabinet_wood_ironage_oak"); world.setBlockState(pos,half.getDefaultState());
		TileEntityHalfCabinet cabinet = (TileEntityHalfCabinet)world.getTileEntity(pos);
		IInventory inventory = cabinet;
		inventory.setInventorySlotContents(17, new ItemStack(Items.EMERALD, 11).setStackDisplayName("Saved half cabinet"));
		cabinet.getSurfaceSetting().setSingleItem(new ItemStack(Items.BREAD),EnumFacing.SOUTH);
		pos=pos.east(4); world.setBlockState(pos,block("barrel_wood_ironage_oak").getDefaultState());
		TileEntityBarrel barrel=(TileEntityBarrel)world.getTileEntity(pos); barrel.getFluidHandler().fill(proofFluid(12345),true); barrel.setSealed(true);
		pos=pos.east(4); world.setBlockState(pos.down(),Blocks.STONE.getDefaultState());
		world.setBlockState(pos,block("bottle_rack_wood_ironage_oak").getDefaultState().withProperty(BottleRack.STANDING,true).withProperty(BottleRack.DATA,true));
		TileEntityBottleRack rack=(TileEntityBottleRack)world.getTileEntity(pos);
		for(int slot=0;slot<9;slot++) rack.insertBottle(slot,new ItemStack(Items.POTIONITEM).setStackDisplayName("Saved potion " + slot),EnumFacing.NORTH);
		pos = pos.east(4);
		world.setBlockState(pos, block("barrel_wood_ironage_oak").getDefaultState());
		TileEntityBarrel bulk = (TileEntityBarrel)world.getTileEntity(pos);
		require(bulk.getBulkStorage().deposit(new ItemStack(Items.WHEAT_SEEDS, 37).setStackDisplayName("Saved grain"), false)
			== 37, "Could not prepare saved solid barrel fixture");
	}
	private static FluidStack proofFluid(int amount) { FluidStack stack=new FluidStack(FluidRegistry.WATER,amount); stack.tag=new NBTTagCompound(); stack.tag.setString("StorageProof","exact batch data"); return stack; }
	private static void click(WorldServer world,FakePlayer player,BlockPos pos,ItemStack stack,EnumFacing side,float x,float y,float z) {
		player.setHeldItem(EnumHand.MAIN_HAND,stack); IBlockState state=world.getBlockState(pos);
		require(state.getBlock().onBlockActivated(world,pos,state,player,EnumHand.MAIN_HAND,stack,side,x,y,z),"Storage activation failed: " + state);
	}
	private static int count(WorldServer world,BlockPos pos,ItemStack expected) { int n=0; for(EntityItem item:world.getEntitiesWithinAABB(EntityItem.class,new AxisAlignedBB(pos.add(-2,-2,-2),pos.add(3,3,3)))) if(!item.isDead && ItemStack.areItemsEqual(expected,item.getEntityItem()) && ItemStack.areItemStackTagsEqual(expected,item.getEntityItem())) n+=item.getEntityItem().stackSize; return n; }
	private static void clearDrops(WorldServer world,BlockPos pos) { for(EntityItem item:world.getEntitiesWithinAABB(EntityItem.class,new AxisAlignedBB(pos.add(-2,-2,-2),pos.add(3,3,3)))) item.setDead(); }
	private static InventoryCrafting grid() { return new InventoryCrafting(new Container(){@Override public boolean canInteractWith(EntityPlayer p){return true;}},3,3); }
	private static ItemStack craft(WorldServer world,InventoryCrafting grid) { ItemStack stack=CraftingManager.getInstance().findMatchingRecipe(grid,world); require(stack!=null,"Missing storage recipe"); return stack; }
	private static void output(ItemStack stack,String name,int count){require(stack.getItem()==Item.getItemFromBlock(block(name)) && stack.stackSize==count,"Wrong storage recipe output " + name + ": " + stack);}
	private static Block block(String name){Block block=Block.getBlockFromName("ironagefurniture:"+name);require(block!=null && block!=Blocks.AIR,"Missing storage block " + name);return block;}
	private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
