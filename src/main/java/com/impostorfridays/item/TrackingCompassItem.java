package com.impostorfridays.item;

import com.impostorfridays.game.TrackingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The Impostor's Tracking Compass.
 *
 * <p>Right-clicking asks the server to open the shared player picker. The needle itself is
 * driven by the vanilla lodestone-tracker component, updated server-side each tick.
 */
public class TrackingCompassItem extends Item {

	public TrackingCompassItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		// Opening the picker is a server decision — the client cannot summon it itself.
		if (!world.isClient() && user instanceof ServerPlayerEntity player) {
			TrackingManager.requestPicker(player);
		}
		return ActionResult.SUCCESS;
	}
}
