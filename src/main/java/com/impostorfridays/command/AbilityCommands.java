package com.impostorfridays.command;

import com.impostorfridays.game.Ability;
import com.impostorfridays.game.AbilityManager;
import com.impostorfridays.game.GameManager;
import com.impostorfridays.game.SnifferManager;
import com.impostorfridays.game.StealManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

/**
 * The Impostor ability commands, plus the Sniffer's {@code /sniff}.
 *
 * <p>None of these use {@code .requires(...)} for the role check: an Innocent typing
 * {@code /blind} should get a clear refusal, not a "command not found" that quietly confirms
 * they are not the Impostor.
 */
public final class AbilityCommands {

	private AbilityCommands() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		registerSimple(dispatcher, "blind", Ability.BLIND);
		registerSimple(dispatcher, "hunt", Ability.HUNT);
		registerSimple(dispatcher, "gravity", Ability.GRAVITY);
		registerSwap(dispatcher);
		registerSteal(dispatcher);
		registerSniff(dispatcher);
	}

	/** Abilities that just fire with no arguments. */
	private static void registerSimple(CommandDispatcher<ServerCommandSource> dispatcher,
			String name, Ability ability) {
		dispatcher.register(CommandManager.literal(name).executes(ctx -> {
			ServerPlayerEntity player = ctx.getSource().getPlayer();
			if (player == null) {
				ctx.getSource().sendError(Text.literal("Only a player can use this."));
				return 0;
			}
			Text error = AbilityManager.validate(player, ability);
			if (error != null) {
				ctx.getSource().sendError(error);
				return 0;
			}

			var server = ctx.getSource().getServer();
			switch (ability) {
				case BLIND -> AbilityManager.startBlind(server, player);
				case HUNT -> AbilityManager.startHunt(server, player);
				case GRAVITY -> AbilityManager.startGravity(server, player);
				default -> {
				}
			}
			AbilityManager.consumeCooldown(player);
			player.sendMessage(Text.literal("/" + ability.getId() + " activated.")
					.formatted(Formatting.GREEN), true);
			return 1;
		}));
	}

	private static void registerSwap(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("swap")
				.then(CommandManager.argument("player1", EntityArgumentType.player())
						.then(CommandManager.argument("player2", EntityArgumentType.player())
								.executes(ctx -> {
									ServerPlayerEntity caster = ctx.getSource().getPlayer();
									if (caster == null) {
										ctx.getSource().sendError(Text.literal("Only a player can use this."));
										return 0;
									}
									Text error = AbilityManager.validate(caster, Ability.SWAP);
									if (error != null) {
										ctx.getSource().sendError(error);
										return 0;
									}

									ServerPlayerEntity a = EntityArgumentType.getPlayer(ctx, "player1");
									ServerPlayerEntity b = EntityArgumentType.getPlayer(ctx, "player2");
									if (a.getUuid().equals(b.getUuid())) {
										ctx.getSource().sendError(Text.literal(
												"Pick two different players to swap.").formatted(Formatting.RED));
										return 0;
									}

									swap(a, b);
									AbilityManager.consumeCooldown(caster);
									caster.sendMessage(Text.literal("Swapped "
											+ a.getGameProfile().name() + " and "
											+ b.getGameProfile().name() + ".")
											.formatted(Formatting.GREEN), true);
									return 1;
								}))));
	}

	/** Swaps two players' positions, correctly across dimensions. */
	private static void swap(ServerPlayerEntity a, ServerPlayerEntity b) {
		ServerWorld worldA = (ServerWorld) a.getEntityWorld();
		ServerWorld worldB = (ServerWorld) b.getEntityWorld();
		Vec3d posA = new Vec3d(a.getX(), a.getY(), a.getZ());
		Vec3d posB = new Vec3d(b.getX(), b.getY(), b.getZ());
		float yawA = a.getYaw();
		float pitchA = a.getPitch();
		float yawB = b.getYaw();
		float pitchB = b.getPitch();

		a.teleportTo(new TeleportTarget(worldB, posB, Vec3d.ZERO, yawB, pitchB,
				TeleportTarget.NO_OP));
		b.teleportTo(new TeleportTarget(worldA, posA, Vec3d.ZERO, yawA, pitchA,
				TeleportTarget.NO_OP));
	}

	/**
	 * {@code /steal} takes no argument: it opens the shared player picker so the Impostor
	 * chooses a target from player heads, exactly like the tracking and sniff UIs.
	 */
	private static void registerSteal(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("steal").executes(ctx -> {
			ServerPlayerEntity player = ctx.getSource().getPlayer();
			if (player == null) {
				ctx.getSource().sendError(Text.literal("Only a player can use this."));
				return 0;
			}
			Text error = AbilityManager.validate(player, Ability.STEAL);
			if (error != null) {
				ctx.getSource().sendError(error);
				return 0;
			}
			StealManager.requestPicker(player);
			return 1;
		}));
	}

	private static void registerSniff(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("sniff").executes(ctx -> {
			ServerPlayerEntity player = ctx.getSource().getPlayer();
			if (player == null) {
				ctx.getSource().sendError(Text.literal("Only a player can use this."));
				return 0;
			}
			if (!GameManager.isActive()) {
				ctx.getSource().sendError(Text.literal("No game is running.").formatted(Formatting.RED));
				return 0;
			}
			SnifferManager.requestPicker(player);
			return 1;
		}));
	}
}
