package com.impostorfridays.game;

import com.impostorfridays.config.GameConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Validation and execution for every Impostor ability.
 *
 * <p>All five share ONE cooldown: using any of them locks the rest. Every check runs
 * server-side regardless of what the client claims.
 */
public final class AbilityManager {

	/** Effects that were running last tick, so we can detect the moment one ends. */
	private static final Set<Ability> runningEffects = EnumSet.noneOf(Ability.class);

	private AbilityManager() {
	}

	// ------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------

	/** Returns an error message if the ability cannot be used, or null if it can. */
	public static Text validate(ServerPlayerEntity player, Ability ability) {
		GameState state = GameManager.getState();
		if (state == null) {
			return Text.literal("No game is running.").formatted(Formatting.RED);
		}
		if (!state.isImpostor(player.getUuid())) {
			return Text.literal("Only the Impostor can do that.").formatted(Formatting.RED);
		}
		if (!GameConfig.get().isAbilityEnabled(ability)) {
			return Text.literal("/" + ability.getId() + " is disabled on this server.")
					.formatted(Formatting.RED);
		}
		if (state.isAbilityRemoved(ability)) {
			return Text.literal("Your /" + ability.getId()
					+ " ability was stripped by the Sniffer.").formatted(Formatting.RED);
		}
		if (!state.isImpostorReady()) {
			return Text.literal("Abilities are on cooldown for another "
					+ formatSeconds(state.getImpostorCooldownTicks()) + ".").formatted(Formatting.RED);
		}
		return null;
	}

	/** Starts the single shared cooldown. Call after an ability actually succeeds. */
	public static void consumeCooldown(ServerPlayerEntity player) {
		GameState state = GameManager.getState();
		if (state == null) {
			return;
		}
		state.startImpostorCooldown(GameConfig.get().getImpostorCooldownSeconds() * 20);
		GameManager.syncTo(player);
	}

	private static int durationTicks(Ability ability) {
		return GameConfig.get().getAbilityDurationSeconds(ability) * 20;
	}

	// ------------------------------------------------------------------
	// /blind — every Innocent at once
	// ------------------------------------------------------------------

