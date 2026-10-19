package net.p3pp3rf1y.sophisticatedcore.client.gui;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.util.Easing;

import java.util.List;
import java.util.Optional;

class SearchBox extends TextBox {
	private static final List<Component> TOOLTIP = List.of(
					Component.translatable(TranslationHelper.INSTANCE.translGui("text_box.search_box")),
					Component.translatable(TranslationHelper.INSTANCE.translGui("text_box.search_box_detail")).withStyle(ChatFormatting.GRAY)
	);
	public static final String MAGNIFYING_GLASS = "\uD83D\uDD0D";
	/**
	 * 这里必须是“不透明”的灰：EditBox 画文字用的是 ARGB，0xBBBBBB 的 alpha 是 0，
	 * 之前输入的文字（以及放大镜提示）全都是全透明 —— 值确实在，过滤也确实生效，就是看不见。
	 */
	public static final int UNFOCUSED_COLOR = ARGB.opaque(0xBBBBBB);
	private final StorageScreenBase<?> screen;
	private long lastFocusChangeTime = 0;
	private final int maximizedX;
	private final int maximizedWidth;

	public SearchBox(Position position, Dimension dimension, StorageScreenBase<?> screen) {
		super(position, dimension);
		this.screen = screen;
		setTextColor(UNFOCUSED_COLOR);
		setTextColorUneditable(UNFOCUSED_COLOR);
		setBordered(false);
		setMaxLength(50);
		setUnfocusedEmptyHint(MAGNIFYING_GLASS);
		maximizedX = position.x();
		maximizedWidth = dimension.width();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}

		if (isEditable()) {
			if (button == 0) {
				setFocused(true);
				screen.setFocused(this);
				// 展开动画还没跑完时内嵌 EditBox 只有几像素宽，光标定位会算到框外（玩家会觉得“点了没反应”）。
				// 点击时先摆到最大宽度，再把这下点击真正转给它。
				setPosition(new Position(maximizedX, y));
				updateDimensions(maximizedWidth, getHeight());
				forwardClickToEditBox(event, isDoubleClick);
			} else if (button == 1) {
				setValue("");
			}
			return true;
		}
		return super.mouseClicked(event, isDoubleClick);
	}

	/**
	 * 收起状态下的搜索框只有 10px 宽、还贴在最右端，玩家按“搜索栏那一条”去点会落在它左边而落空，
	 * 于是焦点被清掉、框再也进不去。命中区按整条最大宽度算（横向不越过整理按钮），点击手感与看到的搜索行一致。
	 */
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		int left = Math.min(x, maximizedX);
		int right = Math.max(x + getWidth(), maximizedX + maximizedWidth);
		return mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + getHeight();
	}

	@Override
	public void setFocused(boolean focused) {
		if (isFocused() != focused) {
			lastFocusChangeTime = System.currentTimeMillis();
		}
		super.setFocused(focused);
		if (focused) {
			setTextColor(-1);
		} else {
			setTextColor(UNFOCUSED_COLOR);
		}
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		int minWidth = getHeight();
		if ((isFocused() && maximizedWidth > getWidth()) || (!isFocused() && getValue().isEmpty() && getWidth() > minWidth)) {
			float ratio = Easing.EASE_IN_OUT_CUBIC.ease(Math.min((System.currentTimeMillis() - lastFocusChangeTime) / 200f, 1));
			int currentWidth = isFocused() ? (int) (minWidth + (maximizedWidth - minWidth) * ratio) : (int) (maximizedWidth - (maximizedWidth - minWidth) * ratio);
			this.setPosition(new Position(maximizedX + maximizedWidth - currentWidth, this.y));
			this.updateDimensions(currentWidth, this.getHeight());
		}

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(0, 0);
		guiGraphics.fill(x, y, x + getWidth(), y + getHeight(), 0xFF777777);
		guiGraphics.pose().popMatrix();
	}

	@Override
	public void extractTooltip(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.extractTooltip(screen, guiGraphics, mouseX, mouseY);
		if (!isFocused() && isMouseOver(mouseX, mouseY)) {
			guiGraphics.setTooltipForNextFrame(screen.getFont(), TOOLTIP, Optional.empty(), mouseX, mouseY);
		}
	}
}
