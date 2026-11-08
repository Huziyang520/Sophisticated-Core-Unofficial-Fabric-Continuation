package net.p3pp3rf1y.sophisticatedcore.client.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

import javax.annotation.Nullable;
import java.util.function.Predicate;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.MouseButtons;

public abstract class SettingsScreen extends AbstractContainerScreen<SettingsContainerMenu<?>> implements InventoryScrollPanel.IInventoryScreen {
	/**
	 * 26.3 makes the base class dimensions final, so each screen tracks its own.
	 */
	protected int guiWidth;
	protected int guiHeight;
	public static final int HEIGHT_WITHOUT_STORAGE_SLOTS = 114;
	public static final Predicate<ItemStack> MATCH_ALL_FILTER = stack -> true;
	private StorageSettingsTabControlBase settingsTabControl;
	private InventoryScrollPanel inventoryScrollPanel = null;
	private TemplatePersistanceControl templatePersistanceControl = null;
	private StorageBackgroundProperties storageBackgroundProperties;
	private boolean mouseDragHandledByOther = false;

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
		int displayableNumberOfRows = Math.min((height - HEIGHT_WITHOUT_STORAGE_SLOTS) / 18, getMenu().getNumberOfRows());
		int newImageHeight = HEIGHT_WITHOUT_STORAGE_SLOTS + getStorageInventoryHeight(displayableNumberOfRows);
		storageBackgroundProperties = (getMenu().getNumberOfStorageInventorySlots() + getMenu().getColumnsTaken() * getMenu().getNumberOfRows()) <= 81 ? StorageBackgroundProperties.REGULAR_9_SLOT : StorageBackgroundProperties.REGULAR_12_SLOT;

