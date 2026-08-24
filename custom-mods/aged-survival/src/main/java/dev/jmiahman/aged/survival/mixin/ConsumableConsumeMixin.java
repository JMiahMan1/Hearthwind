package dev.jmiahman.aged.survival.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.aged.survival.AgedSurvivalDiet;

@Mixin(Consumable.class)
public abstract class ConsumableConsumeMixin {
    @Inject(method = "onConsume",
            at = @At("TAIL"))
    private void aged_survival$afterConsume(Level level, LivingEntity entity,
            ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            AgedSurvivalDiet.onEaten(player, stack);
        }
    }
}
