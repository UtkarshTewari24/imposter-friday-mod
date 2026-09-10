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

	/**
	 * Validates and runs an ability. The single path used by BOTH the commands and the
	 * optional keybinds, so the two can never drift apart in what they allow.
	 *
	 * @return true if the ability actually fired
	 */
	public static boolean tryUse(ServerPlayerEntity player, Ability ability) {
		Text error = validate(player, ability);
		if (error != null) {
			player.sendMessage(error, false);
			return false;
		}
		MinecraftServer server = player.getEntityWorld().getServer();
		switch (ability) {
			case BLIND -> startBlind(server, player);
			case HUNT -> startHunt(server, player);
			case GRAVITY -> startGravity(server, player);
			case STEAL -> {
				StealManager.requestPicker(player);
				// The cooldown is taken when the steal window actually opens.
				return true;
			}
			case SWAP -> {
				player.sendMessage(Text.literal("/swap needs two players — use the command.")
						.formatted(Formatting.RED), false);
				return false;
			}
			default -> {
				return false;
			}
		}
		consumeCooldown(player);
		player.sendMessage(Text.literal("/" + ability.getId() + " activated.")
				.formatted(Formatting.GREEN), true);
		return true;
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
			boolean stillHiddenByHunt = state != null
					&& state.isEffectActive(Ability.HUNT)
					&& state.isImpostor(target.getUuid());
			if (!stillHiddenByHunt) {
				NametagHider.show(target);
			}
		}
	}

	// ------------------------------------------------------------------
	// /hunt — the Impostor's kill window: invisible, faster and stronger
	// ------------------------------------------------------------------

	public static void startHunt(MinecraftServer server, ServerPlayerEntity impostor) {
		GameState state = GameManager.getState();
		int ticks = durationTicks(Ability.HUNT);
		state.startEffect(Ability.HUNT, ticks);

		// All three run for the same configured duration so the window is easy to reason about.
		impostor.addStatusEffect(new StatusEffectInstance(
				StatusEffects.INVISIBILITY, ticks, 0, false, false, false));
		impostor.addStatusEffect(new StatusEffectInstance(
				StatusEffects.STRENGTH, ticks, 1, false, false, false));
		impostor.addStatusEffect(new StatusEffectInstance(
				StatusEffects.SPEED, ticks, 1, false, false, false));

		NametagHider.hide(impostor);
		// Vanilla invisibility still renders armour and held items, so blank them out
		// for everyone else by sending empty equipment.
		broadcastEquipment(server, impostor, true);
	}

	private static void endHunt(MinecraftServer server) {
		GameState state = GameManager.getState();
		if (state == null || state.getImpostorId() == null) {
			return;
		}
		ServerPlayerEntity impostor = server.getPlayerManager().getPlayer(state.getImpostorId());
		if (impostor == null) {
			return;
		}
		impostor.removeStatusEffect(StatusEffects.INVISIBILITY);
		impostor.removeStatusEffect(StatusEffects.STRENGTH);
		impostor.removeStatusEffect(StatusEffects.SPEED);
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
		if (state == null || !state.isEffectActive(Ability.HUNT) || state.getImpostorId() == null) {
			return;
		}
		ServerPlayerEntity impostor = server.getPlayerManager().getPlayer(state.getImpostorId());
		if (impostor == null) {
			return;
		}
		if (impostor == joiner) {
			// The Impostor themselves reconnected mid-effect: everyone else's client has just
			// been handed their real equipment with the spawn packets, so blank it again.
			broadcastEquipment(server, impostor, true);
		} else {
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

	/**
	 * Flips gravity for EVERYONE on the server, the Impostor included — it is a whole-world
	 * event, not something done to other people.
	 *
	 * <p>Anyone in a boat is immune, which gives players a real counter to look for.
	 */
	public static void startGravity(MinecraftServer server, ServerPlayerEntity impostor) {
		GameState state = GameManager.getState();
		int ticks = durationTicks(Ability.GRAVITY);
		state.startEffect(Ability.GRAVITY, ticks);

		for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
			if (isInBoat(target)) {
				continue;
			}
			// Minecraft has no gravity-inversion API. Levitation is the closest playable
			// approximation, and pairs with the client-side 180 degree camera flip.
			target.addStatusEffect(new StatusEffectInstance(
					StatusEffects.LEVITATION, ticks, 0, false, false, false));
		}
		// The camera flip itself is driven by GameSyncS2C.gravityActive.
	}

	/** Boats are the one place anti-gravity does not reach. */
	public static boolean isInBoat(ServerPlayerEntity player) {
		return player.hasVehicle()
				&& player.getVehicle() instanceof net.minecraft.entity.vehicle.AbstractBoatEntity;
	}

	/**
	 * Keeps gravity honest while it runs: someone who leaves a boat mid-effect starts
	 * floating, and someone who climbs into one stops.
	 */
	private static void refreshGravity(MinecraftServer server, int remainingTicks) {
		for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
			if (isInBoat(target)) {
				target.removeStatusEffect(StatusEffects.LEVITATION);
			} else if (!target.hasStatusEffect(StatusEffects.LEVITATION)) {
				target.addStatusEffect(new StatusEffectInstance(
						StatusEffects.LEVITATION, remainingTicks, 0, false, false, false));
			}
		}
	}

	private static void endGravity(MinecraftServer server) {
		for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
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

		if (state.isEffectActive(Ability.GRAVITY)) {
			refreshGravity(server, state.getEffectTicks(Ability.GRAVITY));
		}

		for (Ability ability : new Ability[]{Ability.BLIND, Ability.HUNT, Ability.GRAVITY}) {
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
			case HUNT -> endHunt(server);
			case GRAVITY -> endGravity(server);
			default -> {
			}
		}
	}

	/** Hard reset used by {@code /end} so nothing can outlive a match. */
	public static void cleanupAll(MinecraftServer server) {
		endBlind(server);
		endHunt(server);
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
