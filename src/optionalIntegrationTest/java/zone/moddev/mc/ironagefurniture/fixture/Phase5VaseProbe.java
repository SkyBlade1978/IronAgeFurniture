package zone.moddev.mc.ironagefurniture.fixture;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.VasePlantHelper;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityGlassVase;
import zone.moddev.mc.ironagefurniture.init.FurnitureCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
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
import org.apache.logging.log4j.LogManager;

final class Phase5VaseProbe {
	static void verify(WorldServer world) {
		Block vase = Block.getBlockFromName("ironagefurniture:ornament_glass_vase");
		require(vase != null && vase != Blocks.AIR, "Glass vases were not registered");
		TileEntityGlassVase prototype = new TileEntityGlassVase();
		prototype.setPlant(new ItemStack(Blocks.RED_FLOWER, 1, 2).setStackDisplayName("Legacy vase plant"));
		NBTTagCompound legacy = ((TileEntity)prototype).writeToNBT(new NBTTagCompound());
		require("ironagefurniture:ornament_glass_vase".equals(legacy.getString("id")), "Donor vase save identity changed");
		legacy.setString("id", "ironagefurniture:glass_vase");
		TileEntity recovered = TileEntity.create(world, legacy);
		require(recovered instanceof TileEntityGlassVase && ItemStack.areItemStacksEqual(prototype.getPlant(),
			((TileEntityGlassVase)recovered).getPlant()), "Prototype vase identity lost its plant");
		FakePlayer player = FakePlayerFactory.getMinecraft(world);
		// Forge's Creative break path sends a block correction even for a fake player.
		player.connection = new net.minecraft.network.NetHandlerPlayServer(world.getMinecraftServer(),
			new net.minecraft.network.NetworkManager(net.minecraft.network.EnumPacketDirection.SERVERBOUND), player) {
			@Override public void sendPacket(net.minecraft.network.Packet<?> packet) { }
		};
		player.interactionManager.setGameType(GameType.SURVIVAL);
		BlockPos pos = world.getSpawnPoint().add(120, 4, 120);
		for (int colour = 0; colour < 16; colour++) {
			InventoryCrafting grid = new InventoryCrafting(new Container() {
				@Override public boolean canInteractWith(EntityPlayer p) { return true; }
			}, 3, 3);
			for (int slot : new int[]{1,3,5,7}) grid.setInventorySlotContents(slot, new ItemStack(Blocks.STAINED_GLASS, 1, colour));
			ItemStack crafted = CraftingManager.getInstance().findMatchingRecipe(grid, world);
			require(crafted != null && crafted.getItem() == Item.getItemFromBlock(vase)
				&& crafted.getMetadata() == colour && crafted.stackSize == 4, "Incorrect vase recipe " + colour);
			ItemStack flower = new ItemStack(Blocks.RED_FLOWER, 1, colour % 9).setStackDisplayName("Vase flower " + colour);
			world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
			world.setBlockState(pos, vase.getStateFromMeta(colour));
			click(world, player, pos, flower.copy());
			require(ItemStack.areItemStacksEqual(((TileEntityGlassVase)world.getTileEntity(pos)).getPlant(), flower), "Placed vase lost plant data");
			player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
			require(player.interactionManager.tryHarvestBlock(pos), "Native vase mining failed");
			require(count(world, pos, new ItemStack(vase, 1, colour)) == 1 && count(world, pos, flower) == 1, "Placed vase mining lost or duplicated contents");
			clearDrops(world, pos);
			for (String id : new String[]{"table_dining_oak", "cabinet_wood_ironage_oak", "half_cabinet_wood_ironage_oak", "table_low_oak", "shelf_wall_oak"}) {
				for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
					world.setBlockToAir(pos.up()); world.setBlockToAir(pos);
					Block hostBlock = Block.getBlockFromName("ironagefurniture:" + id);
					IBlockState state = hostBlock.getDefaultState();
					if (hostBlock instanceof zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet)
						state = state.withProperty(net.minecraft.block.BlockHorizontal.FACING, facing);
					if (hostBlock instanceof WallShelf) {
						world.setBlockState(pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState());
						state = state.withProperty(net.minecraft.block.BlockHorizontal.FACING, facing);
					}
					world.setBlockState(pos, state);
					ItemStack displayed = new ItemStack(vase, 1, colour).setStackDisplayName("Display vase " + colour);
					click(world, player, pos, displayed.copy());
					verifySideClicks(world, player, pos, displayed, flower);
					click(world, player, pos, flower.copy());
					SurfaceSettingHost host = (SurfaceSettingHost)world.getTileEntity(pos);
					ItemStack stored = host.getSurfaceSetting().getCompatibilityItem();
					require(stored.getMetadata() == colour && ItemStack.areItemStacksEqual(VasePlantHelper.getPlant(stored), flower), "Surface vase lost colour/plant");
					TileEntity tile = (TileEntity)host;
					NBTTagCompound data = tile.writeToNBT(new NBTTagCompound());
					tile.readFromNBT(data);
					require(data.equals(tile.writeToNBT(new NBTTagCompound())), "Vase surface NBT round-trip changed data");
					click(world, player, pos, null);
					require(!VasePlantHelper.hasPlant(host.getSurfaceSetting().getCompatibilityItem()), "Empty-hand click did not remove plant first");
					click(world, player, pos, flower.copy());
					stored = host.getSurfaceSetting().getCompatibilityItem();
					require(VasePlantHelper.hasPlant(stored), "Plant could not be reinserted");
					int expected = 1;
					if (facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) {
						boolean creative = facing == EnumFacing.WEST;
						player.interactionManager.setGameType(creative ? GameType.CREATIVE : GameType.SURVIVAL);
						player.setPositionAndRotation(pos.getX() + 0.5D + host.getSurfaceOffsetX(), pos.getY() + 2.0D,
							pos.getZ() + 0.5D + host.getSurfaceOffsetZ(), 0, 90);
						player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
						require(player.interactionManager.tryHarvestBlock(host.getSurfaceHeight() < 1 ? pos : pos.up()), "Native displayed vase mining failed");
						require(world.getBlockState(pos).getBlock() == hostBlock && !host.getSurfaceSetting().hasAnyItem(), "Vase mining removed furniture or retained a ghost");
						expected = creative ? 0 : 1;
						player.interactionManager.setGameType(GameType.SURVIVAL);
					}
					world.setBlockToAir(pos);
					require(count(world, pos, displayed) == expected && count(world, pos, flower) == expected,
						"Surface owner break did not split exact vase and plant: " + id + "/" + facing
						+ " vase=" + count(world, pos, displayed) + " plant=" + count(world, pos, flower));
					clearDrops(world, pos);
				}
			}
			pos = pos.east(4);
		}
		// Leave both placed and displayed planted vases for real disk reload tests.
		world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
		world.setBlockState(pos, vase.getStateFromMeta(10));
		click(world, player, pos, new ItemStack(Blocks.RED_FLOWER, 1, 2).setStackDisplayName("Saved vase flower"));
		BlockPos shelfPos = pos.east(4);
		world.setBlockState(shelfPos.south(), Blocks.STONE.getDefaultState());
		Block shelf = Block.getBlockFromName("ironagefurniture:shelf_wall_oak");
		world.setBlockState(shelfPos, shelf.getDefaultState());
		click(world, player, shelfPos, new ItemStack(vase, 1, 11));
		click(world, player, shelfPos, new ItemStack(Blocks.YELLOW_FLOWER).setStackDisplayName("Saved shelf flower"));
		LogManager.getLogger().info("IAF GLASS VASE PROBE PASSED: 16 recipes, placed mining and 320 surface/facing cases with four-sided ray-hit plant insertion, removal and vase retrieval");
	}
	private static void verifySideClicks(WorldServer world, FakePlayer player, BlockPos pos, ItemStack vase, ItemStack flower) {
		for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
			rayClick(world, player, pos, side, flower.copy());
			SurfaceSettingHost host = (SurfaceSettingHost)world.getTileEntity(pos);
			require(ItemStack.areItemStacksEqual(flower, VasePlantHelper.getPlant(host.getSurfaceSetting().getCompatibilityItem())),
				"Side hit did not insert exact plant: " + side + "/" + world.getBlockState(pos));
			player.inventory.clear();
			rayClick(world, player, pos, side, null);
			require(!VasePlantHelper.hasPlant(host.getSurfaceSetting().getCompatibilityItem()) && inventoryCount(player, flower) == 1,
				"Side hit did not return exactly one plant: " + side);
			player.inventory.clear();
			rayClick(world, player, pos, side, null);
			require(!host.getSurfaceSetting().hasAnyItem() && inventoryCount(player, vase) == 1,
				"Side hit did not return exactly one vase: " + side);
			player.inventory.clear();
			click(world, player, pos, vase.copy());
		}
	}
	private static void rayClick(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing side, ItemStack held) {
		SurfaceSettingHost host = (SurfaceSettingHost)world.getTileEntity(pos);
		AxisAlignedBB bounds = zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceItemHit.bounds(host,
			zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot.CENTER, false).offset(pos);
		Vec3d centre = new Vec3d((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2, (bounds.minZ + bounds.maxZ) / 2);
		double reach = (side.getAxis() == EnumFacing.Axis.X ? bounds.maxX - bounds.minX : bounds.maxZ - bounds.minZ) / 2 + 0.01D;
		RayTraceResult hit = world.rayTraceBlocks(centre.addVector(side.getFrontOffsetX() * reach, 0, side.getFrontOffsetZ() * reach), centre);
		require(hit != null, "No side ray hit for vase: " + side);
		BlockPos target = hit.getBlockPos();
		require(target.equals(pos) || target.equals(pos.up()), "Vase ray hit another block");
		IBlockState state = world.getBlockState(target);
		player.setHeldItem(EnumHand.MAIN_HAND, held);
		require(state.getBlock().onBlockActivated(world, target, state, player, EnumHand.MAIN_HAND, held, hit.sideHit,
			(float)(hit.hitVec.xCoord - target.getX()), (float)(hit.hitVec.yCoord - target.getY()), (float)(hit.hitVec.zCoord - target.getZ())),
			"Ray-hit activation failed: " + side);
	}
	private static int inventoryCount(EntityPlayer player, ItemStack expected) {
		int count = 0;
		for (ItemStack item : player.inventory.mainInventory)
			if (item != null && ItemStack.areItemsEqual(expected, item) && ItemStack.areItemStackTagsEqual(expected, item)) count += item.stackSize;
		return count;
	}
	private static void click(WorldServer world, FakePlayer player, BlockPos pos, ItemStack held) {
		player.setHeldItem(EnumHand.MAIN_HAND, held);
		IBlockState state = world.getBlockState(pos);
		SurfaceSettingHost host = world.getTileEntity(pos) instanceof SurfaceSettingHost ? (SurfaceSettingHost)world.getTileEntity(pos) : null;
		float x = (float)(0.5D + (host == null ? 0 : host.getSurfaceOffsetX()));
		float z = (float)(0.5D + (host == null ? 0 : host.getSurfaceOffsetZ()));
		EnumFacing side = host != null && host.getSurfaceHeight() < 1 && host.getSurfaceSetting().hasAnyItem()
			? EnumFacing.NORTH : EnumFacing.UP;
		require(state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, held, side, x, 1, z), "Vase activation failed: " + state);
	}
	private static int count(WorldServer world, BlockPos pos, ItemStack expected) {
		int count = 0;
		for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.add(-2,-2,-2), pos.add(3,3,3))))
			if (!item.isDead && ItemStack.areItemsEqual(expected, item.getEntityItem()) && ItemStack.areItemStackTagsEqual(expected, item.getEntityItem())) count += item.getEntityItem().stackSize;
		return count;
	}
	private static void clearDrops(WorldServer world, BlockPos pos) {
		for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.add(-2,-2,-2), pos.add(3,3,3)))) item.setDead();
	}
	private static void require(boolean valid, String message) { if (!valid) throw new IllegalStateException(message); }
}
