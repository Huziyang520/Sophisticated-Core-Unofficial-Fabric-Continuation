package net.p3pp3rf1y.sophisticatedcore.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

public class StorageGuiHelper {

	private StorageGuiHelper() {}

	public static void renderStorageBackground(Position position, GuiGraphicsExtractor guiGraphics, Identifier textureName, int xSize, int slotsHeight) {
		int x = position.x();
		int y = position.y();
		int slotsTopBottomHeight = Math.min(slotsHeight / 2, 150);
		int yOffset = 0;

		// 26.3：不带 pipeline 的 blit 不会绑定纹理，画出来是空的 —— 背景板就是这样整块消失的
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureName, x, y, 0, 0, xSize,
				StorageScreenBase.SLOTS_Y_OFFSET + slotsTopBottomHeight, 256, 256);

		if (slotsHeight / 2 > 150) {
			int middleHeight = (slotsHeight / 2 - 150) * 2;
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureName, x, y + StorageScreenBase.SLOTS_Y_OFFSET + slotsTopBottomHeight,
					0, StorageScreenBase.SLOTS_Y_OFFSET, xSize, middleHeight, 256, 256);
			yOffset = middleHeight;
		}

		int playerInventoryHeight = 97;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureName, x, y + yOffset + StorageScreenBase.SLOTS_Y_OFFSET + slotsTopBottomHeight,
				0, 256 - (playerInventoryHeight + slotsTopBottomHeight), xSize, playerInventoryHeight + slotsTopBottomHeight, 256, 256);
	}
}
