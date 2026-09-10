package com.impostorfridays.net;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A request to use an ability, sent when a player presses one of the optional keybinds.
 *
 * <p>Carries only the ability name. The server re-runs every check it would run for the
 * equivalent command, so a keybind can never do anything typing the command could not.
 */
public record AbilityUseC2S(String abilityId) implements CustomPayload {

	public static final CustomPayload.Id<AbilityUseC2S> ID =
			new CustomPayload.Id<>(Identifier.of(ImpostorFridays.MOD_ID, "ability_use"));

	public static final PacketCodec<RegistryByteBuf, AbilityUseC2S> CODEC =
			PacketCodec.of(
					(payload, buf) -> buf.writeString(payload.abilityId()),
					buf -> new AbilityUseC2S(buf.readString()));

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
