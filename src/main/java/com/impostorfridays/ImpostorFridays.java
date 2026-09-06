package com.impostorfridays;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (server + client) entrypoint for Impostor Fridays.
 *
 * <p>All authoritative game logic lives on the server side; the client only ever
 * renders what the server tells it. See IMPLEMENTATION_NOTES.md.
 */
public class ImpostorFridays implements ModInitializer {
	public static final String MOD_ID = "impostorfridays";
	public static final Logger LOGGER = LoggerFactory.getLogger("Impostor Fridays");

	@Override
	public void onInitialize() {
		LOGGER.info("Impostor Fridays initializing (common)");
	}
}
