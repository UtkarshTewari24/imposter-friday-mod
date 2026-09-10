package com.impostorfridays.task.pool;

import com.impostorfridays.task.Difficulty;
import com.impostorfridays.task.TaskCategory;
import com.impostorfridays.task.TaskContext;
import com.impostorfridays.task.TaskDefinition;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

import static com.impostorfridays.task.TaskDefinition.major;
import static com.impostorfridays.task.TaskDefinition.sub;

/** Advanced pools — "okay, we need a real plan". Dividing work intelligently should pay off. */
public final class AdvancedTasks {

	private AdvancedTasks() {
	}

	private static final Difficulty D = Difficulty.ADVANCED;

	public static List<TaskDefinition> subTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		// --- High value resources ---
		t.add(sub("ADV_SUB_DIAMOND_64", D, TaskCategory.RESOURCE)
				.desc("Obtain 64 diamonds").mins(25, 40)
				.check(c -> c.gained(Items.DIAMOND, 64)).build());
		t.add(sub("ADV_SUB_EMERALD_32", D, TaskCategory.TRADING)
				.desc("Obtain 32 emeralds").mins(22, 38)
				.check(c -> c.gained(Items.EMERALD, 32)).build());
		t.add(sub("ADV_SUB_GOLD_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 gold ingots").mins(18, 30)
				.check(c -> c.gained(Items.GOLD_INGOT, 32)).build());
		t.add(sub("ADV_SUB_DEBRIS_8", D, TaskCategory.NETHER)
				.desc("Obtain 8 ancient debris").mins(30, 50).nether()
				.check(c -> c.gained(Items.ANCIENT_DEBRIS, 8)).build());
		t.add(sub("ADV_SUB_QUARTZ_32", D, TaskCategory.NETHER)
				.desc("Obtain 32 quartz blocks").mins(25, 40).nether()
				.check(c -> c.gained(Items.QUARTZ_BLOCK, 32)).build());
		t.add(sub("ADV_SUB_AMETHYST_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 amethyst shards").mins(22, 38)
				.check(c -> c.gained(Items.AMETHYST_SHARD, 32)).build());
		t.add(sub("ADV_SUB_SLIME_32", D, TaskCategory.COMBAT)
				.desc("Obtain 32 slimeballs").mins(25, 40)
				.check(c -> c.gained(Items.SLIME_BALL, 32)).build());
		t.add(sub("ADV_SUB_BLAZE_32", D, TaskCategory.NETHER)
				.desc("Obtain 32 blaze rods").mins(28, 45).nether()
				.check(c -> c.gained(Items.BLAZE_ROD, 32)).build());
		t.add(sub("ADV_SUB_PEARLS_24", D, TaskCategory.COMBAT)
				.desc("Obtain 24 ender pearls").mins(25, 40)
				.check(c -> c.gained(Items.ENDER_PEARL, 24)).build());
		t.add(sub("ADV_SUB_OBSIDIAN_16", D, TaskCategory.RESOURCE)
				.desc("Obtain 16 obsidian").mins(18, 30)
				.check(c -> c.gained(Items.OBSIDIAN, 16)).build());

