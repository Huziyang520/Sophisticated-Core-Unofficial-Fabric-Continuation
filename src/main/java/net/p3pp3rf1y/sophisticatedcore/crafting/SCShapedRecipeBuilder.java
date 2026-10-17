package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.advancements.triggers.Criterion;
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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/** Shaped recipe builder that can hand the built recipe to a wrapper instead of writing it out directly. */
public class SCShapedRecipeBuilder implements RecipeBuilder {
	protected final RecipeCategory category;
	private final Item result;
	protected final ItemStack resultStack;
	private final List<String> rows = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
	@Nullable
	private String group;
	private boolean showNotification = true;

	public SCShapedRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
		this(category, new ItemStack(result, count));
	}

	public SCShapedRecipeBuilder(RecipeCategory category, ItemStack result) {
		this.category = category;
		this.result = result.getItem();
		this.resultStack = result;
	}

	public static SCShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
		return shaped(category, result, 1);
	}

	public static SCShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
		return new SCShapedRecipeBuilder(category, result, count);
	}

	/** Adds a key to the recipe pattern. */
	public SCShapedRecipeBuilder define(Character symbol, TagKey<Item> tag) {
		return this.define(symbol, Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag)));
	}

	/** Adds a key to the recipe pattern. */
	public SCShapedRecipeBuilder define(Character symbol, ItemLike item) {
		return this.define(symbol, Ingredient.of(item));
	}

	/** Adds a key to the recipe pattern. */
	public SCShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
		if (this.key.containsKey(symbol)) {
			throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
		} else if (symbol == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(symbol, ingredient);
			return this;
		}
	}

	/** Adds a new entry to the patterns for this recipe. */
	public SCShapedRecipeBuilder pattern(String pattern) {
		if (!this.rows.isEmpty() && pattern.length() != this.rows.get(0).length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.rows.add(pattern);
			return this;
		}
	}

	public SCShapedRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
		this.advancementBuilder.unlockedBy(name, criterion);
		return this;
	}

	@Override
	public SCShapedRecipeBuilder group(@Nullable String groupName) {
		this.group = groupName;
		return this;
	}

	public SCShapedRecipeBuilder showNotification(boolean showNotification) {
		this.showNotification = showNotification;
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
		ShapedRecipe shapedRecipe = new ShapedRecipe(
				RecipeBuilder.createCraftingCommonInfo(this.showNotification),
				RecipeBuilder.createCraftingBookInfo(this.category, this.group),
				ShapedRecipePattern.of(this.key, this.rows),
				ItemStackTemplate.fromNonEmptyStack(this.resultStack)
		);
		recipeOutput.accept(id, shapedRecipe, this.advancementBuilder.build(recipeOutput, id, this.category));
	}
}
