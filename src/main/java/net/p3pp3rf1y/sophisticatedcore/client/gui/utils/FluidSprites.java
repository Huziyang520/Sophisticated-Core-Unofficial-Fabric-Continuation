package net.p3pp3rf1y.sophisticatedcore.client.gui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
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
		if (model != null && model.stillMaterial() != null && model.stillMaterial().sprite() != null) {
			return model.stillMaterial().sprite();
		}

		Identifier fallback;
		if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
			fallback = Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
		} else if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
			fallback = Identifier.fromNamespaceAndPath("minecraft", "block/lava_still");
		} else {
			fallback = SophisticatedCore.getRL("block/xp_still");
		}
		return minecraft.getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(fallback);
	}
}