		// --- Exploration ---
		t.add(sub("ADV_SUB_ANCIENT_CITY", D, TaskCategory.STRUCTURE)
				.desc("Locate an ancient city").mins(25, 45)
				.check(c -> c.visitedStructure("minecraft:ancient_city")).build());
		t.add(sub("ADV_SUB_MANSION", D, TaskCategory.STRUCTURE)
				.desc("Locate a woodland mansion").mins(30, 50)
				.check(c -> c.visitedStructure("minecraft:mansion")).build());
		t.add(sub("ADV_SUB_MONUMENT", D, TaskCategory.STRUCTURE)
				.desc("Locate an ocean monument").mins(25, 42)
				.check(c -> c.visitedStructure("minecraft:monument")).build());
		t.add(sub("ADV_SUB_BASTION", D, TaskCategory.NETHER)
				.desc("Locate a bastion remnant").mins(22, 38).nether()
				.check(c -> c.visitedStructure("minecraft:bastion_remnant")).build());
		t.add(sub("ADV_SUB_TRIAL", D, TaskCategory.STRUCTURE)
				.desc("Locate a trial chamber").mins(22, 38)
				.check(c -> c.visitedStructure("minecraft:trial_chambers")).build());
		t.add(sub("ADV_SUB_FORTRESS", D, TaskCategory.NETHER)
				.desc("Locate a Nether fortress").mins(20, 35).nether()
				.check(c -> c.visitedStructure("minecraft:fortress")).build());
		t.add(sub("ADV_SUB_STRONGHOLD", D, TaskCategory.STRUCTURE)
				.desc("Locate a stronghold").mins(25, 45)
				.check(c -> c.visitedStructure("minecraft:stronghold")).build());
		t.add(sub("ADV_SUB_STRUCTURES_4", D, TaskCategory.EXPLORATION)
				.desc("Visit 4 different generated structures").mins(28, 45)
				.check(c -> c.distinctStructuresVisited() >= 4).build());
		t.add(sub("ADV_SUB_BIOMES_6", D, TaskCategory.EXPLORATION)
				.desc("Visit 6 different biomes").mins(20, 35)
				.check(c -> c.distinctBiomesVisited() >= 6).build());
		t.add(sub("ADV_SUB_ECHO_SHARD", D, TaskCategory.STRUCTURE)
				.desc("Retrieve an echo shard from an ancient city").mins(28, 48)
				.check(c -> c.gained(Items.ECHO_SHARD)).build());

		// --- Animals ---
		t.add(sub("ADV_SUB_BREED_THREE_BIOMES", D, TaskCategory.ANIMAL)
				.desc("Breed a panda, a mooshroom and a goat").mins(30, 50)
				.check(c -> c.allBred("minecraft:panda", "minecraft:mooshroom", "minecraft:goat")).build());
		t.add(sub("ADV_SUB_TADPOLE", D, TaskCategory.ANIMAL)
				.desc("Capture a tadpole in a bucket").mins(18, 32)
				.check(c -> c.gained(Items.TADPOLE_BUCKET)).build());
		t.add(sub("ADV_SUB_RABBIT_LEATHER", D, TaskCategory.ANIMAL)
				.desc("Obtain a rabbit's foot and 16 leather").mins(20, 35)
				.check(c -> c.gained(Items.RABBIT_FOOT) && c.gained(Items.LEATHER, 16)).build());
		t.add(sub("ADV_SUB_TURTLE_SCUTE", D, TaskCategory.ANIMAL)
				.desc("Obtain 5 turtle scutes").mins(25, 45)
				.check(c -> c.gained(Items.TURTLE_SCUTE, 5)).build());
		t.add(sub("ADV_SUB_BREED_FIVE", D, TaskCategory.ANIMAL)
				.desc("Breed five different animal species").mins(25, 42)
				.check(c -> c.distinctSpeciesBred() >= 5).build());
		t.add(sub("ADV_SUB_GOAT_HORN", D, TaskCategory.ANIMAL)
				.desc("Obtain a goat horn from a naturally occurring goat").mins(15, 28)
				.check(c -> c.gained(Items.GOAT_HORN)).build());
		t.add(sub("ADV_SUB_HONEY", D, TaskCategory.FARMING)
				.desc("Obtain honeycomb and a honey bottle").mins(15, 28)
				.check(c -> c.gained(Items.HONEYCOMB) && c.gained(Items.HONEY_BOTTLE)).build());
		t.add(sub("ADV_SUB_THREE_BUCKETS", D, TaskCategory.COLLECTION)
				.desc("Capture three different aquatic mobs in buckets").mins(22, 38)
				.check(c -> {
					int n = 0;
					for (var i : new net.minecraft.item.Item[]{Items.COD_BUCKET, Items.SALMON_BUCKET,
							Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET,
							Items.AXOLOTL_BUCKET, Items.TADPOLE_BUCKET}) {
						if (c.gained(i)) {
							n++;
						}
					}
					return n >= 3;
				}).build());
		t.add(sub("ADV_SUB_SNIFFER_EGG", D, TaskCategory.ANIMAL)
				.desc("Obtain a sniffer egg").mins(28, 48)
				.check(c -> c.gained(Items.SNIFFER_EGG)).build());
		t.add(sub("ADV_SUB_AXOLOTL", D, TaskCategory.ANIMAL)
				.desc("Capture an axolotl in a bucket").mins(20, 35)
				.check(c -> c.gained(Items.AXOLOTL_BUCKET)).build());

