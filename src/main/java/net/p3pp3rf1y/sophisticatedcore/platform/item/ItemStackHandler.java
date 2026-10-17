package net.p3pp3rf1y.sophisticatedcore.platform.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

import java.util.ArrayList;
import java.util.List;

/**
 * A fixed size list of item stacks exposed as a {@link SlottedStackStorage}.
 * <p>
 * Replacement for Porting Lib's class of the same name. Subclasses override {@link #onContentsChanged(int)},
 * {@link #makeSlot(int, ItemStack)} or {@link #getSlotLimit(int)} to change behaviour; {@link #slots} is the
 * backing storage and {@link #getSlot(int)} narrows to {@link ItemStackHandlerSlot} so callers can reach the
 * stack directly.
 */
public class ItemStackHandler implements SlottedStackStorage {
	private static final String SIZE_TAG = "Size";
	private static final String ITEMS_TAG = "Items";
	private static final String SLOT_TAG = "Slot";
	private static final int DEFAULT_SLOT_LIMIT = 64;

	protected final List<ItemStackHandlerSlot> slots = new ArrayList<>();

	public ItemStackHandler(int size) {
		setSize(size);
	}

	/** Creates the slot object for {@code index}. Override to use a custom slot type. */
	protected ItemStackHandlerSlot makeSlot(int index, ItemStack stack) {
		return new ItemStackHandlerSlot(index, this, stack);
	}

	/** Resizes the handler, keeping the stacks of the slots that survive. */
	public void setSize(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Slot count must not be negative: " + size);
		}

		while (slots.size() < size) {
			slots.add(makeSlot(slots.size(), ItemStack.EMPTY));
		}
		while (slots.size() > size) {
			slots.remove(slots.size() - 1);
		}
	}

	@Override
	public int getSlotCount() {
		return slots.size();
	}

	@Override
	public ItemStackHandlerSlot getSlot(int slot) {
		return slots.get(slot);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slots.get(slot).getStack();
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		slots.get(slot).setNewStack(stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return DEFAULT_SLOT_LIMIT;
	}

	/** Subclasses may narrow this to a per-stack limit; defaults to the per-slot limit. */
	protected int getStackLimit(int slot, ItemVariant resource) {
		return getSlotLimit(slot);
	}

	/** Called whenever a slot's contents changed. Defaults to doing nothing. */
	protected void onContentsChanged(int slot) {
		// no-op by default
	}

	/** Called after this handler was loaded from NBT. */
	protected void onLoad() {
		// no-op by default
	}

	/** Reports a stack that could not be read while loading. */
	protected void onLoadFailed(int slot, String error) {
		SophisticatedCore.LOGGER.error("Tried to load an invalid item in slot {}: '{}'", slot, error);
	}

	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		setSize(nbt.contains(SIZE_TAG) ? nbt.getIntOr(SIZE_TAG, getSlotCount()) : getSlotCount());

		ListTag items = nbt.getListOrEmpty(ITEMS_TAG);
		for (int i = 0; i < items.size(); i++) {
			CompoundTag itemTag = items.getCompoundOrEmpty(i);
			int slot = itemTag.getIntOr(SLOT_TAG, 0);
			if (slot >= 0 && slot < getSlotCount()) {
				getSlot(slot).load(provider, itemTag);
			}
		}
	}

	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		ListTag items = new ListTag();
		for (int slot = 0; slot < getSlotCount(); slot++) {
			ItemStack stack = getStackInSlot(slot);
			if (stack.isEmpty()) {
				continue;
			}

			CompoundTag itemTag = new CompoundTag();
			itemTag.putInt(SLOT_TAG, slot);
			Tag encoded = ItemStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
					.resultOrPartial(error -> onLoadFailed(slot, error))
					.orElse(new CompoundTag());
			if (encoded instanceof CompoundTag encodedTag) {
				encodedTag.keySet().forEach(key -> itemTag.put(key, encodedTag.get(key)));
			}
			items.add(itemTag);
		}

		CompoundTag ret = new CompoundTag();
		ret.put(ITEMS_TAG, items);
		ret.putInt(SIZE_TAG, getSlotCount());
		return ret;
	}
}
