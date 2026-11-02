package net.p3pp3rf1y.sophisticatedcore.client.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Item model that picks which model to draw from the stack itself.
 * <p>
 * 26.3 renders items through {@link ItemModel}, which is selected per item id, so a mod that wants
 * a single item to look different per stack (dye color, installed upgrades, filled tanks) needs a
 * model that decides at update time. {@code selector} receives the stack and returns the model to
 * delegate to; returning {@code null} draws nothing.
 * <p>
 * The returned model may be {@code null} and the selector runs on the render thread every frame, so
 * it should stay cheap - prefer reading cached render state over scanning inventories.
 */
public class SelectingItemModel implements ItemModel {
	private final Function<ItemStack, ItemModel> selector;
	@Nullable
	private final ItemModel fallback;

	public SelectingItemModel(Function<ItemStack, ItemModel> selector, @Nullable ItemModel fallback) {
		this.selector = selector;
		this.fallback = fallback;
	}

	@Override
	public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
			ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		ItemModel selected = selector.apply(stack);
		if (selected == null) {
			selected = fallback;
		}
		if (selected != null) {
			selected.update(renderState, stack, resolver, displayContext, level, owner, seed);
		}
	}
}
