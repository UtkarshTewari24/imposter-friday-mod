package com.impostorfridays;

import com.impostorfridays.command.ModCommands;
import com.impostorfridays.config.GameConfig;
import com.impostorfridays.game.GameManager;
import com.impostorfridays.game.TrackingManager;
import com.impostorfridays.item.ModItems;
import com.impostorfridays.net.ModNetworking;
import com.impostorfridays.net.ServerNetworkHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
		GameConfig.get();
		ModItems.register();
		ModNetworking.registerCommon();
		ServerNetworkHandlers.register();
		ModCommands.register();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			GameManager.tick(server);
			TrackingManager.tick(server);
		});

		// Bring joining players in line with the current match (or clear their HUD if none).
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				GameManager.syncTo(handler.getPlayer()));

		LOGGER.info("Impostor Fridays initialized");
	}
}
