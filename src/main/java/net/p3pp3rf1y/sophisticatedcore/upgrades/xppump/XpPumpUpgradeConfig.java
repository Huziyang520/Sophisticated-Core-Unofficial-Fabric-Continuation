package net.p3pp3rf1y.sophisticatedcore.upgrades.xppump;

import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;

public class XpPumpUpgradeConfig {
	public final ConfigSpec.IntValue maxXpPointsPerMending;
	public final ConfigSpec.BooleanValue mendingOn;

	public XpPumpUpgradeConfig(ConfigSpec.Builder builder) {
		builder.comment("Xp Pump Upgrade Settings").push("xpPumpUpgrade");
		mendingOn = builder.comment("Whether xp pump can mend items with mending. Set false here to turn off the feature altogether.").define("mendingOn", true);
		maxXpPointsPerMending = builder.comment("How many experience points at a maximum would be used to mend an item per operation (every 5 ticks and 1 xp point usually translates to 2 damage repaired).").defineInRange("maxXpPointsPerMending", 5, 1, 20);
		builder.pop();
	}
}
