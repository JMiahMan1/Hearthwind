package brightspark.asynclocator.mixins;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.logic.EyeOfEnderData;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EyeOfEnder.class)
public class EyeOfEnderMixin implements EyeOfEnderData {
	private boolean locateTaskOngoing = false;

	@Override
	public void setLocateTaskOngoing(boolean locateTaskOngoing) {
		this.locateTaskOngoing = locateTaskOngoing;
	}

	/*
		Skip EyeOfEnder tick body while an async locate task is ongoing so the entity
		doesn't move or die before a location result arrives.
	 */
	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;tick()V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	public void skipTick(CallbackInfo ci) {
		if (locateTaskOngoing) {
			ALConstants.logDebug("Intercepted EyeOfEnder#tick call - skipping tick");
			ci.cancel();
		}
	}
}
