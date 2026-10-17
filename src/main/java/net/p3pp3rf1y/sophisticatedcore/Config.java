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
		private boolean configChanged = false;
		public final EnabledItems enabledItems;

		public void initListeners() {
			ConfigEvents.reloading(SophisticatedCore.MOD_ID).register(this::onConfigReload);
		}

		@SuppressWarnings("unused") //need the Event parameter for forge reflection to understand what event this listens to
		public void onConfigReload() {
			enabledItems.enabledMap.clear();
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
			private final ConfigSpec.ConfigValue<List<String>> itemsEnableList;
			private final Runnable onConfigChange;
			private final Map<Identifier, Boolean> enabledMap = new ConcurrentHashMap<>();

			EnabledItems(ConfigSpec.Builder builder, Runnable onConfigChange) {
				itemsEnableList = builder.comment("Disable / enable any items here (disables their recipes)").define("enabledItems", new ArrayList<>());
				this.onConfigChange = onConfigChange;
			}

			public boolean isItemEnabled(Item item) {
				return RegistryHelper.getRegistryName(BuiltInRegistries.ITEM, item).map(this::isItemEnabled).orElse(false);
			}

			public boolean isItemEnabled(Identifier itemRegistryName) {
				if (!COMMON_SPEC.isLoaded()) {
					return true;
				}
				if (enabledMap.isEmpty()) {
					loadEnabledMap();
				}
				return enabledMap.computeIfAbsent(itemRegistryName, irn -> {
					addEnabledItemToConfig(itemRegistryName);
					return true;
				});
			}

			private void addEnabledItemToConfig(Identifier itemRegistryName) {
				List<String> list = itemsEnableList.get();
				list.add(itemRegistryName + "|true");
				itemsEnableList.set(list);
				onConfigChange.run();
			}

			private void loadEnabledMap() {
				for (String itemEnabled : itemsEnableList.get()) {
					String[] data = itemEnabled.split("\\|");
					if (data.length == 2) {
						enabledMap.put(Identifier.parse(data[0]), Boolean.valueOf(data[1]));
					} else {
						SophisticatedCore.LOGGER.error("Wrong data for enabledItems - expected registry name|true/false when {} was provided", itemEnabled);
					}
				}
			}
		}
	}
}
