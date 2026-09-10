package com.impostorfridays.task;

import com.impostorfridays.game.GameManager;
import com.impostorfridays.game.GameState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Everything a task's completion check can look at.
 *
 * <p>Two rules hold throughout, because the world persists indefinitely between matches:
 * <ul>
 *   <li>Event facts (advancements, kills, breeding, biomes, structures) are recorded only for
 *       the CURRENT match.</li>
 *   <li>Item objectives measure the INCREASE since {@code /start}, never the raw amount held —
 *       otherwise "obtain a goat horn" completes the instant anyone still has one from an
 *       earlier round.</li>
 * </ul>
 */
public final class TaskContext {

	private final MinecraftServer server;

	// --- recorded events, all match-scoped ---
	final Set<String> newAdvancements = new HashSet<>();
	final Map<String, Integer> killCounts = new HashMap<>();
	/** Baby animals seen, tracked by entity uuid so repeat sightings aren't double counted. */
	final Map<String, Set<UUID>> babiesByType = new HashMap<>();
	final Set<String> biomesVisited = new HashSet<>();
	final Set<String> structuresVisited = new HashSet<>();
	final Set<String> playerDeathCauses = new HashSet<>();
	/** Consecutive mob kills per player, reset whenever that player dies. */
	final Map<UUID, Integer> killStreak = new HashMap<>();
	int villagerTrades;
	/** Entity type ids a player has tamed this match. */
	final Set<String> tamedTypes = new HashSet<>();
	/** True once a tamed, saddled horse exists. */
	boolean saddledHorse;
	/** Block ids seen close to a player, used for "find a dungeon"-style objectives. */
	final Set<String> nearbyBlocks = new HashSet<>();
	/** Hostile kills made while the killer wore no armour at all. */
	int noArmourKills;
	/** Distinct weapon item ids used to land a killing blow. */
	final Set<String> killWeapons = new HashSet<>();

	// --- baselines so nothing carried over from a previous match counts ---
	final Map<Item, Integer> itemBaseline = new HashMap<>();
	final Set<UUID> baselinedPlayers = new HashSet<>();

	TaskContext(MinecraftServer server) {
		this.server = server;
	}

	public MinecraftServer getServer() {
		return server;
	}

	// ------------------------------------------------------------------
	// Recorded events
	// ------------------------------------------------------------------

	public boolean advancementEarned(String id) {
		return newAdvancements.contains(id);
	}

	public boolean killed(String entityId) {
		return killedAtLeast(entityId, 1);
	}

	public boolean killedAtLeast(String entityId, int count) {
		return killCounts.getOrDefault(entityId, 0) >= count;
	}

	/** How many DIFFERENT hostile types have been killed this match. */
	public int distinctMobTypesKilled() {
		return killCounts.size();
	}

	public boolean allKilled(String... entityIds) {
		for (String id : entityIds) {
			if (!killed(id)) {
				return false;
			}
		}
		return true;
	}

	/** Number of babies of this type seen alive, i.e. successful breedings. */
	public int bredCount(String entityId) {
		return babiesByType.getOrDefault(entityId, Set.of()).size();
	}

	public boolean bred(String entityId) {
		return bredCount(entityId) > 0;
	}

	public boolean bredAtLeast(String entityId, int count) {
		return bredCount(entityId) >= count;
	}

	public boolean allBred(String... entityIds) {
		for (String id : entityIds) {
			if (!bred(id)) {
				return false;
			}
		}
		return true;
	}

	/** How many different species have been bred this match. */
	public int distinctSpeciesBred() {
		return (int) babiesByType.values().stream().filter(s -> !s.isEmpty()).count();
	}

	public boolean visitedBiome(String biomeId) {
		return biomesVisited.contains(biomeId);
	}

	public boolean visitedAnyBiome(String... biomeIds) {
		for (String id : biomeIds) {
			if (biomesVisited.contains(id)) {
				return true;
			}
		}
		return false;
	}

	public boolean visitedStructure(String structureId) {
		return structuresVisited.contains(structureId);
	}

	/** Matches structure families with several variants, e.g. every kind of village. */
	public boolean visitedStructureMatching(Predicate<String> matcher) {
		return structuresVisited.stream().anyMatch(matcher);
	}

	public int distinctStructuresVisited() {
		return structuresVisited.size();
	}

	public boolean hasTamed(String entityId) {
		return tamedTypes.contains(entityId);
	}

