package com.impostorfridays.net;

/** What a player-picker selection should do. The picker UI itself is shared between both. */
public enum PickerMode {
	/** Impostor's Tracking Compass — sets the compass target. */
	TRACK,
	/** Sniffer's {@code /sniff} — attempts to identify the Impostor. */
	SNIFF;

	public static PickerMode byOrdinal(int i) {
		PickerMode[] v = values();
		return (i >= 0 && i < v.length) ? v[i] : TRACK;
	}
}
