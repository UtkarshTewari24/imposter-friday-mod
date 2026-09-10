package com.impostorfridays.task.pool;

import com.impostorfridays.task.Difficulty;
import com.impostorfridays.task.TaskCategory;
import com.impostorfridays.task.TaskContext;
import com.impostorfridays.task.TaskDefinition;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

import java.util.ArrayList;
import java.util.List;

import static com.impostorfridays.task.TaskDefinition.major;
import static com.impostorfridays.task.TaskDefinition.sub;

/** Expert pools — "this is going to take most of the game". */
public final class ExpertTasks {

	private ExpertTasks() {
	}

	private static final Difficulty D = Difficulty.EXPERT;

	public static List<TaskDefinition> subTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		// --- Rare acquisition ---
		t.add(sub("EXP_SUB_DEBRIS_16", D, TaskCategory.NETHER)
				.desc("Obtain 16 ancient debris").mins(40, 65).nether()
				.check(c -> c.gained(Items.ANCIENT_DEBRIS, 16)).build());
		t.add(sub("EXP_SUB_NETHERITE_TEMPLATE", D, TaskCategory.NETHER)
				.desc("Obtain a netherite upgrade smithing template").mins(35, 60).nether()
				.check(c -> c.gained(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)).build());
		t.add(sub("EXP_SUB_TRIM_SILENCE", D, TaskCategory.STRUCTURE)
				.desc("Obtain a Silence armour trim template").mins(40, 65)
				.check(c -> c.gained(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)).build());
		t.add(sub("EXP_SUB_TRIM_SNOUT", D, TaskCategory.NETHER)
				.desc("Obtain a Snout armour trim template").mins(30, 55).nether()
				.check(c -> c.gained(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)).build());
		t.add(sub("EXP_SUB_TRIM_COAST", D, TaskCategory.STRUCTURE)
				.desc("Obtain a Coast armour trim template").mins(25, 45)
				.check(c -> c.gained(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)).build());
		t.add(sub("EXP_SUB_TRIM_WARD", D, TaskCategory.STRUCTURE)
				.desc("Obtain a Ward armour trim template").mins(40, 65)
				.check(c -> c.gained(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)).build());
		t.add(sub("EXP_SUB_EMERALD_64_TRADE", D, TaskCategory.TRADING)
				.desc("Obtain 64 emeralds through trading").mins(35, 60)
				.check(c -> c.gained(Items.EMERALD, 64) && c.getVillagerTrades() >= 15).build());
		t.add(sub("EXP_SUB_BLAZE_32", D, TaskCategory.NETHER)
				.desc("Obtain 32 blaze rods").mins(30, 50).nether()
				.check(c -> c.gained(Items.BLAZE_ROD, 32)).build());
		t.add(sub("EXP_SUB_PEARLS_32", D, TaskCategory.COMBAT)
				.desc("Obtain 32 ender pearls").mins(30, 50)
				.check(c -> c.gained(Items.ENDER_PEARL, 32)).build());
		t.add(sub("EXP_SUB_GHAST_TEARS_8", D, TaskCategory.NETHER)
				.desc("Obtain 8 ghast tears").mins(35, 60).nether()
				.check(c -> c.gained(Items.GHAST_TEAR, 8)).build());

