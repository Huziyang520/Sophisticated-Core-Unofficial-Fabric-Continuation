package net.p3pp3rf1y.sophisticatedcore.platform.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fires {@code reloading} callbacks when a config file is loaded or saved.
 * <p>
 * Replaces the {@code NeoForgeModConfigEvents.reloading(...)} hook the mod used through Forge Config API Port.
 * Listeners are plain {@link Runnable}s because only the mod's own classes consume them.
 */
public final class ConfigEvents {
	private static final Map<String, List<Runnable>> RELOAD_LISTENERS = new ConcurrentHashMap<>();

	private ConfigEvents() {
	}

	/** @return a registrar for reload callbacks of {@code modId}. */
	public static Registrar reloading(String modId) {
		return new Registrar(modId);
	}

	static void fireReloaded(String modId) {
		RELOAD_LISTENERS.getOrDefault(modId, List.of()).forEach(Runnable::run);
	}

	public static final class Registrar {
		private final String modId;

		private Registrar(String modId) {
			this.modId = modId;
		}

		public void register(Runnable listener) {
			RELOAD_LISTENERS.computeIfAbsent(modId, key -> new CopyOnWriteArrayList<>()).add(listener);
		}
	}
}
