package com.impostorfridays.game;

import com.impostorfridays.item.ModItems;
import com.impostorfridays.net.OpenPickerS2C;
import com.impostorfridays.net.PickerMode;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Drives the Tracking Compass needle.
 *
 * <p>The needle is the vanilla lodestone tracker, whose target is rewritten server-side each
 * tick. Because a lodestone target in another dimension just makes the needle spin, targets
 * are projected into the holder's own dimension (with the 8:1 Nether scale applied) so the
 * compass keeps pointing sensibly across dimensions.
 */
public final class TrackingManager {

	/** How often the needle target is recomputed. Every few ticks is smooth enough and cheap. */
	private static final int UPDATE_INTERVAL = 5;

	/** Tracker -> tracked player. */
	private static final Map<UUID, UUID> targets = new HashMap<>();
	/** Trackers using the "Nearest Player" pseudo-target. */
	private static final Map<UUID, Boolean> nearestMode = new HashMap<>();

	private static int tickCounter;

	private TrackingManager() {
	}

	public static void clear() {
		targets.clear();
		nearestMode.clear();
	}

	// ------------------------------------------------------------------
	// Picker
	// ------------------------------------------------------------------

	/** Sends the candidate list so the client can open the picker. */
	public static void requestPicker(ServerPlayerEntity player) {
		if (!GameManager.isActive()) {
			player.sendMessage(Text.literal("No game is running.").formatted(Formatting.RED), true);
			return;
		}
		ServerPlayNetworking.send(player,
				new OpenPickerS2C(PickerMode.TRACK.ordinal(), candidatesFor(player)));
	}

	/** Everyone except the requester. */
	public static List<OpenPickerS2C.Entry> candidatesFor(ServerPlayerEntity requester) {
		List<OpenPickerS2C.Entry> entries = new ArrayList<>();
		for (ServerPlayerEntity p : requester.getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
			if (!p.getUuid().equals(requester.getUuid())) {
				entries.add(new OpenPickerS2C.Entry(p.getUuid(), p.getGameProfile().name()));
			}
		}
		return entries;
	}

	/** Applies a validated selection. */
	public static void setTarget(ServerPlayerEntity tracker, UUID target, boolean nearest) {
		if (nearest) {
			nearestMode.put(tracker.getUuid(), true);
			targets.remove(tracker.getUuid());
			tracker.sendMessage(Text.literal("Now tracking: ")
					.formatted(Formatting.GRAY)
					.append(Text.literal("Nearest Player").formatted(Formatting.AQUA)), true);
			return;
		}
		nearestMode.remove(tracker.getUuid());
		targets.put(tracker.getUuid(), target);

		ServerPlayerEntity targetPlayer = tracker.getEntityWorld().getServer().getPlayerManager().getPlayer(target);
		String name = targetPlayer != null ? targetPlayer.getGameProfile().name() : "Unknown";
		tracker.sendMessage(Text.literal("Now tracking: ")
				.formatted(Formatting.GRAY)
				.append(Text.literal(name).formatted(Formatting.AQUA)), true);
	}

	// ------------------------------------------------------------------
	// Needle updates
	// ------------------------------------------------------------------

	public static void tick(MinecraftServer server) {
		if (!GameManager.isActive()) {
			return;
		}
		if (++tickCounter < UPDATE_INTERVAL) {
			return;
		}
		tickCounter = 0;

		for (ServerPlayerEntity holder : server.getPlayerManager().getPlayerList()) {
			ServerPlayerEntity target = resolveTarget(server, holder);
			if (target == null) {
				continue;
			}
			GlobalPos pos = projectInto(holder, target);
			updateCompasses(holder, pos);
		}
	}

