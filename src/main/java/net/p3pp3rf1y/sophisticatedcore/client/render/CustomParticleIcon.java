package net.p3pp3rf1y.sophisticatedcore.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.util.model.ModelData;

/**
 * Lets a block pick its own particle sprite.
 * <p>
 * 26.3 sources terrain particles from {@code BlockStateModelSet.getParticleMaterial(state)}, so a
 * block no longer has a baked model to override. Implementations advertise the sprite to use instead,
 * and the particle mixins consult this interface before falling back to the vanilla material.
 */
public interface CustomParticleIcon {
	/**
	 * Per-position model data that may influence the sprite; {@link ModelData#EMPTY} by default.
	 */
	default ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData tileData) {
		return ModelData.EMPTY;
	}

	/**
	 * Sprite to use for particles spawned from this block.
	 *
	 * @return an atlas sprite, or {@code null} to fall back to the vanilla particle material
	 */
	@javax.annotation.Nullable
	default net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleSprite(ModelData data) {
		return null;
	}
}
