package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.client.render.CustomParticleIcon;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.particle.SophisticatedTerrainParticle;
import net.p3pp3rf1y.sophisticatedcore.util.model.ModelData;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

/**
 * Gives terrain particles a settable sprite so blocks can override it.
 */
@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin extends SingleQuadParticle implements SophisticatedTerrainParticle {
	protected TerrainParticleMixin(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
		super(level, x, y, z, sprite);
	}

	@Override
	public Particle sophisticatedCore_updateSprite(BlockState state, @Nullable BlockPos pos) {
		if (pos != null && state.getBlock() instanceof CustomParticleIcon icon) {
			ModelData data = icon.getModelData(level, pos, state, ModelData.EMPTY);
			TextureAtlasSprite sprite = icon.getParticleSprite(data);
			if (sprite != null) {
				setSprite(sprite);
			}
		}
		return (Particle) this;
	}

	@Override
	public void sophisticatedCore_setSprite(TextureAtlasSprite sprite) {
		setSprite(sprite);
	}
}
