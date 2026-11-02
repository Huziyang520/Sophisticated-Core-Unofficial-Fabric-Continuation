package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.extensions.block.SophisticatedBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets a block replace the vanilla destroy particles.
 * <p>
 * 26.3 moved destroy particles from {@code ParticleEngine} onto {@link ClientLevel}, so the hook
 * lives here now. Cancelling keeps vanilla from spawning its own terrain particles when the block
 * already handled the effect.
 */
@Mixin(ClientLevel.class)
public class ClientLevelDestroyEffectsMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
	private void sophisticatedcore$addDestroyEffects(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (state.sophisticatedCore_addDestroyEffects((ClientLevel) (Object) this, pos, minecraft.particleEngine)) {
			ci.cancel();
		}
	}
}
