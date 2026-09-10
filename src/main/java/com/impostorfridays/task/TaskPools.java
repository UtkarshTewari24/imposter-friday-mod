package com.impostorfridays.task;

import com.impostorfridays.task.pool.AdvancedTasks;
import com.impostorfridays.task.pool.AdvancementPools;
import com.impostorfridays.task.pool.BeginnerTasks;
import com.impostorfridays.task.pool.ExpertTasks;
import com.impostorfridays.task.pool.MasterTasks;
import com.impostorfridays.task.pool.StandardTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The task catalogue and the objective generator.
 *
 * <p>On {@code /start} one of three formats is chosen at random: three sub-tasks, one major
 * objective, or five advancements.
 */
public final class TaskPools {

	private static final Map<Difficulty, List<TaskDefinition>> SUBS = new EnumMap<>(Difficulty.class);
	private static final Map<Difficulty, List<TaskDefinition>> MAJORS = new EnumMap<>(Difficulty.class);

	private static final Random RANDOM = new Random();

	private TaskPools() {
	}

	static {
		register(Difficulty.BEGINNER, BeginnerTasks.subTasks(), BeginnerTasks.majorTasks());
		register(Difficulty.STANDARD, StandardTasks.subTasks(), StandardTasks.majorTasks());
		register(Difficulty.ADVANCED, AdvancedTasks.subTasks(), AdvancedTasks.majorTasks());
		register(Difficulty.EXPERT, ExpertTasks.subTasks(), ExpertTasks.majorTasks());
		register(Difficulty.MASTER, MasterTasks.subTasks(), MasterTasks.majorTasks());
	}

	private static void register(Difficulty d, List<TaskDefinition> subs, List<TaskDefinition> majors) {
		SUBS.put(d, List.copyOf(subs));
		MAJORS.put(d, List.copyOf(majors));
	}

	public static List<TaskDefinition> subPool(Difficulty difficulty) {
		return SUBS.getOrDefault(difficulty, List.of());
	}

	public static List<TaskDefinition> majorPool(Difficulty difficulty) {
		return MAJORS.getOrDefault(difficulty, List.of());
	}

	public static List<AdvancementTask> advancementPool(Difficulty difficulty) {
		return AdvancementPools.pool(difficulty);
	}

	public static TaskDefinition byId(String id) {
		for (Difficulty d : Difficulty.values()) {
			for (TaskDefinition t : subPool(d)) {
				if (t.id().equals(id)) {
					return t;
				}
			}
			for (TaskDefinition t : majorPool(d)) {
				if (t.id().equals(id)) {
					return t;
				}
			}
		}
		return null;
	}

	// ------------------------------------------------------------------
	// Generation
	// ------------------------------------------------------------------

	/** The objectives generated for one match. */
	public record Generated(
			TaskFormat format,
			List<TaskDefinition> tasks,
			List<AdvancementTask> advancements
	) {
		public int size() {
			return format == TaskFormat.FIVE_ADVANCEMENTS ? advancements.size() : tasks.size();
		}
	}

	/** Picks a format at random, then fills it. */
	public static Generated generate(Difficulty difficulty) {
		TaskFormat[] formats = TaskFormat.values();
		return generate(difficulty, formats[RANDOM.nextInt(formats.length)]);
	}

	public static Generated generate(Difficulty difficulty, TaskFormat format) {
		return switch (format) {
			case ONE_MAJOR -> {
				List<TaskDefinition> pool = majorPool(difficulty);
				yield new Generated(format,
						pool.isEmpty() ? List.of() : List.of(pool.get(RANDOM.nextInt(pool.size()))),
						List.of());
			}
			case THREE_SUBTASKS -> new Generated(format, pickThreeDistinct(difficulty), List.of());
			case FIVE_ADVANCEMENTS -> new Generated(format, List.of(), pickFiveAdvancements(difficulty));
		};
	}

	/**
	 * Picks three sub-tasks from three DIFFERENT categories.
	 *
	 * <p>Three independent random picks can easily produce "find a Nether fortress", "obtain
	 * blaze rods" and "obtain nether wart" — technically three objectives, functionally one
	 * expedition. Requiring distinct categories is what keeps the three meaningfully separate
	 * and gets the group splitting up.
	 *
	 * <p>Falls back to allowing a repeated category only if the pool genuinely cannot supply
	 * three different ones, so generation can never fail outright.
	 */
	public static List<TaskDefinition> pickThreeDistinct(Difficulty difficulty) {
		List<TaskDefinition> pool = new ArrayList<>(subPool(difficulty));
		if (pool.size() <= 3) {
			return List.copyOf(pool);
		}
		Collections.shuffle(pool, RANDOM);

		List<TaskDefinition> chosen = new ArrayList<>(3);
		Set<TaskCategory> usedCategories = new HashSet<>();

		for (TaskDefinition candidate : pool) {
			if (chosen.size() == 3) {
				break;
			}
			if (usedCategories.add(candidate.category())) {
				chosen.add(candidate);
			}
		}

		// Top up if the pool didn't have three distinct categories available.
		for (TaskDefinition candidate : pool) {
			if (chosen.size() == 3) {
				break;
			}
			if (!chosen.contains(candidate)) {
				chosen.add(candidate);
			}
		}
		return List.copyOf(chosen);
	}

	/** Picks five distinct advancements from the tier's bracketed pool. */
	public static List<AdvancementTask> pickFiveAdvancements(Difficulty difficulty) {
		List<AdvancementTask> pool = new ArrayList<>(advancementPool(difficulty));
		if (pool.size() <= 5) {
			return List.copyOf(pool);
		}
		Collections.shuffle(pool, RANDOM);
		return List.copyOf(pool.subList(0, 5));
	}
}
