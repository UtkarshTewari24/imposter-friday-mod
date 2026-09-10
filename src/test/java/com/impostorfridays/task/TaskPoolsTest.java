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

class TaskPoolsTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void everyDifficultyHasHealthyPools() {
		for (Difficulty d : Difficulty.values()) {
			assertTrue(TaskPools.subPool(d).size() >= 20,
					d + " needs a large sub-task pool so games don't repeat; had "
							+ TaskPools.subPool(d).size());
			assertTrue(TaskPools.majorPool(d).size() >= 10,
					d + " needs a decent major pool; had " + TaskPools.majorPool(d).size());
			assertTrue(TaskPools.advancementPool(d).size() >= 10,
					d + " needs enough advancements for varied draws of five");
		}
	}

	@Test
	void taskIdsAreUniqueEverywhere() {
		Set<String> seen = new HashSet<>();
		for (Difficulty d : Difficulty.values()) {
			for (TaskDefinition t : TaskPools.subPool(d)) {
				assertTrue(seen.add(t.id()), "duplicate task id: " + t.id());
			}
			for (TaskDefinition t : TaskPools.majorPool(d)) {
				assertTrue(seen.add(t.id()), "duplicate task id: " + t.id());
			}
		}
	}

	@Test
	void everyTaskIsFullyPopulated() {
		for (Difficulty d : Difficulty.values()) {
			for (TaskDefinition t : allOf(d)) {
				assertNotNull(t.check(), t.id() + " has no completion check");
				assertFalse(t.description().isBlank(), t.id() + " has no description");
				assertEquals(d, t.difficulty(), t.id() + " is filed under the wrong difficulty");
				assertTrue(t.minutesLow() > 0 && t.minutesHigh() >= t.minutesLow(),
						t.id() + " has a nonsensical time estimate");
			}
		}
	}

	/**
	 * The rule that stops "find a fortress / get blaze rods / get nether wart" — technically
	 * three objectives, functionally one expedition.
	 */
	@Test
	void threeSubTasksAlwaysComeFromThreeDifferentCategories() {
		for (Difficulty d : Difficulty.values()) {
			for (int attempt = 0; attempt < 200; attempt++) {
				List<TaskDefinition> picked = TaskPools.pickThreeDistinct(d);
				assertEquals(3, picked.size(), d + " should generate exactly three sub-tasks");

				Set<TaskCategory> categories = new HashSet<>();
				for (TaskDefinition t : picked) {
					categories.add(t.category());
				}
				assertEquals(3, categories.size(),
						d + " generated overlapping categories: " + picked.stream()
								.map(t -> t.id() + "(" + t.category() + ")").toList());
			}
		}
	}

	@Test
	void threeSubTasksAreNeverDuplicated() {
		for (Difficulty d : Difficulty.values()) {
			for (int attempt = 0; attempt < 100; attempt++) {
				List<TaskDefinition> picked = TaskPools.pickThreeDistinct(d);
				Set<String> ids = new HashSet<>();
				for (TaskDefinition t : picked) {
					assertTrue(ids.add(t.id()), "the same task was picked twice: " + t.id());
				}
			}
		}
	}

	@Test
	void fiveAdvancementsAreAlwaysFiveDistinct() {
		for (Difficulty d : Difficulty.values()) {
			for (int attempt = 0; attempt < 100; attempt++) {
				List<AdvancementTask> picked = TaskPools.pickFiveAdvancements(d);
				assertEquals(5, picked.size(), d + " should generate five advancements");
				Set<String> ids = new HashSet<>();
				for (AdvancementTask a : picked) {
					assertTrue(ids.add(a.advancementId()), "duplicate advancement: " + a);
				}
			}
		}
	}

	/**
	 * Any whole-group requirement must say so, otherwise the Impostor sabotages it by simply
	 * refusing to take part.
	 */
	@Test
	void groupObjectivesAlwaysExcludeTheImpostorInTheirWording() {
		for (Difficulty d : Difficulty.values()) {
			for (TaskDefinition t : allOf(d)) {
				if (t.groupTask()) {
					assertTrue(t.excludesImpostor(), t.id() + " is a group task but doesn't exclude "
							+ "the Impostor");
					assertTrue(t.displayText().contains("(excluding Impostor)"),
							t.id() + " must show '(excluding Impostor)' but reads: "
									+ t.displayText());
				}
			}
		}
	}

	/** "Everyone"-style wording must be backed by the group flag, or the wording lies. */
	@Test
	void everyoneWordingIsBackedByTheGroupFlag() {
		for (Difficulty d : Difficulty.values()) {
			for (TaskDefinition t : allOf(d)) {
				String lower = t.description().toLowerCase();
				if (lower.startsWith("everyone")) {
					assertTrue(t.groupTask(),
							t.id() + " says \"everyone\" but is not marked as a group task");
				}
			}
		}
	}

	@Test
	void generatorProducesUsableOutputForEveryFormat() {
		for (Difficulty d : Difficulty.values()) {
			for (TaskFormat format : TaskFormat.values()) {
				TaskPools.Generated g = TaskPools.generate(d, format);
				assertEquals(format, g.format());
				assertEquals(format.getObjectiveCount(), g.size(),
						d + "/" + format + " produced the wrong number of objectives");
			}
		}
	}

	@Test
	void randomFormatSelectionEventuallyProducesAllThree() {
		Set<TaskFormat> seen = new HashSet<>();
		for (int i = 0; i < 500; i++) {
			seen.add(TaskPools.generate(Difficulty.STANDARD).format());
		}
		assertEquals(3, seen.size(), "all three formats should be reachable at random");
	}

	/** Estimates must leave the Impostor room to interfere inside a 90 minute match. */
	@Test
	void timeEstimatesFitInsideTheDefaultMatch() {
		for (Difficulty d : Difficulty.values()) {
			for (TaskDefinition t : allOf(d)) {
				assertTrue(t.minutesHigh() <= 88,
						t.id() + " estimates " + t.minutesHigh() + " min, leaving no room inside "
								+ "the 90 minute default");
			}
		}
	}

	/** Three sub-tasks together should not exceed a major objective's workload. */
	@Test
	void subTasksAreIndividuallyShorterThanMajors() {
		for (Difficulty d : Difficulty.values()) {
			int worstSub = TaskPools.subPool(d).stream()
					.mapToInt(TaskDefinition::minutesHigh).max().orElse(0);
			int worstMajor = TaskPools.majorPool(d).stream()
					.mapToInt(TaskDefinition::minutesHigh).max().orElse(0);
			assertTrue(worstSub <= worstMajor,
					d + ": the longest sub-task (" + worstSub + " min) should not exceed the "
							+ "longest major objective (" + worstMajor + " min)");
		}
	}

	@Test
	void difficultyOrderingIsMonotonic() {
		int previous = 0;
		for (Difficulty d : Difficulty.values()) {
			int average = (int) TaskPools.majorPool(d).stream()
					.mapToInt(TaskDefinition::minutesHigh).average().orElse(0);
			assertTrue(average >= previous,
					d + " majors average " + average + " min, which is easier than the tier below");
			previous = average;
		}
	}

	private static List<TaskDefinition> allOf(Difficulty d) {
		List<TaskDefinition> all = new java.util.ArrayList<>(TaskPools.subPool(d));
		all.addAll(TaskPools.majorPool(d));
		return all;
	}
}
