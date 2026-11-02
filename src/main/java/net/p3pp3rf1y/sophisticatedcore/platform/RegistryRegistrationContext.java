package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Supplies the id of the entry currently being registered.
 * <p>
 * 26.3 requires every item and block to know its own id at construction time, but constructors
 * build their own properties internally. Rather than thread a key through every constructor, the
 * registering code publishes the key while the entry is created and the constructor reads it here.
 */
public final class RegistryRegistrationContext {
	private static final ThreadLocal<ResourceKey<Item>> CURRENT_ITEM = new ThreadLocal<>();
	private static final ThreadLocal<ResourceKey<Block>> CURRENT_BLOCK = new ThreadLocal<>();

	private RegistryRegistrationContext() {
	}

	/**
	 * Item properties pre-filled with the id of the item being constructed.
	 */
	public static Item.Properties itemProperties() {
		return new Item.Properties().setId(require(CURRENT_ITEM, "item"));
	}

	/**
	 * Block properties pre-filled with the id of the block being constructed.
	 */
	public static BlockBehaviour.Properties blockProperties() {
		return BlockBehaviour.Properties.of().setId(require(CURRENT_BLOCK, "block"));
	}

	/**
	 * Applies the id of the block being constructed to existing properties.
	 */
	public static BlockBehaviour.Properties blockPropertiesOf(BlockBehaviour.Properties base) {
		return base.setId(require(CURRENT_BLOCK, "block"));
	}

	private static <T> ResourceKey<T> require(ThreadLocal<ResourceKey<T>> holder, String kind) {
		ResourceKey<T> key = holder.get();
		if (key == null) {
			throw new IllegalStateException("Block or item constructed outside of a registration; no " + kind + " id available");
		}
		return key;
	}

	static void withItemId(Identifier id, Runnable action) {
		run(CURRENT_ITEM, ResourceKey.create(Registries.ITEM, id), action);
	}

	static void withBlockId(Identifier id, Runnable action) {
		run(CURRENT_BLOCK, ResourceKey.create(Registries.BLOCK, id), action);
	}

	private static <T> void run(ThreadLocal<ResourceKey<T>> holder, ResourceKey<T> key, Runnable action) {
		ResourceKey<T> previous = holder.get();
		holder.set(key);
		try {
			action.run();
		} finally {
			if (previous == null) {
				holder.remove();
			} else {
				holder.set(previous);
			}
		}
	}
}