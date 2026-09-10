package com.impostorfridays.game;

import java.util.Locale;

/**
 * The Impostor's abilities.
 *
 * <p>All abilities share a single global cooldown (see {@link GameState}) rather than
 * having one cooldown each — using any ability locks all of them.
 */
public enum Ability {
	STEAL("steal", false),
	SWAP("swap", false),
	BLIND("blind", true),
	GRAVITY("gravity", true),
	HUNT("hunt", true);

	private final String id;
	private final boolean hasDuration;

	Ability(String id, boolean hasDuration) {
		this.id = id;
		this.hasDuration = hasDuration;
	}

	public String getId() {
		return id;
	}

	/** Whether this ability runs for a configurable duration, rather than resolving instantly. */
	public boolean hasDuration() {
		return hasDuration;
	}

	public String getDisplayName() {
		return id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1);
	}

	public static Ability byId(String id) {
		for (Ability a : values()) {
			if (a.id.equalsIgnoreCase(id)) {
				return a;
			}
		}
		return null;
	}
}
