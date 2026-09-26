package dev.jmiahman.hearthwind.survival.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import dev.jmiahman.hearthwind.survival.HearthwindSurvivalThirst;
import net.minecraft.world.entity.player.Player;

/**
 * Dehydration parity: every vanilla food exhaustion point (sprint, jump,
 * attack, mining, swimming) also charges the thirst dehydration buffer at
 * {@code exhaustion / hydrating_factor}. Injection sits after the
 * server-side {@code FoodData.addExhaustion} call, exactly like upstream's
 * {@code PlayerEntity.addExhaustion} hook, so creative players (early
 * return) are exempt.
 */
@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {
    @Inject(method = "causeFoodExhaustion",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V",
                    shift = At.Shift.AFTER))
    private void hearthwind$addThirstDehydration(float exhaustion, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!HearthwindSurvivalThirst.hasThirst(player)) {
            return;
        }
        HearthwindSurvivalConfig.Thirst cfg = HearthwindSurvivalConfig.get().thirst;
        float scaled = exhaustion;
        if (cfg.harderNether && player.level().dimension().equals(net.minecraft.world.level.Level.NETHER)) {
            scaled *= (float) cfg.netherFactor;
        }
        HearthwindSurvivalThirst.addDehydration(player, (float) (scaled / cfg.hydratingFactor));
    }
}
