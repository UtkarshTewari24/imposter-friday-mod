package com.impostorfridays.game;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * All mutable state for a single match.
 *
 * <p>Deliberately free of Minecraft API types so the cooldown, role and timer logic
 * can be unit tested directly. The server owns exactly one instance while a game is
 * running; {@code /end} discards it.
 */
public class GameState {
	private final Map<UUID, Role> roles = new HashMap<>();
	private UUID impostorId;
	private UUID snifferId;

	private int remainingTicks;

	/** Shared across every Impostor ability — using any one locks them all. */
	private int impostorCooldownTicks;
	private int snifferCooldownTicks;

	/** Abilities permanently stripped by a successful sniff, for the rest of the match. */
	private final Set<Ability> removedAbilities = EnumSet.noneOf(Ability.class);

	/** Ticks remaining on each timed ability effect. */
	private final Map<Ability, Integer> activeEffectTicks = new HashMap<>();

	/** Players currently dead, mapped to ticks until they may respawn. */
	private final Map<UUID, Integer> respawnTimers = new HashMap<>();

	/** Players sniffed at least once, so the Sniffer isn't told the same thing twice. */
	private final Set<UUID> sniffedPlayers = new HashSet<>();

	public GameState(int totalTicks) {
		this.remainingTicks = totalTicks;
	}

	// ------------------------------------------------------------------
	// Roles
	// ------------------------------------------------------------------

	public void assignRole(UUID player, Role role) {
		roles.put(player, role);
		if (role == Role.IMPOSTOR) {
			impostorId = player;
		} else if (role == Role.SNIFFER) {
			snifferId = player;
		}
	}

	/**
	 * Players who join mid-match are Innocents. Returning INNOCENT for unknown players
	 * (rather than null) keeps every caller from having to null-check.
	 */
	public Role getRole(UUID player) {
		return roles.getOrDefault(player, Role.INNOCENT);
	}

	public boolean isImpostor(UUID player) {
		return player != null && player.equals(impostorId);
	}

	public boolean isSniffer(UUID player) {
		return player != null && player.equals(snifferId);
	}

	public UUID getImpostorId() {
		return impostorId;
	}

	public UUID getSnifferId() {
		return snifferId;
	}

	public Map<UUID, Role> getRoles() {
		return roles;
	}

	// ------------------------------------------------------------------
	// Timer
	// ------------------------------------------------------------------

	public int getRemainingTicks() {
		return remainingTicks;
	}

	public void setRemainingTicks(int ticks) {
		this.remainingTicks = Math.max(0, ticks);
	}

	public boolean isTimeUp() {
		return remainingTicks <= 0;
	}

	// ------------------------------------------------------------------
	// Cooldowns
	// ------------------------------------------------------------------

	public int getImpostorCooldownTicks() {
		return impostorCooldownTicks;
	}

	public boolean isImpostorReady() {
		return impostorCooldownTicks <= 0;
	}

	public void startImpostorCooldown(int ticks) {
		this.impostorCooldownTicks = Math.max(0, ticks);
	}

	public int getSnifferCooldownTicks() {
		return snifferCooldownTicks;
	}

	public boolean isSnifferReady() {
		return snifferCooldownTicks <= 0;
	}

	public void startSnifferCooldown(int ticks) {
		this.snifferCooldownTicks = Math.max(0, ticks);
	}

	// ------------------------------------------------------------------
	// Abilities
	// ------------------------------------------------------------------

	public boolean isAbilityRemoved(Ability a) {
		return removedAbilities.contains(a);
	}

	public void removeAbility(Ability a) {
		removedAbilities.add(a);
	}

	public Set<Ability> getRemovedAbilities() {
		return removedAbilities;
	}

	public int getEffectTicks(Ability a) {
		return activeEffectTicks.getOrDefault(a, 0);
	}

	public boolean isEffectActive(Ability a) {
		return getEffectTicks(a) > 0;
	}

	public void startEffect(Ability a, int ticks) {
		activeEffectTicks.put(a, Math.max(0, ticks));
	}

	public void clearEffect(Ability a) {
		activeEffectTicks.remove(a);
	}

	public Map<Ability, Integer> getActiveEffectTicks() {
		return activeEffectTicks;
	}

	// ------------------------------------------------------------------
	// Death / respawn
	// ------------------------------------------------------------------

	public void markDead(UUID player, int delayTicks) {
		respawnTimers.put(player, Math.max(0, delayTicks));
	}

	public boolean isDead(UUID player) {
		return respawnTimers.containsKey(player);
	}

	public int getRespawnTicks(UUID player) {
		return respawnTimers.getOrDefault(player, 0);
	}

	public void clearDead(UUID player) {
		respawnTimers.remove(player);
	}

	public Map<UUID, Integer> getRespawnTimers() {
		return respawnTimers;
	}

	// ------------------------------------------------------------------
	// Sniffing
	// ------------------------------------------------------------------

	public boolean hasBeenSniffed(UUID player) {
		return sniffedPlayers.contains(player);
	}

	public void markSniffed(UUID player) {
		sniffedPlayers.add(player);
	}

	// ------------------------------------------------------------------
	// Per-tick advance
	// ------------------------------------------------------------------

	/** Advances every countdown by one tick. Pure bookkeeping; callers apply the effects. */
	public void tickCountdowns() {
		if (remainingTicks > 0) {
			remainingTicks--;
		}
		if (impostorCooldownTicks > 0) {
			impostorCooldownTicks--;
		}
		if (snifferCooldownTicks > 0) {
			snifferCooldownTicks--;
		}
		activeEffectTicks.entrySet().removeIf(e -> {
			int left = e.getValue() - 1;
			e.setValue(left);
			return left <= 0;
		});
		respawnTimers.replaceAll((id, t) -> Math.max(0, t - 1));
	}
}
