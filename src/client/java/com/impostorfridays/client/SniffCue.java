package com.impostorfridays.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

/**
 * The "you have been sniffed" feedback.
 *
 * <p>Deliberately client-local: sound is played through the local player's own sound engine
 * and the visual is a screen overlay, so nothing is emitted into the world where bystanders
 * could see or hear that a sniff happened.
 */
public final class SniffCue {
	private static final long DURATION_MS = 2500L;

	private SniffCue() {
	}

	public static void trigger() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}
		ClientGameState.sniffCueUntilMs = System.currentTimeMillis() + DURATION_MS;
		// UI-category sound: audible to this player only, never positional in the world.
		client.player.playSound(SoundEvents.BLOCK_SNIFFER_EGG_CRACK, 0.7f, 1.4f);
	}
}
