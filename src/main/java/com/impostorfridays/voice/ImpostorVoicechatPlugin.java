package com.impostorfridays.voice;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.config.GameConfig;
import com.impostorfridays.game.GameManager;
import com.impostorfridays.game.GameState;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;

import java.util.UUID;

/**
 * Simple Voice Chat integration.
 *
 * <p>Registered through the {@code voicechat} Fabric entrypoint, which Simple Voice Chat
 * resolves via {@code FabricLoader.getEntrypointContainers("voicechat", VoicechatPlugin.class)}.
 * Because nothing else asks for that entrypoint, this class is never loaded when SVC is
 * absent — which is what makes the dependency genuinely soft.
 *
 * <p>The only thing this changes is who can hear whom while dead, and only when the
 * {@code voicechat.muteDead} setting is on. Normal proximity chat is left completely alone,
 * so invisibility, gravity and swaps all behave as usual.
 */
public class ImpostorVoicechatPlugin implements VoicechatPlugin {

	@Override
	public String getPluginId() {
		return ImpostorFridays.MOD_ID;
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(LocationalSoundPacketEvent.class, this::onSoundPacket);
		registration.registerEvent(EntitySoundPacketEvent.class, this::onSoundPacket);
		ImpostorFridays.LOGGER.info("Simple Voice Chat integration active");
	}

	/**
	 * Fires once per receiving player, so the living and the dead can be separated without
	 * muting anybody globally.
	 */
	private void onSoundPacket(SoundPacketEvent<?> event) {
		if (!GameConfig.get().isMuteDeadPlayers()) {
			return;
		}
		GameState state = GameManager.getState();
		if (state == null) {
			return;
		}

		VoicechatConnection sender = event.getSenderConnection();
		VoicechatConnection receiver = event.getReceiverConnection();
		if (sender == null || receiver == null) {
			return;
		}

		UUID senderId = sender.getPlayer().getUuid();
		UUID receiverId = receiver.getPlayer().getUuid();

		// Dead players and living players form two separate audio worlds.
		if (state.isDead(senderId) != state.isDead(receiverId) && event.isCancellable()) {
			event.cancel();
		}
	}
}
