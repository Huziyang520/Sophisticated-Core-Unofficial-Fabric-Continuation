package net.p3pp3rf1y.sophisticatedcore.upgrades.stack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StackUpgradeConfig {
	private static final String REGISTRY_NAME_MATCHER = "([a-z0-9_.-]+:[a-z0-9_/.-]+)";
	private final ModConfigSpec.ConfigValue<List<String>> nonStackableItemsList;
	@Nullable
	private Set<Item> nonStackableItems = null;

	public StackUpgradeConfig(ModConfigSpec.Builder builder) {
		builder.comment("Stack Upgrade Settings").push("stackUpgrade");
		nonStackableItemsList = builder.comment("List of items that are not supposed to stack in storage even when stack upgrade is inserted. Item registry names are expected here.").define("nonStackableItems", this::getDefaultNonStackableList, itemNames -> {
			List<String> registryNames = (List<String>) itemNames;
			return registryNames != null && registryNames.stream().allMatch(itemName -> itemName.matches(REGISTRY_NAME_MATCHER));
		});
		builder.pop();
	}

	private List<String> getDefaultNonStackableList() {
		List<String> ret = new ArrayList<>();

		ret.add(RegistryHelper.getItemKey(Items.BUNDLE).toString());
		ret.add(RegistryHelper.getItemKey(Items.SHULKER_BOX).toString());
		Items.DYED_SHULKER_BOX.forEach(item -> ret.add(RegistryHelper.getItemKey(item).toString()));

		return ret;
	}

	public boolean canStackItem(Item item) {
		if (!Config.COMMON_SPEC.isLoaded()) {
			return true;
		}
		if (nonStackableItems == null) {
			nonStackableItems = new HashSet<>();
			nonStackableItemsList.get().forEach(name -> {
				Identifier registryName = Identifier.parse(name);
				if (BuiltInRegistries.ITEM.containsKey(registryName)) {
					BuiltInRegistries.ITEM.getOptional(registryName).ifPresent(nonStackableItems::add);
				} else {
					SophisticatedCore.LOGGER.error("Item {} is set to not be affected by stack upgrade in config, but it does not exist in item registry", name);
				}
			});
		}
		return !nonStackableItems.contains(item);
	}

	public void clearNonStackableItems() {
		nonStackableItems = null;
	}
}