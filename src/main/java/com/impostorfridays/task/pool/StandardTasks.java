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

/** Standard pools — "we'll need to split up". The normal intended experience. */
public final class StandardTasks {

	private StandardTasks() {
	}

	private static final Difficulty D = Difficulty.STANDARD;

	public static List<TaskDefinition> subTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		// --- Resources ---
		t.add(sub("STD_SUB_DIAMOND_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 diamonds").mins(18, 30)
				.check(c -> c.gained(Items.DIAMOND, 32)).build());
		t.add(sub("STD_SUB_EMERALD_BLOCKS_8", D, TaskCategory.TRADING)
				.desc("Obtain 8 emerald blocks").mins(20, 35)
				.check(c -> c.gained(Items.EMERALD_BLOCK, 8)).build());
		t.add(sub("STD_SUB_GOLD_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 gold ingots").mins(15, 28)
				.check(c -> c.gained(Items.GOLD_INGOT, 32)).build());
		t.add(sub("STD_SUB_OBSIDIAN_8", D, TaskCategory.RESOURCE)
				.desc("Obtain 8 obsidian").mins(10, 20)
				.check(c -> c.gained(Items.OBSIDIAN, 8)).build());
		t.add(sub("STD_SUB_QUARTZ_BLOCK_16", D, TaskCategory.NETHER)
				.desc("Obtain 16 quartz blocks").mins(18, 30).nether()
				.check(c -> c.gained(Items.QUARTZ_BLOCK, 16)).build());
		t.add(sub("STD_SUB_COPPER_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 copper ingots").mins(12, 22)
				.check(c -> c.gained(Items.COPPER_INGOT, 32)).build());
		t.add(sub("STD_SUB_AMETHYST_16", D, TaskCategory.RESOURCE)
				.desc("Obtain 16 amethyst shards").mins(15, 28)
				.check(c -> c.gained(Items.AMETHYST_SHARD, 16)).build());
		t.add(sub("STD_SUB_SLIME_16", D, TaskCategory.COMBAT)
				.desc("Obtain 16 slimeballs").mins(18, 32)
				.check(c -> c.gained(Items.SLIME_BALL, 16)).build());
		t.add(sub("STD_SUB_BLAZE_16", D, TaskCategory.NETHER)
				.desc("Obtain 16 blaze rods").mins(20, 35).nether()
				.check(c -> c.gained(Items.BLAZE_ROD, 16)).build());
		t.add(sub("STD_SUB_PEARLS_16", D, TaskCategory.COMBAT)
				.desc("Obtain 16 ender pearls").mins(18, 32)
				.check(c -> c.gained(Items.ENDER_PEARL, 16)).build());

		// --- Animals ---
		t.add(sub("STD_SUB_BREED_PANDA", D, TaskCategory.ANIMAL)
				.desc("Breed a panda").mins(18, 32)
				.check(c -> c.bred("minecraft:panda")).build());
		t.add(sub("STD_SUB_BREED_MOOSHROOM", D, TaskCategory.ANIMAL)
				.desc("Breed a mooshroom").mins(18, 32)
				.check(c -> c.bred("minecraft:mooshroom")).build());
		t.add(sub("STD_SUB_BREED_GOAT", D, TaskCategory.ANIMAL)
				.desc("Breed a goat").mins(14, 26)
				.check(c -> c.bred("minecraft:goat")).build());
		t.add(sub("STD_SUB_TAME_TWO", D, TaskCategory.ANIMAL)
				.desc("Tame two different types of animal").mins(14, 25)
				.check(c -> c.distinctTamed() >= 2).build());
		t.add(sub("STD_SUB_RABBIT_FOOT", D, TaskCategory.ANIMAL)
				.desc("Obtain a rabbit's foot").mins(15, 30)
				.check(c -> c.gained(Items.RABBIT_FOOT)).build());
		t.add(sub("STD_SUB_GOAT_HORN", D, TaskCategory.ANIMAL)
				.desc("Obtain a goat horn").mins(12, 25)
				.check(c -> c.gained(Items.GOAT_HORN)).build());
		t.add(sub("STD_SUB_HONEY_BOTTLE", D, TaskCategory.FARMING)
				.desc("Obtain a honey bottle").mins(10, 20)
				.check(c -> c.gained(Items.HONEY_BOTTLE)).build());
		t.add(sub("STD_SUB_HONEYCOMB_16", D, TaskCategory.FARMING)
				.desc("Obtain 16 honeycomb").mins(15, 28)
				.check(c -> c.gained(Items.HONEYCOMB, 16)).build());
		t.add(sub("STD_SUB_BREED_THREE", D, TaskCategory.ANIMAL)
				.desc("Breed three different animal species").mins(14, 25)
				.check(c -> c.distinctSpeciesBred() >= 3).build());
		t.add(sub("STD_SUB_FISH_BUCKET", D, TaskCategory.COLLECTION)
				.desc("Capture a live fish in a bucket").mins(8, 18)
				.check(c -> c.gainedAnyOf(1, Items.COD_BUCKET, Items.SALMON_BUCKET,
						Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET)).build());

