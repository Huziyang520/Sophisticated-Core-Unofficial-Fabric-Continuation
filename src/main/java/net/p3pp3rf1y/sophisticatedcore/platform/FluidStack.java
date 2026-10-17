package net.p3pp3rf1y.sophisticatedcore.platform;

import com.mojang.serialization.DynamicOps;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Objects;

/**
 * A fluid type plus an amount, i.e. the fluid equivalent of an {@code ItemStack}.
 * <p>
 * 26.3 has no vanilla fluid stack type and Porting Lib (which provided the one this mod was written
 * against) has no 26.x line, so this is a local replacement. It is backed by a Fabric
 * {@link FluidVariant} so that components (potion contents, and so on) travel with the fluid.
 */
public class FluidStack {
	public static final FluidStack EMPTY = new FluidStack(FluidVariant.blank(), 0);

	private FluidVariant variant;
	private long amount;

	public FluidStack(FluidVariant variant) {
		this(variant, 1);
	}

	public FluidStack(StorageView<FluidVariant> view) {
		this(view.isResourceBlank() ? FluidVariant.blank() : view.getResource(), view.getAmount());
	}

	public FluidStack(Fluid fluid, long amount) {
		this(FluidVariant.of(fluid), amount);
	}

	public FluidStack(FluidStack other, long amount) {
		this(other.getVariant(), amount);
	}

	public FluidStack(FluidVariant variant, long amount) {
		this.variant = variant;
		this.amount = amount;
	}

	public FluidVariant getVariant() {
		return variant;
	}

	public Fluid getFluid() {
		return variant.isBlank() ? Fluids.EMPTY : variant.getFluid();
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public boolean isEmpty() {
		return variant.isBlank() || amount <= 0;
	}

	public boolean isFluidEqual(FluidStack other) {
		return Objects.equals(variant, other.variant);
	}

	/** Fluid and components both have to match — amount is deliberately ignored. */
	public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
		return Objects.equals(first.variant, second.variant);
	}

	public FluidStack copy() {
		return new FluidStack(variant, amount);
	}

	/** @return this stack as NBT, or an empty tag when there is nothing to save. */
	public CompoundTag saveOptional(HolderLookup.Provider registryLookup) {
		if (isEmpty()) {
			return new CompoundTag();
		}

		CompoundTag tag = new CompoundTag();
		ops(registryLookup).ifPresent(dynamicOps -> FluidVariant.CODEC.encodeStart(dynamicOps, variant)
				.resultOrPartial()
				.ifPresent(encoded -> tag.put("variant", encoded)));
		tag.putLong("amount", amount);
		return tag;
	}

	/** @return the stack stored in {@code tag}, or {@link #EMPTY} when it holds no fluid. */
	public static FluidStack parseOptional(HolderLookup.Provider registryLookup, CompoundTag tag) {
		if (tag.isEmpty() || !tag.contains("variant")) {
			return EMPTY;
		}

		return ops(registryLookup)
				.map(dynamicOps -> FluidVariant.CODEC.parse(dynamicOps, tag.get("variant")).result())
				.flatMap(result -> result)
				.map(variant -> new FluidStack(variant, tag.getLong("amount")))
				.orElse(EMPTY);
	}

	private static java.util.Optional<DynamicOps<net.minecraft.nbt.Tag>> ops(HolderLookup.Provider registryLookup) {
		return java.util.Optional.of(RegistryOps.create(NbtOps.INSTANCE, registryLookup));
	}
}
