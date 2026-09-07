package com.impostorfridays.task;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskSetsTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	/**
	 * The important one. {@code resolve()} silently skips ids it cannot find, so a single typo
	 * in a set would quietly drop an objective and nobody would notice until a match ran short.
	 */
	@Test
	void everySetReferencesRealTasks() {
		for (TaskSet set : TaskSets.all()) {
			List<TaskDefinition> resolved = set.resolve();
			assertEquals(set.taskIds().size(), resolved.size(),
					set.id() + " references a task id that does not exist in TaskPools: "
							+ set.taskIds());
		}
	}

	@Test
	void setIdsAndNamesAreUnique() {
		Set<String> ids = new HashSet<>();
		Set<String> names = new HashSet<>();
		for (TaskSet set : TaskSets.all()) {
			assertTrue(ids.add(set.id()), "duplicate set id: " + set.id());
			assertTrue(names.add(set.displayName()), "duplicate set name: " + set.displayName());
		}
	}

	@Test
	void everySetIsThreeObjectives() {
		for (TaskSet set : TaskSets.all()) {
			assertEquals(3, set.taskIds().size(),
					set.id() + " should be three objectives (one anchor, one spread, one light)");
		}
	}

	@Test
	void noSetRepeatsAnObjective() {
		for (TaskSet set : TaskSets.all()) {
			Set<String> seen = new HashSet<>(set.taskIds());
			assertEquals(set.taskIds().size(), seen.size(),
					set.id() + " lists the same objective twice");
		}
	}

	/** A set nobody can finish is an automatic Impostor win, so estimates must fit a match. */
	@Test
	void estimatesFitInsideADefaultMatch() {
		for (TaskSet set : TaskSets.all()) {
			assertTrue(set.estimateMins() >= 30 && set.estimateMins() <= 90,
					set.id() + " estimate of " + set.estimateMins()
							+ " min should sit between 30 and the 90 minute default match length");
		}
	}

	/** Stacking two boss-tier objectives is unfinishable in the time. */
	@Test
	void noSetStacksTwoBossObjectives() {
		Set<String> boss = Set.of("hard_dragon", "hard_wither", "hard_beacon", "hard_full_beacon");
		for (TaskSet set : TaskSets.all()) {
			long bossCount = set.taskIds().stream().filter(boss::contains).count();
			assertTrue(bossCount <= 1,
					set.id() + " stacks " + bossCount + " boss-tier objectives, which cannot be "
							+ "finished inside a match");
		}
	}

	@Test
	void everySetIsFullyDescribed() {
		for (TaskSet set : TaskSets.all()) {
			assertFalse(set.displayName().isBlank(), set.id() + " has no display name");
			assertFalse(set.blurb().isBlank(), set.id() + " has no blurb");
		}
	}

	@Test
	void lookupAndRandomSentinelBehaveSensibly() {
		assertNotNull(TaskSets.byId("set1"));
		assertNotNull(TaskSets.byId("SET1"), "lookup should be case-insensitive");
		assertEquals(null, TaskSets.byId("nope"));

		assertTrue(TaskSets.isRandom("RANDOM"));
		assertTrue(TaskSets.isRandom(null));
		assertTrue(TaskSets.isRandom(""));
		assertFalse(TaskSets.isRandom("set1"));
	}
}