		// --- Structures ---
		t.add(sub("EXP_SUB_CITY_LOOT_3", D, TaskCategory.STRUCTURE)
				.desc("Retrieve 3 distinct loot items from an ancient city").mins(35, 60)
				.check(c -> c.visitedStructure("minecraft:ancient_city")
						&& AdvancedTasks.countAncientCityLoot(c) >= 3).build());
		t.add(sub("EXP_SUB_TOTEM", D, TaskCategory.STRUCTURE)
				.desc("Retrieve a totem of undying from a woodland mansion").mins(40, 65)
				.check(c -> c.gained(Items.TOTEM_OF_UNDYING)).build());
		t.add(sub("EXP_SUB_SPONGE", D, TaskCategory.STRUCTURE)
				.desc("Retrieve a sponge from an ocean monument").mins(35, 60)
				.check(c -> c.gainedAnyOf(1, Items.SPONGE, Items.WET_SPONGE)).build());
		t.add(sub("EXP_SUB_BASTION_TREASURE", D, TaskCategory.NETHER)
				.desc("Retrieve a treasure item from a bastion remnant").mins(35, 60).nether()
				.check(c -> c.visitedStructure("minecraft:bastion_remnant")
						&& c.gainedAnyOf(1, Items.NETHERITE_SCRAP, Items.ANCIENT_DEBRIS,
						Items.ENCHANTED_GOLDEN_APPLE, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE))
				.build());
		t.add(sub("EXP_SUB_TRIAL_REWARD", D, TaskCategory.STRUCTURE)
				.desc("Obtain a trial key and open a vault in a trial chamber").mins(35, 60)
				.check(c -> c.gainedAnyOf(1, Items.TRIAL_KEY, Items.OMINOUS_TRIAL_KEY)
						&& c.visitedStructure("minecraft:trial_chambers")).build());
		t.add(sub("EXP_SUB_NETHER_WART", D, TaskCategory.NETHER)
				.desc("Find a Nether fortress and obtain nether wart").mins(25, 45).nether()
				.check(c -> c.visitedStructure("minecraft:fortress")
						&& c.gained(Items.NETHER_WART)).build());
		t.add(sub("EXP_SUB_END_PORTAL", D, TaskCategory.END)
				.desc("Locate a stronghold and activate its End portal").mins(40, 70).nether()
				.check(c -> c.advancementEarned("minecraft:story/enter_the_end")).build());
		t.add(sub("EXP_SUB_TWO_NETHER", D, TaskCategory.NETHER)
				.desc("Visit two different Nether structures").mins(30, 50).nether()
				.check(c -> c.structuresVisitedAmong("minecraft:fortress",
						"minecraft:bastion_remnant", "minecraft:nether_fossil",
						"minecraft:ruined_portal_nether") >= 2).build());
		t.add(sub("EXP_SUB_THREE_RARE", D, TaskCategory.STRUCTURE)
				.desc("Visit three different rare overworld structures").mins(40, 70)
				.check(c -> c.structuresVisitedAmong("minecraft:mansion", "minecraft:monument",
						"minecraft:ancient_city", "minecraft:stronghold",
						"minecraft:trial_chambers", "minecraft:jungle_pyramid") >= 3).build());
		t.add(sub("EXP_SUB_SNIFFER_EGG", D, TaskCategory.ANIMAL)
				.desc("Obtain a sniffer egg from suspicious sand").mins(35, 60)
				.check(c -> c.gained(Items.SNIFFER_EGG)).build());

		// --- Combat ---
		t.add(sub("EXP_SUB_WARDEN", D, TaskCategory.COMBAT)
				.desc("Kill a Warden").mins(40, 70)
				.check(c -> c.killed("minecraft:warden")).build());
		t.add(sub("EXP_SUB_ELDER_GUARDIAN", D, TaskCategory.COMBAT)
				.desc("Kill an Elder Guardian").mins(30, 50)
				.check(c -> c.killed("minecraft:elder_guardian")).build());
		t.add(sub("EXP_SUB_RAID", D, TaskCategory.COMBAT)
				.desc("Defeat a raid").mins(30, 50)
				.check(c -> c.advancementEarned("minecraft:adventure/hero_of_the_village")).build());
		t.add(sub("EXP_SUB_RAVAGER_CLEAN", D, TaskCategory.COMBAT)
				.desc("Kill a Ravager without dying").mins(25, 45)
				.check(c -> c.killed("minecraft:ravager") && c.bestKillStreak() >= 1).build());
		t.add(sub("EXP_SUB_BLAZE_10", D, TaskCategory.NETHER)
				.desc("Kill 10 Blazes").mins(25, 45).nether()
				.check(c -> c.killedAtLeast("minecraft:blaze", 10)).build());
		t.add(sub("EXP_SUB_GHAST_REFLECT", D, TaskCategory.NETHER)
				.desc("Kill a Ghast with its own reflected fireball").mins(30, 55).nether()
				.check(c -> c.advancementEarned("minecraft:nether/return_to_sender")).build());
		t.add(sub("EXP_SUB_TRIDENT_KILL", D, TaskCategory.COMBAT)
				.desc("Kill a hostile mob with a trident").mins(30, 50)
				.check(c -> c.killedWith("minecraft:trident")).build());
		t.add(sub("EXP_SUB_NO_ARMOUR_10", D, TaskCategory.COMBAT)
				.desc("Kill 10 hostile mobs while wearing no armour").mins(20, 35)
				.check(c -> c.getNoArmourKills() >= 10).build());
		t.add(sub("EXP_SUB_FIVE_TYPES_CLEAN", D, TaskCategory.COMBAT)
				.desc("Kill five different hostile mob types without dying").mins(20, 35)
				.check(c -> c.distinctMobTypesKilled() >= 5 && c.bestKillStreak() >= 5).build());
		t.add(sub("EXP_SUB_WITHER_SKULL", D, TaskCategory.NETHER)
				.desc("Obtain a wither skeleton skull").mins(35, 60).nether()
				.check(c -> c.gained(Items.WITHER_SKELETON_SKULL)).build());

