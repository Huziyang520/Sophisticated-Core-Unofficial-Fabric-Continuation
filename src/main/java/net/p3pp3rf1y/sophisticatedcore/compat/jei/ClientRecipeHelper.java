package net.p3pp3rf1y.sophisticatedcore.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Client side recipe helpers used by the JEI plugin.
 * <p>
 * 26.3 keeps recipes on the server only; the client just holds synced {@code RecipeDisplayEntry} values, so a client
 * side recipe registry cannot be enumerated or looked up by id any more. Everything that needs the recipe objects now
 * has to come from JEI's own recipe manager, which is what the plugin does. The methods are kept so the plugin's call
 * sites compile, and report nothing found.
 */
public class ClientRecipeHelper {
	private ClientRecipeHelper() {
	}

	public static <T extends Recipe<?>> Optional<RecipeHolder<T>> getCraftingRecipeByKey(RecipeType<T> type, Identifier recipeKey) {
		return Optional.empty();
	}

	public static <I extends RecipeInput, T extends Recipe<I>, U extends Recipe<?>> List<RecipeHolder<T>> transformAllRecipesOfType(
			RecipeType<T> recipeType, Class<U> filterRecipeClass, Function<U, T> transformRecipe) {
		return Collections.emptyList();
	}

	public static <I extends RecipeInput, T extends Recipe<I>, U extends Recipe<?>> List<RecipeHolder<T>> transformAllRecipesOfTypeIntoMultiple(
			RecipeType<T> recipeType, Class<U> filterRecipeClass, Function<U, List<RecipeHolder<T>>> transformRecipe) {
		return Collections.emptyList();
	}

	public static CraftingRecipe copyShapedRecipe(ShapedRecipe recipe) {
		return recipe;
	}

	public static CraftingRecipe copyShapelessRecipe(ShapelessRecipe recipe) {
		return recipe;
	}

	/** Resolves a recipe's primary output through its display, since {@code getResultItem} no longer exists. */
	public static ItemStack getResultItem(Recipe<?> recipe) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return ItemStack.EMPTY;
		}

		List<RecipeDisplay> displays = recipe.display();
		return displays.isEmpty() ? ItemStack.EMPTY : displays.getFirst().result().resolveForFirstStack(SlotDisplayContext.fromLevel(level));
	}

	public static <I extends RecipeInput> ItemStack assemble(Recipe<I> recipe, I container) {
		return recipe.assemble(container);
	}
}
