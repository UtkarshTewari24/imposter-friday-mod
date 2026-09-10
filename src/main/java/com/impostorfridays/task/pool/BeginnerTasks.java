package com.impostorfridays.task.pool;

import com.impostorfridays.task.Difficulty;
import com.impostorfridays.task.TaskCategory;
import com.impostorfridays.task.TaskContext;
import com.impostorfridays.task.TaskDefinition;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

import java.util.ArrayList;
import java.util.List;

import static com.impostorfridays.task.TaskDefinition.major;
import static com.impostorfridays.task.TaskDefinition.sub;

/**
 * Beginner pools — "we can definitely do this".
 *
 * <p>A competent player should know what to do almost immediately, while three objectives still
 * occupy meaningful time for a group of six.
 */
public final class BeginnerTasks {

	private BeginnerTasks() {
	}

	private static final Difficulty D = Difficulty.BEGINNER;

	public static List<TaskDefinition> subTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		// --- Resource / crafting ---
		t.add(sub("BEG_SUB_IRON_ARMOR", D, TaskCategory.CRAFTING)
				.desc("Craft a full set of iron armour").mins(8, 15)
				.check(c -> c.gained(Items.IRON_HELMET) && c.gained(Items.IRON_CHESTPLATE)
						&& c.gained(Items.IRON_LEGGINGS) && c.gained(Items.IRON_BOOTS)).build());
		t.add(sub("BEG_SUB_IRON_TOOLS", D, TaskCategory.CRAFTING)
				.desc("Craft an iron pickaxe, axe, shovel and sword").mins(8, 14)
				.check(c -> c.gained(Items.IRON_PICKAXE) && c.gained(Items.IRON_AXE)
						&& c.gained(Items.IRON_SHOVEL) && c.gained(Items.IRON_SWORD)).build());
		t.add(sub("BEG_SUB_IRON_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 iron ingots").mins(10, 18)
				.check(c -> c.gained(Items.IRON_INGOT, 32)).build());
		t.add(sub("BEG_SUB_COAL_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 coal").mins(6, 12)
				.check(c -> c.gained(Items.COAL, 32)).build());
		t.add(sub("BEG_SUB_GOLD_16", D, TaskCategory.RESOURCE)
				.desc("Obtain 16 gold ingots").mins(12, 20)
				.check(c -> c.gained(Items.GOLD_INGOT, 16)).build());
		t.add(sub("BEG_SUB_DIAMOND_16", D, TaskCategory.RESOURCE)
				.desc("Obtain 16 diamonds").mins(15, 25)
				.check(c -> c.gained(Items.DIAMOND, 16)).build());
		t.add(sub("BEG_SUB_REDSTONE_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 redstone dust").mins(8, 15)
				.check(c -> c.gained(Items.REDSTONE, 32)).build());
		t.add(sub("BEG_SUB_LAPIS_16", D, TaskCategory.RESOURCE)
				.desc("Obtain 16 lapis lazuli").mins(8, 15)
				.check(c -> c.gained(Items.LAPIS_LAZULI, 16)).build());
		t.add(sub("BEG_SUB_EMERALD_16", D, TaskCategory.TRADING)
				.desc("Obtain 16 emeralds").mins(12, 22)
				.check(c -> c.gained(Items.EMERALD, 16)).build());
		t.add(sub("BEG_SUB_DIAMOND_PICK", D, TaskCategory.CRAFTING)
				.desc("Craft a diamond pickaxe").mins(12, 20)
				.check(c -> c.gained(Items.DIAMOND_PICKAXE)).build());

