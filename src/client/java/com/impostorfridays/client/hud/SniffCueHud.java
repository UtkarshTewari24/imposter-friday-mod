package com.impostorfridays.client.hud;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.client.ClientGameState;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * The "you have been sniffed" visual.
 *
 * <p>An original, lightweight effect — a green pulse around the edges of the screen plus a
 * short banner — rather than anything reusing Mojang's sniffer mob assets.
 *
 * <p>Critically this is a SCREEN overlay drawn only for the sniffed player, not a world
 * particle. A bystander standing next to them sees and hears nothing, so a sniff can never
 * leak who was tested or that one happened at all.
 */
public final class SniffCueHud {

	private static final long DURATION_MS = 2500L;
	private static final int PULSE_COLOUR = 0x33D17A;

	private SniffCueHud() {
	}

	public static void register() {
		HudElementRegistry.addLast(
				Identifier.of(ImpostorFridays.MOD_ID, "sniff_cue"), SniffCueHud::render);
	}

	private static void render(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tick) {
		long remaining = ClientGameState.sniffCueUntilMs - System.currentTimeMillis();
		if (remaining <= 0) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}

		float progress = 1.0f - ((float) remaining / DURATION_MS);
		// Two quick pulses, fading out overall.
		float pulse = (float) Math.abs(Math.sin(progress * Math.PI * 2.0));
		float fade = 1.0f - progress;
		int alpha = (int) (pulse * fade * 140.0f) & 0xFF;
		if (alpha <= 0) {
			return;
		}

		int width = ctx.getScaledWindowWidth();
		int height = ctx.getScaledWindowHeight();
		int colour = (alpha << 24) | PULSE_COLOUR;
		int band = Math.max(6, height / 14);

		// Edge vignette rather than a full-screen tint, so it never blocks the player's view.
		ctx.fill(0, 0, width, band, colour);
		ctx.fill(0, height - band, width, height, colour);
		ctx.fill(0, band, band, height - band, colour);
		ctx.fill(width - band, band, width, height - band, colour);

		int textAlpha = (int) (fade * 255.0f) & 0xFF;
		if (textAlpha > 8) {
			ctx.drawCenteredTextWithShadow(client.textRenderer,
					Text.literal("✷ You have been sniffed ✷").formatted(Formatting.GREEN),
					width / 2, height / 4, (textAlpha << 24) | 0xFFFFFF);
		}
	}
}
