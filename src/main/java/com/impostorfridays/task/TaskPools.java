package com.impostorfridays.task;

import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The task catalogue, one pool per difficulty.
 *
 * <p>Design bar, per spec: favour real Minecraft progression over "collect N of item X", and
 * prefer objectives that stay hard even when players already have gear from earlier rounds on
 * the same world. Several tasks deliberately span multiple biomes or dimensions so the group
 * has to split up and regroup — that separation is what creates the suspicion the mod is for.
 *
 * <p>Every advancement id below was checked against the real 1.21.11 server data pack.
 */
public final class TaskPools {

	private static final Map<Difficulty, List<TaskDefinition>> POOLS = new EnumMap<>(Difficulty.class);
	private static final Random RANDOM = new Random();

	private TaskPools() {
	}

	static {
		// ------------------------------------------------------------------
		// EASY — achievable by a group that is still finding its feet
		// ------------------------------------------------------------------
		add(new TaskDefinition("easy_breed_three", Difficulty.EASY,
				"Breed a Cow, a Pig and a Chicken",
				"The farm is thriving!",
				ctx -> ctx.allBred("minecraft:cow", "minecraft:pig", "minecraft:chicken")));

		add(new TaskDefinition("easy_iron_everyone", Difficulty.EASY,
				"Everyone (except the Impostor) wears full iron armour",
				"The whole crew is armoured up!",
				ctx -> ctx.allInnocentsWearing(Items.IRON_HELMET, Items.IRON_CHESTPLATE,
						Items.IRON_LEGGINGS, Items.IRON_BOOTS)));

		add(new TaskDefinition("easy_enchant", Difficulty.EASY,
				"Enchant an item at an enchanting table",
				"The table hums with power!",
				ctx -> ctx.advancementEarned("minecraft:story/enchant_item")));

		add(new TaskDefinition("easy_nether", Difficulty.EASY,
				"Build a portal and enter the Nether",
				"You made it to the Nether!",
				ctx -> ctx.advancementEarned("minecraft:story/enter_the_nether")));

		add(new TaskDefinition("easy_goat_horn", Difficulty.EASY,
				"Obtain a Goat Horn",
				"The horn sounds across the hills!",
				ctx -> ctx.anyPlayerHas(Items.GOAT_HORN)));

		add(new TaskDefinition("easy_tame", Difficulty.EASY,
				"Tame an animal, and get a cat and a wolf into the group",
				"You have companions!",
				ctx -> ctx.advancementEarned("minecraft:husbandry/tame_an_animal")
						&& ctx.anyPlayerHas(Items.BONE)));

		add(new TaskDefinition("easy_lava_bucket", Difficulty.EASY,
				"Fill a bucket with lava and smelt iron",
				"Industry begins!",
				ctx -> ctx.allAdvancementsEarned("minecraft:story/lava_bucket",
						"minecraft:story/smelt_iron")));

		add(new TaskDefinition("easy_sleep_and_bread", Difficulty.EASY,
				"Sleep in a bed and bake bread for the group",
				"Rested and fed!",
				ctx -> ctx.advancementEarned("minecraft:adventure/sleep_in_bed")
						&& ctx.totalCount(Items.BREAD) >= 5));

		// ------------------------------------------------------------------
		// STANDARD — the default 90 minute experience
		// ------------------------------------------------------------------
		add(new TaskDefinition("std_breed_biomes", Difficulty.STANDARD,
				"Breed a Panda, a Mooshroom and a Goat",
				"Three biomes, three families!",
				ctx -> ctx.allBred("minecraft:panda", "minecraft:mooshroom", "minecraft:goat")));

		add(new TaskDefinition("std_diamond_everyone", Difficulty.STANDARD,
				"Everyone (except the Impostor) wears full diamond armour",
				"The crew shines!",
				ctx -> ctx.allInnocentsWearing(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
						Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)));

		add(new TaskDefinition("std_full_shulker", Difficulty.STANDARD,
				"Fill a Shulker Box completely (all 27 slots)",
				"Packed to the brim!",
				TaskContext::anyFullShulkerBox));

		add(new TaskDefinition("std_hunt_three", Difficulty.STANDARD,
				"Hunt an adult Frog, a Polar Bear and a Ravager",
				"Fearless hunters!",
				ctx -> ctx.allMobsKilled("minecraft:frog", "minecraft:polar_bear",
						"minecraft:ravager")));

		add(new TaskDefinition("std_decorated_pot", Difficulty.STANDARD,
				"Craft a Decorated Pot from four pottery sherds",
				"An archaeological triumph!",
				ctx -> ctx.advancementEarned(
						"minecraft:adventure/craft_decorated_pot_using_only_sherds")));

		add(new TaskDefinition("std_music_disc", Difficulty.STANDARD,
				"Get a creeper to drop a music disc, then play it in a jukebox",
				"The music plays!",
				ctx -> ctx.advancementEarned("minecraft:adventure/play_jukebox_in_meadows")
						|| ctx.anyPlayerHas(Items.MUSIC_DISC_13)
						|| ctx.anyPlayerHas(Items.MUSIC_DISC_CAT)));

		add(new TaskDefinition("std_ancient_debris", Difficulty.STANDARD,
				"Find Ancient Debris and brew a potion",
				"Deep delving and dark brewing!",
				ctx -> ctx.allAdvancementsEarned("minecraft:nether/obtain_ancient_debris",
						"minecraft:nether/brew_potion")));

		add(new TaskDefinition("std_die_three_ways", Difficulty.STANDARD,
				"Someone must die to a cactus, to the void, and to the wither effect",
				"A trilogy of terrible decisions!",
				ctx -> ctx.diedOf("minecraft:cactus")
						&& ctx.diedOf("minecraft:out_of_world")
						&& ctx.diedOf("minecraft:wither")));

		add(new TaskDefinition("std_totem", Difficulty.STANDARD,
				"Raid a woodland mansion and obtain a Totem of Undying",
				"Cheating death!",
				ctx -> ctx.advancementEarned("minecraft:adventure/totem_of_undying")
						|| ctx.anyPlayerHas(Items.TOTEM_OF_UNDYING)));

		// ------------------------------------------------------------------
		// HARD — each one a single, well-defined, genuinely difficult objective
		// ------------------------------------------------------------------
		add(new TaskDefinition("hard_beacon", Difficulty.HARD,
				"Activate a Beacon",
				"The beacon pierces the sky!",
				ctx -> ctx.advancementEarned("minecraft:nether/create_beacon")));

		add(new TaskDefinition("hard_full_beacon", Difficulty.HARD,
				"Build a Beacon to full power (all four tiers)",
				"A monument of pure resource!",
				ctx -> ctx.advancementEarned("minecraft:nether/create_full_beacon")));

		add(new TaskDefinition("hard_netherite", Difficulty.HARD,
				"Get one player (not the Impostor) into full netherite armour",
				"Forged in the deep!",
				ctx -> ctx.anyInnocentWearing(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
						Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)));

		add(new TaskDefinition("hard_wither", Difficulty.HARD,
				"Summon and defeat the Wither",
				"The Wither falls!",
				ctx -> ctx.advancementEarned("minecraft:nether/summon_wither")
						&& ctx.mobKilled("minecraft:wither")));

		add(new TaskDefinition("hard_dragon", Difficulty.HARD,
				"Defeat the Ender Dragon",
				"The Dragon is slain!",
				ctx -> ctx.advancementEarned("minecraft:end/kill_dragon")));

		add(new TaskDefinition("hard_elytra", Difficulty.HARD,
				"Find an End City and recover an Elytra",
				"You have wings!",
				ctx -> ctx.advancementEarned("minecraft:end/elytra")
						|| ctx.anyPlayerHas(Items.ELYTRA)));

		add(new TaskDefinition("hard_froglights", Difficulty.HARD,
				"Obtain all three colours of Froglight",
				"A rainbow from the Nether!",
				ctx -> ctx.advancementEarned("minecraft:husbandry/froglights")
						|| (ctx.anyPlayerHas(Items.OCHRE_FROGLIGHT)
								&& ctx.anyPlayerHas(Items.VERDANT_FROGLIGHT)
								&& ctx.anyPlayerHas(Items.PEARLESCENT_FROGLIGHT))));

		add(new TaskDefinition("hard_all_effects", Difficulty.HARD,
				"Have every status effect applied at once to a single player",
				"A walking apothecary!",
				ctx -> ctx.advancementEarned("minecraft:nether/all_effects")));

		add(new TaskDefinition("hard_adventuring_time", Difficulty.HARD,
				"Visit a huge spread of biomes (Adventuring Time)",
				"You have seen the world!",
				ctx -> ctx.advancementEarned("minecraft:adventure/adventuring_time")));
	}

	private static void add(TaskDefinition task) {
		POOLS.computeIfAbsent(task.difficulty(), d -> new ArrayList<>()).add(task);
	}

	public static List<TaskDefinition> pool(Difficulty difficulty) {
		return POOLS.getOrDefault(difficulty, List.of());
	}

	/** Picks a random task from the given tier. */
	public static TaskDefinition random(Difficulty difficulty) {
		List<TaskDefinition> pool = pool(difficulty);
		if (pool.isEmpty()) {
			return null;
		}
		return pool.get(RANDOM.nextInt(pool.size()));
	}

	public static TaskDefinition byId(String id) {
		for (List<TaskDefinition> pool : POOLS.values()) {
			for (TaskDefinition task : pool) {
				if (task.id().equals(id)) {
					return task;
				}
			}
		}
		return null;
	}
}
