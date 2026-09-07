package com.impostorfridays.command;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.game.GameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * {@code /amongusreset} — the ONLY command that erases anyone's progress.
 *
 * <p>The whole point of this mod's world model is that gear, builds and loot persist
 * indefinitely across matches; {@code /start} and {@code /end} never touch them. This command
 * is the single deliberate exception, so it is admin-gated and requires explicit confirmation
 * inside a short window.
 *
 * <p>World regeneration is intentionally NOT automated here — see
 * {@link #showWorldResetInstructions} and IMPLEMENTATION_NOTES.md.
 */
public final class ResetCommand {

	/** How long a pending confirmation stays valid. */
	private static final long CONFIRM_WINDOW_MS = 30_000L;

	private static long pendingSince;
	private static String pendingBy;

	private ResetCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("amongusreset")
				.requires(Permissions::isAuthorized)
				.executes(ctx -> {
					warn(ctx.getSource());
					return 1;
				})
				.then(CommandManager.literal("confirm")
						.executes(ctx -> {
							confirm(ctx.getSource());
							return 1;
						}))
				.then(CommandManager.literal("world")
						.executes(ctx -> {
							showWorldResetInstructions(ctx.getSource());
							return 1;
						})));
	}

	// ------------------------------------------------------------------
	// Inventory + XP reset
	// ------------------------------------------------------------------

	private static void warn(ServerCommandSource source) {
		pendingSince = System.currentTimeMillis();
		pendingBy = sourceName(source);

		source.sendFeedback(() -> Text.literal(""), false);
		source.sendFeedback(() -> Text.literal("⚠ THIS WILL WIPE EVERYONE'S ITEMS")
				.formatted(Formatting.RED, Formatting.BOLD), false);
		source.sendFeedback(() -> Text.literal(
				"It clears every player's inventory, ender chest and XP. This cannot be undone.")
				.formatted(Formatting.YELLOW), false);
		source.sendFeedback(() -> Text.literal("It does NOT touch the world, builds, or the seed.")
				.formatted(Formatting.GRAY), false);
		source.sendFeedback(() -> Text.literal("")
				.append(Text.literal("[ Click here to confirm ]")
						.styled(s -> s.withColor(Formatting.RED)
								.withClickEvent(new ClickEvent.RunCommand("/amongusreset confirm"))))
				.append(Text.literal("  (expires in 30 seconds)").formatted(Formatting.DARK_GRAY)), false);
	}

	private static void confirm(ServerCommandSource source) {
		if (pendingSince == 0 || System.currentTimeMillis() - pendingSince > CONFIRM_WINDOW_MS) {
			source.sendError(Text.literal(
					"Nothing pending to confirm. Run /amongusreset first.").formatted(Formatting.RED));
			return;
		}
		if (!sourceName(source).equals(pendingBy)) {
			source.sendError(Text.literal(
					"Only the admin who started the reset can confirm it.").formatted(Formatting.RED));
			return;
		}
		pendingSince = 0;
		pendingBy = null;

		MinecraftServer server = source.getServer();

		// End any running match first, so no state survives the wipe.
		if (GameManager.isActive()) {
			GameManager.end(server);
		}

		int affected = 0;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.getInventory().clear();
			player.getEnderChestInventory().clear();
			player.experienceLevel = 0;
			player.totalExperience = 0;
			player.experienceProgress = 0.0f;
			player.addExperienceLevels(0); // forces the client XP bar to resync
			affected++;
		}

		GameManager.broadcast(server, Text.literal(
				"⚠ An admin has reset all inventories and XP. Fresh start!")
				.formatted(Formatting.RED, Formatting.BOLD));
		ImpostorFridays.LOGGER.warn("/amongusreset confirmed: wiped inventories and XP for {} players",
				affected);

		final int count = affected;
		source.sendFeedback(() -> Text.literal("Reset " + count + " online player(s).")
				.formatted(Formatting.GREEN), true);
		source.sendFeedback(() -> Text.literal(
				"Note: players who are offline keep their items until they next log in and are "
						+ "reset manually.").formatted(Formatting.GRAY), false);
	}

	// ------------------------------------------------------------------
	// World reset — deliberately manual
	// ------------------------------------------------------------------

	/**
	 * Prints the manual procedure instead of doing it.
	 *
	 * <p>Minecraft cannot unload and regenerate a live world in-process; doing this from a
	 * running server means deleting the save directory out from under an open file handle,
	 * which risks a corrupted or half-deleted world. A short manual procedure is both safer
	 * and easier to reason about, so this command refuses to act on its own.
	 */
	private static void showWorldResetInstructions(ServerCommandSource source) {
		source.sendFeedback(() -> Text.literal(""), false);
		source.sendFeedback(() -> Text.literal("Resetting the WORLD is a manual step")
				.formatted(Formatting.GOLD, Formatting.BOLD), false);
		source.sendFeedback(() -> Text.literal(
				"A running server cannot safely delete its own world files, so this command "
						+ "will not do it for you.").formatted(Formatting.GRAY), false);
		source.sendFeedback(() -> Text.literal("On the machine hosting the server:")
				.formatted(Formatting.YELLOW), false);
		source.sendFeedback(() -> Text.literal("  1. Run /stop in the server console")
				.formatted(Formatting.WHITE), false);
		source.sendFeedback(() -> Text.literal("  2. Delete (or rename) the 'world' folder")
				.formatted(Formatting.WHITE), false);
		source.sendFeedback(() -> Text.literal(
				"  3. To keep the same seed, set level-seed= in server.properties first")
				.formatted(Formatting.WHITE), false);
		source.sendFeedback(() -> Text.literal("  4. Start the server again")
				.formatted(Formatting.WHITE), false);
		source.sendFeedback(() -> Text.literal(
				"Renaming rather than deleting keeps a backup you can restore.")
				.formatted(Formatting.DARK_GRAY), false);
	}

	private static String sourceName(ServerCommandSource source) {
		ServerPlayerEntity player = source.getPlayer();
		return player != null ? player.getGameProfile().name() : "CONSOLE";
	}
}
