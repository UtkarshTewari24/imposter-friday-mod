package com.impostorfridays.task;

/**
 * One objective, defined as data with enough metadata to rebalance the pools later without
 * reading any game logic.
 *
 * <p>Built through {@link Builder} so the twelve fields stay readable at the call site.
 *
 * @param id             stable identifier, e.g. {@code ADV_MAJOR_BEACON_001}
 * @param difficulty     which tier this belongs to
 * @param type           sub-task or major objective
 * @param category       used to keep three generated sub-tasks meaningfully distinct
 * @param description    the wording players see; must be objectively measurable
 * @param minutesLow     lower end of the expected time for a competent group of six
 * @param minutesHigh    upper end of that estimate
 * @param requiresNether whether finishing it realistically means a Nether trip
 * @param requiresEnd    whether finishing it realistically means going to the End
 * @param groupTask      whether it needs the whole group rather than any one player
 * @param excludesImpostor whether the Impostor is excluded from a whole-group requirement
 * @param check          the completion predicate, evaluated server-side on a timer
 */
public record TaskDefinition(
		String id,
		Difficulty difficulty,
		TaskType type,
		TaskCategory category,
		String description,
		int minutesLow,
		int minutesHigh,
		boolean requiresNether,
		boolean requiresEnd,
		boolean groupTask,
		boolean excludesImpostor,
		TaskCheck check
) {
	/** The completion predicate for an objective. */
	@FunctionalInterface
	public interface TaskCheck {
		boolean isComplete(TaskContext context);
	}

	/**
	 * The wording shown to players.
	 *
	 * <p>Any whole-group requirement MUST carry "(excluding Impostor)", otherwise the Impostor
	 * could sabotage it simply by refusing to take part. Appended automatically rather than
	 * left to each pool entry to remember.
	 */
	public String displayText() {
		return excludesImpostor ? description + " (excluding Impostor)" : description;
	}

	public String estimateText() {
		return minutesLow == minutesHigh
				? minutesLow + " min"
				: minutesLow + "–" + minutesHigh + " min";
	}

	public static Builder sub(String id, Difficulty difficulty, TaskCategory category) {
		return new Builder(id, difficulty, TaskType.SUB, category);
	}

	public static Builder major(String id, Difficulty difficulty, TaskCategory category) {
		return new Builder(id, difficulty, TaskType.MAJOR, category);
	}

	/** Fluent builder; everything not set has a sensible default. */
	public static final class Builder {
		private final String id;
		private final Difficulty difficulty;
		private final TaskType type;
		private final TaskCategory category;
		private String description = "";
		private int minutesLow = 10;
		private int minutesHigh = 20;
		private boolean requiresNether;
		private boolean requiresEnd;
		private boolean groupTask;
		private boolean excludesImpostor;
		private TaskCheck check = ctx -> false;

		private Builder(String id, Difficulty difficulty, TaskType type, TaskCategory category) {
			this.id = id;
			this.difficulty = difficulty;
			this.type = type;
			this.category = category;
		}

		public Builder desc(String description) {
			this.description = description;
			return this;
		}

		public Builder mins(int low, int high) {
			this.minutesLow = low;
			this.minutesHigh = high;
			return this;
		}

		public Builder nether() {
			this.requiresNether = true;
			return this;
		}

		public Builder end() {
			this.requiresEnd = true;
			return this;
		}

		/** Marks this as a whole-group objective, which also excludes the Impostor. */
		public Builder group() {
			this.groupTask = true;
			this.excludesImpostor = true;
			return this;
		}

		public Builder check(TaskCheck check) {
			this.check = check;
			return this;
		}

		public TaskDefinition build() {
			return new TaskDefinition(id, difficulty, type, category, description,
					minutesLow, minutesHigh, requiresNether, requiresEnd,
					groupTask, excludesImpostor, check);
		}
	}
}
