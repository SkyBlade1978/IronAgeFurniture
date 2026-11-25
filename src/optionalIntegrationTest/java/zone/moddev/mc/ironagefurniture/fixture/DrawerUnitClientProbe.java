package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.DrawerUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDrawerUnit;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class DrawerUnitClientProbe {
	private static BlockPos left;
	private static Future<?> ready;
	private static int step;
	private static int ticks;
	private static int openFrames;

	private DrawerUnitClientProbe() { }

	static void start(Minecraft client, BlockPos position) {
		left = position;
		ready = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
			WorldServer world = player.getServerWorld();
			Block full = Block.getBlockFromName("ironagefurniture:drawer_unit_wood_ironage_oak");
			Block half = Block.getBlockFromName("ironagefurniture:half_drawer_unit_wood_ironage_birch");

			for (BlockPos floor : BlockPos.getAllInBox(left.add(-4, -1, -1), left.add(2, -1, 5))) {
				world.setBlockState(floor, Blocks.STONE.getDefaultState());
			}

			for (int y = 0; y < 3; y++) {
				world.setBlockState(left.up(y), full.getDefaultState());
				world.setBlockState(left.east().up(y), full.getDefaultState());
				world.setBlockState(left.west(2).up(y), half.getDefaultState());
				world.setBlockState(left.west(3).up(y), half.getDefaultState());
			}

			((TileEntityDrawerUnit)world.getTileEntity(left.east())).getDrawer(true).setInventorySlotContents(0,
				new ItemStack(Items.BOOK).setStackDisplayName("Wide drawer"));
			player.inventory.clear();
			player.sendContainerToPlayer(player.inventoryContainer);
			player.setPositionAndUpdate(left.getX() + 0.5D, left.getY(), left.getZ() + 3.125D);
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (left == null || event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft client = Minecraft.getMinecraft();
		try {
			require(++ticks < 220, "Drawer client stalled in step " + step + " at " + left + ": "
				+ client.world.getBlockState(left) + " / " + client.world.getBlockState(left.east()) + ", tile "
				+ client.world.getTileEntity(left.east()));
			if (!ready.isDone()) return;
			ready.get();
			BlockPos right = left.east();
			if (!(client.world.getTileEntity(right) instanceof TileEntityDrawerUnit)
					|| step == 0 && client.player.getDistanceSqToCenter(left) >= 20) return;

			IBlockState actual = actual(client, left);
			switch (step) {
			case 0:
				if (actual.getValue(DrawerUnit.PAIR) != DrawerUnit.Pair.LEFT) return;
				verifyModel(client, left);
				verifyModel(client, left.east().up());
				verifyModel(client, left.west(2).up(2));
				verifyModel(client, left.west(3));
				require(client.playerController.processRightClickBlock(client.player, client.world, null, right,
					EnumFacing.SOUTH, new Vec3d(right).addVector(0.5D, 0.72D, 0.99D), EnumHand.MAIN_HAND)
					== EnumActionResult.SUCCESS, "Drawer click was rejected by client");
				break;
			case 1:
				if (!(client.currentScreen instanceof GuiChest) || !actual.getValue(DrawerUnit.UPPER_OPEN)
						|| !actual(client, right).getValue(DrawerUnit.UPPER_OPEN)) return;
				ContainerChest screen = (ContainerChest)client.player.openContainer;
				ItemStack stack = screen.getLowerChestInventory().getStackInSlot(9);
				if (stack == null) return;
				require(screen.getLowerChestInventory().getSizeInventory() == 18 && "Wide drawer".equals(stack.getDisplayName())
					&& !actual.getValue(DrawerUnit.LOWER_OPEN), "Incorrect drawer inventory or opening state");
				verifyModel(client, left);
				verifyModel(client, right);
				// Keep the native container open while capturing its model without the inventory overlay.
				client.currentScreen = null;
				client.gameSettings.hideGUI = true;
				client.player.setPositionAndRotation(left.getX() - 0.85D, left.getY() + 0.3D,
					left.getZ() + 4.75D, 180.0F, 7.0F);
				client.player.prevRotationYaw = 180.0F;
				client.player.prevRotationPitch = 7.0F;
				break;
			case 2:
				if (++openFrames < 8) return;
				require(actual.getValue(DrawerUnit.UPPER_OPEN), "Open drawer closed before its visual capture");
				verifyOpening(client, left);
				verifyOpening(client, right);
				verifyOpening(client, left.west(2));
				verifyOpening(client, left.west(3));
				VanillaFoodClientProbe.screenshot(client, "phase5-drawer-units-open.png");
				client.player.closeScreen();
				client.gameSettings.hideGUI = false;
				break;
			case 3:
				if (client.currentScreen != null || actual.getValue(DrawerUnit.UPPER_OPEN)
						|| actual(client, right).getValue(DrawerUnit.UPPER_OPEN)) return;
				double x = left.getX() - 0.85D;
				double y = left.getY() + 0.3D;
				double z = left.getZ() + 4.75D;
				client.player.setPositionAndRotation(x, y, z, 180.0F, 7.0F);
				client.player.prevRotationYaw = 180.0F;
				client.player.prevRotationPitch = 7.0F;
				break;
			case 4:
				if (ticks % 15 != 0) return;
				VanillaFoodClientProbe.screenshot(client, "phase5-drawer-units.png");
				DoubleStorageBenchClientProbe.start(client, left.east(8));
				left = null;
				LogManager.getLogger().info("IAF DRAWER UNIT NETWORK PASSED: wide GUI, paired open/close state, and baked full/half models");
				return;
			default:
				throw new IllegalStateException("Unexpected drawer probe step " + step);
			}
			step++;
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static IBlockState actual(Minecraft client, BlockPos pos) {
		return client.world.getBlockState(pos).getActualState(client.world, pos);
	}

	private static void verifyModel(Minecraft client, BlockPos pos) {
		IBlockState state = actual(client, pos);
		IBakedModel model = client.getBlockRendererDispatcher().getModelForState(state);
		require(model.getQuads(state, null, 0).size() > 30 && !"missingno".equals(model.getParticleTexture().getIconName()),
			"Drawer multipart geometry did not bake at " + pos);
	}

	private static void verifyOpening(Minecraft client, BlockPos pos) {
		IBlockState state = actual(client, pos);
		float expected = state.getBlock().getRegistryName().getResourcePath().startsWith("half_drawer") ? 3.0F / 16 : 6.0F / 16;
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			for (DrawerUnit.Pair pair : DrawerUnit.Pair.values()) {
				IBlockState closed = state.withProperty(DrawerUnit.FACING, facing).withProperty(DrawerUnit.PAIR, pair)
					.withProperty(DrawerUnit.UPPER_OPEN, false).withProperty(DrawerUnit.LOWER_OPEN, false);
				for (boolean upper : new boolean[] { false, true }) {
					IBlockState open = closed.withProperty(upper ? DrawerUnit.UPPER_OPEN : DrawerUnit.LOWER_OPEN, true);
					float openFront = front(client.getBlockRendererDispatcher().getModelForState(open), open, upper);
					float closedFront = front(client.getBlockRendererDispatcher().getModelForState(closed), closed, upper);
					require(Math.abs(openFront - closedFront - expected) < 0.0001F,
						"Open drawer geometry did not extend to its collision box on " + open);
				}
			}
		}
	}

	private static float front(IBakedModel model, IBlockState state, boolean upper) {
		float furthest = Float.NEGATIVE_INFINITY;
		EnumFacing facing = state.getValue(DrawerUnit.FACING);
		for (BakedQuad quad : model.getQuads(state, null, 0)) {
			int[] data = quad.getVertexData();
			int stride = data.length / 4;
			for (int vertex = 0; vertex < 4; vertex++) {
				float y = Float.intBitsToFloat(data[vertex * stride + 1]);
				if (y > (upper ? 0.56F : 0.06F) && y < (upper ? 0.94F : 0.44F)) {
					float x = Float.intBitsToFloat(data[vertex * stride]);
					float z = Float.intBitsToFloat(data[vertex * stride + 2]);
					furthest = Math.max(furthest, x * facing.getFrontOffsetX() + z * facing.getFrontOffsetZ());
				}
			}
		}
		return furthest;
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
