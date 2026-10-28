package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.InteractionResult;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientRawInputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 的 {@code MouseHandler} 已删掉 {@code accumulatedScrollY}，原注入点不存在；
 * 这里改挂 {@code onScroll} 头部，语义不变：没人消费就让原版继续处理滚轮。
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true)
	private void sophisticatedcore$onScroll(long handle, double xOffset, double yOffset, CallbackInfo ci) {
		InteractionResult result = ClientRawInputEvent.MOUSE_SCROLLED.invoker().mouseScrolled(Minecraft.getInstance(), xOffset, yOffset);
		if (result != InteractionResult.PASS) {
			ci.cancel();
		}
	}
}
