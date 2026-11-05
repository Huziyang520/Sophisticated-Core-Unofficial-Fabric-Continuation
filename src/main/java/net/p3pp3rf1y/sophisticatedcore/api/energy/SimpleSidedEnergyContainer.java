package net.p3pp3rf1y.sophisticatedcore.api.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Base energy storage with per-side limits and rollback support.
 * <p>
 * Subclasses provide capacity and the per-side insert/extract limits; this class handles the
 * transaction bookkeeping so callers do not have to. Amounts are committed on transaction close,
 * which is what lets the same code run inside a larger transfer without leaving garbage behind.
 */
public abstract class SimpleSidedEnergyContainer implements EnergyStorage {
	public long amount;
	private long capacity = getCapacity();
	private final Map<TransactionContext, Long> transactionSnapshots = new IdentityHashMap<>();

	@Override
	public long getAmount() {
		return amount;
	}

	@Override
	public long getCapacity() {
		return capacity;
	}

	/**
	 * Overrides the reported capacity, for containers whose capacity changes at runtime.
	 */
	protected void setCapacity(long capacity) {
		this.capacity = capacity;
	}

	public long getMaxInsert(@Nullable net.minecraft.core.Direction side) {
		return Long.MAX_VALUE;
	}

	public long getMaxExtract(@Nullable net.minecraft.core.Direction side) {
		return Long.MAX_VALUE;
	}

	@Override
	public boolean supportsInsertion() {
		return getMaxInsert(null) > 0;
	}

	@Override
	public boolean supportsExtraction() {
		return getMaxExtract(null) > 0;
	}

	@Override
	public long insert(long maxAmount, TransactionContext transaction) {
		long inserted = Math.min(Math.min(maxAmount, getMaxInsert(null)), getCapacity() - amount);
		if (inserted <= 0) {
			return 0;
		}
		snapshot(transaction);
		amount += inserted;
		return inserted;
	}

	@Override
	public long extract(long maxAmount, TransactionContext transaction) {
		long extracted = Math.min(Math.min(maxAmount, getMaxExtract(null)), amount);
		if (extracted <= 0) {
			return 0;
		}
		snapshot(transaction);
		amount -= extracted;
		return extracted;
	}

	@Override
	public EnergyStorage getSideStorage(@Nullable net.minecraft.core.Direction side) {
		return new SimpleSidedEnergyContainer() {
			@Override
			public long getMaxInsert(@Nullable net.minecraft.core.Direction ignored) {
				return SimpleSidedEnergyContainer.this.getMaxInsert(side);
			}

			@Override
			public long getMaxExtract(@Nullable net.minecraft.core.Direction ignored) {
				return SimpleSidedEnergyContainer.this.getMaxExtract(side);
			}

			@Override
			public long getAmount() {
				return SimpleSidedEnergyContainer.this.getAmount();
			}

			@Override
			public long getCapacity() {
				return SimpleSidedEnergyContainer.this.getCapacity();
			}

			@Override
			public long insert(long maxAmount, TransactionContext transaction) {
				return SimpleSidedEnergyContainer.this.insert(maxAmount, transaction);
			}

			@Override
			public long extract(long maxAmount, TransactionContext transaction) {
				return SimpleSidedEnergyContainer.this.extract(maxAmount, transaction);
			}

			@Override
			public EnergyStorage getSideStorage(@Nullable net.minecraft.core.Direction ignored) {
				return this;
			}

			@Override
			protected void onFinalCommit() {
				SimpleSidedEnergyContainer.this.onFinalCommit();
			}
		};
	}

	/**
	 * Called once per transaction that actually changed the stored amount.
	 */
	protected void onFinalCommit() {
		// no-op by default
	}

	private void snapshot(TransactionContext transaction) {
		if (transactionSnapshots.containsKey(transaction)) {
			return;
		}
		long oldAmount = amount;
		transactionSnapshots.put(transaction, oldAmount);
		transaction.addCloseCallback((context, result) -> {
			Long snapshot = transactionSnapshots.remove(context);
			if (snapshot == null) {
				return;
			}
			if (result.wasCommitted()) {
				onFinalCommit();
			} else {
				amount = snapshot;
			}
		});
	}
}
