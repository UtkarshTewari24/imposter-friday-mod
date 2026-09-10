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

/**
 * Master pools — "we need basically everyone contributing efficiently".
 *
 * <p>The final boss of the task system, but still fun Minecraft objectives rather than grind
 * walls: difficulty comes from progression depth, not from inflated counts.
 */
public final class MasterTasks {

	private MasterTasks() {
	}

	private static final Difficulty D = Difficulty.MASTER;

	public static List<TaskDefinition> subTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		// --- Extremely valuable resources ---
		t.add(sub("MAS_SUB_DEBRIS_8", D, TaskCategory.NETHER)
				.desc("Obtain 8 ancient debris").mins(35, 60).nether()
				.check(c -> c.gained(Items.ANCIENT_DEBRIS, 8)).build());
		t.add(sub("MAS_SUB_NETHERITE_TEMPLATE", D, TaskCategory.NETHER)
				.desc("Obtain a netherite upgrade smithing template").mins(35, 60).nether()
				.check(c -> c.gained(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)).build());
		t.add(sub("MAS_SUB_EMERALD_64_TRADE", D, TaskCategory.TRADING)
				.desc("Obtain 64 emeralds through villager trading").mins(35, 60)
				.check(c -> c.gained(Items.EMERALD, 64) && c.getVillagerTrades() >= 20).build());
		t.add(sub("MAS_SUB_BLAZE_32", D, TaskCategory.NETHER)
				.desc("Obtain 32 blaze rods").mins(30, 50).nether()
				.check(c -> c.gained(Items.BLAZE_ROD, 32)).build());
		t.add(sub("MAS_SUB_PEARLS_32", D, TaskCategory.COMBAT)
				.desc("Obtain 32 ender pearls").mins(30, 50)
				.check(c -> c.gained(Items.ENDER_PEARL, 32)).build());
		t.add(sub("MAS_SUB_GHAST_TEARS_16", D, TaskCategory.NETHER)
				.desc("Obtain 16 ghast tears").mins(45, 75).nether()
				.check(c -> c.gained(Items.GHAST_TEAR, 16)).build());
		t.add(sub("MAS_SUB_SHULKER_SHELLS_16", D, TaskCategory.END)
				.desc("Obtain 16 shulker shells").mins(50, 80).end()
				.check(c -> c.gained(Items.SHULKER_SHELL, 16)).build());
		t.add(sub("MAS_SUB_CRYING_OBSIDIAN_16", D, TaskCategory.NETHER)
				.desc("Obtain 16 crying obsidian").mins(35, 60).nether()
				.check(c -> c.gained(Items.CRYING_OBSIDIAN, 16)).build());
		t.add(sub("MAS_SUB_GOLD_BLOCKS_32", D, TaskCategory.NETHER)
				.desc("Obtain 32 gold blocks").mins(45, 75).nether()
				.check(c -> c.gained(Items.GOLD_BLOCK, 32)).build());
		t.add(sub("MAS_SUB_DIAMOND_BLOCKS_8", D, TaskCategory.RESOURCE)
				.desc("Obtain 8 diamond blocks").mins(40, 70)
				.check(c -> c.gained(Items.DIAMOND_BLOCK, 8)).build());

