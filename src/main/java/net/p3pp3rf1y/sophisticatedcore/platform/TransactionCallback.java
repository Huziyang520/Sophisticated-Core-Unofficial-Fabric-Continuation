package net.p3pp3rf1y.sophisticatedcore.platform;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Runs a callback once the outermost transfer transaction commits.
 * <p>
 * Nested transactions defer the callback upwards, so a rolled back parent never applies it.
 */
public final class TransactionCallback {
	private TransactionCallback() {
	}

	public static void onSuccess(TransactionContext ctx, Runnable callback) {
		if (ctx == null) {
			callback.run();
			return;
		}
		ctx.addCloseCallback((transaction, result) -> {
			if (transaction.nestingDepth() == 0) {
				if (result.wasCommitted()) {
					callback.run();
				}
			} else {
				onSuccess(transaction.getOpenTransaction(transaction.nestingDepth() - 1), callback);
			}
		});
	}
}
