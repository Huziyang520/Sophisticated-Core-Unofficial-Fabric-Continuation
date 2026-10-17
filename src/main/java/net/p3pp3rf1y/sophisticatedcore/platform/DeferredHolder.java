package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A registry entry that is created before the registry is actually written to.
 * <p>
 * Replacement for Porting Lib's {@code DeferredHolder}. Fabric has no freeze/unfreeze dance like
 * (Neo)Forge, so this is only a thin "name first, value later" shell that keeps the original call
 * sites shaped the same way: declare the holder in a static field, get the value via {@link #get()}.
 *
 * @param <R> the registry element type
 * @param <T> the concrete type held by this entry
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
	private final ResourceKey<R> key;
	@Nullable
	private T value;

	protected DeferredHolder(ResourceKey<R> key) {
		this.key = key;
	}

	public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, Identifier id) {
		return new DeferredHolder<>(ResourceKey.create(registryKey, id));
	}

	/** @return the id this entry is registered under. */
	public Identifier getId() {
		return key.location();
	}

	/** @return the registry key of this entry. */
	public ResourceKey<R> getKey() {
		return key;
	}

	@Override
	public T get() {
		if (value == null) {
			throw new IllegalStateException("Tried to access a registry entry before registration: " + key.location());
		}
		return value;
	}

	public boolean isPresent() {
		return value != null;
	}

	protected void bind(T value) {
		this.value = value;
	}
}
