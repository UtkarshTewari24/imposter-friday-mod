package com.impostorfridays.command;

import com.impostorfridays.game.GameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Registration for the match lifecycle commands. */
public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerLifecycle(dispatcher);
			SetupCommand.register(dispatcher);
			AbilityCommands.register(dispatcher);
			ResetCommand.register(dispatcher);
		});
	}

	private static void registerLifecycle(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("start")
				.executes(ctx -> {
					ServerCommandSource source = ctx.getSource();
					if (!Permissions.checkAuthorized(source)) {
						return 0;
					}
					Text error = GameManager.start(source.getServer());
					if (error != null) {
						source.sendError(error);
						return 0;
					}
					return 1;
				}));

		dispatcher.register(CommandManager.literal("end")
				.executes(ctx -> {
					ServerCommandSource source = ctx.getSource();
					if (!Permissions.checkAuthorized(source)) {
						return 0;
					}
					if (!GameManager.isActive()) {
						source.sendFeedback(() -> Text.literal("No game is running.")
								.formatted(Formatting.YELLOW), false);
						return 0;
					}
					GameManager.end(source.getServer());
					return 1;
				}));
	}
}
