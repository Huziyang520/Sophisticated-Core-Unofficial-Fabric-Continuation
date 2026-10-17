package net.p3pp3rf1y.sophisticatedcore.platform;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

/**
 * Runs a callback only if the surrounding transfer transaction actually commits.
 * <p>
 * Replacement for Porting Lib's {@code TransactionCallback}. Fabric expresses this with a close
 * callback on the transaction context instead of an explicit "execute" action.
 */
public final class TransactionCallback {
	private TransactionCallback() {
	}

	/**
	 * @param transaction the transaction to hook; when {@code null} the callback runs immediately,
	 *                    which is how callers express "not inside a transaction".
	 * @param callback    the work to run once the transaction commits
	 */
	public static void onSuccess(@Nullable TransactionContext transaction, Runnable callback) {
		if (transaction == null) {
			callback.run();
			return;
		}

		transaction.addCloseCallback((ctx, result) -> {
			if (result.wasCommitted()) {
				callback.run();
			}
		});
	}
}
