package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

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
		return getChildAt(mouseX, mouseY).map(l -> {
			if (l.mouseClicked(event, false)) {
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
	public void renderTooltip(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.setTooltipForNextFrame(screen, guiGraphics, mouseX, mouseY);
		children.forEach(c -> c.setTooltipForNextFrame(screen, guiGraphics, mouseX, mouseY));
	}
}
