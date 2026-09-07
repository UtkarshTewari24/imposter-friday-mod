package com.impostorfridays;

import com.impostorfridays.command.ModCommands;
import com.impostorfridays.config.GameConfig;
import com.impostorfridays.game.AbilityManager;
import com.impostorfridays.game.DeathManager;
import com.impostorfridays.game.GameManager;
import com.impostorfridays.game.TrackingManager;
import com.impostorfridays.item.ModItems;
import com.impostorfridays.net.ModNetworking;
import com.impostorfridays.net.ServerNetworkHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
			DeathManager.tick(server);
			AbilityManager.tick(server);
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof net.minecraft.server.network.ServerPlayerEntity player) {
				DeathManager.onDeath(player);
			}
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
				DeathManager.onRespawn(newPlayer));

		// Bring joining players in line with the current match (or clear their HUD if none).
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				GameManager.syncTo(handler.getPlayer()));

		MixinAudit.runIfDevelopment();

		LOGGER.info("Impostor Fridays initialized");
	}
}
