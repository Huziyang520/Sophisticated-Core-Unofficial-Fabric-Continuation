package net.p3pp3rf1y.sophisticatedcore.upgrades.cooking;

import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;

public class CookingUpgradeConfig {
	public final ConfigSpec.DoubleValue cookingSpeedMultiplier;
	public final ConfigSpec.DoubleValue fuelEfficiencyMultiplier;

	public CookingUpgradeConfig(ConfigSpec.Builder builder, final String upgradeName, String path) {
		builder.comment(upgradeName + " Settings").push(path);
		cookingSpeedMultiplier = builder.comment("Smelting speed multiplier (1.0 equals speed at which vanilla furnace smelts items)")
				.defineInRange("smeltingSpeedMultiplier", 1.0D, 0.25D, 4.0D);
		fuelEfficiencyMultiplier = builder.comment("Fuel efficiency multiplier (1.0 equals speed at which it's used in vanilla furnace)")
				.defineInRange("fuelEfficiencyMultiplier", 1.0D, 0.25D, 4.0D);
	}

	public static CookingUpgradeConfig getInstance(ConfigSpec.Builder builder, final String upgradeName, String path) {
		CookingUpgradeConfig instance = new CookingUpgradeConfig(builder, upgradeName, path);
		builder.pop();
		return instance;
	}
}
