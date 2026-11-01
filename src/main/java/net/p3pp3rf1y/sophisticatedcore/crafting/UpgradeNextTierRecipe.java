package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;

import java.util.Optional;

/**
 * A shaped recipe that copies the applied upgrade onto the crafted result.
 */
public class UpgradeNextTierRecipe extends ShapedRecipe implements IWrapperRecipe<ShapedRecipe> {
	private final ShapedRecipe compose;

	public UpgradeNextTierRecipe(ShapedRecipe compose) {
		super(sophisticatedCore_commonInfo(compose), sophisticatedCore_bookInfo(compose), compose.pattern, compose.result);
		this.compose = compose;
	}

	@Override
	public ShapedRecipe getCompose() {
		return compose;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack nextTier = super.assemble(inv);
		getUpgrade(inv).ifPresent(upgrade -> nextTier.applyComponents(upgrade.getComponents()));
		return nextTier;
	}

	private Optional<ItemStack> getUpgrade(CraftingInput inv) {
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.getItem() instanceof IUpgradeItem) {
				return Optional.of(slotStack);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public RecipeSerializer<ShapedRecipe> getSerializer() {
		return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER.get();
	}

	/**
	 * Wraps the vanilla shaped recipe serializer so this recipe still loads and syncs.
	 */
	private static net.minecraft.world.item.crafting.Recipe.CommonInfo sophisticatedCore_commonInfo(ShapedRecipe recipe) {
		return new net.minecraft.world.item.crafting.Recipe.CommonInfo(true);
	}

	private static net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo sophisticatedCore_bookInfo(ShapedRecipe recipe) {
		return new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group());
	}

	public static RecipeSerializer<UpgradeNextTierRecipe> createSerializer() {
		return RecipeWrapperSerializer.create(UpgradeNextTierRecipe::new, ShapedRecipe.SERIALIZER);
	}
}
