package com.impostorfridays.game;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.config.GameConfig;
import com.impostorfridays.net.GameSyncS2C;
import com.impostorfridays.net.RoleAnnounceS2C;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.world.rule.GameRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Owns the single active match and drives it forward each tick.
 *
 * <p>Everything here is server-side and authoritative. Clients are told the outcome via
 * {@link GameSyncS2C} and never decide anything themselves.
 *
 * <p><b>World persistence:</b> neither {@link #start} nor {@link #end} touches player
 * inventories, XP, or world state. Only {@code /amongusreset} does. See IMPLEMENTATION_NOTES.md.
 */
public final class GameManager {
	/** Minimum players required to start, per spec. */
	public static final int MIN_PLAYERS = 2;

	/** How often full state is pushed to clients (ticks). */
	private static final int SYNC_INTERVAL = 10;

	private static GameState state;
	private static int syncCounter;

	/** Shared task description, driven by the task system. */
	private static String taskText = "";
	private static boolean taskComplete;

	private static final Random RANDOM = new Random();

	private GameManager() {
	}

	public static GameState getState() {
		return state;
	}

	public static boolean isActive() {
		return state != null;
	}

	// ------------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------------

	/**
	 * Starts a match.
	 *
	 * @return an error message if the game could not start, or null on success
	 */
	public static Text start(MinecraftServer server) {
		if (isActive()) {
			return Text.literal("A game is already running. Use /end first.").formatted(Formatting.RED);
		}

		List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
		if (players.size() < MIN_PLAYERS) {
			return Text.literal("Need at least " + MIN_PLAYERS + " players to start (currently "
					+ players.size() + ").").formatted(Formatting.RED);
		}

		GameConfig cfg = GameConfig.get();
		state = new GameState(cfg.getGameLengthTicks());
		syncCounter = 0;
		taskComplete = false;

		// Deaths must never cost anyone their gear.
		enforceKeepInventory(server);

		assignRoles(players, cfg);

		TrackingManager.clear();

		// Announce each player's own role, and nobody else's.
		for (ServerPlayerEntity p : players) {
			Role role = state.getRole(p.getUuid());
			if (role == Role.IMPOSTOR) {
				TrackingManager.giveCompass(p);
			}
			ServerPlayNetworking.send(p, new RoleAnnounceS2C(role.ordinal()));
			syncTo(p);
		}

		broadcast(server, Text.literal("The game has begun! ").formatted(Formatting.GOLD)
				.append(Text.literal(cfg.getGameLengthMinutes() + " minutes on the clock.")
						.formatted(Formatting.YELLOW)));

		ImpostorFridays.LOGGER.info("Game started with {} players ({} minutes)",
				players.size(), cfg.getGameLengthMinutes());
		return null;
	}

	/** Randomly assigns exactly one Impostor, optionally one Sniffer, and Innocents for the rest. */
	private static void assignRoles(List<ServerPlayerEntity> players, GameConfig cfg) {
		List<ServerPlayerEntity> shuffled = new ArrayList<>(players);
		Collections.shuffle(shuffled, RANDOM);

		state.assignRole(shuffled.get(0).getUuid(), Role.IMPOSTOR);

		int next = 1;
		// A Sniffer only makes sense when somebody is left to be innocent alongside them.
		if (cfg.isSnifferEnabled() && shuffled.size() >= 3) {
			state.assignRole(shuffled.get(1).getUuid(), Role.SNIFFER);
			next = 2;
		}
		for (int i = next; i < shuffled.size(); i++) {
			state.assignRole(shuffled.get(i).getUuid(), Role.INNOCENT);
		}
	}

	/**
	 * Ends the match and clears all state.
	 *
	 * <p>Safe to call with no game running. Explicitly does NOT touch inventories or the world.
	 */
	public static void end(MinecraftServer server) {
		if (!isActive()) {
			return;
		}
		// Undo anything that would otherwise outlive the match.
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			clearPlayerEffects(p);
		}

		TrackingManager.clear();

		state = null;
		taskText = "";
		taskComplete = false;
		syncCounter = 0;

		// Push the cleared state so every HUD overlay disappears.
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			syncTo(p);
		}

		broadcast(server, Text.literal("The game has ended.").formatted(Formatting.GOLD));
		ImpostorFridays.LOGGER.info("Game ended and state cleared");
	}

	/** Removes any mod-applied status effects so nothing leaks past the match. */
	private static void clearPlayerEffects(ServerPlayerEntity player) {
		player.setInvisible(false);
		player.clearStatusEffects();
	}

	// ------------------------------------------------------------------
	// Tick
	// ------------------------------------------------------------------

	public static void tick(MinecraftServer server) {
		if (!isActive()) {
			return;
		}

		state.tickCountdowns();

		if (state.isTimeUp()) {
			broadcast(server, Text.literal("Time is up! The Impostor wins.").formatted(Formatting.RED));
			end(server);
			return;
		}

		if (++syncCounter >= SYNC_INTERVAL) {
			syncCounter = 0;
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				syncTo(p);
			}
		}
	}

	// ------------------------------------------------------------------
	// Sync
	// ------------------------------------------------------------------

	/** Pushes this player's own view of the game. Never includes anyone else's role. */
	public static void syncTo(ServerPlayerEntity player) {
		if (!isActive()) {
			ServerPlayNetworking.send(player,
					new GameSyncS2C(false, 0, Role.INNOCENT.ordinal(), 0, 0, false, 0, "", false));
			return;
		}

		UUID id = player.getUuid();
		boolean impostor = state.isImpostor(id);
		boolean sniffer = state.isSniffer(id);

		ServerPlayNetworking.send(player, new GameSyncS2C(
				true,
				state.getRemainingTicks(),
				state.getRole(id).ordinal(),
				impostor ? state.getImpostorCooldownTicks() : 0,
				sniffer ? state.getSnifferCooldownTicks() : 0,
				!impostor && state.isEffectActive(Ability.GRAVITY),
				state.getRespawnTicks(id),
				taskText,
				taskComplete
		));
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	/**
	 * Guarantees nobody loses items on death.
	 *
	 * <p>1.21.11 reworked the GameRules API into an immutable value map, so setting a rule
	 * programmatically is awkward. Going through the vanilla command is stable, does the
	 * change-notification bookkeeping for us, and reads clearly in the server log.
	 */
	private static void enforceKeepInventory(MinecraftServer server) {
		if (Boolean.TRUE.equals(server.getOverworld().getGameRules().getValue(GameRules.KEEP_INVENTORY))) {
			return;
		}
		server.getCommandManager().parseAndExecute(
				server.getCommandSource().withSilent(), "gamerule keepInventory true");
		ImpostorFridays.LOGGER.info("Enabled keepInventory for the match");
	}

	public static void broadcast(MinecraftServer server, Text message) {
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			p.sendMessage(message, false);
		}
	}

	public static String getTaskText() {
		return taskText;
	}

	public static void setTaskText(String text) {
		taskText = text == null ? "" : text;
	}

	public static boolean isTaskComplete() {
		return taskComplete;
	}

	public static void setTaskComplete(boolean complete) {
		taskComplete = complete;
	}
}
