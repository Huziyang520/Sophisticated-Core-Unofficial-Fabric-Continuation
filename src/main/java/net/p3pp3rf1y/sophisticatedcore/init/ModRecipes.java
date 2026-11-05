package net.p3pp3rf1y.sophisticatedcore.init;

import net.p3pp3rf1y.sophisticatedcore.platform.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.p3pp3rf1y.sophisticatedcore.crafting.ItemEnabledCondition;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeClearRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;

import java.util.function.Supplier;

public class ModRecipes {
	public static final ResourceConditionType<ItemEnabledCondition> ITEM_ENABLED_CONDITION = ResourceConditionType.create(SophisticatedCore.getRL("item_enabled"), ItemEnabledCondition.CODEC);
	private ModRecipes() {
	}

	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, SophisticatedCore.MOD_ID);


	public static final Supplier<RecipeSerializer<?>> UPGRADE_NEXT_TIER_SERIALIZER = RECIPE_SERIALIZERS.register("upgrade_next_tier", UpgradeNextTierRecipe::createSerializer);
	public static final Supplier<RecipeSerializer<UpgradeClearRecipe>> UPGRADE_CLEAR_SERIALIZER = RECIPE_SERIALIZERS.register("upgrade_clear", () -> new RecipeSerializer<>(UpgradeClearRecipe.CODEC, UpgradeClearRecipe.STREAM_CODEC));


	public static void registerHandlers() {
		RECIPE_SERIALIZERS.register();
		// 26.3 keeps no recipe manager on the client; custom serializers only reach it when synced explicitly.
		RecipeSynchronization.synchronizeRecipeSerializer(UPGRADE_NEXT_TIER_SERIALIZER.get());
		RecipeSynchronization.synchronizeRecipeSerializer(UPGRADE_CLEAR_SERIALIZER.get());

		ResourceConditions.register(ITEM_ENABLED_CONDITION);
	}
}
