package net.p3pp3rf1y.sophisticatedcore.upgrades.cooking;

import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;

public class AutoCookingUpgradeConfig extends CookingUpgradeConfig {
	public final ConfigSpec.IntValue inputFilterSlots;
	public final ConfigSpec.IntValue inputFilterSlotsInRow;
	public final ConfigSpec.IntValue fuelFilterSlots;
	public final ConfigSpec.IntValue fuelFilterSlotsInRow;

	public AutoCookingUpgradeConfig(ConfigSpec.Builder builder, String upgradeName, String path) {
		super(builder, upgradeName, path);
		inputFilterSlots = builder.comment("Number of input filter slots").defineInRange("inputFilterSlots", 8, 1, 20);
		inputFilterSlotsInRow = builder.comment("Number of input filter slots displayed in a row").defineInRange("inputFilterSlotsInRow", 4, 1, 6);
		fuelFilterSlots = builder.comment("Number of fuel filter slots").defineInRange("fuelFilterSlots", 4, 1, 20);
		fuelFilterSlotsInRow = builder.comment("Number of fuel filter slots displayed in a row").defineInRange("fuelFilterSlotsInRow", 4, 1, 6);
		builder.pop();
	}
}
