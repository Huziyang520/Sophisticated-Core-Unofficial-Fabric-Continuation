package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.MouseButtons;
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

	// 26.3 returns true for any hovered child, which short-circuits slot clicks; keep 1.21.1 semantics.
	@WrapOperation(
			method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"),
			require = 1)
	private boolean sophisticatedCore$screenMouseClicked(AbstractContainerScreen screen, MouseButtonEvent event, boolean doubleClick, Operation<Boolean> original) {
		Object self = (Object) this;
		if (!(self instanceof StorageScreenBase<?> || self instanceof SettingsScreen)) {
			return original.call(screen, event, doubleClick);
		}
		for (GuiEventListener child : children()) {
			if (child.mouseClicked(event, doubleClick)) {
				setFocused(child);
				if (MouseButtons.toLegacy(event.button()) == 0) {
					setDragging(true);
				}
				return true;
			}
		}
		return false;
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
