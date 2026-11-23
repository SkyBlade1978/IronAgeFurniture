package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.ToolRack;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityToolRack;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class ToolRackClientProbe {
	private static BlockPos first;
	private static Future<?> prepared;
	private static int step;
	private static int ticks;

	private ToolRackClientProbe() { }

	static void start(Minecraft client, BlockPos origin) {
		first = origin;
		client.playerController.setGameType(GameType.SURVIVAL);
		client.player.inventory.currentItem = 0;
		prepared = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
			WorldServer world = player.getServerWorld();
			Block rack = Block.getBlockFromName("ironagefurniture:tool_rack_wood_ironage_oak");

			for (BlockPos wall : BlockPos.getAllInBox(origin.add(-1, -1, 1), origin.add(4, 2, 1))) {
				world.setBlockState(wall, Blocks.STONEBRICK.getDefaultState());
			}
			for (BlockPos floor : BlockPos.getAllInBox(origin.add(-1, -1, -5), origin.add(4, -1, 0))) {
				world.setBlockState(floor, Blocks.PLANKS.getDefaultState());
			}

			ItemStack[] tools = {null, new ItemStack(Items.SHIELD), ToolRackProbe.pickaxe(), new ItemStack(Items.IRON_SHOVEL),
				new ItemStack(Items.GOLDEN_SWORD), ShelvingPantryProbe.enchantedSword(), new ItemStack(Items.SHEARS),
				new ItemStack(Items.FLINT_AND_STEEL)};
			for (int i = 0; i < 4; i++) {
				BlockPos pos = origin.east(i);
				world.setBlockState(pos, rack.getDefaultState());
				TileEntityToolRack tile = (TileEntityToolRack)world.getTileEntity(pos);
				tile.insertTool(0, tools[i * 2]);
				tile.insertTool(1, tools[i * 2 + 1]);
			}

			player.interactionManager.setGameType(GameType.SURVIVAL);
			player.inventory.clear();
			player.inventory.currentItem = 0;
			player.inventory.setInventorySlotContents(0, ToolRackProbe.shield());
			player.sendContainerToPlayer(player.inventoryContainer);
			player.setPositionAndUpdate(origin.getX() + 0.65D, origin.getY(), origin.getZ() - 2.25D);
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (first == null || event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		try {
			require(++ticks < 220, "Tool rack client stalled at step " + step);
			if (!prepared.isDone()) {
				return;
			}
			prepared.get();
			TileEntity tile = client.world.getTileEntity(first);
			if (!(tile instanceof TileEntityToolRack)) {
				return;
			}

			ItemStack stored = ((TileEntityToolRack)tile).getTool(0);
			ItemStack held = client.player.getHeldItemMainhand();
			switch (step) {
			case 0:
				if (client.player.getDistanceSqToCenter(first) >= 20 || !ItemStack.areItemStacksEqual(held, ToolRackProbe.shield())) {
					return;
				}
				require(mask(client, first) == 2 && mask(client, first.east()) == 3
					&& mask(client, first.east(3)) == 1, "Tool rack joins did not render at the expected ends");
				click(client);
				break;
			case 1:
				if (held != null || !ItemStack.areItemStacksEqual(stored, ToolRackProbe.shield())) {
					return;
				}
				click(client);
				break;
			case 2:
				if (stored != null || !ItemStack.areItemStacksEqual(held, ToolRackProbe.shield())) {
					return;
				}
				click(client);
				break;
			case 3:
				if (held != null || !ItemStack.areItemStacksEqual(stored, ToolRackProbe.shield())) {
					return;
				}
				client.player.setPositionAndRotation(first.getX() + 2, first.getY() + 0.22D, first.getZ() - 3.75D, 0, 16);
				client.player.prevRotationYaw = 0;
				client.player.prevRotationPitch = 16;
				break;
			case 4:
				if (ticks % 16 != 0) {
					return;
				}
				VanillaFoodClientProbe.screenshot(client, "phase5-tool-racks.png");
				first = null;
				LogManager.getLogger().info("IAF TOOL RACK NETWORK PASSED: continuous rail and exact shield hang, pickup and rehang");
				client.playerController.setGameType(GameType.CREATIVE);
				client.getIntegratedServer().addScheduledTask(() -> client.getIntegratedServer().getPlayerList()
					.getPlayerByUUID(client.player.getUniqueID()).interactionManager.setGameType(GameType.CREATIVE));
				VanillaFoodClientProbe.prepareGallery(client);
				return;
			default:
				throw new IllegalStateException("Unexpected tool rack step " + step);
			}
			step++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static int mask(Minecraft client, BlockPos pos) {
		IBlockState state = client.world.getBlockState(pos);
		return state.getActualState(client.world, pos).getValue(ToolRack.CONNECTIONS);
	}

	private static void click(Minecraft client) {
		Vec3d click = new Vec3d(first).addVector(11.5D / 16, 0.75D, 12.5D / 16);
		require(client.playerController.processRightClickBlock(client.player, client.world, null, first, EnumFacing.NORTH,
			click, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS, "Client rack hook click was rejected");
	}

	private static void require(boolean condition, String problem) {
		if (!condition) {
			throw new IllegalStateException(problem);
		}
	}
}