	public int distinctTamed() {
		return tamedTypes.size();
	}

	public boolean hasSaddledHorse() {
		return saddledHorse;
	}

	/** True if a player has been near this block this match, e.g. a spawner for a dungeon. */
	public boolean sawBlock(String blockId) {
		return nearbyBlocks.contains(blockId);
	}

	public int distinctBiomesVisited() {
		return biomesVisited.size();
	}

	/** How many of the listed structures have been entered this match. */
	public int structuresVisitedAmong(String... structureIds) {
		int found = 0;
		for (String id : structureIds) {
			if (structuresVisited.contains(id)) {
				found++;
			}
		}
		return found;
	}

	public boolean diedOf(String damageTypeId) {
		return playerDeathCauses.contains(damageTypeId);
	}

	public int getVillagerTrades() {
		return villagerTrades;
	}

	public int getNoArmourKills() {
		return noArmourKills;
	}

	/** How many different weapons have landed a killing blow this match. */
	public int distinctKillWeapons() {
		return killWeapons.size();
	}

	public boolean killedWith(String itemId) {
		return killWeapons.contains(itemId);
	}

	/** The best current run of kills without dying, across all players. */
	public int bestKillStreak() {
		return killStreak.values().stream().mapToInt(Integer::intValue).max().orElse(0);
	}

	// ------------------------------------------------------------------
	// Players
	// ------------------------------------------------------------------

	public List<ServerPlayerEntity> allPlayers() {
		return server.getPlayerManager().getPlayerList();
	}

	/** Players who count toward "everyone" objectives — the Impostor is always excluded. */
	public List<ServerPlayerEntity> innocentPlayers() {
		GameState state = GameManager.getState();
		List<ServerPlayerEntity> result = new ArrayList<>();
		for (ServerPlayerEntity player : allPlayers()) {
			if (state == null || !state.isImpostor(player.getUuid())) {
				result.add(player);
			}
		}
		return result;
	}

	// ------------------------------------------------------------------
	// Items — always measured as a gain since /start
	// ------------------------------------------------------------------

	public int totalCount(Item item) {
		int total = 0;
		for (ServerPlayerEntity player : allPlayers()) {
			total += countIn(player, stack -> stack.isOf(item));
		}
		return total;
	}

	public boolean gained(Item item) {
		return gained(item, 1);
	}

	public boolean gained(Item item, int amount) {
		return totalCount(item) - itemBaseline.getOrDefault(item, 0) >= amount;
	}

	/** Total gained across any of the listed items combined, e.g. "32 cooked food items". */
	public boolean gainedAnyOf(int amount, Item... items) {
		int total = 0;
		for (Item item : items) {
			total += totalCount(item) - itemBaseline.getOrDefault(item, 0);
		}
		return total >= amount;
	}

	/** Total gained across every item matching a predicate, e.g. all cooked foods. */
	public boolean gainedMatching(int amount, Predicate<Item> matcher) {
		int total = 0;
		for (Item item : Registries.ITEM) {
			if (matcher.test(item)) {
				total += Math.max(0, totalCount(item) - itemBaseline.getOrDefault(item, 0));
			}
		}
		return total >= amount;
	}

	/** How many DIFFERENT items in a tag have been newly obtained, e.g. 10 kinds of flower. */
	public int distinctGainedInTag(TagKey<Item> tag) {
		int distinct = 0;
		for (Item item : Registries.ITEM) {
			if (item.getDefaultStack().isIn(tag)
					&& totalCount(item) - itemBaseline.getOrDefault(item, 0) > 0) {
				distinct++;
			}
		}
		return distinct;
	}

	/** How many DIFFERENT matching items have been newly obtained, e.g. music discs. */
	public int distinctGainedMatching(Predicate<Item> matcher) {
		int distinct = 0;
		for (Item item : Registries.ITEM) {
			if (matcher.test(item)
					&& totalCount(item) - itemBaseline.getOrDefault(item, 0) > 0) {
				distinct++;
			}
		}
		return distinct;
	}

	/** Matches by registry id path, used for families with no vanilla tag. */
	public static Predicate<Item> idPath(Predicate<String> pathMatcher) {
		return item -> {
			Identifier id = Registries.ITEM.getId(item);
			return id != null && pathMatcher.test(id.getPath());
		};
	}

