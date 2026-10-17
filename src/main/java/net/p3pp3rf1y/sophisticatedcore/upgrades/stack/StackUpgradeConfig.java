package net.p3pp3rf1y.sophisticatedcore.upgrades.stack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.platform.config.ConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.RegistryHelper;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StackUpgradeConfig {
	private static final String REGISTRY_NAME_MATCHER = "([a-z0-9_.-]+:[a-z0-9_/.-]+)";
	private final ConfigSpec.ConfigValue<List<String>> nonStackableItemsList;
	@Nullable
	private Set<Item> nonStackableItems = null;

	public StackUpgradeConfig(ConfigSpec.Builder builder) {
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
		// 26.3 turned dyed shulkers into the single shulker_box item with a colour component, so one entry covers them all
		ret.add(RegistryHelper.getItemKey(Items.SHULKER_BOX).toString());

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
					nonStackableItems.add(BuiltInRegistries.ITEM.getValue(registryName));
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
