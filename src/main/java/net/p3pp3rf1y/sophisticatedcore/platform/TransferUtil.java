package net.p3pp3rf1y.sophisticatedcore.platform;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

import org.jetbrains.annotations.Nullable;

/**
 * Small helpers around the Fabric Transfer API that the original Forge-flavoured code expected from
 * Porting Lib's {@code TransferUtil}.
 */
public final class TransferUtil {
	private TransferUtil() {
	}

	/**
	 * @return the first fluid this storage can give, capped at {@code maxAmount}, or
	 * {@link FluidStack#EMPTY}. Never modifies the storage.
	 */
	public static FluidStack simulateExtractAnyFluid(Storage<FluidVariant> storage, long maxAmount) {
		ResourceAmount<FluidVariant> content = StorageUtil.findExtractableContent(storage, null);
		if (content == null || content.resource() == null || content.resource().isBlank() || content.amount() <= 0) {
			return FluidStack.EMPTY;
		}
		return new FluidStack(content.resource(), Math.min(content.amount(), maxAmount));
	}

	/** @return the first non blank fluid stored, with the amount the storage holds, or {@link FluidStack#EMPTY}. */
	public static FluidStack getFirstFluid(Storage<FluidVariant> storage) {
		FluidVariant variant = StorageUtil.findStoredResource(storage, v -> !v.isBlank());
		if (variant == null || variant.isBlank()) {
			return FluidStack.EMPTY;
		}
		ResourceAmount<FluidVariant> content = StorageUtil.findExtractableContent(storage, null);
		long amount = content == null || content.resource() == null ? 1 : content.amount();
		return new FluidStack(variant, amount);
	}

	/** @return {@code storage} or an empty view when it is {@code null}. */
	@Nullable
	public static Storage<FluidVariant> orEmpty(@Nullable Storage<FluidVariant> storage) {
		return storage;
	}
}
