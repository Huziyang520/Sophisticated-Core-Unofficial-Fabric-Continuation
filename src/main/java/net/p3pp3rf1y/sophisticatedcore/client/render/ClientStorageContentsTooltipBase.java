package net.p3pp3rf1y.sophisticatedcore.client.render;

import org.joml.Matrix3x2fStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.*;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.CountAbbreviator;
import net.p3pp3rf1y.sophisticatedcore.fluid.FluidUtil;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

import org.jetbrains.annotations.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Storage contents tooltip shared by storage items (backpacks, chests, barrels, shulker boxes).
 * <p>
 * Since 26.x a tooltip is rendered in two passes over all of its components: {@code extractText} draws the text of
 * every component first, then {@code extractImage} draws every component's images on top. Drawing text from the
 * image pass therefore buried it under the images of the components that follow, and the component reported no
 * height to the positioner, so the tooltip came out as an empty box. This class lays the contents out once into
 * text and item entries and replays the matching subset in each pass.
 * <p>
 * All of that state used to be {@code static}, i.e. shared by every item hovering on screen: one component's uuid
 * decided whether another component refreshed, and the last item to be laid out won the positions. It is per instance
 * now; the only thing that stays global is the "contents changed, rebuild on next draw" flag, because the content
 * payloads arrive without a reference to the tooltip instance.
 */
public abstract class ClientStorageContentsTooltipBase implements ClientTooltipComponent {
	private static final int REFRESH_INTERVAL = 20;
	private static final String STORAGE_ITEM = "storage";
	private static volatile boolean refreshRequested = true;

	@Nullable
	private UUID storageUuid = null;
	private long lastRequestTime = 0;
	private List<IUpgradeWrapper> upgrades = new ArrayList<>();
	private List<ItemStack> sortedContents = new ArrayList<>();
	private final List<Component> tooltipLines = new ArrayList<>();
	private final List<TextEntry> textEntries = new ArrayList<>();
	private final List<ItemEntry> itemEntries = new ArrayList<>();
	private final List<UpgradeToggle> toggles = new ArrayList<>();
	private int height = 0;
	private int width = 0;

	private record TextEntry(Component text, int y) {
	}

	private record ItemEntry(ItemStack stack, int x, int y, boolean withCount) {
	}

	private record UpgradeToggle(int x, int y, boolean enabled) {
	}

	/** Asks every contents tooltip to rebuild on its next draw. */
	public static void refreshContents() {
		refreshRequested = true;
	}

	protected abstract IStorageWrapper getStorageWrapper();

	private void initContents(LocalPlayer player) {
		IStorageWrapper wrapper = getStorageWrapper();
		UUID newUuid = wrapper.getContentsUuid().orElse(null);
		if (storageUuid == null ? newUuid != null : !storageUuid.equals(newUuid)) {
			lastRequestTime = 0;
			storageUuid = newUuid;
			refreshRequested = true;
		}
		if (storageUuid != null) {
			requestContents(player, wrapper);
		}
	}

	protected void setLastRequestTime(long lastRequestTime) {
		this.lastRequestTime = lastRequestTime;
	}

	protected long getLastRequestTime() {
		return lastRequestTime;
	}

	private void requestContents(LocalPlayer player, IStorageWrapper wrapper) {
		if (getLastRequestTime() + REFRESH_INTERVAL < player.level().getGameTime()) {
			setLastRequestTime(player.level().getGameTime());
			wrapper.getContentsUuid().ifPresent(this::sendInventorySyncRequest);
		}
	}

	protected abstract void sendInventorySyncRequest(UUID uuid);

	/**
	 * Whether the contents are readable right now. Contents that travel with the item ({@code BLOCK_ENTITY_DATA}) can be
	 * read immediately; contents kept in a server side copy by uuid need a round trip first. Returning false makes the
	 * tooltip say "syncing" instead of "empty" - both used to read as "there is nothing in this container".
	 */
	protected boolean hasContentsData(IStorageWrapper wrapper) {
		return true;
	}

