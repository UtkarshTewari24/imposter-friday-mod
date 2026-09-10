package com.impostorfridays.task;

import net.minecraft.util.Identifier;

/**
 * One vanilla advancement used by the five-advancement format.
 *
 * <p>Pools are bracketed per difficulty so any random five are of comparable weight — otherwise
 * one draw could be five trivial early-game advancements and the next five genuine expeditions.
 *
 * @param advancementId the vanilla advancement, e.g. {@code minecraft:story/mine_diamond}
 * @param displayName   the in-game title, e.g. "Diamonds!"
 */
public record AdvancementTask(String advancementId, String displayName) {

	public Identifier id() {
		return Identifier.of(advancementId);
	}

	public static AdvancementTask of(String path, String displayName) {
		return new AdvancementTask("minecraft:" + path, displayName);
	}
}
