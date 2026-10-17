package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.function.IntConsumer;

public class ItemButton extends ButtonBase {
	private final ItemStack stack;
	private final Component narration;

	public ItemButton(Position position, IntConsumer onClick, ItemStack stack, Component narration) {
		super(position, Dimension.SQUARE_16, onClick);
		this.stack = stack;
		this.narration = narration;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		//noop
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Matrix3x2fStack pose = guiGraphics.pose();
		pose.pushMatrix();
		pose.translate(0, 0);
		guiGraphics.item(stack, x, y);
		pose.popMatrix();
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, narration);
		narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
	}
}
