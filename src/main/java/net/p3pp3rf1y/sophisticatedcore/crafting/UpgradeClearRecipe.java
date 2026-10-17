package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;

/** Clears the stored data of a single upgrade, turning it back into a plain item. */
public class UpgradeClearRecipe extends CustomRecipe {
	public UpgradeClearRecipe() {
		super();
	}

	@Override
	public boolean matches(CraftingInput inventory, Level level) {
		boolean upgradePresent = false;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof UpgradeItemBase && !stack.getComponents().isEmpty() && !upgradePresent) {
					upgradePresent = true;
				} else {
					return false;
				}
			}
		}

		return upgradePresent;
	}

	@Override
	public ItemStack assemble(CraftingInput inventory) {
		ItemStack upgrade = ItemStack.EMPTY;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItemBase) {
				upgrade = stack;
			}
		}
		return new ItemStack(upgrade.getItem(), 1);
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModRecipes.UPGRADE_CLEAR_SERIALIZER.get();
	}
}
