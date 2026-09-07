package com.impostorfridays.task;

import java.util.List;

/**
 * A curated, named bundle of objectives the Innocents must complete together.
 *
 * <p>Sets exist so a match has several things to chase rather than one, which pushes the group
 * to split up and regroup — the social pressure the whole mod is built around.
 *
 * <p>Tasks are referenced by id from {@link TaskPools} rather than redefined, so a set is pure
 * data and nothing is duplicated.
 *
 * @param id            stable identifier used in config
 * @param displayName   what the setup panel shows
 * @param blurb         one-line description of the flavour and difficulty
 * @param estimateMins  roughly how long a group of 4-8 should need
 * @param taskIds       the objectives, in the order they are listed on the HUD
 */
public record TaskSet(
		String id,
		String displayName,
		String blurb,
		int estimateMins,
		List<String> taskIds
) {
	/** Resolves this set's task ids into definitions, skipping any that no longer exist. */
	public List<TaskDefinition> resolve() {
		return taskIds.stream()
				.map(TaskPools::byId)
				.filter(java.util.Objects::nonNull)
				.toList();
	}
}