		// --- Food / farming ---
		t.add(sub("BEG_SUB_COOKED_32", D, TaskCategory.FARMING)
				.desc("Obtain 32 cooked food items").mins(8, 15)
				.check(c -> c.gainedMatching(32, TaskContext.COOKED_FOOD)).build());
		t.add(sub("BEG_SUB_BREED_COWS", D, TaskCategory.ANIMAL)
				.desc("Breed 5 cows").mins(8, 14)
				.check(c -> c.bredAtLeast("minecraft:cow", 5)).build());
		t.add(sub("BEG_SUB_BREED_SHEEP", D, TaskCategory.ANIMAL)
				.desc("Breed 5 sheep").mins(8, 14)
				.check(c -> c.bredAtLeast("minecraft:sheep", 5)).build());
		t.add(sub("BEG_SUB_BREED_PIGS", D, TaskCategory.ANIMAL)
				.desc("Breed 5 pigs").mins(8, 14)
				.check(c -> c.bredAtLeast("minecraft:pig", 5)).build());
		t.add(sub("BEG_SUB_BREED_CHICKENS", D, TaskCategory.ANIMAL)
				.desc("Breed 5 chickens").mins(8, 14)
				.check(c -> c.bredAtLeast("minecraft:chicken", 5)).build());
		t.add(sub("BEG_SUB_WHEAT_32", D, TaskCategory.FARMING)
				.desc("Harvest 32 wheat").mins(8, 15)
				.check(c -> c.gained(Items.WHEAT, 32)).build());
		t.add(sub("BEG_SUB_CARROT_32", D, TaskCategory.FARMING)
				.desc("Harvest 32 carrots").mins(8, 15)
				.check(c -> c.gained(Items.CARROT, 32)).build());
		t.add(sub("BEG_SUB_POTATO_32", D, TaskCategory.FARMING)
				.desc("Harvest 32 potatoes").mins(8, 15)
				.check(c -> c.gained(Items.POTATO, 32)).build());
		t.add(sub("BEG_SUB_PUMPKIN_16", D, TaskCategory.FARMING)
				.desc("Harvest 16 pumpkins").mins(8, 16)
				.check(c -> c.gained(Items.PUMPKIN, 16)).build());
		t.add(sub("BEG_SUB_MELON_16", D, TaskCategory.FARMING)
				.desc("Harvest 16 melon slices").mins(8, 16)
				.check(c -> c.gained(Items.MELON_SLICE, 16)).build());

		// --- Animals ---
		t.add(sub("BEG_SUB_TAME_WOLF", D, TaskCategory.ANIMAL)
				.desc("Tame a wolf").mins(6, 14)
				.check(c -> c.hasTamed("minecraft:wolf")).build());
		t.add(sub("BEG_SUB_TAME_CAT", D, TaskCategory.ANIMAL)
				.desc("Tame a cat").mins(8, 16)
				.check(c -> c.hasTamed("minecraft:cat")).build());
		t.add(sub("BEG_SUB_SADDLED_HORSE", D, TaskCategory.ANIMAL)
				.desc("Tame a horse and equip it with a saddle").mins(10, 18)
				.check(TaskContext::hasSaddledHorse).build());
		t.add(sub("BEG_SUB_GOAT_HORN", D, TaskCategory.ANIMAL)
				.desc("Obtain a goat horn").mins(10, 20)
				.check(c -> c.gained(Items.GOAT_HORN)).build());
		t.add(sub("BEG_SUB_BREED_COW_SHEEP", D, TaskCategory.ANIMAL)
				.desc("Breed a cow and a sheep").mins(6, 12)
				.check(c -> c.allBred("minecraft:cow", "minecraft:sheep")).build());

