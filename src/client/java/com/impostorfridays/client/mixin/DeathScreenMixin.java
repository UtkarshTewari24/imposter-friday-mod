package com.impostorfridays.client.mixin;

import com.impostorfridays.client.ClientGameState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Puts the respawn countdown directly on the Respawn button and keeps it disabled until
 * the wait is over — rather than showing the timer somewhere else on screen.
 *
 * <p>The button is identified by its vanilla label during {@code init}, not by list index,
 * so a change in button order cannot silently disable the wrong control.
 */
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {

	@Shadow
	@Final
	private List<ButtonWidget> buttons;

	@Unique
	private ButtonWidget impostorfridays$respawnButton;

	@Unique
	private static final Text RESPAWN_LABEL = Text.translatable("deathScreen.respawn");

	@Inject(method = "init", at = @At("RETURN"))
	private void impostorfridays$findRespawnButton(CallbackInfo ci) {
		impostorfridays$respawnButton = null;
		for (ButtonWidget button : this.buttons) {
			if (button.getMessage().getString().equals(RESPAWN_LABEL.getString())) {
				impostorfridays$respawnButton = button;
				break;
			}
		}
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void impostorfridays$applyCountdown(DrawContext ctx, int mouseX, int mouseY, float delta,
			CallbackInfo ci) {
		ButtonWidget button = impostorfridays$respawnButton;
		if (button == null) {
			return;
		}

		int ticks = ClientGameState.active ? ClientGameState.respawnTicks : 0;
		if (ticks > 0) {
			int seconds = (ticks + 19) / 20; // round up, so it never shows "0" while still locked
			button.active = false;
			button.setMessage(Text.literal("Respawn in " + seconds
					+ (seconds == 1 ? " second" : " seconds")));
		} else {
			button.active = true;
			button.setMessage(RESPAWN_LABEL);
		}
	}
}
