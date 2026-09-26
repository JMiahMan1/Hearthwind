package dev.jmiahman.hearthwind.survival.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import dev.jmiahman.hearthwind.survival.HearthwindSurvivalThirst;
import net.minecraft.server.level.ServerPlayer;

/**
 * Dehydration parity: waking from a real sleep (sleep timer >= 100) costs
 * {@code sleep_thirst_consumption} thirst and {@code sleep_hunger_consumption}
 * hunger points. Upstream injected the same logic at the sleepTimer field
 * read inside {@code PlayerEntity.wakeUp}.
 */
@Mixin(ServerPlayer.class)
public abstract class PlayerSleepMixin {
    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void hearthwind$sleepConsumption(boolean wakeImmediately, boolean updateLevelForSleepingPlayers,
            CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.level().isClientSide() || !HearthwindSurvivalThirst.hasThirst(player)
                || player.getSleepTimer() < 100) {
            return;
        }
        HearthwindSurvivalConfig.Thirst cfg = HearthwindSurvivalConfig.get().thirst;
        HearthwindSurvivalThirst.addThirst(player, -cfg.sleepThirstConsumption);
        int food = player.getFoodData().getFoodLevel();
        if (food > 0) {
            player.getFoodData().setFoodLevel(Math.max(food - cfg.sleepHungerConsumption, 0));
        }
    }
}
