package com.impostorfridays.game;

import com.impostorfridays.config.GameConfig;
import com.impostorfridays.net.OpenPickerS2C;
import com.impostorfridays.net.PickerMode;
import com.impostorfridays.net.SniffedS2C;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Sniffer role.
 *
 * <p>A successful sniff permanently strips one random ability from the Impostor for the rest
 * of the match. The sniffed player is told it happened; nobody else can tell, because the cue
 * is delivered as a private packet rather than as world particles.
 */
public final class SnifferManager {

	private static final Random RANDOM = new Random();

	private SnifferManager() {
	}

	/** Opens the picker for {@code /sniff}, reusing the Tracking Compass UI. */
	public static void requestPicker(ServerPlayerEntity player) {
		GameState state = GameManager.getState();
		if (state == null) {
			player.sendMessage(Text.literal("No game is running.").formatted(Formatting.RED), false);
			return;
		}
		if (!state.isSniffer(player.getUuid())) {
			player.sendMessage(Text.literal("Only the Sniffer can do that.").formatted(Formatting.RED), false);
			return;
		}
		if (!state.isSnifferReady()) {
			player.sendMessage(Text.literal("Sniff is on cooldown for another "
					+ formatSeconds(state.getSnifferCooldownTicks()) + ".").formatted(Formatting.RED), false);
			return;
		}
		List<OpenPickerS2C.Entry> entries = TrackingManager.candidatesFor(player);
		ServerPlayNetworking.send(player, new OpenPickerS2C(PickerMode.SNIFF.ordinal(), entries));
	}

	/** Resolves a validated sniff attempt. */
	public static void sniff(ServerPlayerEntity sniffer, ServerPlayerEntity target) {
		GameState state = GameManager.getState();
		if (state == null || !state.isSniffer(sniffer.getUuid()) || !state.isSnifferReady()) {
			return;
		}

		state.startSnifferCooldown(GameConfig.get().getSnifferCooldownSeconds() * 20);
		state.markSniffed(target.getUuid());

		// The target always learns they were sniffed, whoever they are — otherwise only the
		// Impostor would ever get the cue, which would itself leak information.
		ServerPlayNetworking.send(target, new SniffedS2C());

		if (state.isImpostor(target.getUuid())) {
			Ability removed = removeRandomAbility(state);
			if (removed != null) {
				sniffer.sendMessage(Text.literal("★ You found the Impostor! ")
						.formatted(Formatting.GOLD, Formatting.BOLD)
						.append(Text.literal("Their /" + removed.getId() + " ability is gone for good.")
								.formatted(Formatting.YELLOW)), false);
				target.sendMessage(Text.literal("You have been sniffed. Your /" + removed.getId()
						+ " ability has been stripped.").formatted(Formatting.RED), false);
			} else {
				sniffer.sendMessage(Text.literal("★ You found the Impostor! "
						+ "They have no abilities left to strip.").formatted(Formatting.GOLD), false);
			}
		} else {
			sniffer.sendMessage(Text.literal("Not the Impostor.").formatted(Formatting.GRAY), false);
		}
	}

	/** Strips one still-available ability at random. Returns null if none remain. */
	private static Ability removeRandomAbility(GameState state) {
		GameConfig cfg = GameConfig.get();
		List<Ability> available = new ArrayList<>();
		for (Ability a : Ability.values()) {
			if (cfg.isAbilityEnabled(a) && !state.isAbilityRemoved(a)) {
				available.add(a);
			}
		}
		if (available.isEmpty()) {
			return null;
		}
		Ability chosen = available.get(RANDOM.nextInt(available.size()));
		state.removeAbility(chosen);
		return chosen;
	}

	private static String formatSeconds(int ticks) {
		int seconds = Math.max(0, ticks) / 20;
		return seconds >= 60 ? (seconds / 60) + "m " + (seconds % 60) + "s" : seconds + "s";
	}
}
