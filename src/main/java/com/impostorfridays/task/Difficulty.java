package com.impostorfridays.task;

import net.minecraft.util.Formatting;

import java.util.Locale;

/**
 * The five difficulty tiers.
 *
 * <p>Difficulty is meant to come from Minecraft progression and interesting objectives, never
 * from inflated numbers — "collect 500 cobblestone" is harder than 32 but it is not more fun.
 */
public enum Difficulty {
	BEGINNER("Beginner", Formatting.GREEN, "We can definitely do this."),
	STANDARD("Standard", Formatting.BLUE, "We'll need to split up."),
	ADVANCED("Advanced", Formatting.GOLD, "Okay, we need a real plan."),
	EXPERT("Expert", Formatting.RED, "This is going to take most of the game."),
	MASTER("Master", Formatting.LIGHT_PURPLE, "We need basically everyone contributing efficiently.");

	private final String displayName;
	private final Formatting colour;
	private final String intendedFeeling;

	Difficulty(String displayName, Formatting colour, String intendedFeeling) {
		this.displayName = displayName;
		this.colour = colour;
		this.intendedFeeling = intendedFeeling;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Formatting getColour() {
		return colour;
	}

	/** The feeling this tier is balanced to produce, kept next to the code that uses it. */
	public String getIntendedFeeling() {
		return intendedFeeling;
	}

	public static Difficulty byName(String name) {
		if (name != null) {
			for (Difficulty d : values()) {
				if (d.name().equalsIgnoreCase(name.trim())) {
					return d;
				}
			}
		}
		return STANDARD;
	}

	public String lower() {
		return name().toLowerCase(Locale.ROOT);
	}
}
