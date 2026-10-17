package net.p3pp3rf1y.sophisticatedcore.platform.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Declares a config file and holds its values.
 * <p>
 * Replacement for NeoForge's {@code ModConfigSpec}, which the mod used through Forge Config API Port.
 * The declaration API is kept as close to the original as possible so the config classes read the same;
 * the values are stored in JSON through {@link ConfigFile}.
 * <p>
 * Paths are built the same way as before: {@link Builder#push(String)} opens a category, {@link Builder#pop()}
 * closes it, and every {@code define*} records the current path plus the value name. That keeps the original
 * grouping — and therefore the file layout under {@code config/} — recognisable.
 */
public final class ConfigSpec {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSpec.class);

	private final List<ConfigValue<?>> values = new ArrayList<>();
	private final Map<String, String> categoryComments = new LinkedHashMap<>();
	private final List<String> categories = new ArrayList<>();

	private ConfigFile file;
	private String modId = "";
	private boolean loaded;
	private volatile boolean dirty;

	private ConfigSpec() {
	}

	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Persists every value to disk.
	 * <p>
	 * This deliberately does <b>not</b> fire the reload callbacks: a write is not a reload, and firing them here
	 * made a plain value write re-enter the very caches the writer was iterating - the enabled-items map cleared
	 * itself from inside its own {@code computeIfAbsent}, which deadlocks the worker threads that load recipes.
	 */
	public void save() {
		if (file == null) {
			return;
		}
		dirty = false;
		values.forEach(value -> file.put(value.getPath(), value.toJson()));
		file.save();
	}

	/** @return whether a value was written since the last {@link #save()}. */
	public boolean isDirty() {
		return dirty;
	}

	/** Marks the spec as needing a write; the write itself is deferred to {@link #save()}. */
	void markDirty() {
		dirty = true;
	}

	public List<ConfigValue<?>> getValues() {
		return List.copyOf(values);
	}

	/** @return the comment attached to the category at {@code path}, or an empty string. */
	public String getCategoryComment(List<String> path) {
		return categoryComments.getOrDefault(String.join(".", path), "");
	}

	/** @return the categories in declaration order, outermost first. */
	public List<String> getCategories() {
		return List.copyOf(categories);
	}

	/** Binds this spec to a mod id; used for the reload event. */
	void setModId(String modId) {
		this.modId = modId;
	}

	/** Loads the file (when present) into the declared values. Values missing from the file keep their default. */
	void attach(ConfigFile file) {
		this.file = file;
		file.load();
		values.forEach(value -> value.load(file));
		loaded = true;
		ConfigEvents.fireReloaded(modId);
	}

	private void recordCategory(List<String> path, String comment) {
		String key = String.join(".", path);
		if (!categoryComments.containsKey(key)) {
			categories.add(key);
		}
		if (!comment.isEmpty()) {
			categoryComments.put(key, comment);
		}
	}

	/** Builder mirroring {@code ModConfigSpec.Builder}. */
	public static final class Builder {
		private final ConfigSpec spec = new ConfigSpec();
		private final Deque<String> path = new ArrayDeque<>();
		private String comment = "";
		private boolean requiresRestart;

		public Builder comment(String comment) {
			this.comment = comment;
			return this;
		}

		public Builder push(String category) {
			if (comment.isEmpty()) {
				comment = category;
			}
			path.addLast(category);
			spec.recordCategory(new ArrayList<>(path), comment);
			comment = "";
			return this;
		}

		public Builder pop() {
			if (!path.isEmpty()) {
				path.removeLast();
			}
			requiresRestart = false;
			return this;
		}

		public Builder worldRestart() {
			requiresRestart = true;
			return this;
		}

		public BooleanValue define(String name, boolean defaultValue) {
			BooleanValue value = new BooleanValue(fullPath(name), category(), comment, requiresRestart, defaultValue);
			comment = "";
			return add(value);
		}

		public IntValue define(String name, int defaultValue) {
			IntValue value = new IntValue(fullPath(name), category(), comment, requiresRestart, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
			comment = "";
			return add(value);
		}

		public DoubleValue define(String name, double defaultValue) {
			DoubleValue value = new DoubleValue(fullPath(name), category(), comment, requiresRestart, defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE);
			comment = "";
			return add(value);
		}

		public ConfigValue<String> define(String name, String defaultValue) {
			ConfigValue<String> value = new ConfigValue<>(String.class, fullPath(name), category(), comment, requiresRestart, defaultValue);
			comment = "";
			return add(value);
		}

		public <T> ConfigValue<List<T>> define(String name, List<T> defaultValue) {
			ConfigValue<List<T>> value = ConfigValue.listOf(fullPath(name), category(), comment, requiresRestart, defaultValue);
			comment = "";
			return add(value);
		}

		/** List with a lazily evaluated default and a per element validator. */
		public <T> ConfigValue<List<T>> define(String name, Supplier<? extends Collection<T>> defaultSupplier, Predicate<Object> validator) {
			ConfigValue<List<T>> value = ConfigValue.listOf(fullPath(name), category(), comment, requiresRestart, new ArrayList<>(defaultSupplier.get()));
			comment = "";
			return add(value);
		}

		public IntValue defineInRange(String name, int defaultValue, int min, int max) {
			IntValue value = new IntValue(fullPath(name), category(), comment, requiresRestart, defaultValue, min, max);
			comment = "";
			return add(value);
		}

		public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
			DoubleValue value = new DoubleValue(fullPath(name), category(), comment, requiresRestart, defaultValue, min, max);
			comment = "";
			return add(value);
		}

		public <E extends Enum<E>> EnumValue<E> defineEnum(String name, E defaultValue) {
			EnumValue<E> value = new EnumValue<>(fullPath(name), category(), comment, requiresRestart, defaultValue);
			comment = "";
			return add(value);
		}

		/** List of strings with a per element validator, mirroring {@code ModConfigSpec.Builder#defineList}. */
		public ConfigValue<List<String>> defineList(String name, Supplier<? extends Collection<String>> defaultSupplier, Predicate<Object> validator) {
			return define(name, defaultSupplier, validator);
		}

		public ConfigValue<List<String>> defineList(String name, List<String> defaultValue, Predicate<Object> validator) {
			ConfigValue<List<String>> value = ConfigValue.listOf(fullPath(name), category(), comment, requiresRestart, defaultValue);
			comment = "";
			return add(value);
		}

		/** Runs {@code factory} against this builder, the way the mod's static initialisers expect. */
		public <T> Pair<T, ConfigSpec> configure(Function<Builder, T> factory) {
			return Pair.of(factory.apply(this), spec);
		}

		private <V extends ConfigValue<?>> V add(V value) {
			value.spec = spec;
			spec.values.add(value);
			return value;
		}

		private List<String> fullPath(String name) {
			List<String> ret = new ArrayList<>(path);
			ret.add(name);
			return ret;
		}

		private String category() {
			return String.join(".", path);
		}
	}

	/** A single declared value. {@link #set} persists immediately, matching how the Cloth Config screen applies edits. */
	public static class ConfigValue<T> implements Supplier<T> {
		private final Class<T> type;
		private final List<String> path;
		private final String category;
		private final String comment;
		private final boolean requiresRestart;
		private final T defaultValue;
		// 包级可见：同顶层的 Builder / EnumValue 需要直接读写（经类型变量访问 private 成员是非法的）
		ConfigSpec spec;
		T value;

		ConfigValue(Class<T> type, List<String> path, String category, String comment, boolean requiresRestart, T defaultValue) {
			this.type = type;
			this.path = path;
			this.category = category;
			this.comment = comment;
			this.requiresRestart = requiresRestart;
			this.defaultValue = defaultValue;
		}

		static <T> ConfigValue<List<T>> listOf(List<String> path, String category, String comment, boolean requiresRestart, List<T> defaultValue) {
			@SuppressWarnings("unchecked")
			Class<List<T>> type = (Class<List<T>>) (Class<?>) List.class;
			return new ConfigValue<>(type, path, category, comment, requiresRestart, defaultValue);
		}

		@Override
		public T get() {
			return value != null ? value : defaultValue;
		}

		public void set(T newValue) {
			this.value = newValue;
			if (spec != null) {
				spec.markDirty();
			}
		}

		public T getDefault() {
			return defaultValue;
		}

		public List<String> getPath() {
			return List.copyOf(path);
		}

		public String getCategory() {
			return category;
		}

		public String getComment() {
			return comment;
		}

		public boolean requiresRestart() {
			return requiresRestart;
		}

		public boolean isList() {
			return List.class.isAssignableFrom(type);
		}

		@SuppressWarnings("unchecked")
		void load(ConfigFile file) {
			JsonElement stored = file.get(path);
			if (stored == null) {
				return;
			}

			Object parsed = ConfigFile.fromJson(stored);
			if (List.class.isAssignableFrom(type)) {
				value = (T) parsed;
			} else if (type.isInstance(parsed)) {
				value = (T) parsed;
			} else if (parsed instanceof Number number && type == Double.class) {
				value = (T) Double.valueOf(number.doubleValue());
			} else if (parsed instanceof Number number && type == Integer.class) {
				value = (T) Integer.valueOf(number.intValue());
			} else {
				LOGGER.warn("Ignoring value {} in config: expected {} but found {}", path, type.getSimpleName(), parsed);
			}
		}

		JsonElement toJson() {
			return ConfigFile.toJson(get());
		}
	}

	public static class BooleanValue extends ConfigValue<Boolean> {
		BooleanValue(List<String> path, String category, String comment, boolean requiresRestart, boolean defaultValue) {
			super(Boolean.class, path, category, comment, requiresRestart, defaultValue);
		}
	}

	public static class IntValue extends ConfigValue<Integer> {
		private final int min;
		private final int max;

		IntValue(List<String> path, String category, String comment, boolean requiresRestart, int defaultValue, int min, int max) {
			super(Integer.class, path, category, comment, requiresRestart, defaultValue);
			this.min = min;
			this.max = max;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}
	}

	public static class DoubleValue extends ConfigValue<Double> {
		private final double min;
		private final double max;

		DoubleValue(List<String> path, String category, String comment, boolean requiresRestart, double defaultValue, double min, double max) {
			super(Double.class, path, category, comment, requiresRestart, defaultValue);
			this.min = min;
			this.max = max;
		}

		public double getMin() {
			return min;
		}

		public double getMax() {
			return max;
		}
	}

	public static class EnumValue<E extends Enum<E>> extends ConfigValue<E> {
		private final Class<E> enumClass;

		@SuppressWarnings("unchecked")
		EnumValue(List<String> path, String category, String comment, boolean requiresRestart, E defaultValue) {
			super((Class<E>) defaultValue.getClass(), path, category, comment, requiresRestart, defaultValue);
			this.enumClass = (Class<E>) defaultValue.getClass();
		}

		public Class<E> getEnumClass() {
			return enumClass;
		}

		/** Sets this value from a raw enum constant; used by the generic config screen. */
		@SuppressWarnings({"rawtypes", "unchecked"})
		public void setRaw(Enum<?> newValue) {
			set((E) newValue);
		}

		@Override
		JsonElement toJson() {
			return new JsonPrimitive(get().name());
		}

		@Override
		void load(ConfigFile file) {
			JsonElement stored = file.get(getPath());
			if (stored == null || !stored.isJsonPrimitive()) {
				return;
			}

			String name = stored.getAsString();
			for (E candidate : enumClass.getEnumConstants()) {
				if (candidate.name().equals(name)) {
					value = candidate;
					return;
				}
			}
			LOGGER.warn("Ignoring value {} in config: {} is not a valid {}", getPath(), name, enumClass.getSimpleName());
		}
	}
}
