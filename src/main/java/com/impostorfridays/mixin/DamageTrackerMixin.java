package com.impostorfridays.mixin;

import com.impostorfridays.game.GameManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Anonymises player-vs-player kill messages during a match.
 *
 * <p>Injecting here rather than at the broadcast site is deliberate: the chat broadcast AND
 * the packet that populates the victim's own death screen both read this one method, so a
 * single override closes both leaks. Natural deaths are left completely alone.
 */
@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {

	@Shadow
	@Final
	private LivingEntity entity;

	@Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
	private void impostorfridays$anonymisePlayerKills(CallbackInfoReturnable<Text> cir) {
		if (!GameManager.isActive()) {
			return;
		}
		if (!(this.entity instanceof PlayerEntity)) {
			return;
		}

		DamageSource source = this.entity.getRecentDamageSource();
		if (source == null) {
			return;
		}

		Entity attacker = source.getAttacker();
		// Only anonymise kills BY another player. Fall damage, lava, mobs and suicides keep
		// their normal vanilla messages.
		if (!(attacker instanceof PlayerEntity) || attacker == this.entity) {
			return;
		}

		cir.setReturnValue(Text.literal("Player killed by Player").formatted(Formatting.GRAY));
	}
}
