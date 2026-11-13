package zone.moddev.mc.ironagefurniture.fixture;

import java.util.UUID;
import com.mojang.authlib.GameProfile;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaFoodPlacement;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityVanillaFoodDisplay;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;

/** Real Forge event dispatch, food allowlist, protection gates, drops and support loss. */
final class VanillaFoodSurfaceProbe {
	private VanillaFoodSurfaceProbe() { }

	static void verify(WorldServer world) {
		EntityPlayerMP player = new EntityPlayerMP(world.getMinecraftServer(), world,
			new GameProfile(UUID.fromString("ff38cc4c-fb53-424e-b9f4-e4679d20626b"), "IAFFoodProbe"),
			new PlayerInteractionManager(world));
		player.connection = new NetHandlerPlayServer(world.getMinecraftServer(),
			new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
			@Override public void sendPacket(Packet<?> packet) { }
		};
		Block display = Block.getBlockFromName("ironagefurniture:vanilla_food_display");
		require(display != Blocks.AIR && Item.getItemFromBlock(display) == null, "Food display must be hidden");
		player.inventory.clear();
		player.capabilities.isCreativeMode = false;
		player.capabilities.allowEdit = true;
		player.setSneaking(true);
		BlockPos pos = new BlockPos(80, 101, 12);
		int cases = 0;
		for (VanillaSurfaceFoods.Food food : VanillaSurfaceFoods.all()) {
			ItemStack known = new ItemStack(food.item, 1, Math.max(0, food.metadata));
			if (!VanillaFoodPlacement.isKnownFood(known)) continue;
			for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
				world.setBlockState(pos, Blocks.PLANKS.getStateFromMeta(cases % 6));
				player.setPositionAndRotation(pos.getX() + 0.5D, pos.getY() + 1.5D,
					pos.getZ() + 2.0D, facing.getHorizontalAngle(), 60.0F);
				ItemStack held = known.copy().setStackDisplayName("Exact food " + cases);
				held.getTagCompound().setInteger("CustomData", cases);
				held.stackSize = 3;
				ItemStack expected = held.copy(); expected.stackSize = 1;
				PlayerInteractEvent.RightClickBlock event = click(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, held);
				require(event.isCanceled() && held.stackSize == 2, "Food placement did not consume one portion");
				require(player.getCooldownTracker().hasCooldown(held.getItem()), "Placement can also start eating");
				require(world.getBlockState(pos).getBlock() == Blocks.PLANKS, "Placement replaced the vanilla support");
				TileEntity tile = world.getTileEntity(pos.up());
				require(tile instanceof TileEntityVanillaFoodDisplay, "Food display tile missing");
				SurfaceSettingHost host = (SurfaceSettingHost)tile;
				require(ItemStack.areItemStacksEqual(expected, host.getSurfaceSetting().getCompatibilityItem()), "Food data changed");
				SurfaceSettingHost reloaded = (SurfaceSettingHost)TileEntity.create(world, tile.writeToNBT(new NBTTagCompound()));
				require(ItemStack.areItemStacksEqual(expected, reloaded.getSurfaceSetting().getCompatibilityItem()), "Food NBT reload changed data");
				player.setSneaking(false);
				player.setHeldItem(EnumHand.MAIN_HAND, null);
				require(display.onBlockActivated(world, pos.up(), display.getDefaultState(), player,
					EnumHand.MAIN_HAND, null, EnumFacing.UP, 0.5F, 0.1F, 0.5F), "Cannot retrieve food");
				require(world.isAirBlock(pos.up()) && ItemStack.areItemStacksEqual(expected,
					player.inventory.getStackInSlot(0)), "Food retrieval lost/duplicated data or retained display");
				player.inventory.clear(); player.setSneaking(true);
				pos = pos.east(2); cases++;
			}
		}
		verifyDeclinedInteractions(world, player, pos);
		verifyDrops(world, player, pos.south(8), display);
		// Persist one display for the subsequent real disk/chunk reload gate.
		BlockPos saved = new BlockPos(80, 103, 8);
		world.setBlockState(saved, Blocks.PLANKS.getDefaultState());
		player.setSneaking(true); player.capabilities.isCreativeMode = false;
		click(world, player, saved, EnumHand.MAIN_HAND, EnumFacing.UP,
			new ItemStack(Items.BREAD).setStackDisplayName("Persisted oak bar loaf"));
		require(world.getTileEntity(saved.up()) instanceof TileEntityVanillaFoodDisplay, "Saved food fixture missing");
		player.setHeldItem(EnumHand.MAIN_HAND, null); player.setSneaking(false);
		LogManager.getLogger().info("IAF VANILLA FOOD SURFACE PROBE PASSED: {} food/facing cases and safety gates", cases);
	}

	private static void verifyDeclinedInteractions(WorldServer world, EntityPlayerMP player, BlockPos pos) {
		world.setBlockState(pos, Blocks.PLANKS.getDefaultState());
		FakePlayer automation = FakePlayerFactory.getMinecraft(world);
		automation.setSneaking(true);
		declines(world, automation, pos, EnumHand.MAIN_HAND, EnumFacing.UP,
			new ItemStack(Items.BREAD), "Automation click intercepted");
		automation.setSneaking(false);
		for (ItemStack held : new ItemStack[]{new ItemStack(Items.ARMOR_STAND), new ItemStack(Items.GLASS_BOTTLE),
				new ItemStack(Items.DIAMOND), new ItemStack(Items.CAKE), new ItemStack(Items.FISH, 1, 9)}) {
			declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, held, "Non-food or unsupported item intercepted");
		}
		player.setSneaking(false);
		declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, new ItemStack(Items.BREAD), "Ordinary eating intercepted");
		player.setSneaking(true);
		declines(world, player, pos, EnumHand.OFF_HAND, EnumFacing.UP, new ItemStack(Items.BREAD), "Offhand intercepted");
		player.setHeldItem(EnumHand.OFF_HAND, new ItemStack(Items.SHIELD));
		declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, new ItemStack(Items.BREAD), "Offhand interaction intercepted");
		player.setHeldItem(EnumHand.OFF_HAND, null);
		declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.NORTH, new ItemStack(Items.BREAD), "Side face intercepted");
		for (Block support : new Block[]{Blocks.CHEST, Blocks.FURNACE, Blocks.CRAFTING_TABLE,
				Blocks.FARMLAND, Blocks.WOODEN_SLAB, Blocks.OAK_STAIRS,
				Block.getBlockFromName("ironagefurniture:table_dining_oak")}) {
			world.setBlockState(pos, support.getDefaultState());
			declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, new ItemStack(Items.CARROT), "Interactive/modded/partial surface intercepted");
		}
		world.setBlockState(pos, Blocks.PLANKS.getDefaultState());
		for (Block occupied : new Block[]{Blocks.STONE, Blocks.TALLGRASS, Blocks.WATER}) {
			world.setBlockState(pos.up(), occupied.getDefaultState());
			declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, new ItemStack(Items.APPLE), "Occupied space overwritten");
			world.setBlockToAir(pos.up());
		}
		player.capabilities.allowEdit = false;
		declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, new ItemStack(Items.BREAD), "Adventure restriction ignored");
		player.capabilities.allowEdit = true;
		boolean enabled = IronAgeFurnitureConfiguration.VANILLA_FOOD_SURFACES;
		try {
			IronAgeFurnitureConfiguration.VANILLA_FOOD_SURFACES = false;
			declines(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, new ItemStack(Items.BREAD), "Disabled food placement intercepted");
		} finally { IronAgeFurnitureConfiguration.VANILLA_FOOD_SURFACES = enabled; }
		Object veto = new Veto();
		MinecraftForge.EVENT_BUS.register(veto);
		try {
			ItemStack bread = new ItemStack(Items.BREAD, 2);
			click(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, bread);
			require(world.isAirBlock(pos.up()) && bread.stackSize == 2, "Another mod's cancelled event ignored");
		} finally { MinecraftForge.EVENT_BUS.unregister(veto); }
		for (Result result : new Result[]{Result.DENY, Result.ALLOW}) {
			for (boolean item : new boolean[]{true, false}) {
				ItemStack bread = new ItemStack(Items.BREAD, 2);
				player.setHeldItem(EnumHand.MAIN_HAND, bread);
				PlayerInteractEvent.RightClickBlock event = event(player, pos, EnumHand.MAIN_HAND, EnumFacing.UP);
				if (item) event.setUseItem(result); else event.setUseBlock(result);
				MinecraftForge.EVENT_BUS.post(event);
				require(!event.isCanceled() && world.isAirBlock(pos.up()) && bread.stackSize == 2,
					"Another mod's explicit interaction result ignored");
			}
		}
	}

	private static void verifyDrops(WorldServer world, EntityPlayerMP player, BlockPos start, Block display) {
		for (int mode = 0; mode < 7; mode++) {
			BlockPos pos = start.east(mode * 4);
			world.setBlockState(pos, Blocks.PLANKS.getDefaultState());
			player.capabilities.isCreativeMode = mode == 0;
			ItemStack apple = new ItemStack(Items.APPLE).setStackDisplayName("Drop " + mode);
			click(world, player, pos, EnumHand.MAIN_HAND, EnumFacing.UP, apple.copy());
			if (mode == 0) require(player.getHeldItemMainhand() != null, "Creative placement consumed food");
			if (mode < 2) {
				display.removedByPlayer(display.getDefaultState(), world, pos.up(), player, false);
			} else if (mode == 2) world.setBlockToAir(pos);
			else if (mode == 3) world.setBlockState(pos, Blocks.CHEST.getDefaultState());
			else if (mode == 4) display.onBlockExploded(world, pos.up(), null);
			else if (mode == 5) {
				// Exercise the real piston movement path with the display in front.
				world.setBlockState(pos.up().west(), Blocks.PISTON.getDefaultState()
					.withProperty(net.minecraft.block.BlockPistonBase.FACING, EnumFacing.EAST));
				world.setBlockState(pos.up().west(2), Blocks.REDSTONE_BLOCK.getDefaultState());
				BlockPos piston = pos.up().west();
				require(Blocks.PISTON.eventReceived(world.getBlockState(piston), world, piston,
					0, EnumFacing.EAST.getIndex()), "Piston did not extend");
			} else world.setBlockToAir(pos.up());
			require(world.getBlockState(pos.up()).getBlock() != display, "Food display survived removal/support loss");
			int count = 0;
			for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class,
					new AxisAlignedBB(pos.add(-1, -1, -1), pos.add(2, 3, 2)))) {
				if (ItemStack.areItemsEqual(apple, drop.getEntityItem())
						&& ItemStack.areItemStackTagsEqual(apple, drop.getEntityItem())) count += drop.getEntityItem().stackSize;
			}
			require(count == (mode == 0 ? 0 : 1), "Food drop lost or duplicated: " + mode + " -> " + count);
			player.capabilities.isCreativeMode = false;
		}
	}

	private static void declines(WorldServer world, EntityPlayerMP player, BlockPos pos, EnumHand hand,
			EnumFacing side, ItemStack held, String reason) {
		IBlockState above = world.getBlockState(pos.up());
		int count = held.stackSize;
		PlayerInteractEvent.RightClickBlock event = click(world, player, pos, hand, side, held);
		require(!event.isCanceled() && world.getBlockState(pos.up()).equals(above) && held.stackSize == count, reason);
	}

	private static PlayerInteractEvent.RightClickBlock click(WorldServer world, EntityPlayerMP player,
			BlockPos pos, EnumHand hand, EnumFacing side, ItemStack held) {
		player.setHeldItem(hand, held);
		PlayerInteractEvent.RightClickBlock event = event(player, pos, hand, side);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	private static PlayerInteractEvent.RightClickBlock event(EntityPlayerMP player, BlockPos pos, EnumHand hand, EnumFacing side) {
		return new PlayerInteractEvent.RightClickBlock(player, hand, player.getHeldItem(hand), pos, side,
			new Vec3d(pos).addVector(0.5D, 1.0D, 0.5D));
	}

	public static final class Veto {
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public void veto(PlayerInteractEvent.RightClickBlock event) { event.setCanceled(true); }
	}

	private static void require(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
}
