package net.p3pp3rf1y.sophisticatedcore.client.gui.utils;

import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.material.Fluid;
import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;

import org.jetbrains.annotations.Nullable;
import java.util.*;
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
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(),
				x + texData.getXOffset(), y + texData.getYOffset(),
				texData.getU(), texData.getV(),
				texData.getWidth(), texData.getHeight(),
				texData.getTextureWidth(), texData.getTextureHeight());
	}

	public static void blit(GuiGraphicsExtractor guiGraphics, int x, int y, TextureBlitData texData, int width, int height) {
		int halfWidth = width / 2;
		int secondHalfWidth = width - halfWidth;
		int halfHeight = height / 2;
		int secondHalfHeight = height - halfHeight;

		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset(), y + texData.getYOffset(), texData.getU(), texData.getV(), halfWidth, halfHeight, texData.getTextureWidth(), texData.getTextureHeight());
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset() + halfWidth, y + texData.getYOffset(), texData.getU() + texData.getWidth() - secondHalfWidth, texData.getV(), secondHalfWidth, halfHeight, texData.getTextureWidth(), texData.getTextureHeight());
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset(), y + texData.getYOffset() + halfHeight, texData.getU(), texData.getV() + texData.getHeight() - secondHalfHeight, halfWidth, secondHalfHeight, texData.getTextureWidth(), texData.getTextureHeight());
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(), x + texData.getXOffset() + halfWidth, y + texData.getYOffset() + halfHeight, texData.getU() + texData.getWidth() - secondHalfWidth, texData.getV() + texData.getHeight() - secondHalfHeight, secondHalfWidth, secondHalfHeight, texData.getTextureWidth(), texData.getTextureHeight());
	}

	/** Draws the sprite with an ARGB tint; 26.3 takes the tint as the last argument of the textured blit. */
	public static void coloredBlit(GuiGraphicsExtractor guiGraphics, int x, int y, TextureBlitData texData, int color) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texData.getTextureName(),
				x + texData.getXOffset(), y + texData.getYOffset(),
				texData.getU(), texData.getV(),
				texData.getWidth(), texData.getHeight(),
				texData.getTextureWidth(), texData.getTextureHeight(), color);
	}

	/** Draws the vanilla tooltip frame behind custom tooltip contents. */
	public static void renderTooltipBackground(GuiGraphicsExtractor guiGraphics, int tooltipWidth, int leftX, int topY, int tooltipHeight,
											   int backgroundColor, int borderColorStart, int borderColorEnd) {
		guiGraphics.fill(leftX - 3, topY - 4, leftX + tooltipWidth + 3, topY - 3, backgroundColor);
		guiGraphics.fill(leftX - 3, topY + tooltipHeight + 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 4, backgroundColor);
		guiGraphics.fill(leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, backgroundColor);
		guiGraphics.fill(leftX - 4, topY - 3, leftX - 3, topY + tooltipHeight + 3, backgroundColor);
		guiGraphics.fill(leftX + tooltipWidth + 3, topY - 3, leftX + tooltipWidth + 4, topY + tooltipHeight + 3, backgroundColor);
		guiGraphics.fillGradient(leftX - 3, topY - 3 + 1, leftX - 3 + 1, topY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		guiGraphics.fillGradient(leftX + tooltipWidth + 2, topY - 3 + 1, leftX + tooltipWidth + 3, topY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		guiGraphics.fill(leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY - 3 + 1, borderColorStart);
		guiGraphics.fill(leftX - 3, topY + tooltipHeight + 2, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, borderColorEnd);
	}

	public static void writeTooltipLines(GuiGraphicsExtractor guiGraphics, List<FormattedCharSequence> textLines, Font font, float leftX, int topY, int color) {
		int y = topY;
		for (int i = 0; i < textLines.size(); ++i) {
			FormattedCharSequence line = textLines.get(i);
			if (line != null) {
				guiGraphics.text(font, line, (int) leftX, y, color, true);
			}

			if (i == 0) {
				y += 2;
			}

			y += 10;
		}
	}

	public static void fill(GuiGraphicsExtractor guiGraphics, float minX, float minY, float maxX, float maxY, int color) {
		guiGraphics.fill(Math.round(minX), Math.round(minY), Math.round(maxX), Math.round(maxY), color);
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
	 * Tiles a fluid sprite down a 16 pixel wide column. The extractor takes texture pixel coordinates, so the atlas size
	 * is derived from the sprite's normalised bounds instead of being queried from the atlas.
	 */
	public static void renderTiledFluidTextureAtlas(GuiGraphicsExtractor guiGraphics, TextureAtlasSprite sprite, int color, int x, int y, int height) {
		float uSpan = sprite.getU1() - sprite.getU0();
		float vSpan = sprite.getV1() - sprite.getV0();
		int spriteWidth = sprite.contents().width();
		int spriteHeight = sprite.contents().height();
		if (uSpan <= 0 || vSpan <= 0 || spriteHeight <= 0) {
			return;
		}

		int atlasWidth = Math.round(spriteWidth / uSpan);
		int atlasHeight = Math.round(spriteHeight / vSpan);
		float u = sprite.getU0() * atlasWidth;
		float v = sprite.getV0() * atlasHeight;

		int remainingHeight = height;
		int startY = y;
		do {
			int renderHeight = Math.min(spriteHeight, remainingHeight);
			remainingHeight -= renderHeight;
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprite.atlasLocation(), x, startY, u, v, 16, renderHeight, atlasWidth, atlasHeight, color);
			startY += renderHeight;
		} while (remainingHeight > 0);
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
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, x, y, u, v, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, x, y + halfHeight, u, v + textureBgHeight - halfHeight, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, x + halfWidth, y, u + textureBgWidth - halfWidth, v, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_CONTROLS, x + halfWidth, y + halfHeight, u + textureBgWidth - halfWidth, v + textureBgHeight - halfHeight, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
	}

	/**
	 * Draws an item inside a GUI slot.
	 * <p>
	 * 26.3 draws GUI items through the extractor's item atlas and the GUI pose stack is 2D only, so the 3D rotation the
	 * old {@code ItemRenderer} path supported cannot be expressed any more. {@code rotation} stays in the signature so
	 * the call sites keep working, but it is ignored.
	 */
	public static void tryRenderGuiItem(GuiGraphicsExtractor guiGraphics, @Nullable LivingEntity livingEntity, ItemStack stack, int x, int y, int rotation) {
		if (stack.isEmpty()) {
			return;
		}

		if (livingEntity != null) {
			guiGraphics.item(livingEntity, stack, x, y, y);
		} else {
			guiGraphics.item(stack, x, y);
		}
	}

	public static void setTooltip(Screen screen, GuiGraphicsExtractor guiGraphics, List<Component> components, int x, int y) {
		List<ClientTooltipComponent> list = gatherTooltipComponents(components, x, screen.width, screen.height, screen.getFont());
		guiGraphics.tooltip(screen.getFont(), list, x, y, DefaultTooltipPositioner.INSTANCE, null, false);
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

	/** 26.3 dropped {@code Screen.hasShiftDown} / {@code hasControlDown}; keyboard state is read directly. */
	public static boolean isShiftDown() {
		return InputConstants.isKeyDown(InputConstants.getKey("key.keyboard.left.shift").getValue()) || InputConstants.isKeyDown(InputConstants.getKey("key.keyboard.right.shift").getValue());
	}

	public static boolean isControlDown() {
		return InputConstants.isKeyDown(InputConstants.getKey("key.keyboard.left.control").getValue()) || InputConstants.isKeyDown(InputConstants.getKey("key.keyboard.right.control").getValue());
	}

	/**
	 * Returns the still sprite of a fluid. 26.3 no longer exposes fluid sprites through {@code FluidRenderHandler}, so
	 * they are looked up on the block atlas using the vanilla {@code block/<name>_still} convention.
	 */
	public static TextureAtlasSprite getStillFluidSprite(Fluid fluid) {
		Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
		Identifier texture = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), "block/" + fluidId.getPath() + "_still");
		return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, texture));
	}

	/** 26.3 取贴图精灵统一走 {@code AtlasManager}，方块图集里包含了物品与 GUI 图标。 */
	public static TextureAtlasSprite getSprite(Identifier spriteName) {
		return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, spriteName));
	}

	/** 26.3 的物品 tooltip 由 extractor 直接接管，这里保留原来的调用形状。 */
	public static void renderTooltip(GuiGraphicsExtractor guiGraphics, ItemStack stack, int mouseX, int mouseY) {
		guiGraphics.setTooltipForNextFrame(Minecraft.getInstance().font, stack, mouseX, mouseY);
	}
}
