package net.p3pp3rf1y.sophisticatedcore.settings;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;

import java.util.*;

public class SettingsTemplateStorage extends SavedData {
	/**
	 * 26.3 persists saved data through a codec instead of {@code save}/{@code load}.
	 * The tag layout is kept byte-for-byte identical to the previous format.
	 */
	private static final Codec<SettingsTemplateStorage> CODEC = CompoundTag.CODEC.xmap(
			SettingsTemplateStorage::load,
			SettingsTemplateStorage::save);

	private static final SavedDataType<SettingsTemplateStorage> TYPE = new SavedDataType<>(
			SophisticatedCore.getRL("settings_templates"),
			SettingsTemplateStorage::new,
			CODEC,
			null);

	private Map<UUID, Map<Integer, CompoundTag>> playerTemplates = new HashMap<>();
	private Map<UUID, Map<String, CompoundTag>> playerNamedTemplates = new HashMap<>();
	private static final SettingsTemplateStorage clientStorageCopy = new SettingsTemplateStorage();

	private SettingsTemplateStorage() {
	}

	private SettingsTemplateStorage(Map<UUID, Map<Integer, CompoundTag>> playerTemplates, Map<UUID, Map<String, CompoundTag>> playerNamedTemplates) {
		this.playerTemplates = playerTemplates;
		this.playerNamedTemplates = playerNamedTemplates;
	}

	public void putPlayerTemplate(Player player, int slot, CompoundTag settingsTag) {
		playerTemplates.computeIfAbsent(player.getUUID(), u -> new HashMap<>()).put(slot, settingsTag);
		setDirty();
	}

	public void putPlayerNamedTemplate(Player player, String name, CompoundTag settingsTag) {
		playerNamedTemplates.computeIfAbsent(player.getUUID(), u -> new TreeMap<>()).put(name, settingsTag);
		setDirty();
	}

	public Map<Integer, CompoundTag> getPlayerTemplates(Player player) {
		return playerTemplates.getOrDefault(player.getUUID(), new HashMap<>());
	}

	public Map<String, CompoundTag> getPlayerNamedTemplates(Player player) {
		return playerNamedTemplates.getOrDefault(player.getUUID(), new TreeMap<>());
	}

	private static CompoundTag save(SettingsTemplateStorage storage) {
		CompoundTag tag = new CompoundTag();
		NBTHelper.putMap(tag, "playerTemplates", storage.playerTemplates, UUID::toString,
				slotTemplates -> NBTHelper.putMap(new CompoundTag(), "slotTemplates", slotTemplates, String::valueOf, settingsTag -> settingsTag));
		NBTHelper.putMap(tag, "playerNamedTemplates", storage.playerNamedTemplates, UUID::toString,
				namedTemplates -> NBTHelper.putMap(new CompoundTag(), "namedTemplates", namedTemplates, v -> v, settingsTag -> settingsTag));
		return tag;
	}

	public static SettingsTemplateStorage get() {
		if (SophisticatedCore.isLogicalServerThread()) {
			MinecraftServer server = SophisticatedCore.getCurrentServer();
			if (server != null) {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				//noinspection ConstantConditions - by this time overworld is loaded
				SavedDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(TYPE);
			}
		}
		return clientStorageCopy;
	}

	private static SettingsTemplateStorage load(CompoundTag tag) {
		return new SettingsTemplateStorage(
				NBTHelper.getMap(tag, "playerTemplates", UUID::fromString,
						(key, playerTemplatesTag) -> NBTHelper.getMap((CompoundTag) playerTemplatesTag, "slotTemplates", Integer::valueOf, (k, settingsTag) -> Optional.of((CompoundTag) settingsTag))
				).orElse(new HashMap<>()),
				NBTHelper.getMap(tag, "playerNamedTemplates", UUID::fromString,
						(key, playerNamedTemplatesTag) -> NBTHelper.getMap((CompoundTag) playerNamedTemplatesTag, "namedTemplates", v -> v, (k, settingsTag) -> Optional.of((CompoundTag) settingsTag), TreeMap::new)
				).orElse(new TreeMap<>()));
	}

	public void clearPlayerTemplates(Player player) {
		playerTemplates.remove(player.getUUID());
		playerNamedTemplates.remove(player.getUUID());
		setDirty();
	}
}