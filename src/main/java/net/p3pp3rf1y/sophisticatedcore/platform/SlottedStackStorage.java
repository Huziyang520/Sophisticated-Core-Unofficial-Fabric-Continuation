package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;

/**
 * A slot-indexed item storage that also exposes plain {@link ItemStack}s.
 * <p>
 * The rest of the code is written against stacks, while transfer interactions need variants;
 * this keeps both views on the same backing slots.
 */
public interface SlottedStackStorage extends SlottedStorage<ItemVariant> {
	/**
	 * The stack in the given slot. Never mutate the returned stack.
	 */
	ItemStack getStackInSlot(int slot);

	/**
	 * Replaces the stack in the given slot. The stack is not copied.
	 */
	void setStackInSlot(int slot, ItemStack stack);

	int getSlotLimit(int slot);

	default boolean isItemValid(int slot, ItemVariant resource, int count) {
		return true;
	}

	default long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).insert(resource, maxAmount, transaction);
	}

	default long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).extract(resource, maxAmount, transaction);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	default Iterator<StorageView<ItemVariant>> iterator() {
		return (Iterator) getSlots().iterator();
	}
}
