package net.p3pp3rf1y.sophisticatedcore.client.gui;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ImageButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;

public class StorageSettingsTab extends Tab {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(16, 96), Dimension.SQUARE_16);
	private final StorageScreenBase<?> screen;

	public StorageSettingsTab(Position position, StorageScreenBase<?> screen, String tabTooltip) {
		super(position, Component.translatable(tabTooltip), onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 3), Dimension.SQUARE_16, ICON, onTabIconClicked));
		this.screen = screen;
	}

	@Override
	protected void onTabIconClicked(int button) {
		// 打开设置页是「服务端关闭当前菜单 → 重新开会设置菜单」，客户端会先 setScreen(null) 再 setScreen(设置页)，
		// 26.3 这两步都会把光标 warp 到窗口正中。这里记下点击时的位置，设置页 init() 结束后拨回去。
		CursorRestorer.remember();
		screen.getMenu().openSettings();
	}
}
