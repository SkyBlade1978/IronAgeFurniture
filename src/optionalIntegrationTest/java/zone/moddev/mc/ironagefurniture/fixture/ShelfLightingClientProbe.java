package zone.moddev.mc.ironagefurniture.fixture;

import java.util.Random;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.client.particle.CandleFlameParticle;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ShelfLightingClientProbe {
	private int pops;
	private int ambient;

	public static void verify(Minecraft client, BlockPos origin) {
		ShelfLightingClientProbe sounds = new ShelfLightingClientProbe();
		ParticleManager original = client.effectRenderer;
		DisplayParticles particles = new DisplayParticles(client, original);
		Random triggerEveryEffect = new Random() {
			@Override
			public int nextInt(int bound) {
				return 0;
			}
		};

		client.effectRenderer = particles;
		MinecraftForge.EVENT_BUS.register(sounds);
		try {
			int[] columns = { 0, 1, 3, 4 };
			int[] light = { 12, 15, 15, 15 };
			for (int index = 0; index < columns.length; index++) {
				BlockPos pos = origin.add(columns[index], 0, 2);
				IBlockState state = client.world.getBlockState(pos);
				client.world.checkLight(pos);
				require(state.getLightValue(client.world, pos) == light[index]
					&& client.world.getLightFor(EnumSkyBlock.BLOCK, pos) >= light[index],
					"Shelf block light did not propagate at " + pos);
				state.getBlock().randomDisplayTick(state, client.world, pos, triggerEveryEffect);
			}

			require(particles.lava == 4 && particles.smoke == 2 && particles.flames == 2,
				"Incorrect display particles: lava " + particles.lava + ", smoke " + particles.smoke + ", flame " + particles.flames);
			require(sounds.pops == 4 && sounds.ambient == 4,
				"Incorrect lava sounds: pop " + sounds.pops + ", ambient " + sounds.ambient);
			require(particles.highestSmoke < origin.getY() + 0.91D, "Candle effects are too close to the shelf above");
			LogManager.getLogger().info("IAF SHELF LIGHT CLIENT PROBE PASSED: real block light, wick flame and smoke, lamp and bucket lava particles and sounds");
		} finally {
			MinecraftForge.EVENT_BUS.unregister(sounds);
			client.effectRenderer = original;
		}
	}

	@SubscribeEvent
	public void onSound(PlaySoundEvent event) {
		String id = event.getSound().getSoundLocation().toString();
		if ("minecraft:block.lava.pop".equals(id)) {
			this.pops++;
		} else if ("minecraft:block.lava.ambient".equals(id)) {
			this.ambient++;
		}
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	private static final class DisplayParticles extends ParticleManager {
		private final ParticleManager delegate;
		private int lava;
		private int smoke;
		private int flames;
		private double highestSmoke = Double.NEGATIVE_INFINITY;

		DisplayParticles(Minecraft client, ParticleManager delegate) {
			super(client.world, client.getTextureManager());
			this.delegate = delegate;
		}

		@Override
		public Particle spawnEffectParticle(int particle, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed, int... parameters) {
			if (particle == EnumParticleTypes.LAVA.getParticleID()) {
				this.lava++;
			} else if (particle == EnumParticleTypes.SMOKE_NORMAL.getParticleID()) {
				this.smoke++;
				this.highestSmoke = Math.max(this.highestSmoke, y);
			}
			return this.delegate.spawnEffectParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
		}

		@Override
		public void addEffect(Particle particle) {
			if (particle instanceof CandleFlameParticle) {
				this.flames++;
			}
			this.delegate.addEffect(particle);
		}
	}
}
