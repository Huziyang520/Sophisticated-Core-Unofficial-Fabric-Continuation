package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;

import java.util.List;
import java.util.Optional;

/**
 * Wraps a shaped recipe so the crafted upgrade keeps the components of the upgrade that was used as an ingredient.
 * <p>
 * 26.3 made the recipe base classes carry their own construction info (common info, book info, pattern, template) and
 * stopped exposing that as fields, so this delegates to the wrapped recipe instead of extending it.
 */
public class UpgradeNextTierRecipe implements CraftingRecipe, IWrapperRecipe<ShapedRecipe> {
	private final ShapedRecipe compose;

	public UpgradeNextTierRecipe(ShapedRecipe compose) {
		this.compose = compose;
	}

	@Override
	public ShapedRecipe getCompose() {
		return compose;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return compose.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack nextTier = compose.assemble(input);
		getUpgrade(input).ifPresent(upgrade -> nextTier.applyComponents(upgrade.getComponents()));
		return nextTier;
	}

	private Optional<ItemStack> getUpgrade(CraftingInput input) {
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack slotStack = input.getItem(slot);
			if (slotStack.getItem() instanceof IUpgradeItem) {
				return Optional.of(slotStack);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public String group() {
		return compose.group();
	}

	@Override
	public boolean showNotification() {
		return compose.showNotification();
	}

	@Override
	public CraftingBookCategory category() {
		return compose.category();
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return compose.recipeBookCategory();
	}

	@Override
	public PlacementInfo placementInfo() {
		return compose.placementInfo();
	}

	@Override
	public List<RecipeDisplay> display() {
		return compose.display();
	}

	@Override
	public RecipeType<CraftingRecipe> getType() {
		return compose.getType();
	}

	@Override
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER.get();
	}

	public static class Serializer {
		private Serializer() {
		}

		public static RecipeSerializer<UpgradeNextTierRecipe> create() {
			return RecipeWrapperSerializer.create(UpgradeNextTierRecipe::new, ShapedRecipe.SERIALIZER);
		}
	}
}
