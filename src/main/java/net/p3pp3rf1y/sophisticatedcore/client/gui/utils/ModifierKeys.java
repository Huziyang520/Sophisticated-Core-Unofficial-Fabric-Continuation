package net.p3pp3rf1y.sophisticatedcore.client.gui.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

/**
 * Reads the current modifier key state.
 * <p>
 * 26.3 exposes the modifiers on the input event and no longer offers the static helpers on
 * {@code Screen}, so the keyboard state is queried directly.
 */
public final class ModifierKeys {
	private ModifierKeys() {
	}

	public static boolean isShiftDown() {
		return isDown(InputConstants.KEY_LSHIFT) || isDown(InputConstants.KEY_RSHIFT);
	}

	public static boolean isControlDown() {
		return isDown(InputConstants.KEY_LCONTROL) || isDown(InputConstants.KEY_RCONTROL);
	}

	public static boolean isAltDown() {
		return isDown(InputConstants.KEY_LALT) || isDown(InputConstants.KEY_RALT);
	}

	private static boolean isDown(int key) {
		return InputConstants.isKeyDown(key);
	}
}
