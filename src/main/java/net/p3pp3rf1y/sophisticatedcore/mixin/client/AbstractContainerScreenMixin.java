package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.gui.screens.inventory.SophisticatedAbstractContainerScreen;
import net.p3pp3rf1y.sophisticatedcore.util.MixinHelper;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements SophisticatedAbstractContainerScreen {
	@Shadow protected abstract void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick);

	@Shadow
	@Nullable
	public Slot hoveredSlot;

	@Shadow protected int leftPos;

	@Shadow protected int topPos;

	@Shadow protected int imageWidth;

	@Unique
	private boolean sophisticatedCore$isStorageScreen;
	@Unique
	private boolean sophisticatedCore$isSettingsScreen;

	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Inject(
			method = "extractRenderState",
			at = @At("HEAD")
	)
	private void sophisticatedCore$renderHead(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		sophisticatedCore$isStorageScreen = (Object) this instanceof StorageScreenBase;
		sophisticatedCore$isSettingsScreen = (Object) this instanceof SettingsScreen;
	}

	@Inject(
			method = "extractRenderState",
			at = @At("TAIL")
	)
	private void sophisticatedCore$renderTail(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		sophisticatedCore$isStorageScreen = false;
		sophisticatedCore$isSettingsScreen = false;
	}

	@WrapOperation(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/NonNullList;size()I"
			)
	)
    private int sophisticatedCore$MenuSlotSize(NonNullList<Slot> instance, Operation<Integer> original) {
		if (sophisticatedCore$isStorageScreen) {
			return StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS;
		}
		return original.call(instance);
	}

    @WrapOperation(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"
			)
	)
    private Object sophisticatedCore$MenuSlotGet(NonNullList<Slot> instance, int i, Operation<Slot> original) {
		if (sophisticatedCore$isStorageScreen) {
			StorageContainerMenuBase<?> menu = MixinHelper.<StorageScreenBase<? extends StorageContainerMenuBase<?>>>cast(this).getMenu();
			return menu.getSlot(menu.getInventorySlotsSize() - StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS + i);
		}

		return original.call(instance, i);
    }

	@WrapWithCondition(
			method = "extractRenderState",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hoveredSlot:Lnet/minecraft/world/inventory/Slot;",
					opcode = Opcodes.PUTFIELD,
					ordinal = 0
			)
	)
	private boolean sophisticatedCore$bypassHoveredSlotReset(AbstractContainerScreen<T> instance, Slot newSlot) {
		return !sophisticatedCore$isStorageScreen;
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