	private void refreshContents(IStorageWrapper wrapper) {
		sortedContents.clear();
		upgrades.clear();
		tooltipLines.clear();
		boolean contentsDataKnown = hasContentsData(wrapper);
		if (contentsDataKnown) {
			wrapper.onContentsNbtUpdated();
			sortedContents = InventoryHelper.getCompactedStacksSortedByCount(wrapper.getInventoryHandler());
			upgrades = new ArrayList<>(wrapper.getUpgradeHandler().getSlotWrappers().values());
			addMultiplierTooltip(wrapper);
			addFluidTooltip(wrapper);
		}
		if (upgrades.isEmpty() && sortedContents.isEmpty()) {
			String lineKey = TranslationHelper.INSTANCE.translItemTooltip(STORAGE_ITEM) + (contentsDataKnown ? ".empty" : ".syncing");
			tooltipLines.add(Component.translatable(lineKey).withStyle(ChatFormatting.YELLOW));
		}
		layout();
		calculateWidth();
	}

	/**
	 * Turns the current contents into fixed positions that both render passes can replay.
	 */
	private void layout() {
		textEntries.clear();
		itemEntries.clear();
		toggles.clear();
		int y = 0;
		for (Component line : tooltipLines) {
			textEntries.add(new TextEntry(line, y));
			y += 10;
		}
		if (!upgrades.isEmpty()) {
			textEntries.add(new TextEntry(Component.translatable(TranslationHelper.INSTANCE.translItemTooltip(STORAGE_ITEM) + ".upgrades").withStyle(ChatFormatting.YELLOW), y));
			y += 10;
			int x = 0;
			for (IUpgradeWrapper upgradeWrapper : upgrades) {
				if (upgradeWrapper.canBeDisabled()) {
					toggles.add(new UpgradeToggle(x, y + 3, upgradeWrapper.isEnabled()));
					x += 4;
				}
				itemEntries.add(new ItemEntry(upgradeWrapper.getUpgradeStack(), x, y, false));
				x += DEFAULT_STACK_WIDTH;
			}
			y += 20;
		}
		if (!sortedContents.isEmpty()) {
			textEntries.add(new TextEntry(Component.translatable(TranslationHelper.INSTANCE.translItemTooltip(STORAGE_ITEM) + ".inventory").withStyle(ChatFormatting.YELLOW), y));
			y += 10;
			int x = 0;
			for (int i = 0; i < sortedContents.size(); i++) {
				if (i > 0 && i % MAX_STACKS_ON_LINE == 0) {
					x = 0;
					y += 20;
				}
				ItemStack stack = sortedContents.get(i);
				int stackWidth = Math.max(getStackCountWidth(Minecraft.getInstance().font, stack), DEFAULT_STACK_WIDTH);
				itemEntries.add(new ItemEntry(stack, x + stackWidth - DEFAULT_STACK_WIDTH, y, true));
				x += stackWidth;
			}
			y += 20;
		}
		height = y > 0 ? y : 12;
	}

	private void calculateWidth() {
		int upgradesWidth = calculateUpgradesWidth();
		int contentsWidth = calculateContentsWidth();
		int tooltipContentsWidth = calculateTooltipLinesWidth();
		width = Math.max(Math.max(upgradesWidth, contentsWidth), tooltipContentsWidth) + 2;
	}

	private int calculateTooltipLinesWidth() {
		return tooltipLines.stream().map(this::getTooltipWidth).max(Comparator.naturalOrder()).orElse(0);
	}

	private int calculateUpgradesWidth() {
		int upgradesWidth = 0;
		for (IUpgradeWrapper upgradeWrapper : upgrades) {
			upgradesWidth += (upgradeWrapper.canBeDisabled() ? 4 : 0) + DEFAULT_STACK_WIDTH;
		}
		return upgradesWidth;
	}

	private int calculateContentsWidth() {
		Font fontRenderer = Minecraft.getInstance().font;
		int contentsWidth = 0;
		for (int i = 0; i < sortedContents.size() && i < MAX_STACKS_ON_LINE; i++) {
			contentsWidth += Math.max(getStackCountWidth(fontRenderer, sortedContents.get(i)), DEFAULT_STACK_WIDTH);
		}
		return contentsWidth;
	}

	private int getStackCountWidth(Font fontRenderer, ItemStack stack) {
		return fontRenderer.width(CountAbbreviator.abbreviate(stack.getCount())) + COUNT_PADDING;
	}

