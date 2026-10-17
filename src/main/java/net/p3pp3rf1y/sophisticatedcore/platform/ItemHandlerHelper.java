package net.p3pp3rf1y.sophisticatedcore.platform;

import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Inventory helpers that the original code took from Forge's {@code ItemHandlerHelper}.
 */
public final class ItemHandlerHelper {
	private ItemHandlerHelper() {
	}

	/** Puts {@code stack} into the player's inventory, dropping whatever does not fit. */
	public static void giveItemToPlayer(Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		if (!player.getInventory().add(stack)) {
			player.drop(stack, false, Prediction.PREDICTED);
		}
	}
}
