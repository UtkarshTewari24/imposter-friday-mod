package com.impostorfridays.game;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

	@Test
	void timerCountsDownAndStopsAtZero() {
		GameState state = new GameState(3);
		state.tickCountdowns();
		assertEquals(2, state.getRemainingTicks());
		state.tickCountdowns();
		state.tickCountdowns();
		assertTrue(state.isTimeUp());

		// Must not go negative once expired.
		state.tickCountdowns();
		assertEquals(0, state.getRemainingTicks());
	}

	@Test
	void abilitiesShareOneCooldown() {
		GameState state = new GameState(1000);
		assertTrue(state.isImpostorReady());

		state.startImpostorCooldown(2);
		assertFalse(state.isImpostorReady(), "using any ability must lock all of them");

		state.tickCountdowns();
		assertFalse(state.isImpostorReady());
		state.tickCountdowns();
		assertTrue(state.isImpostorReady(), "cooldown should clear once it runs out");
	}

	@Test
	void snifferCooldownIsIndependentOfImpostorCooldown() {
		GameState state = new GameState(1000);
		state.startImpostorCooldown(100);
		assertTrue(state.isSnifferReady(), "sniffer must not be locked by impostor ability use");
	}

	@Test
	void removedAbilitiesArePermanent() {
		GameState state = new GameState(1000);
		assertFalse(state.isAbilityRemoved(Ability.BLIND));
		state.removeAbility(Ability.BLIND);
		assertTrue(state.isAbilityRemoved(Ability.BLIND));

		// Still gone after a long stretch of ticking.
		for (int i = 0; i < 100; i++) {
			state.tickCountdowns();
		}
		assertTrue(state.isAbilityRemoved(Ability.BLIND), "a sniffed-away ability never comes back");
	}

	@Test
	void timedEffectsExpireOnTheirOwn() {
		GameState state = new GameState(1000);
		state.startEffect(Ability.GRAVITY, 2);
		assertTrue(state.isEffectActive(Ability.GRAVITY));

		state.tickCountdowns();
		assertTrue(state.isEffectActive(Ability.GRAVITY));
		state.tickCountdowns();
		assertFalse(state.isEffectActive(Ability.GRAVITY), "effect must clear itself at the end");
	}

	@Test
	void unknownPlayersDefaultToInnocent() {
		GameState state = new GameState(1000);
		assertEquals(Role.INNOCENT, state.getRole(UUID.randomUUID()),
				"players who join mid-match are Innocents, never null");
	}

	@Test
	void roleLookupsIdentifyTheImpostor() {
		GameState state = new GameState(1000);
		UUID impostor = UUID.randomUUID();
		UUID innocent = UUID.randomUUID();
		state.assignRole(impostor, Role.IMPOSTOR);
		state.assignRole(innocent, Role.INNOCENT);

		assertTrue(state.isImpostor(impostor));
		assertFalse(state.isImpostor(innocent));
		assertFalse(state.isImpostor(null), "a null uuid must never match the impostor");
		assertEquals(impostor, state.getImpostorId());
	}

	@Test
	void deathTimersTickDownButPlayerStaysDeadUntilCleared() {
		GameState state = new GameState(1000);
		UUID player = UUID.randomUUID();
		state.markDead(player, 2);
		assertTrue(state.isDead(player));

		state.tickCountdowns();
		state.tickCountdowns();
		state.tickCountdowns();
		assertEquals(0, state.getRespawnTicks(player));
		assertTrue(state.isDead(player), "still flagged dead until respawn actually happens");

		state.clearDead(player);
		assertFalse(state.isDead(player));
	}
}
