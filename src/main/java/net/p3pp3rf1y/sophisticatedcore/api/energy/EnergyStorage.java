package net.p3pp3rf1y.sophisticatedcore.api.energy;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import org.jetbrains.annotations.Nullable;

/**
 * Unified energy storage contract for 26.3.
 * <p>
 * The Team Reborn energy API is no longer published, so this mirrors its shape on top of the
 * Fabric transfer transaction model. Amounts are longs in energy units; transfers must respect
 * the supplied {@link TransactionContext} so they can be nested and rolled back.
 */
public interface EnergyStorage {
	/**
	 * Lookup for energy storage on an item. Callers resolve it through a {@link ContainerItemContext}.
	 */
	ItemApiLookup<EnergyStorage, ContainerItemContext> ITEM = ItemApiLookup.get(
			Identifier.fromNamespaceAndPath(SophisticatedCore.MOD_ID, "energy"), EnergyStorage.class, ContainerItemContext.class);

	/**
	 * Lookup for energy storage on a block side.
	 */
	BlockApiLookup<EnergyStorage, Direction> SIDED = BlockApiLookup.get(
			Identifier.fromNamespaceAndPath(SophisticatedCore.MOD_ID, "sided_energy"), EnergyStorage.class, Direction.class);

	/**
	 * Storage that holds nothing and accepts nothing, for callers that need a non-null placeholder.
	 */
	EnergyStorage EMPTY = new EnergyStorage() {
		@Override
		public long insert(long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public long extract(long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public long getAmount() {
			return 0;
		}

		@Override
		public long getCapacity() {
			return 0;
		}

		@Override
		public boolean supportsInsertion() {
			return false;
		}

		@Override
		public boolean supportsExtraction() {
			return false;
		}
	};

	/**
	 * Inserts energy and returns the amount that was accepted.
	 */
	long insert(long maxAmount, TransactionContext transaction);

	/**
	 * Extracts energy and returns the amount that was taken.
	 */
	long extract(long maxAmount, TransactionContext transaction);

	long getAmount();

	long getCapacity();

	/**
	 * Whether {@link #insert} can accept anything right now.
	 */
	default boolean supportsInsertion() {
		return true;
	}

	/**
	 * Whether {@link #extract} can return anything right now.
	 */
	default boolean supportsExtraction() {
		return true;
	}

	/**
	 * Side-specific view used by blocks that expose different limits per face.
	 */
	@Nullable
	default EnergyStorage getSideStorage(@Nullable Direction side) {
		return this;
	}
}
