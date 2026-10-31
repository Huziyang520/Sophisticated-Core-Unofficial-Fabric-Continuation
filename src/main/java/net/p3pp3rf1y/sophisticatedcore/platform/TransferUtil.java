package net.p3pp3rf1y.sophisticatedcore.platform;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for moving fluids in and out of a transfer {@link Storage}.
 * <p>
 * Every mutating helper opens (or nests into) a transaction and commits it, so callers that
 * only want to probe a storage must wrap the call in a transaction and abort it.
 */
public final class TransferUtil {
	private TransferUtil() {
	}

	public static Transaction getTransaction() {
		if (Transaction.isOpen()) {
			TransactionContext open = Transaction.getCurrentUnsafe();
			if (open != null) {
				return open.openNested();
			}
		}
		return Transaction.openOuter();
	}

	/**
	 * The first fluid held by the storage, or {@code null} when it holds none.
	 */
	@Nullable
	public static FluidStack getFirstFluid(Storage<FluidVariant> storage) {
		try (Transaction transaction = getTransaction()) {
			for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
				return new FluidStack(view);
			}
		}
		return null;
	}

	/**
	 * Extracts whatever the storage can give, without committing the change.
	 */
	public static FluidStack simulateExtractAnyFluid(Storage<FluidVariant> storage, long maxAmount) {
		if (!storage.supportsExtraction()) {
			return FluidStack.EMPTY;
		}
		try (Transaction transaction = getTransaction()) {
			ResourceAmount<FluidVariant> extracted = StorageUtil.extractAny(storage, maxAmount, transaction);
			return extracted == null ? FluidStack.EMPTY : new FluidStack(extracted);
		}
	}

	/**
	 * The fluid held by the given item stack, if it can hold one.
	 */
	public static FluidStack getFluidContained(ItemStack container) {
		if (container.isEmpty()) {
			return FluidStack.EMPTY;
		}
		Storage<FluidVariant> storage = new MutableContainerItemContext(container.copyWithCount(1)).find(FluidStorage.ITEM);
		if (storage == null) {
			return FluidStack.EMPTY;
		}
		FluidStack first = getFirstFluid(storage);
		return first == null ? FluidStack.EMPTY : first;
	}

	/**
	 * Fills a bucket with the given fluid variant and returns the resulting stack.
	 */
	public static ItemStack getFilledBucket(FluidVariant variant) {
		MutableContainerItemContext context = new MutableContainerItemContext(new ItemStack(Items.BUCKET));
		try (Transaction transaction = getTransaction()) {
			Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
			if (storage != null) {
				storage.insert(variant, FluidConstants.BUCKET, transaction);
				transaction.commit();
			}
		}
		return context.getItemVariant().toStack();
	}
}
