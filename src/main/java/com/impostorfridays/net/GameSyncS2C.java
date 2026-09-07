package com.impostorfridays.net;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Authoritative per-player game state, pushed from the server.
 *
 * <p>The client renders only what arrives here — it never estimates the timer or
 * cooldowns locally, so nothing can drift or desync.
 *
 * @param active         whether a match is running at all
 * @param remainingTicks ticks left in the match
 * @param roleOrdinal    THIS player's role only; never anyone else's
 * @param impostorCooldownTicks shared ability cooldown (0 when not the Impostor)
 * @param snifferCooldownTicks  sniff cooldown (0 when not the Sniffer)
 * @param gravityActive  whether this player's camera should be flipped
 * @param respawnTicks   ticks until this player may respawn (0 when alive)
 * @param taskText       current shared task description
 * @param taskComplete   whether the shared task has been completed
 */
public record GameSyncS2C(
		boolean active,
		int remainingTicks,
		int roleOrdinal,
		int impostorCooldownTicks,
		int snifferCooldownTicks,
		boolean gravityActive,
		int respawnTicks,
		String taskText,
		boolean taskComplete
) implements CustomPayload {

	public static final CustomPayload.Id<GameSyncS2C> ID =
			new CustomPayload.Id<>(Identifier.of(ImpostorFridays.MOD_ID, "game_sync"));

	public static final PacketCodec<RegistryByteBuf, GameSyncS2C> CODEC =
			PacketCodec.of(GameSyncS2C::write, GameSyncS2C::read);

	private void write(RegistryByteBuf buf) {
		buf.writeBoolean(active);
		buf.writeVarInt(remainingTicks);
		buf.writeVarInt(roleOrdinal);
		buf.writeVarInt(impostorCooldownTicks);
		buf.writeVarInt(snifferCooldownTicks);
		buf.writeBoolean(gravityActive);
		buf.writeVarInt(respawnTicks);
		buf.writeString(taskText);
		buf.writeBoolean(taskComplete);
	}

	private static GameSyncS2C read(RegistryByteBuf buf) {
		return new GameSyncS2C(
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readString(),
				buf.readBoolean()
		);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
