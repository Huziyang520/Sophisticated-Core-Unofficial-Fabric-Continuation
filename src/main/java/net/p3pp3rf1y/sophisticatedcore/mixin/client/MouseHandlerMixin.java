package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.InteractionResult;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientRawInputEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Publishes raw scroll input before vanilla consumes it.
 * <p>
 * 26.3 removed the {@code accumulatedScrollY} field this used to anchor to, so the hook
 * now runs at the head of {@code onScroll} with the raw offsets.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void sophisticatedCore$onScroll(long handle, double xOffset, double yOffset, CallbackInfo ci) {
		if (handle != this.minecraft.getWindow().handle()) {
			return;
		}
		// Screens and overlays handle their own scrolling.
		if (this.minecraft.gui.overlay() != null || this.minecraft.gui.screen() != null) {
			return;
		}
		InteractionResult result = ClientRawInputEvent.MOUSE_SCROLLED.invoker().mouseScrolled(minecraft, xOffset, yOffset);
		if (result != InteractionResult.PASS) {
			ci.cancel();
		}
	}
}
