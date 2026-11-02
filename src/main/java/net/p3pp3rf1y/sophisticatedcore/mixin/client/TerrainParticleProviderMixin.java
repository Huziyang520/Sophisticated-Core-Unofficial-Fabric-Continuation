package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.client.render.CustomParticleIcon;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.particle.SophisticatedTerrainParticle;
import net.p3pp3rf1y.sophisticatedcore.util.model.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies a block's custom particle sprite to the terrain particles it spawns.
 * <p>
 * 26.3 picks the sprite from the baked block state model and no longer routes through a model
 * shaper, so the sprite is swapped after the particle is created: the block is asked for its
 * {@link CustomParticleIcon} sprite and, when present, that replaces the vanilla one.
 */
@Mixin(TerrainParticle.Provider.class)
public class TerrainParticleProviderMixin {
	@Inject(method = "createParticle(Lnet/minecraft/core/particles/BlockParticleOption;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/util/RandomSource;)Lnet/minecraft/client/particle/Particle;",
			at = @At("RETURN"))
	private void sophisticatedcore$swapSprite(BlockParticleOption option, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, RandomSource random, CallbackInfoReturnable<Particle> cir) {
		Particle particle = cir.getReturnValue();
		if (!(particle instanceof SophisticatedTerrainParticle terrainParticle)) {
			return;
		}
		BlockState state = option.getState();
		if (!(state.getBlock() instanceof CustomParticleIcon icon)) {
			return;
		}
		BlockPos pos = BlockPos.containing(x, y, z);
		ModelData data = icon.getModelData(level, pos, state, ModelData.EMPTY);
		net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = icon.getParticleSprite(data);
		if (sprite != null) {
			terrainParticle.sophisticatedCore_setSprite(sprite);
		}
	}
}
