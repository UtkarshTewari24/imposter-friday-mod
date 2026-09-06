package com.impostorfridays.client;

import com.impostorfridays.ImpostorFridays;
import net.fabricmc.api.ClientModInitializer;

/** Client entrypoint: HUD rendering, screens, and the camera-flip effect. */
public class ImpostorFridaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ImpostorFridays.LOGGER.info("Impostor Fridays initializing (client)");
	}
}
