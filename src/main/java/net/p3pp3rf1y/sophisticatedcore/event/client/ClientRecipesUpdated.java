package net.p3pp3rf1y.sophisticatedcore.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired when the client receives an updated recipe set.
 * <p>
 * 26.3 dropped {@code ClientPacketListener.recipeManager} in favour of a {@code ClientRecipeContainer} that only the
 * client can read, so the event carries no payload; listeners invalidate their own caches.
 */
@Environment(EnvType.CLIENT)
public interface ClientRecipesUpdated {
	Event<ClientRecipesUpdated> EVENT = EventFactory.createArrayBacked(ClientRecipesUpdated.class, callbacks -> () -> {
		for (ClientRecipesUpdated event : callbacks)
			event.onRecipesUpdated();
	});

	void onRecipesUpdated();
}
