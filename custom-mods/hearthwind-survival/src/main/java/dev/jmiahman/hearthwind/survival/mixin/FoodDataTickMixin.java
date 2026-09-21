package dev.jmiahman.hearthwind.survival.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalDiet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

/**
 * NutritionZ parity decay: the original injects after the
 * {@code Math.max(foodLevel - 1, 0)} assignment inside HungerManager.update
 * (FoodData.tick in 26.2), so each hunger point lost to exhaustion drops
 * every nutrient by one.
 */
@Mixin(FoodData.class)
public abstract class FoodDataTickMixin {
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0))
    private void hearthwind$decayNutrition(ServerPlayer player, CallbackInfo ci) {
        HearthwindSurvivalDiet.applyDecay(player);
    }
}
