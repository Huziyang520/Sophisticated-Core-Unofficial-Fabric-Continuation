package net.p3pp3rf1y.sophisticatedcore.client.gui;

import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix3x2fStack;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.*;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.*;
import net.p3pp3rf1y.sophisticatedcore.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.network.TransferFullSlotPayload;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.ICraftingUIPart;
import net.p3pp3rf1y.sophisticatedcore.util.CountAbbreviator;

import org.jetbrains.annotations.Nullable;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.GUI_CONTROLS;

public abstract class StorageScreenBase<S extends StorageContainerMenuBase<?>> extends AbstractContainerScreen<S>
		implements InventoryScrollPanel.IInventoryScreen {
	public static final int ERROR_BACKGROUND_COLOR = 0xF0100010;
	public static final int ERROR_BORDER_COLOR = DyeColor.RED.getTextureDiffuseColor();
	private static final int DISABLED_SLOT_COLOR = -1072689136;
	private static final int UPGRADE_TOP_HEIGHT = 7;
	private static final int UPGRADE_SLOT_HEIGHT = 16;
	private static final int UPGRADE_BOTTOM_HEIGHT = 6;
	public static final int UPGRADE_INVENTORY_OFFSET = 21;
	public static final int DISABLED_SLOT_X_POS = -2000;
	static final int SLOTS_Y_OFFSET = 17;
	static final int SLOTS_X_OFFSET = 7;
	public static final int ERROR_SLOT_COLOR = (DyeColor.RED.getTextureDiffuseColor() & 0x00_FFFFFF) | 0xAA000000;
	private static final int ERROR_TEXT_COLOR = DyeColor.RED.getTextureDiffuseColor();
	public static final int HEIGHT_WITHOUT_STORAGE_SLOTS = 114;

	private UpgradeSettingsTabControl settingsTabControl;
	/**
	 * 26.3 客户端菜单的升级槽数量是同步后才到位的，所以这里不能在构造器里定死成 final —— 否则升级栏永远按 0 个槽算，
	 * 箱子该有 1 个槽、木桶 0~4 个槽都会画不出来。每次刷新尺寸时从菜单重新取。
	 */
	private int numberOfUpgradeSlots;
	@Nullable
	private Button sortButton = null;
	@Nullable
	private ToggleButton<SortBy> sortByButton = null;
	/**
	 * 排序方式图标不能直接读菜单：菜单里的值要绕一圈（发包 → 服务端 wrapper → 回同步），
	 * 背包更是把值写进物品组件，客户端那份随时可能被下一次同步盖回去 —— 表现就是“点了图标不变”。
	 * 这里存一份本地选择，点击后立刻用它画图标，等菜单里的值追上来再清掉。
	 */
	@Nullable
	private SortBy pendingSortBy = null;

	private InventoryScrollPanel inventoryScrollPanel = null;
	private final Set<ToggleButton<Boolean>> upgradeSwitches = new HashSet<>();

	private final Map<Integer, UpgradeInventoryPartBase<?>> inventoryParts = new LinkedHashMap<>();

	private static ICraftingUIPart craftingUIPart = ICraftingUIPart.NOOP;
	private static ISlotDecorationRenderer slotDecorationRenderer = (guiGraphics, slot) -> {
	};

	/**
	 * 26.3 made {@code AbstractContainerScreen.imageWidth/imageHeight} final, so a screen with a variable size can no
	 * longer assign them. These shadow the parent fields: the mod draws and lays out against this size and fixes the
	 * centring in {@link #repositionElements()}.
	 */
	protected int imageWidth;
	protected int imageHeight;
	/**
	 * 26.3 的 {@code inventoryLabelX/Y} 是父类的 protected 字段，而父类的 {@code extractLabels} 读的就是父类那两个字段。
	 * 之前这里又声明了一份同名私有字段，只会盖住自己的名字、盖不住父类的方法：标签永远画在父类构造时算出的 8 / (166-94)，
	 * 槽位却按本屏幕的行数往下排 —— 行数一多，“物品栏”三个字就离 3×9 格越来越远。
	 * 所以这里不再声明，直接复用继承来的字段（下方 updateDimensionsAndSlotPositions 里赋值）。
	 */
	protected StorageBackgroundProperties storageBackgroundProperties;
	@Nullable
	private Button transferToStorageButton;
	@Nullable
	private Button transferToInventoryButton;
	private TextBox searchBox;
	private Predicate<ItemStack> stackFilter = stack -> searchBox == null || searchBox.getValue().isEmpty()
			|| (!stack.isEmpty() && stack.getHoverName().getString().toLowerCase().contains(searchBox.getValue().toLowerCase()));
	private int visibleSlotsCount;
	private boolean initializing = true;

	public static void setCraftingUIPart(ICraftingUIPart part) {
		craftingUIPart = part;
	}

	public static void setSlotDecorationRenderer(ISlotDecorationRenderer renderer) {
		slotDecorationRenderer = renderer;
	}

	protected StorageScreenBase(S menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		numberOfUpgradeSlots = getMenu().getNumberOfUpgradeSlots();
		visibleSlotsCount = getMenu().getNumberOfStorageInventorySlots();
		updateDimensionsAndSlotPositions(Minecraft.getInstance().getWindow().getGuiScaledHeight());
	}

	public ICraftingUIPart getCraftingUIAddition() {
		return craftingUIPart;
	}

	@Override
	public void resize(int width, int height) {
		updateDimensionsAndSlotPositions(height);
		super.resize(width, height);
	}

	/** 26.3 的父类按固定 176x166 居中；本屏幕尺寸随行数变化，重算后仍然只写 leftPos/topPos 这一处。 */
	@Override
	protected void repositionElements() {
		super.repositionElements();
		leftPos = (width - imageWidth) / 2;
		topPos = (height - imageHeight) / 2;
	}

	/**
	 * 屏幕尺寸与所有槽位坐标的唯一计算点。
	 * <p>
	 * 这里算出的 leftPos/topPos 是之后所有绘制的唯一基准：背景、页签、按钮、槽位坐标写回都用它派生，
	 * 任何地方都不再重算 (width - imageWidth) / 2 —— 一旦两处算得不一样，背景格子与物品就会整体错位，
	 * GUI 缩放调得越大错得越明显。
	 */
	private void updateDimensionsAndSlotPositions(int height) {
		numberOfUpgradeSlots = getMenu().getNumberOfUpgradeSlots();
		int totalRows = getMenu().getNumberOfRows();
		int rowsThatFit = (height - HEIGHT_WITHOUT_STORAGE_SLOTS) / 18;
		// 矮窗口兜底：行数被算成 0 时滚动面板高度也是 0，所有槽位都会被布局到 -100，界面看起来一格都没有。
		// 至少保留一行（放不下就让它超出屏幕中线，仍然能滚动查看），保证「永远有格子」。
		int displayableNumberOfRows = totalRows <= 0 ? 0 : Math.min(Math.max(1, rowsThatFit), totalRows);
		int newImageHeight = HEIGHT_WITHOUT_STORAGE_SLOTS + getStorageInventoryHeight(displayableNumberOfRows);
		storageBackgroundProperties = (getMenu().getNumberOfStorageInventorySlots() + getMenu().getColumnsTaken() * totalRows) <= 81 ? StorageBackgroundProperties.REGULAR_9_SLOT : StorageBackgroundProperties.REGULAR_12_SLOT;

		imageWidth = storageBackgroundProperties.getSlotsOnLine() * 18 + 14;
		updateStorageSlotsPositions();
		if (displayableNumberOfRows < totalRows) {
			storageBackgroundProperties = storageBackgroundProperties == StorageBackgroundProperties.REGULAR_9_SLOT ? StorageBackgroundProperties.WIDER_9_SLOT : StorageBackgroundProperties.WIDER_12_SLOT;
			imageWidth += 6;
		}
		imageHeight = newImageHeight;
		inventoryLabelY = imageHeight - 94;
		inventoryLabelX = 8 + storageBackgroundProperties.getPlayerInventoryXOffset();
		if (width > 0 && height > 0) {
			leftPos = (width - imageWidth) / 2;
			topPos = (height - imageHeight) / 2;
		}
		updatePlayerSlotsPositions();
		updateUpgradeSlotsPositions();
		updateTransferButtonsPositions();
	}

	protected int getStorageInventoryHeight(int displayableNumberOfRows) {
		return displayableNumberOfRows * 18;
	}

	@Override
	public Slot getSlot(int slotIndex) {
		return getMenu().getSlot(slotIndex);
	}

	/**
	 * 升级槽在界面内的 Y 偏移。底图、槽位坐标、左侧开关都从这里派生，
	 * 免得三处各写一遍 6 + i * 16，改一个常量就错位。
	 */
	private int getUpgradeSlotY(int slotIndex) {
		return 6 + slotIndex * UPGRADE_SLOT_HEIGHT;
	}

	protected void updateUpgradeSlotsPositions() {
		for (int slotIndex = 0; slotIndex < numberOfUpgradeSlots; slotIndex++) {
			Slot slot = getMenu().getSlot(getMenu().getFirstUpgradeSlot() + slotIndex);
			slot.y = getUpgradeSlotY(slotIndex);
		}
	}

	protected void updateStorageSlotsPositions() {
		int yPosition = 18;

		visibleSlotsCount = 0;
		int slotIndex = 0;
		while (slotIndex < getMenu().getNumberOfStorageInventorySlots()) {
			Slot slot = getMenu().getSlot(slotIndex);
			int lineIndex = visibleSlotsCount % getSlotsOnLine();
			slotIndex++;

			if (stackFilter.test(slot.getItem())) {
				slot.x = 8 + lineIndex * 18;
				slot.y = yPosition;
				visibleSlotsCount++;
				if (visibleSlotsCount % getSlotsOnLine() == 0) {
					yPosition += 18;
				}
			} else {
				slot.x = DISABLED_SLOT_X_POS;
			}

		}
	}

	@Override
	public Predicate<ItemStack> getStackFilter() {
		return stackFilter;
	}

	protected void updatePlayerSlotsPositions() {
		int playerInventoryXOffset = storageBackgroundProperties.getPlayerInventoryXOffset();

		int yPosition = inventoryLabelY + 12;

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				int slotIndex = j + i * 9;
				int xPosition = playerInventoryXOffset + 8 + j * 18;
				Slot slot = getMenu().getSlot(getMenu().getInventorySlotsSize() - StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS + slotIndex);
				slot.x = xPosition;
				slot.y = yPosition;
			}
			yPosition += 18;
		}

		yPosition += 4;

		for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
			int xPosition = playerInventoryXOffset + 8 + slotIndex * 18;
			Slot slot = getMenu().getSlot(getMenu().getInventorySlotsSize() - StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS + 3 * 9 + slotIndex);
			slot.x = xPosition;
			slot.y = yPosition;
		}
	}

	@Override
	protected void init() {
		super.init();
		// 26.3 的 super.init() 用父类固定尺寸居中，这里必须按真实行数再算一次，
		// 否则从打开界面起 leftPos/topPos 就是错的（只有窗口尺寸变化后才会被 resize 顺带纠正）。
		updateDimensionsAndSlotPositions(height);
		updateInventoryScrollPanel();
		craftingUIPart.setStorageScreen(this);
		initUpgradeSettingsControl();
		initUpgradeInventoryParts();
		addUpgradeSwitches();
		getMenu().setUpgradeChangeListener(c -> {
			updateDimensionsAndSlotPositions(height);
			updateInventoryScrollPanel();
			children().remove(settingsTabControl);
			craftingUIPart.onCraftingSlotsHidden();
			initUpgradeSettingsControl();
			initUpgradeInventoryParts();
			addUpgradeSwitches();
			updateTransferButtonsPositions();
		});
		if (shouldShowSortButtons()) {
			addSortButtons();
		}

		addTransferButtons();
		addSearchBox();

		initializing = false;
	}

	protected void addSearchBox() {
		SortButtonsPosition sortButtonsPosition = Config.CLIENT.sortButtonsPosition.get();
		int x = 7;
		int xEnd = sortButtonsPosition == SortButtonsPosition.TITLE_LINE_RIGHT ? getSortButtonsPosition(sortButtonsPosition).x() - 1 - leftPos : imageWidth - 7;
		int width = xEnd - x;

		// init()/resize 会重建这个控件：重建时把玩家已经敲进去的词带过来，否则一次窗口尺寸变化（或任何重新 init 的路径）
		// 就等于把搜索框清空，玩家看到的是“点一下/动一下窗口，字就没了”。
		String previousPhrase = searchBox == null ? null : searchBox.getValue();
		searchBox = new SearchBox(new Position(leftPos + x, topPos + 5), new Dimension(width, 10), this);
		searchBox.setResponder(this::onSearchPhraseChange);
		if (previousPhrase != null) {
			searchBox.setValue(previousPhrase);
		} else if (getMenu().shouldKeepSearchPhrase()) {
			searchBox.setValue(getMenu().getSearchPhrase());
		}
		addRenderableWidget(searchBox);
	}

	private void onSearchPhraseChange(String searchPhrase) {
		if (!initializing) {
			getMenu().setSearchPhrase(searchPhrase);
		}
		updateSearchFilter(searchPhrase);
		if (inventoryScrollPanel != null) {
			inventoryScrollPanel.resetScrollDistance();
			inventoryScrollPanel.updateSlotsPosition();
		} else {
			updateStorageSlotsPositions();
		}
	}

	private void updateSearchFilter(String searchPhrase) {
		if (searchPhrase.trim().isEmpty()) {
			stackFilter = stack -> true;
			return;
		}

		String[] searchTerms = searchPhrase.trim().split(" ");

		List<Predicate<ItemStack>> filters = new ArrayList<>();

		for (String searchTerm : searchTerms) {
			if (searchTerm.startsWith("@")) {
				String modName = searchTerm.substring(1).toLowerCase();
				filters.add(stack -> modName.isEmpty() || BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().contains(modName));
			} else if (searchTerm.startsWith("#")) {
				String tooltipKeyword = searchTerm.substring(1).toLowerCase();
				filters.add(stack -> getTooltipFromItem(minecraft, stack).stream().anyMatch(line -> line.getString().toLowerCase().contains(tooltipKeyword)));
			} else {
				filters.add(stack -> stack.getHoverName().getString().toLowerCase().contains(searchTerm.toLowerCase()));
			}
		}

		stackFilter = stack -> !stack.isEmpty() && filters.stream().allMatch(f -> f.test(stack));
	}

	private void addTransferButtons() {
		transferToStorageButton = new TransferButton(filterByContents -> getMenu().transferItemsToStorage(filterByContents), ButtonDefinitions.TRANSFER_TO_STORAGE, ButtonDefinitions.TRANSFER_TO_STORAGE_FILTERED);
		addRenderableWidget(transferToStorageButton);

		transferToInventoryButton = new TransferButton(filterByContents -> getMenu().transferItemsToPlayerInventory(filterByContents), ButtonDefinitions.TRANSFER_TO_INVENTORY, ButtonDefinitions.TRANSFER_TO_INVENTORY_FILTERED);
		addRenderableWidget(transferToInventoryButton);
		updateTransferButtonsPositions();
	}

	protected boolean shouldShowSortButtons() {
		return true;
	}

	private void updateInventoryScrollPanel() {
		if (inventoryScrollPanel != null) {
			removeWidget(inventoryScrollPanel);
		}

		int numberOfVisibleRows = getNumberOfVisibleRows();
		if (numberOfVisibleRows < getMenu().getNumberOfRows()) {
			inventoryScrollPanel = new InventoryScrollPanel(Minecraft.getInstance(), this, 0, getMenu().getNumberOfStorageInventorySlots(), getSlotsOnLine(), numberOfVisibleRows * 18, topPos + SLOTS_Y_OFFSET, leftPos + SLOTS_X_OFFSET);
			addRenderableWidget(inventoryScrollPanel);
			inventoryScrollPanel.updateSlotsPosition();
		} else {
			inventoryScrollPanel = null;
		}
	}

	private void updateTransferButtonsPositions() {
		if (transferToStorageButton == null || transferToInventoryButton == null) {
			return;
		}
		transferToStorageButton.setPosition(new Position(leftPos + inventoryLabelX + 137, topPos + inventoryLabelY - 2));
		transferToInventoryButton.setPosition(new Position(leftPos + inventoryLabelX + 149, topPos + inventoryLabelY - 2));
	}

	private int getNumberOfVisibleRows() {
		int rows = (imageHeight - HEIGHT_WITHOUT_STORAGE_SLOTS) / 18;
		return Math.min(Math.max(0, rows), getMenu().getNumberOfRows());
	}

	public int getSlotsOnLine() {
		// 升级占用的列多于背景列数时不能让这个值变成 0/负数，否则槽位布局会除零，整片格子消失
		return Math.max(1, storageBackgroundProperties.getSlotsOnLine() - getMenu().getColumnsTaken());
	}

	private void initUpgradeInventoryParts() {
		inventoryParts.clear();
		if (getMenu().getColumnsTaken() == 0) {
			return;
		}

		int numberOfVisibleRows = getNumberOfVisibleRows();
		int scrollBarOffset = numberOfVisibleRows < getMenu().getNumberOfRows() ? 6 : 0;
		AtomicReference<Position> pos = new AtomicReference<>(new Position(SLOTS_X_OFFSET + getSlotsOnLine() * 18 + scrollBarOffset, SLOTS_Y_OFFSET));
		int height = numberOfVisibleRows * 18;
		for (Map.Entry<Integer, UpgradeContainerBase<?, ?>> entry : getMenu().getUpgradeContainers().entrySet()) {
			UpgradeContainerBase<?, ?> container = entry.getValue();
			UpgradeGuiManager.getInventoryPart(entry.getKey(), container, pos.get(), height, this).ifPresent(part -> {
				inventoryParts.put(entry.getKey(), part);
				pos.set(new Position(pos.get().x() + 36, pos.get().y()));
			});
		}
	}

	private void addUpgradeSwitches() {
		// 升级变化时会被再次调用，旧控件必须先摘掉，否则 children 里会越积越多
		upgradeSwitches.forEach(this::removeWidget);
		upgradeSwitches.clear();
		for (int slot = 0; slot < numberOfUpgradeSlots; slot++) {
			if (menu.canDisableUpgrade(slot)) {
				int finalSlot = slot;
				// 开关和对应的升级槽同一行（槽位顶部 +2），不跟随循环变量累加，避免跳槽后错位
				ToggleButton<Boolean> upgradeSwitch = new ToggleButton<>(new Position(leftPos - 22, topPos + getUpgradeSlotY(slot) + 2), ButtonDefinitions.UPGRADE_SWITCH,
						button -> getMenu().setUpgradeEnabled(finalSlot, !getMenu().getUpgradeEnabled(finalSlot)), () -> getMenu().getUpgradeEnabled(finalSlot));
				// 26.3 的 addWidget 只把控件加进 children，不会进渲染列表；本屏幕自己遍历 children 绘制，
				// 但用 addRenderableWidget 才能保证走任何父类渲染路径时也不会漏画
				addRenderableWidget(upgradeSwitch);
				upgradeSwitches.add(upgradeSwitch);
			}
		}
	}

	private void addSortButtons() {
		SortButtonsPosition sortButtonsPosition = Config.CLIENT.sortButtonsPosition.get();
		if (sortButtonsPosition == SortButtonsPosition.HIDDEN) {
			return;
		}

		Position pos = getSortButtonsPosition(sortButtonsPosition);

		sortButton = new Button(new Position(pos.x(), pos.y()), ButtonDefinitions.SORT, button -> {
			SophisticatedCore.LOGGER.debug("Sort button clicked (button {}, sortBy {})", button, getDisplayedSortBy());
			if (button == 0) {
				getMenu().sort();
				//noinspection ConstantConditions - by this point player can't be null
				Minecraft.getInstance().player.sendOverlayMessage(Component.literal("Sorted"));
			}
		});
		addRenderableWidget(sortButton);
		sortByButton = new ToggleButton<>(new Position(pos.x() + 12, pos.y()), ButtonDefinitions.SORT_BY, button -> {
			if (button == 0) {
				SortBy next = getDisplayedSortBy().next();
				// 先落本地再发包：按钮图标读的是 getDisplayedSortBy()，点一下就必须换，
				// 不能等服务端/物品组件那一圈同步回来（背包那条路经常把它盖回去）。
				pendingSortBy = next;
				getMenu().setSortBy(next);
			}
		}, this::getDisplayedSortBy);
		addRenderableWidget(sortByButton);
	}

	/** 按钮图标与 tooltip 都读这里：本地选择优先，菜单里的值追上来后自动交还。 */
	private SortBy getDisplayedSortBy() {
		if (pendingSortBy != null && getMenu().getSortBy() == pendingSortBy) {
			pendingSortBy = null; // 服务端/物品组件已经确认，之后跟随菜单即可
		}
		return pendingSortBy != null ? pendingSortBy : getMenu().getSortBy();
	}

	private Position getSortButtonsPosition(SortButtonsPosition sortButtonsPosition) {
		return switch (sortButtonsPosition) {
			case BELOW_UPGRADES ->
					new Position(leftPos - UPGRADE_INVENTORY_OFFSET - 2, topPos + getUpgradeHeightWithoutBottom() + UPGRADE_BOTTOM_HEIGHT + 2);
			case BELOW_UPGRADE_TABS ->
					new Position(settingsTabControl.getX() + 2, settingsTabControl.getY() + Math.max(0, settingsTabControl.getHeight() + 2));
			default -> new Position(leftPos + imageWidth - 31, topPos + 4);
		};
	}

	private void initUpgradeSettingsControl() {
		settingsTabControl = new UpgradeSettingsTabControl(new Position(leftPos + imageWidth, topPos + 4), this, getStorageSettingsTabTooltip());
		addWidget(settingsTabControl);
	}

	protected abstract String getStorageSettingsTabTooltip();

	public int getUpgradeHeight() {
		return getUpgradeHeightWithoutBottom() + UPGRADE_TOP_HEIGHT;
	}

	protected int getUpgradeHeightWithoutBottom() {
		return UPGRADE_BOTTOM_HEIGHT + numberOfUpgradeSlots * UPGRADE_SLOT_HEIGHT;
	}

	public Optional<Rect2i> getSortButtonsRectangle() {
		if (sortButton == null || sortByButton == null) {
			return Optional.empty();
		}
		return GuiHelper.getPositiveRectangle(sortButton.getX(), sortButton.getY(), sortByButton.getX() + sortByButton.getWidth() - sortButton.getX(), sortByButton.getY() + sortByButton.getHeight() - sortButton.getY());
	}

	/**
	 * 26.3 的三段式之一：背景。
	 * <p>
	 * 父类在 {@code extractRenderStateWithTooltipAndSubtitles} 里先调它，再调 {@code extractContents}。
	 * 所以存储背景板、格子底纹、升级栏底图、设置页签都必须在这里画，才会落在物品和控件下面。
	 */
	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (menu.detectSettingsChangeAndReload()) {
			updateDimensionsAndSlotPositions(height);
			updateInventoryScrollPanel();
		}
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

		extractStorageBackground(guiGraphics);

		// 设置页签是用 addWidget 加进来的（不能走 addRenderableWidget，否则会被父类的渲染列表再画一次），
		// 26.3 里 addWidget 不进渲染列表，所以整列页签必须在这里手动画，否则侧边按钮一个都不显示。
		settingsTabControl.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	/** 背景板只认 leftPos/topPos，不再重算 (width - imageWidth) / 2。 */
	private void extractStorageBackground(GuiGraphicsExtractor guiGraphics) {
		drawInventoryBg(guiGraphics, leftPos, topPos, storageBackgroundProperties.getTextureName());
		if (inventoryScrollPanel == null) {
			drawSlotBg(guiGraphics, leftPos, topPos, visibleSlotsCount);
			drawSlotOverlays(guiGraphics);
		}
		drawUpgradeBackground(guiGraphics);
	}

	/**
	 * 26.3 的三段式之二：控件 + 槽位 + 物品 + 标签。
	 * <p>
	 * 父类的 {@code extractContents} 会先画所有 renderables，再 translate(leftPos, topPos) 调
	 * {@code extractLabels}，最后把所有 menu.slots 再画一遍。本屏幕的滚动面板、升级槽、玩家背包槽各有自己的绘制入口，
	 * 照搬父类会重复画，所以这里按上游结构自己走一遍：控件 → 标签/槽位（translate 后）→ 搜索框 → 页签前景。
	 */
	@Override
	public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		// 父类是在自己的 extractContents 里定位 hoveredSlot 的；跳过父类后必须自己来，
		// 否则槽位高亮和物品 tooltip 没有目标（父类 extractTooltip 也读这个字段）
		hoveredSlot = findSlot(mouseX, mouseY);

		for (GuiEventListener child : children()) {
			if (child == settingsTabControl || child == searchBox) {
				continue; // 页签在背景/前景两段画，搜索框在标签之后画（会盖住标题）
			}
			if (child instanceof Renderable renderable) {
				renderable.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
			}
		}

		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(leftPos, topPos);
		extractLabels(guiGraphics, mouseX, mouseY);
		poseStack.popMatrix();

		if (searchBox != null) {
			searchBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		}
		settingsTabControl.extractForeground(guiGraphics, mouseX, mouseY, partialTicks);
		renderErrorOverlay(guiGraphics);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.extractLabels(guiGraphics, mouseX, mouseY);
		renderUpgradeInventoryParts(guiGraphics, mouseX, mouseY);
		renderUpgradeSlots(guiGraphics, mouseX, mouseY);
		if (inventoryScrollPanel == null) {
			renderStorageInventorySlots(guiGraphics, mouseX, mouseY);
		}
		renderPlayerInventorySlots(guiGraphics, mouseX, mouseY);
	}

	/** 玩家背包 36 格。父类原本在 extractContents 里统一画，这里跳过父类后要自己补上。 */
	private void renderPlayerInventorySlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		int firstPlayerSlot = getMenu().getInventorySlotsSize() - StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS;
		if (firstPlayerSlot < 0) {
			return;
		}
		for (int i = 0; i < StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS; i++) {
			Slot slot = getMenu().realInventorySlots.get(firstPlayerSlot + i);
			if (slot.isActive()) {
				extractSlot(guiGraphics, slot, mouseX, mouseY);
				if (isHoveringSlot(slot, mouseX, mouseY)) {
					hoveredSlot = slot;
					GuiHelper.renderSlotHighlight(guiGraphics, slot.x, slot.y, 0, sophisticatedCore_getSlotColor(slot.index));
				}
			}
		}
	}

	private void renderUpgradeInventoryParts(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		inventoryParts.values().forEach(ip -> ip.extractRenderState(guiGraphics, mouseX, mouseY));
	}

	private void renderStorageInventorySlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		renderStorageInventorySlots(guiGraphics, mouseX, mouseY, true);
	}

	private void renderStorageInventorySlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean canShowHover) {
		for (int slotId = 0; slotId < menu.realInventorySlots.size() && slotId < getMenu().getNumberOfStorageInventorySlots(); ++slotId) {
			Slot slot = menu.realInventorySlots.get(slotId);
			extractSlot(guiGraphics, slot, mouseX, mouseY);

			if (canShowHover && isHoveringSlot(slot, mouseX, mouseY) && slot.isActive()) {
				hoveredSlot = slot;
				GuiHelper.renderSlotHighlight(guiGraphics, slot.x, slot.y, 0, sophisticatedCore_getSlotColor(slotId));
			}
		}
	}

	private void renderUpgradeSlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		for (int slotId = 0; slotId < menu.upgradeSlots.size(); ++slotId) {
			Slot slot = menu.upgradeSlots.get(slotId);
			if (slot.x != DISABLED_SLOT_X_POS) {
				extractSlot(guiGraphics, slot, mouseX, mouseY);
				if (!slot.isActive()) {
					renderSlotOverlay(guiGraphics, slot, DISABLED_SLOT_COLOR);
				}
			}

			if (isHoveringSlot(slot, mouseX, mouseY) && slot.isActive()) {
				hoveredSlot = slot;
				GuiHelper.renderSlotHighlight(guiGraphics, slot.x, slot.y, 0, sophisticatedCore_getSlotColor(slotId));
			}
		}
	}

	@Override
	public void extractSlot(GuiGraphicsExtractor guiGraphics, Slot slot, int mouseX, int mouseY) {
		int i = slot.x;
		int j = slot.y;
		ItemStack stackToRender = slot.getItem();
		boolean flag = false;
		// 26.3 把拖拽状态从 AbstractContainerScreen 移走了，父类自己负责画出被拖动的堆叠
		ItemStack carriedStack = getMenu().getCarried();
		String stackCountText = null;
		if (getMenu().isInfiniteSlot(slot.index)) {
			stackCountText = "∞";
		}

		if (isQuickCrafting && quickCraftSlots.contains(slot) && !carriedStack.isEmpty()) {
			if (quickCraftSlots.size() == 1) {
				return;
			}

			if (StorageContainerMenuBase.canItemQuickReplace(slot, carriedStack) && menu.canDragTo(slot)) {
				flag = true;
				int slotStackCount = stackToRender.isEmpty() ? 0 : stackToRender.getCount();
				int renderCount = StorageContainerMenuBase.getQuickCraftPlaceCount(slot, quickCraftSlots.size(), quickCraftingType, carriedStack) + slotStackCount;
				int slotLimit = stackToRender.isEmpty() ? 64 : slot.getMaxStackSize(stackToRender);
				if (renderCount > slotLimit) {
					stackCountText = ChatFormatting.YELLOW + CountAbbreviator.abbreviate(slotLimit);
				}
				stackToRender = carriedStack.copyWithCount(renderCount);
			} else {
				quickCraftSlots.remove(slot);
					}
		}
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(0, 0);
		if (stackToRender.isEmpty() && slot.isActive()) {
			renderSlotBackground(guiGraphics, slot, i, j);
		} else {
			renderStack(guiGraphics, i, j, stackToRender, flag, stackCountText);
			slotDecorationRenderer.renderDecoration(guiGraphics, slot);
		}
		poseStack.popMatrix();
	}

	private void renderStack(GuiGraphicsExtractor guiGraphics, int x, int y, ItemStack itemstack, boolean flag, @Nullable String stackCountText) {
		if (flag) {
			guiGraphics.fill(x, y, x + 16, y + 16, -2130706433);
		}

		guiGraphics.item(itemstack, x, y);
		if (shouldUseSpecialCountRender(itemstack)) {
			guiGraphics.itemDecorations(font, itemstack, x, y, "");
			if (stackCountText == null) {
				stackCountText = CountAbbreviator.abbreviate(itemstack.getCount());
			}
			renderStackCount(guiGraphics, stackCountText, x, y);
		} else {
			guiGraphics.itemDecorations(font, itemstack, x, y, stackCountText);
		}
	}

	private void renderSlotBackground(GuiGraphicsExtractor guiGraphics, Slot slot, int i, int j) {
		Optional<ItemStack> memorizedStack = getMenu().getMemorizedStackInSlot(slot.index);
		if (getMenu().isStorageInventorySlot(slot.index)) {
			if (memorizedStack.isPresent()) {
				guiGraphics.item(memorizedStack.get(), i, j);
				drawStackOverlay(guiGraphics, i, j);
				return;
			} else if (!getMenu().getSlotFilterItem(slot.index).isEmpty()) {
				guiGraphics.item(getMenu().getSlotFilterItem(slot.index), i, j);
				drawStackOverlay(guiGraphics, i, j);
				return;
			}
		}
		Identifier noItemIcon = slot.getNoItemIcon();
		if (noItemIcon != null) {
			// 之前按 blocks 图集的 sprite 查：这些 id 指向 textures/item/*.png，图集里没有，
			// 拿到的是 missing sprite —— 升级槽因此显示成紫黑格。改用独立贴图直绘。
			GuiHelper.renderSlotIcon(guiGraphics, noItemIcon, i, j);
		}
	}

	private void drawStackOverlay(GuiGraphicsExtractor guiGraphics, int x, int y) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiHelper.GUI_CONTROLS, x, y, 77, 0, 16, 16, 256, 256);
		guiGraphics.pose().popMatrix();
	}

	private boolean shouldUseSpecialCountRender(ItemStack itemstack) {
		return itemstack.getCount() > 99;
	}

	private void renderSlotOverlay(GuiGraphicsExtractor guiGraphics, Slot slot, int slotColor) {
		renderSlotOverlay(guiGraphics, slot, slotColor, 0, 16);
	}

	private void renderSlotOverlay(GuiGraphicsExtractor guiGraphics, Slot slot, int slotColor, int yOffset, int height) {
		renderOverlay(guiGraphics, slotColor, slot.x, slot.y + yOffset, 16, height);
	}

	public void renderOverlay(GuiGraphicsExtractor guiGraphics, int slotColor, int xPos, int yPos, int width, int height) {
		guiGraphics.fillGradient(xPos, yPos, xPos + width, yPos + height, slotColor, slotColor);
	}

	protected void drawSlotBg(GuiGraphicsExtractor guiGraphics, int x, int y, int visibleSlotsCount) {
		int slotsOnLine = getSlotsOnLine();
		int slotRows = visibleSlotsCount / slotsOnLine;
		int remainingSlots = visibleSlotsCount % slotsOnLine;
		GuiHelper.renderSlotsBackground(guiGraphics, x + StorageScreenBase.SLOTS_X_OFFSET, y + StorageScreenBase.SLOTS_Y_OFFSET, slotsOnLine, slotRows, remainingSlots);
	}

	private void drawSlotOverlays(GuiGraphicsExtractor guiGraphics) {
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(sophisticatedCore_getGuiLeft(), sophisticatedCore_getGuiTop());
		for (int slotNumber = 0; slotNumber < menu.getNumberOfStorageInventorySlots(); slotNumber++) {
			List<Integer> colors = menu.getSlotOverlayColors(slotNumber);
			if (!colors.isEmpty()) {
				int stripeHeight = 16 / colors.size();
				int i = 0;
				for (int slotColor : colors) {
					int yOffset = i * stripeHeight;
					renderSlotOverlay(guiGraphics, menu.getSlot(slotNumber), slotColor & 0x00_FFFFFF | 0x50_000000, yOffset, i == colors.size() - 1 ? 16 - yOffset : stripeHeight);
					i++;
				}
			}
		}
		poseStack.popMatrix();
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
		inventoryParts.values().forEach(part -> part.extractTooltip(this, guiGraphics, x, y));
		if (getMenu().getCarried().isEmpty() && hoveredSlot != null) {
			if (hoveredSlot.hasItem()) {
				super.extractTooltip(guiGraphics, x, y);
			} else if (hoveredSlot instanceof INameableEmptySlot emptySlot && emptySlot.hasEmptyTooltip()) {
				guiGraphics.setComponentTooltipForNextFrame(font, Collections.singletonList(emptySlot.getEmptyTooltip()), x, y);
			}
		}
		if (settingsTabControl != null) {
			settingsTabControl.extractTooltip(this, guiGraphics, x, y);
		}
		// 升级开关是浮在升级栏左侧的独立控件，不画 tooltip 的话看不出开/关状态
		upgradeSwitches.forEach(us -> us.extractTooltip(this, guiGraphics, x, y));
		if (sortButton != null) {
			sortButton.extractTooltip(this, guiGraphics, x, y);
		}
		if (sortByButton != null) {
			sortByButton.extractTooltip(this, guiGraphics, x, y);
		}
		if (transferToStorageButton != null) {
			transferToStorageButton.extractTooltip(this, guiGraphics, x, y);
		}
		if (transferToInventoryButton != null) {
			transferToInventoryButton.extractTooltip(this, guiGraphics, x, y);
		}
		if (searchBox != null) {
			searchBox.extractTooltip(this, guiGraphics, x, y);
		}
	}

	@Override
	protected List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
		List<Component> ret = getTooltipFromItem(minecraft, itemStack);
		if (hoveredSlot != null && hoveredSlot instanceof StorageInventorySlot && hoveredSlot.getMaxStackSize() != itemStack.getMaxStackSize()) {
			ret.add(Component.translatable(TranslationHelper.INSTANCE.translGuiTooltip("stack_count"),
							Component.literal(NumberFormat.getNumberInstance().format(itemStack.getCount())).withStyle(ChatFormatting.DARK_AQUA)
									.append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
									.append(Component.literal(NumberFormat.getNumberInstance().format(hoveredSlot.getMaxStackSize(itemStack))).withStyle(ChatFormatting.DARK_AQUA)))
					.withStyle(ChatFormatting.GRAY)
			);
		}
		return ret;
	}

	public void drawInventoryBg(GuiGraphicsExtractor guiGraphics, int x, int y, Identifier textureName) {
		StorageGuiHelper.renderStorageBackground(new Position(x, y), guiGraphics, textureName, imageWidth, imageHeight - HEIGHT_WITHOUT_STORAGE_SLOTS);
	}

	private void drawUpgradeBackground(GuiGraphicsExtractor guiGraphics) {
		if (numberOfUpgradeSlots == 0) {
			return;
		}

		int heightWithoutBottom = getUpgradeHeightWithoutBottom();

		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, leftPos - UPGRADE_INVENTORY_OFFSET, topPos, 0, 0, 26, 4, 256, 256);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, leftPos - UPGRADE_INVENTORY_OFFSET, topPos + 4, 0, 4, 25, heightWithoutBottom - 4, 256, 256);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, leftPos - UPGRADE_INVENTORY_OFFSET, topPos + heightWithoutBottom, 0, 198, 25, UPGRADE_BOTTOM_HEIGHT, 256, 256);

		boolean previousHasSwitch = false;
		for (int slot = 0; slot < numberOfUpgradeSlots; slot++) {
			if (menu.canDisableUpgrade(slot)) {
				int y = topPos + 5 + slot * UPGRADE_SLOT_HEIGHT + (previousHasSwitch ? 1 : 0);

				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, leftPos - UPGRADE_INVENTORY_OFFSET - 4, y, 0,
						204 + (previousHasSwitch ? 1 : 0), 7, 18 - (previousHasSwitch ? 1 : 0), 256, 256);
				previousHasSwitch = true;
			} else {
				previousHasSwitch = false;
			}
		}
	}

	public UpgradeSettingsTabControl getUpgradeSettingsControl() {
		return settingsTabControl;
	}

	/**
	 * 26.3 replaced {@code AbstractContainerScreen.findSlot} with a private hovered-slot lookup, so the screen scans the
	 * menu slots itself and additionally asks the scroll panel for the slot under the cursor.
	 */
	@Nullable
	private Slot findSlotInMenu(double mouseX, double mouseY) {
		for (Slot slot : menu.slots) {
			if (isHoveringSlot(slot, mouseX, mouseY) && slot.isActive()) {
				return slot;
			}
		}

		return null;
	}

	@Nullable
	public Slot findSlot(double mouseX, double mouseY) {
		for (int i = 0; i < menu.upgradeSlots.size(); ++i) {
			Slot slot = menu.upgradeSlots.get(i);
			if (isHoveringSlot(slot, mouseX, mouseY) && slot.isActive()) {
				return slot;
			}
		}

		if (inventoryScrollPanel != null) {
			Optional<Slot> result = inventoryScrollPanel.findSlot(mouseX, mouseY);
			if (result.isPresent()) {
				return result.get();
			}
			Slot slot = findSlotInMenu(mouseX, mouseY);

			return slot == null || menu.isStorageInventorySlot(slot.index) ? null : slot; //if super finds inventory slot that's hidden inside the scroll panel just return null
		} else {
			for (int i = 0; i < menu.realInventorySlots.size(); ++i) {
				Slot slot = menu.realInventorySlots.get(i);
				if (isHoveringSlot(slot, mouseX, mouseY) && slot.isActive()) {
					return slot;
				}
			}
			return findSlotInMenu(mouseX, mouseY);
		}
	}


	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		for (UpgradeInventoryPartBase<?> inventoryPart : inventoryParts.values()) {
			if (inventoryPart.handleMouseReleased(mouseX, mouseY, button)) {
				return true;
			}
		}

		handleQuickMoveAll(mouseX, mouseY, button);

		return super.mouseReleased(event);
	}

	private void handleQuickMoveAll(double mouseX, double mouseY, int button) {
		Slot slot = findSlot(mouseX, mouseY);
		if (!getMenu().getCarried().isEmpty() && slot != null && button == 0 && menu.canTakeItemForPickAll(ItemStack.EMPTY, slot) && GuiHelper.isShiftDown() && !lastQuickMoved.isEmpty()) {
			for (Slot slot2 : menu.realInventorySlots) {
				tryQuickMoveSlot(button, slot, slot2);
			}
		}
	}

	private void tryQuickMoveSlot(int button, Slot slot, Slot slot2) {
		//noinspection ConstantConditions - by this point minecraft isn't null
		if (slot2.mayPickup(minecraft.player) && slot2.hasItem() && slot2.sophisticatedCore_isSameInventory(slot)) {
			ItemStack slotItem = slot2.getItem();
			if (ItemStack.isSameItemSameComponents(lastQuickMoved, slotItem)) {
				if (slotItem.getCount() > slotItem.getMaxStackSize()) {
					PacketDistributor.sendToServer(new TransferFullSlotPayload(slot2.index));
				} else {
					slotClicked(slot2, slot2.index, button, ContainerInput.QUICK_MOVE);
				}
			}
		}
	}

	@Override
	protected void slotClicked(Slot slot, int slotNumber, int mouseButton, ContainerInput type) {
		if (type == ContainerInput.PICKUP_ALL && !menu.getSlotUpgradeContainer(slot).map(c -> c.allowsPickupAll(slot)).orElse(true)) {
			type = ContainerInput.PICKUP;
		}

		handleInventoryMouseClick(slotNumber, mouseButton, type);
	}

	private void handleInventoryMouseClick(int slotNumber, int mouseButton, ContainerInput type) {
		StorageContainerMenuBase<?> menu = getMenu();
		if (minecraft == null || minecraft.gameMode == null || minecraft.player == null) {
			return;
		}

		// 26.3 的点击包要带 HashedStack，手工拼包已不可行，交给游戏模式统一构造并发送
		minecraft.gameMode.handleContainerInput(menu.containerId, slotNumber, mouseButton, type, minecraft.player);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		Slot slot = findSlot(mouseX, mouseY);
		if (GuiHelper.isShiftDown() && GuiHelper.isControlDown() && slot instanceof StorageInventorySlot && button == 0) {
			PacketDistributor.sendToServer(new TransferFullSlotPayload(slot.index));
			return true;
		}
		// 整理 / 排序方式两个 12×12 按钮压在标题行最右侧，和搜索框同一条水平线：
		// 父类的派发是按 children 顺序找“第一个命中”的控件，命中者返回 false 时整次点击就被吞掉，
		// 不再问后面的控件。只要有任何先加的控件（滚动面板、页签容器、搜索框）在这块区域命中，
		// 按钮就永远收不到点击。这里直接对这两个按钮做一次命中转发，保证点击落到 onClick。
		if (sortButton != null && sortButton.mouseClicked(event, isDoubleClick)) {
			return true;
		}
		if (sortByButton != null && sortByButton.mouseClicked(event, isDoubleClick)) {
			return true;
		}
		GuiEventListener focused = getFocused();
		if (focused != null && !focused.isMouseOver(mouseX, mouseY) && (focused instanceof WidgetBase widgetBase)) {
			// 焦点以屏幕持有的那份为准：只清控件自己的标记，屏幕仍然认为搜索框是 focused，
			// 之后每帧的焦点对齐又会把输入焦点塞回它，点别处也继续吃按键。
			widgetBase.setFocused(false);
			setFocused((GuiEventListener) null);
		}

		// 26.3 的父类找槽位只扫 menu.slots，而升级槽 / no-sort 槽从来没进过那份列表
		// （它们只在 upgradeSlots / realInventorySlots 里），所以按坐标永远找不到：
		// 升级放不进去、放进去也拿不下来。这类槽位在这里直接按普通点击语义转发。
		if (slot != null && !menu.slots.contains(slot)) {
			clickSlotNotInMenu(slot, event, isDoubleClick);
			return true;
		}

		return super.mouseClicked(event, isDoubleClick);
	}

	/**
	 * 模拟 26.3 父类的槽位点击语义（左/右键、Shift 快速移动、双击收集），
	 * 用于父类看不到的那部分槽位。真正的发包仍然走 {@link #slotClicked} 的同一条路。
	 */
	private void clickSlotNotInMenu(Slot slot, MouseButtonEvent event, boolean isDoubleClick) {
		int button = event.button();
		int containerButton = switch (button) {
			case 1 -> 0; // 右键
			case 3 -> 1; // 中键：和父类一致，当成取出一半
			default -> button;
		};
		ContainerInput input;
		if (button == 0 && event.hasShiftDown()) {
			input = ContainerInput.QUICK_MOVE;
		} else if (isDoubleClick && getMenu().getCarried().isEmpty()) {
			input = ContainerInput.PICKUP_ALL;
		} else {
			input = ContainerInput.PICKUP;
		}
		SophisticatedCore.LOGGER.debug("Slot {} is not part of menu.slots, forwarding click directly (button {}, input {})", slot.index, containerButton, input);
		slotClicked(slot, slot.index, containerButton, input);
	}

	/**
	 * 26.3 的 {@code ContainerEventHandler.mouseClicked} 只要命中某个子控件就返回 true（哪怕该控件自己
	 * 返回 false），{@code AbstractContainerScreen} 随即整段跳过槽位处理。滚动面板正好盖住所有存储格，
	 * 命中它就等于把格子点击全吞掉 —— 行数多到出滚动条的界面（背包最典型）因此“有时完全点不动”。
	 * 这里在“光标下确实是槽位、且最上层是滚动面板”时让开面板，让父类继续走槽位流程。
	 */
	@Override
	public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
		List<? extends GuiEventListener> children = children();
		boolean slotUnderMouse = findSlot(mouseX, mouseY) != null;
		for (int i = children.size() - 1; i >= 0; i--) {
			GuiEventListener child = children.get(i);
			if (slotUnderMouse && child == inventoryScrollPanel) {
				continue;
			}
			if (child.isMouseOver(mouseX, mouseY)) {
				return Optional.of(child);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		for (GuiEventListener child : children()) {
			if (child.isMouseOver(mouseX, mouseY) && child.mouseDragged(event, dragX, dragY)) {
				return true;
			}
		}
		Slot slot = findSlot(mouseX, mouseY);
		ItemStack itemstack = getMenu().getCarried();
		if (isQuickCrafting) {
			if (slot != null && !itemstack.isEmpty()
					&& (itemstack.getCount() > quickCraftSlots.size() || quickCraftingType == 2)
					&& StorageContainerMenuBase.canItemQuickReplace(slot, itemstack) && slot.mayPlace(itemstack)
					&& menu.canDragTo(slot)
					&& isAllowedSlotCombination(slot, itemstack)) {
				quickCraftSlots.add(slot);
					}
			return true;
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	private boolean isAllowedSlotCombination(Slot slot, ItemStack carried) {
		if (quickCraftSlots.isEmpty() || !(carried.getItem() instanceof UpgradeItemBase<?> upgradeItem) || upgradeItem.getInventoryColumnsTaken() == 0) {
			return true;
		}
		return quickCraftSlots.contains(slot) || (!(quickCraftSlots.iterator().next() instanceof StorageContainerMenuBase.StorageUpgradeSlot) && !(slot instanceof StorageContainerMenuBase.StorageUpgradeSlot));
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn) {
		return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn) && hasClickedOutsideOfUpgradeSlots(mouseX, mouseY)
				&& hasClickedOutsideOfUpgradeSettings(mouseX, mouseY);
	}

	private boolean hasClickedOutsideOfUpgradeSettings(double mouseX, double mouseY) {
		return settingsTabControl.getTabRectangles().stream().noneMatch(r -> r.contains((int) mouseX, (int) mouseY));
	}

	private boolean hasClickedOutsideOfUpgradeSlots(double mouseX, double mouseY) {
		return !getUpgradeSlotsRectangle().map(r -> r.contains((int) mouseX, (int) mouseY)).orElse(false);
	}

	public Optional<Rect2i> getUpgradeSlotsRectangle() {
		return numberOfUpgradeSlots == 0 ?
				Optional.empty() :
				GuiHelper.getPositiveRectangle(leftPos - UPGRADE_INVENTORY_OFFSET - (!upgradeSwitches.isEmpty() ? 4 : 0), topPos, UPGRADE_INVENTORY_OFFSET + (!upgradeSwitches.isEmpty() ? 4 : 0), getUpgradeHeight());
	}

	private void renderStackCount(GuiGraphicsExtractor guiGraphics, String count, int x, int y) {
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(0.0F, 0.0F);
		float scale = Math.min(1f, (float) 16 / font.width(count));
		if (scale < 1f) {
			poseStack.scale(scale, scale);
		}
		guiGraphics.text(font, count, (int)((x + 19 - 2 - (font.width(count) * scale)) / scale), (int)((y + 6 + 3 + (1 / (scale * scale) - 1)) / scale), 0xFFFFFFFF, true);
		poseStack.popMatrix();
	}


	private void renderErrorOverlay(GuiGraphicsExtractor guiGraphics) {
		menu.getErrorUpgradeSlotChangeResult().ifPresent(upgradeSlotChangeResult -> upgradeSlotChangeResult.getErrorMessage().ifPresent(overlayErrorMessage -> {
			Matrix3x2fStack poseStack = guiGraphics.pose();
			poseStack.pushMatrix();
			poseStack.translate(sophisticatedCore_getGuiLeft(), sophisticatedCore_getGuiTop());
			upgradeSlotChangeResult.errorUpgradeSlots().forEach(slotIndex -> {
				Slot upgradeSlot = menu.getSlot(menu.getFirstUpgradeSlot() + slotIndex);
				GuiHelper.renderSlotHighlight(guiGraphics, upgradeSlot.x, upgradeSlot.y, 0, ERROR_SLOT_COLOR);
			});
			upgradeSlotChangeResult.errorInventorySlots().forEach(slotIndex -> {
				Slot slot = menu.getSlot(slotIndex);
				//noinspection ConstantConditions
				if (slot != null) {
					GuiHelper.renderSlotHighlight(guiGraphics, slot.x, slot.y, 0, ERROR_SLOT_COLOR);
				}
			});
			upgradeSlotChangeResult.errorInventoryParts().forEach(partIndex -> {
				if (inventoryParts.size() > partIndex) {
					UpgradeInventoryPartBase<?> inventoryPart = inventoryParts.get(partIndex);
					if (inventoryPart != null) {
						inventoryPart.renderErrorOverlay(guiGraphics);
					}
				}
			});
			poseStack.popMatrix();

			renderErrorMessage(guiGraphics, poseStack, overlayErrorMessage);
		}));
	}

	private void renderErrorMessage(GuiGraphicsExtractor guiGraphics, Matrix3x2fStack matrixStack, Component overlayErrorMessage) {
		matrixStack.pushMatrix();
		matrixStack.translate((float) width / 2, (float) ((double) topPos + inventoryLabelY + 4));
		Font fontrenderer = Minecraft.getInstance().font;

		int tooltipWidth = font.width(overlayErrorMessage);

		List<FormattedCharSequence> wrappedTextLines = new ArrayList<>();
		int maxLineWidth = 260;
		if (tooltipWidth > maxLineWidth) {
			int wrappedTooltipWidth = 0;
			List<FormattedCharSequence> wrappedLine = font.split(overlayErrorMessage, maxLineWidth);

			for (FormattedCharSequence line : wrappedLine) {
				int lineWidth = font.width(line);
				if (lineWidth > wrappedTooltipWidth) {
					wrappedTooltipWidth = lineWidth;
				}
				wrappedTextLines.add(line);
			}
			tooltipWidth = wrappedTooltipWidth;
		} else {
			wrappedTextLines.add(overlayErrorMessage.getVisualOrderText());
		}

		int tooltipHeight = 8;
		if (wrappedTextLines.size() > 1) {
			tooltipHeight += 2 + (wrappedTextLines.size() - 1) * 10;
		}

		float leftX = (float) -tooltipWidth / 2;

		GuiHelper.renderTooltipBackground(guiGraphics, tooltipWidth, (int) leftX, 0, tooltipHeight, StorageScreenBase.ERROR_BACKGROUND_COLOR, StorageScreenBase.ERROR_BORDER_COLOR, StorageScreenBase.ERROR_BORDER_COLOR);
		GuiHelper.writeTooltipLines(guiGraphics, wrappedTextLines, fontrenderer, leftX, 0, ERROR_TEXT_COLOR);
		matrixStack.popMatrix();
	}

	@Override
	public void renderInventorySlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean canShowHover) {
		renderStorageInventorySlots(guiGraphics, mouseX, mouseY, canShowHover);
	}

	@Override
	public boolean isMouseOverSlot(Slot slot, double mouseX, double mouseY) {
		return isHoveringSlot(slot, mouseX, mouseY);
	}

	public boolean isHoveringSlot(Slot slot, double mouseX, double mouseY) {
		return isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && getUpgradeSettingsControl().slotIsNotCoveredAt(slot, mouseX, mouseY);
	}

	@Override
	public int getTopY() {
		return sophisticatedCore_getGuiTop();
	}

	@Override
	public void drawSlotBg(GuiGraphicsExtractor guiGraphics, int visibleSlotsCount) {
		// 滚动面板的 drawBackground 在绝对屏幕坐标里跑（scissor 也是绝对坐标），这里必须用 leftPos/topPos，
		// 不能再算一次 (width - imageWidth) / 2 —— 那正是之前「物品与格子错位」的来源
		drawSlotBg(guiGraphics, leftPos, topPos, visibleSlotsCount);
		drawSlotOverlays(guiGraphics);
	}

	@Override
	public int getLeftX() {
		return sophisticatedCore_getGuiLeft();
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		settingsTabControl.tick();
	}

	private class TransferButton extends Button {
		private final ButtonDefinition shiftDefinition;
		private final ButtonDefinition definition;

		public TransferButton(Consumer<Boolean> transferItems, ButtonDefinition shiftDefinition, ButtonDefinition definition) {
			super(new Position(StorageScreenBase.this.leftPos, StorageScreenBase.this.topPos), definition, button -> {
				if (button == 0) {
					transferItems.accept(!GuiHelper.isShiftDown());
				}
			});
			this.shiftDefinition = shiftDefinition;
			this.definition = definition;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
			if (GuiHelper.isShiftDown()) {
				GuiHelper.blit(guiGraphics, x, y, shiftDefinition.getForegroundTexture());
			} else {
				GuiHelper.blit(guiGraphics, x, y, definition.getForegroundTexture());
			}
		}

		@Override
		protected List<Component> getTooltip() {
			if (GuiHelper.isShiftDown()) {
				return shiftDefinition.getTooltip();
			} else {
				return definition.getTooltip();
			}
		}
	}

}
