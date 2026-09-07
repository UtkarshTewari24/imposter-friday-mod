package com.impostorfridays.task;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.config.GameConfig;
import com.impostorfridays.game.GameManager;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs the shared task for the current match.
 *
 * <p>Completion is polled on a timer rather than hooked into a dozen game events, which keeps
 * the {@link TaskDefinition} predicates simple and side-effect free.
 *
 * <p><b>Advancements are match-scoped.</b> A baseline of every already-earned advancement is
 * captured at {@code /start}, and only advancements completed after that count. Without this,
 * a task like "enter the Nether" would complete instantly on a world where somebody had done
 * it in a previous round.
 */
public final class TaskManager {

	/** How often completion is evaluated and the world is scanned. */
	private static final int POLL_INTERVAL = 40;

	private static TaskContext context;
	private static TaskDefinition activeTask;
	private static Set<String> advancementBaseline = new HashSet<>();
	private static int tickCounter;
	private static boolean completed;

	private TaskManager() {
	}

	public static TaskDefinition getActiveTask() {
		return activeTask;
	}

	// ------------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------------

	public static void start(MinecraftServer server) {
		context = new TaskContext(server);
		completed = false;
		tickCounter = 0;

		activeTask = TaskPools.random(GameConfig.get().getDifficulty());
		if (activeTask == null) {
			ImpostorFridays.LOGGER.warn("No tasks defined for difficulty {}",
					GameConfig.get().getDifficulty());
			GameManager.setTaskText("");
			return;
		}

		advancementBaseline = snapshotAdvancements(server);
		GameManager.setTaskText(activeTask.description());
		GameManager.setTaskComplete(false);

		ImpostorFridays.LOGGER.info("Task for this match [{}]: {}",
				activeTask.id(), activeTask.description());
	}

	public static void end() {
		context = null;
		activeTask = null;
		advancementBaseline = new HashSet<>();
		completed = false;
		tickCounter = 0;
	}

	/** Every advancement any player has already completed, so prior rounds do not count. */
	private static Set<String> snapshotAdvancements(MinecraftServer server) {
		Set<String> done = new HashSet<>();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			for (AdvancementEntry entry : server.getAdvancementLoader().getAdvancements()) {
				if (player.getAdvancementTracker().getProgress(entry).isDone()) {
					done.add(entry.id().toString());
				}
			}
		}
		return done;
	}

	// ------------------------------------------------------------------
	// Event recording
	// ------------------------------------------------------------------

	/** Records a mob killed by a player, and a player's own cause of death. */
	public static void onDeath(LivingEntity entity, DamageSource source) {
		if (context == null) {
			return;
		}
		if (entity instanceof PlayerEntity) {
			source.getTypeRegistryEntry().getKey()
					.ifPresent(key -> context.playerDeathCauses.add(key.getValue().toString()));
			return;
		}
		// Only count mobs a player actually killed.
		if (source.getAttacker() instanceof PlayerEntity) {
			context.mobsKilled.add(EntityType.getId(entity.getType()).toString());
		}
	}

	// ------------------------------------------------------------------
	// Polling
	// ------------------------------------------------------------------

	public static void tick(MinecraftServer server) {
		if (context == null || activeTask == null || completed || !GameManager.isActive()) {
			return;
		}
		if (++tickCounter < POLL_INTERVAL) {
			return;
		}
		tickCounter = 0;

		refreshAdvancements(server);
		scanForBabyAnimals(server);

		if (activeTask.check().isComplete(context)) {
			completed = true;
			GameManager.setTaskComplete(true);
			GameManager.broadcast(server, Text.literal("✔ TASK COMPLETE — ")
					.formatted(Formatting.GREEN, Formatting.BOLD)
					.append(Text.literal(activeTask.completionMessage())
							.formatted(Formatting.GREEN)));
			GameManager.broadcast(server, Text.literal("The Innocents win!")
					.formatted(Formatting.GOLD, Formatting.BOLD));
			GameManager.end(server);
		}
	}

	/** Adds advancements completed since the match began. */
	private static void refreshAdvancements(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			for (AdvancementEntry entry : server.getAdvancementLoader().getAdvancements()) {
				String id = entry.id().toString();
				if (advancementBaseline.contains(id) || context.newAdvancements.contains(id)) {
					continue;
				}
				if (player.getAdvancementTracker().getProgress(entry).isDone()) {
					context.newAdvancements.add(id);
				}
			}
		}
	}

	/**
	 * Records baby animals currently alive.
	 *
	 * <p>Simpler and more reliable than hooking breeding events, and it naturally covers
	 * any route to a baby animal the players find.
	 */
	private static void scanForBabyAnimals(MinecraftServer server) {
		for (ServerWorld world : server.getWorlds()) {
			for (Entity entity : world.iterateEntities()) {
				if (entity instanceof PassiveEntity passive && passive.isBaby()) {
					context.babyAnimals.add(EntityType.getId(entity.getType()).toString());
				}
			}
		}
	}
}
