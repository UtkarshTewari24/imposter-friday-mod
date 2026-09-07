package com.impostorfridays.net;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * One-shot role reveal, sent only at {@code /start}.
 *
 * <p>Deliberately separate from {@link GameSyncS2C} so the announcement fires exactly once
 * and never re-triggers on death or on a reconnect mid-match.
 */
public record RoleAnnounceS2C(int roleOrdinal) implements CustomPayload {

	public static final CustomPayload.Id<RoleAnnounceS2C> ID =
			new CustomPayload.Id<>(Identifier.of(ImpostorFridays.MOD_ID, "role_announce"));

	public static final PacketCodec<RegistryByteBuf, RoleAnnounceS2C> CODEC =
			PacketCodec.of(
					(payload, buf) -> buf.writeVarInt(payload.roleOrdinal()),
					buf -> new RoleAnnounceS2C(buf.readVarInt())
			);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
