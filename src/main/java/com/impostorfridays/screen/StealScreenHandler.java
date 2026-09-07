package com.impostorfridays.screen;

import com.impostorfridays.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The {@code /steal} container: a real 9x6 chest view of the target's inventory.
 *
 * <p>Deliberately a genuine container rather than a click-to-transfer menu — items are picked
 * up, carried on the cursor and dropped into your own inventory as usual.
 *
 * <p>Two hard rules, both enforced server-side because the client is not trusted:
 * <ul>
 *   <li>Exactly ONE stack may leave the target per activation.</li>
 *   <li>The Tracking Compass can never be taken.</li>
 * </ul>
 */
public class StealScreenHandler extends GenericContainerScreenHandler {

	public static final int ROWS = 6;
	public static final int CHEST_SLOTS = ROWS * 9;

	/** Chest slot -> the target's real inventory slot it mirrors. */
	private final Map<Integer, Integer> slotMapping;
	private final UUID targetId;
	private final SimpleInventory view;

	/** Chest slots that must never be interactive (filler, and the locked compass). */
	private final java.util.Set<Integer> lockedSlots;

	/**
	 * Immutable copy of what each mapped slot held when the window opened.
	 *
	 * <p>Reconciliation must diff against THIS, not against the target's live inventory — the
	 * target is still playing and may pick up or use items while the window is open. Diffing
	 * against the live stack would remove the wrong amount in both directions.
	 */
	private final Map<Integer, ItemStack> originalStacks = new HashMap<>();

	/** Once one stack has been taken, every further steal is refused. */
	private boolean stealUsed;

	public StealScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory view,
			Map<Integer, Integer> slotMapping, java.util.Set<Integer> lockedSlots, UUID targetId) {
		super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, view, ROWS);
		this.view = view;
		this.slotMapping = slotMapping;
		this.lockedSlots = lockedSlots;
		this.targetId = targetId;

		// The view is an untouched snapshot at construction time, so copy it now.
		for (Integer chestSlot : slotMapping.keySet()) {
			originalStacks.put(chestSlot, view.getStack(chestSlot).copy());
		}
	}

	/** What this slot held when the window opened. Never null for a mapped slot. */
	public ItemStack getOriginalStack(int chestSlot) {
		return originalStacks.getOrDefault(chestSlot, ItemStack.EMPTY);
	}

	public UUID getTargetId() {
		return targetId;
	}

	public boolean isStealUsed() {
		return stealUsed;
	}

	public SimpleInventory getView() {
		return view;
	}

	public Map<Integer, Integer> getSlotMapping() {
		return slotMapping;
	}

	/** True while the given index refers to the target's half of the window. */
	private boolean isChestSlot(int slotIndex) {
		return slotIndex >= 0 && slotIndex < CHEST_SLOTS;
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		// Locked slots (filler + the compass) are inert for EVERY click type, including
		// hotbar/offhand swaps, which would otherwise bypass a take-only check.
		if (isChestSlot(slotIndex) && lockedSlots.contains(slotIndex)) {
			return;
		}

		int beforeCount = countChestItems();

		if (stealUsed && isChestSlot(slotIndex)) {
			// Already taken this activation — refuse further interaction with the target's side.
			if (player instanceof ServerPlayerEntity serverPlayer) {
				serverPlayer.sendMessage(Text.literal("You can only steal one item per use.")
						.formatted(Formatting.RED), true);
			}
			return;
		}

		super.onSlotClick(slotIndex, button, actionType, player);

		// If the target's side lost anything, that was the one permitted steal.
		if (countChestItems() < beforeCount) {
			stealUsed = true;
			if (player instanceof ServerPlayerEntity serverPlayer) {
				serverPlayer.sendMessage(Text.literal("Item stolen. Close the menu to finish.")
						.formatted(Formatting.GREEN), true);
			}
		}
	}

	/** Total item count on the target's side of the window. */
	private int countChestItems() {
		int total = 0;
		for (int i = 0; i < view.size(); i++) {
			ItemStack stack = view.getStack(i);
			if (!stack.isEmpty() && !isFiller(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	static boolean isFiller(ItemStack stack) {
		return stack.isOf(Items.GRAY_STAINED_GLASS_PANE) || stack.isOf(Items.BARRIER);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		// Apply the theft to the target's REAL inventory only now, from the server's own
		// diff of the snapshot — never from anything the client asserted.
		if (player instanceof ServerPlayerEntity serverPlayer) {
			com.impostorfridays.game.StealManager.reconcile(this, serverPlayer);
		}
		super.onClosed(player);
	}

	/** A visibly inert placeholder used for padding and for the locked compass slot. */
	public static ItemStack filler(String label) {
		ItemStack stack = new ItemStack(label == null ? Items.GRAY_STAINED_GLASS_PANE : Items.BARRIER);
		stack.set(DataComponentTypes.CUSTOM_NAME,
				Text.literal(label == null ? " " : label).formatted(Formatting.DARK_GRAY));
		return stack;
	}

	/** Placeholder shown where the Tracking Compass sits, so it reads as locked, not missing. */
	public static ItemStack lockedCompass() {
		ItemStack stack = new ItemStack(Items.BARRIER);
		stack.set(DataComponentTypes.CUSTOM_NAME,
				Text.literal("Tracking Compass (locked)").formatted(Formatting.DARK_RED));
		return stack;
	}

	/** Convenience for callers building the view. */
	public static boolean isTrackingCompass(ItemStack stack) {
		return stack.isOf(ModItems.TRACKING_COMPASS);
	}

	public static Inventory emptyView() {
		return new SimpleInventory(CHEST_SLOTS);
	}
}
