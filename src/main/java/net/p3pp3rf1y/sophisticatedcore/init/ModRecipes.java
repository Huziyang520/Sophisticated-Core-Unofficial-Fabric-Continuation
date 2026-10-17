package net.p3pp3rf1y.sophisticatedcore.init;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.crafting.ItemEnabledCondition;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeClearRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;
import net.p3pp3rf1y.sophisticatedcore.platform.DeferredHolder;
import net.p3pp3rf1y.sophisticatedcore.platform.DeferredRegister;

public class ModRecipes {
	private ModRecipes() {
	}

	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, SophisticatedCore.MOD_ID);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UpgradeNextTierRecipe>> UPGRADE_NEXT_TIER_SERIALIZER =
			RECIPE_SERIALIZERS.register("upgrade_next_tier", UpgradeNextTierRecipe.Serializer::create);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UpgradeClearRecipe>> UPGRADE_CLEAR_SERIALIZER =
			RECIPE_SERIALIZERS.register("upgrade_clear", () -> RecipeWrapperSerializer.unit(new UpgradeClearRecipe()));

	public static final ResourceConditionType<ItemEnabledCondition> ITEM_ENABLED_CONDITION = ResourceConditionType.create(SophisticatedCore.getRL("item_enabled"), ItemEnabledCondition.CODEC);

	public static void registerHandlers() {
		RECIPE_SERIALIZERS.register();

		ResourceConditions.register(ITEM_ENABLED_CONDITION);
	}
}
