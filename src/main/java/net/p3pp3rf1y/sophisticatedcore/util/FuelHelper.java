package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public final class FuelHelper {
	private FuelHelper() {
	}

	/**
	 * Returns the burn time of a stack in ticks, or 0 when it is not a fuel.
	 * <p>
	 * 26.3 turned fuel into the {@code cooking_fuel} data component and resolves its burn time against a loot context,
	 * which only the furnace has. Constant values, which is what vanilla fuel definitions use, can be read directly; a
	 * context backed provider cannot, so it reports 0 and the item is simply not treated as fuel.
	 */
	public static int getBurnTime(ItemStack stack) {
		CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
		if (fuel == null) {
			return 0;
		}

		ResolvableInt burnTime = fuel.burnTime();
		if (burnTime instanceof ResolvableInt.Constant constant) {
			return Math.max(0, constant.value());
		}

		return 0;
	}

	public static boolean isFuel(ItemStack stack) {
		return getBurnTime(stack) > 0;
	}
}
