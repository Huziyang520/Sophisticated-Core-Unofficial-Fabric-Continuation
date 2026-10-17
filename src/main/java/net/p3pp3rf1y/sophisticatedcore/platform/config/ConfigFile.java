package net.p3pp3rf1y.sophisticatedcore.platform.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes one config file as nested JSON, addressed by the category path plus the value name.
 * <p>
 * The values themselves live in {@link ConfigSpec}; this class only moves them on and off disk.
 */
public final class ConfigFile {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private final Path path;
	private JsonObject root = new JsonObject();

	private ConfigFile(Path path) {
		this.path = path;
	}

	static ConfigFile of(Path path) {
		return new ConfigFile(path);
	}

	public Path getPath() {
		return path;
	}

	void load() {
		if (!Files.isRegularFile(path)) {
			root = new JsonObject();
			return;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			root = parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
		} catch (IOException | RuntimeException e) {
			SophisticatedCore.LOGGER.error("Unable to read config file {}, using defaults", path, e);
			root = new JsonObject();
		}
	}

	void save() {
		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(root, writer);
			}
		} catch (IOException e) {
			SophisticatedCore.LOGGER.error("Unable to write config file {}", path, e);
		}
	}

	/** @return the stored value at {@code path}, or {@code null} when the file does not contain it. */
	JsonElement get(List<String> path) {
		JsonObject current = root;
		for (int i = 0; i < path.size() - 1; i++) {
			JsonElement child = current.get(path.get(i));
			if (child == null || !child.isJsonObject()) {
				return null;
			}
			current = child.getAsJsonObject();
		}
		return current.get(path.get(path.size() - 1));
	}

	/** Stores {@code value} at {@code path}, creating the intermediate objects. */
	void put(List<String> path, JsonElement value) {
		JsonObject current = root;
		for (int i = 0; i < path.size() - 1; i++) {
			String key = path.get(i);
			JsonElement child = current.get(key);
			if (child == null || !child.isJsonObject()) {
				JsonObject created = new JsonObject();
				current.add(key, created);
				current = created;
			} else {
				current = child.getAsJsonObject();
			}
		}
		current.add(path.get(path.size() - 1), value);
	}

	static JsonElement toJson(Object value) {
		if (value instanceof Boolean bool) {
			return GSON.toJsonTree(bool);
		}
		if (value instanceof Number number) {
			return GSON.toJsonTree(number);
		}
		if (value instanceof String string) {
			return GSON.toJsonTree(string);
		}
		if (value instanceof List<?> list) {
			JsonArray array = new JsonArray();
			list.forEach(element -> array.add(toJson(element)));
			return array;
		}
		throw new IllegalArgumentException("Unsupported config value type: " + (value == null ? "null" : value.getClass()));
	}

	static List<Object> fromJsonList(JsonElement element) {
		List<Object> ret = new ArrayList<>();
		if (element != null && element.isJsonArray()) {
			element.getAsJsonArray().forEach(child -> ret.add(fromJson(child)));
		}
		return ret;
	}

	static Object fromJson(JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return null;
		}
		if (element.isJsonArray()) {
			return fromJsonList(element);
		}
		if (element.isJsonObject()) {
			return element.getAsJsonObject().entrySet().stream()
					.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> fromJson(e.getValue())));
		}
		if (element.getAsJsonPrimitive().isBoolean()) {
			return element.getAsBoolean();
		}
		if (element.getAsJsonPrimitive().isNumber()) {
			return element.getAsNumber();
		}
		return element.getAsString();
	}
}
