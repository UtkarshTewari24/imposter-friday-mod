package com.impostorfridays.game;

/** The three roles a player can hold during a match. */
public enum Role {
	INNOCENT("Innocent", 0x55FF55),
	IMPOSTOR("Impostor", 0xFF5555),
	SNIFFER("Sniffer", 0x55FFFF);

	private final String displayName;
	private final int color;

	Role(String displayName, int color) {
		this.displayName = displayName;
		this.color = color;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** RGB colour used for the role announcement and HUD text. */
	public int getColor() {
		return color;
	}
}
