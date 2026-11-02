package net.p3pp3rf1y.sophisticatedcore.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import javax.annotation.Nullable;

/**
 * Per-render-state data store for 26.3.
 * <p>
 * 26.3 splits entity rendering into an extract pass that fills an {@link EntityRenderState} and a
 * submit pass that draws from it. Submit only sees the state, never the live entity, so feature
 * layers that need entity data must stash it during extract. Fabric implements
 * {@link FabricRenderState} on every render state for exactly this.
 */
public final class RenderStateData {
	private RenderStateData() {
	}

	/**
	 * Creates a key with a stable debug name.
	 */
	public static <T> RenderStateDataKey<T> createKey(String name) {
		return RenderStateDataKey.create(() -> name);
	}

	/**
	 * Reads previously attached data from a render state.
	 *
	 * @return the attached value, or {@code null} when nothing was attached
	 */
	@Nullable
	public static <T> T get(EntityRenderState state, RenderStateDataKey<T> key) {
		return asFabricState(state).getData(key);
	}

	/**
	 * Reads attached data, falling back to {@code fallback} when absent.
	 */
	public static <T> T getOrDefault(EntityRenderState state, RenderStateDataKey<T> key, T fallback) {
		return asFabricState(state).getDataOrDefault(key, fallback);
	}

	/**
	 * Attaches data to a render state during the extract pass.
	 */
	public static <T> void set(EntityRenderState state, RenderStateDataKey<T> key, @Nullable T value) {
		asFabricState(state).setData(key, value);
	}

	private static FabricRenderState asFabricState(EntityRenderState state) {
		if (state instanceof FabricRenderState fabricState) {
			return fabricState;
		}
		throw new IllegalStateException("Render state does not implement FabricRenderState; Fabric API rendering module is required");
	}
}
