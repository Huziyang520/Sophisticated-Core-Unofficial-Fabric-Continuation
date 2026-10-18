package net.p3pp3rf1y.sophisticatedcore.platform.config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a Cloth Config screen from a {@link ConfigSpec}.
 * <p>
 * This is the replacement for the NeoForge config screen: categories are laid out as a left hand tree
 * ({@code setGlobalized} + {@code setGlobalizedExpanded}), every entry starts from the current value rather
 * than the default, and edits are written straight back through the value's setter.
 */
public final class ConfigScreens {
	private ConfigScreens() {
	}

	/** @return a ready to display screen for {@code spec}; {@code parent} is shown when the screen closes. */
	public static Screen create(Component title, ConfigSpec spec, @Nullable Screen parent) {
		return create(title, parent, List.of(spec));
	}

	/**
	 * @return one screen covering every value of {@code specs}, saving all of them when it closes.
	 * <p>
	 * A mod usually owns more than one spec (a common one and a client one), and Mod Menu only offers a single
	 * config entry per mod, so the specs share one screen instead of hiding one behind a second button.
	 */
	public static Screen create(Component title, @Nullable Screen parent, List<ConfigSpec> specs) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setTitle(title)
				.setSavingRunnable(() -> specs.forEach(ConfigSpec::save));
		if (parent != null) {
			builder.setParentScreen(parent);
		}
		// 顶栏标签页：配置节动辄二十多个，左侧树状栏会挤成一列难以阅读的滚动列表
		builder.setAlwaysShowTabs(true);

		ConfigEntryBuilder entries = ConfigEntryBuilder.create();
		for (ConfigSpec spec : specs) {
			Map<String, List<ConfigSpec.ConfigValue<?>>> byCategory = new LinkedHashMap<>();
			spec.getValues().forEach(value -> byCategory.computeIfAbsent(value.getCategory(), key -> new ArrayList<>()).add(value));
			byCategory.forEach((category, values) -> {
				ConfigCategory clothCategory = builder.getOrCreateCategory(categoryTitle(spec, category));
				values.forEach(value -> clothCategory.addEntry(createEntry(spec, value, entries)));
			});
		}

		return builder.build();
	}

	private static Component categoryTitle(ConfigSpec spec, String category) {
		if (category.isEmpty()) {
			return Component.translatable("config." + spec.getModId() + ".general");
		}

		// The key mirrors the config path; the fallback keeps the screen readable when a language file has no entry,
		// instead of printing the raw key like the previous version did.
		return Component.translatableWithFallback("config." + spec.getModId() + "." + category, humanize(lastSegment(category)));
	}

	/** Turns {@code smeltingSpeedMultiplier} into {@code Smelting Speed Multiplier}. */
	static String humanize(String camelCase) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < camelCase.length(); i++) {
			char c = camelCase.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				out.append(' ');
			}
			out.append(i == 0 ? Character.toUpperCase(c) : c);
		}
		return out.toString();
	}

	private static String lastSegment(String path) {
		int dot = path.lastIndexOf('.');
		return dot < 0 ? path : path.substring(dot + 1);
	}

	/** @return the translation key of an entry, e.g. {@code config.sophisticatedcore.common.enabledItems}. */
	static String entryKey(ConfigSpec spec, ConfigSpec.ConfigValue<?> value) {
		return "config." + spec.getModId() + "." + String.join(".", value.getPath());
	}

	@SuppressWarnings("unchecked")
	private static AbstractConfigListEntry<?> createEntry(ConfigSpec spec, ConfigSpec.ConfigValue<?> value, ConfigEntryBuilder entries) {
		Component label = Component.translatableWithFallback(entryKey(spec, value), humanize(value.getPath().get(value.getPath().size() - 1)));
		Component[] tooltip = value.getComment().isEmpty() ? new Component[0]
				: new Component[]{Component.translatableWithFallback(entryKey(spec, value) + ".tooltip", value.getComment())};
		boolean restart = value.requiresRestart();

		if (value instanceof ConfigSpec.BooleanValue boolValue) {
			var entry = entries.startBooleanToggle(label, boolValue.get())
					.setDefaultValue(boolValue.getDefault())
					.setSaveConsumer(boolValue::set);
			if (tooltip.length > 0) {
				entry.setTooltip(tooltip);
			}
			if (restart) {
				entry.requireRestart(true);
			}
			return entry.build();
		}

		if (value instanceof ConfigSpec.IntValue intValue) {
			var entry = entries.startIntField(label, intValue.get())
					.setDefaultValue(intValue.getDefault())
					.setSaveConsumer(intValue::set);
			if (intValue.getMin() != Integer.MIN_VALUE) {
				entry.setMin(intValue.getMin());
			}
			if (intValue.getMax() != Integer.MAX_VALUE) {
				entry.setMax(intValue.getMax());
			}
			if (tooltip.length > 0) {
				entry.setTooltip(tooltip);
			}
			if (restart) {
				entry.requireRestart(true);
			}
			return entry.build();
		}

		if (value instanceof ConfigSpec.DoubleValue doubleValue) {
			var entry = entries.startDoubleField(label, doubleValue.get())
					.setDefaultValue(doubleValue.getDefault())
					.setSaveConsumer(doubleValue::set);
			if (doubleValue.getMin() != -Double.MAX_VALUE) {
				entry.setMin(doubleValue.getMin());
			}
			if (doubleValue.getMax() != Double.MAX_VALUE) {
				entry.setMax(doubleValue.getMax());
			}
			if (tooltip.length > 0) {
				entry.setTooltip(tooltip);
			}
			if (restart) {
				entry.requireRestart(true);
			}
			return entry.build();
		}

		if (value instanceof ConfigSpec.EnumValue<?> enumValue) {
			return enumEntry(label, enumValue, entries, tooltip, restart);
		}

		if (value.isList()) {
			ConfigSpec.ConfigValue<List<String>> listValue = (ConfigSpec.ConfigValue<List<String>>) value;
			var entry = entries.startStrList(label, new ArrayList<>(listValue.get()))
					.setDefaultValue(listValue.getDefault())
					.setSaveConsumer(listValue::set);
			if (tooltip.length > 0) {
				entry.setTooltip(tooltip);
			}
			if (restart) {
				entry.requireRestart(true);
			}
			return entry.build();
		}

		ConfigSpec.ConfigValue<String> stringValue = (ConfigSpec.ConfigValue<String>) value;
		var entry = entries.startStrField(label, stringValue.get())
				.setDefaultValue(stringValue.getDefault())
				.setSaveConsumer(stringValue::set);
		if (tooltip.length > 0) {
			entry.setTooltip(tooltip);
		}
		if (restart) {
			entry.requireRestart(true);
		}
		return entry.build();
	}

	private static AbstractConfigListEntry<?> enumEntry(Component label, ConfigSpec.EnumValue<?> value, ConfigEntryBuilder entries,
														Component[] tooltip, boolean restart) {
		return buildEnum(label, value, entries, tooltip, restart);
	}

	private static <E extends Enum<E>> AbstractConfigListEntry<?> buildEnum(Component label, ConfigSpec.EnumValue<E> value, ConfigEntryBuilder entries,
																			Component[] tooltip, boolean restart) {
		var entry = entries.startEnumSelector(label, value.getEnumClass(), value.get())
				.setDefaultValue(value.getDefault())
				.setSaveConsumer(value::set);
		if (tooltip.length > 0) {
			entry.setTooltip(tooltip);
		}
		if (restart) {
			entry.requireRestart(true);
		}
		return entry.build();
	}
}
