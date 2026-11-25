package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceItemHit;
import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.Blocks.ShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;
import zone.moddev.mc.ironagefurniture.api.VasePlantHelper;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSettingHost;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;

/** Actual side-ray click packets, rather than direct centre-point activation. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class GlassVaseNetworkClientProbe {
	private static BlockPos pos;
	private static Future<?> pending;
	private static int step, ticks;
	private static final String VASE = "Packet vase", FLOWER = "Packet flower";
	private GlassVaseNetworkClientProbe() { }
	static void start(Minecraft client) {
		pos = client.player.getPosition().east(2);
		client.playerController.setGameType(GameType.SURVIVAL);
		pending = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = player(client);
			player.interactionManager.setGameType(GameType.SURVIVAL);
			player.inventory.clear();
			player.getServerWorld().setBlockToAir(pos.up());
			player.getServerWorld().setBlockState(pos, Block.getBlockFromName("ironagefurniture:table_dining_oak").getDefaultState());
			player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Block.getBlockFromName("ironagefurniture:ornament_glass_vase"), 2, 10).setStackDisplayName(VASE));
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}
	@SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (pos == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();
		try {
			if (++ticks > 200) {
				throw new IllegalStateException("Vase packet test timed out at step " + step
					+ "; block=" + client.world.getBlockState(pos) + "; displayed=" + displayed(client)
					+ "; held=" + client.player.getHeldItemMainhand());
			}
			if (pending != null) { if (!pending.isDone()) return; pending.get(); pending = null; }
			if (step == 0) {
				// Wait for the table update as well as the held-item packet before clicking.
				Block table = Block.getBlockFromName("ironagefurniture:table_dining_oak");
				if (client.world.getBlockState(pos).getBlock() != table || !client.world.isAirBlock(pos.up())
						|| !named(client.player.getHeldItemMainhand(), VASE, 2)) {
					return;
				}

				client.playerController.processRightClickBlock(client.player, client.world, client.player.getHeldItemMainhand(),
					pos, EnumFacing.UP, new Vec3d(pos).addVector(0.5, 1, 0.5), EnumHand.MAIN_HAND);
				step++;
			} else if (step == 1) {
				if (!named(displayed(client), VASE, 1) || !named(client.player.getHeldItemMainhand(), VASE, 1)) return;
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP p = player(client);
					p.inventory.setInventorySlotContents(1, p.getHeldItemMainhand());
					p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Blocks.RED_FLOWER, 2, 2).setStackDisplayName(FLOWER));
					p.sendContainerToPlayer(p.inventoryContainer);
				}); step++;
			} else if (step == 2) {
				if (!named(client.player.getHeldItemMainhand(), FLOWER, 2)) return;
				clickEdge(client); step++;
			} else if (step == 3) {
				if (!named(VasePlantHelper.getPlant(displayed(client)), FLOWER, 1) || !named(client.player.getHeldItemMainhand(), FLOWER, 1)) return;
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP p = player(client);
					p.inventory.setInventorySlotContents(2, p.getHeldItemMainhand()); p.setHeldItem(EnumHand.MAIN_HAND, null);
					p.sendContainerToPlayer(p.inventoryContainer);
				}); step++;
			} else if (step == 4) {
				if (client.player.getHeldItemMainhand() != null) return;
				clickEdge(client); step++;
			} else if (step == 5) {
				if (VasePlantHelper.hasPlant(displayed(client)) || !named(client.player.inventory.getStackInSlot(2), FLOWER, 2)) return;
				clickEdge(client); step++;
			} else if (step == 6) {
				if (displayed(client) != null || !named(client.player.inventory.getStackInSlot(1), VASE, 2)) return;
				require(client.world.getBlockState(pos).getBlock() == Block.getBlockFromName("ironagefurniture:table_dining_oak"), "Vase pickup removed table");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP p = player(client);
					require(named(p.inventory.getStackInSlot(1), VASE, 2) && p.inventory.getStackInSlot(1).getMetadata() == 10
						&& named(p.inventory.getStackInSlot(2), FLOWER, 2) && p.getServerWorld().isAirBlock(pos.up()),
						"Vase/flower packet retrieval lost NBT, duplicated items or retained reservation");
				}); step++;
			} else if (step == 7) {
				LogManager.getLogger().info("IAF GLASS VASE NETWORK CLIENT PROBE PASSED: side-hit packets insert plant, remove plant, retrieve exact vase and retain table");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP p = player(client);
					p.inventory.clear();
					p.getServerWorld().setBlockState(pos.down(), Blocks.STONE.getDefaultState());
					p.getServerWorld().setBlockState(pos, Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak")
						.getDefaultState().withProperty(RackBlock.STANDING, true));
					ItemStack vase = new ItemStack(Block.getBlockFromName("ironagefurniture:ornament_glass_vase"), 1, 3)
						.setStackDisplayName(VASE);
					VasePlantHelper.addPlant(vase, new ItemStack(Blocks.RED_FLOWER, 1, 0).setStackDisplayName("First rose"));
					((TileEntityShelvingUnit)p.getServerWorld().getTileEntity(pos)).getLevel(true)
						.getSurfaceSetting().setSingleItem(vase, EnumFacing.NORTH);
					((TileEntityShelvingUnit)p.getServerWorld().getTileEntity(pos)).getLevel(true).markSurfaceSettingChanged();
					p.setHeldItem(EnumHand.MAIN_HAND,
						new ItemStack(Block.getBlockFromName("ironagefurniture:ornament_glass_vase"), 1, 11));
					p.sendContainerToPlayer(p.inventoryContainer);
				});
				step++;
			} else if (step == 8) {
				if (shelfLevel(client) == null || !shelfLevel(client).getSurfaceSetting().hasAnyItem()
						|| client.player.getHeldItemMainhand() == null
						|| client.player.getHeldItemMainhand().getMetadata() != 11) return;
				client.playerController.processRightClickBlock(client.player, client.world, client.player.getHeldItemMainhand(),
					pos, EnumFacing.UP, new Vec3d(pos).addVector(0.5D, ShelvingUnit.UPPER_HEIGHT + 0.1D, 0.5D), EnumHand.MAIN_HAND);
				step++;
			} else if (step == 9) {
				if (shelfLevel(client).getSurfaceSetting().getItemCount() != 2 || client.player.getHeldItemMainhand() != null) return;
				pending = give(client, new ItemStack(Blocks.RED_FLOWER, 2, 8).setStackDisplayName("Second daisy"));
				step++;
			} else if (step == 10) {
				if (!named(client.player.getHeldItemMainhand(), "Second daisy", 2)) return;
				clickShelfVase(client, Slot.GUEST_B);
				step++;
			} else if (step == 11) {
				ItemStack first = shelfLevel(client).getSurfaceSetting().getItem(Slot.GUEST_A);
				ItemStack second = shelfLevel(client).getSurfaceSetting().getItem(Slot.GUEST_B);
				if (!named(VasePlantHelper.getPlant(second), "Second daisy", 1)
						|| !named(client.player.getHeldItemMainhand(), "Second daisy", 1)) return;
				require(named(VasePlantHelper.getPlant(first), "First rose", 1), "Second vase planting changed the first flower");
				VanillaFoodClientProbe.screenshot(client, "phase5-paired-vases-network.png");
				pending = give(client, null);
				step++;
			} else if (step == 12) {
				if (client.player.getHeldItemMainhand() != null) return;
				clickShelfVase(client, Slot.GUEST_A);
				step++;
			} else if (step == 13) {
				if (VasePlantHelper.hasPlant(shelfLevel(client).getSurfaceSetting().getItem(Slot.GUEST_A))) return;
				require(named(VasePlantHelper.getPlant(shelfLevel(client).getSurfaceSetting().getItem(Slot.GUEST_B)),
					"Second daisy", 1), "First vase pickup changed the second flower");
				pending = give(client, null);
				step++;
			} else if (step == 14) {
				if (client.player.getHeldItemMainhand() != null) return;
				clickShelfVase(client, Slot.GUEST_A);
				step++;
			} else {
				if (shelfLevel(client).getSurfaceSetting().getItemCount() != 1) return;
				require(shelfLevel(client).getSurfaceSetting().getItem(Slot.GUEST_B).getMetadata() == 11,
					"Paired vase pickup removed the wrong colour");
				pos = null;
				LogManager.getLogger().info("IAF PAIRED VASE NETWORK PASSED: second vase placement and independent flower packets");
				PantryIngredientClientProbe.start(client);
			}
		} catch (Exception failure) { client.shutdown(); throw failure; }
	}
	private static ShelfLevel shelfLevel(Minecraft client) {
		return client.world.getTileEntity(pos) instanceof TileEntityShelvingUnit
			? ((TileEntityShelvingUnit)client.world.getTileEntity(pos)).getLevel(true) : null;
	}
	private static Future<?> give(Minecraft client, ItemStack stack) {
		return client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP p = player(client);
			p.setHeldItem(EnumHand.MAIN_HAND, stack);
			p.sendContainerToPlayer(p.inventoryContainer);
		});
	}
	private static void clickShelfVase(Minecraft client, Slot slot) {
		AxisAlignedBB bounds = ShelvingUnit.getItemBounds(shelfLevel(client), slot, true).offset(pos);
		Vec3d centre = new Vec3d((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2,
			(bounds.minZ + bounds.maxZ) / 2);
		IBlockState state = client.world.getBlockState(pos);
		RayTraceResult hit = state.collisionRayTrace(client.world, pos,
			new Vec3d(centre.xCoord, centre.yCoord, bounds.minZ - 0.01D), centre);
		require(hit != null && hit.subHit >= 100, "Paired vase has no front-edge hit");
		client.playerController.processRightClickBlock(client.player, client.world, client.player.getHeldItemMainhand(),
			pos, hit.sideHit, hit.hitVec, EnumHand.MAIN_HAND);
	}
	private static void clickEdge(Minecraft client) {
		SurfaceSettingHost host = (SurfaceSettingHost)client.world.getTileEntity(pos);
		AxisAlignedBB bounds = SurfaceItemHit.bounds(host, Slot.CENTER, true).offset(pos);
		Vec3d centre = new Vec3d((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2, (bounds.minZ + bounds.maxZ) / 2);
		BlockPos target = pos.up(); IBlockState state = client.world.getBlockState(target);
		RayTraceResult hit = state.collisionRayTrace(client.world, target,
			new Vec3d(centre.xCoord, centre.yCoord, bounds.minZ - 0.01D), centre);
		require(hit != null && hit.sideHit == EnumFacing.NORTH, "Vase has no front-edge hit");
		client.playerController.processRightClickBlock(client.player, client.world, client.player.getHeldItemMainhand(),
			target, hit.sideHit, hit.hitVec, EnumHand.MAIN_HAND);
	}
	private static ItemStack displayed(Minecraft client) {
		return client.world.getTileEntity(pos) instanceof SurfaceSettingHost
			? ((SurfaceSettingHost)client.world.getTileEntity(pos)).getSurfaceSetting().getCompatibilityItem() : null;
	}
	private static EntityPlayerMP player(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}
	private static boolean named(ItemStack stack, String name, int size) { return stack != null && stack.stackSize == size && name.equals(stack.getDisplayName()); }
	private static void require(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
}