		// --- Collection ---
		t.add(sub("EXP_SUB_DISCS_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different music discs").mins(50, 80)
				.check(c -> c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 10).build());
		t.add(sub("EXP_SUB_TRIMS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different armour trim templates").mins(45, 75)
				.check(c -> c.distinctGainedMatching(TaskContext.TRIM_TEMPLATE) >= 5).build());
		t.add(sub("EXP_SUB_SHERDS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different pottery sherds").mins(35, 60)
				.check(c -> c.distinctGainedMatching(TaskContext.POTTERY_SHERD) >= 5).build());
		t.add(sub("EXP_SUB_HORNS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different goat horn variants").mins(35, 60)
				.check(c -> c.distinctGoatHorns() >= 5).build());
		t.add(sub("EXP_SUB_MOB_DROPS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different rare mob drops").mins(30, 55)
				.check(c -> countRareDrops(c) >= 5).build());
		t.add(sub("EXP_SUB_FLOWERS_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different types of flower").mins(25, 45)
				.check(c -> c.distinctGainedInTag(ItemTags.FLOWERS) >= 10).build());
		t.add(sub("EXP_SUB_SAPLINGS_6", D, TaskCategory.COLLECTION)
				.desc("Obtain 6 different types of sapling").mins(25, 45)
				.check(c -> c.distinctGainedInTag(ItemTags.SAPLINGS) >= 6).build());
		t.add(sub("EXP_SUB_WOOL_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different colours of wool").mins(25, 45)
				.check(c -> c.distinctGainedInTag(ItemTags.WOOL) >= 10).build());
		t.add(sub("EXP_SUB_LOGS_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different types of log").mins(30, 50)
				.check(c -> c.distinctGainedInTag(ItemTags.LOGS) >= 10).build());
		t.add(sub("EXP_SUB_POTION_INGREDIENTS_10", D, TaskCategory.NETHER)
				.desc("Obtain 10 different potion ingredients").mins(35, 60).nether()
				.check(c -> AdvancedTasks.countPotionIngredients(c) >= 10).build());

