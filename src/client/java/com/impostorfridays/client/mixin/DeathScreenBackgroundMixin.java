package com.impostorfridays.client.mixin;

import com.impostorfridays.client.ClientGameState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the death screen's view of the world with a flat red field.
 *
 * <p>Being able to watch the scene where you died tells you who was standing there, which ruins
 * a backstab. Painting over it keeps the killer ambiguous, which is the whole point of the
 * anonymised death messages too.
 *
 * <p>Injected after vanilla's own background so it covers the world render, and only while a
 * match is running — outside a game the normal death screen is left alone.
 */
@Mixin(DeathScreen.class)
public abstract class DeathScreenBackgroundMixin {

	/** Opaque dark red. */
	private static final int IMPOSTORFRIDAYS$RED = 0xFF3B0D0D;

	@Inject(method = "renderBackground", at = @At("TAIL"))
	private void impostorfridays$hideTheScene(DrawContext ctx, int mouseX, int mouseY, float delta,
			CallbackInfo ci) {
		if (!ClientGameState.active) {
			return;
		}
		ctx.fill(0, 0, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(),
				IMPOSTORFRIDAYS$RED);
	}
}