		// --- Exploration ---
		t.add(sub("STD_SUB_MANSION", D, TaskCategory.STRUCTURE)
				.desc("Locate a woodland mansion").mins(25, 45)
				.check(c -> c.visitedStructure("minecraft:mansion")).build());
		t.add(sub("STD_SUB_OUTPOST", D, TaskCategory.STRUCTURE)
				.desc("Locate a pillager outpost").mins(12, 25)
				.check(c -> c.visitedStructure("minecraft:pillager_outpost")).build());
		t.add(sub("STD_SUB_BASTION", D, TaskCategory.NETHER)
				.desc("Locate a bastion remnant").mins(20, 35).nether()
				.check(c -> c.visitedStructure("minecraft:bastion_remnant")).build());
		t.add(sub("STD_SUB_FORTRESS", D, TaskCategory.NETHER)
				.desc("Locate a Nether fortress").mins(18, 32).nether()
				.check(c -> c.visitedStructure("minecraft:fortress")).build());
		t.add(sub("STD_SUB_ANCIENT_CITY", D, TaskCategory.STRUCTURE)
				.desc("Locate an ancient city").mins(22, 40)
				.check(c -> c.visitedStructure("minecraft:ancient_city")).build());
		t.add(sub("STD_SUB_TRIAL_CHAMBER", D, TaskCategory.STRUCTURE)
				.desc("Locate a trial chamber").mins(18, 32)
				.check(c -> c.visitedStructure("minecraft:trial_chambers")).build());
		t.add(sub("STD_SUB_JUNGLE_TEMPLE", D, TaskCategory.STRUCTURE)
				.desc("Locate a jungle temple").mins(18, 35)
				.check(c -> c.visitedStructure("minecraft:jungle_pyramid")).build());
		t.add(sub("STD_SUB_MONUMENT", D, TaskCategory.STRUCTURE)
				.desc("Locate an ocean monument").mins(20, 35)
				.check(c -> c.visitedStructure("minecraft:monument")).build());
		t.add(sub("STD_SUB_STRONGHOLD", D, TaskCategory.STRUCTURE)
				.desc("Locate a stronghold").mins(22, 40)
				.check(c -> c.visitedStructure("minecraft:stronghold")).build());
		t.add(sub("STD_SUB_BURIED_TREASURE", D, TaskCategory.STRUCTURE)
				.desc("Locate buried treasure").mins(18, 32)
				.check(c -> c.visitedStructure("minecraft:buried_treasure")).build());

		// --- Combat ---
		t.add(sub("STD_SUB_KILL_RAVAGER", D, TaskCategory.COMBAT)
				.desc("Kill a Ravager").mins(15, 30)
				.check(c -> c.killed("minecraft:ravager")).build());
		t.add(sub("STD_SUB_KILL_GUARDIAN", D, TaskCategory.COMBAT)
				.desc("Kill a Guardian").mins(20, 35)
				.check(c -> c.killed("minecraft:guardian")).build());
		t.add(sub("STD_SUB_KILL_WITCH", D, TaskCategory.COMBAT)
				.desc("Kill a Witch").mins(10, 22)
				.check(c -> c.killed("minecraft:witch")).build());
		t.add(sub("STD_SUB_KILL_BLAZE", D, TaskCategory.NETHER)
				.desc("Kill a Blaze").mins(18, 32).nether()
				.check(c -> c.killed("minecraft:blaze")).build());
		t.add(sub("STD_SUB_KILL_VINDICATOR", D, TaskCategory.COMBAT)
				.desc("Kill a Vindicator").mins(15, 30)
				.check(c -> c.killed("minecraft:vindicator")).build());
		t.add(sub("STD_SUB_STREAK_10", D, TaskCategory.COMBAT)
				.desc("Kill 10 hostile mobs without dying").mins(10, 20)
				.check(c -> c.bestKillStreak() >= 10).build());
		t.add(sub("STD_SUB_THREE_TYPES", D, TaskCategory.COMBAT)
				.desc("Kill three different hostile mob types").mins(8, 18)
				.check(c -> c.distinctMobTypesKilled() >= 3).build());
		t.add(sub("STD_SUB_SNIPER", D, TaskCategory.COMBAT)
				.desc("Kill a skeleton with a bow from at least 50 blocks away").mins(12, 25)
				.check(c -> c.advancementEarned("minecraft:adventure/sniper_duel")).build());
		t.add(sub("STD_SUB_NO_ARMOUR_5", D, TaskCategory.COMBAT)
				.desc("Kill 5 hostile mobs while wearing no armour").mins(10, 20)
				.check(c -> c.getNoArmourKills() >= 5).build());
		t.add(sub("STD_SUB_TWO_WEAPONS", D, TaskCategory.COMBAT)
				.desc("Land killing blows with three different weapons").mins(10, 20)
				.check(c -> c.distinctKillWeapons() >= 3).build());

