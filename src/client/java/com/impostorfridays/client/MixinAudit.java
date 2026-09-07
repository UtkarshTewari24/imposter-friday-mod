package com.impostorfridays.client;

import com.impostorfridays.ImpostorFridays;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import java.util.function.Supplier;

/**
 * Self-check that every client mixin target actually loads.
 *
 * <p>{@code DeathScreen} and {@code Camera} are not touched during a normal startup, so a broken
 * injection would otherwise stay invisible until a player died or {@code /gravity} fired.
 *
 * <p>Runs automatically in development; enable on a real client with
 * {@code -Dimpostorfridays.mixinAudit=true}.
 *
 * <p>See the common {@code MixinAudit} for why these are class literals rather than name strings.
 */
public final class MixinAudit {

	private static final List<Supplier<Class<?>>> TARGETS = List.of(
			() -> net.minecraft.client.gui.screen.DeathScreen.class,
			() -> net.minecraft.client.render.Camera.class
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
				Class.forName(loaded.getName(), true, MixinAudit.class.getClassLoader());
				ImpostorFridays.LOGGER.info("[mixin-audit] OK: {}", loaded.getName());
			} catch (Throwable t) {
				ImpostorFridays.LOGGER.error("[mixin-audit] FAILED — a mixin target could not be "
						+ "loaded, so an injection is probably broken", t);
			}
		}
	}
}