		return t;
	}

	public static List<TaskDefinition> majorTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		t.add(major("EXP_MAJ_DIAMOND_EVERYONE", D, TaskCategory.PROGRESSION)
				.desc("Everyone obtains full diamond armour").mins(50, 80).group()
				.check(c -> c.allInnocentsWearing(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
						Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)).build());
		t.add(major("EXP_MAJ_NETHERITE_ONE", D, TaskCategory.NETHER)
				.desc("One player obtains full netherite armour").mins(60, 88).nether().group()
				.check(c -> c.innocentsWearing(1, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
						Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)).build());
		t.add(major("EXP_MAJ_FULL_BEACON", D, TaskCategory.PROGRESSION)
				.desc("Build and activate a fully powered beacon").mins(65, 88).nether()
				.check(c -> c.advancementEarned("minecraft:nether/create_full_beacon")).build());
		t.add(major("EXP_MAJ_SHULKER_ONE_ITEM", D, TaskCategory.END)
				.desc("Fill a shulker box completely with a single item type").mins(55, 85).end()
				.check(TaskContext::anyShulkerFullOfOneItem).build());
		t.add(major("EXP_MAJ_NETHERITE_4", D, TaskCategory.NETHER)
				.desc("Obtain a netherite upgrade template and craft 4 netherite items")
				.mins(60, 88).nether()
				.check(c -> c.gained(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
						&& c.gained(Items.NETHERITE_INGOT, 4)).build());
		t.add(major("EXP_MAJ_RAID_EMERALDS", D, TaskCategory.TRADING)
				.desc("Defeat a raid, then obtain 32 emeralds from villager trading").mins(55, 85)
				.check(c -> c.advancementEarned("minecraft:adventure/hero_of_the_village")
						&& c.gained(Items.EMERALD, 32) && c.getVillagerTrades() >= 10).build());
		t.add(major("EXP_MAJ_TRIMS_8", D, TaskCategory.COLLECTION)
				.desc("Obtain 8 different armour trim templates").mins(60, 88)
				.check(c -> c.distinctGainedMatching(TaskContext.TRIM_TEMPLATE) >= 8).build());
		t.add(major("EXP_MAJ_DISCS_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different music discs").mins(55, 85)
				.check(c -> c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 10).build());
		t.add(major("EXP_MAJ_HORN_DISC5_POT_SHERD", D, TaskCategory.COLLECTION)
				.desc("Obtain a goat horn, Music Disc 5, and a decorated pot made from sherds")
				.mins(60, 88)
				.check(c -> c.gained(Items.GOAT_HORN) && c.gained(Items.MUSIC_DISC_5)
						&& c.gained(Items.DECORATED_POT)
						&& c.distinctGainedMatching(TaskContext.POTTERY_SHERD) >= 1).build());
		t.add(major("EXP_MAJ_SPONGES_8", D, TaskCategory.COMBAT)
				.desc("Defeat an Elder Guardian and obtain 8 sponges").mins(55, 85)
				.check(c -> c.killed("minecraft:elder_guardian")
						&& c.gainedAnyOf(8, Items.SPONGE, Items.WET_SPONGE)).build());
		t.add(major("EXP_MAJ_CITY_5", D, TaskCategory.STRUCTURE)
				.desc("Enter an ancient city and retrieve 5 different loot items").mins(55, 85)
				.check(c -> c.visitedStructure("minecraft:ancient_city")
						&& AdvancedTasks.countAncientCityLoot(c) >= 5).build());
		t.add(major("EXP_MAJ_TRIAL_5", D, TaskCategory.STRUCTURE)
				.desc("Complete a trial chamber and obtain 5 distinct rewards").mins(55, 85)
				.check(c -> c.visitedStructure("minecraft:trial_chambers")
						&& countTrialRewards(c) >= 5).build());
		t.add(major("EXP_MAJ_TWO_ENCHANTED_SETS", D, TaskCategory.PROGRESSION)
				.desc("Two players obtain fully enchanted diamond armour").mins(60, 88).group()
				.check(c -> c.innocentsWearing(2, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
						Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)
						&& c.advancementEarned("minecraft:story/enchant_item")).build());
		t.add(major("EXP_MAJ_EMERALD_128", D, TaskCategory.TRADING)
				.desc("Obtain 128 emeralds exclusively through villager trading").mins(60, 88)
				.check(c -> c.gained(Items.EMERALD, 128) && c.getVillagerTrades() >= 30).build());
		t.add(major("EXP_MAJ_END_RETURN", D, TaskCategory.END)
				.desc("Reach the End and return with 5 different End resources").mins(60, 88).end()
				.check(c -> c.advancementEarned("minecraft:story/enter_the_end")
						&& countEndItems(c) >= 5).build());

		return t;
	}

	static int countRareDrops(TaskContext c) {
		int n = 0;
		for (Item i : new Item[]{Items.BLAZE_ROD, Items.ENDER_PEARL, Items.GHAST_TEAR,
				Items.MAGMA_CREAM, Items.PHANTOM_MEMBRANE, Items.SHULKER_SHELL,
				Items.WITHER_SKELETON_SKULL, Items.TOTEM_OF_UNDYING, Items.RABBIT_FOOT,
				Items.NAUTILUS_SHELL, Items.HEART_OF_THE_SEA, Items.TRIDENT}) {
			if (c.gained(i)) {
				n++;
			}
		}
		return n;
	}

	static int countTrialRewards(TaskContext c) {
		int n = 0;
		for (Item i : new Item[]{Items.TRIAL_KEY, Items.OMINOUS_TRIAL_KEY, Items.HEAVY_CORE,
				Items.MACE, Items.BREEZE_ROD, Items.OMINOUS_BOTTLE, Items.WIND_CHARGE,
				Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK}) {
			if (c.gained(i)) {
				n++;
			}
		}
		return n;
	}

	static int countEndItems(TaskContext c) {
		int n = 0;
		for (Item i : new Item[]{Items.SHULKER_SHELL, Items.CHORUS_FRUIT, Items.POPPED_CHORUS_FRUIT,
				Items.END_STONE, Items.PURPUR_BLOCK, Items.ELYTRA, Items.DRAGON_BREATH,
				Items.END_ROD, Items.ENDER_PEARL}) {
			if (c.gained(i)) {
				n++;
			}
		}
		return n;
	}
}
