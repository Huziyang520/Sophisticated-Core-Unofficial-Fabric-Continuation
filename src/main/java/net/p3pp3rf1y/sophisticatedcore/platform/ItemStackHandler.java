package net.p3pp3rf1y.sophisticatedcore.platform;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * A list of item slots addressed by index, the storage type the mod's inventories build on.
 */
public class ItemStackHandler implements SlottedStackStorage {
	private final List<ItemStackHandlerSlot> slots;
	private final SortedSet<ItemStackHandlerSlot> nonEmptySlots;
	private final Map<Item, SortedSet<ItemStackHandlerSlot>> lookup;

	public ItemStackHandler() {
		this(1);
	}

	public ItemStackHandler(int stacks) {
		this(createEmptyStackArray(stacks));
	}

	public ItemStackHandler(ItemStack[] stacks) {
		this.slots = new ArrayList<>(stacks.length);
		this.nonEmptySlots = createSlotSet();
		this.lookup = new HashMap<>();
		for (int i = 0; i < stacks.length; i++) {
			slots.add(makeSlot(i, stacks[i]));
		}
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long inserted = 0;
		Iterator<ItemStackHandlerSlot> itr = getInsertableSlotsFor(resource);
		while (itr.hasNext()) {
			inserted += itr.next().insert(resource, maxAmount - inserted, transaction);
			if (inserted >= maxAmount) {
				break;
			}
		}
		return inserted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		SortedSet<ItemStackHandlerSlot> containing = getSlotsContaining(resource.getItem());
		if (containing.isEmpty()) {
			return 0;
		}
		long extracted = 0;
		for (ItemStackHandlerSlot slot : containing) {
			extracted += slot.extract(resource, maxAmount - extracted, transaction);
			if (extracted >= maxAmount) {
				break;
			}
		}
		return extracted;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
		return (Iterable) nonEmptySlots;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
		return (Iterator) nonEmptySlots.iterator();
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
	@SuppressWarnings({"unchecked", "rawtypes"})
	public List<SingleSlotStorage<ItemVariant>> getSlots() {
		return (List) slots;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return getSlot(slot).getStack();
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		getSlot(slot).setNewStack(stack);
	}

	public ItemVariant getVariantInSlot(int slot) {
		return getSlot(slot).getResource();
	}

	@Override
	public int getSlotLimit(int slot) {
		return getStackInSlot(slot).getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
	}

	/**
	 * The maximum amount of the given variant the slot accepts.
	 */
	protected int getStackLimit(int slot, ItemVariant resource) {
		return Math.min(getSlotLimit(slot), resource.getItem().getDefaultMaxStackSize());
	}

	/**
	 * Called once per modified slot after a transaction commits.
	 */
	protected void onContentsChanged(int slot) {
	}

	/**
	 * All slots holding the given item, ordered by index. Do not modify the result.
	 */
	public SortedSet<ItemStackHandlerSlot> getSlotsContaining(Item item) {
		SortedSet<ItemStackHandlerSlot> found = lookup.get(item);
		return found == null ? createSlotSet() : found;
	}

	/**
	 * Called after NBT has been loaded into this handler.
	 */
	protected void onLoad() {
	}

	public boolean empty() {
		return nonEmptySlots.isEmpty();
	}

	/**
	 * Resizes the handler, dropping all contents.
	 */
	public void setSize(int size) {
		slots.clear();
		nonEmptySlots.clear();
		lookup.clear();
		for (int i = 0; i < size; i++) {
			slots.add(makeSlot(i, ItemStack.EMPTY));
		}
	}

	protected ItemStackHandlerSlot makeSlot(int index, ItemStack stack) {
		return new ItemStackHandlerSlot(index, this, stack);
	}

	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("Size", slots.size());
		ListTag savedSlots = new ListTag();
		for (ItemStackHandlerSlot slot : slots) {
			if (slot.getStack().isEmpty()) {
				continue;
			}
			CompoundTag itemTag = new CompoundTag();
			itemTag.putInt("Slot", slot.getIndex());
			Tag saved = slot.save(provider, itemTag);
			if (saved instanceof CompoundTag compound) {
				savedSlots.add(compound);
			}
		}
		nbt.put("Items", savedSlots);
		return nbt;
	}

	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		setSize(nbt.contains("Size") ? nbt.getIntOr("Size", slots.size()) : slots.size());
		ListTag savedSlots = nbt.getListOrEmpty("Items");
		for (int i = 0; i < savedSlots.size(); i++) {
			CompoundTag slotTag = savedSlots.getCompoundOrEmpty(i);
			int index = slotTag.getIntOr("Slot", -1);
			if (index >= 0 && index < slots.size()) {
				slots.get(index).load(provider, slotTag);
			}
		}
		onLoad();
	}

	void onStackChange(ItemStackHandlerSlot slot, ItemStack oldStack, ItemStack newStack) {
		if (ItemStack.isSameItem(oldStack, newStack)) {
			return;
		}
		SortedSet<ItemStackHandlerSlot> oldItemSlots = lookup.get(oldStack.getItem());
		if (oldItemSlots != null) {
			oldItemSlots.remove(slot);
		}
		lookup.computeIfAbsent(newStack.getItem(), key -> createSlotSet()).add(slot);
		if (oldStack.isEmpty()) {
			nonEmptySlots.add(slot);
		} else if (newStack.isEmpty()) {
			nonEmptySlots.remove(slot);
		}
	}

	void initSlot(ItemStackHandlerSlot slot) {
		ItemStack stack = slot.getStack();
		lookup.computeIfAbsent(stack.getItem(), key -> createSlotSet()).add(slot);
		if (!stack.isEmpty()) {
			nonEmptySlots.add(slot);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + slots + ']';
	}

	private Iterator<ItemStackHandlerSlot> getInsertableSlotsFor(ItemVariant variant) {
		SortedSet<ItemStackHandlerSlot> matching = getSlotsContaining(variant.getItem());
		SortedSet<ItemStackHandlerSlot> emptySlots = getSlotsContaining(Items.AIR);
		if (matching.isEmpty()) {
			return emptySlots.isEmpty() ? Collections.emptyIterator() : emptySlots.iterator();
		}
		if (emptySlots.isEmpty()) {
			return matching.iterator();
		}
		SortedSet<ItemStackHandlerSlot> merged = createSlotSet();
		merged.addAll(matching);
		merged.addAll(emptySlots);
		return merged.iterator();
	}

	private static SortedSet<ItemStackHandlerSlot> createSlotSet() {
		return new ObjectAVLTreeSet<>(Comparator.comparingInt(ItemStackHandlerSlot::getIndex));
	}

	private static ItemStack[] createEmptyStackArray(int size) {
		ItemStack[] stacks = new ItemStack[size];
		java.util.Arrays.fill(stacks, ItemStack.EMPTY);
		return stacks;
	}
}
