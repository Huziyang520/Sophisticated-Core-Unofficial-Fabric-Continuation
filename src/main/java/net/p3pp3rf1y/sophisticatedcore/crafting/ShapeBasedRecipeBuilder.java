package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

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
	public void save(RecipeOutput recipeOutput) {
		save(recipeOutput, net.minecraft.data.recipes.RecipeBuilder.getDefaultRecipeId(sophisticatedCore_resultStack()));
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> recipeKey) {
		HoldingRecipeOutput holdingRecipeOutput = new HoldingRecipeOutput(recipeOutput);
		super.save(holdingRecipeOutput, recipeKey);

		if (!(holdingRecipeOutput.getRecipe() instanceof ShapedRecipe compose)) {
			return;
		}

		withConditions(recipeOutput, new ItemEnabledCondition(getResult())).accept(recipeKey, factory.apply(compose), holdingRecipeOutput.getAdvancementHolder());
	}

	protected RecipeOutput withConditions(final RecipeOutput exporter, final ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return new RecipeOutput() {
			@Override
			public void accept(ResourceKey<Recipe<?>> location, Recipe<?> recipe, AdvancementHolder advancement) {
				FabricDataGenHelper.addConditions(recipe, conditions);
				exporter.accept(location, recipe, advancement);
			}

			public Advancement.Builder advancement() {
				return exporter.advancement();
			}

			@Override
			public <S> net.minecraft.core.HolderGetter<S> lookup(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends S>> registryKey) {
				return exporter.lookup(registryKey);
			}

			@Override
			public <S> java.util.stream.Stream<net.minecraft.core.Holder.Reference<S>> listContextElements(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends S>> registryKey) {
				return exporter.listContextElements(registryKey);
			}
		};
	}
}