	private int getTooltipWidth(Component component) {
		return Minecraft.getInstance().font.width(component.getVisualOrderText());
	}

	private void addMultiplierTooltip(IStorageWrapper wrapper) {
		double multiplier = wrapper.getInventoryHandler().getStackSizeMultiplier();
		if (multiplier > 1) {
			DecimalFormat df = new DecimalFormat("0.###");
			tooltipLines.add(Component.translatable(TranslationHelper.INSTANCE.translItemTooltip(STORAGE_ITEM) + ".stack_multiplier",
					Component.literal(df.format(multiplier)).withStyle(ChatFormatting.WHITE)
			).withStyle(ChatFormatting.GREEN));
		}
	}

	private void addFluidTooltip(IStorageWrapper wrapper) {
		wrapper.getFluidHandler().ifPresent(fluidHandler -> {
			for (StorageView<FluidVariant> view : fluidHandler) {
				if (view.isResourceBlank()) {
					tooltipLines.add(Component.translatable(getEmptyFluidTooltipTranslation()).withStyle(ChatFormatting.BLUE));
				} else {
					tooltipLines.add(Component.translatable(getFluidTooltipTranslation(),
							Component.literal(CountAbbreviator.abbreviate(FluidUtil.toBuckets(view.getAmount()))).withStyle(ChatFormatting.WHITE),
							((MutableComponent) FluidVariantAttributes.getName(view.getResource())).withStyle(ChatFormatting.BLUE)
					));
				}
			}
		});
	}

	protected String getFluidTooltipTranslation() {
		return TranslationHelper.INSTANCE.translItemTooltip(STORAGE_ITEM) + ".fluid";
	}

	protected String getEmptyFluidTooltipTranslation() {
		return TranslationHelper.INSTANCE.translItemTooltip(STORAGE_ITEM) + ".fluid_empty";
	}

	private static final TextureBlitData UPGRADE_ON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(4, 128), Dimension.RECTANGLE_4_10);
	private static final TextureBlitData UPGRADE_OFF = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(0, 128), Dimension.RECTANGLE_4_10);
	private static final int MAX_STACKS_ON_LINE = 9;
	private static final int DEFAULT_STACK_WIDTH = 18;
	private static final int COUNT_PADDING = 2;

	@Override
	public int getWidth(Font font) {
		refreshIfNeeded();
		return width;
	}

	@Override
	public int getHeight(Font font) {
		refreshIfNeeded();
		return height;
	}

	@Override
	public void extractText(GuiGraphicsExtractor guiGraphics, Font font, int leftX, int topY) {
		refreshIfNeeded();
		for (TextEntry entry : textEntries) {
			// The alpha has to be opaque: 0xFFFFFF is 0x00FFFFFF (fully transparent), so the text was drawn but
			// invisible, leaving only the tooltip frame. Vanilla text components use -1 (0xFFFFFFFF) as well.
			guiGraphics.text(font, entry.text(), leftX, topY + entry.y(), 0xFFFFFFFF);
		}
	}

	@Override
	public void extractImage(Font font, int leftX, int topY, int width, int height, GuiGraphicsExtractor guiGraphics) {
		refreshIfNeeded();
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		for (UpgradeToggle toggle : toggles) {
			GuiHelper.blit(guiGraphics, leftX + toggle.x(), topY + toggle.y(), toggle.enabled() ? UPGRADE_ON : UPGRADE_OFF);
		}
		for (ItemEntry entry : itemEntries) {
			int x = leftX + entry.x();
			int y = topY + entry.y();
			guiGraphics.item(entry.stack(), x, y);
			if (entry.withCount() && entry.stack().getCount() > 1) {
				guiGraphics.itemDecorations(font, entry.stack(), x, y, CountAbbreviator.abbreviate(entry.stack().getCount()));
			}
		}
		poseStack.popMatrix();
	}

	/**
	 * Both passes are invoked once per frame, so the contents are only fetched once per refresh flag rather than in
	 * each pass.
	 */
	private void refreshIfNeeded() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		initContents(player);
		if (refreshRequested) {
			refreshRequested = false;
			refreshContents(getStorageWrapper());
		}
	}
}
