package com.impostorfridays.mixin;

import com.impostorfridays.game.DeathManager;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enforces the respawn delay server-side, so a modified client cannot skip it. */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onClientStatus", at = @At("HEAD"), cancellable = true)
	private void impostorfridays$holdRespawn(ClientStatusC2SPacket packet, CallbackInfo ci) {
		if (packet.getMode() != ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) {
			return;
		}
		if (!DeathManager.canRespawn(this.player)) {
			ci.cancel();
		}
	}
}
