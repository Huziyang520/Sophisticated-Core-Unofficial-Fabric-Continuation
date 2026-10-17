package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

/** Wraps a {@link RecipeOutput} so every recipe written through it carries the given conditions. */
public final class RecipeConditions {
	private RecipeConditions() {
	}

	public static RecipeOutput withConditions(RecipeOutput exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return new RecipeOutput() {
			@Override
			public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
				FabricDataGenHelper.addConditions(recipe, conditions);
				exporter.accept(id, recipe, advancement);
			}

			@Override
			public Advancement.Builder advancement() {
				return exporter.advancement();
			}
		};
	}
}
