package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;

/**
 * Player utilities bridging the 1.21.x permission level API onto 26.3's permission sets.
 */
public class PlayerHelper {
	private PlayerHelper() {
	}

	/** 26.3 replaced {@code PlayerHelper.hasPermissionLevel(Player, int)} with named permissions. */
	public static boolean hasPermissionLevel(Player player, int level) {
		if (level <= 0) {
			return true;
		}

		Permission permission = switch (level) {
			case 1 -> Permissions.COMMANDS_MODERATOR;
			case 2 -> Permissions.COMMANDS_GAMEMASTER;
			case 3 -> Permissions.COMMANDS_ADMIN;
			default -> Permissions.COMMANDS_OWNER;
		};
		return player.permissions().hasPermission(permission);
	}
}