		// --- Rare structures ---
		t.add(sub("MAS_SUB_CITY_LOOT", D, TaskCategory.STRUCTURE)
				.desc("Retrieve a rare loot item from an ancient city").mins(35, 60)
				.check(c -> c.visitedStructure("minecraft:ancient_city")
						&& c.gainedAnyOf(1, Items.ECHO_SHARD, Items.DISC_FRAGMENT_5)).build());
		t.add(sub("MAS_SUB_MANSION_TOTEM", D, TaskCategory.STRUCTURE)
				.desc("Retrieve a totem of undying from a woodland mansion").mins(45, 75)
				.check(c -> c.gained(Items.TOTEM_OF_UNDYING)).build());
		t.add(sub("MAS_SUB_SPONGE", D, TaskCategory.STRUCTURE)
				.desc("Retrieve a sponge from an ocean monument").mins(35, 60)
				.check(c -> c.gainedAnyOf(1, Items.SPONGE, Items.WET_SPONGE)).build());
		t.add(sub("MAS_SUB_BASTION_TREASURE", D, TaskCategory.NETHER)
				.desc("Retrieve a treasure item from a bastion remnant").mins(35, 60).nether()
				.check(c -> c.visitedStructure("minecraft:bastion_remnant")
						&& c.gainedAnyOf(1, Items.NETHERITE_SCRAP, Items.ANCIENT_DEBRIS,
						Items.ENCHANTED_GOLDEN_APPLE)).build());
		t.add(sub("MAS_SUB_TRIAL_REWARD", D, TaskCategory.STRUCTURE)
				.desc("Obtain a heavy core or a breeze rod from a trial chamber").mins(40, 70)
				.check(c -> c.gainedAnyOf(1, Items.HEAVY_CORE, Items.BREEZE_ROD)).build());
		t.add(sub("MAS_SUB_TRIM_FROM_STRUCTURE", D, TaskCategory.COLLECTION)
				.desc("Obtain 3 different armour trim templates").mins(40, 70)
				.check(c -> c.distinctGainedMatching(TaskContext.TRIM_TEMPLATE) >= 3).build());
		t.add(sub("MAS_SUB_STRUCTURE_ITEMS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different structure-exclusive loot items").mins(45, 75)
				.check(c -> ExpertTasks.countRareDrops(c) >= 5).build());
		t.add(sub("MAS_SUB_THREE_RARE_STRUCTURES", D, TaskCategory.STRUCTURE)
				.desc("Visit three different rare structures").mins(45, 75)
				.check(c -> c.structuresVisitedAmong("minecraft:mansion", "minecraft:monument",
						"minecraft:ancient_city", "minecraft:stronghold",
						"minecraft:trial_chambers", "minecraft:bastion_remnant") >= 3).build());
		t.add(sub("MAS_SUB_SIX_STRUCTURES", D, TaskCategory.EXPLORATION)
				.desc("Visit 6 different generated structures").mins(40, 70)
				.check(c -> c.distinctStructuresVisited() >= 6).build());
		t.add(sub("MAS_SUB_TWO_NETHER", D, TaskCategory.NETHER)
				.desc("Visit two different Nether structures").mins(30, 50).nether()
				.check(c -> c.structuresVisitedAmong("minecraft:fortress",
						"minecraft:bastion_remnant", "minecraft:nether_fossil") >= 2).build());

		// --- Combat ---
		t.add(sub("MAS_SUB_WARDEN", D, TaskCategory.COMBAT)
				.desc("Kill a Warden").mins(45, 75)
				.check(c -> c.killed("minecraft:warden")).build());
		t.add(sub("MAS_SUB_RAID", D, TaskCategory.COMBAT)
				.desc("Defeat a raid").mins(30, 55)
				.check(c -> c.advancementEarned("minecraft:adventure/hero_of_the_village")).build());
		t.add(sub("MAS_SUB_ELDER_GUARDIAN", D, TaskCategory.COMBAT)
				.desc("Kill an Elder Guardian").mins(35, 60)
				.check(c -> c.killed("minecraft:elder_guardian")).build());
		t.add(sub("MAS_SUB_RAVAGER_RAID", D, TaskCategory.COMBAT)
				.desc("Kill a Ravager during a raid").mins(35, 60)
				.check(c -> c.killed("minecraft:ravager")
						&& c.advancementEarned("minecraft:adventure/voluntary_exile")).build());
		t.add(sub("MAS_SUB_WITHER", D, TaskCategory.COMBAT)
				.desc("Summon and defeat the Wither").mins(50, 80).nether()
				.check(c -> c.advancementEarned("minecraft:nether/summon_wither")
						&& c.killed("minecraft:wither")).build());
		t.add(sub("MAS_SUB_GHAST_REFLECT", D, TaskCategory.NETHER)
				.desc("Kill a Ghast with its own reflected fireball").mins(30, 55).nether()
				.check(c -> c.advancementEarned("minecraft:nether/return_to_sender")).build());
		t.add(sub("MAS_SUB_TRIDENT_KILL", D, TaskCategory.COMBAT)
				.desc("Kill a hostile mob with a trident").mins(30, 55)
				.check(c -> c.killedWith("minecraft:trident")).build());
		t.add(sub("MAS_SUB_NO_ARMOUR_15", D, TaskCategory.COMBAT)
				.desc("Kill 15 hostile mobs while wearing no armour").mins(25, 45)
				.check(c -> c.getNoArmourKills() >= 15).build());
		t.add(sub("MAS_SUB_FIVE_WEAPONS", D, TaskCategory.COMBAT)
				.desc("Land killing blows with five different weapons").mins(25, 45)
				.check(c -> c.distinctKillWeapons() >= 5).build());
		t.add(sub("MAS_SUB_EIGHT_TYPES", D, TaskCategory.COMBAT)
				.desc("Kill eight different hostile mob types").mins(25, 45)
				.check(c -> c.distinctMobTypesKilled() >= 8).build());

