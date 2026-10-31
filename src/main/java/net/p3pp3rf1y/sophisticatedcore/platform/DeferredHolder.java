package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A lazily resolved reference to a registered value.
 * <p>
 * 26.3 made {@link Holder} a sealed interface, so this can no longer implement it; the
 * {@link #get()} supplier is what the mod's registration code actually uses.
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
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
		return (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
	}

	protected final void bind(boolean throwOnMissingRegistry) {
		if (holder != null) {
			return;
		}
		Registry<R> registry = getRegistry();
		if (registry != null) {
			holder = registry.get(key).orElse(null);
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

	public Optional<ResourceKey<R>> unwrapKey() {
		return Optional.of(key);
	}

	public boolean isBound() {
		bind(false);
		return holder != null;
	}

	@Override
	public String toString() {
		return "DeferredHolder{" + key + "}";
	}
}
