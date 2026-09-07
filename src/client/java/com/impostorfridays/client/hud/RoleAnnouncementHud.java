package com.impostorfridays.client.hud;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.client.ClientGameState;
import com.impostorfridays.game.Role;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

/**
 * The big centred "you are the Impostor" reveal shown once at {@code /start}.
 *
 * <p>Purely time-based and client-local: it fades itself out and is never re-sent, so it
 * cannot reappear on death or leak into the next match. Shows only this player's own role.
 */
public final class RoleAnnouncementHud {

	private static final long VISIBLE_MS = 4000L;
	private static final long FADE_MS = 1000L;

	private RoleAnnouncementHud() {
	}

	public static void register() {
		HudElementRegistry.addLast(
				Identifier.of(ImpostorFridays.MOD_ID, "role_announcement"), RoleAnnouncementHud::render);
	}

	/** Call when a RoleAnnounceS2C arrives. */
	public static void show(Role role) {
		ClientGameState.announcedRole = role;
		ClientGameState.roleAnnounceUntilMs = System.currentTimeMillis() + VISIBLE_MS;
	}

	private static void render(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tick) {
		Role role = ClientGameState.announcedRole;
		if (role == null) {
			return;
		}
		long remaining = ClientGameState.roleAnnounceUntilMs - System.currentTimeMillis();
		if (remaining <= 0) {
			ClientGameState.announcedRole = null;
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}

		// Fade out over the final second.
		float alpha = remaining >= FADE_MS ? 1.0f : (float) remaining / FADE_MS;
		int alphaBits = ((int) (alpha * 255) & 0xFF) << 24;
		if (alphaBits == 0) {
			return;
		}

		TextRenderer font = client.textRenderer;
		int centreX = ctx.getScaledWindowWidth() / 2;
		int centreY = ctx.getScaledWindowHeight() / 3;

		// Dim the world slightly so the reveal reads clearly.
		ctx.fill(0, 0, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(),
				(int) (alpha * 0x60) << 24);

		ctx.drawCenteredTextWithShadow(font, Text.literal("You are"),
				centreX, centreY - 14, alphaBits | 0xAAAAAA);

		// Draw the role name at 3x using the 2D matrix stack.
		Matrix3x2fStack matrices = ctx.getMatrices();
		matrices.pushMatrix();
		matrices.translate(centreX, centreY);
		matrices.scale(3.0f, 3.0f);
		ctx.drawCenteredTextWithShadow(font, Text.literal(role.getDisplayName().toUpperCase()),
				0, 0, alphaBits | role.getColor());
		matrices.popMatrix();

		ctx.drawCenteredTextWithShadow(font, Text.literal(describe(role)),
				centreX, centreY + 34, alphaBits | 0xCCCCCC);
	}

	private static String describe(Role role) {
		return switch (role) {
			case IMPOSTOR -> "Stop the others finishing the task. Nobody can know.";
			case SNIFFER -> "Find the Impostor. Use /sniff to test a suspect.";
			case INNOCENT -> "Finish the task before time runs out.";
		};
	}
}
