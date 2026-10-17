package net.p3pp3rf1y.sophisticatedcore.extensions.inventory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

public interface SophisticatedSlot {
    default boolean sophisticatedCore_isSameInventory(Slot other) {
        return ((Slot)this).container == other.container;
    }

    default int sophisticatedCore_getSlotIndex() {
        return 0;
    }

    default Slot sophisticatedCore_setBackground(Identifier atlas, Identifier sprite) {
        throw new RuntimeException("Should have been overriden by mixin.");
    }
}
