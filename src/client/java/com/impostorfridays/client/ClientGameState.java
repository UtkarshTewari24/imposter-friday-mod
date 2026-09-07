package com.impostorfridays.client;

import com.impostorfridays.game.Role;
import com.impostorfridays.net.GameSyncS2C;

import java.util.List;

/**
 * The client's mirror of server state.
 *
 * <p>Written only by the {@code GameSyncS2C} receiver and read by the HUD. Nothing here is
 * ever computed locally — the server is the single source of truth for the timer and
 * cooldowns, so these values cannot drift.
 */
public final class ClientGameState {
	public static boolean active;
	public static int remainingTicks;
	public static Role role = Role.INNOCENT;
	public static int impostorCooldownTicks;
	public static int snifferCooldownTicks;
	public static boolean gravityActive;
	public static int respawnTicks;
	/** Every objective in the current set, with its completion state. */
	public static List<GameSyncS2C.TaskLine> tasks = List.of();

	/** Wall-clock deadline for the one-shot role announcement overlay. */
	public static long roleAnnounceUntilMs;
	public static Role announcedRole;

	/** Wall-clock deadline for the "you were sniffed" cue. */
	public static long sniffCueUntilMs;

	private ClientGameState() {
	}

	/** Wipes everything, so no overlay can outlive a match. */
	public static void reset() {
		active = false;
		remainingTicks = 0;
		role = Role.INNOCENT;
		impostorCooldownTicks = 0;
		snifferCooldownTicks = 0;
		gravityActive = false;
		respawnTicks = 0;
		tasks = List.of();
		roleAnnounceUntilMs = 0;
		announcedRole = null;
		sniffCueUntilMs = 0;
	}

	/** Formats a tick count as {@code M:SS}, or {@code H:MM:SS} past an hour. */
	public static String formatTicks(int ticks) {
		int totalSeconds = Math.max(0, ticks) / 20;
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		if (hours > 0) {
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		}
		return String.format("%d:%02d", minutes, seconds);
	}
}
