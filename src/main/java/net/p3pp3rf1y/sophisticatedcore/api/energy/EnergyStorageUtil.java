package net.p3pp3rf1y.sophisticatedcore.api.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for moving energy between storages.
 * <p>
 * Both sides may be {@code null} when the lookup found nothing; a missing side simply moves zero
 * energy, which keeps callers from having to null-check every transfer.
 */
public final class EnergyStorageUtil {
	private EnergyStorageUtil() {
	}

	/**
	 * Moves up to {@code maxAmount} energy from {@code from} to {@code to} in a single transaction.
	 *
	 * @return the amount that actually moved
	 */
	public static long move(@Nullable EnergyStorage from, @Nullable EnergyStorage to, long maxAmount) {
		if (from == null || to == null || maxAmount <= 0 || !from.supportsExtraction() || !to.supportsInsertion()) {
			return 0;
		}

		try (Transaction transaction = Transaction.openOuter()) {
			long extracted = from.extract(maxAmount, transaction);
			if (extracted <= 0) {
				return 0;
			}
			long inserted = to.insert(extracted, transaction);
			if (inserted <= 0) {
				return 0;
			}
			transaction.commit();
			return inserted;
		}
	}

	/**
	 * Same as {@link #move(EnergyStorage, EnergyStorage, long)}; the trailing context is accepted
	 * for call sites that used to pass an explicit lookup context.
	 */
	public static long move(@Nullable EnergyStorage from, @Nullable EnergyStorage to, long maxAmount, @Nullable Object context) {
		return move(from, to, maxAmount);
	}

	/**
	 * Moves energy in simulation only, reporting how much would move.
	 */
	public static long simulateMove(@Nullable EnergyStorage from, @Nullable EnergyStorage to, long maxAmount) {
		if (from == null || to == null || maxAmount <= 0 || !from.supportsExtraction() || !to.supportsInsertion()) {
			return 0;
		}

		try (Transaction transaction = Transaction.openOuter()) {
			long extracted = from.extract(maxAmount, transaction);
			if (extracted <= 0) {
				return 0;
			}
			return to.insert(extracted, transaction);
		}
	}
}
