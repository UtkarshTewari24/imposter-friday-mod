package com.impostorfridays.game;

import com.impostorfridays.screen.StealScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Builds and resolves the {@code /steal} container.
 *
 * <p>The window shows a SNAPSHOT of the target's inventory. Nothing is removed from the real
 * inventory until the window closes, at which point the server diffs the snapshot itself and
 * applies at most one stack. The client's view is never treated as authoritative.
 */
public final class StealManager {

	// Chest layout: armour + offhand on the top row, a spacer, then main inventory, then hotbar.
	private static final int ARMOUR_ROW_START = 0;
	private static final int SPACER_ROW_START = 9;
	private static final int MAIN_ROW_START = 18;
	private static final int HOTBAR_ROW_START = 45;

	// PlayerInventory indices (verified against 1.21.11: MAIN_SIZE=36, OFF_HAND_SLOT=40).
	private static final int ARMOUR_FIRST = 36;
	private static final int OFFHAND = 40;

	private StealManager() {
	}

	/** Opens the steal window. Cooldown is consumed on OPEN, so it cannot be used for free recon. */
	public static void open(ServerPlayerEntity impostor, ServerPlayerEntity target) {
		Map<Integer, Integer> mapping = new HashMap<>();
		Set<Integer> locked = new HashSet<>();
		SimpleInventory view = new SimpleInventory(StealScreenHandler.CHEST_SLOTS);

		PlayerInventory targetInv = target.getInventory();

		// Top row: helmet, chest, legs, boots, then offhand.
		int[] armourSlots = {ARMOUR_FIRST + 3, ARMOUR_FIRST + 2, ARMOUR_FIRST + 1, ARMOUR_FIRST};
		for (int i = 0; i < armourSlots.length; i++) {
			place(view, mapping, locked, ARMOUR_ROW_START + i, armourSlots[i], targetInv);
		}
		place(view, mapping, locked, ARMOUR_ROW_START + 4, OFFHAND, targetInv);
		for (int i = 5; i < 9; i++) {
			view.setStack(ARMOUR_ROW_START + i, StealScreenHandler.filler(null));
			locked.add(ARMOUR_ROW_START + i);
		}

		// Spacer row.
		for (int i = 0; i < 9; i++) {
			view.setStack(SPACER_ROW_START + i, StealScreenHandler.filler(null));
			locked.add(SPACER_ROW_START + i);
		}

		// Main inventory (target slots 9..35).
		for (int i = 0; i < 27; i++) {
			place(view, mapping, locked, MAIN_ROW_START + i, 9 + i, targetInv);
		}

		// Hotbar (target slots 0..8).
		for (int i = 0; i < 9; i++) {
			place(view, mapping, locked, HOTBAR_ROW_START + i, i, targetInv);
		}

		UUID targetId = target.getUuid();
		impostor.openHandledScreen(new SimpleNamedScreenHandlerFactory(
				(syncId, playerInventory, player) ->
						new StealScreenHandler(syncId, playerInventory, view, mapping, locked, targetId),
				Text.literal(target.getGameProfile().name() + "'s Inventory")
						.formatted(Formatting.DARK_RED)));

		AbilityManager.consumeCooldown(impostor);
	}

	/** Copies one real slot into the view, locking it if it holds the Tracking Compass. */
	private static void place(SimpleInventory view, Map<Integer, Integer> mapping, Set<Integer> locked,
			int chestSlot, int targetSlot, PlayerInventory targetInv) {
		if (targetSlot >= targetInv.size()) {
			view.setStack(chestSlot, StealScreenHandler.filler(null));
			locked.add(chestSlot);
			return;
		}
		ItemStack stack = targetInv.getStack(targetSlot);

		if (StealScreenHandler.isTrackingCompass(stack)) {
			// Shown as an explicit locked marker rather than the real item, so it is
			// visibly non-interactable instead of merely rejecting the click afterwards.
			view.setStack(chestSlot, StealScreenHandler.lockedCompass());
			locked.add(chestSlot);
			return;
		}

		view.setStack(chestSlot, stack.copy());
		mapping.put(chestSlot, targetSlot);
	}

	/**
	 * Applies the single permitted theft to the target's real inventory.
	 *
	 * <p>Runs on window close. Diffs the snapshot against the live inventory and removes at
	 * most one stack, so a desynced or hostile client cannot cause a larger loss.
	 */
	public static void reconcile(StealScreenHandler handler, ServerPlayerEntity impostor) {
		if (!handler.isStealUsed()) {
			return;
		}
		var server = impostor.getEntityWorld().getServer();
		ServerPlayerEntity target = server.getPlayerManager().getPlayer(handler.getTargetId());
		if (target == null) {
			// Target logged off mid-steal; nothing safe to apply.
			return;
		}

		PlayerInventory targetInv = target.getInventory();
		SimpleInventory view = handler.getView();

		for (Map.Entry<Integer, Integer> entry : handler.getSlotMapping().entrySet()) {
			int chestSlot = entry.getKey();
			int targetSlot = entry.getValue();
			if (targetSlot >= targetInv.size()) {
				continue;
			}

			ItemStack remaining = view.getStack(chestSlot);
			ItemStack original = targetInv.getStack(targetSlot);
			if (original.isEmpty()) {
				continue;
			}

			int taken = original.getCount() - (remaining.isEmpty() ? 0 : remaining.getCount());
			if (taken > 0) {
				original.decrement(taken);
				if (original.isEmpty()) {
					targetInv.setStack(targetSlot, ItemStack.EMPTY);
				}
				target.currentScreenHandler.sendContentUpdates();
				// Exactly one stack, then stop — the hard limit the spec requires.
				return;
			}
		}
	}
}
