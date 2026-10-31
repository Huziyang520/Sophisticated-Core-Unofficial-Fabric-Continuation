package net.p3pp3rf1y.sophisticatedcore.platform;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

/**
 * Small helpers around transfer storages that the mod's fluid and item code keeps needing.
 */
public final class TransferHelper {
	private TransferHelper() {
	}

	/**
	 * Inserts the stack into the player's inventory and drops whatever does not fit.
	 */
	public static void giveItemToPlayer(Player player, @NotNull ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		try (Transaction transaction = Transaction.openOuter()) {
			PlayerInventoryStorage.of(player).offerOrDrop(ItemVariant.of(stack), stack.getCount(), transaction);
			transaction.commit();
		}
	}

	/**
	 * Exposes a {@link BucketPickup} block as a one-bucket extraction-only fluid storage.
	 */
	public static class BucketPickupHandlerWrapper implements SingleSlotStorage<FluidVariant>, ExtractionOnlyStorage<FluidVariant> {
		private final BucketPickup bucketPickupHandler;
		private final Level level;
		private final BlockPos blockPos;

		public BucketPickupHandlerWrapper(BucketPickup bucketPickupHandler, Level level, BlockPos blockPos) {
			this.bucketPickupHandler = bucketPickupHandler;
			this.level = level;
			this.blockPos = blockPos;
		}

		@Override
		public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			if (resource.isBlank() || maxAmount < FluidConstants.BUCKET) {
				return 0;
			}
			FluidState fluidState = level.getFluidState(blockPos);
			if (fluidState.isEmpty() || resource.getFluid() != fluidState.getType()) {
				return 0;
			}
			TransactionCallback.onSuccess(transaction,
					() -> bucketPickupHandler.pickupBlock(null, level, blockPos, level.getBlockState(blockPos)));
			return FluidConstants.BUCKET;
		}

		@Override
		public boolean isResourceBlank() {
			return getResource().isBlank();
		}

		@Override
		public FluidVariant getResource() {
			return FluidVariant.of(level.getFluidState(blockPos).getType());
		}

		@Override
		public long getAmount() {
			return level.getFluidState(blockPos).isEmpty() ? 0 : FluidConstants.BUCKET;
		}

		@Override
		public long getCapacity() {
			return FluidConstants.BUCKET;
		}
	}

	/**
	 * Plays the pickup sound and drops leftovers for a stack that could not be fully given away.
	 */
	public static void dropRemainder(Player player, ItemStack stack, int remainder) {
		Level level = player.level();
		if (remainder <= 0 || level.isClientSide) {
			return;
		}
		ItemEntity entity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), stack.copyWithCount(remainder));
		entity.setPickUpDelay(40);
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
		level.addFreshEntity(entity);
		level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
				SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F);
	}

	/**
	 * Convenience for code that only needs to know whether a storage accepts anything at all.
	 */
	public static boolean isEmpty(Storage<?> storage) {
		for (var ignored : storage.nonEmptyViews()) {
			return false;
		}
		return true;
	}
}
