package com.impostorfridays.task;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskPoolsTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		// TaskPools references Items, whose static fields need the registries initialised.
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void everyTierHasAHealthyPool() {
		for (Difficulty difficulty : Difficulty.values()) {
			List<TaskDefinition> pool = TaskPools.pool(difficulty);
			assertTrue(pool.size() >= 6,
					difficulty + " should have at least 6 tasks but had " + pool.size());
		}
	}

	@Test
	void taskIdsAreUnique() {
		Set<String> seen = new HashSet<>();
		for (Difficulty difficulty : Difficulty.values()) {
			for (TaskDefinition task : TaskPools.pool(difficulty)) {
				assertTrue(seen.add(task.id()), "duplicate task id: " + task.id());
			}
		}
	}

	@Test
	void everyTaskIsFullyPopulated() {
		for (Difficulty difficulty : Difficulty.values()) {
			for (TaskDefinition task : TaskPools.pool(difficulty)) {
				assertNotNull(task.check(), task.id() + " has no completion check");
				assertFalse(task.description().isBlank(), task.id() + " has no description");
				assertFalse(task.completionMessage().isBlank(), task.id() + " has no completion message");
				assertEquals(difficulty, task.difficulty(),
						task.id() + " is filed under the wrong difficulty");
			}
		}
	}

	@Test
	void randomAlwaysReturnsATaskFromTheRequestedTier() {
		for (Difficulty difficulty : Difficulty.values()) {
			for (int i = 0; i < 50; i++) {
				TaskDefinition task = TaskPools.random(difficulty);
				assertNotNull(task);
				assertEquals(difficulty, task.difficulty());
			}
		}
	}

	@Test
	void tasksCanBeLookedUpById() {
		TaskDefinition beacon = TaskPools.byId("hard_beacon");
		assertNotNull(beacon, "the flagship hard task should be findable by id");
		assertEquals(Difficulty.HARD, beacon.difficulty());
	}
}