	private static ServerPlayerEntity resolveTarget(MinecraftServer server, ServerPlayerEntity holder) {
		if (Boolean.TRUE.equals(nearestMode.get(holder.getUuid()))) {
			return findNearest(server, holder);
		}
		UUID targetId = targets.get(holder.getUuid());
		if (targetId == null) {
			return null;
		}
		ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetId);
		// Target logged off — keep the entry so it resumes if they return.
		return target;
	}

	private static ServerPlayerEntity findNearest(MinecraftServer server, ServerPlayerEntity holder) {
		ServerPlayerEntity best = null;
		double bestDist = Double.MAX_VALUE;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (p.getUuid().equals(holder.getUuid()) || p.getEntityWorld() != holder.getEntityWorld()) {
				continue;
			}
			double d = p.squaredDistanceTo(holder);
			if (d < bestDist) {
				bestDist = d;
				best = p;
			}
		}
		return best;
	}

	/**
	 * Converts the target's position into a point in the holder's own dimension, so the
	 * needle points at a meaningful bearing rather than spinning.
	 */
	private static GlobalPos projectInto(ServerPlayerEntity holder, ServerPlayerEntity target) {
		RegistryKey<World> holderDim = holder.getEntityWorld().getRegistryKey();
		RegistryKey<World> targetDim = target.getEntityWorld().getRegistryKey();

		double x = target.getX();
		double z = target.getZ();

		if (!holderDim.equals(targetDim)) {
			boolean holderInNether = holderDim.equals(World.NETHER);
			boolean targetInNether = targetDim.equals(World.NETHER);
			if (holderInNether && !targetInNether) {
				x /= 8.0;
				z /= 8.0;
			} else if (!holderInNether && targetInNether) {
				x *= 8.0;
				z *= 8.0;
			}
		}
		return GlobalPos.create(holderDim, BlockPos.ofFloored(x, holder.getY(), z));
	}

	private static void updateCompasses(ServerPlayerEntity holder, GlobalPos pos) {
		// tracked=false skips vanilla's "is there really a lodestone there?" check, which would
		// otherwise wipe the target immediately.
		LodestoneTrackerComponent tracker =
				new LodestoneTrackerComponent(Optional.of(pos), false);

		var inventory = holder.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.isOf(ModItems.TRACKING_COMPASS)) {
				stack.set(DataComponentTypes.LODESTONE_TRACKER, tracker);
			}
		}
	}

	/**
	 * Removes every Tracking Compass from every player.
	 *
	 * <p>Called on {@code /end}. Without this a previous round's Impostor would still be
	 * holding a compass during the next match — both a role leak and a source of confusion.
	 * This is the only inventory change {@code /end} makes, and it only ever removes a mod
	 * item, never anything the players found or built.
	 */
	public static void removeAllCompasses(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			var inventory = player.getInventory();
			for (int i = 0; i < inventory.size(); i++) {
				if (inventory.getStack(i).isOf(ModItems.TRACKING_COMPASS)) {
					inventory.setStack(i, ItemStack.EMPTY);
				}
			}
		}
	}

	/**
	 * Strips Tracking Compasses from one player unless they are the current Impostor.
	 *
	 * <p>{@code /end} can only clear compasses from players who are online. Someone who was
	 * offline at the time would still be carrying one when they next log in — and if they are
	 * not the Impostor that round, it both leaks and confuses. Checked on every join.
	 */
	public static void stripCompassUnlessImpostor(ServerPlayerEntity player) {
		GameState state = GameManager.getState();
		if (state != null && state.isImpostor(player.getUuid())) {
			return;
		}
		var inventory = player.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			if (inventory.getStack(i).isOf(ModItems.TRACKING_COMPASS)) {
				inventory.setStack(i, ItemStack.EMPTY);
			}
		}
	}

	/** Gives the holder a Tracking Compass if they don't already have one. */
	public static void giveCompass(ServerPlayerEntity player) {
		var inventory = player.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			if (inventory.getStack(i).isOf(ModItems.TRACKING_COMPASS)) {
				return;
			}
		}
		ItemStack stack = new ItemStack(ModItems.TRACKING_COMPASS);
		if (!inventory.insertStack(stack)) {
			player.dropItem(stack, false);
		}
	}
}
