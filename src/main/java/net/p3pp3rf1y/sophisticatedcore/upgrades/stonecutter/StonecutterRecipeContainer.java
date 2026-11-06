package net.p3pp3rf1y.sophisticatedcore.upgrades.stonecutter;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.common.gui.IServerUpdater;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SlotSuppliedHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingItemHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StonecutterRecipeContainer {
	private static final String DATA_SELECTED_RECIPE_INDEX = "selectedRecipeIndex";
	private final Slot inputSlot;
	private final IServerUpdater serverUpdater;
	private final Level level;
	private final Slot outputSlot;
	private final ResultContainer resultInventory = new ResultContainer();
	private List<RecipeHolder<StonecutterRecipe>> recipes = Lists.newArrayList();
	private List<ItemStack> recipeResults = Lists.newArrayList();
	private final DataSlot selectedRecipe = DataSlot.standalone();
	private Item inputItem = Items.AIR;
	private final CraftingItemHandler inputInventory;
	private Runnable inventoryUpdateListener = () -> {};
	private final Supplier<Optional<Identifier>> getLastSelectedRecipeId;
	private final Consumer<Identifier> setLastSelectedRecipeId;
	private long lastOnTake = -1;

	public StonecutterRecipeContainer(StonecutterUpgradeContainer upgradeContainer, Consumer<Slot> addSlot, IServerUpdater serverUpdater, ContainerLevelAccess worldPosCallable, Level level) {
		inputSlot = new SlotSuppliedHandler(upgradeContainer.getUpgradeWrapper()::getInputInventory, 0, -1, -1) {
			@Override
			public void setChanged() {
				super.setChanged();
				onCraftMatrixChanged(inputInventory);
			}

			@Override
			public ItemStack remove(int amount) {
				ItemStack ret = super.remove(amount);
				if (getItem().isEmpty()) {
					setChanged();
				}
				return ret;
			}
		};
		this.serverUpdater = serverUpdater;
		this.level = level;
		addSlot.accept(inputSlot);
		inputInventory = new CraftingItemHandler(upgradeContainer.getUpgradeWrapper()::getInputInventory, this::onCraftMatrixChanged);
		outputSlot = new ResultSlot(worldPosCallable);
		addSlot.accept(outputSlot);

		getLastSelectedRecipeId = upgradeContainer.getUpgradeWrapper()::getRecipeId;
		setLastSelectedRecipeId = upgradeContainer.getUpgradeWrapper()::setRecipeId;

		onCraftMatrixChanged(inputInventory);
	}

	private void onCraftMatrixChanged(Container inventory) {
		ItemStack itemstack = inputSlot.getItem();
		if (itemstack.getItem() != inputItem) {
			inputItem = itemstack.getItem();
			updateAvailableRecipes(inventory, itemstack);
		}
		inventoryUpdateListener.run();
	}

	private void updateAvailableRecipes(Container inventory, ItemStack stack) {
		recipes.clear();
		recipeResults.clear();
		selectedRecipe.set(-1);
		if (!level.isClientSide()) {
			outputSlot.set(ItemStack.EMPTY);
		}
		if (!stack.isEmpty()) {
			recipeResults.addAll(RecipeHelper.getStonecutterResults(stack, level));
			if (!level.isClientSide()) {
				recipes.addAll(RecipeHelper.getRecipesOfType(RecipeType.STONECUTTING, new SingleRecipeInput(stack)));
			}
			getLastSelectedRecipeId.get().ifPresent(id -> {
				for (int i = 0; i < recipes.size(); i++) {
					if (recipes.get(i).id().identifier().equals(id)) {
						selectedRecipe.set(i);
						updateRecipeResultSlot();
					}
				}
			});
		}
	}

	public Slot getInputSlot() {
		return inputSlot;
	}

	public Slot getOutputSlot() {
		return outputSlot;
	}

	public void setInventoryUpdateListener(Runnable listenerIn) {
		inventoryUpdateListener = listenerIn;
	}

	public List<ItemStack> getRecipeList() {
		return recipeResults;
	}

	public int getSelectedRecipe() {
		int selected = selectedRecipe.get();
		ItemStack result = outputSlot.getItem();
		if (selected < 0 && level.isClientSide() && !result.isEmpty()) {
			for (int i = 0; i < recipeResults.size(); i++) {
				if (ItemStack.isSameItemSameComponents(recipeResults.get(i), result)) {
					selectedRecipe.set(i);
					return i;
				}
			}
		}
		return selected;
	}

	public boolean hasItemsInInputSlot() {
		return inputSlot.hasItem() && !recipeResults.isEmpty();
	}

	public boolean selectRecipe(int recipeIndex) {
		if (isIndexInRecipeBounds(recipeIndex)) {
			selectedRecipe.set(recipeIndex);
			if (!level.isClientSide()) {
				setLastSelectedRecipeId.accept(recipes.get(recipeIndex).id().identifier());
			}
			updateRecipeResultSlot();
			serverUpdater.sendDataToServer(() -> NBTHelper.putInt(new CompoundTag(), DATA_SELECTED_RECIPE_INDEX, recipeIndex));
		}
		return true;
	}

	private boolean isIndexInRecipeBounds(int index) {
		return index >= 0 && index < recipeResults.size();
	}

	private void updateRecipeResultSlot() {
		int index = selectedRecipe.get();
		if (!level.isClientSide()) {
			if (!recipes.isEmpty() && index >= 0 && index < recipes.size()) {
				RecipeHolder<StonecutterRecipe> stonecuttingrecipe = recipes.get(index);
				resultInventory.setRecipeUsed(stonecuttingrecipe);
				outputSlot.set(stonecuttingrecipe.value().assemble(new SingleRecipeInput(inputInventory.getItem(0))));
			} else {
				outputSlot.set(ItemStack.EMPTY);
			}
		} else if (index >= 0 && index < recipeResults.size()) {
			outputSlot.set(recipeResults.get(index).copy());
		} else {
			outputSlot.set(ItemStack.EMPTY);
		}
	}

	public void handlePacket(CompoundTag data) {
		if (data.contains(DATA_SELECTED_RECIPE_INDEX)) {
			selectRecipe(data.getIntOr(DATA_SELECTED_RECIPE_INDEX, 0));
		}
	}

	public boolean isNotResultSlot(Slot slot) {
		return slot != outputSlot;
	}

	private class ResultSlot extends Slot {
		private final ContainerLevelAccess worldPosCallable;

		public ResultSlot(ContainerLevelAccess worldPosCallable) {
			super(resultInventory, 1, -1, -1);
			this.worldPosCallable = worldPosCallable;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			stack.onCraftedBy(player, stack.getCount());
			resultInventory.awardUsedRecipes(player, List.of(inputSlot.getItem()));
			ItemStack itemstack = inputSlot.remove(1);
			if (!itemstack.isEmpty()) {
				updateRecipeResultSlot();
			}

			worldPosCallable.execute((world, pos) -> {
				long l = world.getGameTime();
				if (lastOnTake != l) {
					world.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
					lastOnTake = l;
				}
			});
			super.onTake(player, stack);
		}
	}
}