		// --- Rare collections ---
		t.add(sub("MAS_SUB_TRIMS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different armour trim templates").mins(45, 75)
				.check(c -> c.distinctGainedMatching(TaskContext.TRIM_TEMPLATE) >= 5).build());
		t.add(sub("MAS_SUB_DISCS_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different music discs").mins(50, 80)
				.check(c -> c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 10).build());
		t.add(sub("MAS_SUB_SHERDS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different pottery sherds").mins(35, 60)
				.check(c -> c.distinctGainedMatching(TaskContext.POTTERY_SHERD) >= 5).build());
		t.add(sub("MAS_SUB_HORNS_5", D, TaskCategory.COLLECTION)
				.desc("Obtain 5 different goat horn variants").mins(35, 60)
				.check(c -> c.distinctGoatHorns() >= 5).build());
		t.add(sub("MAS_SUB_RARE_DROPS_8", D, TaskCategory.COLLECTION)
				.desc("Obtain 8 different rare mob drops").mins(45, 75)
				.check(c -> ExpertTasks.countRareDrops(c) >= 8).build());
		t.add(sub("MAS_SUB_NETHER_INGREDIENTS", D, TaskCategory.NETHER)
				.desc("Obtain 5 different Nether-exclusive potion ingredients").mins(35, 60).nether()
				.check(c -> countNetherIngredients(c) >= 5).build());
		t.add(sub("MAS_SUB_LOGS_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different types of log").mins(30, 50)
				.check(c -> c.distinctGainedInTag(ItemTags.LOGS) >= 10).build());
		t.add(sub("MAS_SUB_WOOL_16", D, TaskCategory.COLLECTION)
				.desc("Obtain all 16 colours of wool").mins(35, 60)
				.check(c -> c.distinctGainedInTag(ItemTags.WOOL) >= 16).build());
		t.add(sub("MAS_SUB_FLOWERS_14", D, TaskCategory.COLLECTION)
				.desc("Obtain 14 different types of flower").mins(35, 60)
				.check(c -> c.distinctGainedInTag(ItemTags.FLOWERS) >= 14).build());
		t.add(sub("MAS_SUB_ENCHANTED_BOOKS_10", D, TaskCategory.PROGRESSION)
				.desc("Obtain 10 enchanted books").mins(40, 70)
				.check(c -> c.gained(Items.ENCHANTED_BOOK, 10)).build());

