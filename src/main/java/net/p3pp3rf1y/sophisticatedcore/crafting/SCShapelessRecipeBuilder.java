package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

/** Shapeless recipe builder that can hand the built recipe to a wrapper instead of writing it out directly. */
public class SCShapelessRecipeBuilder implements RecipeBuilder {
	protected final RecipeCategory category;
	private final Item result;
	protected final ItemStack resultStack;
	private final NonNullList<Ingredient> ingredients = NonNullList.create();
	private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
	@Nullable
	private String group;

	public SCShapelessRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
		this(category, new ItemStack(result, count));
	}

	public SCShapelessRecipeBuilder(RecipeCategory category, ItemStack result) {
		this.category = category;
		this.result = result.getItem();
		this.resultStack = result;
	}

	public static SCShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
		return new SCShapelessRecipeBuilder(category, result, 1);
	}

	public static SCShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
		return new SCShapelessRecipeBuilder(category, result, count);
	}

	public static SCShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStack result) {
		return new SCShapelessRecipeBuilder(category, result);
	}

	/** Adds an ingredient that can be any item in the given tag. */
	public SCShapelessRecipeBuilder requires(TagKey<Item> tag) {
		return this.requires(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag)));
	}

	/** Adds an ingredient of the given item. */
	public SCShapelessRecipeBuilder requires(ItemLike item) {
		return this.requires(item, 1);
	}

	/** Adds the given ingredient multiple times. */
	public SCShapelessRecipeBuilder requires(ItemLike item, int quantity) {
		for (int i = 0; i < quantity; i++) {
			this.requires(Ingredient.of(item));
		}
		return this;
	}

	/** Adds an ingredient. */
	public SCShapelessRecipeBuilder requires(Ingredient ingredient) {
		return this.requires(ingredient, 1);
	}

	/** Adds an ingredient multiple times. */
	public SCShapelessRecipeBuilder requires(Ingredient ingredient, int quantity) {
		for (int i = 0; i < quantity; i++) {
			this.ingredients.add(ingredient);
		}
		return this;
	}

	public SCShapelessRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
		this.advancementBuilder.unlockedBy(name, criterion);
		return this;
	}

	@Override
	public SCShapelessRecipeBuilder group(@Nullable String groupName) {
		this.group = groupName;
		return this;
	}

	public Item getResult() {
		return this.result;
	}

	@Override
	public ResourceKey<Recipe<?>> defaultId() {
		return RecipeBuilder.getDefaultRecipeId(resultStack);
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
		ShapelessRecipe shapelessRecipe = new ShapelessRecipe(
				RecipeBuilder.createCraftingCommonInfo(true),
				RecipeBuilder.createCraftingBookInfo(this.category, this.group),
				ItemStackTemplate.fromNonEmptyStack(this.resultStack),
				this.ingredients
		);
		recipeOutput.accept(id, shapelessRecipe, this.advancementBuilder.build(recipeOutput, id, this.category));
	}
}
