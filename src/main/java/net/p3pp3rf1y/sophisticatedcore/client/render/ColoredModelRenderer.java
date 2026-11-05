package net.p3pp3rf1y.sophisticatedcore.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Colored model-part drawing for 26.3.
 * <p>
 * 26.3 removed {@code MultiBufferSource} from entity layer rendering and moved everything to
 * {@link SubmitNodeCollector}. A collector cannot hand out a vertex consumer directly, so colored
 * model parts go through {@code submitCustomGeometry}. That callback receives a pose rather than a
 * stack, which is why the caller's stack is captured here and reused inside the callback.
 */
public final class ColoredModelRenderer {
	private ColoredModelRenderer() {
	}

	/**
	 * Submits a model part tinted with {@code color}.
	 * <p>
	 * The color is a packed value with the channels in the high bytes, e.g. {@code 0xFFFF0000} for a
	 * solid red tint. Use {@link #getColor(int, int, int)} to build one from separate channels.
	 */
	public static void submitColoredModelPart(SubmitNodeCollector collector, ModelPart part, PoseStack poseStack,
			RenderType renderType, int packedLight, int color) {
		submit(collector, part, poseStack, renderType, packedLight, OverlayTexture.NO_OVERLAY, color);
	}

	/**
	 * Submits a model part with no tint applied.
	 */
	public static void submitModelPart(SubmitNodeCollector collector, ModelPart part, PoseStack poseStack,
			RenderType renderType, int packedLight) {
		submit(collector, part, poseStack, renderType, packedLight, OverlayTexture.NO_OVERLAY, -1);
	}

	/**
	 * Submits a model part with an explicit overlay value, for effects that need it.
	 */
	public static void submitColoredModelPart(SubmitNodeCollector collector, ModelPart part, PoseStack poseStack,
			RenderType renderType, int packedLight, int overlay, int color) {
		submit(collector, part, poseStack, renderType, packedLight, overlay, color);
	}

	private static void submit(SubmitNodeCollector collector, ModelPart part, PoseStack poseStack,
			RenderType renderType, int packedLight, int overlay, int color) {
		collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
			// submitCustomGeometry defers the callback and snapshots the pose, so the caller's
			// stack is no longer in scope here; rebuild one from the snapshotted pose.
			PoseStack deferredStack = new PoseStack();
			deferredStack.last().pose().set(pose.pose());
			deferredStack.last().normal().set(pose.normal());
			if (color == -1) {
				part.render(deferredStack, consumer, packedLight, overlay);
			} else {
				part.render(deferredStack, consumer, packedLight, overlay, color);
			}
		});
	}

	/**
	 * Submits geometry built directly from a vertex consumer callback.
	 */
	public static void submitGeometry(SubmitNodeCollector collector, PoseStack poseStack, RenderType renderType,
			SubmitNodeCollector.CustomGeometryRenderer renderer) {
		collector.submitCustomGeometry(poseStack, renderType, renderer);
	}

	/**
	 * Packs separate color channels into the format model parts expect.
	 */
	public static int getColor(int red, int green, int blue) {
		return 0xFF_000000 | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
	}

	/**
	 * Splits a packed color into its red, green and blue channels.
	 */
	public static int[] getColorComponents(int color) {
		return new int[]{(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF};
	}

	private static int clamp(int channel) {
		return Math.max(0, Math.min(255, channel));
	}
}