	public static void startBlind(MinecraftServer server, ServerPlayerEntity impostor) {
		GameState state = GameManager.getState();
		int ticks = durationTicks(Ability.BLIND);
		state.startEffect(Ability.BLIND, ticks);

		for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
			if (state.isImpostor(target.getUuid())) {
				continue;
			}
			// showParticles=false and showIcon=false: nothing that lets a bystander work out
			// who is or is not affected, which would out the Impostor by omission.
			target.addStatusEffect(new StatusEffectInstance(
					StatusEffects.BLINDNESS, ticks, 0, false, false, false));
			target.addStatusEffect(new StatusEffectInstance(
					StatusEffects.DARKNESS, ticks, 0, false, false, false));
			target.addStatusEffect(new StatusEffectInstance(
					StatusEffects.WEAKNESS, ticks, 2, false, false, false));
			NametagHider.hide(target);
		}
	}

	private static void endBlind(MinecraftServer server) {
		GameState state = GameManager.getState();
		for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
			target.removeStatusEffect(StatusEffects.BLINDNESS);
			target.removeStatusEffect(StatusEffects.DARKNESS);
			target.removeStatusEffect(StatusEffects.WEAKNESS);
			// Don't un-hide the Impostor if /invis is still running and hiding them.
			boolean stillHiddenByInvis = state != null
					&& state.isEffectActive(Ability.INVIS)
					&& state.isImpostor(target.getUuid());
			if (!stillHiddenByInvis) {
				NametagHider.show(target);
			}
		}
	}

	// ------------------------------------------------------------------
	// /invis — total invisibility for the Impostor
	// ------------------------------------------------------------------

	public static void startInvis(MinecraftServer server, ServerPlayerEntity impostor) {
		GameState state = GameManager.getState();
		int ticks = durationTicks(Ability.INVIS);
		state.startEffect(Ability.INVIS, ticks);

		impostor.addStatusEffect(new StatusEffectInstance(
				StatusEffects.INVISIBILITY, ticks, 0, false, false, false));
		NametagHider.hide(impostor);
		// Vanilla invisibility still renders armour and held items, so blank them out
		// for everyone else by sending empty equipment.
		broadcastEquipment(server, impostor, true);
	}

	private static void endInvis(MinecraftServer server) {
		GameState state = GameManager.getState();
		if (state == null || state.getImpostorId() == null) {
			return;
		}
		ServerPlayerEntity impostor = server.getPlayerManager().getPlayer(state.getImpostorId());
		if (impostor == null) {
			return;
		}
		impostor.removeStatusEffect(StatusEffects.INVISIBILITY);
		NametagHider.show(impostor);
		broadcastEquipment(server, impostor, false);
	}

	/**
	 * Brings a player who just joined in line with any effect already running.
	 *
	 * <p>Without this, someone joining mid-{@code /invis} receives the Impostor's real equipment
	 * with their normal spawn packets and sees floating armour — which would out the Impostor.
	 */
	public static void onPlayerJoin(MinecraftServer server, ServerPlayerEntity joiner) {
		GameState state = GameManager.getState();
		if (state == null || !state.isEffectActive(Ability.INVIS) || state.getImpostorId() == null) {
			return;
		}
		ServerPlayerEntity impostor = server.getPlayerManager().getPlayer(state.getImpostorId());
		if (impostor != null && impostor != joiner) {
			joiner.networkHandler.sendPacket(equipmentPacket(impostor, true));
		}
	}

	/** Clears anything that would otherwise be left stuck on a player who left. */
	public static void onPlayerDisconnect(ServerPlayerEntity leaver) {
		GameState state = GameManager.getState();
		if (state == null) {
			return;
		}
		// Don't leave them flagged dead — otherwise the respawn gate and voice muting would
		// still apply to them the moment they reconnect.
		state.clearDead(leaver.getUuid());
		NametagHider.show(leaver);
	}

	private static EntityEquipmentUpdateS2CPacket equipmentPacket(ServerPlayerEntity subject,
			boolean blank) {
		List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = blank ? ItemStack.EMPTY : subject.getEquippedStack(slot);
			equipment.add(new Pair<>(slot, stack.copy()));
		}
		return new EntityEquipmentUpdateS2CPacket(subject.getId(), equipment);
	}

	/** Sends either blanked-out or real equipment for this player to everyone else. */
	private static void broadcastEquipment(MinecraftServer server, ServerPlayerEntity subject,
			boolean blank) {
		EntityEquipmentUpdateS2CPacket packet = equipmentPacket(subject, blank);

		for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
			if (viewer != subject) {
				viewer.networkHandler.sendPacket(packet);
			}
		}
	}

	// ------------------------------------------------------------------
	// /gravity — everyone except the Impostor
	// ------------------------------------------------------------------

	public static void startGravity(MinecraftServer server, ServerPlayerEntity impostor) {
		GameState state = GameManager.getState();
		int ticks = durationTicks(Ability.GRAVITY);
		state.startEffect(Ability.GRAVITY, ticks);

		for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
			if (state.isImpostor(target.getUuid())) {
				continue;
			}
			// Minecraft has no gravity-inversion API. Levitation is the closest playable
			// approximation, and pairs with the client-side 180 degree camera flip.
			target.addStatusEffect(new StatusEffectInstance(
					StatusEffects.LEVITATION, ticks, 0, false, false, false));
		}
		// The camera flip itself is driven by GameSyncS2C.gravityActive.
	}

	private static void endGravity(MinecraftServer server) {
		GameState state = GameManager.getState();
		for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
			if (state != null && state.isImpostor(target.getUuid())) {
				continue;
			}
			target.removeStatusEffect(StatusEffects.LEVITATION);
			// Cushion the drop so the ability is never lethal on its own.
			target.addStatusEffect(new StatusEffectInstance(
					StatusEffects.SLOW_FALLING, 200, 0, false, false, false));
		}
	}

	// ------------------------------------------------------------------
	// Tick: detect effects ending and clean up
	// ------------------------------------------------------------------

	public static void tick(MinecraftServer server) {
		GameState state = GameManager.getState();
		if (state == null) {
			if (!runningEffects.isEmpty()) {
				// Game ended mid-effect — tear everything down.
				cleanupAll(server);
			}
			return;
		}

		for (Ability ability : new Ability[]{Ability.BLIND, Ability.INVIS, Ability.GRAVITY}) {
			boolean active = state.isEffectActive(ability);
			boolean wasActive = runningEffects.contains(ability);
			if (active && !wasActive) {
				runningEffects.add(ability);
			} else if (!active && wasActive) {
				runningEffects.remove(ability);
				endEffect(server, ability);
			}
		}
	}

	private static void endEffect(MinecraftServer server, Ability ability) {
		switch (ability) {
			case BLIND -> endBlind(server);
			case INVIS -> endInvis(server);
			case GRAVITY -> endGravity(server);
			default -> {
			}
		}
	}

	/** Hard reset used by {@code /end} so nothing can outlive a match. */
	public static void cleanupAll(MinecraftServer server) {
		endBlind(server);
		endInvis(server);
		endGravity(server);
		runningEffects.clear();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			NametagHider.show(player);
			broadcastEquipment(server, player, false);
		}
	}

	static String formatSeconds(int ticks) {
		int seconds = Math.max(0, ticks) / 20;
		return seconds >= 60 ? (seconds / 60) + "m " + (seconds % 60) + "s" : seconds + "s";
	}
}
