package net.p3pp3rf1y.sophisticatedcore.platform.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Binds a {@link ConfigSpec} to its file under {@code config/} and loads it.
 * <p>
 * Replaces {@code NeoForgeConfigRegistry.INSTANCE.register(modId, ModConfig.Type, spec)} from Forge Config API Port.
 * The file is named {@code <modId>-<side>.json} so the three sides stay separated the way they were before.
 */
public final class ConfigRegistry {
	private ConfigRegistry() {
	}

	public static void register(String modId, ConfigSide side, ConfigSpec spec) {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(modId + "-" + side.getFileNameSuffix() + ".json");
		spec.setModId(modId);
		spec.attach(ConfigFile.of(path));
	}
}
