package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Collects entries and registers them all when {@link #register()} runs, mirroring the
 * deferred-registration style the mod's init code is written in.
 * <p>
 * Vanilla registries are mutable at init time, so registration is a plain {@link Registry#register} call.
 */
public class DeferredRegister<T> {
	public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
		return new DeferredRegister<>(registry.key(), namespace);
	}

	public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
		return new DeferredRegister<>(registryKey, namespace);
	}

	public static <B> DeferredRegister<B> create(Identifier registryName, String modId) {
		return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), modId);
	}

	private final ResourceKey<? extends Registry<T>> registryKey;
	private final String namespace;
	private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
	private final Collection<DeferredHolder<T, ? extends T>> entriesView = Collections.unmodifiableCollection(entries.keySet());
	private boolean registered;

	protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
		this.registryKey = registryKey;
		this.namespace = namespace;
	}

	public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
		return register(name, key -> supplier.get());
	}

	public <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, ? extends I> function) {
		if (registered) {
			throw new IllegalStateException("Cannot register new entries after register() was called: " + name);
		}
		Identifier key = Identifier.fromNamespaceAndPath(namespace, name);
		DeferredHolder<T, I> holder = DeferredHolder.create(registryKey, key);
		if (entries.putIfAbsent(holder, () -> function.apply(key)) != null) {
			throw new IllegalArgumentException("Duplicate registration " + name);
		}
		return holder;
	}

	/**
	 * Registers everything collected so far. Safe to call once per register.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void register() {
		if (registered) {
			return;
		}
		registered = true;
        // identifier() is the registry itself; registry() is the root that holds every registry.
		Registry<T> registry = (Registry<T>) rawRoot().getValue(rawKey());
		if (registry == null) {
			throw new IllegalStateException("Registry not present: " + registryKey.identifier());
		}
		// 26.3 wants the id while the entry is constructed, so it is published for the call.
		boolean items = Registries.ITEM.identifier().equals(registryKey.identifier());
		boolean blocks = Registries.BLOCK.identifier().equals(registryKey.identifier());
		entries.forEach((holder, supplier) -> {
			if (items) {
				RegistryRegistrationContext.withItemId(holder.getId(), () -> Registry.register(registry, holder.getId(), supplier.get()));
			} else if (blocks) {
				RegistryRegistrationContext.withBlockId(holder.getId(), () -> Registry.register(registry, holder.getId(), supplier.get()));
			} else {
				Registry.register(registry, holder.getId(), supplier.get());
			}
		});
	}


	@SuppressWarnings("rawtypes")
	private static Registry rawRoot() {
		return BuiltInRegistries.REGISTRY;
	}

	@SuppressWarnings("rawtypes")
	private ResourceKey rawKey() {
		return ResourceKey.createRegistryKey(registryKey.identifier());
	}


	public Collection<DeferredHolder<T, ? extends T>> getEntries() {
		return entriesView;
	}

	public ResourceKey<? extends Registry<T>> getRegistryKey() {
		return registryKey;
	}

	public Identifier getRegistryName() {
		return registryKey.identifier();
	}

	public String getNamespace() {
		return namespace;
	}
}