		// --- Exploration ---
		t.add(sub("BEG_SUB_FIND_VILLAGE", D, TaskCategory.STRUCTURE)
				.desc("Find a village").mins(8, 18)
				.check(c -> c.visitedStructureMatching(id -> id.contains("village"))).build());
		t.add(sub("BEG_SUB_BIOME_DESERT", D, TaskCategory.EXPLORATION)
				.desc("Find a desert biome").mins(6, 15)
				.check(c -> c.visitedBiome("minecraft:desert")).build());
		t.add(sub("BEG_SUB_BIOME_JUNGLE", D, TaskCategory.EXPLORATION)
				.desc("Find a jungle biome").mins(10, 20)
				.check(c -> c.visitedAnyBiome("minecraft:jungle", "minecraft:sparse_jungle",
						"minecraft:bamboo_jungle")).build());
		t.add(sub("BEG_SUB_BIOME_BADLANDS", D, TaskCategory.EXPLORATION)
				.desc("Find a badlands biome").mins(10, 22)
				.check(c -> c.visitedAnyBiome("minecraft:badlands", "minecraft:eroded_badlands",
						"minecraft:wooded_badlands")).build());
		t.add(sub("BEG_SUB_BIOME_TAIGA", D, TaskCategory.EXPLORATION)
				.desc("Find a taiga biome").mins(6, 15)
				.check(c -> c.visitedAnyBiome("minecraft:taiga", "minecraft:snowy_taiga",
						"minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga")).build());
		t.add(sub("BEG_SUB_RUINED_PORTAL", D, TaskCategory.STRUCTURE)
				.desc("Find a ruined portal").mins(6, 15)
				.check(c -> c.visitedStructureMatching(id -> id.contains("ruined_portal"))).build());
		t.add(sub("BEG_SUB_SHIPWRECK", D, TaskCategory.STRUCTURE)
				.desc("Find a shipwreck").mins(8, 18)
				.check(c -> c.visitedStructureMatching(id -> id.contains("shipwreck"))).build());
		t.add(sub("BEG_SUB_DESERT_TEMPLE", D, TaskCategory.STRUCTURE)
				.desc("Find a desert temple").mins(10, 20)
				.check(c -> c.visitedStructure("minecraft:desert_pyramid")).build());
		t.add(sub("BEG_SUB_MINESHAFT", D, TaskCategory.STRUCTURE)
				.desc("Find a mineshaft").mins(8, 18)
				.check(c -> c.visitedStructureMatching(id -> id.contains("mineshaft"))).build());
		t.add(sub("BEG_SUB_DUNGEON", D, TaskCategory.STRUCTURE)
				.desc("Find a dungeon (a room with a monster spawner)").mins(10, 20)
				.check(c -> c.sawBlock("minecraft:spawner")).build());

		// --- Building / utility ---
		t.add(sub("BEG_SUB_ENCHANT_TABLE", D, TaskCategory.CRAFTING)
				.desc("Craft and place an enchanting table").mins(12, 20)
				.check(c -> c.sawBlock("minecraft:enchanting_table")
						|| c.gained(Items.ENCHANTING_TABLE)).build());
		t.add(sub("BEG_SUB_BREWING_STAND", D, TaskCategory.CRAFTING)
				.desc("Craft a brewing stand").mins(12, 22).nether()
				.check(c -> c.gained(Items.BREWING_STAND)).build());
		t.add(sub("BEG_SUB_COMPASS", D, TaskCategory.CRAFTING)
				.desc("Craft a compass").mins(6, 12)
				.check(c -> c.gained(Items.COMPASS)).build());
		t.add(sub("BEG_SUB_CLOCK", D, TaskCategory.CRAFTING)
				.desc("Craft a clock").mins(10, 18)
				.check(c -> c.gained(Items.CLOCK)).build());
		t.add(sub("BEG_SUB_SHIELD", D, TaskCategory.CRAFTING)
				.desc("Craft a shield").mins(4, 10)
				.check(c -> c.gained(Items.SHIELD)).build());

