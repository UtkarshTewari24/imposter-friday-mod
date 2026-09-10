package com.impostorfridays.task;

/**
 * Category tags used to keep a generated set of three sub-tasks meaningfully distinct.
 *
 * <p>Without these, three random picks can be "find a Nether fortress", "obtain blaze rods" and
 * "obtain nether wart" — technically three objectives, functionally one expedition. The generator
 * requires three different categories.
 */
public enum TaskCategory {
	RESOURCE,
	CRAFTING,
	FARMING,
	ANIMAL,
	EXPLORATION,
	STRUCTURE,
	COMBAT,
	COLLECTION,
	NETHER,
	END,
	TRADING,
	PROGRESSION
}
