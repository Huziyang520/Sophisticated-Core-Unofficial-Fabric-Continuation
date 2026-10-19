package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class TextBox extends WidgetBase {
	private final EditBox editBox;
	@Nullable
	private String unfocusedEmptyHint = null;

	public TextBox(Position position, Dimension dimension) {
		super(position, dimension);
		editBox = new EditBox(minecraft.font, position.x(), position.y(), dimension.width(), dimension.height(), Component.empty());
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		//noop
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(0, 0);
		editBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		if (editBox.getValue().isEmpty() && unfocusedEmptyHint != null && !editBox.isFocused()) {
			int x = editBox.getX() + editBox.getWidth() / 2 + 2/* editBox.isBordered() ? editBox.getX() + 4 : editBox.getX()*/;
			int y = editBox.isBordered() ? editBox.getY() + (editBox.getHeight() - 8) / 2 : editBox.getY();
			// textColor 由 SearchBox 设成 0xBBBBBB（不带 alpha），直接拿来画提示就是全透明 —— 放大镜图标会“消失”。
			// 这里统一补满 alpha，颜色语义交给调用方，别把 alpha 和 RGB 混用。
			guiGraphics.text(this.font, unfocusedEmptyHint, x - this.font.width(unfocusedEmptyHint) / 2, y, ARGB.opaque(editBox.textColor));
		}
		poseStack.popMatrix();
	}

	/**
	 * 把一次左键点击转给内部的 EditBox：EditBox 不是屏幕的直系子控件，收不到父类派发，
	 * 不转这一下就只能“第一次点进去有焦点”，之后点文本里任何位置都不会移动光标/选词，
	 * 玩家会认为搜索框“再点就没反应了”。
	 */
	public void forwardClickToEditBox(MouseButtonEvent event, boolean isDoubleClick) {
		editBox.onClick(event, isDoubleClick);
	}

	@Override
	public void setFocused(boolean focused) {
		if (editBox.isFocused() != focused) {
			editBox.setFocused(focused);
		}
		super.setFocused(focused);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();
		int scanCode = event.keycode();
		int modifiers = event.modifiers();
		if (!editBox.isFocused()) {
			return false;
		}
		editBox.keyPressed(event);
		if (keyCode == InputConstants.KEY_RETURN) {
			onEnterPressed();
		}
		return keyCode != InputConstants.KEY_ESCAPE;
	}

	protected void onEnterPressed() {
		//noop
	}

	public String getValue() {
		return editBox.getValue();
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return editBox.charTyped(event);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		editBox.updateNarration(narrationElementOutput);
	}

	public void setValue(String value) {
		editBox.setValue(value);
	}

	public void setValueWithoutNotification(String value) {
		Consumer<String> temp = editBox.responder;
		editBox.setResponder(null);
		editBox.setValue(value);
		editBox.setResponder(temp);
	}

	public void setTextColor(int color) {
		editBox.setTextColor(color);
	}

	public void setTextColorUneditable(int color) {
		editBox.setTextColorUneditable(color);
	}

	public void setBordered(boolean bordered) {
		editBox.setBordered(bordered);

		if (!bordered) {
			editBox.setX(x + 1);
			editBox.setY(y + 1);
			editBox.setWidth(getWidth() - 6);
		} else {
			editBox.setX(x);
			editBox.setY(y);
			editBox.setWidth(getWidth());
		}
	}

	public void setMaxLength(int maxLength) {
		editBox.setMaxLength(maxLength);
	}

	public void setResponder(Consumer<String> responder) {
		editBox.setResponder(responder);
	}

	public void setEditable(boolean editable) {
		editBox.setEditable(editable);
	}

	public boolean isEditable() {
		return editBox.isEditable();
	}

	public void setUnfocusedEmptyHint(String hint) {
		this.unfocusedEmptyHint = hint;
	}

	@Override
	public void setPosition(Position position) {
		super.setPosition(position);
		editBox.setX(position.x());
		editBox.setY(position.y());
		setBordered(editBox.isBordered());
	}

	@Override
	protected void updateDimensions(int width, int height) {
		super.updateDimensions(width, height);
		editBox.setWidth(width);
		setBordered(editBox.isBordered());
	}
}