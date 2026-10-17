package net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * Notes drifting out of the jukebox upgrade.
 * <p>
 * 26.3 renamed {@code TextureSheetParticle} to {@link SingleQuadParticle}, made {@code getGroup} the render type
 * accessor and turned the colour fields into setters, so this is the same particle against the new base class.
 */
public class JukeboxUpgradeNoteParticle extends SingleQuadParticle {
	private static final float BASE_QUAD_SIZE = 1.5F * 1.5F;

	protected JukeboxUpgradeNoteParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		xd *= 0.01F;
		yd *= 0.05F;
		zd *= 0.01F;
		yd += 0.01D;
		double color = level.getRandom().nextDouble();
		setColor(
				Math.max(0.0F, Mth.sin((color + 0.0F) * ((float) Math.PI * 2F)) * 0.65F + 0.35F),
				Math.max(0.0F, Mth.sin((color + 0.33333334F) * ((float) Math.PI * 2F)) * 0.65F + 0.35F),
				Math.max(0.0F, Mth.sin((color + 0.6666667F) * ((float) Math.PI * 2F)) * 0.65F + 0.35F)
		);
		scale(BASE_QUAD_SIZE);
		setLifetime(20);
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.OPAQUE;
	}

	@Override
	public float getQuadSize(float scaleFactor) {
		return BASE_QUAD_SIZE * Mth.clamp((age + scaleFactor) / lifetime * 32.0F, 0.0F, 1.0F);
	}

	@Override
	public void tick() {
		xo = x;
		yo = y;
		zo = z;
		if (age++ >= lifetime) {
			remove();
		} else {
			move(xd, yd, zd);
			if (y == yo) {
				xd *= 1.1D;
				zd *= 1.1D;
			}
			if (onGround) {
				xd *= 0.7F;
				zd *= 0.7F;
			}
		}
	}

	public static class Factory implements ParticleProvider<JukeboxUpgradeNoteParticleData> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Nullable
		@Override
		public Particle createParticle(JukeboxUpgradeNoteParticleData type, ClientLevel level, double x, double y, double z,
									   double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			JukeboxUpgradeNoteParticle particle = new JukeboxUpgradeNoteParticle(level, x, y, z);
			particle.setSpriteFromAge(spriteSet);
			return particle;
		}
	}
}
