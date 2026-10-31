package net.p3pp3rf1y.sophisticatedcore.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;


public class REIClientCompat implements REIClientPlugin {
	@Override
	public void registerDisplays(DisplayRegistry registry) {
		// 26.3 sends display data instead of raw recipes, and the upgrade recipes are plain
		// crafting recipes, so REI already shows them through the vanilla crafting category.
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerDraggableStackVisitor(new REISettingsGhostIngredientHandler<>());
	}
}
