package com.impostorfridays.config;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.game.Ability;
import com.impostorfridays.task.Difficulty;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Mod configuration, persisted to {@code config/amongusgame.properties}.
 *
 * <p>Edited in-game through {@code /amongussetup} and written on "Save Settings".
 * Every value is clamped on load so a hand-edited file can never put the game into
 * an unplayable state.
 */
public final class GameConfig {
	public static final String FILE_NAME = "amongusgame.properties";

	private static GameConfig instance;

	// --- Match ---
	private int gameLengthMinutes = 90;
	private int respawnDelaySeconds = 10;

	// --- Impostor ---
	/** Single shared cooldown across all abilities. */
	private int impostorCooldownSeconds = 300;
	private final Set<Ability> enabledAbilities = EnumSet.allOf(Ability.class);
	private final Map<Ability, Integer> abilityDurations = new EnumMap<>(Ability.class);

	// --- Sniffer ---
	private boolean snifferEnabled = true;
	private int snifferCooldownSeconds = 180;

	// --- Tasks ---
	private Difficulty difficulty = Difficulty.STANDARD;
	/**
	 * Whether {@code /start} revokes every advancement so objectives cannot be pre-completed.
	 * Destructive to progression, so it is a toggle.
	 */
	private boolean resetAdvancementsOnStart = true;

	// --- Simple Voice Chat ---
	/** When true, dead players cannot be heard by (or hear) the living. */
	private boolean muteDeadPlayers = false;

	// Package-private so unit tests can build an isolated instance without touching disk.
	GameConfig() {
		for (Ability a : Ability.values()) {
			if (a.hasDuration()) {
				abilityDurations.put(a, 30);
			}
		}
	}

