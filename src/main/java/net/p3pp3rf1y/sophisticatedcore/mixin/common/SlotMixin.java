package net.p3pp3rf1y.sophisticatedcore.mixin.common;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.extensions.inventory.SophisticatedSlot;
import net.p3pp3rf1y.sophisticatedcore.util.MixinHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin implements SophisticatedSlot {
    @Shadow
    @Final
    private int slot;

    // 26.3 returns only the sprite id; the block atlas is implied.
    @Unique
    private Identifier sophisticatedCore_noItemIcon;

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void sophisticatedcore$background(CallbackInfoReturnable<Identifier> cir) {
        if (sophisticatedCore_noItemIcon != null) {
            cir.setReturnValue(sophisticatedCore_noItemIcon);
        }
    }

    @Override
    public Slot sophisticatedCore_setBackground(Identifier atlas, Identifier sprite) {
        this.sophisticatedCore_noItemIcon = sprite;
        return MixinHelper.cast(this);
    }

    @Unique
    @Override
    public int sophisticatedCore_getSlotIndex() {
        return slot;
    }
}
