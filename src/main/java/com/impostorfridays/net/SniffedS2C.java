package com.impostorfridays.net;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Tells a player they have just been sniffed.
 *
 * <p>Sent ONLY to the sniffed player. The cue is rendered client-side rather than as world
 * particles precisely so bystanders cannot see that a sniff happened or who it landed on.
 */
public record SniffedS2C() implements CustomPayload {

	public static final CustomPayload.Id<SniffedS2C> ID =
			new CustomPayload.Id<>(Identifier.of(ImpostorFridays.MOD_ID, "sniffed"));

	public static final PacketCodec<RegistryByteBuf, SniffedS2C> CODEC =
			PacketCodec.unit(new SniffedS2C());

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
