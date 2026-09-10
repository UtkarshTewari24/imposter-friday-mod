package com.impostorfridays.mixin;

import com.impostorfridays.task.TaskManager;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Counts completed villager trades.
 *
 * <p>Fabric has no trade event, and several objectives are defined in terms of trades, so this
 * hooks the one place every completed trade passes through. Injected at HEAD purely as an
 * observer — it never alters the trade.
 */
@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin {

	@Inject(method = "trade", at = @At("HEAD"))
	private void impostorfridays$countTrade(TradeOffer offer, CallbackInfo ci) {
		TaskManager.onVillagerTrade();
	}
}
