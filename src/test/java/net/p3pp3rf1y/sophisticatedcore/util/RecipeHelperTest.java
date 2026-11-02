package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipeHelperTest {

	private static Level regularOrderRecipesLevel;
	private static Level reverseOrderRecipesLevel;

	private static List<RecipeHolder<CraftingRecipe>> getCraftingRecipes() {
		List<RecipeHolder<CraftingRecipe>> craftingRecipes = new ArrayList<>();
		//stones
		craftingRecipes.add(shaped("granite_to_diorite", Items.GRANITE, Items.DIORITE));
		craftingRecipes.add(shapeless("granite_from_diorite", Items.DIORITE, Items.GRANITE, 9));
		craftingRecipes.add(shaped("stone_to_granite", Items.STONE, Items.GRANITE));
		craftingRecipes.add(shapeless("stone_from_granite", Items.GRANITE, Items.STONE, 9));

		//gold
		craftingRecipes.add(shaped("gold_ingot_to_gold_block", Items.GOLD_INGOT, Items.GOLD_BLOCK));
		craftingRecipes.add(shapeless("gold_ingot_from_gold_block", Items.GOLD_BLOCK, Items.GOLD_INGOT, 9));
		craftingRecipes.add(shaped("gold_nugget_to_gold_ingot", Items.GOLD_NUGGET, Items.GOLD_INGOT));
		craftingRecipes.add(shapeless("gold_nugget_from_gold_ingot", Items.GOLD_INGOT, Items.GOLD_NUGGET, 9));


		//confusion recipes
		craftingRecipes.add(shaped("gold_nugget_to_diorite", Items.GOLD_NUGGET, Items.DIORITE));
		craftingRecipes.add(shaped("granite_to_gold_block", Items.GRANITE, Items.GOLD_BLOCK));
		craftingRecipes.add(shapeless("gold_nugget_from_granite", Items.GRANITE, Items.GOLD_NUGGET, 9));
		craftingRecipes.add(shapeless("granite_from_diamond", Items.DIAMOND, Items.GRANITE, 9));
		craftingRecipes.add(shapeless("iron_nugget_from_granite", Items.GRANITE, Items.IRON_NUGGET, 9));
		craftingRecipes.add(shapeless("stone_from_gold_ingot", Items.GOLD_INGOT, Items.STONE, 9));
		craftingRecipes.add(shapeless("torches_from_gold_block", Items.GOLD_BLOCK, Items.TORCH, 9));

		return craftingRecipes;
	}

	static Stream<Level> classParams() {
		return Stream.of(regularOrderRecipesLevel, reverseOrderRecipesLevel);
	}

	static Stream<Arguments> withClassParams(List<Arguments> methodParams) {
		return classParams().flatMap(classParam -> methodParams.stream().map(arguments -> new CombinedArguments(classParam, arguments)));
	}

	private static class CombinedArguments implements Arguments {
		private final Object[] arguments;

		public CombinedArguments(Level level, Arguments methodArguments) {
			arguments = new Object[methodArguments.get().length + 1];
			arguments[0] = level;
			System.arraycopy(methodArguments.get(), 0, arguments, 1, methodArguments.get().length);
		}
		@Override
		public Object[] get() {
			return arguments;
		}
	}

	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		regularOrderRecipesLevel = getLevelWithRecipeManagerFor(getCraftingRecipes());

		List<RecipeHolder<CraftingRecipe>> reverseOrderRecipes = getCraftingRecipes();
		Collections.reverse(reverseOrderRecipes);
		reverseOrderRecipesLevel = getLevelWithRecipeManagerFor(reverseOrderRecipes);
	}

	private static Level getLevelWithRecipeManagerFor(List<RecipeHolder<CraftingRecipe>> craftingRecipes) {
		RecipeManager mockRecipeManager = mock(RecipeManager.class);
		when(mockRecipeManager.getRecipes()).thenReturn(new ArrayList<>(craftingRecipes));

		ServerLevel level = mock(ServerLevel.class);
		when(level.recipeAccess()).thenReturn(mockRecipeManager);
		return level;
	}

	/**
	 * 26.3 recipes carry a common info record, a book info record and a template result.
	 */
	private static RecipeHolder<CraftingRecipe> shaped(String id, Item ingredient, Item result) {
		List<Optional<Ingredient>> pattern = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			pattern.add(Optional.of(Ingredient.of(ingredient)));
		}
		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.parse(id)),
				new ShapedRecipe(RecipeBuilder.createCraftingCommonInfo(true),
						new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
						new ShapedRecipePattern(3, 3, pattern, Optional.empty()),
						ItemStackTemplate.fromNonEmptyStack(new ItemStack(result))));
	}

	private static RecipeHolder<CraftingRecipe> shapeless(String id, Item ingredient, Item result, int count) {
		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.parse(id)),
				new ShapelessRecipe(RecipeBuilder.createCraftingCommonInfo(true),
						new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
						ItemStackTemplate.fromNonEmptyStack(new ItemStack(result, count)),
						List.of(Ingredient.of(ingredient))));
	}

	@AfterEach
	void clearCache() {
		RecipeHelper.onRecipesUpdated();
	}

	@ParameterizedTest
	@MethodSource
	void testGetCompatingResult(Level level, Item item, RecipeHelper.CompactingResult expectedResult) {
		RecipeHelper.setLevel(level);

		RecipeHelper.CompactingResult actualResult = RecipeHelper.getCompactingResult(new ItemStack(item), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE);

		assertCompactingResultEquals(expectedResult, actualResult, "getCompactingResult returned wrong result");
	}

	static Stream<Arguments> testGetCompatingResult() {
		return withClassParams(
				List.of(
						Arguments.of(Items.GOLD_INGOT, new RecipeHelper.CompactingResult(new ItemStack(Items.GOLD_BLOCK), Collections.emptyList())),
						Arguments.of(Items.GOLD_NUGGET, new RecipeHelper.CompactingResult(new ItemStack(Items.GOLD_INGOT), Collections.emptyList())),
						Arguments.of(Items.GRANITE, new RecipeHelper.CompactingResult(new ItemStack(Items.DIORITE), Collections.emptyList())),
						Arguments.of(Items.STONE, new RecipeHelper.CompactingResult(new ItemStack(Items.GRANITE), Collections.emptyList()))
				)
		);
	}


	@ParameterizedTest
	@MethodSource
	void testGetUncompactingResult(Level level, Item item, RecipeHelper.UncompactingResult expectedResult) {
		RecipeHelper.setLevel(level);

		RecipeHelper.UncompactingResult actualResult = RecipeHelper.getUncompactingResult(new ItemStack(item));

		assertUncompactingResultEquals(expectedResult, actualResult, "getUncompactingResult returned wrong result");
	}

	static Stream<Arguments> testGetUncompactingResult() {
		return withClassParams(
				List.of(
						Arguments.of(Items.GOLD_BLOCK, new RecipeHelper.UncompactingResult(new ItemStack(Items.GOLD_INGOT), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GOLD_INGOT, new RecipeHelper.UncompactingResult(new ItemStack(Items.GOLD_NUGGET), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.DIORITE, new RecipeHelper.UncompactingResult(new ItemStack(Items.GRANITE), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GRANITE, new RecipeHelper.UncompactingResult(new ItemStack(Items.STONE), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE))
				)
		);
	}

	@ParameterizedTest
	@MethodSource
	void testGetItemCompactingShapes(Level level, Item item, Set<RecipeHelper.CompactingShape> shapes) {
		RecipeHelper.setLevel(level);

		Set<RecipeHelper.CompactingShape> actualShapes = RecipeHelper.getItemCompactingShapes(new ItemStack(item));

		if (!Objects.equals(shapes, actualShapes)) {
			assertionFailure().message("getItemCompactingShapes returned wrong result")
					.expected(shapes)
					.actual(actualShapes)
					.buildAndThrow();
		}
	}

	static Stream<Arguments> testGetItemCompactingShapes() {
		return withClassParams(
				List.of(
						Arguments.of(Items.GOLD_INGOT, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GOLD_NUGGET, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GRANITE, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.STONE, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE))
				)
		);
	}

	private static void assertCompactingResultEquals(RecipeHelper.CompactingResult expected, RecipeHelper.CompactingResult actual, Object message) {
		if (ItemStack.matches(expected.getResult(), actual.getResult())
				&& areItemListsEqual(expected.getRemainingItems(), actual.getRemainingItems())) {
			return;
		}

		assertionFailure().message(message)
				.expected(expected.getResult() + ":" + expected.getRemainingItems())
				.actual(actual.getResult() + ":" + actual.getRemainingItems())
				.buildAndThrow();
	}


	private static boolean areItemListsEqual(List<ItemStack> expected, List<ItemStack> actual) {
		if (expected.size() != actual.size()) {
			return false;
		}
		for (int i = 0; i < expected.size(); i++) {
			if (!ItemStack.matches(expected.get(i), actual.get(i))) {
				return false;
			}
		}
		return true;
	}
	private static void assertUncompactingResultEquals(RecipeHelper.UncompactingResult expected, RecipeHelper.UncompactingResult actual, Object message) {
		if (ItemStack.isSameItemSameComponents(expected.getResult(), actual.getResult()) && expected.getCompactUsingShape() == actual.getCompactUsingShape()) {
			return;
		}
		assertionFailure().message(message)
				.expected(expected.getResult() + ":" + expected.getCompactUsingShape())
				.actual(actual.getResult() + ":" + actual.getCompactUsingShape())
				.buildAndThrow();
	}
}
