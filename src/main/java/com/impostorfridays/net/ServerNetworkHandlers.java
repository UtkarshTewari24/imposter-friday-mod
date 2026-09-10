package com.impostorfridays.net;

import com.impostorfridays.game.GameManager;
import com.impostorfridays.game.SnifferManager;
import com.impostorfridays.game.TrackingManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

/**
 * Server-side receivers for client packets.
 *
 * <p>Everything arriving here is untrusted. Each handler re-checks that a game is running,
 * that the sender holds the role the action requires, and that the target is a real player,
 * before doing anything.
 */
public final class ServerNetworkHandlers {
	private ServerNetworkHandlers() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(AbilityUseC2S.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleAbilityUse(player, payload));
		});

		ServerPlayNetworking.registerGlobalReceiver(PickerSelectC2S.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleSelect(player, payload));
		});
	}

	private static void handleAbilityUse(ServerPlayerEntity player, AbilityUseC2S payload) {
		if (!GameManager.isActive()) {
			player.sendMessage(Text.literal("No game is running.").formatted(Formatting.RED), true);
			return;
		}
		com.impostorfridays.game.Ability ability =
				com.impostorfridays.game.Ability.byId(payload.abilityId());
		if (ability == null) {
			return;
		}
		com.impostorfridays.game.AbilityManager.tryUse(player, ability);
	}

	private static void handleSelect(ServerPlayerEntity player, PickerSelectC2S payload) {
		if (!GameManager.isActive()) {
			player.sendMessage(Text.literal("No game is running.").formatted(Formatting.RED), true);
			return;
		}

		PickerMode mode = PickerMode.byOrdinal(payload.mode());
		UUID targetId = payload.target();

		// Validate the target is a real, online player (unless using the nearest pseudo-target).
		ServerPlayerEntity target = player.getEntityWorld().getServer()
				.getPlayerManager().getPlayer(targetId);
		if (!payload.nearest() && target == null) {
			player.sendMessage(Text.literal("That player is no longer online.")
					.formatted(Formatting.RED), true);
			return;
		}
		if (!payload.nearest() && targetId.equals(player.getUuid())) {
			player.sendMessage(Text.literal("You cannot pick yourself.").formatted(Formatting.RED), true);
			return;
		}

		switch (mode) {
			case TRACK -> {
				// Only the Impostor carries a Tracking Compass.
				if (!GameManager.getState().isImpostor(player.getUuid())) {
					return;
				}
				TrackingManager.setTarget(player, targetId, payload.nearest());
			}
			case STEAL -> {
				if (!GameManager.getState().isImpostor(player.getUuid()) || payload.nearest()) {
					return;
				}
				Text error = com.impostorfridays.game.AbilityManager.validate(
						player, com.impostorfridays.game.Ability.STEAL);
				if (error != null) {
					player.sendMessage(error, false);
					return;
				}
				com.impostorfridays.game.StealManager.open(player, target);
			}
			case SNIFF -> {
				if (!GameManager.getState().isSniffer(player.getUuid())) {
					return;
				}
				if (payload.nearest()) {
					// "Nearest" is a tracking-only convenience; sniffing needs a named target.
					return;
				}
				SnifferManager.sniff(player, target);
			}
		}
	}
}
