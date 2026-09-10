package com.impostorfridays.config;

import com.impostorfridays.game.Ability;
import com.impostorfridays.task.Difficulty;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameConfigTest {

	@Test
	void roundTripsEverySetting() {
		GameConfig original = new GameConfig();
		original.setGameLengthMinutes(45);
		original.setRespawnDelaySeconds(15);
		original.setImpostorCooldownSeconds(240);
		original.setSnifferEnabled(false);
		original.setSnifferCooldownSeconds(120);
		original.setDifficulty(Difficulty.MASTER);
		original.setMuteDeadPlayers(true);
		original.setAbilityEnabled(Ability.GRAVITY, false);
		original.setAbilityDurationSeconds(Ability.BLIND, 45);

		Properties saved = original.writeTo();

		GameConfig loaded = new GameConfig();
		loaded.readFrom(saved);

		assertEquals(45, loaded.getGameLengthMinutes());
		assertEquals(15, loaded.getRespawnDelaySeconds());
		assertEquals(240, loaded.getImpostorCooldownSeconds());
		assertFalse(loaded.isSnifferEnabled());
		assertEquals(120, loaded.getSnifferCooldownSeconds());
		assertEquals(Difficulty.MASTER, loaded.getDifficulty());
		assertTrue(loaded.isMuteDeadPlayers());
		assertFalse(loaded.isAbilityEnabled(Ability.GRAVITY));
		assertTrue(loaded.isAbilityEnabled(Ability.BLIND));
		assertEquals(45, loaded.getAbilityDurationSeconds(Ability.BLIND));
	}

	@Test
	void clampsOutOfRangeValues() {
		GameConfig cfg = new GameConfig();
		cfg.setGameLengthMinutes(-10);
		assertEquals(1, cfg.getGameLengthMinutes(), "length must clamp to at least 1 minute");

		cfg.setGameLengthMinutes(99999);
		assertEquals(600, cfg.getGameLengthMinutes(), "length must clamp to the 600 minute cap");
	}

	@Test
	void survivesGarbageInTheFile() {
		Properties junk = new Properties();
		junk.setProperty("game.lengthMinutes", "not-a-number");
		junk.setProperty("task.difficulty", "NONSENSE");

		GameConfig cfg = new GameConfig();
		cfg.readFrom(junk);

		assertEquals(90, cfg.getGameLengthMinutes(), "bad number falls back to the default");
		assertEquals(Difficulty.STANDARD, cfg.getDifficulty(), "bad difficulty falls back to STANDARD");
	}

	@Test
	void oldThreeTierConfigsMigrateRatherThanResetting() {
		Properties legacy = new Properties();
		legacy.setProperty("task.difficulty", "HARD");
		GameConfig cfg = new GameConfig();
		cfg.readFrom(legacy);
		assertEquals(Difficulty.EXPERT, cfg.getDifficulty(),
				"a pre-existing HARD config should become EXPERT, not silently reset");

		legacy.setProperty("task.difficulty", "EASY");
		cfg.readFrom(legacy);
		assertEquals(Difficulty.BEGINNER, cfg.getDifficulty());
	}

	@Test
	void everyDifficultyRoundTrips() {
		for (Difficulty d : Difficulty.values()) {
			GameConfig cfg = new GameConfig();
			cfg.setDifficulty(d);
			GameConfig loaded = new GameConfig();
			loaded.readFrom(cfg.writeTo());
			assertEquals(d, loaded.getDifficulty(), d + " should survive a save/load round trip");
		}
	}

	@Test
	void convertsMinutesToTicks() {
		GameConfig cfg = new GameConfig();
		cfg.setGameLengthMinutes(90);
		assertEquals(90 * 60 * 20, cfg.getGameLengthTicks());
	}
}
