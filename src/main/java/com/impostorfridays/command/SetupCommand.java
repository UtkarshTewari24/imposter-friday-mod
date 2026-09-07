package com.impostorfridays.command;

import com.impostorfridays.config.GameConfig;
import com.impostorfridays.game.Ability;
import com.impostorfridays.task.Difficulty;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * {@code /amongussetup} — the operator config panel.
 *
 * <p>Rendered as an interactive chat panel rather than a container GUI: it needs no
 * client-side screen, works on vanilla clients, and every control is a real click.
 * See IMPLEMENTATION_NOTES.md for why this was chosen over a chest UI.
 *
 * <p>Edits apply immediately in memory; "Save Settings" writes them to
 * {@code config/amongusgame.properties}.
 */
public final class SetupCommand {
	private SetupCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("amongussetup")
				.requires(Permissions::isAuthorized)
				.executes(ctx -> {
					showPanel(ctx.getSource());
					return 1;
				})
				.then(CommandManager.literal("save")
						.executes(ctx -> {
							GameConfig.get().save();
							ctx.getSource().sendFeedback(() -> Text.literal("✔ Settings saved to config/"
									+ GameConfig.FILE_NAME).formatted(Formatting.GREEN), false);
							return 1;
						}))
				.then(CommandManager.literal("reload")
						.executes(ctx -> {
							GameConfig.get().load();
							ctx.getSource().sendFeedback(() -> Text.literal("↻ Settings reloaded from disk.")
									.formatted(Formatting.YELLOW), false);
							showPanel(ctx.getSource());
							return 1;
						}))
				.then(CommandManager.literal("set")
						.then(CommandManager.argument("key", StringArgumentType.word())
								.then(CommandManager.argument("value", StringArgumentType.word())
										.executes(ctx -> {
											String key = StringArgumentType.getString(ctx, "key");
											String value = StringArgumentType.getString(ctx, "value");
											applySetting(ctx.getSource(), key, value);
											return 1;
										})))));
	}

	// ------------------------------------------------------------------
	// Mutation
	// ------------------------------------------------------------------

	private static void applySetting(ServerCommandSource source, String key, String value) {
		GameConfig cfg = GameConfig.get();
		try {
			switch (key) {
				case "length" -> cfg.setGameLengthMinutes(Integer.parseInt(value));
				case "respawn" -> cfg.setRespawnDelaySeconds(Integer.parseInt(value));
				case "cooldown" -> cfg.setImpostorCooldownSeconds(Integer.parseInt(value));
				case "sniffercd" -> cfg.setSnifferCooldownSeconds(Integer.parseInt(value));
				case "sniffer" -> cfg.setSnifferEnabled(Boolean.parseBoolean(value));
				case "mutedead" -> cfg.setMuteDeadPlayers(Boolean.parseBoolean(value));
				case "difficulty" -> cfg.setDifficulty(Difficulty.byName(value));
				default -> {
					if (key.startsWith("ability.")) {
						Ability a = Ability.byId(key.substring("ability.".length()));
						if (a == null) {
							source.sendError(Text.literal("Unknown ability: " + key));
							return;
						}
						cfg.setAbilityEnabled(a, Boolean.parseBoolean(value));
					} else if (key.startsWith("duration.")) {
						Ability a = Ability.byId(key.substring("duration.".length()));
						if (a == null) {
							source.sendError(Text.literal("Unknown ability: " + key));
							return;
						}
						cfg.setAbilityDurationSeconds(a, Integer.parseInt(value));
					} else {
						source.sendError(Text.literal("Unknown setting: " + key));
						return;
					}
				}
			}
		} catch (NumberFormatException e) {
			source.sendError(Text.literal("'" + value + "' is not a number."));
			return;
		}
		showPanel(source);
	}

	// ------------------------------------------------------------------
	// Panel rendering
	// ------------------------------------------------------------------

	private static void showPanel(ServerCommandSource source) {
		GameConfig cfg = GameConfig.get();

		send(source, Text.literal(""));
		send(source, Text.literal("━━━━━━━ ").formatted(Formatting.DARK_GRAY)
				.append(Text.literal("Impostor Fridays Setup").formatted(Formatting.GOLD, Formatting.BOLD))
				.append(Text.literal(" ━━━━━━━").formatted(Formatting.DARK_GRAY)));

		send(source, header("Match"));
		send(source, numberRow("Game length", cfg.getGameLengthMinutes(), "min", "length", 5));
		send(source, numberRow("Respawn delay", cfg.getRespawnDelaySeconds(), "sec", "respawn", 5));

		send(source, header("Impostor"));
		send(source, numberRow("Ability cooldown", cfg.getImpostorCooldownSeconds(), "sec", "cooldown", 30));
		for (Ability a : Ability.values()) {
			send(source, abilityRow(cfg, a));
		}

		send(source, header("Sniffer"));
		send(source, toggleRow("Sniffer role", cfg.isSnifferEnabled(), "sniffer"));
		send(source, numberRow("Sniff cooldown", cfg.getSnifferCooldownSeconds(), "sec", "sniffercd", 30));

		send(source, header("Tasks"));
		send(source, difficultyRow(cfg));

		send(source, header("Voice chat"));
		send(source, toggleRow("Mute dead players", cfg.isMuteDeadPlayers(), "mutedead"));

		send(source, Text.literal(""));
		send(source, Text.literal("           ")
				.append(button("[ Save Settings ]", "/amongussetup save",
						"Write these settings to disk", Formatting.GREEN))
				.append(Text.literal("  "))
				.append(button("[ Reload ]", "/amongussetup reload",
						"Discard changes and reload from disk", Formatting.GRAY)));
		send(source, Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_GRAY));
	}

	private static Text header(String name) {
		return Text.literal(" " + name).formatted(Formatting.AQUA, Formatting.BOLD);
	}

	/** A label with [-] value [+] controls. */
	private static Text numberRow(String label, int value, String unit, String key, int step) {
		return Text.literal("  " + label + ": ").formatted(Formatting.GRAY)
				.append(button(" [-] ", "/amongussetup set " + key + " " + (value - step),
						"Decrease by " + step, Formatting.RED))
				.append(Text.literal(value + " " + unit).formatted(Formatting.WHITE))
				.append(button(" [+] ", "/amongussetup set " + key + " " + (value + step),
						"Increase by " + step, Formatting.GREEN));
	}

	private static Text toggleRow(String label, boolean on, String key) {
		return Text.literal("  " + label + ": ").formatted(Formatting.GRAY)
				.append(button(on ? "[ ON ]" : "[ OFF ]", "/amongussetup set " + key + " " + (!on),
						"Click to turn " + (on ? "off" : "on"), on ? Formatting.GREEN : Formatting.RED));
	}

	private static Text abilityRow(GameConfig cfg, Ability a) {
		boolean on = cfg.isAbilityEnabled(a);
		MutableText row = Text.literal("   /" + a.getId()).formatted(Formatting.GRAY)
				.append(Text.literal(" ").formatted(Formatting.GRAY))
				.append(button(on ? "[ ON ]" : "[ OFF ]",
						"/amongussetup set ability." + a.getId() + " " + (!on),
						"Enable or disable /" + a.getId(), on ? Formatting.GREEN : Formatting.RED));
		if (a.hasDuration()) {
			int d = cfg.getAbilityDurationSeconds(a);
			row.append(Text.literal("  duration: ").formatted(Formatting.DARK_GRAY))
					.append(button("[-]", "/amongussetup set duration." + a.getId() + " " + (d - 5),
							"Decrease by 5", Formatting.RED))
					.append(Text.literal(" " + d + "s ").formatted(Formatting.WHITE))
					.append(button("[+]", "/amongussetup set duration." + a.getId() + " " + (d + 5),
							"Increase by 5", Formatting.GREEN));
		}
		return row;
	}

	private static Text difficultyRow(GameConfig cfg) {
		MutableText row = Text.literal("  Difficulty: ").formatted(Formatting.GRAY);
		for (Difficulty d : Difficulty.values()) {
			boolean sel = cfg.getDifficulty() == d;
			row.append(button(sel ? "[" + d.getDisplayName() + "]" : " " + d.getDisplayName() + " ",
					"/amongussetup set difficulty " + d.name(),
					"Use the " + d.getDisplayName() + " task pool",
					sel ? Formatting.GOLD : Formatting.DARK_GRAY));
		}
		return row;
	}

	private static MutableText button(String label, String command, String tooltip, Formatting color) {
		return Text.literal(label).styled(s -> s
				.withColor(color)
				.withClickEvent(new ClickEvent.RunCommand(command))
				.withHoverEvent(new HoverEvent.ShowText(Text.literal(tooltip))));
	}

	private static void send(ServerCommandSource source, Text text) {
		source.sendFeedback(() -> text, false);
	}
}
