package net.p3pp3rf1y.sophisticatedcore.platform.config;

import org.jetbrains.annotations.Nullable;

/** Which side a config file belongs to; mirrors the config types the mod registered before. */
public enum ConfigSide {
	CLIENT("client"),
	COMMON("common"),
	SERVER("server");

	private final String fileNameSuffix;

	ConfigSide(String fileNameSuffix) {
		this.fileNameSuffix = fileNameSuffix;
	}

	public String getFileNameSuffix() {
		return fileNameSuffix;
	}

	/** @return the side for a stored name, or {@code null} when it is not one of ours. */
	@Nullable
	public static ConfigSide byName(String name) {
		for (ConfigSide side : values()) {
			if (side.name().equalsIgnoreCase(name)) {
				return side;
			}
		}
		return null;
	}
}
