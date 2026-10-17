package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

/**
 * Carries the registry key of the entry that is currently being constructed.
 * <p>
 * 26.3 requires {@code Item.Properties} and {@code BlockBehaviour.Properties} to know their registry id before the
 * item or block is constructed ({@code Properties.setId}): the description id and the default model are derived from
 * it, and both are resolved inside the constructor. Vanilla passes the id explicitly because it builds the properties
 * itself; this mod builds them at the call site instead, so the id is published on a thread local by
 * {@link DeferredRegister} around the factory call and picked up by the item and block base classes.
 */
public final class RegistryIdContext {
	private static final ThreadLocal<ResourceKey<?>> CURRENT = new ThreadLocal<>();

	private RegistryIdContext() {
	}

	/** Publishes the key of the entry about to be constructed. */
	public static void push(ResourceKey<?> key) {
		CURRENT.set(key);
	}

	/** Clears the published key; always call this in a finally block. */
	public static void clear() {
		CURRENT.remove();
	}

	/** Applies the published item id to {@code properties}, if an item is being constructed. */
	public static Item.Properties applyToItem(Item.Properties properties) {
		ResourceKey<?> key = CURRENT.get();
		if (key != null && key.isFor(Registries.ITEM)) {
			return properties.setId(itemKey(key));
		}

		return properties;
	}

	/** Applies the published block id to {@code properties}, if a block is being constructed. */
	public static BlockBehaviour.Properties applyToBlock(BlockBehaviour.Properties properties) {
		ResourceKey<?> key = CURRENT.get();
		if (key != null && key.isFor(Registries.BLOCK)) {
			return properties.setId(blockKey(key));
		}

		return properties;
	}

	/** The key currently being constructed, or {@code null} outside of a registration call. */
	@Nullable
	public static ResourceKey<?> current() {
		return CURRENT.get();
	}

	@SuppressWarnings("unchecked")
	private static ResourceKey<Item> itemKey(ResourceKey<?> key) {
		return (ResourceKey<Item>) key;
	}

	@SuppressWarnings("unchecked")
	private static ResourceKey<Block> blockKey(ResourceKey<?> key) {
		return (ResourceKey<Block>) key;
	}
}