	public static final Predicate<Item> MUSIC_DISC = idPath(p -> p.startsWith("music_disc_"));
	public static final Predicate<Item> TRIM_TEMPLATE =
			idPath(p -> p.endsWith("_armor_trim_smithing_template"));
	public static final Predicate<Item> POTTERY_SHERD = idPath(p -> p.endsWith("_pottery_sherd"));
	public static final Predicate<Item> COOKED_FOOD = idPath(p -> p.startsWith("cooked_")
			|| p.equals("bread") || p.equals("baked_potato") || p.equals("mushroom_stew")
			|| p.equals("rabbit_stew") || p.equals("beetroot_soup") || p.equals("golden_carrot")
			|| p.equals("pumpkin_pie") || p.equals("dried_kelp"));

	/** Distinct goat horn instruments held, since all variants share one item id. */
	public int distinctGoatHorns() {
		Set<String> instruments = new HashSet<>();
		for (ServerPlayerEntity player : allPlayers()) {
			PlayerInventory inv = player.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ItemStack stack = inv.getStack(i);
				var instrument = stack.get(DataComponentTypes.INSTRUMENT);
				if (instrument != null) {
					instruments.add(instrument.toString());
				}
			}
		}
		return instruments.size();
	}

	// ------------------------------------------------------------------
	// Gear
	// ------------------------------------------------------------------

	/** True if every Innocent is wearing all four given armour pieces. */
	public boolean allInnocentsWearing(Item helmet, Item chest, Item legs, Item boots) {
		List<ServerPlayerEntity> players = innocentPlayers();
		if (players.isEmpty()) {
			return false;
		}
		for (ServerPlayerEntity player : players) {
			if (!isWearing(player, helmet, chest, legs, boots)) {
				return false;
			}
		}
		return true;
	}

	/** True if at least {@code count} Innocents are wearing the full set. */
	public boolean innocentsWearing(int count, Item helmet, Item chest, Item legs, Item boots) {
		int wearing = 0;
		for (ServerPlayerEntity player : innocentPlayers()) {
			if (isWearing(player, helmet, chest, legs, boots)) {
				wearing++;
			}
		}
		return wearing >= count;
	}

	private static boolean isWearing(ServerPlayerEntity player, Item helmet, Item chest,
			Item legs, Item boots) {
		return player.getEquippedStack(EquipmentSlot.HEAD).isOf(helmet)
				&& player.getEquippedStack(EquipmentSlot.CHEST).isOf(chest)
				&& player.getEquippedStack(EquipmentSlot.LEGS).isOf(legs)
				&& player.getEquippedStack(EquipmentSlot.FEET).isOf(boots);
	}

	/** True if any player holds a shulker box with all 27 slots occupied. */
	public boolean anyFullShulkerBox() {
		return anyShulkerBoxWhere(container -> container.streamNonEmpty().count() >= 27);
	}

	/** True if any shulker box satisfies the given test on its contents. */
	public boolean anyShulkerBoxWhere(Predicate<ContainerComponent> test) {
		for (ServerPlayerEntity player : allPlayers()) {
			PlayerInventory inv = player.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ContainerComponent container = inv.getStack(i).get(DataComponentTypes.CONTAINER);
				if (container != null && test.test(container)) {
					return true;
				}
			}
		}
		return false;
	}

	/** True if any shulker box is full of a single item type. */
	public boolean anyShulkerFullOfOneItem() {
		return anyShulkerBoxWhere(container -> {
			List<ItemStack> stacks = container.stream().filter(s -> !s.isEmpty()).toList();
			if (stacks.size() < 27) {
				return false;
			}
			Item first = stacks.get(0).getItem();
			return stacks.stream().allMatch(s -> s.isOf(first));
		});
	}

	/** True if any shulker box is full of one specific item. */
	public boolean anyShulkerFullOf(Item item) {
		return anyShulkerBoxWhere(container ->
				container.stream().filter(s -> s.isOf(item)).count() >= 27);
	}

	private static int countIn(ServerPlayerEntity player, Predicate<ItemStack> matcher) {
		int total = 0;
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if (!stack.isEmpty() && matcher.test(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	/** Folds a player's current inventory into the baseline. Called once per player. */
	void addToBaseline(ServerPlayerEntity player) {
		if (!baselinedPlayers.add(player.getUuid())) {
			return;
		}
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if (!stack.isEmpty()) {
				itemBaseline.merge(stack.getItem(), stack.getCount(), Integer::sum);
			}
		}
	}
}
