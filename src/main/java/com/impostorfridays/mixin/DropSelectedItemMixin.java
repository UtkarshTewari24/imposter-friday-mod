package com.impostorfridays.mixin;

import com.impostorfridays.game.TrackingManager;
import com.impostorfridays.item.ModItems;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Tracking Compass cannot be dropped.
 *
 * <p>Pressing the drop key while holding it cycles to the next player instead, the way
 * Minecraft Manhunt's tracker behaves. Losing your compass mid-match — by accident or because
 * someone made you drop it — would take you out of the game entirely.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class DropSelectedItemMixin {

	@Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
	private void impostorfridays$cycleInsteadOfDropping(boolean entireStack, CallbackInfo ci) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		if (!player.getMainHandStack().isOf(ModItems.TRACKING_COMPASS)) {
			return;
		}
		ci.cancel();
		TrackingManager.cycleTarget(player);
	}
}
