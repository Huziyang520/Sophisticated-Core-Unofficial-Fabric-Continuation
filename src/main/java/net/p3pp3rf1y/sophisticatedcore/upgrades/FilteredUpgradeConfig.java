package net.p3pp3rf1y.sophisticatedcore.upgrades;

import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;

public class FilteredUpgradeConfig extends FilteredUpgradeConfigBase {
	public FilteredUpgradeConfig(ConfigSpec.Builder builder, String name, String path, int defaultFilterSlots, int defaultSlotsInRow) {
		super(builder, name, path, defaultFilterSlots, defaultSlotsInRow);
		builder.pop();
	}
}
