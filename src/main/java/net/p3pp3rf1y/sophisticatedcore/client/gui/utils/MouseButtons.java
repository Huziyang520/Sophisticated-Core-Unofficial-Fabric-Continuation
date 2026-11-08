package net.p3pp3rf1y.sophisticatedcore.client.gui.utils;

/**
 * Mouse button codes changed with the SDL input rewrite in 26.3.
 * <p>
 * Vanilla converts them at the container-click boundary; widget handlers still expect the old
 * GLFW values, so every entry point that reads a raw button code must normalize it here.
 */
public final class MouseButtons {
	public static final int LEFT = 1;
	public static final int RIGHT = 3;
	public static final int LEGACY_LEFT = 0;
	public static final int LEGACY_RIGHT = 1;

	private MouseButtons() {
	}

	public static int toLegacy(int button) {
		return switch (button) {
			case LEFT -> LEGACY_LEFT;
			case RIGHT -> LEGACY_RIGHT;
			default -> button;
		};
	}
}
