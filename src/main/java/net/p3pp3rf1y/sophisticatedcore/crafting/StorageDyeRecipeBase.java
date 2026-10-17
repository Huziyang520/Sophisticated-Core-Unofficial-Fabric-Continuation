package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Shared logic for the dye recipes: one storage item plus dyes laid out in columns around it. */
public abstract class StorageDyeRecipeBase extends CustomRecipe {
	protected StorageDyeRecipeBase() {
		super();
	}

	@Override
	public boolean matches(CraftingInput inv, Level worldIn) {
		boolean storagePresent = false;
		boolean dyePresent = false;
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.isEmpty()) {
				continue;
			}
			if (isDyeableStorageItem(slotStack)) {
				if (storagePresent) {
					return false;
				}
				storagePresent = true;
			} else if (slotStack.is(ConventionalItemTags.DYES)) {
				dyePresent = true;
			} else {
				return false;
			}
		}
		return storagePresent && dyePresent;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		Map<Integer, List<DyeColor>> columnDyes = new HashMap<>();
		int storageColumn = -1;
		ItemStack storageStack = ItemStack.EMPTY;

		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.isEmpty()) {
				continue;
			}
			int column = slot % inv.width();
			if (isDyeableStorageItem(slotStack)) {
				if (storageColumn >= 0) {
					return ItemStack.EMPTY;
				}

				storageColumn = column;
				storageStack = slotStack;
			} else if (slotStack.is(ConventionalItemTags.DYES)) {
				DyeColor dyeColor = getColorFromStack(slotStack);
				if (dyeColor == null) {
					return ItemStack.EMPTY;
				}
				columnDyes.computeIfAbsent(column, c -> new ArrayList<>()).add(dyeColor);
			} else {
				return ItemStack.EMPTY;
			}
		}
		if (storageColumn < 0) {
			return ItemStack.EMPTY;
		}

		ItemStack coloredStorage = storageStack.copy();
		coloredStorage.setCount(1);

		applyTintColors(columnDyes, coloredStorage, storageColumn);

		return coloredStorage;
	}

	protected abstract boolean isDyeableStorageItem(ItemStack stack);

	private void applyTintColors(Map<Integer, List<DyeColor>> columnDyes, ItemStack coloredStorage, int storageColumn) {
		List<DyeColor> mainDyes = new ArrayList<>();
		List<DyeColor> trimDyes = new ArrayList<>();

		for (Map.Entry<Integer, List<DyeColor>> entry : columnDyes.entrySet()) {
			if (entry.getKey() <= storageColumn) {
				mainDyes.addAll(entry.getValue());
			}
			if (entry.getKey() >= storageColumn) {
				trimDyes.addAll(entry.getValue());
			}
		}

		applyColors(coloredStorage, mainDyes, trimDyes);
	}

	protected abstract void applyColors(ItemStack coloredStorage, List<DyeColor> mainDyes, List<DyeColor> trimDyes);


	/** @return the colour of a dye stack, read from the conventional dye tags, or {@code null} when it is not a dye. */
	@Nullable
	public static DyeColor getColorFromStack(ItemStack stack) {
		for (DyeColor color : DyeColor.values()) {
			if (stack.is(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", color.getName() + "_dyes")))) {
				return color;
			}
		}

		return null;
	}
}
