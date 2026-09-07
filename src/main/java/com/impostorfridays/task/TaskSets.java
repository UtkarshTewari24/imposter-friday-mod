package com.impostorfridays.task;

import java.util.List;

/**
 * The preset task sets, selectable in {@code /amongussetup}.
 *
 * <p><b>Balance intent.</b> Each set is three objectives sized so a group of 4-8 can finish
 * inside a 90 minute match while an Impostor is actively disrupting them — which is the balance
 * that matters, because a set nobody can finish is an automatic Impostor win and a set finished
 * in twenty minutes gives the Impostor no room to work.
 *
 * <p>Every set follows the same shape:
 * <ul>
 *   <li><b>One anchor</b> — the hard objective that decides the match</li>
 *   <li><b>One spread</b> — something needing two or three different biomes or dimensions, so
 *       the group must split up and people end up alone with each other</li>
 *   <li><b>One light</b> — a quick win, so a set never feels hopeless after a bad start</li>
 * </ul>
 *
 * <p>Sets deliberately avoid stacking two boss-tier objectives (Wither plus Dragon), which is
 * unfinishable in the time, and avoid three tasks in the same place, which removes the splitting
 * up that makes the game work.
 */
public final class TaskSets {

	/** Config value meaning "draw one random task from the difficulty pool" — the old behaviour. */
	public static final String RANDOM = "RANDOM";

	private static final List<TaskSet> SETS = List.of(
			new TaskSet("set1", "Set 1 — First Light",
					"Gentle. Good for a group's first game.", 50,
					List.of(
							"easy_breed_three",     // light   — the farm, close to home
							"easy_iron_everyone",   // spread  — everyone must gear up together
							"easy_goat_horn"        // anchor  — someone has to go to the mountains
					)),

			new TaskSet("set2", "Set 2 — Groundwork",
					"The standard game. Sends people in three directions.", 70,
					List.of(
							"easy_enchant",         // light   — home base tech
							"std_breed_biomes",     // spread  — three biomes, big split
							"std_blaze_rods"        // anchor  — a Nether fortress run
					)),

			new TaskSet("set3", "Set 3 — Deep Cuts",
					"Heavier. Mining, a village project, and a bit of luck.", 80,
					List.of(
							"std_music_disc",       // light   — a creeper and a skeleton
							"std_diamond_everyone", // spread  — the whole crew underground
							"std_cure_villager"     // anchor  — a real multi-step project
					)),

			new TaskSet("set4", "Set 4 — The Long Haul",
					"Hard. Built around one big objective.", 90,
					List.of(
							"std_music_disc",       // light
							"std_breed_biomes",     // spread
							"hard_beacon"           // anchor — the Wither, then the beacon
					)),

			new TaskSet("set5", "Set 5 — Endgame",
					"Very hard. Only try this with a group that knows what it is doing.", 90,
					List.of(
							"easy_iron_everyone",   // light   — gear up before the run
							"std_blaze_rods",       // spread  — and the rods feed the anchor
							"hard_dragon"           // anchor  — the Ender Dragon
					)),

			new TaskSet("set6", "Set 6 — Scattered",
					"Maximum splitting up. Nobody stays together for long.", 75,
					List.of(
							"easy_enchant",         // light   — the one reason to come home
							"std_breed_biomes",     // spread  — three biomes
							"std_hunt_three"        // anchor  — three more biomes, plus a raid
					))
	);

	private TaskSets() {
	}

	public static List<TaskSet> all() {
		return SETS;
	}

	public static TaskSet byId(String id) {
		for (TaskSet set : SETS) {
			if (set.id().equalsIgnoreCase(id)) {
				return set;
			}
		}
		return null;
	}

	/** True when the config value means "use the difficulty pool" rather than a named set. */
	public static boolean isRandom(String value) {
		return value == null || value.isBlank() || RANDOM.equalsIgnoreCase(value);
	}
}
