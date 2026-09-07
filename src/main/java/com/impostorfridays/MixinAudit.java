package com.impostorfridays;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Development-only self-check for common/server mixins.
 *
 * <p>Mixins apply lazily, when their target class is first loaded, so a broken injection can
 * stay hidden until the exact moment it matters (a player dying, a respawn request arriving).
 * Force-loading the targets at boot turns that into an immediate, obvious dev-log failure.
 *
 * <p>Never runs outside a development environment.
 */
public final class MixinAudit {

	private static final String[] TARGETS = {
			"net.minecraft.entity.damage.DamageTracker",
			"net.minecraft.server.network.ServerPlayNetworkHandler",
	};

	private MixinAudit() {
	}

	public static void runIfDevelopment() {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
			return;
		}
		for (String target : TARGETS) {
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
