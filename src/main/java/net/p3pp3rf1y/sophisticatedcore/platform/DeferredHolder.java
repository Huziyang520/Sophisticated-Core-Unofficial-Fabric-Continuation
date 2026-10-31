package net.p3pp3rf1y.sophisticatedcore.platform;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link Holder} built from a key alone, bound lazily once the value is registered.
 * <p>
 * Lets code refer to its own entries from static initialisers without ordering constraints.
 */
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {
	public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, Identifier valueName) {
		return create(ResourceKey.create(registryKey, valueName));
	}

	public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
		return new DeferredHolder<>(key);
	}

	protected final ResourceKey<R> key;

	@Nullable
	private Holder<R> holder;

	protected DeferredHolder(ResourceKey<R> key) {
		this.key = key;
		bind(false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T value() {
		bind(true);
		if (holder == null) {
			throw new NullPointerException("Trying to access unbound value: " + key);
		}
		return (T) holder.value();
	}

	@Override
	public T get() {
		return value();
	}

	public Optional<T> asOptional() {
		return isBound() ? Optional.of(value()) : Optional.empty();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	protected Registry<R> getRegistry() {
		return (Registry<R>) BuiltInRegistries.REGISTRY.get(key.registry());
	}

	protected final void bind(boolean throwOnMissingRegistry) {
		if (holder != null) {
			return;
		}
		Registry<R> registry = getRegistry();
		if (registry != null) {
			holder = registry.getHolder(key).orElse(null);
		} else if (throwOnMissingRegistry) {
			throw new IllegalStateException("Registry not present for " + this + ": " + key.registry());
		}
	}

	public Identifier getId() {
		return key.identifier();
	}

	public ResourceKey<R> getKey() {
		return key;
	}

	@Override
	public boolean isBound() {
		bind(false);
		return holder != null && holder.isBound();
	}

	@Override
	public boolean areComponentsBound() {
		bind(false);
		return holder != null && holder.areComponentsBound();
	}

	@Override
	public DataComponentMap components() {
		bind(true);
		return holder == null ? DataComponentMap.EMPTY : holder.components();
	}

	@Override
	public boolean is(Identifier id) {
		return id.equals(key.identifier());
	}

	@Override
	public boolean is(ResourceKey<R> other) {
		return other == key;
	}

	@Override
	public boolean is(Predicate<ResourceKey<R>> filter) {
		return filter.test(key);
	}

	@Override
	public boolean is(TagKey<R> tag) {
		bind(false);
		return holder != null && holder.is(tag);
	}

	@Override
	public boolean is(Holder<R> other) {
		bind(false);
		return holder != null && holder.is(other);
	}

	@Override
	public Stream<TagKey<R>> tags() {
		bind(false);
		return holder != null ? holder.tags() : Stream.empty();
	}

	@Override
	public Either<ResourceKey<R>, R> unwrap() {
		return Either.left(key);
	}

	@Override
	public Optional<ResourceKey<R>> unwrapKey() {
		return Optional.of(key);
	}

	@Override
	public Kind kind() {
		return Kind.REFERENCE;
	}

	@Override
	public boolean canSerializeIn(HolderOwner<R> owner) {
		bind(false);
		return holder != null && holder.canSerializeIn(owner);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof Holder<?> h && h.kind() == Kind.REFERENCE && h.unwrapKey().orElse(null) == key;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public String toString() {
		return "DeferredHolder{" + key + "}";
	}
}
