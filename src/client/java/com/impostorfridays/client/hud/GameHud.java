package com.impostorfridays.client.hud;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.client.ClientGameState;
import com.impostorfridays.game.Role;
import com.impostorfridays.net.GameSyncS2C;

import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * All in-world HUD overlays: the match timer, the shared task, and the role-gated
 * cooldown readout.
 *
 * <p>Everything renders from {@link ClientGameState}, so all three vanish the moment the
 * server reports no active game.
 */
public final class GameHud {

	private static final int PANEL_BG = 0x90000000;
	private static final int PANEL_EDGE = 0x40FFFFFF;

	private GameHud() {
	}

	public static void register() {
		HudElementRegistry.addLast(
				Identifier.of(ImpostorFridays.MOD_ID, "timer"), GameHud::renderTimer);
		HudElementRegistry.addLast(
				Identifier.of(ImpostorFridays.MOD_ID, "cooldown"), GameHud::renderCooldown);
	}

	/**
	 * Whether HUD overlays should draw at all right now.
	 *
	 * <p>Deliberately does NOT check for an open screen: the spec requires the timer to be
	 * visible at all times during a match, including while someone is in their inventory or in
	 * the /steal container.
	 */
	private static boolean shouldRender(MinecraftClient client) {
		return ClientGameState.active
				&& client.player != null
				&& !client.options.hudHidden;
	}

	// ------------------------------------------------------------------
	// Timer + task (top centre)
	// ------------------------------------------------------------------

	private static void renderTimer(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tick) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!shouldRender(client)) {
			return;
		}
		TextRenderer font = client.textRenderer;
		int screenWidth = ctx.getScaledWindowWidth();

		String time = ClientGameState.formatTicks(ClientGameState.remainingTicks);
		// Turn red for the last five minutes.
		Formatting colour = ClientGameState.remainingTicks <= 5 * 60 * 20
				? Formatting.RED : Formatting.WHITE;
		Text timeText = Text.literal(time).formatted(colour, Formatting.BOLD);

		int textWidth = font.getWidth(timeText);
		int boxWidth = textWidth + 16;
		int left = (screenWidth - boxWidth) / 2;

		panel(ctx, left, 2, left + boxWidth, 16);
		ctx.drawCenteredTextWithShadow(font, timeText, screenWidth / 2, 6, 0xFFFFFFFF);

		renderTasks(ctx, font, screenWidth);
	}

	/**
	 * The objective list under the timer.
	 *
	 * <p>Completed objectives stay on screen struck through rather than disappearing, so the
	 * group can see how far through a set they are at a glance.
	 */
	private static void renderTasks(DrawContext ctx, TextRenderer font, int screenWidth) {
		List<GameSyncS2C.TaskLine> tasks = ClientGameState.tasks;
		if (tasks.isEmpty()) {
			return;
		}

		int done = 0;
		for (GameSyncS2C.TaskLine line : tasks) {
			if (line.done()) {
				done++;
			}
		}

		// Build the display lines first so the panel can be sized to the widest one.
		List<Text> lines = new java.util.ArrayList<>();
		if (tasks.size() > 1) {
			lines.add(Text.literal("Objectives  " + done + "/" + tasks.size())
					.formatted(Formatting.GOLD, Formatting.BOLD));
		}
		for (GameSyncS2C.TaskLine line : tasks) {
			lines.add(line.done()
					? Text.literal("✔ " + line.text()).formatted(Formatting.GREEN, Formatting.STRIKETHROUGH)
					: Text.literal("• " + line.text()).formatted(Formatting.YELLOW));
		}

		int widest = 0;
		for (Text line : lines) {
			widest = Math.max(widest, font.getWidth(line));
		}

		int top = 18;
		int lineHeight = 11;
		int boxLeft = (screenWidth - widest - 12) / 2;
		panel(ctx, boxLeft, top, boxLeft + widest + 12, top + lines.size() * lineHeight + 5);

		int y = top + 4;
		for (Text line : lines) {
			ctx.drawCenteredTextWithShadow(font, line, screenWidth / 2, y, 0xFFFFFFFF);
			y += lineHeight;
		}
	}

	// ------------------------------------------------------------------
	// Cooldown (right edge) — only the Impostor and the Sniffer ever see this
	// ------------------------------------------------------------------

	private static void renderCooldown(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tick) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!shouldRender(client)) {
			return;
		}

		Role role = ClientGameState.role;
		int ticks;
		String label;
		if (role == Role.IMPOSTOR) {
			ticks = ClientGameState.impostorCooldownTicks;
			label = "Ability";
		} else if (role == Role.SNIFFER) {
			ticks = ClientGameState.snifferCooldownTicks;
			label = "Sniff";
		} else {
			// Innocents must never see a cooldown readout.
			return;
		}

		TextRenderer font = client.textRenderer;
		Text text = ticks > 0
				? Text.literal(label + " Cooldown: " + ClientGameState.formatTicks(ticks))
						.formatted(Formatting.GRAY)
				: Text.literal(label + " Ready").formatted(Formatting.GREEN, Formatting.BOLD);

		int width = font.getWidth(text);
		int right = ctx.getScaledWindowWidth() - 4;
		int left = right - width - 10;
		int top = ctx.getScaledWindowHeight() / 2 - 6;

		panel(ctx, left, top, right, top + 14);
		ctx.drawTextWithShadow(font, text, left + 5, top + 4, 0xFFFFFFFF);
	}

	/** A dark rounded-looking panel with a subtle 1px edge, matching vanilla tooltips. */
	private static void panel(DrawContext ctx, int x1, int y1, int x2, int y2) {
		ctx.fill(x1, y1, x2, y2, PANEL_BG);
		ctx.fill(x1, y1, x2, y1 + 1, PANEL_EDGE);
		ctx.fill(x1, y2 - 1, x2, y2, PANEL_EDGE);
		ctx.fill(x1, y1, x1 + 1, y2, PANEL_EDGE);
		ctx.fill(x2 - 1, y1, x2, y2, PANEL_EDGE);
	}
}
