package com.impostorfridays.task;

/**
 * One shared objective, defined as data rather than as a branch in game logic.
 *
 * <p>Adding a task means adding one entry to {@link TaskPools} — no core code changes.
 *
 * @param id          stable identifier, used in logs and config
 * @param difficulty  which pool this belongs to
 * @param description the text shown on everyone's HUD
 * @param completionMessage what is broadcast when it is finished
 * @param check       the completion predicate, evaluated server-side on a timer
 */
public record TaskDefinition(
		String id,
		Difficulty difficulty,
		String description,
		String completionMessage,
		TaskCheck check
) {
	/** The completion predicate for a task. */
	@FunctionalInterface
	public interface TaskCheck {
		boolean isComplete(TaskContext context);
	}
}
