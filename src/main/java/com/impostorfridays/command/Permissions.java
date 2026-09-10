package com.impostorfridays.command;

import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Locale;

/**
 * Permission checks for every mod command.
 *
 * <p>Authorisation is granted by EITHER being named in {@link #ADMIN_USERNAMES} OR holding
 * vanilla operator level 2+, so the server owner can OP additional people without editing code.
 */
public final class Permissions {

	/**
	 * Always-authorised admins, regardless of vanilla OP status.
	 * Edit this list to change the trusted roster.
	 *
	 * <p>NOTE: these are Mojang <em>usernames</em>, not UUIDs, as specified. A username change
	 * would silently break the check — see IMPLEMENTATION_NOTES.md.
	 */
	public static final List<String> ADMIN_USERNAMES = List.of(
			"MrBoombox840",
			"SpeedTellyYT"
	);

	/**
	 * Vanilla permission level accepted as the alternative to the allowlist.
	 * GAMEMASTERS is level 2 — the classic "op" tier for gameplay commands.
	 *
	 * <p>1.21.11 replaced {@code hasPermissionLevel(int)} with this predicate-based system.
	 */
	public static final Permission OP_LEVEL = new Permission.Level(PermissionLevel.GAMEMASTERS);

	private Permissions() {
	}

	public static boolean isAdminName(String username) {
		if (username == null) {
			return false;
		}
		String lower = username.toLowerCase(Locale.ROOT);
		for (String admin : ADMIN_USERNAMES) {
			if (admin.toLowerCase(Locale.ROOT).equals(lower)) {
				return true;
			}
		}
		return false;
	}

	/** True if the source may run mod admin commands. Console always qualifies. */
	public static boolean isAuthorized(ServerCommandSource source) {
		ServerPlayerEntity player = source.getPlayer();
		if (player == null) {
			// Console / command block running at sufficient level.
			return hasOpLevel(source);
		}
		// GameProfile is a record in authlib 7.x, so the accessor is name(), not getName().
		return isAdminName(player.getGameProfile().name()) || hasOpLevel(source);
	}

	/**
	 * Checks authorisation and explains the refusal.
	 *
	 * <p>Deliberately used INSTEAD of Brigadier's {@code .requires(...)} for admin commands.
	 * {@code requires} hides the command entirely from anyone unauthorised, so a server owner
	 * who simply hasn't been OP'd sees "Unknown command" and has no idea why — which reads like
	 * the mod is broken rather than like a permission problem.
	 *
	 * @return true if the source may proceed
	 */
	public static boolean checkAuthorized(ServerCommandSource source) {
		if (isAuthorized(source)) {
			return true;
		}
		source.sendError(net.minecraft.text.Text.literal(
				"You need to be a server operator to use this. Run 'op "
						+ nameOf(source) + "' from the server console.")
				.formatted(net.minecraft.util.Formatting.RED));
		return false;
	}

	private static String nameOf(ServerCommandSource source) {
		ServerPlayerEntity player = source.getPlayer();
		return player != null ? player.getGameProfile().name() : "<yourname>";
	}

	private static boolean hasOpLevel(ServerCommandSource source) {
		return source.getPermissions().hasPermission(OP_LEVEL);
	}
}