		return t;
	}

	public static List<TaskDefinition> majorTasks() {
		List<TaskDefinition> t = new ArrayList<>();

		t.add(major("BEG_MAJ_IRON_EVERYONE", D, TaskCategory.PROGRESSION)
				.desc("Everyone obtains a full set of iron armour").mins(25, 45).group()
				.check(c -> c.allInnocentsWearing(Items.IRON_HELMET, Items.IRON_CHESTPLATE,
						Items.IRON_LEGGINGS, Items.IRON_BOOTS)).build());
		t.add(major("BEG_MAJ_DIAMOND_32", D, TaskCategory.RESOURCE)
				.desc("Obtain 32 diamonds").mins(30, 55)
				.check(c -> c.gained(Items.DIAMOND, 32)).build());
		t.add(major("BEG_MAJ_ENCHANT_SETUP", D, TaskCategory.PROGRESSION)
				.desc("Build a full enchanting setup: an enchanting table and 15 bookshelves")
				.mins(30, 50)
				.check(c -> (c.sawBlock("minecraft:enchanting_table")
						|| c.gained(Items.ENCHANTING_TABLE)) && c.gained(Items.BOOKSHELF, 15)).build());
		t.add(major("BEG_MAJ_NETHER_PORTAL", D, TaskCategory.NETHER)
				.desc("Construct and activate a Nether portal, then step through").mins(25, 45).nether()
				.check(c -> c.advancementEarned("minecraft:story/enter_the_nether")).build());
		t.add(major("BEG_MAJ_EMERALD_64", D, TaskCategory.TRADING)
				.desc("Obtain 64 emeralds by any legitimate survival method").mins(35, 60)
				.check(c -> c.gained(Items.EMERALD, 64)).build());
		t.add(major("BEG_MAJ_FOOD_HOARD", D, TaskCategory.FARMING)
				.desc("Stockpile 512 food items between the group").mins(30, 50)
				.check(c -> c.gainedMatching(512,
						item -> item.getComponents().get(DataComponentTypes.FOOD) != null)).build());
		t.add(major("BEG_MAJ_FLOWERS_10", D, TaskCategory.COLLECTION)
				.desc("Obtain 10 different types of flower").mins(25, 45)
				.check(c -> c.distinctGainedInTag(ItemTags.FLOWERS) >= 10).build());
		t.add(major("BEG_MAJ_BREED_FIVE", D, TaskCategory.ANIMAL)
				.desc("Breed cows, sheep, pigs, chickens and rabbits at least once each")
				.mins(30, 50)
				.check(c -> c.allBred("minecraft:cow", "minecraft:sheep", "minecraft:pig",
						"minecraft:chicken", "minecraft:rabbit")).build());
		t.add(major("BEG_MAJ_THREE_STRUCTURES", D, TaskCategory.STRUCTURE)
				.desc("Visit three different generated structures").mins(30, 50)
				.check(c -> c.distinctStructuresVisited() >= 3).build());
		t.add(major("BEG_MAJ_DIAMOND_KIT", D, TaskCategory.PROGRESSION)
				.desc("Obtain a diamond pickaxe, a diamond sword and full diamond armour")
				.mins(35, 60)
				.check(c -> c.gained(Items.DIAMOND_PICKAXE) && c.gained(Items.DIAMOND_SWORD)
						&& c.gained(Items.DIAMOND_HELMET) && c.gained(Items.DIAMOND_CHESTPLATE)
						&& c.gained(Items.DIAMOND_LEGGINGS) && c.gained(Items.DIAMOND_BOOTS)).build());
		t.add(major("BEG_MAJ_TRADES_10", D, TaskCategory.TRADING)
				.desc("Set up villager trading and complete 10 trades").mins(25, 45)
				.check(c -> c.getVillagerTrades() >= 10).build());
		t.add(major("BEG_MAJ_DISCS_4", D, TaskCategory.COLLECTION)
				.desc("Collect 4 different music discs").mins(30, 55)
				.check(c -> c.distinctGainedMatching(TaskContext.MUSIC_DISC) >= 4).build());
		t.add(major("BEG_MAJ_ENDER_PEARLS_16", D, TaskCategory.COMBAT)
				.desc("Obtain 16 ender pearls").mins(30, 50)
				.check(c -> c.gained(Items.ENDER_PEARL, 16)).build());
		t.add(major("BEG_MAJ_BREW_3", D, TaskCategory.NETHER)
				.desc("Obtain a brewing stand and brew potions").mins(30, 55).nether()
				.check(c -> c.gained(Items.BREWING_STAND)
						&& c.advancementEarned("minecraft:nether/brew_potion")).build());
		t.add(major("BEG_MAJ_ORES_10", D, TaskCategory.RESOURCE)
				.desc("Obtain 10 different underground ore materials").mins(30, 50)
				.check(c -> {
					int found = 0;
					for (Item item : new Item[]{Items.COAL, Items.RAW_IRON, Items.RAW_COPPER,
							Items.RAW_GOLD, Items.DIAMOND, Items.EMERALD, Items.REDSTONE,
							Items.LAPIS_LAZULI, Items.QUARTZ, Items.AMETHYST_SHARD,
							Items.ANCIENT_DEBRIS}) {
						if (c.gained(item)) {
							found++;
						}
					}
					return found >= 10;
				}).build());

		return t;
	}
}
