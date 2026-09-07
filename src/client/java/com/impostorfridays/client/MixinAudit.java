package com.impostorfridays.client;

import com.impostorfridays.ImpostorFridays;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Development-only self-check for client mixins.
 *
 * <p>Mixins apply lazily, when their target class is first loaded. Screens like
 * {@code DeathScreen} are not loaded during a normal startup, so a broken injection would
 * stay invisible until a player actually died mid-game. Force-loading the targets at boot
 * turns that into an immediate, obvious failure in the dev log.
 *
 * <p>Never runs outside a development environment.
 */
public final class MixinAudit {

	private static final String[] CLIENT_MIXIN_TARGETS = {
			"net.minecraft.client.gui.screen.DeathScreen",
			"net.minecraft.client.render.Camera",
	};

	private MixinAudit() {
	}

	public static void runIfDevelopment() {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
			return;
		}
		for (String target : CLIENT_MIXIN_TARGETS) {
			try {
				Class.forName(target);
				ImpostorFridays.LOGGER.info("[mixin-audit] OK: {}", target);
			} catch (Throwable t) {
				ImpostorFridays.LOGGER.error("[mixin-audit] FAILED to load {} — a mixin targeting it "
						+ "is probably broken", target, t);
			}
		}
	}
}
