package com.impostorfridays.game;

import com.impostorfridays.config.GameConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Death and respawn handling.
 *
 * <p>Death is never elimination: roles, abilities and the shared cooldown all survive it
 * untouched. An Impostor who dies is still the Impostor when they get back up.
 */
public final class DeathManager {

	private DeathManager() {
	}

	/** Starts the respawn countdown for a player who just died. */
	public static void onDeath(ServerPlayerEntity player) {
		GameState state = GameManager.getState();
		if (state == null) {
			return;
		}
		int delayTicks = GameConfig.get().getRespawnDelaySeconds() * 20;
		state.markDead(player.getUuid(), delayTicks);
		GameManager.syncTo(player);
	}

	/**
	 * Whether this player is allowed to respawn yet.
	 *
	 * <p>Enforced server-side as well as on the button, so a modified client cannot skip
	 * the wait by sending the respawn packet directly.
	 */
	public static boolean canRespawn(ServerPlayerEntity player) {
		GameState state = GameManager.getState();
		if (state == null) {
			return true;
		}
		UUID id = player.getUuid();
		if (!state.isDead(id)) {
			return true;
		}
		return state.getRespawnTicks(id) <= 0;
	}

	/** Clears the dead flag and restores anything the player must always be holding. */
	public static void onRespawn(ServerPlayerEntity player) {
		GameState state = GameManager.getState();
		if (state == null) {
			return;
		}
		state.clearDead(player.getUuid());

		// The Impostor keeps their compass across deaths.
		if (state.isImpostor(player.getUuid())) {
			TrackingManager.giveCompass(player);
		}
		GameManager.syncTo(player);
	}

	/** Keeps dead players' HUD countdown ticking. */
	public static void tick(MinecraftServer server) {
		GameState state = GameManager.getState();
		if (state == null || state.getRespawnTimers().isEmpty()) {
			return;
		}
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (state.isDead(player.getUuid())) {
				GameManager.syncTo(player);
			}
		}
	}
}
