package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.input.MouseButtonEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.MouseButtons;

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
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = MouseButtons.toLegacy(event.button());
		return getChildAt(mouseX, mouseY).map(l -> {
			if (l.mouseClicked(event, doubleClick)) {
				setFocused(l);
				if (button == 0) {
					setDragging(true);
				}
				return true;
			}
			return false;
		}).orElse(false);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = MouseButtons.toLegacy(event.button());
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
	public void renderTooltip(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.renderTooltip(screen, guiGraphics, mouseX, mouseY);
		children.forEach(c -> c.renderTooltip(screen, guiGraphics, mouseX, mouseY));
	}
}
