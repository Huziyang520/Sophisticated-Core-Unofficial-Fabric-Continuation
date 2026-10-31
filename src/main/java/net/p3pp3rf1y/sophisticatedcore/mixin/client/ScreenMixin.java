package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops the vanilla screen extraction for the mod's screens, which draw themselves.
 */
@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(
			method = "extractRenderState",
			at = @At("HEAD"),
			order = 100000,
			cancellable = true
	)
	private void sophisticatedCore$cancelVanillaExtraction(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if ((Object) this instanceof StorageScreenBase<?> || (Object) this instanceof SettingsScreen) {
			ci.cancel();
		}
	}
}