		// --- Combat ---
		t.add(sub("ADV_SUB_RAVAGER", D, TaskCategory.COMBAT)
				.desc("Kill a Ravager").mins(18, 32)
				.check(c -> c.killed("minecraft:ravager")).build());
		t.add(sub("ADV_SUB_ELDER_GUARDIAN", D, TaskCategory.COMBAT)
				.desc("Kill an Elder Guardian").mins(28, 48)
				.check(c -> c.killed("minecraft:elder_guardian")).build());
		t.add(sub("ADV_SUB_WARDEN", D, TaskCategory.COMBAT)
				.desc("Kill a Warden").mins(35, 60)
				.check(c -> c.killed("minecraft:warden")).build());
		t.add(sub("ADV_SUB_BLAZE_10", D, TaskCategory.NETHER)
				.desc("Kill 10 Blazes").mins(22, 38).nether()
				.check(c -> c.killedAtLeast("minecraft:blaze", 10)).build());
		t.add(sub("ADV_SUB_FIVE_TYPES", D, TaskCategory.COMBAT)
				.desc("Kill five different hostile mob types").mins(15, 28)
				.check(c -> c.distinctMobTypesKilled() >= 5).build());
		t.add(sub("ADV_SUB_RAID", D, TaskCategory.COMBAT)
				.desc("Win a raid").mins(28, 48)
				.check(c -> c.advancementEarned("minecraft:adventure/hero_of_the_village")).build());
		t.add(sub("ADV_SUB_TRIDENT_KILL", D, TaskCategory.COMBAT)
				.desc("Kill a mob with a trident").mins(25, 45)
				.check(c -> c.killedWith("minecraft:trident")).build());
		t.add(sub("ADV_SUB_SNIPER", D, TaskCategory.COMBAT)
				.desc("Kill a skeleton with a bow from at least 50 blocks away").mins(15, 28)
				.check(c -> c.advancementEarned("minecraft:adventure/sniper_duel")).build());
		t.add(sub("ADV_SUB_FOUR_MOBS", D, TaskCategory.COMBAT)
				.desc("Kill a skeleton, creeper, zombie and spider without dying").mins(15, 28)
				.check(c -> c.allKilled("minecraft:skeleton", "minecraft:creeper",
						"minecraft:zombie", "minecraft:spider") && c.bestKillStreak() >= 4).build());
		t.add(sub("ADV_SUB_TRIAL_ENCOUNTER", D, TaskCategory.STRUCTURE)
				.desc("Obtain a trial key from a trial chamber").mins(25, 45)
				.check(c -> c.gainedAnyOf(1, Items.TRIAL_KEY, Items.OMINOUS_TRIAL_KEY)).build());

