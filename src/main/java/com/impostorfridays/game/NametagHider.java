package com.impostorfridays.game;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Hides nametags by parking players on a scoreboard team whose nametag rule is NEVER.
 *
 * <p>Server-side, so it works on vanilla clients and cannot be undone locally.
 */
public final class NametagHider {

	private static final String TEAM_NAME = "if_hidden";

	private NametagHider() {
	}

	private static Team team(ServerPlayerEntity player) {
		Scoreboard scoreboard = player.getEntityWorld().getServer().getScoreboard();
		Team existing = scoreboard.getTeam(TEAM_NAME);
		if (existing == null) {
			existing = scoreboard.addTeam(TEAM_NAME);
			existing.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
		}
		return existing;
	}

	public static void hide(ServerPlayerEntity player) {
		Scoreboard scoreboard = player.getEntityWorld().getServer().getScoreboard();
		scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team(player));
	}

	/**
	 * Deletes the hidden team entirely, releasing everyone on it.
	 *
	 * <p>Scoreboard teams are saved with the world. If the server is killed mid-match, players
	 * hidden by {@code /blind} or {@code /invis} would stay on the team and have their nametags
	 * hidden permanently, across restarts, with nothing in game to explain why. Called on server
	 * start and again at /start.
	 */
	public static void reset(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();
		Team hidden = scoreboard.getTeam(TEAM_NAME);
		if (hidden != null) {
			scoreboard.removeTeam(hidden);
		}
	}

	public static void show(ServerPlayerEntity player) {
		Scoreboard scoreboard = player.getEntityWorld().getServer().getScoreboard();
		Team hidden = scoreboard.getTeam(TEAM_NAME);
		if (hidden != null) {
			scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), hidden);
		}
	}
}
