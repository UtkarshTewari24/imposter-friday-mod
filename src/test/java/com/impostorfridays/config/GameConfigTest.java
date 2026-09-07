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
		original.setDifficulty(Difficulty.HARD);
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
		assertEquals(Difficulty.HARD, loaded.getDifficulty());
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
	void taskSetRoundTripsAndRejectsUnknownIds() {
		GameConfig cfg = new GameConfig();
		cfg.setTaskSet("set3");
		assertEquals("set3", cfg.getTaskSet());

		Properties saved = cfg.writeTo();
		GameConfig loaded = new GameConfig();
		loaded.readFrom(saved);
		assertEquals("set3", loaded.getTaskSet());

		// A hand-edited file naming a set that doesn't exist must not break the game.
		cfg.setTaskSet("does_not_exist");
		assertEquals("RANDOM", cfg.getTaskSet(), "unknown set ids fall back to RANDOM");
	}

	@Test
	void convertsMinutesToTicks() {
		GameConfig cfg = new GameConfig();
		cfg.setGameLengthMinutes(90);
		assertEquals(90 * 60 * 20, cfg.getGameLengthTicks());
	}
}
