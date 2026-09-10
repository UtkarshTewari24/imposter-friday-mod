package com.impostorfridays.game;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.UUID;

/**
 * The end-of-match reveal: everyone returns to spawn and the Impostor lights up red.
 *
 * <p>The red outline is done with a scoreboard team rather than the glowing effect alone,
 * because the glow takes its colour from the entity's team — a bare glowing effect would render
 * plain white and read as a bug.
 */
public final class EndGameReveal {

	private static final String TEAM_NAME = "if_impostor_reveal";
	/** How long the outline stays up after the match ends. */
	private static final int GLOW_TICKS = 30 * 20;

	private EndGameReveal() {
	}

	/** Highlights the Impostor in red and sends everyone back to world spawn. */
	public static void run(MinecraftServer server, UUID impostorId, boolean innocentsWon) {
		revealImpostor(server, impostorId);
		teleportEveryoneToSpawn(server);

		ServerPlayerEntity impostor = impostorId == null
				? null : server.getPlayerManager().getPlayer(impostorId);
		String name = impostor != null ? impostor.getGameProfile().name() : "The Impostor";

		GameManager.broadcast(server, Text.literal("The Impostor was ")
				.formatted(Formatting.GRAY)
				.append(Text.literal(name).formatted(Formatting.RED, Formatting.BOLD))
				.append(Text.literal(innocentsWon ? " — and they lost." : " — and they won.")
						.formatted(Formatting.GRAY)));
	}

	private static void revealImpostor(MinecraftServer server, UUID impostorId) {
		if (impostorId == null) {
			return;
		}
		ServerPlayerEntity impostor = server.getPlayerManager().getPlayer(impostorId);
		if (impostor == null) {
			return;
		}

		Scoreboard scoreboard = server.getScoreboard();
		Team team = scoreboard.getTeam(TEAM_NAME);
		if (team == null) {
			team = scoreboard.addTeam(TEAM_NAME);
		}
		// The team colour is what makes the outline red rather than white.
		team.setColor(Formatting.RED);
		scoreboard.addScoreHolderToTeam(impostor.getNameForScoreboard(), team);

		impostor.addStatusEffect(new StatusEffectInstance(
				StatusEffects.GLOWING, GLOW_TICKS, 0, false, false, true));
		impostor.setGlowing(true);
	}

	private static void teleportEveryoneToSpawn(MinecraftServer server) {
		ServerWorld overworld = server.getOverworld();
		BlockPos spawn = overworld.getSpawnPoint().getPos();
		Vec3d target = new Vec3d(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.teleportTo(new TeleportTarget(overworld, target, Vec3d.ZERO,
					player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
		}
	}

	/** Clears the reveal so a new match never starts with someone still outlined. */
	public static void clear(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();
		Team team = scoreboard.getTeam(TEAM_NAME);
		if (team != null) {
			scoreboard.removeTeam(team);
		}
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.setGlowing(false);
			player.removeStatusEffect(StatusEffects.GLOWING);
		}
	}
}
