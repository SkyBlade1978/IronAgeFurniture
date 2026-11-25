package zone.moddev.mc.ironagefurniture.fixture;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.tile.ProduceTray;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class HarvestCraftClientProbe {
	private static final String[][] PAGES = {
		{"pear", "orange", "tomato", "strawberry", "grape", "banana", "pineapple", "cantaloupe"},
		{"parsnip", "cucumber", "corn", "onion", "garlic", "broccoli", "cabbage", "whitemushroom"},
		{"lettuce", "asparagus", "coffeebean", "rice", "flour", "almond", "anchovyraw", "venisonraw"},
		{"banananutbread", "applepie", "carrotcake", "cheeseburger", "pbandj", "pancakes", "beetsoup", "chocolateyogurt"},
		{"blueberryjuice", "grapejuice", "cherryjelly", "maplesyrup", "peanutbuttercookies", "pizza", "fishandchips", "chocolatemilkshake"},
		{"cheesecake", "cherrycheesecake", "redvelvetcake", "pineappleupsidedowncake", "carrotcake", "chocolatesprinklecake", "keylimepie", "pecanpie"},
		{"blueberrymuffin", "pumpkinmuffin", "frosteddonut", "chocolatedonut", "brownie", "croissant", "cinnamonroll", "softpretzel"},
		{"waffles", "blueberrypancakes", "spagetti", "lasagna", "sushi", "beefwellington", "friedegg", "omelet"},
		{"boiledegg", "cheese", "firmtofu", "rawtofacon", "chocolatecherry", "chocolatestrawberry", "cottoncandy", "scrambledegg"},
		{"guacamole", "salsa", "gravy", "custard", "mangochutney", "nutella", "pickles", "saltedsunflowerseeds"}
	};
	private static BlockPos origin;
	private static int page;
	private static int ticks;

	private HarvestCraftClientProbe() { }

	static boolean start(Minecraft client) {
		if (!Loader.isModLoaded("iafphcaddon")) return false;
		client.displayGuiScreen(null);
		client.gameSettings.hideGUI = true;
		client.gameSettings.fovSetting = 40;
		client.gameSettings.viewBobbing = false;
		client.gameSettings.showInventoryAchievementHint = false;
		client.guiAchievement.clearAchievements();
		require(!client.getTextureMapBlocks().getAtlasSprite("iafphcaddon:blocks/food_white").getIconName().contains("missing"),
			"Prepared-food material was not stitched into the atlas");
		int covered = 0;
		java.util.List<String> textures = new java.util.ArrayList<>();
		textures.add("item\ttexture\taverage_rgb");
		for (Item item : Item.REGISTRY) {
			if (!item.getRegistryName().getResourceDomain().equals("harvestcraft")) continue;
			ItemStack stack = new ItemStack(item);
			if (SurfaceContentRegistry.describe(stack).getShelfLayout().getCapacity() == 0) continue;
			require(SurfaceRenderRegistry.get(stack) != null, "Unregistered client renderer " + item.getRegistryName());
			require(!client.getRenderItem().getItemModelWithOverrides(stack, null, null).getParticleTexture()
				.getIconName().contains("missing"), "Missing HarvestCraft texture " + item.getRegistryName());
			net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = client.getRenderItem()
				.getItemModelWithOverrides(stack, null, null).getParticleTexture();
			long r = 0, g = 0, b = 0, count = 0;
			if (sprite.getFrameCount() > 0) for (int pixel : sprite.getFrameTextureData(0)[0]) {
				if ((pixel >>> 24) < 128) continue;
				r += (pixel >> 16) & 255; g += (pixel >> 8) & 255; b += pixel & 255; count++;
			}
			textures.add(item.getRegistryName() + "\t" + sprite.getIconName() + "\t"
				+ (count == 0 ? "empty" : String.format("%02X%02X%02X", r / count, g / count, b / count)));
			covered++;
		}
		require(covered > 600, "Unexpectedly small client display coverage");
		LogManager.getLogger().info("IAF HARVESTCRAFT RENDER REGISTRY PASSED: {} items with runtime textures", covered);
		try {
			java.nio.file.Files.write(java.nio.file.Paths.get("harvestcraft-render-textures.tsv"), textures, java.nio.charset.StandardCharsets.UTF_8);
		} catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
		origin = client.player.getPosition().north(6).west(2).up(3);
		showPage(client);
		return true;
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (origin == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();
		camera(client);
		if (++ticks < 32) return;
		try {
			if (page == PAGES.length + 1 && ticks == 32) {
				VanillaFoodClientProbe.screenshot(client, "phase5-harvestcraft-seed-barrels.png");
				return;
			}
			if (page == PAGES.length + 1 && ticks < 48) return;
			String screenshot = page < PAGES.length ? "phase5-harvestcraft-shelves-" + (page + 1) + ".png"
				: page == PAGES.length ? "phase5-harvestcraft-trays-barrels.png" : "phase5-harvestcraft-seed-barrels-topdown.png";
			VanillaFoodClientProbe.screenshot(client, screenshot);
			if (++page <= PAGES.length + 1) {
				showPage(client);
				ticks = 0;
				return;
			}
			origin = null;
			LogManager.getLogger().info("IAF HARVESTCRAFT CLIENT PASSED: shelves, produce trays/barrels, and granular radish/other seed fills");
			if (Boolean.getBoolean("iaf.probe.harvestcraftDisplaysOnly")) {
				java.nio.file.Files.write(java.nio.file.Paths.get("harvestcraft-client-pass.properties"),
					java.util.Collections.singletonList("galleries=" + (PAGES.length + 2)), java.nio.charset.StandardCharsets.UTF_8);
			} else {
				VanillaFoodNetworkClientProbe.writePassReceipt();
			}
			client.shutdown();
		} catch (Exception failure) {
			client.shutdown();
			throw failure;
		}
	}

	private static void showPage(Minecraft client) {
		for (int x = -2; x <= 7; x++) for (int z = -1; z <= 4; z++) for (int y = 0; y <= 3; y++) {
			client.world.setBlockToAir(origin.add(x, y, z));
		}
		if (page < PAGES.length) {
			Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
			for (int column = 0; column < 4; column++) {
				BlockPos pos = origin.add(column, 0, 2);
				client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
				client.world.setBlockState(pos, shelf.getDefaultState().withProperty(RackBlock.STANDING, true)
					.withProperty(RackBlock.DATA, true), 2);
				TileEntityShelvingUnit tile = (TileEntityShelvingUnit)client.world.getTileEntity(pos);
				for (int tier = 0; tier < 2; tier++) {
					ItemStack sample = sample(PAGES[page][column * 2 + tier]);
					int capacity = SurfaceContentRegistry.describe(sample).getCategory()
						== zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSetting.Category.BOTTLE ? 8
						: SurfaceContentRegistry.describe(sample).getShelfLayout().getCapacity();
					require(SurfaceRenderRegistry.get(sample) != null && capacity > 0, "Missing HarvestCraft renderer/layout " + sample);
					for (int i = 0; i < capacity; i++) require(tile.getLevel(tier == 1).addMedia(sample), "Could not fill shelf");
				}
			}
		} else if (page == PAGES.length) {
			Block rack = Block.getBlockFromName("ironagefurniture:produce_rack");
			String[] produce = {"tomato", "pear", "corn", "cabbage", "parsnip", "strawberry"};
			for (int column = 0; column < 2; column++) {
				BlockPos pos = origin.add(column, 0, 2);
				client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
				client.world.setBlockState(pos, rack.getDefaultState(), 2);
				client.world.setBlockState(pos.up(), rack.getDefaultState().withProperty(ProduceFurniture.SECOND, true), 2);
				TileEntityProduceFurniture tile = (TileEntityProduceFurniture)client.world.getTileEntity(pos);
				tile.setWood("oak");
				for (int tier = 0; tier < 3; tier++) {
					ProduceTray tray = new ProduceTray("oak");
					ItemStack item = sample(produce[column * 3 + tier]);
					for (int i = 0; i < ProduceTray.capacity(item); i++) require(tray.offer(item, false), "Could not fill produce tray");
					tile.setTray(tier, tray);
				}
			}
			Block barrel = Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak");
			for (int i = 0; i < 4; i++) {
				BlockPos pos = origin.add(3 + i % 2, 0, 1 + i / 2);
				client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
				client.world.setBlockState(pos, barrel.getDefaultState(), 2);
				TileEntityBarrel tile = (TileEntityBarrel)client.world.getTileEntity(pos);
				ItemStack item = sample(new String[] {"parsnip", "tomato", "cucumber", "pear"}[i]);
				net.minecraft.nbt.NBTTagCompound saved = tile.writeToNBT(new net.minecraft.nbt.NBTTagCompound());
				net.minecraft.nbt.NBTTagCompound bulk = new net.minecraft.nbt.NBTTagCompound();
				bulk.setTag("Stack", item.writeToNBT(new net.minecraft.nbt.NBTTagCompound()));
				bulk.setInteger("Amount", zone.moddev.mc.skysfurniturecore.api.vessel.BarrelBulkStorage.capacityFor(item));
				saved.setTag("BulkContents", bulk);
				tile.readFromNBT(saved);
			}
		} else {
			String[] names = {"radishseed", "radishseed", "radishseed", "tomatoseed", "barleyseed", "rice"};
			int[] amounts = {64, 256, 512, 512, 512, 512};
			Block barrel = Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak");
			for (int i = 0; i < names.length; i++) {
				BlockPos pos = origin.add(1 + i % 3, 0, 1 + i / 3);
				client.world.setBlockState(pos.down(), Blocks.STONEBRICK.getDefaultState(), 2);
				client.world.setBlockState(pos, barrel.getDefaultState(), 2);
				TileEntityBarrel tile = (TileEntityBarrel)client.world.getTileEntity(pos);
				ItemStack item = sample(names[i]);
				require(zone.moddev.mc.skysfurniturecore.api.vessel.BarrelBulkStorage.getGranularColour(item) != null,
					"Missing granular material for " + names[i]);
				net.minecraft.nbt.NBTTagCompound saved = tile.writeToNBT(new net.minecraft.nbt.NBTTagCompound());
				net.minecraft.nbt.NBTTagCompound bulk = new net.minecraft.nbt.NBTTagCompound();
				bulk.setTag("Stack", item.writeToNBT(new net.minecraft.nbt.NBTTagCompound()));
				bulk.setInteger("Amount", amounts[i]);
				saved.setTag("BulkContents", bulk);
				tile.readFromNBT(saved);
			}
		}
		camera(client);
	}

	private static void camera(Minecraft client) {
		client.world.setWorldTime(6000);
		client.player.capabilities.isFlying = true;
		client.player.motionX = client.player.motionY = client.player.motionZ = 0;
		double x = origin.getX() + 2.1, y = origin.getY() + (page >= PAGES.length ? 3.3 : 1.4) - client.player.getEyeHeight(), z = origin.getZ() - 3.2;
		float yaw = page == PAGES.length ? -9 : 0;
		float pitch = page >= PAGES.length ? 25 : 18;
		if (page == PAGES.length + 1 && ticks >= 32) {
			x = origin.getX() + 2.5;
			y = origin.getY() + 4.5 - client.player.getEyeHeight();
			z = origin.getZ() + 2.0;
			yaw = 0;
			pitch = 90;
		}
		client.player.setPositionAndRotation(x, y, z, yaw, pitch);
		client.player.prevPosX = client.player.lastTickPosX = x;
		client.player.prevPosY = client.player.lastTickPosY = y;
		client.player.prevPosZ = client.player.lastTickPosZ = z;
		client.player.prevRotationYaw = yaw;
		client.player.prevRotationPitch = pitch;
	}

	private static ItemStack sample(String name) {
		Item item = Item.getByNameOrId("harvestcraft:" + name + "item");
		require(item != null, "Missing pinned HarvestCraft item " + name);
		return new ItemStack(item);
	}

	private static void require(boolean valid, String message) {
		if (!valid) throw new IllegalStateException(message);
	}
}
