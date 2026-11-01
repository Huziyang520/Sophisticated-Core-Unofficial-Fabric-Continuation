package net.p3pp3rf1y.sophisticatedcore.util;

import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.inventory.IInventoryHandlerHelper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryStorageWrapper;

/**
 * Entity lookups that expose the mod's item handlers to other mods.
 */
public class Capabilities {
	private Capabilities() {
	}

	public static final class Entity {
		private Entity() {
		}

		public static final EntityApiLookup<IInventoryHandlerHelper, Void> ENTITY = EntityApiLookup.get(SophisticatedCore.getRL("entity_item_storage"), IInventoryHandlerHelper.class, Void.class);

		/**
		 * Capability for an inventory of entity that should be accessible to automation,
		 * in the sense that droppers, hoppers, and similar modded devices will try to use it.
		 */
		public static final EntityApiLookup<IInventoryHandlerHelper, Direction> ENTITY_AUTOMATION = EntityApiLookup.get(SophisticatedCore.getRL("entity_automation_item_storage"), IInventoryHandlerHelper.class, Direction.class);

		static {
			// The container vehicles expose a vanilla Container, so the wrapper is built from that.
			registerContainerEntity(EntityTypes.OAK_CHEST_BOAT);
			registerContainerEntity(EntityTypes.CHEST_MINECART);
			registerContainerEntity(EntityTypes.HOPPER_MINECART);

			ENTITY.registerForType((player, ctx) -> InventoryStorageWrapper.of(player), EntityTypes.PLAYER);
		}

		private static <T extends net.minecraft.world.entity.Entity & Container> void registerContainerEntity(EntityType<T> entityType) {
			ENTITY.registerForType((entity, ctx) -> InventoryStorageWrapper.of(entity), entityType);
			ENTITY_AUTOMATION.registerForType((entity, direction) -> InventoryStorageWrapper.of(entity), entityType);
		}
	}
}