	public static GameConfig get() {
		if (instance == null) {
			instance = new GameConfig();
			instance.load();
		}
		return instance;
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	// ------------------------------------------------------------------
	// Persistence
	// ------------------------------------------------------------------

	public void load() {
		Path path = configPath();
		if (!Files.exists(path)) {
			ImpostorFridays.LOGGER.info("No config found, writing defaults to {}", path);
			save();
			return;
		}
		Properties props = new Properties();
		try (InputStream in = Files.newInputStream(path)) {
			props.load(in);
		} catch (IOException e) {
			ImpostorFridays.LOGGER.error("Failed to read config, using defaults", e);
			return;
		}
		readFrom(props);
	}

	/** Split out from {@link #load()} so the round-trip can be unit tested without touching disk. */
	public void readFrom(Properties props) {
		gameLengthMinutes = clamp(getInt(props, "game.lengthMinutes", gameLengthMinutes), 1, 600);
		respawnDelaySeconds = clamp(getInt(props, "game.respawnDelaySeconds", respawnDelaySeconds), 0, 300);
		impostorCooldownSeconds = clamp(getInt(props, "impostor.cooldownSeconds", impostorCooldownSeconds), 0, 3600);
		snifferEnabled = getBool(props, "sniffer.enabled", snifferEnabled);
		snifferCooldownSeconds = clamp(getInt(props, "sniffer.cooldownSeconds", snifferCooldownSeconds), 0, 3600);
		difficulty = Difficulty.byName(props.getProperty("task.difficulty", difficulty.name()));
		resetAdvancementsOnStart = getBool(props, "task.resetAdvancementsOnStart",
				resetAdvancementsOnStart);
		muteDeadPlayers = getBool(props, "voicechat.muteDead", muteDeadPlayers);

		for (Ability a : Ability.values()) {
			boolean on = getBool(props, "ability." + a.getId() + ".enabled", enabledAbilities.contains(a));
			setAbilityEnabled(a, on);
			if (a.hasDuration()) {
				int def = abilityDurations.getOrDefault(a, 30);
				abilityDurations.put(a, clamp(getInt(props, "ability." + a.getId() + ".durationSeconds", def), 1, 600));
			}
		}
	}

	/** Split out from {@link #save()} so the round-trip can be unit tested without touching disk. */
	public Properties writeTo() {
		Properties props = new Properties();
		props.setProperty("game.lengthMinutes", Integer.toString(gameLengthMinutes));
		props.setProperty("game.respawnDelaySeconds", Integer.toString(respawnDelaySeconds));
		props.setProperty("impostor.cooldownSeconds", Integer.toString(impostorCooldownSeconds));
		props.setProperty("sniffer.enabled", Boolean.toString(snifferEnabled));
		props.setProperty("sniffer.cooldownSeconds", Integer.toString(snifferCooldownSeconds));
		props.setProperty("task.difficulty", difficulty.name());
		props.setProperty("task.resetAdvancementsOnStart",
				Boolean.toString(resetAdvancementsOnStart));
		props.setProperty("voicechat.muteDead", Boolean.toString(muteDeadPlayers));
		for (Ability a : Ability.values()) {
			props.setProperty("ability." + a.getId() + ".enabled", Boolean.toString(enabledAbilities.contains(a)));
			if (a.hasDuration()) {
				props.setProperty("ability." + a.getId() + ".durationSeconds",
						Integer.toString(abilityDurations.getOrDefault(a, 30)));
			}
		}
		return props;
	}

	public void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			try (OutputStream out = Files.newOutputStream(path)) {
				writeTo().store(out, "Impostor Fridays settings - edit in-game with /amongussetup");
			}
			ImpostorFridays.LOGGER.info("Saved config to {}", path);
		} catch (IOException e) {
			ImpostorFridays.LOGGER.error("Failed to save config", e);
		}
	}

	private static int getInt(Properties p, String key, int def) {
		try {
			return Integer.parseInt(p.getProperty(key, Integer.toString(def)).trim());
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static boolean getBool(Properties p, String key, boolean def) {
		return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(def)).trim());
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}

	// ------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------

	public int getGameLengthMinutes() {
		return gameLengthMinutes;
	}

	public void setGameLengthMinutes(int v) {
		this.gameLengthMinutes = clamp(v, 1, 600);
	}

	public int getRespawnDelaySeconds() {
		return respawnDelaySeconds;
	}

	public void setRespawnDelaySeconds(int v) {
		this.respawnDelaySeconds = clamp(v, 0, 300);
	}

	public int getImpostorCooldownSeconds() {
		return impostorCooldownSeconds;
	}

	public void setImpostorCooldownSeconds(int v) {
		this.impostorCooldownSeconds = clamp(v, 0, 3600);
	}

	public boolean isSnifferEnabled() {
		return snifferEnabled;
	}

	public void setSnifferEnabled(boolean v) {
		this.snifferEnabled = v;
	}

	public int getSnifferCooldownSeconds() {
		return snifferCooldownSeconds;
	}

	public void setSnifferCooldownSeconds(int v) {
		this.snifferCooldownSeconds = clamp(v, 0, 3600);
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public boolean isResetAdvancementsOnStart() {
		return resetAdvancementsOnStart;
	}

	public void setResetAdvancementsOnStart(boolean value) {
		this.resetAdvancementsOnStart = value;
	}

	public void setDifficulty(Difficulty d) {
		this.difficulty = d;
	}

	public boolean isMuteDeadPlayers() {
		return muteDeadPlayers;
	}

	public void setMuteDeadPlayers(boolean v) {
		this.muteDeadPlayers = v;
	}

	public boolean isAbilityEnabled(Ability a) {
		return enabledAbilities.contains(a);
	}

	public void setAbilityEnabled(Ability a, boolean on) {
		if (on) {
			enabledAbilities.add(a);
		} else {
			enabledAbilities.remove(a);
		}
	}

	public int getAbilityDurationSeconds(Ability a) {
		return abilityDurations.getOrDefault(a, 30);
	}

	public void setAbilityDurationSeconds(Ability a, int seconds) {
		if (a.hasDuration()) {
			abilityDurations.put(a, clamp(seconds, 1, 600));
		}
	}

	public int getGameLengthTicks() {
		return gameLengthMinutes * 60 * 20;
	}
}
