package com.impostorfridays.task;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.config.GameConfig;
import com.impostorfridays.game.GameManager;
import com.impostorfridays.net.GameSyncS2C;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.List;
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
	/** Every objective for this match. One entry for RANDOM, several for a preset set. */
	private static List<TaskDefinition> activeTasks = List.of();
	/** Ids of objectives already finished, so each is announced exactly once. */
	private static final Set<String> completedTaskIds = new HashSet<>();
	private static TaskSet activeSet;
	/**
	 * Per-player snapshot of advancements already earned before this match counted.
	 *
	 * <p>Keyed by player because a player who joins mid-match brings their own history with them.
	 * A single shared baseline taken at /start would treat every advancement a late joiner
	 * already had as "newly earned", instantly completing tasks like "enter the Nether".
	 */
	private static Map<UUID, Set<String>> advancementBaseline = new HashMap<>();
	private static int tickCounter;
	private static boolean completed;

	private TaskManager() {
	}

	public static List<TaskDefinition> getActiveTasks() {
		return activeTasks;
	}

	/** The preset set in play, or null when running a single random task. */
	public static TaskSet getActiveSet() {
		return activeSet;
	}

	// ------------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------------

	public static void start(MinecraftServer server) {
		context = new TaskContext(server);
		completed = false;
		tickCounter = 0;
		completedTaskIds.clear();

		GameConfig cfg = GameConfig.get();
		String setId = cfg.getTaskSet();
		activeSet = TaskSets.isRandom(setId) ? null : TaskSets.byId(setId);

		if (activeSet != null) {
			activeTasks = activeSet.resolve();
		} else {
			TaskDefinition single = TaskPools.random(cfg.getDifficulty());
			activeTasks = single == null ? List.of() : List.of(single);
		}

		if (activeTasks.isEmpty()) {
			ImpostorFridays.LOGGER.warn("No tasks available (set={}, difficulty={})",
					setId, cfg.getDifficulty());
			GameManager.setTaskLines(List.of());
			return;
		}

		advancementBaseline = new HashMap<>();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			advancementBaseline.put(player.getUuid(), snapshotAdvancements(server, player));
			context.addToBaseline(player);
		}
		publishTaskLines();

		if (activeSet != null) {
			ImpostorFridays.LOGGER.info("Task set for this match: {} ({} objectives, ~{} min)",
					activeSet.displayName(), activeTasks.size(), activeSet.estimateMins());
		}
		for (TaskDefinition task : activeTasks) {
			ImpostorFridays.LOGGER.info("  objective [{}]: {}", task.id(), task.description());
		}
	}

	/** Pushes the current objective list and its completion state to every HUD. */
	private static void publishTaskLines() {
		List<GameSyncS2C.TaskLine> lines = new ArrayList<>();
		for (TaskDefinition task : activeTasks) {
			lines.add(new GameSyncS2C.TaskLine(task.description(), completedTaskIds.contains(task.id())));
		}
		GameManager.setTaskLines(lines);
	}

	public static void end() {
		context = null;
		activeTasks = List.of();
		activeSet = null;
		completedTaskIds.clear();
		advancementBaseline = new HashMap<>();
		completed = false;
		tickCounter = 0;
	}

	/** Everything this player had already completed, so prior rounds never count. */
	private static Set<String> snapshotAdvancements(MinecraftServer server, ServerPlayerEntity player) {
		Set<String> done = new HashSet<>();
		for (AdvancementEntry entry : server.getAdvancementLoader().getAdvancements()) {
			if (player.getAdvancementTracker().getProgress(entry).isDone()) {
				done.add(entry.id().toString());
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
		if (context == null || activeTasks.isEmpty() || completed || !GameManager.isActive()) {
			return;
		}
		if (++tickCounter < POLL_INTERVAL) {
			return;
		}
		tickCounter = 0;

		refreshAdvancements(server);
		scanForBabyAnimals(server);

		boolean somethingFinished = false;
		for (TaskDefinition task : activeTasks) {
			if (completedTaskIds.contains(task.id())) {
				continue;
			}
			if (task.check().isComplete(context)) {
				completedTaskIds.add(task.id());
				somethingFinished = true;
				GameManager.broadcast(server, Text.literal("✔ ")
						.formatted(Formatting.GREEN, Formatting.BOLD)
						.append(Text.literal(task.completionMessage()).formatted(Formatting.GREEN))
						.append(Text.literal("  (" + completedTaskIds.size() + "/"
								+ activeTasks.size() + ")").formatted(Formatting.DARK_GREEN)));
			}
		}

		if (somethingFinished) {
			publishTaskLines();
		}

		// The Innocents win only once every objective in the set is done.
		if (completedTaskIds.size() >= activeTasks.size()) {
			completed = true;
			GameManager.broadcast(server, Text.literal("The Innocents win!")
					.formatted(Formatting.GOLD, Formatting.BOLD));
			GameManager.end(server);
		}
	}

	/** Adds advancements completed since the match began. */
	private static void refreshAdvancements(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			Set<String> baseline = advancementBaseline.get(player.getUuid());
			if (baseline == null) {
				// A player who joined mid-match. Snapshot them now and count nothing this pass,
				// so their existing history cannot complete the task for everyone.
				advancementBaseline.put(player.getUuid(), snapshotAdvancements(server, player));
				context.addToBaseline(player);
				continue;
			}
			for (AdvancementEntry entry : server.getAdvancementLoader().getAdvancements()) {
				String id = entry.id().toString();
				if (baseline.contains(id) || context.newAdvancements.contains(id)) {
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
