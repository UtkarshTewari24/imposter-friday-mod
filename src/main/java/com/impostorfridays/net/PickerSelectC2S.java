package com.impostorfridays.net;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * The client's chosen target from the picker.
 *
 * <p>Untrusted: the server re-validates the sender's role, the cooldown, that a game is
 * active, and that the target is a real online player before acting on any of it.
 */
public record PickerSelectC2S(int mode, UUID target, boolean nearest) implements CustomPayload {

	public static final CustomPayload.Id<PickerSelectC2S> ID =
			new CustomPayload.Id<>(Identifier.of(ImpostorFridays.MOD_ID, "picker_select"));

	public static final PacketCodec<RegistryByteBuf, PickerSelectC2S> CODEC =
			PacketCodec.of(
					(payload, buf) -> {
						buf.writeVarInt(payload.mode());
						buf.writeUuid(payload.target());
						buf.writeBoolean(payload.nearest());
					},
					buf -> new PickerSelectC2S(buf.readVarInt(), buf.readUuid(), buf.readBoolean())
			);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
