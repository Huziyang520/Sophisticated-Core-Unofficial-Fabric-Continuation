package net.p3pp3rf1y.sophisticatedcore.upgrades.magnet;

import net.p3pp3rf1y.sophisticatedcore.upgrades.FilteredUpgradeConfigBase;
import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;

public class MagnetUpgradeConfig extends FilteredUpgradeConfigBase {
	public final ConfigSpec.IntValue magnetRange;

	public MagnetUpgradeConfig(ConfigSpec.Builder builder, String name, String path, int defaultFilterSlots, int defaultSlotsInRow, int defaultMagnetRange) {
		super(builder, name, path, defaultFilterSlots, defaultSlotsInRow);
		magnetRange = builder.comment("Range around storage in blocks at which magnet will pickup items").defineInRange("magnetRange", defaultMagnetRange, 1, 20);
		builder.pop();
	}
}
