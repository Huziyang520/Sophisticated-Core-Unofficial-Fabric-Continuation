package net.p3pp3rf1y.sophisticatedcore.platform;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * One slot of an {@link ItemStackHandler}, backed by a stack and participating in transfer transactions.
 */
public class ItemStackHandlerSlot extends SingleStackStorage {
	private final int index;
	private final ItemStackHandler handler;
	private ItemStack stack;
	private ItemStack lastStack;
	private ItemVariant variant;

	public ItemStackHandlerSlot(int index, ItemStackHandler handler, ItemStack initial) {
		this.index = index;
		this.handler = handler;
		this.lastStack = initial.copy();
		setStack(initial);
		handler.initSlot(this);
	}

	@Override
	protected boolean canInsert(ItemVariant itemVariant) {
		return handler.isItemValid(index, itemVariant, 1);
	}

	@Override
	protected int getCapacity(ItemVariant itemVariant) {
		return handler.getStackLimit(index, itemVariant);
	}

	@Override
	protected ItemStack getStack() {
		return stack;
	}

	/**
	 * Only for use inside a transaction; {@link #setNewStack} commits immediately.
	 */
	@Override
	protected void setStack(ItemStack newStack) {
		this.stack = newStack;
		this.variant = ItemVariant.of(newStack);
	}

	public void setNewStack(ItemStack newStack) {
		setStack(newStack);
		onFinalCommit();
	}

	@Override
	public ItemVariant getResource() {
		return variant;
	}

	public int getIndex() {
		return index;
	}

	@Override
	protected void onFinalCommit() {
		onStackChange();
		notifyHandlerOfChange();
	}

	protected void onStackChange() {
		handler.onStackChange(this, lastStack, stack);
		this.lastStack = stack.copy();
	}

	protected void notifyHandlerOfChange() {
		handler.onContentsChanged(index);
	}

	/**
	 * Writes this slot into an existing tag; {@code Slot} is a reserved key handled by the caller.
	 *
	 * @return {@code null} to skip saving this slot
	 */
	@Nullable
	public Tag save(HolderLookup.Provider provider, Tag tag) {
		return ItemStack.CODEC.encode(stack, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
	}

	public void load(HolderLookup.Provider provider, CompoundTag tag) {
		ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result().ifPresent(this::setStack);
		onStackChange();
	}
}
