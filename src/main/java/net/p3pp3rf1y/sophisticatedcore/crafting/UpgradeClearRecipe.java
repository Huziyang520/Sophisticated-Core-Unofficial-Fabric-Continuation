package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;

public class UpgradeClearRecipe extends CustomRecipe {
	/**
	 * 26.3 turned recipe serializers into a codec record, so the category travels in the JSON.
	 */
	public static final MapCodec<UpgradeClearRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(CustomRecipe::category)
	).apply(instance, UpgradeClearRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeClearRecipe> STREAM_CODEC = StreamCodec.composite(
			CraftingBookCategory.STREAM_CODEC, CustomRecipe::category,
			UpgradeClearRecipe::new);

	public UpgradeClearRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput inventory, Level level) {
		boolean upgradePresent = false;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof UpgradeItemBase && !stack.getComponents().isEmpty() && !upgradePresent) {
					upgradePresent = true;
				} else {
					return false;
				}
			}
		}

		return upgradePresent;
	}

	@Override
	public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registries) {
		ItemStack upgrade = ItemStack.EMPTY;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItemBase) {
				upgrade = stack;
			}
		}
		return new ItemStack(upgrade.getItem(), 1);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 1 && height >= 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.UPGRADE_CLEAR_SERIALIZER.get();
	}
}