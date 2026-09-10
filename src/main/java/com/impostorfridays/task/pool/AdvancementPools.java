package com.impostorfridays.task.pool;

import com.impostorfridays.task.AdvancementTask;
import com.impostorfridays.task.Difficulty;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Advancement pools for the five-advancement format.
 *
 * <p><b>Bracketing.</b> Each tier is deliberately kept narrow so that any random five are of
 * comparable weight. That rule forced a few advancements to move tier from the original design
 * notes, because they were dramatically harder than everything sitting beside them:
 *
 * <ul>
 *   <li><b>Two by Two</b> (breed every animal, including all frog and horse variants) and
 *       <b>A Complete Catalogue</b> (tame all 11 cat variants) are among the hardest in the game.
 *       Both are MASTER only.</li>
 *   <li><b>A Balanced Diet</b> requires eating an enchanted golden apple, so it depends on
 *       finding one — EXPERT and MASTER only.</li>
 *   <li><b>Serious Dedication</b> (netherite hoe) means spending a netherite ingot on a hoe;
 *       EXPERT and MASTER only.</li>
 *   <li><b>A Furious Cocktail</b> / <b>How Did We Get Here</b>-style effect stacking needs a
 *       near-complete brewing setup, so it stays at EXPERT and above.</li>
 * </ul>
 *
 * <p>Leaving any of those in Beginner or Standard would mean one draw of five was a comfortable
 * evening and the next was impossible, which is exactly what the bracketing rule exists to stop.
 */
public final class AdvancementPools {

	private AdvancementPools() {
	}

	private static final Map<Difficulty, List<AdvancementTask>> POOLS = new EnumMap<>(Difficulty.class);

