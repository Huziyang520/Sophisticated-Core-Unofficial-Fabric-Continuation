package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

/**
 * Reads the burn time of a fuel stack.
 * <p>
 * 26.3 removed the Fabric {@code FuelRegistry}; fuel is now the {@code COOKING_FUEL} data component.
 * Only constant burn times can be resolved without a loot context, which covers every vanilla fuel
 * and any datapack that writes a plain number.
 */
public final class FuelHelper {
	private FuelHelper() {
	}

	public static int getBurnTime(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
		if (fuel == null) {
			return 0;
		}
		ResolvableInt burnTime = fuel.burnTime();
		return burnTime instanceof ResolvableInt.Constant constant ? constant.value() : 0;
	}

	public static boolean isFuel(ItemStack stack) {
		return getBurnTime(stack) > 0;
	}
}
