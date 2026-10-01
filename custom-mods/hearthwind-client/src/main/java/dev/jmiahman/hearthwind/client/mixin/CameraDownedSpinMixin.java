package dev.jmiahman.hearthwind.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.client.DownedHud;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

/**
 * revive 1.0.7's death-camera spin.
 *
 * <p>The reference's {@code client/CameraMixin} injects into {@code Camera.update}
 * and, while {@code deathTime > 20} with {@code thirdPersonOnDeath} set, calls
 * {@code Camera.setRotation(deathTime * CONFIG.rotationSpeed, 0.0f)} every
 * frame - an ABSOLUTE yaw that keeps increasing, so the camera revolves once
 * per {@code 360 / rotationSpeed} ticks. Aged ships
 * {@code rotationSpeed: 0.3} (the mod's own default is 0.4), which is one
 * revolution every 1200 ticks, i.e. 60 seconds.
 *
 * <p>26.2's {@code Camera.minecraft} is private and {@code setRotation} is
 * protected, so both are reached with shadows. The spin is driven off the
 * downed overlay rather than {@code deathTime}: our player is never
 * technically dead, so {@code deathTime} never advances and the reference's
 * own gate could never fire.
 */
@Mixin(Camera.class)
public abstract class CameraDownedSpinMixin {

    /** {@code ReviveConfig.rotationSpeed} as Aged ships it. One turn per 60s. */
    private static final float ROTATION_SPEED = 0.3f;

    /** The reference's gate: the spin starts a second after the down. */
    private static final int SPIN_DELAY_TICKS = 20;

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "update", at = @At("TAIL"))
    private void hearthwind$spinWhileDowned(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.player == null || !DownedHud.isDowned()) {
            return;
        }
        int downedTicks = minecraft.player.tickCount;
        if (downedTicks <= SPIN_DELAY_TICKS) {
            return;
        }
        setRotation(downedTicks * ROTATION_SPEED, 0.0f);
    }
}