package com.impostorfridays;

import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import java.util.function.Supplier;

/**
 * Self-check that every common/server mixin target actually loads.
 *
 * <p>Mixins apply lazily, when their target class is first loaded, so a broken injection can stay
 * hidden until the exact moment it matters — a player dying, or a respawn request arriving.
 * Force-loading the targets at boot turns that into an immediate, obvious log failure.
 *
 * <p>Runs automatically in development. On a real server, enable it with
 * {@code -Dimpostorfridays.mixinAudit=true}. Worth doing once after any Minecraft or Fabric
 * update, because mixins are remapped when the jar is built — a production failure is possible
 * even when the dev environment is perfectly healthy.
 *
 * <p><b>Targets are class literals, not name strings.</b> In production Minecraft is
 * intermediary-mapped, so {@code Class.forName("net.minecraft.entity.damage.DamageTracker")}
 * throws ClassNotFoundException there no matter how healthy the mixin is. Loom rewrites class
 * literals to the correct intermediary name at build time; it cannot rewrite a string. Each
 * literal sits behind a supplier so it resolves inside the try block rather than during this
 * class's own initialisation.
 */
public final class MixinAudit {

	private static final List<Supplier<Class<?>>> TARGETS = List.of(
			() -> net.minecraft.entity.damage.DamageTracker.class,
			() -> net.minecraft.server.network.ServerPlayNetworkHandler.class
	);

	private MixinAudit() {
	}

	public static void runIfDevelopment() {
		boolean forced = Boolean.getBoolean("impostorfridays.mixinAudit");
		if (!forced && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
			return;
		}
		for (Supplier<Class<?>> target : TARGETS) {
			try {
				Class<?> loaded = target.get();
				// Force full initialisation so any mixin transform has definitely been applied.
				Class.forName(loaded.getName(), true, MixinAudit.class.getClassLoader());
				ImpostorFridays.LOGGER.info("[mixin-audit] OK: {}", loaded.getName());
			} catch (Throwable t) {
				ImpostorFridays.LOGGER.error("[mixin-audit] FAILED — a mixin target could not be "
						+ "loaded, so an injection is probably broken", t);
			}
		}
	}
}
