package net.p3pp3rf1y.sophisticatedcore.client.gui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up the still texture of a fluid for GUI drawing.
 * <p>
 * 26.3 dropped {@code FluidRenderHandler.getFluidSprites}; sprites now come from the baked
 * {@link FluidModel} the model manager keeps per fluid.
 */
public final class FluidSprites {
	private FluidSprites() {
	}

	@Nullable
	public static TextureAtlasSprite getStillSprite(Fluid fluid) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.getModelManager() == null) {
			return null;
		}
		FluidModel model = minecraft.getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
		return model == null ? null : model.stillMaterial().sprite();
	}
}
