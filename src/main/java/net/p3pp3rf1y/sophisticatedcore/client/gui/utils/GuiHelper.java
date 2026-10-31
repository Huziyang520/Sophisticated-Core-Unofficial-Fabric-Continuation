package net.p3pp3rf1y.sophisticatedcore.client.gui.utils;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class GuiHelper {
	public static final Identifier GUI_CONTROLS = SophisticatedCore.getRL("textures/gui/gui_controls.png");
	private static final int GUI_CONTROLS_TEXTURE_WIDTH = 256;
	private static final int GUI_CONTROLS_TEXTURE_HEIGHT = 256;
	public static final TextureBlitData BAR_BACKGROUND_BOTTOM = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 66), Dimension.SQUARE_18);
	public static final TextureBlitData BAR_BACKGROUND_MIDDLE = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 48), Dimension.SQUARE_18);
	public static final TextureBlitData BAR_BACKGROUND_TOP = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 30), Dimension.SQUARE_18);
	public static final Identifier ICONS = SophisticatedCore.getRL("textures/gui/icons.png");
	public static final TextureBlitData CRAFTING_RESULT_SLOT = new TextureBlitData(GUI_CONTROLS, new UV(71, 216), new Dimension(26, 26));
	public static final TextureBlitData DEFAULT_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(47, 0), Dimension.SQUARE_18);
	public static final TextureBlitData DEFAULT_BUTTON_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(29, 0), Dimension.SQUARE_18);
	public static final TextureBlitData SMALL_BUTTON_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 18), Dimension.SQUARE_12);
	public static final TextureBlitData SMALL_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(41, 18), Dimension.SQUARE_12);
	public static final Identifier SLOTS_BACKGROUND = SophisticatedCore.getRL("textures/gui/slots_background.png");

	private static final Map<Integer, TextureBlitData> SLOTS_BACKGROUNDS = new HashMap<>();

	private GuiHelper() {}

	public static void renderItemInGUI(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition) {
		renderItemInGUI(guiGraphics, minecraft, stack, xPosition, yPosition, false);
	}

	public static void renderSlotsBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int slotWidth, int slotHeight) {
		for (int currentY = y, remainingSlotHeight = slotHeight; remainingSlotHeight > 0; currentY += 12 * 18, remainingSlotHeight -= Math.min(slotHeight, 12)) {
			int finalRemainingSlotHeight = remainingSlotHeight;
			int key = getSlotsBackgroundKey(slotWidth, remainingSlotHeight);
			blit(guiGraphics, x, currentY, SLOTS_BACKGROUNDS.computeIfAbsent(key, k ->
					new TextureBlitData(SLOTS_BACKGROUND, Dimension.SQUARE_256, new UV(0, 0), new Dimension(slotWidth * 18, finalRemainingSlotHeight * 18))
			));
		}
	}

	private static int getSlotsBackgroundKey(int slotWidth, int slotHeight) {
		return slotWidth * 31 + slotHeight;
	}

	public static void renderItemInGUI(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition, boolean renderOverlay) {
		renderItemInGUI(guiGraphics, minecraft, stack, xPosition, yPosition, renderOverlay, null);
	}

	public static void renderItemInGUI(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition, boolean renderOverlay,
									   @Nullable String countText) {
		guiGraphics.item(stack, xPosition, yPosition);
		if (renderOverlay) {
			guiGraphics.itemDecorations(minecraft.font, stack, xPosition, yPosition, countText);
		}
	}

	public static void blit(GuiGraphicsExtractor guiGraphics, int x, int y, TextureBlitData texData) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset(), y + texData.getYOffset(), texData.getU(), texData.getV(), texData.getWidth(), texData.getHeight(), texData.getTextureWidth(), texData.getTextureHeight());
	}

	public static void blit(GuiGraphicsExtractor guiGraphics, int x, int y, TextureBlitData texData, int width, int height) {
		int halfWidth = width / 2;
		int secondHalfWidth = width - halfWidth;
		int halfHeight = height / 2;
		int secondHalfHeight = height - halfHeight;

		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset(), y + texData.getYOffset(), texData.getU(), texData.getV(), halfWidth, halfHeight, texData.getTextureWidth(), texData.getTextureHeight());
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset() + halfWidth, y + texData.getYOffset(), (float) texData.getU() + texData.getWidth() - secondHalfWidth, texData.getV(), secondHalfWidth, halfHeight, texData.getTextureWidth(), texData.getTextureHeight());
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset(), y + texData.getYOffset() + halfHeight, texData.getU(), (float) texData.getV() + texData.getHeight() - secondHalfHeight, halfWidth, secondHalfHeight, texData.getTextureWidth(), texData.getTextureHeight());
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset() + halfWidth, y + texData.getYOffset() + halfHeight, (float) texData.getU() + texData.getWidth() - secondHalfWidth, (float) texData.getV() + texData.getHeight() - secondHalfHeight, secondHalfWidth, secondHalfHeight, texData.getTextureWidth(), texData.getTextureHeight());
	}

	public static void coloredBlit(GuiGraphicsExtractor guiGraphics, int x, int y, TextureBlitData texData, int color) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(),
				x + texData.getXOffset(), y + texData.getYOffset(),
				texData.getWidth(), texData.getHeight(),
				texData.getU(), texData.getV(),
				texData.getWidth(), texData.getHeight(),
				texData.getTextureWidth(), texData.getTextureHeight(),
				color);
	}
	public static void renderTooltipBackground(GuiGraphicsExtractor guiGraphics, int tooltipWidth, int leftX, int topY, int tooltipHeight, int backgroundColor, int borderColorStart, int borderColorEnd) {
		fillRect(guiGraphics, leftX - 3, topY - 4, leftX + tooltipWidth + 3, topY - 3, backgroundColor);
		fillRect(guiGraphics, leftX - 3, topY + tooltipHeight + 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 4, backgroundColor);
		fillRect(guiGraphics, leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, backgroundColor);
		fillRect(guiGraphics, leftX - 4, topY - 3, leftX - 3, topY + tooltipHeight + 3, backgroundColor);
		fillRect(guiGraphics, leftX + tooltipWidth + 3, topY - 3, leftX + tooltipWidth + 4, topY + tooltipHeight + 3, backgroundColor);
		fillGradientRect(guiGraphics, leftX - 3, topY - 3 + 1, leftX - 3 + 1, topY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		fillGradientRect(guiGraphics, leftX + tooltipWidth + 2, topY - 3 + 1, leftX + tooltipWidth + 3, topY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		fillRect(guiGraphics, leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY - 3 + 1, borderColorStart);
		fillRect(guiGraphics, leftX - 3, topY + tooltipHeight + 2, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, borderColorEnd);
	}

	private static void fillRect(GuiGraphicsExtractor guiGraphics, float x1, float y1, float x2, float y2, int color) {
		guiGraphics.fill((int) Math.min(x1, x2), (int) Math.min(y1, y2), (int) Math.max(x1, x2), (int) Math.max(y1, y2), color);
	}

	private static void fillGradientRect(GuiGraphicsExtractor guiGraphics, float x1, float y1, float x2, float y2, int colorTop, int colorBottom) {
		guiGraphics.fillGradient((int) Math.min(x1, x2), (int) Math.min(y1, y2), (int) Math.max(x1, x2), (int) Math.max(y1, y2), colorTop, colorBottom);
	}
	public static void writeTooltipLines(GuiGraphicsExtractor guiGraphics, List<FormattedCharSequence> textLines, Font font, float leftX, int topY, int color) {
		for (int i = 0; i < textLines.size(); ++i) {
			FormattedCharSequence line = textLines.get(i);
			if (line != null) {
				guiGraphics.text(font, line, (int) leftX, topY, color, true);
			}

			if (i == 0) {
				topY += 2;
			}

			topY += 10;
		}
	}

	public static void fill(GuiGraphicsExtractor guiGraphics, float minX, float minY, float maxX, float maxY, int color) {
		fillRect(guiGraphics, minX, minY, maxX, maxY, color);
	}
	public static ToggleButton.StateData getButtonStateData(UV uv, Dimension dimension, Position offset, Component... tooltip) {
		return getButtonStateData(uv, dimension, offset, Arrays.asList(tooltip));
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, String tooltip, Dimension dimension) {
		return getButtonStateData(uv, tooltip, dimension, new Position(0, 0));
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, String tooltip, Dimension dimension, Position offset) {
		return new ToggleButton.StateData(new TextureBlitData(ICONS, offset, Dimension.SQUARE_256, uv, dimension),
				Component.translatable(tooltip)
		);
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, Dimension dimension, Position offset, List<Component> tooltip) {
		return new ToggleButton.StateData(new TextureBlitData(ICONS, offset, Dimension.SQUARE_256, uv, dimension), tooltip);
	}

	public static void renderSlotsBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int slotsInRow, int fullSlotRows, int extraRowSlots) {
		renderSlotsBackground(guiGraphics, x, y, slotsInRow, fullSlotRows);
		if (extraRowSlots > 0) {
			renderSlotsBackground(guiGraphics, x, y + fullSlotRows * 18, extraRowSlots, 1);
		}
	}

	/**
	 * Draws a fluid sprite tiled downwards from {@code y} over {@code height} pixels.
	 * 26.3 removed the immediate-mode vertex API, so each 16px band is one sprite blit.
	 */
	public static void renderTiledFluidTextureAtlas(GuiGraphicsExtractor guiGraphics, TextureAtlasSprite sprite, int color, int x, int y, int height) {
		int spriteHeight = sprite.contents().height();
		int startY = y;
		int remaining = height;
		do {
			int renderHeight = Math.min(spriteHeight, remaining);
			remaining -= renderHeight;
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, startY, 16, renderHeight, color);
			startY += renderHeight;
		} while (remaining > 0);
	}
	public static void renderControlBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int renderWidth, int renderHeight) {
		int u = 29;
		int v = 146;
		int textureBgWidth = 66;
		int textureBgHeight = 56;
		renderControlBackground(guiGraphics, x, y, renderWidth, renderHeight, u, v, textureBgWidth, textureBgHeight);
	}

	public static void renderControlBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int renderWidth, int renderHeight, int u, int v, int textureBgWidth, int textureBgHeight) {
		int halfWidth = renderWidth / 2;
		int halfHeight = renderHeight / 2;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiHelper.GUI_CONTROLS, x, y, u, v, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiHelper.GUI_CONTROLS, x, y + halfHeight, u, (float) v + textureBgHeight - halfHeight, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiHelper.GUI_CONTROLS, x + halfWidth, y, (float) u + textureBgWidth - halfWidth, v, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiHelper.GUI_CONTROLS, x + halfWidth, y + halfHeight, (float) u + textureBgWidth - halfWidth, (float) v + textureBgHeight - halfHeight, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
	}

	/**
	 * Draws an item stack rotated in the GUI plane, which the stock {@code item} helper cannot do.
	 */
	public static void tryRenderGuiItem(GuiGraphicsExtractor guiGraphics, ItemStack stack, int x, int y, int rotation) {
		if (stack.isEmpty()) {
			return;
		}
		Matrix3x2fStack pose = guiGraphics.pose();
		pose.pushMatrix();
		pose.translate(x + 8F, y + 8F);
		if (rotation != 0) {
			pose.rotate((float) Math.toRadians(rotation));
		}
		pose.translate(-8F, -8F);
		guiGraphics.item(stack, 0, 0);
		pose.popMatrix();
	}
	public static void renderTooltip(Screen screen, GuiGraphicsExtractor guiGraphics, List<Component> components, int x, int y) {
		List<ClientTooltipComponent> list = gatherTooltipComponents(components, x, screen.width, screen.height, screen.font);
		guiGraphics.tooltip(screen.font, list, x, y, DefaultTooltipPositioner.INSTANCE);
	}

	//copy of ForgeHooksClient.gatherTooltipComponents with splitting always called so that new lines in translation are properly wrapped
	public static List<ClientTooltipComponent> gatherTooltipComponents(List<? extends FormattedText> textElements, int mouseX, int screenWidth, int screenHeight, Font font) {
		List<Either<FormattedText, TooltipComponent>> elements = textElements.stream()
				.map((Function<FormattedText, Either<FormattedText, TooltipComponent>>) Either::left)
				.collect(Collectors.toCollection(ArrayList::new));


		/*var event = new RenderTooltipEvent.GatherComponents(ItemStack.EMPTY, screenWidth, screenHeight, elements, -1);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			return List.of();
		}*/

		// text wrapping
		int tooltipTextWidth = elements.stream()
				.mapToInt(either -> either.map(font::width, component -> 0))
				.max()
				.orElse(0);

		int tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) // if the tooltip doesn't fit on the screen
			{
				if (mouseX > screenWidth / 2) {
					tooltipTextWidth = mouseX - 12 - 8;
				} else {
					tooltipTextWidth = screenWidth - 16 - mouseX;
				}
			}
		}

		/*if (event.getMaxWidth() > 0 && tooltipTextWidth > event.getMaxWidth()) {
			tooltipTextWidth = event.getMaxWidth();
		}*/

		int tooltipTextWidthF = tooltipTextWidth;
		return elements.stream()
				.flatMap(either -> either.map(
						text -> font.split(text, tooltipTextWidthF).stream().map(ClientTooltipComponent::create),
						component -> Stream.of(ClientTooltipComponent.create(component))
				))
				.toList();
	}

	public static Optional<Rect2i> getPositiveRectangle(int x, int y, int width, int height) {
		if (x + width <= 0 || y + height <= 0) {
			return Optional.empty();
		}

		int positiveX = Math.max(0, x);
		int positiveY = Math.max(0, y);
		int positiveWidth = width + Math.min(0, x);
		int positiveHeight = height + Math.min(0, y);

		return Optional.of(new Rect2i(positiveX, positiveY, positiveWidth, positiveHeight));
	}

	public static void renderSlotHighlight(GuiGraphicsExtractor guiGraphics, int x, int y, int blitOffset, int color) {
		guiGraphics.fillGradient(x, y, x + 16, y + 16, color, color);
	}
}
