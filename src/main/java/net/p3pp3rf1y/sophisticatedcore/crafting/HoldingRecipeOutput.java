package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nullable;

/**
 * Captures the recipe a builder produced without writing it anywhere.
 * <p>
 * 26.3 keys recipes by {@link ResourceKey} and exposes registry lookups through the output, so the
 * wrapped output is kept and its lookups are forwarded.
 */
public class HoldingRecipeOutput implements RecipeOutput {
	private final RecipeOutput wrapped;
	private final Advancement.Builder advancement;
	private Recipe<?> recipe;
	@Nullable
	private ResourceKey<Recipe<?>> recipeKey;
	@Nullable
	private AdvancementHolder advancementHolder;

	public HoldingRecipeOutput(RecipeOutput wrapped) {
		this.wrapped = wrapped;
		this.advancement = wrapped.advancement();
	}

	@Override
	public Advancement.Builder advancement() {
		return advancement;
	}

	@Override
	public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, AdvancementHolder advancement) {
		this.recipeKey = id;
		this.recipe = recipe;
		this.advancementHolder = advancement;
	}

	@Override
	public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> registryKey) {
		return wrapped.lookup(registryKey);
	}

	@Override
	public <S> java.util.stream.Stream<net.minecraft.core.Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> registryKey) {
		return wrapped.listContextElements(registryKey);
	}

	public Recipe<?> getRecipe() {
		return recipe;
	}

	@Nullable
	public ResourceKey<Recipe<?>> getRecipeKey() {
		return recipeKey;
	}

	@Nullable
	public AdvancementHolder getAdvancementHolder() {
		return advancementHolder;
	}
}