	static {
		// --- BEGINNER: early survival, all reachable in the first half hour ---
		POOLS.put(Difficulty.BEGINNER, List.of(
				AdvancementTask.of("story/mine_stone", "Stone Age"),
				AdvancementTask.of("story/upgrade_tools", "Getting an Upgrade"),
				AdvancementTask.of("story/smelt_iron", "Acquire Hardware"),
				AdvancementTask.of("story/obtain_armor", "Suit Up"),
				AdvancementTask.of("story/lava_bucket", "Hot Stuff"),
				AdvancementTask.of("story/iron_tools", "Isn't It Iron Pick"),
				AdvancementTask.of("story/deflect_arrow", "Not Today, Thank You"),
				AdvancementTask.of("story/mine_diamond", "Diamonds!"),
				AdvancementTask.of("adventure/sleep_in_bed", "Sweet Dreams"),
				AdvancementTask.of("adventure/kill_a_mob", "Monster Hunter"),
				AdvancementTask.of("adventure/shoot_arrow", "Take Aim"),
				AdvancementTask.of("adventure/ol_betsy", "Ol' Betsy"),
				AdvancementTask.of("adventure/honey_block_slide", "Sticky Situation"),
				AdvancementTask.of("husbandry/plant_seed", "A Seedy Place"),
				AdvancementTask.of("husbandry/breed_an_animal", "The Parrots and the Bats"),
				AdvancementTask.of("husbandry/tame_an_animal", "Best Friends Forever"),
				AdvancementTask.of("husbandry/fishy_business", "Fishy Business"),
				AdvancementTask.of("husbandry/safely_harvest_honey", "Bee Our Guest"),
				AdvancementTask.of("husbandry/make_a_sign_glow", "Glow and Behold!"),
				AdvancementTask.of("husbandry/wax_on", "Wax On")
		));

		// --- STANDARD: needs the Nether, a village, or a real expedition ---
		POOLS.put(Difficulty.STANDARD, List.of(
				AdvancementTask.of("story/enter_the_nether", "We Need to Go Deeper"),
				AdvancementTask.of("story/enchant_item", "Enchanter"),
				AdvancementTask.of("story/form_obsidian", "Ice Bucket Challenge"),
				AdvancementTask.of("story/shiny_gear", "Cover Me with Diamonds"),
				AdvancementTask.of("nether/find_fortress", "A Terrible Fortress"),
				AdvancementTask.of("nether/obtain_blaze_rod", "Into Fire"),
				AdvancementTask.of("nether/brew_potion", "Local Brewery"),
				AdvancementTask.of("nether/ride_strider", "This Boat Has Legs"),
				AdvancementTask.of("nether/distract_piglin", "Oh Shiny"),
				AdvancementTask.of("adventure/trade", "What a Deal!"),
				AdvancementTask.of("adventure/sniper_duel", "Sniper Duel"),
				AdvancementTask.of("adventure/summon_iron_golem", "Hired Help"),
				AdvancementTask.of("adventure/throw_trident", "A Throwaway Joke"),
				AdvancementTask.of("adventure/voluntary_exile", "Voluntary Exile"),
				AdvancementTask.of("adventure/salvage_sherd", "Respecting the Remnants"),
				AdvancementTask.of("husbandry/tactical_fishing", "Tactical Fishing"),
				AdvancementTask.of("husbandry/axolotl_in_a_bucket", "The Cutest Predator"),
				AdvancementTask.of("husbandry/tadpole_in_a_bucket", "Bukkit Bukkit"),
				AdvancementTask.of("husbandry/wax_off", "Wax Off"),
				AdvancementTask.of("husbandry/ride_a_boat_with_a_goat", "Whatever Floats Your Goat!")
		));

		// --- ADVANCED: multi-step projects and dangerous places ---
		POOLS.put(Difficulty.ADVANCED, List.of(
				AdvancementTask.of("story/follow_ender_eye", "Eye Spy"),
				AdvancementTask.of("story/cure_zombie_villager", "Zombie Doctor"),
				AdvancementTask.of("nether/find_bastion", "Those Were the Days"),
				AdvancementTask.of("nether/loot_bastion", "War Pigs"),
				AdvancementTask.of("nether/obtain_ancient_debris", "Hidden in the Depths"),
				AdvancementTask.of("nether/obtain_crying_obsidian", "Who is Cutting Onions?"),
				AdvancementTask.of("nether/charge_respawn_anchor", "Not Quite \"Nine\" Lives"),
				AdvancementTask.of("nether/return_to_sender", "Return to Sender"),
				AdvancementTask.of("nether/fast_travel", "Subspace Bubble"),
				AdvancementTask.of("nether/get_wither_skull", "Spooky Scary Skeleton"),
				AdvancementTask.of("nether/uneasy_alliance", "Uneasy Alliance"),
				AdvancementTask.of("adventure/totem_of_undying", "Postmortal"),
				AdvancementTask.of("adventure/hero_of_the_village", "Hero of the Village"),
				AdvancementTask.of("adventure/whos_the_pillager_now", "Who's the Pillager Now?"),
				AdvancementTask.of("adventure/two_birds_one_arrow", "Two Birds, One Arrow"),
				AdvancementTask.of("adventure/arbalistic", "Arbalistic"),
				AdvancementTask.of("adventure/bullseye", "Bullseye"),
				AdvancementTask.of("adventure/very_very_frightening", "Very Very Frightening"),
				AdvancementTask.of("husbandry/froglights", "With Our Powers Combined!"),
				AdvancementTask.of("husbandry/silk_touch_nest", "Total Beelocation")
		));

		// --- EXPERT: the End, the deep dark, and long brewing chains ---
		POOLS.put(Difficulty.EXPERT, List.of(
				AdvancementTask.of("story/enter_the_end", "The End?"),
				AdvancementTask.of("nether/create_beacon", "Bring Home the Beacon"),
				AdvancementTask.of("nether/summon_wither", "Withering Heights"),
				AdvancementTask.of("nether/netherite_armor", "Cover Me in Debris"),
				AdvancementTask.of("nether/all_potions", "A Furious Cocktail"),
				AdvancementTask.of("nether/explore_nether", "Hot Tourist Destinations"),
				AdvancementTask.of("end/kill_dragon", "Free the End"),
				AdvancementTask.of("end/enter_end_gateway", "Remote Getaway"),
				AdvancementTask.of("end/dragon_breath", "You Need a Mint"),
				AdvancementTask.of("end/find_end_city", "The City at the End of the Game"),
				AdvancementTask.of("end/levitate", "Great View From Up Here"),
				AdvancementTask.of("adventure/adventuring_time", "Adventuring Time"),
				AdvancementTask.of("adventure/trim_with_any_armor_pattern", "Crafting a New Look"),
				AdvancementTask.of("adventure/under_lock_and_key", "Under Lock and Key"),
				AdvancementTask.of("adventure/craft_decorated_pot_using_only_sherds", "Careful Restoration"),
				AdvancementTask.of("adventure/kill_mob_near_sculk_catalyst", "It Spreads"),
				AdvancementTask.of("adventure/avoid_vibration", "Sneak 100"),
				AdvancementTask.of("husbandry/balanced_diet", "A Balanced Diet"),
				AdvancementTask.of("husbandry/obtain_netherite_hoe", "Serious Dedication"),
				AdvancementTask.of("husbandry/leash_all_frog_variants", "When the Squad Hops into Town")
		));

		// --- MASTER: the genuinely enormous ones ---
		POOLS.put(Difficulty.MASTER, List.of(
				AdvancementTask.of("nether/create_full_beacon", "Beaconator"),
				AdvancementTask.of("nether/all_effects", "How Did We Get Here?"),
				AdvancementTask.of("nether/all_potions", "A Furious Cocktail"),
				AdvancementTask.of("nether/netherite_armor", "Cover Me in Debris"),
				AdvancementTask.of("end/respawn_dragon", "The End... Again..."),
				AdvancementTask.of("end/dragon_egg", "The Next Generation"),
				AdvancementTask.of("end/elytra", "Sky's the Limit"),
				AdvancementTask.of("end/kill_dragon", "Free the End"),
				AdvancementTask.of("adventure/adventuring_time", "Adventuring Time"),
				AdvancementTask.of("adventure/trim_with_all_exclusive_armor_patterns", "Smithing with Style"),
				AdvancementTask.of("adventure/overoverkill", "Over-Overkill"),
				AdvancementTask.of("adventure/arbalistic", "Arbalistic"),
				AdvancementTask.of("adventure/kill_all_mobs", "Monsters Hunted"),
				AdvancementTask.of("husbandry/bred_all_animals", "Two by Two"),
				AdvancementTask.of("husbandry/complete_catalogue", "A Complete Catalogue"),
				AdvancementTask.of("husbandry/balanced_diet", "A Balanced Diet"),
				AdvancementTask.of("husbandry/obtain_netherite_hoe", "Serious Dedication"),
				AdvancementTask.of("husbandry/whole_pack", "The Whole Pack")
		));
	}

	public static List<AdvancementTask> pool(Difficulty difficulty) {
		return POOLS.getOrDefault(difficulty, List.of());
	}
}
