package com.impostorfridays.task;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.config.GameConfig;
import com.impostorfridays.game.GameManager;
import com.impostorfridays.net.GameSyncS2C;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runs the objectives for the current match.
 *
 * <p>Completion is polled on a timer rather than hooked into a dozen game events, which keeps
 * every {@link TaskDefinition} predicate simple and side-effect free.
 *
 * <p>Everything is match-scoped. Advancements and item counts are baselined per player on first
 * sight, so neither a previous round nor a late joiner's history can complete an objective.
 */
public final class TaskManager {

	/** How often completion is evaluated and the world is scanned. */
	private static final int POLL_INTERVAL = 40;
	/** Radius searched around each player for objective-relevant blocks. */
	private static final int BLOCK_SCAN_RADIUS = 6;

	private static TaskContext context;
	private static TaskPools.Generated generated;
	private static final Set<String> completedIds = new HashSet<>();
	private static Map<UUID, Set<String>> advancementBaseline = new HashMap<>();
	private static int tickCounter;
	private static boolean allComplete;

	private TaskManager() {
	}

	public static TaskPools.Generated getGenerated() {
		return generated;
	}

	public static TaskFormat getFormat() {
		return generated == null ? null : generated.format();
	}

	// ------------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------------

	public static void start(MinecraftServer server) {
		context = new TaskContext(server);
		completedIds.clear();
		allComplete = false;
		tickCounter = 0;

		GameConfig cfg = GameConfig.get();
		generated = TaskPools.generate(cfg.getDifficulty());

		advancementBaseline = new HashMap<>();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			advancementBaseline.put(player.getUuid(), snapshotAdvancements(server, player));
			context.addToBaseline(player);
		}

		publishTaskLines();