		return t;
	}

	public static List<TaskDefinition> majorTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		t.add(major("STD_MAJ_DIAMOND_EVERYONE", D, TaskCategory.PROGRESSION)
				.desc("Everyone obtains full diamond armour").mins(45, 75).group()
				.check(c -> c.allInnocentsWearing(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
						Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)).build());
		t.add(major("STD_MAJ_BEACON", D, TaskCategory.PROGRESSION)
				.desc("Activate a beacon").mins(45, 75).nether()
				.check(c -> c.advancementEarned("minecraft:nether/create_beacon")).build());
		t.add(major("STD_MAJ_DIAMOND_64", D, TaskCategory.RESOURCE)
				.desc("Obtain 64 diamonds").mins(40, 70)
				.check(c -> c.gained(Items.DIAMOND, 64)).build());
		t.add(major("STD_MAJ_SHULKER_FULL", D, TaskCategory.END)
				.desc("Obtain a shulker box and fill all 27 slots").mins(50, 80).end()
				.check(TaskContext::anyFullShulkerBox).build());
		t.add(major("STD_MAJ_HORN_DISC_POT", D, TaskCategory.COLLECTION)
				.desc("Obtain a goat horn, a music disc and a decorated pot").mins(40, 70)
				.check(c -> c.gained(Items.GOAT_HORN)
						&& c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 1
						&& c.gained(Items.DECORATED_POT)).build());
		t.add(major("STD_MAJ_TRADES_20", D, TaskCategory.TRADING)
				.desc("Complete 20 successful villager trades").mins(35, 60)
				.check(c -> c.getVillagerTrades() >= 20).build());
		t.add(major("STD_MAJ_DISCS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different music discs").mins(45, 75)
				.check(c -> c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 5).build());
		t.add(major("STD_MAJ_END_PORTAL", D, TaskCategory.END)
				.desc("Find a stronghold and activate the End portal").mins(45, 75).nether()
				.check(c -> c.advancementEarned("minecraft:story/follow_ender_eye")
						&& c.advancementEarned("minecraft:story/enter_the_end")).build());
		t.add(major("STD_MAJ_RAID", D, TaskCategory.COMBAT)
				.desc("Defeat a raid and gain the Hero of the Village effect").mins(40, 70)
				.check(c -> c.advancementEarned("minecraft:adventure/hero_of_the_village")).build());
		t.add(major("STD_MAJ_BLAZE_PEARLS", D, TaskCategory.NETHER)
				.desc("Obtain 32 blaze rods and 16 ender pearls").mins(45, 75).nether()
				.check(c -> c.gained(Items.BLAZE_ROD, 32) && c.gained(Items.ENDER_PEARL, 16)).build());
		t.add(major("STD_MAJ_ENCHANT_DIAMOND", D, TaskCategory.PROGRESSION)
				.desc("Obtain full diamond armour and enchant every piece").mins(45, 75)
				.check(c -> c.gained(Items.DIAMOND_HELMET) && c.gained(Items.DIAMOND_CHESTPLATE)
						&& c.gained(Items.DIAMOND_LEGGINGS) && c.gained(Items.DIAMOND_BOOTS)
						&& c.advancementEarned("minecraft:story/enchant_item")).build());
		t.add(major("STD_MAJ_WOOD_TYPES", D, TaskCategory.COLLECTION)
				.desc("Obtain 8 different types of log").mins(35, 60)
				.check(c -> c.distinctGainedInTag(net.minecraft.registry.tag.ItemTags.LOGS) >= 8).build());
		t.add(major("STD_MAJ_STRUCTURES_5", D, TaskCategory.STRUCTURE)
				.desc("Visit 5 different generated structures").mins(45, 75)
				.check(c -> c.distinctStructuresVisited() >= 5).build());
		t.add(major("STD_MAJ_CURE_VILLAGER", D, TaskCategory.TRADING)
				.desc("Cure a zombie villager").mins(40, 70).nether()
				.check(c -> c.advancementEarned("minecraft:story/cure_zombie_villager")).build());
		t.add(major("STD_MAJ_WOOL_16", D, TaskCategory.COLLECTION)
				.desc("Obtain all 16 colours of wool").mins(40, 70)
				.check(c -> c.distinctGainedInTag(net.minecraft.registry.tag.ItemTags.WOOL) >= 16).build());

		return t;
	}
}
