package net.p3pp3rf1y.sophisticatedcore;

import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SortButtonsPosition;
import net.p3pp3rf1y.sophisticatedcore.util.RegistryHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Config {

	private Config() {}

	public static final Client CLIENT;
	public static final ConfigSpec CLIENT_SPEC;
	public static final Common COMMON;
	public static final ConfigSpec COMMON_SPEC;

	static {
		final Pair<Client, ConfigSpec> clientSpec = new ConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = clientSpec.getRight();
		CLIENT = clientSpec.getLeft();

		final Pair<Common, ConfigSpec> commonSpec = new ConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = commonSpec.getRight();
		COMMON = commonSpec.getLeft();
	}

	public static class Client {
		public final ConfigSpec.EnumValue<SortButtonsPosition> sortButtonsPosition;
		public final ConfigSpec.BooleanValue playButtonSound;

		Client(ConfigSpec.Builder builder) {
			builder.comment("Client Settings").push("client");
			sortButtonsPosition = builder.comment("Positions where sort buttons can display to help with conflicts with controls from other mods").defineEnum("sortButtonsPosition", SortButtonsPosition.TITLE_LINE_RIGHT);
			playButtonSound = builder.comment("Whether click sound should play when custom buttons are clicked in gui").define("playButtonSound", true);
			builder.pop();
		}
	}

	public static class Common {
		private volatile boolean configChanged = false;
		public final EnabledItems enabledItems;

		public void initListeners() {
			ConfigEvents.reloading(SophisticatedCore.MOD_ID).register(this::onConfigReload);
		}

		/**
		 * Rebuilds the enabled-items cache after the config file was read.
		 * <p>
		 * The cache is swapped for a fresh map rather than cleared in place: recipe loading runs on parallel worker
		 * threads, and clearing a map that another thread is populating is how the old code deadlocked.
		 */
		public void onConfigReload() {
			enabledItems.reloadFromConfig();
		}

		Common(ConfigSpec.Builder builder) {
			builder.comment("Common Settings").push("common");

			enabledItems = new EnabledItems(builder, () -> configChanged = true);
		}

		public void saveIfChanged() {
			if (configChanged) {
				configChanged = false;
				COMMON_SPEC.save();
			}
		}

		public static class EnabledItems {
			private static final String ENABLED_SUFFIX = "|true";

			private final ConfigSpec.ConfigValue<List<String>> itemsEnableList;
			private final Runnable onConfigChange;
			private final Object lock = new Object();
			private volatile Map<Identifier, Boolean> enabledMap = new ConcurrentHashMap<>();
			private boolean initialized;

			EnabledItems(ConfigSpec.Builder builder, Runnable onConfigChange) {
				itemsEnableList = builder.comment("Disable / enable any items here (disables their recipes)").define("enabledItems", new ArrayList<>());
				this.onConfigChange = onConfigChange;
			}

			public boolean isItemEnabled(Item item) {
				return RegistryHelper.getRegistryName(BuiltInRegistries.ITEM, item).map(this::isItemEnabled).orElse(false);
			}

			/**
			 * @return whether the item is enabled, recording an unknown item as enabled.
			 * <p>
			 * Nothing here may touch the config file from inside a map computation: the previous implementation
			 * persisted the entry from a {@code computeIfAbsent} callback, which re-entered this class through the
			 * reload event and blocked the loading thread forever. The write is only flagged now and happens on the
			 * next {@link Common#saveIfChanged()}.
			 */
			public boolean isItemEnabled(Identifier itemRegistryName) {
				if (!COMMON_SPEC.isLoaded()) {
					return true;
				}

				ensureInitialized();

				Boolean known = enabledMap.get(itemRegistryName);
				if (known != null) {
					return known;
				}

				synchronized (lock) {
					known = enabledMap.get(itemRegistryName);
					if (known != null) {
						return known;
					}

					enabledMap.put(itemRegistryName, Boolean.TRUE);
					recordNewItem(itemRegistryName);
					return true;
				}
			}

			private void ensureInitialized() {
				if (initialized) {
					return;
				}

				synchronized (lock) {
					if (!initialized) {
						initialized = true;
						rebuildEnabledMap();
					}
				}
			}

			/** Re-reads the config list into a fresh map, then publishes it. */
			public void reloadFromConfig() {
				synchronized (lock) {
					initialized = true;
					rebuildEnabledMap();
				}
			}

			private void rebuildEnabledMap() {
				Map<Identifier, Boolean> rebuilt = new ConcurrentHashMap<>();
				for (String itemEnabled : itemsEnableList.get()) {
					String[] data = itemEnabled.split("\\|");
					if (data.length == 2) {
						try {
							rebuilt.put(Identifier.parse(data[0]), Boolean.valueOf(data[1]));
						} catch (RuntimeException e) {
							SophisticatedCore.LOGGER.error("Wrong registry name in enabledItems - {}", itemEnabled);
						}
					} else {
						SophisticatedCore.LOGGER.error("Wrong data for enabledItems - expected registry name|true/false when {} was provided", itemEnabled);
					}
				}
				enabledMap = rebuilt;
			}

			private void recordNewItem(Identifier itemRegistryName) {
				List<String> list = new ArrayList<>(itemsEnableList.get());
				list.add(itemRegistryName + ENABLED_SUFFIX);
				itemsEnableList.set(list);
				onConfigChange.run();
			}
		}
	}
}
