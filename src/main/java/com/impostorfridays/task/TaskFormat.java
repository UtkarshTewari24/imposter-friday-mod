package com.impostorfridays.task;

/**
 * The three shapes a match's objectives can take. One is chosen at random on {@code /start}.
 */
public enum TaskFormat {
	/** Three independently completable objectives, deliberately from different categories. */
	THREE_SUBTASKS("3 Objectives", 3),
	/** One large objective that contains substantial work by itself. */
	ONE_MAJOR("Major Objective", 1),
	/** Five specific vanilla advancements. */
	FIVE_ADVANCEMENTS("5 Advancements", 5);

	private final String displayName;
	private final int objectiveCount;

	TaskFormat(String displayName, int objectiveCount) {
		this.displayName = displayName;
		this.objectiveCount = objectiveCount;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getObjectiveCount() {
		return objectiveCount;
	}
}
