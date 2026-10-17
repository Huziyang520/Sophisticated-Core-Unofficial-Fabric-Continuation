package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;

import java.util.function.Function;

/** Shaped recipe that is written out as a wrapped variant, with the item enabled condition attached. */
public class ShapeBasedRecipeBuilder extends SCShapedRecipeBuilder {
	private final Function<ShapedRecipe, ? extends ShapedRecipe> factory;

	private ShapeBasedRecipeBuilder(ItemStack result, Function<ShapedRecipe, ? extends ShapedRecipe> factory) {
		super(RecipeCategory.MISC, result);
		this.factory = factory;
	}

	public static ShapeBasedRecipeBuilder shaped(ItemStack result) {
		return new ShapeBasedRecipeBuilder(result, r -> r);
	}

	public static ShapeBasedRecipeBuilder shaped(ItemLike result) {
		return shaped(new ItemStack(result));
	}

	public static ShapeBasedRecipeBuilder shaped(ItemLike result, Function<ShapedRecipe, ? extends ShapedRecipe> factory) {
		return shaped(new ItemStack(result, 1), factory);
	}

	public static ShapeBasedRecipeBuilder shaped(ItemStack result, Function<ShapedRecipe, ? extends ShapedRecipe> factory) {
		return new ShapeBasedRecipeBuilder(result, factory);
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
		HoldingRecipeOutput holdingRecipeOutput = new HoldingRecipeOutput(recipeOutput.advancement());
		super.save(holdingRecipeOutput, id);

		if (!(holdingRecipeOutput.getRecipe() instanceof ShapedRecipe compose)) {
			return;
		}

		RecipeConditions.withConditions(recipeOutput, new ItemEnabledCondition(getResult()))
				.accept(id, factory.apply(compose), holdingRecipeOutput.getAdvancementHolder());
	}
}
