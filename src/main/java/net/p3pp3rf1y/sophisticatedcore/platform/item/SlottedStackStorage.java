package net.p3pp3rf1y.sophisticatedcore.platform.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

/**
 * A slot based item storage that can also be addressed by plain {@link ItemStack}s.
 * <p>
 * Replacement for Porting Lib's interface of the same name. It extends the Fabric
 * {@link SlottedStorage} (so it plugs into the native transfer API) and adds the stack shaped accessors
 * the mod was written against. The Fabric side is satisfied by default methods, so implementations
 * only have to provide the stack shaped ones.
 */
public interface SlottedStackStorage extends SlottedStorage<ItemVariant> {
	ItemStack getStackInSlot(int slot);

	void setStackInSlot(int slot, ItemStack stack);

	int getSlotLimit(int slot);

	/** @return the variant held in {@code slot}, blank for an empty slot. */
	default ItemVariant getVariantInSlot(int slot) {
		return ItemVariant.of(getStackInSlot(slot));
	}

	/** Inserts into one slot only, honouring the transaction. */
	default long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).insert(resource, maxAmount, transaction);
	}

	/** Extracts from one slot only, honouring the transaction. */
	default long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).extract(resource, maxAmount, transaction);
	}

	default boolean isItemValid(int slot, ItemVariant resource, int count) {
		return true;
	}

	@Override
	default java.util.Iterator<StorageView<ItemVariant>> iterator() {
		return new java.util.Iterator<>() {
			private int next = 0;

			@Override
			public boolean hasNext() {
				return next < getSlotCount();
			}

			@Override
			public StorageView<ItemVariant> next() {
				return getSlot(next++);
			}
		};
	}

	@Override
	default long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long inserted = 0;
		for (int slot = 0; slot < getSlotCount() && inserted < maxAmount; slot++) {
			inserted += insertSlot(slot, resource, maxAmount - inserted, transaction);
		}
		return inserted;
	}

	@Override
	default long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = 0;
		for (int slot = 0; slot < getSlotCount() && extracted < maxAmount; slot++) {
			extracted += extractSlot(slot, resource, maxAmount - extracted, transaction);
		}
		return extracted;
	}
}
