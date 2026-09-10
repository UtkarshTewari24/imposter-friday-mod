package com.impostorfridays.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/** Registers every custom payload type. Must run on both sides during mod init. */
public final class ModNetworking {
	private ModNetworking() {
	}

	public static void registerCommon() {
		PayloadTypeRegistry.playS2C().register(GameSyncS2C.ID, GameSyncS2C.CODEC);
		PayloadTypeRegistry.playS2C().register(RoleAnnounceS2C.ID, RoleAnnounceS2C.CODEC);
		PayloadTypeRegistry.playS2C().register(SniffedS2C.ID, SniffedS2C.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenPickerS2C.ID, OpenPickerS2C.CODEC);
		PayloadTypeRegistry.playC2S().register(PickerSelectC2S.ID, PickerSelectC2S.CODEC);
		PayloadTypeRegistry.playC2S().register(AbilityUseC2S.ID, AbilityUseC2S.CODEC);
	}
}
