package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.gui.screens.inventory.SophisticatedAbstractContainerScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Exposes the parts of {@link AbstractContainerScreen} the mod's screens need.
 * <p>
 * 26.3 split the old {@code render} body into {@code extractRenderState} /
 * {@code extractContents} / {@code extractSlots}, and the storage screens override
 * {@code extractContents} wholesale, so the render-loop redirections this mixin used to
 * carry are no longer needed.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements SophisticatedAbstractContainerScreen {
	@Shadow
	@Nullable
	public Slot hoveredSlot;

	@Shadow protected int leftPos;

	@Shadow protected int topPos;

	@Shadow protected int imageWidth;

	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "getHoveredSlot", at = @At("HEAD"), cancellable = true)
	private void sophisticatedCore$getHoveredSlot(double mouseX, double mouseY, CallbackInfoReturnable<Slot> cir) {
		if ((Object) this instanceof StorageScreenBase<?> screen) {
			cir.setReturnValue(screen.sophisticatedCore_findSlot(mouseX, mouseY));
		} else if ((Object) this instanceof SettingsScreen screen) {
			cir.setReturnValue(screen.sophisticatedCore_findSlot(mouseX, mouseY));
		}
	}

	@Override
	public int sophisticatedCore_getXSize() {
		return imageWidth;
	}

	@Override
	public int sophisticatedCore_getGuiLeft() {
		return leftPos;
	}

	@Override
	public int sophisticatedCore_getGuiTop() {
		return topPos;
	}

	@Override
	@Nullable
	public Slot sophisticatedCore_getSlotUnderMouse() {
		return hoveredSlot;
	}
}
