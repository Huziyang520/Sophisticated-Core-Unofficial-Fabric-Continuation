package net.p3pp3rf1y.sophisticatedcore.client.render;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.init.ModFluids;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client side rendering setup for the experience fluid.
 * <p>
 * 26.3 dropped the {@code FluidType} sprite holders and {@code FluidRenderHandler} no longer exposes sprites: fluids are
 * drawn from a {@link FluidModel} that vanilla only defines for water and lava. Mod fluids therefore have to declare
 * their still/flowing materials through {@link FluidRenderingRegistry}, and the resulting texture ids are also exposed
 * here so that GUI tank rendering can look up the same sprite.
 */
public final class ModFluidRendering {
	private static final Map<Fluid, Identifier> STILL_SPRITES = new IdentityHashMap<>();

	private ModFluidRendering() {
	}

	/** Called from the client initialiser, after the fluids themselves have been registered. */
	public static void register() {
		registerFluid(ModFluids.XP_STILL.get(), ModFluids.XP_FLOWING.get(), "block/xp_still", "block/xp_flowing");
	}

	private static void registerFluid(Fluid still, Fluid flowing, String stillTexture, String flowingTexture) {
		Identifier stillSprite = Identifier.fromNamespaceAndPath(SophisticatedCore.MOD_ID, stillTexture);
		Identifier flowingSprite = Identifier.fromNamespaceAndPath(SophisticatedCore.MOD_ID, flowingTexture);
		//no overlay material, no tint source: the supplied textures are already coloured, matching the pre 26.3 setup
		FluidRenderingRegistry.register(still, flowing, new FluidModel.Unbaked(new Material(stillSprite), new Material(flowingSprite), null, null));
		STILL_SPRITES.put(still, stillSprite);
		STILL_SPRITES.put(flowing, stillSprite);
	}

	/** The block atlas sprite a fluid should be drawn with in GUIs, when this mod declares one for it. */
	public static Optional<Identifier> getStillSprite(Fluid fluid) {
		return Optional.ofNullable(STILL_SPRITES.get(fluid));
	}
}
