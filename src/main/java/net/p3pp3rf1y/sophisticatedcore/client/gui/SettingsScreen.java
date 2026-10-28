package net.p3pp3rf1y.sophisticatedcore.client.gui;

import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix3x2fStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.InventoryScrollPanel;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageBackgroundProperties;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class SettingsScreen extends AbstractContainerScreen<SettingsContainerMenu<?>> implements InventoryScrollPanel.IInventoryScreen {
	public static final int HEIGHT_WITHOUT_STORAGE_SLOTS = 114;
	public static final Predicate<ItemStack> MATCH_ALL_FILTER = stack -> true;
	private StorageSettingsTabControlBase settingsTabControl;
	private InventoryScrollPanel inventoryScrollPanel = null;
	private TemplatePersistanceControl templatePersistanceControl = null;
	/** 26.3 made the parent fields final; this screen is sized from the menu so it shadows them. */
	protected int imageWidth;
	protected int imageHeight;
	private StorageBackgroundProperties storageBackgroundProperties;
	private boolean mouseDragHandledByOther = false;
	/**
	 * 26.3 的 {@code ContainerEventHandler.mouseClicked} 命中任一子控件就返回 true（哪怕控件自己没处理），
	 * 设置界面里页签、模板控件、装饰图标都会命中，于是设置项/按键的点击全被吞掉。
	 * 这里自己按「最上层优先、未处理就继续问」派发，交给父类走槽位逻辑时把 getChildAt 关掉。
	 */
	private boolean suppressChildHit = false;

	protected SettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		updateDimensionsAndSlotPositions(Minecraft.getInstance().getWindow().getGuiScaledHeight());
		settingsTabControl = initializeTabControl();
	}

	@Override
	public void resize(int width, int height) {
		updateDimensionsAndSlotPositions(height);
		super.resize(width, height);
	}

	private void updateDimensionsAndSlotPositions(int height) {
		int totalRows = getMenu().getNumberOfRows();
		int rowsThatFit = (height - HEIGHT_WITHOUT_STORAGE_SLOTS) / 18;
		// 矮窗口兜底：行数算成 0 会让滚动面板高度为 0，所有槽位被布局到 -100，界面一格都没有
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
	}

	protected int getStorageInventoryHeight(int displayableNumberOfRows) {
		return displayableNumberOfRows * 18;
	}

	private void updateInventoryScrollPanel() {
		if (inventoryScrollPanel != null) {
			removeWidget(inventoryScrollPanel);
		}

		int numberOfVisibleRows = getNumberOfVisibleRows();
		if (numberOfVisibleRows < getMenu().getNumberOfRows()) {
			inventoryScrollPanel = new InventoryScrollPanel(Minecraft.getInstance(), this, 0, getMenu().getNumberOfStorageInventorySlots(), getSlotsOnLine(), numberOfVisibleRows * 18, topPos + StorageScreenBase.SLOTS_Y_OFFSET, leftPos + StorageScreenBase.SLOTS_X_OFFSET);
			addRenderableWidget(inventoryScrollPanel);
			inventoryScrollPanel.updateSlotsPosition();
		} else {
			inventoryScrollPanel = null;
		}
	}

	private int getNumberOfVisibleRows() {
		return Math.min((imageHeight - HEIGHT_WITHOUT_STORAGE_SLOTS) / 18, getMenu().getNumberOfRows());
	}

	protected void updateStorageSlotsPositions() {
		int yPosition = 18;

		int slotIndex = 0;
		while (slotIndex < getMenu().getNumberOfStorageInventorySlots()) {
			Slot slot = getMenu().getSlot(slotIndex);
			int lineIndex = slotIndex % getSlotsOnLine();
			slot.x = 8 + lineIndex * 18;
			slot.y = yPosition;

			slotIndex++;
			if (slotIndex % getSlotsOnLine() == 0) {
				yPosition += 18;
			}
		}
	}

	public int getSlotsOnLine() {
		return Math.max(1, storageBackgroundProperties.getSlotsOnLine() - getMenu().getColumnsTaken());
	}

	@Override
	protected void init() {
		super.init();
		updateDimensionsAndSlotPositions(height);
		updateInventoryScrollPanel();
		settingsTabControl = initializeTabControl();
		templatePersistanceControl = initializeTemplatePersistanceControl();
		addWidget(settingsTabControl);
		addWidget(templatePersistanceControl);
		// 从容器界面切进来时把光标拨回切换前的位置（26.3 的 setScreen 会两次 warp 到窗口正中）
		CursorRestorer.restoreIfPending();
	}

	private TemplatePersistanceControl initializeTemplatePersistanceControl() {
		return new TemplatePersistanceControl(new Position(leftPos + inventoryLabelX - 29, topPos + inventoryLabelY + 29), getMenu().getTemplatePersistanceContainer());
	}

	protected abstract StorageSettingsTabControlBase initializeTabControl();

	/**
	 * 26.3 三段式之一：模糊背景 → 本模组背景板 → 设置页签。
	 * 页签必须画在背景板之后，否则整块背景会把它盖住（这就是「侧边页签不见了」的另一半原因）。
	 */
	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		// Same as the container screen: the warp of Gui.setScreen happens after init(), so restore on the first frame.
		CursorRestorer.restoreWhenFrameRendered();
		menu.detectSettingsChangeAndReload();
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

		StorageGuiHelper.renderStorageBackground(new Position(leftPos, topPos), guiGraphics, storageBackgroundProperties.getTextureName(), imageWidth, getStorageInventoryHeight(getNumberOfVisibleRows()));
		if (inventoryScrollPanel == null) {
			drawSlotBg(guiGraphics, leftPos, topPos, getMenu().getStorageInventorySlots().size());
		}
		settingsTabControl.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		templatePersistanceControl.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		hoveredSlot = findSlot(mouseX, mouseY);

		for (GuiEventListener child : children()) {
			if (child == settingsTabControl || child == templatePersistanceControl) {
				continue; // 这两个已经在背景阶段画过
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

		// 展开的页签要盖在槽位上面
		settingsTabControl.extractForeground(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		// 父类会依次调 extractBackground / extractContents / 手上物品 / extractTooltip，不要再手动重复调一遍
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		settingsTabControl.extractTooltip(this, guiGraphics, mouseX, mouseY);
		templatePersistanceControl.extractTooltip(this, guiGraphics, mouseX, mouseY);
	}

	protected void drawSlotBg(GuiGraphicsExtractor guiGraphics, int x, int y, int visibleSlotsCount) {
		int slotsOnLine = getSlotsOnLine();
		int slotRows = visibleSlotsCount / slotsOnLine;
		int remainingSlots = visibleSlotsCount % slotsOnLine;
		GuiHelper.renderSlotsBackground(guiGraphics, x + StorageScreenBase.SLOTS_X_OFFSET, y + StorageScreenBase.SLOTS_Y_OFFSET, slotsOnLine, slotRows, remainingSlots);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.extractLabels(guiGraphics, mouseX, mouseY);
		if (inventoryScrollPanel == null) {
			renderInventorySlots(guiGraphics, mouseX, mouseY, true);
		}
	}

	@Override
	public void renderInventorySlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean canShowHover) {
		for (int slotId = 0; slotId < menu.ghostSlots.size(); ++slotId) {
			Slot slot = menu.ghostSlots.get(slotId);
			extractSlot(guiGraphics, slot, mouseX, mouseY);

			settingsTabControl.renderSlotOverlays(guiGraphics, slot, this::renderSlotOverlay, isTemplateLoadHovered());

			if (canShowHover && isHovering(slot, mouseX, mouseY) && slot.isActive()) {
				hoveredSlot = slot;
				GuiHelper.renderSlotHighlight(guiGraphics, slot.x, slot.y, 0, sophisticatedCore_getSlotColor(slotId));
			}

			settingsTabControl.renderSlotExtra(guiGraphics, slot);
		}
	}

	@Override
	public void extractSlot(GuiGraphicsExtractor guiGraphics, Slot slot, int mouseX, int mouseY) {
		ItemStack itemstack = slot.getItem() != ItemStack.EMPTY ? slot.getItem() : settingsTabControl.getSlotStackDisplayOverride(slot.sophisticatedCore_getSlotIndex(), isTemplateLoadHovered());

		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(0, 0);
		//noinspection ConstantConditions - by this point minecraft isn't null
		if (!settingsTabControl.renderGuiItem(guiGraphics, itemstack, slot, isTemplateLoadHovered())) {
			if (!getMenu().getSlotFilterItem(slot.index).isEmpty()) {
				guiGraphics.item(getMenu().getSlotFilterItem(slot.index), slot.x, slot.y);
			} else {
				Identifier noItemIcon = slot.getNoItemIcon();
				if (noItemIcon != null) {
					GuiHelper.renderSlotIcon(guiGraphics, noItemIcon, slot.x, slot.y);
				}
			}
		}
		poseStack.popMatrix();

		settingsTabControl.drawSlotStackOverlay(guiGraphics, slot, isTemplateLoadHovered());
	}

	private boolean isTemplateLoadHovered() {
		return templatePersistanceControl.isTemplateLoadHovered();
	}

	@SuppressWarnings("java:S2589") // slot can actually be null despite being marked non null
	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type) {
		//noinspection ConstantConditions
		if (slot != null) {
			settingsTabControl.handleSlotClick(slot, mouseButton);
		}
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		double mouseX = event.x();
		double mouseY = event.y();
		// 26.3 的 MouseButtonEvent 给的是 1/3，而设置页按容器编号 0/1 解释；拖选必须与点击走同一套换算
		int button = StorageScreenBase.getContainerClickButton(event.button());
		if (mouseDragHandledByOther) {
			return false;
		}
		Slot slot = findSlot(mouseX, mouseY);
		if (slot != null) {
			settingsTabControl.handleSlotClick(slot, button);
		}
		for (GuiEventListener child : children()) {
			if (child.isMouseOver(mouseX, mouseY) && child.mouseDragged(event, dragX, dragY)) {
				return true;
			}
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Nullable
	public Slot findSlot(double mouseX, double mouseY) {
		for (int i = 0; i < menu.ghostSlots.size(); ++i) {
			Slot slot = menu.ghostSlots.get(i);
			if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot.isActive()) {
				return slot;
			}
		}

		return null;
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn) {
		return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn) && hasClickedOutsideOfSettings(mouseX, mouseY);
	}

	private boolean hasClickedOutsideOfSettings(double mouseX, double mouseY) {
		return settingsTabControl.getTabRectangles().stream().noneMatch(r -> r.contains((int) mouseX, (int) mouseY));
	}

	/**
	 * 26.3 的父类派发按 children 顺序取“第一个命中”的控件；命中的若是纯装饰（页签标签、面板底图、模板控件）且它返回 false，
	 * 这次点击就整颗丢掉，后面的设置项永远收不到。改成从后往前（后加的在上面）取最上层命中的控件。
	 */
	@Override
	public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
		if (suppressChildHit) {
			return Optional.empty();
		}
		List<? extends GuiEventListener> children = children();
		for (int i = children.size() - 1; i >= 0; i--) {
			GuiEventListener child = children.get(i);
			if (child.isMouseOver(mouseX, mouseY)) {
				return Optional.of(child);
			}
		}
		return Optional.empty();
	}

	/**
	 * 设置项的点击必须落到真正能处理它的控件上。
	 * <p>
	 * 26.3 的父类派发取“第一个命中”的控件，并且只要命中就返回 true（即使控件自己不处理），
	 * 于是页签/模板控件/幽灵槽图标命中时，后面的设置项按钮永远收不到点击 —— 玩家看到的就是
	 * “背包设置里的各类按键全都没反应”。这里按最上层优先逐个询问，只有真正处理了才结束；
	 * 都不处理时再交给父类做槽位逻辑（这一小段里让 getChildAt 返回空，避免又被吞一次）。
	 */
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		List<? extends GuiEventListener> children = children();
		for (int i = children.size() - 1; i >= 0; i--) {
			GuiEventListener child = children.get(i);
			if (!child.isMouseOver(mouseX, mouseY)) {
				continue;
			}
			if (child.mouseClicked(event, isDoubleClick)) {
				setFocused(child);
				if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
					setDragging(true);
				}
				return true;
			}
		}

		suppressChildHit = true;
		try {
			return super.mouseClicked(event, isDoubleClick);
		} finally {
			suppressChildHit = false;
		}
	}

	private void renderSlotOverlay(GuiGraphicsExtractor guiGraphics, int xPos, int yPos, int height, int slotColor) {
		guiGraphics.fillGradient(xPos, yPos, xPos + 16, yPos + height, slotColor, slotColor);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();
		int scanCode = event.keycode();
		int modifiers = event.modifiers();
		if (keyCode == InputConstants.KEY_ESCAPE) {
			// 关设置 → 服务端重开容器界面，同样会走 setScreen(null)+setScreen(新界面) 的两次光标 warp
			CursorRestorer.remember();
			sendStorageInventoryScreenOpenMessage();
			return true;
		}
		return super.keyPressed(event);
	}

	protected abstract void sendStorageInventoryScreenOpenMessage();

	public StorageSettingsTabControlBase getSettingsTabControl() {
		return settingsTabControl;
	}

	@Override
	public boolean isMouseOverSlot(Slot slot, double mouseX, double mouseY) {
		return isHovering(slot, mouseX, mouseY);
	}

	@Override
	public void drawSlotBg(GuiGraphicsExtractor guiGraphics, int visibleSlotsCount) {
		drawSlotBg(guiGraphics, leftPos, topPos, visibleSlotsCount);
	}

	@Override
	public int getTopY() {
		return sophisticatedCore_getGuiTop();
	}

	@Override
	public int getLeftX() {
		return sophisticatedCore_getGuiLeft();
	}

	@Override
	public Slot getSlot(int slotIndex) {
		return getMenu().getSlot(slotIndex);
	}

	public void startMouseDragHandledByOther() {
		mouseDragHandledByOther = true;
	}

	public void stopMouseDragHandledByOther() {
		mouseDragHandledByOther = false;
	}

	@Override
	public Predicate<ItemStack> getStackFilter() {
		return MATCH_ALL_FILTER;
	}
}
