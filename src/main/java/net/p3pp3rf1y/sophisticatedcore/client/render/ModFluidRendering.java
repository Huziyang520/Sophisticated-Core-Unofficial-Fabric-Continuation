package net.p3pp3rf1y.sophisticatedcore.client.render;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.network.chat.Component;
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
	/**
	 * Display name key for the fluid.
	 * <p>
	 * 26.3 has no {@code FluidType}, so a fluid's name comes from Fabric's
	 * {@code FluidVariantAttributeHandler#getName(FluidVariant)}. Its default implementation falls back to
	 * {@code Util.makeDescriptionId("block", <fluid id>)} - i.e. {@code block.sophisticatedcore.xp_still} - which is
	 * why the tank tooltip showed a raw id. Registering a handler gives the fluid a single, stable key.
	 */
	public static final String EXPERIENCE_NAME_KEY = "fluid.sophisticatedcore.experience";

	private static final Map<Fluid, Identifier> STILL_SPRITES = new IdentityHashMap<>();
	private static final FluidVariantAttributeHandler EXPERIENCE_NAME = new FluidVariantAttributeHandler() {
		@Override
		public Component getName(FluidVariant fluidVariant) {
			return Component.translatable(EXPERIENCE_NAME_KEY);
		}
	};


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
		FluidVariantAttributes.register(still, EXPERIENCE_NAME);
		FluidVariantAttributes.register(flowing, EXPERIENCE_NAME);
		STILL_SPRITES.put(still, stillSprite);
		STILL_SPRITES.put(flowing, stillSprite);
	}

	/** The block atlas sprite a fluid should be drawn with in GUIs, when this mod declares one for it. */
	public static Optional<Identifier> getStillSprite(Fluid fluid) {
		return Optional.ofNullable(STILL_SPRITES.get(fluid));
	}
}