		guiWidth = storageBackgroundProperties.getSlotsOnLine() * 18 + 14;
		updateStorageSlotsPositions();
		if (displayableNumberOfRows < getMenu().getNumberOfRows()) {
			storageBackgroundProperties = storageBackgroundProperties == StorageBackgroundProperties.REGULAR_9_SLOT ? StorageBackgroundProperties.WIDER_9_SLOT : StorageBackgroundProperties.WIDER_12_SLOT;
			guiWidth += 6;
		}
		guiHeight = newImageHeight;
		inventoryLabelY = guiHeight - 94;
		inventoryLabelX = 8 + storageBackgroundProperties.getPlayerInventoryXOffset();
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
			inventoryScrollPanel = new InventoryScrollPanel(Minecraft.getInstance(), this, 0, getMenu().getNumberOfStorageInventorySlots(), getSlotsOnLine(), numberOfVisibleRows * 18, sophisticatedCore_getGuiTop() + 17, sophisticatedCore_getGuiLeft() + 7);
			addRenderableWidget(inventoryScrollPanel);
			inventoryScrollPanel.updateSlotsPosition();
		} else {
			inventoryScrollPanel = null;
		}
	}

	private int getNumberOfVisibleRows() {
		return Math.min((guiHeight - HEIGHT_WITHOUT_STORAGE_SLOTS) / 18, getMenu().getNumberOfRows());
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
		return storageBackgroundProperties.getSlotsOnLine() - getMenu().getColumnsTaken();
	}

	@Override
	protected void init() {
		super.init();
		leftPos = (width - guiWidth) / 2;
		topPos = (height - guiHeight) / 2;
		updateInventoryScrollPanel();
		settingsTabControl = initializeTabControl();
		templatePersistanceControl = initializeTemplatePersistanceControl();
		addWidget(settingsTabControl);
		addWidget(templatePersistanceControl);
	}

	private TemplatePersistanceControl initializeTemplatePersistanceControl() {
		return new TemplatePersistanceControl(new Position(leftPos + inventoryLabelX - 29, topPos + inventoryLabelY + 29), getMenu().getTemplatePersistanceContainer());
	}

	protected abstract StorageSettingsTabControlBase initializeTabControl();

	@Override
	public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int x = (width - guiWidth) / 2;
		int y = (height - guiHeight) / 2;
		StorageGuiHelper.renderStorageBackground(new Position(x, y), guiGraphics, storageBackgroundProperties.getTextureName(), guiWidth, getStorageInventoryHeight(getNumberOfVisibleRows()));
		if (inventoryScrollPanel == null) {
			drawSlotBg(guiGraphics, x, y, getMenu().getStorageInventorySlots().size());
		}
		super.extractContents(guiGraphics, mouseX, mouseY, partialTicks);
	}

	protected void drawSlotBg(GuiGraphicsExtractor guiGraphics, int x, int y, int visibleSlotsCount) {
		int slotsOnLine = getSlotsOnLine();
		int slotRows = visibleSlotsCount / slotsOnLine;
		int remainingSlots = visibleSlotsCount % slotsOnLine;
		GuiHelper.renderSlotsBackground(guiGraphics, x + StorageScreenBase.SLOTS_X_OFFSET, y + StorageScreenBase.SLOTS_Y_OFFSET, slotsOnLine, slotRows, remainingSlots);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		menu.detectSettingsChangeAndReload();

		// Fix for Blur+
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		guiGraphics.nextStratum();
		extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		poseStack.popMatrix();

		settingsTabControl.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		templatePersistanceControl.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		settingsTabControl.renderTooltip(this, guiGraphics, mouseX, mouseY);
		templatePersistanceControl.renderTooltip(this, guiGraphics, mouseX, mouseY);
	}

	@Override
	public void extractTransparentBackground(GuiGraphicsExtractor guiGraphics) {
		Matrix3x2fStack pose = guiGraphics.pose();
		pose.pushMatrix();
		guiGraphics.nextStratum();
		super.extractTransparentBackground(guiGraphics);
		pose.popMatrix();
	}

	@Override
	protected void extractSlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		// Ghost slots are drawn by renderInventorySlots from extractLabels.
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
		guiGraphics.nextStratum();
		//noinspection ConstantConditions - by this point minecraft isn't null
		if (!settingsTabControl.renderGuiItem(guiGraphics, itemstack, slot, isTemplateLoadHovered())) {
			if (!getMenu().getSlotFilterItem(slot.index).isEmpty()) {
				guiGraphics.item(getMenu().getSlotFilterItem(slot.index), slot.x, slot.y);
			} else {
				Identifier noItemIcon = slot.getNoItemIcon();
				if (noItemIcon != null) {
					TextureAtlasSprite textureatlassprite = guiGraphics.getSprite(new net.minecraft.client.resources.model.sprite.SpriteId(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, noItemIcon));
					guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, textureatlassprite, slot.x, slot.y, 16, 16);
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
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		// 26.3 stops at the first hovered child and swallows the click; walk all children like 1.21.1.
		for (GuiEventListener child : children()) {
			if (child.mouseClicked(event, doubleClick)) {
				setFocused(child);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		// 26.3 AbstractContainerScreen no longer forwards scrolling to children.
		for (GuiEventListener child : children()) {
			if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = MouseButtons.toLegacy(event.button());
		if (mouseDragHandledByOther) {
			return false;
		}
		Slot slot = sophisticatedCore_findSlot(mouseX, mouseY);
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

	/**
	 * 26.3 made the base slot lookup private, so this is a local helper now.
	 */
	public Slot sophisticatedCore_findSlot(double mouseX, double mouseY) {
		for (int i = 0; i < menu.ghostSlots.size(); ++i) {
			Slot slot = menu.ghostSlots.get(i);
			if (isHovering(slot, mouseX, mouseY) && slot.isActive()) {
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

	private void renderSlotOverlay(GuiGraphicsExtractor guiGraphics, int xPos, int yPos, int height, int slotColor) {
		guiGraphics.fillGradient(xPos, yPos, xPos + 16, yPos + height, slotColor, slotColor);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();
		if (keyCode == InputConstants.KEY_ESCAPE) {
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
		drawSlotBg(guiGraphics, (width - guiWidth) / 2, (height - guiHeight) / 2, visibleSlotsCount);
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