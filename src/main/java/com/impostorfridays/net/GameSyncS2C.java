package com.impostorfridays.net;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

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
 * @param tasks          every objective in the current set, with its completion state
 */
public record GameSyncS2C(
		boolean active,
		int remainingTicks,
		int roleOrdinal,
		int impostorCooldownTicks,
		int snifferCooldownTicks,
		boolean gravityActive,
		int respawnTicks,
		List<TaskLine> tasks
) implements CustomPayload {

	/** One objective as shown on the HUD. */
	public record TaskLine(String text, boolean done) {
	}

	public static final CustomPayload.Id<GameSyncS2C> ID =
			new CustomPayload.Id<>(Identifier.of(ImpostorFridays.MOD_ID, "game_sync"));

	public static final PacketCodec<RegistryByteBuf, GameSyncS2C> CODEC =
			PacketCodec.of(GameSyncS2C::write, GameSyncS2C::read);

	/** The payload sent when no match is running, which clears every overlay. */
	public static GameSyncS2C inactive() {
		return new GameSyncS2C(false, 0, 0, 0, 0, false, 0, List.of());
	}

	private void write(RegistryByteBuf buf) {
		buf.writeBoolean(active);
		buf.writeVarInt(remainingTicks);
		buf.writeVarInt(roleOrdinal);
		buf.writeVarInt(impostorCooldownTicks);
		buf.writeVarInt(snifferCooldownTicks);
		buf.writeBoolean(gravityActive);
		buf.writeVarInt(respawnTicks);
		buf.writeVarInt(tasks.size());
		for (TaskLine line : tasks) {
			buf.writeString(line.text());
			buf.writeBoolean(line.done());
		}
	}

	private static GameSyncS2C read(RegistryByteBuf buf) {
		boolean active = buf.readBoolean();
		int remaining = buf.readVarInt();
		int role = buf.readVarInt();
		int impostorCd = buf.readVarInt();
		int snifferCd = buf.readVarInt();
		boolean gravity = buf.readBoolean();
		int respawn = buf.readVarInt();
		int count = buf.readVarInt();
		List<TaskLine> tasks = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			tasks.add(new TaskLine(buf.readString(), buf.readBoolean()));
		}
		return new GameSyncS2C(active, remaining, role, impostorCd, snifferCd, gravity, respawn, tasks);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