		return t;
	}

	public static List<TaskDefinition> majorTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		t.add(major("MAS_MAJ_DIAMOND_EVERYONE", D, TaskCategory.PROGRESSION)
				.desc("Everyone obtains full diamond armour").mins(50, 80).group()
				.check(c -> c.allInnocentsWearing(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
						Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)).build());
		t.add(major("MAS_MAJ_NETHERITE_ONE", D, TaskCategory.NETHER)
				.desc("One player obtains full netherite armour").mins(65, 88).nether().group()
				.check(c -> c.innocentsWearing(1, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
						Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)).build());
		t.add(major("MAS_MAJ_FULL_BEACON", D, TaskCategory.PROGRESSION)
				.desc("Build and activate a fully powered beacon").mins(70, 88).nether()
				.check(c -> c.advancementEarned("minecraft:nether/create_full_beacon")).build());
		t.add(major("MAS_MAJ_SHULKER_DIAMOND", D, TaskCategory.END)
				.desc("Fill a shulker box completely with diamond blocks").mins(70, 88).end()
				.check(c -> c.anyShulkerFullOf(Items.DIAMOND_BLOCK)).build());
		t.add(major("MAS_MAJ_NETHERITE_KIT", D, TaskCategory.NETHER)
				.desc("Craft a full set of netherite armour and four netherite tools")
				.mins(70, 88).nether().group()
				.check(c -> c.gained(Items.NETHERITE_HELMET) && c.gained(Items.NETHERITE_CHESTPLATE)
						&& c.gained(Items.NETHERITE_LEGGINGS) && c.gained(Items.NETHERITE_BOOTS)
						&& c.gained(Items.NETHERITE_PICKAXE) && c.gained(Items.NETHERITE_SWORD)
						&& c.gained(Items.NETHERITE_AXE) && c.gained(Items.NETHERITE_SHOVEL)).build());
		t.add(major("MAS_MAJ_TRIMS_12", D, TaskCategory.COLLECTION)
				.desc("Obtain 12 different armour trim templates").mins(70, 88)
				.check(c -> c.distinctGainedMatching(TaskContext.TRIM_TEMPLATE) >= 12).build());
		t.add(major("MAS_MAJ_DISCS_15", D, TaskCategory.COLLECTION)
				.desc("Obtain 15 different music discs").mins(70, 88)
				.check(c -> c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 15).build());
		t.add(major("MAS_MAJ_RAID_TRADES_20", D, TaskCategory.TRADING)
				.desc("Defeat a raid, gain Hero of the Village, then complete 20 villager trades")
				.mins(60, 88)
				.check(c -> c.advancementEarned("minecraft:adventure/hero_of_the_village")
						&& c.getVillagerTrades() >= 20).build());
		t.add(major("MAS_MAJ_SPONGES_8", D, TaskCategory.COMBAT)
				.desc("Defeat an Elder Guardian and retrieve 8 sponges").mins(60, 88)
				.check(c -> c.killed("minecraft:elder_guardian")
						&& c.gainedAnyOf(8, Items.SPONGE, Items.WET_SPONGE)).build());
		t.add(major("MAS_MAJ_CITY_6", D, TaskCategory.STRUCTURE)
				.desc("Explore an ancient city and retrieve 6 different loot items").mins(60, 88)
				.check(c -> c.visitedStructure("minecraft:ancient_city")
						&& AdvancedTasks.countAncientCityLoot(c) >= 6).build());
		t.add(major("MAS_MAJ_TRIAL_8", D, TaskCategory.STRUCTURE)
				.desc("Complete a trial chamber and obtain 8 distinct rewards").mins(65, 88)
				.check(c -> c.visitedStructure("minecraft:trial_chambers")
						&& ExpertTasks.countTrialRewards(c) >= 8).build());
		t.add(major("MAS_MAJ_COLLECTOR", D, TaskCategory.COLLECTION)
				.desc("Obtain a goat horn, Music Disc 5, a decorated pot and 5 trim templates")
				.mins(70, 88)
				.check(c -> c.gained(Items.GOAT_HORN) && c.gained(Items.MUSIC_DISC_5)
						&& c.gained(Items.DECORATED_POT)
						&& c.distinctGainedMatching(TaskContext.TRIM_TEMPLATE) >= 5).build());
		t.add(major("MAS_MAJ_ENCHANTED_NETHERITE", D, TaskCategory.PROGRESSION)
				.desc("Equip one player with fully enchanted netherite armour and 4 netherite tools")
				.mins(70, 88).nether().group()
				.check(c -> c.innocentsWearing(1, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
						Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)
						&& c.gained(Items.NETHERITE_PICKAXE) && c.gained(Items.NETHERITE_SWORD)
						&& c.gained(Items.NETHERITE_AXE) && c.gained(Items.NETHERITE_SHOVEL)
						&& c.advancementEarned("minecraft:story/enchant_item")).build());
		t.add(major("MAS_MAJ_EMERALD_128", D, TaskCategory.TRADING)
				.desc("Obtain 128 emeralds through villager trading").mins(65, 88)
				.check(c -> c.gained(Items.EMERALD, 128) && c.getVillagerTrades() >= 30).build());
		t.add(major("MAS_MAJ_DRAGON", D, TaskCategory.END)
				.desc("Reach the End, defeat the Ender Dragon, and return to the Overworld alive")
				.mins(70, 88).end()
				.check(c -> c.advancementEarned("minecraft:end/kill_dragon")).build());

		return t;
	}

	static int countNetherIngredients(TaskContext c) {
		int n = 0;
		for (Item i : new Item[]{Items.NETHER_WART, Items.BLAZE_POWDER, Items.GHAST_TEAR,
				Items.MAGMA_CREAM, Items.BLAZE_ROD, Items.CRIMSON_FUNGUS}) {
			if (c.gained(i)) {
				n++;
			}
		}
		return n;
	}
}
