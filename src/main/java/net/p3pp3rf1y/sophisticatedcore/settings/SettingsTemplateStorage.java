package net.p3pp3rf1y.sophisticatedcore.settings;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;

import java.util.*;

public class SettingsTemplateStorage extends SavedData {
	private static final net.minecraft.world.level.saveddata.SavedDataType<SettingsTemplateStorage> SAVED_DATA_TYPE =
			new net.minecraft.world.level.saveddata.SavedDataType<>(
					SophisticatedCore.getRL("settings_templates"),
					SettingsTemplateStorage::new,
					CompoundTag.CODEC.xmap(SettingsTemplateStorage::fromTag, SettingsTemplateStorage::toTag),
					net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_MAP_DATA);
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

	/**
	 * 26.3 的 SavedData 只能通过 {@link net.minecraft.world.level.saveddata.SavedDataType} 登记，
	 * 所以这里保留 NBT 读写作为 codec 的两端。
	 */
	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		NBTHelper.putMap(tag, "playerTemplates", playerTemplates, UUID::toString, slotTemplates -> NBTHelper.putMap(new CompoundTag(), "slotTemplates", slotTemplates, String::valueOf, settingsTag -> settingsTag));
		NBTHelper.putMap(tag, "playerNamedTemplates", playerNamedTemplates, UUID::toString, namedTemplates -> NBTHelper.putMap(new CompoundTag(), "namedTemplates", namedTemplates, v -> v, settingsTag -> settingsTag));
		return tag;
	}

	public static SettingsTemplateStorage get() {
		if (SophisticatedCore.isLogicalServerThread()) {
			MinecraftServer server = SophisticatedCore.getCurrentServer();
			if (server != null) {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				//noinspection ConstantConditions - by this time overworld is loaded
				net.minecraft.world.level.storage.SavedDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(SAVED_DATA_TYPE);
			}
		}
		return clientStorageCopy;
	}

	private static SettingsTemplateStorage fromTag(CompoundTag tag) {
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
