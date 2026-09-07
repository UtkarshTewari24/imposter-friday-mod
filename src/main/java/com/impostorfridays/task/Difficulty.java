package com.impostorfridays.task;

import java.util.Locale;

/** Task difficulty tier, chosen in {@code /amongussetup}. */
public enum Difficulty {
	EASY,
	STANDARD,
	HARD;

	public String getDisplayName() {
		String n = name().toLowerCase(Locale.ROOT);
		return n.substring(0, 1).toUpperCase(Locale.ROOT) + n.substring(1);
	}

	public static Difficulty byName(String name) {
		for (Difficulty d : values()) {
			if (d.name().equalsIgnoreCase(name)) {
				return d;
			}
		}
		return STANDARD;
	}
}