		return t;
	}

	public static List<TaskDefinition> majorTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		t.add(major("ADV_MAJ_BEACON", D, TaskCategory.PROGRESSION)
				.desc("Activate a beacon").mins(45, 75).nether()
				.check(c -> c.advancementEarned("minecraft:nether/create_beacon")).build());
		t.add(major("ADV_MAJ_DIAMOND_EVERYONE", D, TaskCategory.PROGRESSION)
				.desc("Everyone obtains full diamond armour").mins(45, 75).group()
				.check(c -> c.allInnocentsWearing(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
						Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)).build());
		t.add(major("ADV_MAJ_NETHERITE_ONE", D, TaskCategory.NETHER)
				.desc("One player obtains full netherite armour").mins(55, 85).nether().group()
				.check(c -> c.innocentsWearing(1, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
						Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)).build());
		t.add(major("ADV_MAJ_SHULKER", D, TaskCategory.END)
				.desc("Obtain a shulker box and fill all 27 slots").mins(50, 80).end()
				.check(TaskContext::anyFullShulkerBox).build());
		t.add(major("ADV_MAJ_DIAMOND_128", D, TaskCategory.RESOURCE)
				.desc("Obtain 128 diamonds").mins(50, 80)
				.check(c -> c.gained(Items.DIAMOND, 128)).build());
		t.add(major("ADV_MAJ_RAID_TRADES", D, TaskCategory.TRADING)
				.desc("Defeat a raid, then complete 10 villager trades").mins(45, 75)
				.check(c -> c.advancementEarned("minecraft:adventure/hero_of_the_village")
						&& c.getVillagerTrades() >= 10).build());
		t.add(major("ADV_MAJ_HORN_DISC5_POT", D, TaskCategory.COLLECTION)
				.desc("Obtain a goat horn, Music Disc 5 and a decorated pot").mins(50, 80)
				.check(c -> c.gained(Items.GOAT_HORN) && c.gained(Items.MUSIC_DISC_5)
						&& c.gained(Items.DECORATED_POT)).build());
		t.add(major("ADV_MAJ_EMERALD_64_TRADE", D, TaskCategory.TRADING)
				.desc("Obtain 64 emeralds through villager trading").mins(45, 75)
				.check(c -> c.gained(Items.EMERALD, 64) && c.getVillagerTrades() >= 15).build());
		t.add(major("ADV_MAJ_ENCHANT_PROGRESSION", D, TaskCategory.PROGRESSION)
				.desc("Build an enchanting table with 15 bookshelves and enchant 5 diamond items")
				.mins(45, 75)
				.check(c -> c.gained(Items.BOOKSHELF, 15)
						&& c.advancementEarned("minecraft:story/enchant_item")
						&& c.gained(Items.DIAMOND, 25)).build());
		t.add(major("ADV_MAJ_SPONGE", D, TaskCategory.COMBAT)
				.desc("Defeat an Elder Guardian and obtain a sponge").mins(45, 75)
				.check(c -> c.killed("minecraft:elder_guardian")
						&& c.gainedAnyOf(1, Items.SPONGE, Items.WET_SPONGE)).build());
		t.add(major("ADV_MAJ_ANCIENT_CITY_LOOT", D, TaskCategory.STRUCTURE)
				.desc("Enter an ancient city and retrieve 3 different loot items").mins(45, 75)
				.check(c -> c.visitedStructure("minecraft:ancient_city")
						&& countAncientCityLoot(c) >= 3).build());
		t.add(major("ADV_MAJ_POTIONS", D, TaskCategory.NETHER)
				.desc("Brew potions and obtain 6 different potion ingredients").mins(45, 75).nether()
				.check(c -> c.advancementEarned("minecraft:nether/brew_potion")
						&& countPotionIngredients(c) >= 6).build());
		t.add(major("ADV_MAJ_TWO_DIAMOND_SETS", D, TaskCategory.PROGRESSION)
				.desc("Two players obtain full diamond armour").mins(45, 75).group()
				.check(c -> c.innocentsWearing(2, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
						Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)).build());
		t.add(major("ADV_MAJ_DISCS_8", D, TaskCategory.COLLECTION)
				.desc("Obtain 8 different music discs").mins(55, 85)
				.check(c -> c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 8).build());
		t.add(major("ADV_MAJ_END_PORTAL", D, TaskCategory.END)
				.desc("Obtain 12 Eyes of Ender and activate the End portal").mins(50, 80).nether()
				.check(c -> c.gained(Items.ENDER_EYE, 12)
						&& c.advancementEarned("minecraft:story/enter_the_end")).build());

		return t;
	}

	static int countAncientCityLoot(TaskContext c) {
		int n = 0;
		for (var i : new net.minecraft.item.Item[]{Items.ECHO_SHARD, Items.DISC_FRAGMENT_5,
				Items.SCULK_CATALYST, Items.SCULK_SENSOR, Items.SCULK_SHRIEKER,
				Items.SOUL_TORCH, Items.CANDLE}) {
			if (c.gained(i)) {
				n++;
			}
		}
		return n;
	}

	static int countPotionIngredients(TaskContext c) {
		int n = 0;
		for (var i : new net.minecraft.item.Item[]{Items.NETHER_WART, Items.BLAZE_POWDER,
				Items.GHAST_TEAR, Items.SPIDER_EYE, Items.MAGMA_CREAM, Items.SUGAR,
				Items.GLISTERING_MELON_SLICE, Items.GOLDEN_CARROT, Items.PUFFERFISH,
				Items.RABBIT_FOOT, Items.PHANTOM_MEMBRANE, Items.TURTLE_SCUTE}) {
			if (c.gained(i)) {
				n++;
			}
		}
		return n;
	}
}
