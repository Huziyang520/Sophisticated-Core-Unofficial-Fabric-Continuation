package net.p3pp3rf1y.sophisticatedcore.platform.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.p3pp3rf1y.sophisticatedcore.platform.TransactionCallback;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * One slot of an {@link ItemStackHandler} exposed as a Fabric {@link SingleSlotStorage}.
 * <p>
 * Replacement for Porting Lib's class of the same name. Writes are deferred through the surrounding
 * transaction so a rolled back transfer leaves the slot untouched.
 */
public class ItemStackHandlerSlot implements SingleSlotStorage<ItemVariant> {
	protected final int index;
	protected final ItemStackHandler handler;
	private ItemStack stack;

	public ItemStackHandlerSlot(int index, ItemStackHandler handler, ItemStack initial) {
		this.index = index;
		this.handler = handler;
		this.stack = initial;
	}

	public int getIndex() {
		return index;
	}

	public ItemStack getStack() {
		return stack;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
	}

	/** Sets the stack and notifies the handler that the slot changed. */
	public void setNewStack(ItemStack stack) {
		setStack(stack);
		onStackChange();
	}

	/** Called after this slot's contents changed. Defaults to notifying the owning handler. */
	protected void onStackChange() {
		handler.onContentsChanged(index);
	}

	/** Reads the stack stored in {@code tag}. Unknown keys (for example the slot index) are ignored. */
	public void load(HolderLookup.Provider provider, CompoundTag tag) {
		ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
				.resultOrPartial(error -> handler.onLoadFailed(index, error))
				.ifPresent(this::setStack);
		onStackChange();
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (resource.isBlank() || maxAmount <= 0) {
			return 0;
		}

		ItemStack current = getStack();
		if (!current.isEmpty() && !resource.matches(current)) {
			return 0;
		}

		int limit = handler.getSlotLimit(index);
		int currentCount = current.isEmpty() ? 0 : current.getCount();
		long insertable = Math.min(maxAmount, Math.max(0, limit - currentCount));
		if (insertable <= 0) {
			return 0;
		}

		ItemStack updated = current.isEmpty()
				? resource.toStack((int) insertable)
				: current.copyWithCount((int) (currentCount + insertable));
		TransactionCallback.onSuccess(transaction, () -> setNewStack(updated));
		return insertable;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack current = getStack();
		if (current.isEmpty() || maxAmount <= 0 || !resource.matches(current)) {
			return 0;
		}

		long extractable = Math.min(maxAmount, current.getCount());
		int remaining = current.getCount() - (int) extractable;
		ItemStack updated = remaining <= 0 ? ItemStack.EMPTY : current.copyWithCount(remaining);
		TransactionCallback.onSuccess(transaction, () -> setNewStack(updated));
		return extractable;
	}

	@Override
	public boolean isResourceBlank() {
		return stack.isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(stack);
	}

	@Override
	public long getAmount() {
		return stack.getCount();
	}

	@Override
	public long getCapacity() {
		return handler.getSlotLimit(index);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof ItemStackHandlerSlot slot && slot.index == index && slot.handler == handler;
	}

	@Override
	public int hashCode() {
		return Objects.hash(System.identityHashCode(handler), index);
	}
}
