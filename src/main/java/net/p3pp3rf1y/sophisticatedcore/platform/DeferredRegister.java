package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Collects registry entries and writes them into the registry in one go.
 * <p>
 * Replacement for Porting Lib's {@code DeferredRegister}. Entries are declared with
 * {@link #register(String, Supplier)} and flushed by {@link #register()} during mod initialisation.
 *
 * @param <R> the registry element type
 */
public class DeferredRegister<R> {
	private final Registry<R> registry;
	private final String modId;
	private final List<Pending<R>> entries = new ArrayList<>();

	private DeferredRegister(Registry<R> registry, String modId) {
		this.registry = registry;
		this.modId = modId;
	}

	/** Creates a register for an already resolved registry. */
	public static <R> DeferredRegister<R> create(Registry<R> registry, String modId) {
		return new DeferredRegister<>(registry, modId);
	}

	/** Creates a register for a registry identified by its key. */
	public static <R> DeferredRegister<R> create(ResourceKey<? extends Registry<R>> registryKey, String modId) {
		return new DeferredRegister<>(resolveByName(registryKey.location()), modId);
	}

	/** Creates a register for a registry identified by its name. */
	public static <R> DeferredRegister<R> create(Identifier registryName, String modId) {
		return new DeferredRegister<>(resolveByName(registryName), modId);
	}

	@SuppressWarnings("unchecked")
	private static <R> Registry<R> resolveByName(Identifier registryName) {
		Object found = BuiltInRegistries.REGISTRY.getValue(registryName);
		if (found == null) {
			throw new IllegalStateException("Unknown registry: " + registryName);
		}
		return (Registry<R>) found;
	}

	/**
	 * Declares an entry. The value supplier is only invoked from {@link #register()}.
	 */
	public <I extends R> DeferredHolder<R, I> register(String name, Supplier<? extends I> valueSupplier) {
		DeferredHolder<R, I> holder = DeferredHolder.create(registry.key(), Identifier.fromNamespaceAndPath(modId, name));
		entries.add(new PendingEntry<>(holder, valueSupplier));
		return holder;
	}

	/** Writes every declared entry into the registry and binds the holders. */
	public void register() {
		for (Pending<R> entry : entries) {
			entry.write(registry);
		}
		entries.clear();
	}

	private interface Pending<R> {
		void write(Registry<R> registry);
	}

	private static final class PendingEntry<R, I extends R> implements Pending<R> {
		private final DeferredHolder<R, I> holder;
		private final Supplier<? extends I> valueSupplier;

		private PendingEntry(DeferredHolder<R, I> holder, Supplier<? extends I> valueSupplier) {
			this.holder = holder;
			this.valueSupplier = valueSupplier;
		}

		@Override
		public void write(Registry<R> registry) {
			I value = valueSupplier.get();
			Registry.register(registry, holder.getId(), value);
			holder.bind(value);
		}
	}
}
