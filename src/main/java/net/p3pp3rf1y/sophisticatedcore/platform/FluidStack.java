package net.p3pp3rf1y.sophisticatedcore.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A fluid plus an amount, i.e. the fluid equivalent of an {@code ItemStack}.
 * <p>
 * 26.3 has no vanilla fluid stack type and Porting Lib has no 26.x line, so this is a local
 * replacement backed by a Fabric {@link FluidVariant} so that components travel with the fluid.
 */
public final class FluidStack implements DataComponentHolder {
	public static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
			.validate(holder -> holder.is(Fluids.EMPTY.builtInRegistryHolder())
					? com.mojang.serialization.DataResult.error(() -> "Fluid must not be minecraft:empty")
					: com.mojang.serialization.DataResult.success(holder));

	public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(
			instance -> instance.group(
							FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
							Codec.LONG.validate(l -> l <= 0
									? com.mojang.serialization.DataResult.error(() -> "Amount must be positive")
									: com.mojang.serialization.DataResult.success(l)).fieldOf("amount").forGetter(FluidStack::getAmount),
							DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
									.forGetter(stack -> stack.variant.getComponentsPatch()))
					.apply(instance, FluidStack::new)));

	public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
			.xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
		private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);

		@Override
		public FluidStack decode(RegistryFriendlyByteBuf buf) {
			long amount = buf.readVarLong();
			if (amount <= 0) {
				return FluidStack.EMPTY;
			}
			Holder<Fluid> holder = FLUID_STREAM_CODEC.decode(buf);
			DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
			return new FluidStack(holder, amount, patch);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
			if (stack.isEmpty()) {
				buf.writeVarLong(0);
				return;
			}
			buf.writeVarLong(stack.getAmount());
			FLUID_STREAM_CODEC.encode(buf, stack.getFluidHolder());
			DataComponentPatch.STREAM_CODEC.encode(buf, stack.variant.getComponentsPatch());
		}
	};

	private static final Logger LOGGER = LogUtils.getLogger();

	public static final FluidStack EMPTY = new FluidStack(FluidVariant.blank(), 0);

	private final FluidVariant variant;
	private long amount;

	public FluidStack(FluidVariant variant) {
		this(variant, 1);
	}

	public FluidStack(FluidVariant variant, long amount) {
		this.variant = variant;
		this.amount = amount;
	}

	public FluidStack(Holder<Fluid> fluid, long amount, DataComponentPatch patch) {
		this(FluidVariant.of(fluid.value(), patch), amount);
	}

	public FluidStack(Fluid fluid, long amount) {
		this(FluidVariant.of(fluid), amount);
	}

	public FluidStack(FluidStack other, long amount) {
		this(other.variant, amount);
	}

	public FluidStack(StorageView<FluidVariant> view) {
		this(view.isResourceBlank() ? FluidVariant.blank() : view.getResource(), view.getAmount());
	}

	public FluidStack(ResourceAmount<FluidVariant> resourceAmount) {
		this(resourceAmount.resource(), resourceAmount.amount());
	}

	public static Optional<FluidStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
		return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
				.resultOrPartial(error -> LOGGER.error("Tried to load invalid fluid: '{}'", error));
	}

	public static FluidStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
		return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
	}

	public boolean isEmpty() {
		return this == EMPTY || variant.isBlank() || amount <= 0;
	}

	public Fluid getFluid() {
		return isEmpty() ? Fluids.EMPTY : variant.getFluid();
	}

	public Holder<Fluid> getFluidHolder() {
		return isEmpty() ? Fluids.EMPTY.builtInRegistryHolder() : variant.typeHolder();
	}

	public FluidVariant getVariant() {
		return variant;
	}

	public long getAmount() {
		return isEmpty() ? 0 : amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public void grow(long addedAmount) {
		setAmount(getAmount() + addedAmount);
	}

	public void shrink(long removedAmount) {
		grow(-removedAmount);
	}

	public void limitSize(long limit) {
		if (!isEmpty() && getAmount() > limit) {
			setAmount(limit);
		}
	}

	public boolean is(TagKey<Fluid> tag) {
		return getFluid().builtInRegistryHolder().is(tag);
	}

	public boolean is(Fluid fluid) {
		return getFluid() == fluid;
	}

	public boolean is(Predicate<Holder<Fluid>> predicate) {
		return predicate.test(getFluidHolder());
	}

	public boolean is(Holder<Fluid> holder) {
		return is(holder.value());
	}

	public boolean is(HolderSet<Fluid> holderSet) {
		return holderSet.contains(getFluidHolder());
	}

	public Stream<TagKey<Fluid>> getTags() {
		return getFluid().builtInRegistryHolder().tags();
	}

	public Component getHoverName() {
		return net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes.getName(variant);
	}

	/**
	 * Looks the fluid up in an arbitrary registry, used by viewer integrations that need the raw key.
	 */
	@Nullable
	public <T> T getKeyOfType(Class<T> type) {
		return type.isInstance(getFluid()) ? type.cast(getFluid()) : null;
	}

	public Tag save(HolderLookup.Provider lookupProvider, Tag prefix) {
		if (isEmpty()) {
			throw new IllegalStateException("Cannot encode empty FluidStack");
		}
		return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
	}

	public Tag saveOptional(HolderLookup.Provider lookupProvider) {
		return isEmpty() ? new CompoundTag() : save(lookupProvider, new CompoundTag());
	}

	public FluidStack copy() {
		return isEmpty() ? EMPTY : new FluidStack(variant, amount);
	}

	public FluidStack copyWithAmount(long newAmount) {
		return isEmpty() ? EMPTY : new FluidStack(variant, newAmount);
	}

	public static boolean matches(FluidStack first, FluidStack second) {
		if (first == second) {
			return true;
		}
		return first.getAmount() == second.getAmount() && isSameFluidSameComponents(first, second);
	}

	public static boolean isSameFluid(FluidStack first, FluidStack second) {
		return first.is(second.getFluid());
	}

	public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
		if (!first.is(second.getFluid())) {
			return false;
		}
		return first.isEmpty() && second.isEmpty() || Objects.equals(first.getComponents(), second.getComponents());
	}

	public static int hashFluidAndComponents(@Nullable FluidStack stack) {
		if (stack == null) {
			return 0;
		}
		int i = 31 + stack.getFluid().hashCode();
		return 31 * i + stack.getComponents().hashCode();
	}

	@Override
	public DataComponentMap getComponents() {
		return variant.getComponents();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof FluidStack other && matches(this, other);
	}

	@Override
	public int hashCode() {
		return (int) (getAmount() * 31 + hashFluidAndComponents(this));
	}

	@Override
	public String toString() {
		return getAmount() + " " + getFluid();
	}
}
