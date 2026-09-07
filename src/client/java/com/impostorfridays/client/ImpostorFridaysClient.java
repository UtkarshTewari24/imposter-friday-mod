package com.impostorfridays.client;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.client.hud.GameHud;
import com.impostorfridays.client.hud.RoleAnnouncementHud;
import com.impostorfridays.client.screen.PlayerPickerScreen;
import com.impostorfridays.game.Role;
import com.impostorfridays.net.GameSyncS2C;
import com.impostorfridays.net.OpenPickerS2C;
import com.impostorfridays.net.PickerMode;
import com.impostorfridays.net.RoleAnnounceS2C;
import com.impostorfridays.net.SniffedS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client entrypoint: HUD rendering, screens, and the camera-flip effect. */
public class ImpostorFridaysClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		GameHud.register();
		RoleAnnouncementHud.register();

		ClientPlayNetworking.registerGlobalReceiver(GameSyncS2C.ID, (payload, context) ->
				context.client().execute(() -> applySync(payload)));

		ClientPlayNetworking.registerGlobalReceiver(RoleAnnounceS2C.ID, (payload, context) ->
				context.client().execute(() ->
						RoleAnnouncementHud.show(roleOf(payload.roleOrdinal()))));

		ClientPlayNetworking.registerGlobalReceiver(SniffedS2C.ID, (payload, context) ->
				context.client().execute(SniffCue::trigger));

		ClientPlayNetworking.registerGlobalReceiver(OpenPickerS2C.ID, (payload, context) ->
				context.client().execute(() -> context.client().setScreen(
						new PlayerPickerScreen(PickerMode.byOrdinal(payload.mode()), payload.entries()))));

		// Never let stale overlays survive a disconnect.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientGameState.reset());

		MixinAudit.runIfDevelopment();

		ImpostorFridays.LOGGER.info("Impostor Fridays client initialized");
	}

	private static void applySync(GameSyncS2C payload) {
		ClientGameState.active = payload.active();
		ClientGameState.remainingTicks = payload.remainingTicks();
		ClientGameState.role = roleOf(payload.roleOrdinal());
		ClientGameState.impostorCooldownTicks = payload.impostorCooldownTicks();
		ClientGameState.snifferCooldownTicks = payload.snifferCooldownTicks();
		ClientGameState.gravityActive = payload.gravityActive();
		ClientGameState.respawnTicks = payload.respawnTicks();
		ClientGameState.taskText = payload.taskText();
		ClientGameState.taskComplete = payload.taskComplete();

		if (!payload.active()) {
			// Clear one-shot overlays too, so /end wipes the screen completely.
			ClientGameState.announcedRole = null;
			ClientGameState.roleAnnounceUntilMs = 0;
		}
	}

	private static Role roleOf(int ordinal) {
		Role[] values = Role.values();
		return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : Role.INNOCENT;
	}
}
