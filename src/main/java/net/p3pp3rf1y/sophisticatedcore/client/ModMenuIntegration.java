package net.p3pp3rf1y.sophisticatedcore.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new ConfigurationScreen(SophisticatedCore.MOD_ID, parent);
	}
}
