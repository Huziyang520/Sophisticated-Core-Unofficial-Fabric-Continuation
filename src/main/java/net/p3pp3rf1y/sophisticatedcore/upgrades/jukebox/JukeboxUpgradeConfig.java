package net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox;

import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;

public class JukeboxUpgradeConfig {
	public final ConfigSpec.IntValue numberOfSlots;
	public final ConfigSpec.IntValue slotsInRow;

	public JukeboxUpgradeConfig(ConfigSpec.Builder builder, String upgradeName, String path, int defaultNumberOfSlots) {
		builder.comment(upgradeName + " Settings").push(path);
		numberOfSlots = builder.comment("Number of slots for discs in jukebox upgrade").defineInRange("numberOfSlots", defaultNumberOfSlots, 1, 16);
		slotsInRow = builder.comment("Number of lots displayed in a row").defineInRange("slotsInRow", 4, 1, 6);

		builder.pop();
	}
}
