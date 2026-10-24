package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CompositeWidgetBase<T extends WidgetBase> extends WidgetBase implements ContainerEventHandler {
	protected final List<T> children = new ArrayList<>();

	private boolean dragging = false;

	@Nullable
	private GuiEventListener listener;

	protected CompositeWidgetBase(Position position, Dimension dimension) {
		super(position, dimension);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		children.forEach(child -> child.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks));
	}

	protected <U extends T> U addChild(U child) {
		children.add(child);
		return child;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return children;
	}

	@Override
	public boolean isDragging() {
		for (T child : children) {
			if (child instanceof ContainerEventHandler containerEventHandler && containerEventHandler.isDragging()) {
				return true;
			}
		}
		return dragging;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		// 26.3 的 getChildAt 只返回“第一个命中”的子控件：命中的若是纯装饰（页签标签 Label、面板底、被展开页签盖住的
		// 收起页签…）并且它返回 false，这次点击就整颗丢掉，后面真正可点的按钮永远收不到 —— 设置界面里就是
		// “设置项点了没反应”。这里按层级（后加的在上面）从后往前逐个询问，第一个处理掉的为准。
		for (int i = children.size() - 1; i >= 0; i--) {
			T child = children.get(i);
			if (!child.isMouseOver(mouseX, mouseY)) {
				continue;
			}
			if (child.mouseClicked(event, false)) {
				setFocused(child);
				if (button == InputConstants.MOUSE_BUTTON_LEFT) {
					setDragging(true);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		return getChildAt(mouseX, mouseY).map(l -> l.mouseDragged(event, dragX, dragY)).orElse(false);
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Nullable
	@Override
	public GuiEventListener getFocused() {
		return listener;
	}

	@Override
	public void setFocused(@Nullable GuiEventListener listener) {
		this.listener = listener;
	}

	@Override
	public void extractTooltip(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.extractTooltip(screen, guiGraphics, mouseX, mouseY);
		children.forEach(c -> c.extractTooltip(screen, guiGraphics, mouseX, mouseY));
	}
}
