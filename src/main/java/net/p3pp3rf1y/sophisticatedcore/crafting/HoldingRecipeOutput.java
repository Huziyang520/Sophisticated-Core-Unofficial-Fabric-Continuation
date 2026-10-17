package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link RecipeOutput} that remembers what was passed to {@link #accept} instead of writing it out.
 * Used to build a base recipe first and then emit a wrapped variant with conditions attached.
 */
public class HoldingRecipeOutput implements RecipeOutput {
	private final Advancement.Builder advancement;
	private Recipe<?> recipe;
	@Nullable
	private AdvancementHolder advancementHolder;

	public HoldingRecipeOutput(Advancement.Builder advancement) {
		this.advancement = advancement;
	}

	@Override
	public Advancement.Builder advancement() {
		return advancement;
	}

	@Override
	public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
		this.recipe = recipe;
		this.advancementHolder = advancement;
	}

	public Recipe<?> getRecipe() {
		return recipe;
	}

	@Nullable
	public AdvancementHolder getAdvancementHolder() {
		return advancementHolder;
	}
}
