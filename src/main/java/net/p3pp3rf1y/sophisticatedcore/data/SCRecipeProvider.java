package net.p3pp3rf1y.sophisticatedcore.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeClearRecipe;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen entry for the core's recipes.
 * <p>
 * 26.3 replaced the {@code buildRecipes(RecipeOutput)} hook with a {@link RecipeProvider}
 * factory, so the recipe body lives inside that provider.
 */
public class SCRecipeProvider extends FabricRecipeProvider {
	public SCRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries,
			BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
		return new RecipeProvider(recipeOutput, advancementOutput) {
			@Override
			protected void buildRecipes() {
				SpecialRecipeBuilder.special(() -> new UpgradeClearRecipe(CraftingBookCategory.MISC))
						.save(output, ResourceKey.create(Registries.RECIPE, SophisticatedCore.getRL("upgrade_clear")));
			}
		};
	}
}
