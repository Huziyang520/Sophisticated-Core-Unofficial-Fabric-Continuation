package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.p3pp3rf1y.sophisticatedcore.extensions.block.SophisticatedBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets a block replace the vanilla hit particles while mining.
 * <p>
 * 26.3 spawns those effects from {@link ClientLevel} rather than {@code ParticleEngine}, so the hook
 * moved here. The hit result is rebuilt from the mining target because the vanilla signature only
 * carries a position and a face.
 */
@Mixin(ClientLevel.class)
public class ClientLevelHitEffectsMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "addBreakingBlockEffects", at = @At("HEAD"), cancellable = true)
	private void sophisticatedcore$addHitEffects(BlockPos pos, Direction face, boolean isDestroying, CallbackInfo ci) {
		if (!isDestroying) {
			return;
		}
		BlockState state = ((ClientLevel) (Object) this).getBlockState(pos);
		HitResult target = minecraft.hitResult instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)
				? minecraft.hitResult
				: new BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), face, pos, false);
		if (state.sophisticatedCore_addHitEffects((ClientLevel) (Object) this, target, minecraft.particleEngine)) {
			ci.cancel();
		}
	}
}
