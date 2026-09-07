package com.impostorfridays.client.mixin;

import com.impostorfridays.client.ClientGameState;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The client half of {@code /gravity}: rolls the camera 180 degrees so the world appears
 * upside down.
 *
 * <p>Every signature here was read off the real 1.21.11 mappings rather than recalled.
 * Two things in particular differ from older versions and would fail at runtime if assumed:
 * {@code update} takes a {@link World} (not a BlockView), and the rotation is built by
 * {@code Quaternionf.rotationYXZ(yaw, pitch, roll)} whose THIRD argument is the roll we
 * override from 0 to PI.
 *
 * <p>Reproduces vanilla's own {@code setRotation} maths exactly, then recomputes the three
 * derived basis vectors — skipping that step leaves movement and projection out of sync
 * with what is drawn.
 *
 * <p>The flip is driven entirely by server-pushed state read fresh every frame, so dying,
 * respawning, disconnecting, or {@code /end} can never strand a player upside down.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	@Final
	private static Vector3f HORIZONTAL;
	@Shadow
	@Final
	private static Vector3f VERTICAL;
	@Shadow
	@Final
	private static Vector3f DIAGONAL;

	@Shadow
	@Final
	private Vector3f horizontalPlane;
	@Shadow
	@Final
	private Vector3f verticalPlane;
	@Shadow
	@Final
	private Vector3f diagonalPlane;
	@Shadow
	@Final
	private Quaternionf rotation;

	@Shadow
	private float yaw;
	@Shadow
	private float pitch;

	/** Vanilla's own degrees-to-radians constant, kept identical so the maths matches exactly. */
	@org.spongepowered.asm.mixin.Unique
	private static final float IMPOSTORFRIDAYS$DEG_TO_RAD = 0.017453292f;

	@Inject(method = "update", at = @At("TAIL"))
	private void impostorfridays$applyGravityFlip(World world, Entity focusedEntity,
			boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (!ClientGameState.active || !ClientGameState.gravityActive) {
			return;
		}

		// Same as vanilla setRotation, but with roll = PI instead of 0.
		this.rotation.rotationYXZ(
				(float) Math.PI - this.yaw * IMPOSTORFRIDAYS$DEG_TO_RAD,
				-this.pitch * IMPOSTORFRIDAYS$DEG_TO_RAD,
				(float) Math.PI);

		HORIZONTAL.rotate(this.rotation, this.horizontalPlane);
		VERTICAL.rotate(this.rotation, this.verticalPlane);
		DIAGONAL.rotate(this.rotation, this.diagonalPlane);
	}
}
