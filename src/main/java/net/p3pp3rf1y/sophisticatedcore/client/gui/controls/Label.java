package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import org.jetbrains.annotations.Nullable;
import java.util.function.IntSupplier;

public class Label extends WidgetBase {
	private static final int DEFAULT_GUI_TEXT_COLOR = 4210752;
	private final Component labelText;
	private final int color;
	@Nullable
	private IntSupplier maxTextWidth = null;

	public Label(Position position, Component labelText) {
		this(position, labelText, DEFAULT_GUI_TEXT_COLOR);
	}

	public Label(Position position, Component labelText, int color) {
		super(position, new Dimension(Minecraft.getInstance().font.width(labelText), 8));
		this.labelText = labelText;
		this.color = color;
	}

	/**
	 * Constrains how much of the text may be drawn. Tab titles used to overflow the tab body and cover the slots next
	 * to them; an open settings tab is wider than the closed one, so the limit has to be applied while drawing.
	 */
	public void setMaxTextWidth(IntSupplier maxTextWidth) {
		this.maxTextWidth = maxTextWidth;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		//noop
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (maxTextWidth == null) {
			guiGraphics.text(minecraft.font, labelText, x, y, color, false);
			return;
		}
		int limit = maxTextWidth.getAsInt();
		String text = labelText.getString();
		if (limit <= 0) {
			return;
		}
		if (minecraft.font.width(text) <= limit) {
			guiGraphics.text(minecraft.font, labelText, x, y, color, false);
		} else {
			guiGraphics.text(minecraft.font, minecraft.font.plainSubstrByWidth(text, Math.max(0, limit - 6)) + "...", x, y, color, false);
		}
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		//TODO add narration
	}
}
