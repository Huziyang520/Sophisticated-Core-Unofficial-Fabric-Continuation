package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.function.IntConsumer;

public abstract class ButtonBase extends WidgetBase {
	protected IntConsumer onClick;

	protected ButtonBase(Position position, Dimension dimension, IntConsumer onClick) {
		super(position, dimension);
		this.onClick = onClick;
	}

	protected void setOnClick(IntConsumer onClick) {
		this.onClick = onClick;
	}

	/**
	 * Left and right clicks are both meaningful (several toggles walk their state backwards on right click). Middle
	 * click and the side buttons are reported as unhandled instead of firing the callback with a sound and no effect.
	 */
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (button != InputConstants.MOUSE_BUTTON_LEFT && button != InputConstants.MOUSE_BUTTON_RIGHT) {
			return false;
		}
		onClick.accept(button);
		if (Boolean.TRUE.equals(Config.CLIENT.playButtonSound.get())) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
		return true;
	}
}
