package net.p3pp3rf1y.sophisticatedcore.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.Identifier;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

/**
 * JEI entry point for the core.
 * <p>
 * The upgrade recipes used to be re-registered here as plain shaped recipes so the viewer could
 * render them. 26.3 no longer syncs raw recipes to the client, and those recipes are ordinary
 * crafting recipes anyway, so the viewer picks them up from the synced displays and the extra
 * registration is gone.
 */
@SuppressWarnings("unused")
@JeiPlugin
public class CorePlugin implements IModPlugin {
	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(SophisticatedCore.MOD_ID, "default");
	}
}
