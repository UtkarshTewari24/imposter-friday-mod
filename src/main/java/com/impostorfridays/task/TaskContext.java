package com.impostorfridays.task;

import com.impostorfridays.game.GameManager;
import com.impostorfridays.game.GameState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Everything a task's completion check can look at.
 *
 * <p>Event-based facts (advancements, kills, deaths, baby animals) are recorded by
 * {@link TaskManager} and are always scoped to the CURRENT match — an advancement a player
 * earned in a previous round on the same world never counts.
 */
public final class TaskContext {

	private final MinecraftServer server;

	/** Advancements newly completed since this match started. */
	final Set<String> newAdvancements = new HashSet<>();
	/** Entity type ids killed by a player this match. */
	final Set<String> mobsKilled = new HashSet<>();
	/** Damage type ids that killed a player this match. */
	final Set<String> playerDeathCauses = new HashSet<>();
	/** Entity type ids of baby animals seen alive this match (i.e. successfully bred). */
	final Set<String> babyAnimals = new HashSet<>();

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

	public boolean allAdvancementsEarned(String... ids) {
		for (String id : ids) {
			if (!newAdvancements.contains(id)) {
				return false;
			}
		}
		return true;
	}

	public boolean mobKilled(String entityId) {
		return mobsKilled.contains(entityId);
	}

	public boolean allMobsKilled(String... entityIds) {
		for (String id : entityIds) {
			if (!mobsKilled.contains(id)) {
				return false;
			}
		}
		return true;
	}

	public boolean diedOf(String damageTypeId) {
		return playerDeathCauses.contains(damageTypeId);
	}

	public boolean bred(String entityId) {
		return babyAnimals.contains(entityId);
	}

	public boolean allBred(String... entityIds) {
		for (String id : entityIds) {
			if (!babyAnimals.contains(id)) {
				return false;
			}
		}
		return true;
	}

	// ------------------------------------------------------------------
	// Live world queries
	// ------------------------------------------------------------------

	/** Players who count toward "everyone" objectives — the Impostor is excluded. */
	public List<ServerPlayerEntity> innocentPlayers() {
		GameState state = GameManager.getState();
		List<ServerPlayerEntity> result = new ArrayList<>();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (state == null || !state.isImpostor(player.getUuid())) {
				result.add(player);
			}
		}
		return result;
	}

	public List<ServerPlayerEntity> allPlayers() {
		return server.getPlayerManager().getPlayerList();
	}

	/** True if any player is currently carrying at least one of this item. */
	public boolean anyPlayerHas(Item item) {
		return totalCount(item) > 0;
	}

	/** Total count of an item across every player's inventory. */
	public int totalCount(Item item) {
		int total = 0;
		for (ServerPlayerEntity player : allPlayers()) {
			PlayerInventory inv = player.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isOf(item)) {
					total += stack.getCount();
				}
			}
		}
		return total;
	}

	/** True if every Innocent is wearing all four of the given armour pieces. */
	public boolean allInnocentsWearing(Item helmet, Item chest, Item legs, Item boots) {
		List<ServerPlayerEntity> players = innocentPlayers();
		if (players.isEmpty()) {
			return false;
		}
		for (ServerPlayerEntity player : players) {
			if (!player.getEquippedStack(EquipmentSlot.HEAD).isOf(helmet)
					|| !player.getEquippedStack(EquipmentSlot.CHEST).isOf(chest)
					|| !player.getEquippedStack(EquipmentSlot.LEGS).isOf(legs)
					|| !player.getEquippedStack(EquipmentSlot.FEET).isOf(boots)) {
				return false;
			}
		}
		return true;
	}

	/** True if any single Innocent is wearing all four pieces. */
	public boolean anyInnocentWearing(Item helmet, Item chest, Item legs, Item boots) {
		for (ServerPlayerEntity player : innocentPlayers()) {
			if (player.getEquippedStack(EquipmentSlot.HEAD).isOf(helmet)
					&& player.getEquippedStack(EquipmentSlot.CHEST).isOf(chest)
					&& player.getEquippedStack(EquipmentSlot.LEGS).isOf(legs)
					&& player.getEquippedStack(EquipmentSlot.FEET).isOf(boots)) {
				return true;
			}
		}
		return false;
	}

	/** True if any player holds a shulker box with all 27 slots occupied. */
	public boolean anyFullShulkerBox() {
		for (ServerPlayerEntity player : allPlayers()) {
			PlayerInventory inv = player.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ItemStack stack = inv.getStack(i);
				ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
				if (container == null) {
					continue;
				}
				long filled = container.streamNonEmpty().count();
				if (filled >= 27) {
					return true;
				}
			}
		}
		return false;
	}
}
