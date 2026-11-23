package zone.moddev.mc.ironagefurniture.client.particle;

import java.util.Random;

import zone.moddev.mc.ironagefurniture.api.surface.FurnitureSurfaceDisplay;
import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShelvingUnit.ShelfLevel;

import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SurfaceDisplayEffects {
	private static final double LAMP_LAVA_HEIGHT = 4.0D / 16;
	private static final double BUCKET_LAVA_HEIGHT = 4.24D / 16;
	private static final double CANDLE_WICK = -1.5D / 16;
	private static final double CANDLE_FLAME_HEIGHT = 6.7D / 16;

	private SurfaceDisplayEffects() { }

	public static void tickShelf(TileEntityShelvingUnit shelf, Random random) {
		tickLevel(shelf, shelf.getLevel(false), random);
		tickLevel(shelf, shelf.getLevel(true), random);
	}

	private static void tickLevel(TileEntityShelvingUnit shelf, ShelfLevel level, Random random) {
		Vec3d origin = new Vec3d(shelf.getPos()).addVector(0.5D + level.getSurfaceOffsetX(),
			level.getSurfaceHeight(), 0.5D + level.getSurfaceOffsetZ());
		World world = shelf.getWorld();
		tickSurface(world, origin, level.getSurfaceSetting(), level.getScale(), random);

		float shelfYaw = -shelf.getFacing().getOpposite().getHorizontalAngle();
		for (int index = 0; index < level.getVisibleArrangementCount(); index++) {
			ItemStack stack = level.getMediaItem(index);
			boolean lavaBucket = stack.getItem() == Items.LAVA_BUCKET;
			FurnitureSurfaceDisplay display = FurnitureSurfaceDisplay.find(stack);
			if (!lavaBucket && !hasLightEffects(display)) {
				continue;
			}

			ShelfLayout.Placement placement = level.getShelfPlacement(index);
			Vec3d offset = new Vec3d(placement.x, placement.y, placement.z).rotateYaw((float)Math.toRadians(shelfYaw));
			Vec3d item = origin.add(offset);
			if (lavaBucket) {
				lava(world, item.addVector(0, BUCKET_LAVA_HEIGHT * placement.scaleY, 0), random);
			} else {
				tickLight(world, display, item, shelfYaw + placement.yaw, placement.scale, placement.scaleY, random);
			}
		}
	}

	public static void tickSurface(World world, Vec3d origin, SurfaceSetting setting, double scale, Random random) {
		for (Slot slot : Slot.values()) {
			FurnitureSurfaceDisplay display = FurnitureSurfaceDisplay.find(setting.getItem(slot));
			if (hasLightEffects(display)) {
				Vec3d item = origin.addVector((setting.getX(slot) - 0.5D) * scale, 0, (setting.getZ(slot) - 0.5D) * scale);
				float yaw = setting.getFacing(slot).getOpposite().getHorizontalAngle();
				tickLight(world, display, item, yaw, scale, scale, random);
			}
		}
	}

	private static boolean hasLightEffects(FurnitureSurfaceDisplay display) {
		return display == FurnitureSurfaceDisplay.LAVA_LAMP || display == FurnitureSurfaceDisplay.CANDLE;
	}

	private static void tickLight(World world, FurnitureSurfaceDisplay display, Vec3d item,
			float yaw, double scale, double scaleY, Random random) {
		if (display == FurnitureSurfaceDisplay.LAVA_LAMP) {
			lava(world, item.addVector(0, LAMP_LAVA_HEIGHT * scaleY, 0), random);
		} else {
			Vec3d wick = new Vec3d(CANDLE_WICK * scale, CANDLE_FLAME_HEIGHT * scaleY, CANDLE_WICK * scale)
				.rotateYaw((float)Math.toRadians(yaw));
			candle(world, item.add(wick), random);
		}
	}

	private static void lava(World world, Vec3d point, Random random) {
		if (random.nextInt(25) == 0) {
			world.spawnParticle(EnumParticleTypes.LAVA, point.xCoord, point.yCoord, point.zCoord, 0, 0, 0);
			world.playSound(point.xCoord, point.yCoord, point.zCoord, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
				0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
		}

		if (random.nextInt(200) == 0) {
			world.playSound(point.xCoord, point.yCoord, point.zCoord, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS,
				0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
		}
	}

	private static void candle(World world, Vec3d wick, Random random) {
		if (random.nextInt(3) == 0) {
			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, wick.xCoord, wick.yCoord + 0.04D, wick.zCoord, 0, 0, 0);
		}

		CandleFlameParticle.spawn(world, wick.xCoord, wick.yCoord, wick.zCoord);
	}
}