		ImpostorFridays.LOGGER.info("Objectives [{} / {}]:",
				cfg.getDifficulty().getDisplayName(), generated.format().getDisplayName());
		for (String line : objectiveDescriptions()) {
			ImpostorFridays.LOGGER.info("  - {}", line);
		}
	}

	public static void end() {
		context = null;
		generated = null;
		completedIds.clear();
		advancementBaseline = new HashMap<>();
		allComplete = false;
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
	// Objective listing
	// ------------------------------------------------------------------

	private static List<String> objectiveDescriptions() {
		List<String> lines = new ArrayList<>();
		if (generated == null) {
			return lines;
		}
		if (generated.format() == TaskFormat.FIVE_ADVANCEMENTS) {
			for (AdvancementTask a : generated.advancements()) {
				lines.add(a.displayName());
			}
		} else {
			for (TaskDefinition t : generated.tasks()) {
				lines.add(t.displayText());
			}
		}
		return lines;
	}

	private static boolean isObjectiveComplete(int index) {
		if (generated.format() == TaskFormat.FIVE_ADVANCEMENTS) {
			AdvancementTask a = generated.advancements().get(index);
			return completedIds.contains(a.advancementId());
		}
		return completedIds.contains(generated.tasks().get(index).id());
	}

	/** Pushes the objective list and completion state to every HUD. */
	private static void publishTaskLines() {
		List<GameSyncS2C.TaskLine> lines = new ArrayList<>();
		if (generated != null) {
			List<String> descriptions = objectiveDescriptions();
			for (int i = 0; i < descriptions.size(); i++) {
				lines.add(new GameSyncS2C.TaskLine(descriptions.get(i), isObjectiveComplete(i)));
			}
		}
		GameManager.setTaskLines(lines);
	}

	// ------------------------------------------------------------------
	// Event recording
	// ------------------------------------------------------------------

	/** Records a kill, its weapon and armour conditions, or a player's cause of death. */
	public static void onDeath(LivingEntity entity, DamageSource source) {
		if (context == null) {
			return;
		}

		if (entity instanceof PlayerEntity player) {
			source.getTypeRegistryEntry().getKey()
					.ifPresent(key -> context.playerDeathCauses.add(key.getValue().toString()));
			// Dying breaks any "without dying" run.
			context.killStreak.put(player.getUuid(), 0);
			return;
		}

		if (!(source.getAttacker() instanceof ServerPlayerEntity killer)) {
			return;
		}

		String typeId = EntityType.getId(entity.getType()).toString();
		context.killCounts.merge(typeId, 1, Integer::sum);
		context.killStreak.merge(killer.getUuid(), 1, Integer::sum);

		ItemStack weapon = killer.getMainHandStack();
		if (!weapon.isEmpty()) {
			var weaponId = Registries.ITEM.getId(weapon.getItem());
			if (weaponId != null) {
				context.killWeapons.add(weaponId.toString());
			}
		}

		boolean wearingArmour = false;
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
				EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (!killer.getEquippedStack(slot).isEmpty()) {
				wearingArmour = true;
				break;
			}
		}
		if (!wearingArmour) {
			context.noArmourKills++;
		}
	}

	/** Called by the trade mixin whenever a villager trade is used. */
	public static void onVillagerTrade() {
		if (context != null) {
			context.villagerTrades++;
		}
	}

	// ------------------------------------------------------------------
	// Polling
	// ------------------------------------------------------------------

	public static void tick(MinecraftServer server) {
		if (context == null || generated == null || allComplete || !GameManager.isActive()) {
			return;
		}
		if (++tickCounter < POLL_INTERVAL) {
			return;
		}
		tickCounter = 0;

		refreshAdvancements(server);
		scanWorld(server);
		evaluate(server);
	}

	private static void evaluate(MinecraftServer server) {
		boolean somethingFinished = false;
		List<String> descriptions = objectiveDescriptions();

		for (int i = 0; i < descriptions.size(); i++) {
			if (isObjectiveComplete(i)) {
				continue;
			}
			boolean done;
			String id;
			if (generated.format() == TaskFormat.FIVE_ADVANCEMENTS) {
				AdvancementTask a = generated.advancements().get(i);
				id = a.advancementId();
				done = context.advancementEarned(a.advancementId());
			} else {
				TaskDefinition t = generated.tasks().get(i);
				id = t.id();
				done = t.check().isComplete(context);
			}
			if (done) {
				completedIds.add(id);
				somethingFinished = true;
				GameManager.broadcast(server, Text.literal("✔ ")
						.formatted(Formatting.GREEN, Formatting.BOLD)
						.append(Text.literal(descriptions.get(i)).formatted(Formatting.GREEN))
						.append(Text.literal("  (" + completedIds.size() + "/" + descriptions.size()
								+ ")").formatted(Formatting.DARK_GREEN)));
			}
		}

		if (somethingFinished) {
			publishTaskLines();
		}

		if (!descriptions.isEmpty() && completedIds.size() >= descriptions.size()) {
			allComplete = true;
			GameManager.broadcast(server, Text.literal("The Innocents win!")
					.formatted(Formatting.GOLD, Formatting.BOLD));
			GameManager.endWithWinner(server, true);
		}
	}

	private static void refreshAdvancements(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			Set<String> baseline = advancementBaseline.get(player.getUuid());
			if (baseline == null) {
				// Joined mid-match: baseline them now and count nothing this pass.
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

	/** One pass over players recording biome, structure, tamed animals and nearby blocks. */
	private static void scanWorld(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			ServerWorld world = (ServerWorld) player.getEntityWorld();
			BlockPos pos = player.getBlockPos();

			world.getBiome(pos).getKey()
					.ifPresent(key -> context.biomesVisited.add(key.getValue().toString()));

			var start = world.getStructureAccessor().getStructureContaining(pos, entry -> true);
			if (start != null && start.hasChildren()) {
				world.getRegistryManager()
						.getOptional(net.minecraft.registry.RegistryKeys.STRUCTURE)
						.map(reg -> reg.getId(start.getStructure()))
						.ifPresent(id -> {
							if (id != null) {
								context.structuresVisited.add(id.toString());
							}
						});
			}

			scanNearbyBlocks(world, pos);
		}

		for (ServerWorld world : server.getWorlds()) {
			for (Entity entity : world.iterateEntities()) {
				recordEntity(entity);
			}
		}
	}

	private static void scanNearbyBlocks(ServerWorld world, BlockPos centre) {
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int x = -BLOCK_SCAN_RADIUS; x <= BLOCK_SCAN_RADIUS; x++) {
			for (int y = -BLOCK_SCAN_RADIUS; y <= BLOCK_SCAN_RADIUS; y++) {
				for (int z = -BLOCK_SCAN_RADIUS; z <= BLOCK_SCAN_RADIUS; z++) {
					cursor.set(centre.getX() + x, centre.getY() + y, centre.getZ() + z);
					if (!world.isChunkLoaded(cursor.getX() >> 4, cursor.getZ() >> 4)) {
						continue;
					}
					var block = world.getBlockState(cursor).getBlock();
					if (block == net.minecraft.block.Blocks.SPAWNER
							|| block == net.minecraft.block.Blocks.ENCHANTING_TABLE
							|| block == net.minecraft.block.Blocks.BEACON) {
						var id = Registries.BLOCK.getId(block);
						if (id != null) {
							context.nearbyBlocks.add(id.toString());
						}
					}
				}
			}
		}
	}

	private static void recordEntity(Entity entity) {
		String typeId = EntityType.getId(entity.getType()).toString();

		if (entity instanceof PassiveEntity passive && passive.isBaby()) {
			context.babiesByType.computeIfAbsent(typeId, k -> new HashSet<>()).add(entity.getUuid());
		}
		if (entity instanceof TameableEntity tameable && tameable.getOwner() != null) {
			context.tamedTypes.add(typeId);
		}
		if (entity instanceof net.minecraft.entity.passive.AbstractHorseEntity horse
				&& horse.isTame()
				&& !horse.getEquippedStack(EquipmentSlot.SADDLE).isEmpty()) {
			context.saddledHorse = true;
			context.tamedTypes.add(typeId);
		}
	}
}
