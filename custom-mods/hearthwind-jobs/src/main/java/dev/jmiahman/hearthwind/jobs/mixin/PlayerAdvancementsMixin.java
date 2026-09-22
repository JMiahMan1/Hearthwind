package dev.jmiahman.hearthwind.jobs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

import dev.jmiahman.hearthwind.jobs.PlayerAdvancementTracker;

/**
 * Routes Age advancement completions into {@link PlayerAdvancementTracker}.
 * Vanilla applies rewards/toasts only on the !done -> done transition of
 * {@code award}; injecting on the rewards call observes exactly that moment.
 * The HEAD inject additionally holds age5 closed until smithing 20 +
 * builder job level 3 are met (vanilla criteria cannot express skill levels).
 */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private void hearthwind_jobs$gateAge5(AdvancementHolder advancement, String criterion,
            CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = this.player;
        if (self == null || !PlayerAdvancementTracker.isAge5(advancement.id())) {
            return;
        }
        if (PlayerAdvancementTracker.meetsAge5Gates(self)) {
            return;
        }
        AdvancementProgress progress = ((PlayerAdvancements) (Object) this)
                .getOrStartProgress(advancement);
        if (!progress.isDone()) {
            PlayerAdvancementTracker.notifyAge5Gated(self);
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "award", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void hearthwind_jobs$onAdvancementCompleted(AdvancementHolder advancement,
            String criterion, CallbackInfoReturnable<Boolean> cir) {
        PlayerAdvancementTracker.onAdvancementDone(this.player, advancement);
    }
}
