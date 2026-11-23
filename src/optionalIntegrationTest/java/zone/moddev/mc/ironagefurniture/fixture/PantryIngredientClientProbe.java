package zone.moddev.mc.ironagefurniture.fixture;

import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.RackBlock;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Real client clicks exercise the shelf and normal item use. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class PantryIngredientClientProbe {
	private enum Sample {
		EGG(Items.EGG, null, null),
		MILK(Items.MILK_BUCKET, null, null),
		SEEDS(Items.WHEAT_SEEDS, Blocks.FARMLAND, Blocks.WHEAT),
		MUSHROOM(Item.getItemFromBlock(Blocks.RED_MUSHROOM), Blocks.MYCELIUM, Blocks.RED_MUSHROOM),
		EMPTY_BUCKET(Items.BUCKET, null, null),
		WATER_BUCKET(Items.WATER_BUCKET, null, null),
		LAVA_BUCKET(Items.LAVA_BUCKET, null, null);

		private final Item item;
		private final Block support;
		private final Block crop;

		Sample(Item item, Block support, Block crop) {
			this.item = item;
			this.support = support;
			this.crop = crop;
		}

		boolean isBucket() {
			return this == EMPTY_BUCKET || this == WATER_BUCKET || this == LAVA_BUCKET;
		}
	}

	private static BlockPos shelfPos;
	private static BlockPos plantPos;
	private static BlockPos liquidPos;
	private static ItemStack expected;
	private static Future<?> pending;
	private static int phase;
	private static int sampleIndex;
	private static int ticks;

	private PantryIngredientClientProbe() { }

	static void start(Minecraft client) {
		shelfPos = client.player.getPosition().north(2);
		plantPos = shelfPos.east();
		liquidPos = shelfPos.south(2).west(3);
		client.player.inventory.currentItem = 0;
		pending = client.getIntegratedServer().addScheduledTask(() -> {
			WorldServer world = serverPlayer(client).getServerWorld();
			Block shelf = Block.getBlockFromName("ironagefurniture:shelving_unit_wood_ironage_oak");
			world.setBlockState(shelfPos.down(), Blocks.STONE.getDefaultState());
			world.setBlockState(shelfPos, shelf.getDefaultState().withProperty(RackBlock.STANDING, true));
		});
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) throws Exception {
		if (shelfPos == null || event.phase != TickEvent.Phase.END) return;
		Minecraft client = Minecraft.getMinecraft();

		try {
			if (++ticks > 240) {
				throw new IllegalStateException("Ingredient network probe timed out at " + sampleIndex + "/" + phase
					+ " with " + client.player.getHeldItemMainhand() + " in hand");
			}
			if (pending != null) {
				if (!pending.isDone()) return;
				pending.get();
				pending = null;
			}

			Sample sample = Sample.values()[sampleIndex];
			if (phase == 0) {
				equip(client, sample);
				phase++;
			} else if (phase == 1) {
				if (!ItemStack.areItemStacksEqual(client.player.getHeldItemMainhand(), expected)) return;
				if (sample.isBucket()) {
					aimAt(client, new Vec3d(shelfPos).addVector(0.5D, 0.20D, 0.5D));
				}
				clickShelf(client, client.player.getHeldItemMainhand());
				require(!client.player.isHandActive(), "Shelving started using " + sample);
				phase++;
			} else if (phase == 2) {
				if (!ItemStack.areItemStacksEqual(displayed(client), expected) || client.player.getHeldItemMainhand() != null) return;
				pending = client.getIntegratedServer().addScheduledTask(() -> verifyDisplayOnly(client, sample));
				clickShelf(client, null);
				phase++;
			} else if (phase == 3) {
				ItemStack held = client.player.getHeldItemMainhand();
				if (!ItemStack.areItemStacksEqual(held, expected) || displayed(client) != null) return;
				if (sample == Sample.MILK) {
					setUseKey(client, true);
				}
				if (sample.isBucket()) {
					boolean filling = sample == Sample.EMPTY_BUCKET;
					Vec3d target = new Vec3d(liquidPos).addVector(0.5D, filling ? 0.5D : -0.05D, 0.5D);
					BlockPos targetBlock = filling ? liquidPos : liquidPos.down();
					RayTraceResult hit = client.world.rayTraceBlocks(client.player.getPositionEyes(1.0F),
						target, filling, !filling, false);
					require(hit != null && targetBlock.equals(hit.getBlockPos()),
						"Bucket test target " + targetBlock + " is obstructed by " + hit);
					aimAt(client, target);
				}
				if (sample.crop == null) {
					client.playerController.processRightClick(client.player, client.world, held, EnumHand.MAIN_HAND);
				} else {
					client.playerController.processRightClickBlock(client.player, client.world, held,
						plantPos, EnumFacing.UP, new Vec3d(plantPos).addVector(0.5D, 1.0D, 0.5D), EnumHand.MAIN_HAND);
				}
				phase++;
			} else if (normalUseComplete(client, sample)) {
				if (sample == Sample.MILK) {
					setUseKey(client, false);
				}
				if (++sampleIndex == Sample.values().length) {
					shelfPos = null;
					LogManager.getLogger().info("IAF PANTRY INGREDIENT NETWORK PASSED: shelf placement and normal egg, milk, plant and bucket use");
					TravelGearClientProbe.start(client);
				} else {
					phase = 0;
				}
			}
		} catch (Exception failure) {
			setUseKey(client, false);
			client.shutdown();
			throw failure;
		}
	}

	private static void setUseKey(Minecraft client, boolean pressed) {
		KeyBinding.setKeyBindState(client.gameSettings.keyBindUseItem.getKeyCode(), pressed);
	}

	private static void aimAt(Minecraft client, Vec3d target) {
		Vec3d direction = target.subtract(client.player.getPositionEyes(1.0F));
		float yaw = (float)Math.toDegrees(Math.atan2(-direction.xCoord, direction.zCoord));
		float pitch = (float)-Math.toDegrees(Math.atan2(direction.yCoord, Math.hypot(direction.xCoord, direction.zCoord)));
		client.player.rotationYaw = yaw;
		client.player.rotationPitch = pitch;
		client.getConnection().sendPacket(new CPacketPlayer.Rotation(yaw, pitch, client.player.onGround));
	}

	private static void equip(Minecraft client, Sample sample) {
		expected = new ItemStack(sample.item).setStackDisplayName("Pantry test " + sample.name());
		pending = client.getIntegratedServer().addScheduledTask(() -> {
			EntityPlayerMP player = serverPlayer(client);
			WorldServer world = player.getServerWorld();
			world.setBlockToAir(plantPos.up());
			if (sample.support != null) {
				world.setBlockState(plantPos, sample.support.getDefaultState());
			}
			if (sample == Sample.MILK) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 600));
			}
			if (sample.isBucket()) {
				world.setBlockState(liquidPos.down(), Blocks.STONE.getDefaultState());
				world.setBlockToAir(liquidPos.up());
				world.setBlockState(liquidPos, (sample == Sample.EMPTY_BUCKET ? Blocks.WATER : Blocks.AIR).getDefaultState(), 2);
			}
			player.inventory.clear();
			player.inventory.currentItem = 0;
			player.setHeldItem(EnumHand.MAIN_HAND, expected.copy());
			player.sendContainerToPlayer(player.inventoryContainer);
		});
	}

	private static void verifyDisplayOnly(Minecraft client, Sample sample) {
		EntityPlayerMP player = serverPlayer(client);
		WorldServer world = player.getServerWorld();
		require(!player.isHandActive(), "Shelving started using " + sample + " on the server");
		if (sample == Sample.MILK) {
			require(player.isPotionActive(MobEffects.SPEED), "Shelving milk removed a potion effect");
		} else if (sample == Sample.EGG) {
			require(world.getEntitiesWithinAABB(EntityEgg.class, new AxisAlignedBB(shelfPos).expand(8, 8, 8)).isEmpty(),
				"Shelving spawned an egg projectile");
		} else if (sample.isBucket()) {
			Block expected = sample == Sample.EMPTY_BUCKET ? Blocks.WATER : Blocks.AIR;
			require(world.getBlockState(liquidPos).getBlock() == expected, "Shelving changed the nearby liquid fixture");
			for (EnumFacing side : EnumFacing.values()) {
				require(!world.getBlockState(shelfPos.offset(side)).getMaterial().isLiquid(),
					"Shelving poured a bucket next to the shelf");
			}
		} else {
			require(world.isAirBlock(shelfPos.up()) && world.isAirBlock(shelfPos.north())
				&& world.isAirBlock(plantPos.up()), "Shelving planted " + sample);
		}
	}

	private static boolean normalUseComplete(Minecraft client, Sample sample) {
		ItemStack held = client.player.getHeldItemMainhand();
		if (sample == Sample.MILK) {
			return held != null && held.getItem() == Items.BUCKET && !client.player.isPotionActive(MobEffects.SPEED);
		}
		if (sample == Sample.EMPTY_BUCKET) {
			return held != null && held.getItem() == Items.WATER_BUCKET && client.world.isAirBlock(liquidPos);
		}
		if (sample.isBucket()) {
			Material contents = sample == Sample.WATER_BUCKET ? Material.WATER : Material.LAVA;
			return held != null && held.getItem() == Items.BUCKET
				&& client.world.getBlockState(liquidPos).getMaterial() == contents;
		}
		return held == null && (sample.crop == null || client.world.getBlockState(plantPos.up()).getBlock() == sample.crop);
	}

	private static void clickShelf(Minecraft client, ItemStack held) {
		EnumActionResult result = client.playerController.processRightClickBlock(client.player, client.world, held,
			shelfPos, EnumFacing.NORTH, new Vec3d(shelfPos).addVector(0.5D, 0.20D, 0.25D), EnumHand.MAIN_HAND);
		require(result == EnumActionResult.SUCCESS, "Shelf click fell through to normal item use");
	}

	private static ItemStack displayed(Minecraft client) {
		TileEntity tile = client.world.getTileEntity(shelfPos);
		return tile instanceof TileEntityShelvingUnit ? ((TileEntityShelvingUnit)tile).getLevel(false).getMediaItem(0) : null;
	}

	private static EntityPlayerMP serverPlayer(Minecraft client) {
		return client.getIntegratedServer().getPlayerList().getPlayerByUUID(client.player.getUniqueID());
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new IllegalStateException(message);
	}
}
