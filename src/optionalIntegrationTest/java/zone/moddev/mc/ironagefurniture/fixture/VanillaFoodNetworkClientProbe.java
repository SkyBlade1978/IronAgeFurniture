package zone.moddev.mc.ironagefurniture.fixture;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Future;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.client.gui.GuiShelfMedia;
import zone.moddev.mc.ironagefurniture.client.gui.ShelfMediaInputProbe;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;

/** Real client packets prove placement, no simultaneous eating, and synchronised pickup. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class VanillaFoodNetworkClientProbe {
	private static BlockPos support;
	private static BlockPos barrelPos;
	private static BlockPos discShelf;
	private static GuiButton shelfClose;
	private static ItemStack pickedBottle;
	private static ItemStack pickedSecondBottle;
	private static Future<?> pending;
	private static int step;
	private static int ticks;
	private static int cases;
	private static final String NAME = "Network food portion";
	private static final String DISC_NAME = "Disc selected from the middle";

	private VanillaFoodNetworkClientProbe() { }

	@SubscribeEvent
	public static void findShelfClose(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.getGui() instanceof GuiShelfMedia) {
			String label = I18n.format("gui.ironagefurniture.shelving.close");
			shelfClose = event.getButtonList().stream().filter(button -> label.equals(button.displayString))
				.findFirst().orElseThrow(() -> new IllegalStateException("Shelf picker has no Close button"));
		}
	}

	static void start(Minecraft client, int verifiedCases) {
		client.gameSettings.pauseOnLostFocus = false;
		client.displayGuiScreen(null);
		cases = verifiedCases;
		support = client.player.getPosition().east(2);
		client.player.inventory.currentItem = 0;
		client.playerController.setGameType(GameType.SURVIVAL);
		pending = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			WorldServer world = player.getServerWorld();
			world.setBlockState(support, Blocks.PLANKS.getDefaultState());
			world.setBlockToAir(support.up());
			player.interactionManager.setGameType(GameType.SURVIVAL);
			player.inventory.clear();
			player.inventory.currentItem = 0;
			player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BREAD, 3).setStackDisplayName(NAME));
			player.getFoodStats().setFoodLevel(10);
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (support == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();
		try {
			if (++ticks > 200) throw new IllegalStateException("Food packet test timed out at step " + step
				+ "; hand=" + client.player.getHeldItemMainhand() + "; slot1=" + client.player.inventory.getStackInSlot(1)
				+ "; display=" + client.world.getBlockState(support.up()));
			if (pending != null) {
				if (!pending.isDone()) return;
				pending.get(); pending = null;
			}
			if (step == 0) {
				if (client.world.getBlockState(support).getBlock() != Blocks.PLANKS
						|| !isBread(client.player.getHeldItemMainhand(), 3)) return;
				setSneaking(client, true);
				ItemStack held = client.player.getHeldItemMainhand();
				client.playerController.processRightClickBlock(client.player, client.world, held,
					support, EnumFacing.UP, new Vec3d(support).addVector(0.5D, 1.0D, 0.5D), EnumHand.MAIN_HAND);
				// This is Minecraft 1.10's immediate fall-through after the cancelled block click.
				client.playerController.processRightClick(client.player, client.world, held, EnumHand.MAIN_HAND);
				require(!client.player.isHandActive(), "Food placement also started client eating");
				step = 1;
			} else if (step == 1) {
				TileEntity tile = client.world.getTileEntity(support.up());
				if (!(tile instanceof SurfaceSettingHost) || !isBread(client.player.getHeldItemMainhand(), 2)) return;
				require(isBread(((SurfaceSettingHost)tile).getSurfaceSetting().getCompatibilityItem(), 1), "Client food NBT did not sync");
				setSneaking(client, false);
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = serverPlayer(client);
					require(!player.isHandActive(), "Food placement also started server eating");
					require(isBread(player.getHeldItemMainhand(), 2), "Server did not consume exactly one food");
					player.inventory.setInventorySlotContents(1, player.getHeldItemMainhand());
					player.setHeldItem(EnumHand.MAIN_HAND, null);
					player.sendContainerToPlayer(player.inventoryContainer);
				});
				step = 2;
			} else if (step == 2) {
				if (client.player.getHeldItemMainhand() != null || !isBread(client.player.inventory.getStackInSlot(1), 2)) return;
				client.playerController.processRightClickBlock(client.player, client.world, null,
					support.up(), EnumFacing.UP, new Vec3d(support).addVector(0.5D, 1.1D, 0.5D), EnumHand.MAIN_HAND);
				step = 3;
			} else if (step == 3) {
				if (!client.world.isAirBlock(support.up()) || !isBread(client.player.inventory.getStackInSlot(1), 3)) return;
				require(client.world.getBlockState(support).getBlock() == Blocks.PLANKS, "Network pickup removed vanilla support");
				require(client.player.getHeldItemMainhand() == null, "Network pickup duplicated food into the empty hand");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = serverPlayer(client);
					require(player.getServerWorld().isAirBlock(support.up()) && player.getHeldItemMainhand() == null
						&& isBread(player.inventory.getStackInSlot(1), 3),
						"Server pickup diverged from client");
					player.interactionManager.setGameType(GameType.CREATIVE);
					player.getFoodStats().setFoodLevel(20);
				});
				step = 4;
			} else if (step == 4) {
				client.playerController.setGameType(GameType.CREATIVE);
				barrelPos = support.east(3);
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					WorldServer world = serverPlayer(client).getServerWorld();
					world.setBlockState(barrelPos, net.minecraft.block.Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak").getDefaultState());
					((zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel)world.getTileEntity(barrelPos)).getFluidHandler()
						.fill(new net.minecraftforge.fluids.FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, 4321), true);
				});
				step = 5;
			} else if (step == 5) {
				TileEntity tile = client.world.getTileEntity(barrelPos);
				if (!(tile instanceof zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel)
						|| ((zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel)tile).getFluidAmount() != 4321) return;
				client.playerController.processRightClickBlock(client.player,client.world,client.player.getHeldItemMainhand(),
					barrelPos,EnumFacing.NORTH,new Vec3d(barrelPos).addVector(.5,.5,.5),EnumHand.MAIN_HAND);
				step = 6;
			} else if (step == 6) {
				if (!(client.currentScreen instanceof zone.moddev.mc.ironagefurniture.client.gui.GuiBarrel)) return;
				zone.moddev.mc.ironagefurniture.api.network.StorageNetwork.channel.sendToServer(
					new zone.moddev.mc.ironagefurniture.api.network.StorageNetwork.Seal(barrelPos,true));
				step = 7;
			} else if (step == 7) {
				zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel tile = (zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel)client.world.getTileEntity(barrelPos);
				if (!tile.isSealed()) return;
				pending = client.getIntegratedServer().addScheduledTask(() -> require(
					((zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel)serverPlayer(client).getServerWorld().getTileEntity(barrelPos)).isSealed(),
					"Barrel seal did not reach the server"));
				step = 8;
			} else if (step == 8) {
				zone.moddev.mc.ironagefurniture.api.network.StorageNetwork.channel.sendToServer(
					new zone.moddev.mc.ironagefurniture.api.network.StorageNetwork.Seal(barrelPos,false));
				step = 9;
			} else if (step == 9) {
				zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel tile = (zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel)client.world.getTileEntity(barrelPos);
				if (tile.isSealed()) return;
				require(tile.getFluidAmount()==4321,"Seal GUI changed the fluid amount");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel serverTile = (zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel)serverPlayer(client).getServerWorld().getTileEntity(barrelPos);
					require(!serverTile.isSealed() && serverTile.getFluidAmount()==4321,"Barrel unseal diverged from client");
				});
				client.player.closeScreen(); step = 10;
			} else if (step == 10) {
				discShelf = support.east(2);
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					WorldServer world = serverPlayer(client).getServerWorld();
					Block unit = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
					world.setBlockState(discShelf.down(), Blocks.STONE.getDefaultState());
					world.setBlockState(discShelf, unit.getDefaultState()
						.withProperty(RackBlock.STANDING, true).withProperty(RackBlock.DATA, true));
					TileEntityShelvingUnit unitTile = (TileEntityShelvingUnit)world.getTileEntity(discShelf);
					ShelfLevel level = unitTile.getLevel(true);
					level.addMedia(new ItemStack(Items.RECORD_CAT));
					level.addMedia(selectedDisc());
					level.addMedia(new ItemStack(Items.RECORD_BLOCKS));
					ShelfLevel books = unitTile.getLevel(false);
					books.addMedia(new ItemStack(Items.BOOK));
					books.addMedia(selectedBook());
					books.addMedia(new ItemStack(Items.ENCHANTED_BOOK)
						.setStackDisplayName("The mason's handbook for building staircases, arches and balconies"));
					level.markSurfaceSettingChanged();
				});
				step = 11;
			} else if (step == 11) {
				TileEntity tile = client.world.getTileEntity(discShelf);
				if (!(tile instanceof TileEntityShelvingUnit)
						|| ((TileEntityShelvingUnit)tile).getLevel(true).getMediaCount() != 3) return;
				client.playerController.processRightClickBlock(client.player, client.world, null, discShelf,
					EnumFacing.NORTH, new Vec3d(discShelf).addVector(0.5D, 0.65D, 0.25D), EnumHand.MAIN_HAND);
				step = 12;
			} else if (step == 12) {
				if (!(client.currentScreen instanceof GuiShelfMedia)
						|| !ItemStack.areItemStacksEqual(client.player.openContainer.getSlot(1).getStack(), selectedDisc())) return;
				step = 13;
			} else if (step == 13) {
				VanillaFoodClientProbe.screenshot(client, "phase5-shelving-disc-selector.png");
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 1);
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 1);
				step = 14;
			} else if (step == 14) {
				ShelfLevel level = ((TileEntityShelvingUnit)client.world.getTileEntity(discShelf)).getLevel(true);
				if (!(client.currentScreen instanceof GuiShelfMedia) || level.getMediaCount() != 2
						|| client.player.openContainer.getSlot(1).getHasStack()
						|| !ItemStack.areItemStacksEqual(client.player.getHeldItemMainhand(), selectedDisc())) return;
				require(level.copyMediaItem(1).getItem() == Items.RECORD_BLOCKS, "Disc row did not compact on the client");
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 2);
				step = 15;
			} else if (step == 15) {
				ItemStack secondDisc = client.player.inventory.getStackInSlot(2);
				if (!(client.currentScreen instanceof GuiShelfMedia) || secondDisc == null
						|| secondDisc.getItem() != Items.RECORD_BLOCKS) return;
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 0);
				step = 16;
			} else if (step == 16) {
				ItemStack thirdDisc = client.player.inventory.getStackInSlot(3);
				if (!(client.currentScreen instanceof GuiShelfMedia) || thirdDisc == null
						|| thirdDisc.getItem() != Items.RECORD_CAT) return;
				ShelfMediaInputProbe.clickClose((GuiShelfMedia)client.currentScreen, shelfClose);
				require(client.currentScreen == null, "Close did not dismiss the disc picker");
				LogManager.getLogger().info("IAF DISC SELECTOR CLIENT PROBE PASSED: repeated picks in stable rows and Close");
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = serverPlayer(client);
					player.inventory.setInventorySlotContents(4, player.getHeldItemMainhand());
					player.setHeldItem(EnumHand.MAIN_HAND, null);
					player.sendContainerToPlayer(player.inventoryContainer);
				});
				step = 17;
			} else if (step == 17) {
				if (client.player.getHeldItemMainhand() != null) return;
				client.playerController.processRightClickBlock(client.player, client.world, null, discShelf,
					EnumFacing.NORTH, new Vec3d(discShelf).addVector(0.5D, 0.20D, 0.25D), EnumHand.MAIN_HAND);
				step = 18;
			} else if (step == 18) {
				if (!(client.currentScreen instanceof GuiShelfMedia)
						|| !ItemStack.areItemStacksEqual(client.player.openContainer.getSlot(1).getStack(), selectedBook())) return;
				require(client.player.openContainer.inventorySlots.size() == 5, "Book picker displayed disc capacity");
				step = 19;
			} else if (step == 19) {
				VanillaFoodClientProbe.screenshot(client, "phase5-shelving-book-selector.png");
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 1);
				step = 20;
			} else if (step == 20) {
				TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(discShelf);
				if (!(client.currentScreen instanceof GuiShelfMedia) || tile.getLevel(false).getMediaCount() != 2
						|| !ItemStack.areItemStacksEqual(client.player.getHeldItemMainhand(), selectedBook())) return;
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 0);
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 2);
				step = 21;
			} else if (step == 21) {
				ItemStack plainBook = client.player.inventory.getStackInSlot(5);
				ItemStack enchantedBook = client.player.inventory.getStackInSlot(6);
				if (!(client.currentScreen instanceof GuiShelfMedia) || client.world.getTileEntity(discShelf) != null
						|| plainBook == null || enchantedBook == null || plainBook.getItem() != Items.BOOK
						|| enchantedBook.getItem() != Items.ENCHANTED_BOOK) return;
				require(!client.player.openContainer.getSlot(0).getHasStack()
					&& !client.player.openContainer.getSlot(2).getHasStack(), "Emptied book rows are still selectable");
				ShelfMediaInputProbe.clickClose((GuiShelfMedia)client.currentScreen, shelfClose);
				require(client.currentScreen == null, "Close did not dismiss the emptied book picker");
				LogManager.getLogger().info("IAF BOOK SELECTOR CLIENT PROBE PASSED: exact NBT, repeated picks and Close after emptying");
				client.playerController.setGameType(GameType.SURVIVAL);
				pending = client.getIntegratedServer().addScheduledTask(() -> {
					EntityPlayerMP player = serverPlayer(client);
					WorldServer world = player.getServerWorld();
					player.interactionManager.setGameType(GameType.SURVIVAL);
					player.inventory.clear();
					world.setBlockState(discShelf, world.getBlockState(discShelf).withProperty(RackBlock.DATA, true));
					ShelfLevel level = ((TileEntityShelvingUnit)world.getTileEntity(discShelf)).getLevel(true);
					java.util.List<ItemStack> bottles = Phase5BottleProbe.samples();
					for (int index = 0; index < 7; index++) {
						level.addMedia(bottles.get(index % bottles.size()));
					}
					pickedSecondBottle = bottles.get(2);
					pickedBottle = bottles.get(bottles.size() > 8 ? 7 : 4).copy().setStackDisplayName("Bottle from the back row");
					level.markSurfaceSettingChanged();
					player.setHeldItem(EnumHand.MAIN_HAND, pickedBottle.copy());
					player.sendContainerToPlayer(player.inventoryContainer);
				});
				step = 22;
			} else if (step == 22) {
				TileEntity tile = client.world.getTileEntity(discShelf);
				if (!(tile instanceof TileEntityShelvingUnit) || !ItemStack.areItemStacksEqual(
						client.player.getHeldItemMainhand(), pickedBottle)) return;
				client.playerController.processRightClickBlock(client.player, client.world, client.player.getHeldItemMainhand(),
					discShelf, EnumFacing.NORTH, new Vec3d(discShelf).addVector(0.5D, 0.65D, 0.25D), EnumHand.MAIN_HAND);
				step = 23;
			} else if (step == 23) {
				ShelfLevel level = ((TileEntityShelvingUnit)client.world.getTileEntity(discShelf)).getLevel(true);
				if (level.getMediaCount() != 8 || client.player.getHeldItemMainhand() != null) return;
				client.playerController.processRightClickBlock(client.player, client.world, null, discShelf,
					EnumFacing.NORTH, new Vec3d(discShelf).addVector(0.5D, 0.65D, 0.25D), EnumHand.MAIN_HAND);
				step = 24;
			} else if (step == 24) {
				if (!(client.currentScreen instanceof GuiShelfMedia) || !ItemStack.areItemStacksEqual(
						client.player.openContainer.getSlot(7).getStack(), pickedBottle)) return;
				require(client.player.openContainer.inventorySlots.size() == 8, "Bottle picker did not show eight slots");
				step = 25;
			} else if (step == 25) {
				VanillaFoodClientProbe.screenshot(client, "phase5-shelving-bottle-selector.png");
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 7);
				client.playerController.sendEnchantPacket(client.player.openContainer.windowId, 2);
				step = 26;
			} else if (step == 26) {
				ShelfLevel level = ((TileEntityShelvingUnit)client.world.getTileEntity(discShelf)).getLevel(true);
				if (!(client.currentScreen instanceof GuiShelfMedia) || level.getMediaCount() != 6
						|| !ItemStack.areItemStacksEqual(client.player.inventory.getStackInSlot(0), pickedBottle)
						|| !ItemStack.areItemStacksEqual(client.player.inventory.getStackInSlot(1), pickedSecondBottle)) return;
				ShelfMediaInputProbe.clickClose((GuiShelfMedia)client.currentScreen, shelfClose);
				require(client.currentScreen == null, "Close did not dismiss the bottle picker");
				LogManager.getLogger().info("IAF BOTTLE SELECTOR CLIENT PROBE PASSED: eight upright bottles and exact repeat pickups");
				step = 27;
			} else {
				client.playerController.setGameType(GameType.CREATIVE);
				support = null;
				GlassVaseNetworkClientProbe.start(client);
			}
		} catch (Exception failure) {
			setSneaking(client, false);
			client.shutdown();
			throw failure;
		}
	}

	static void complete(Minecraft client) throws Exception {
		LogManager.getLogger().info("IAF VANILLA FOOD NETWORK CLIENT PROBE PASSED");
		LogManager.getLogger().info("IAF STORAGE NETWORK CLIENT PROBE PASSED: native GUI, seal and unseal packets, exact fluid sync");
		Files.write(Paths.get("surface-bounds-pass.properties"),
			("status=PASS\ncases=" + (cases + 2) + "\nnetworkFood=PASS\nnetworkStorage=PASS\nnetworkVase=PASS\n").getBytes(StandardCharsets.UTF_8));
		LogManager.getLogger().info("IAF SURFACE BOUNDS CLIENT PROBE PASSED: {} cases", cases + 2);
		VanillaFoodClientProbe.prepareGallery(client);
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static boolean isBread(ItemStack stack, int count) {
		return stack != null && stack.getItem() == Items.BREAD && stack.stackSize == count && NAME.equals(stack.getDisplayName());
	}

	private static ItemStack selectedDisc() {
		return new ItemStack(Items.RECORD_13).setStackDisplayName(DISC_NAME);
	}

	private static ItemStack selectedBook() {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		NBTTagCompound data = new NBTTagCompound();
		NBTTagList pages = new NBTTagList();
		pages.appendTag(new NBTTagString("{\"text\":\"Oak, birch and dark oak\"}"));
		data.setTag("pages", pages);
		data.setString("title", "Notes from the workshop");
		data.setString("author", "IAF probe");
		book.setTagCompound(data);
		return book;
	}

	private static void setSneaking(Minecraft client, boolean sneaking) {
		KeyBinding.setKeyBindState(client.gameSettings.keyBindSneak.getKeyCode(), sneaking);
		client.player.movementInput.sneak = sneaking;
		client.player.setSneaking(sneaking);
		client.player.connection.sendPacket(new CPacketEntityAction(client.player,
			sneaking ? CPacketEntityAction.Action.START_SNEAKING : CPacketEntityAction.Action.STOP_SNEAKING));
	}

	private static void require(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
}
