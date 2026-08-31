package dev.jmiahman.hearthwind.survival.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.hearthwind.survival.DietGates;
import dev.jmiahman.hearthwind.survival.HearthwindSurvivalDiet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

@Mixin(Consumable.class)
public abstract class ConsumableConsumeMixin {
    @Inject(method = "onConsume",
            at = @At("HEAD"),
            cancellable = true)
    private void aged_survival$beforeConsume(Level level, LivingEntity entity,
            ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof ServerPlayer player) {
            // Flasks: decrement fill + hydration + thirst roll; skip the
            // food/diet path entirely (a flask is not food).
            if (stack.is(dev.jmiahman.hearthwind.survival.FlaskItems.LEATHER_FLASK)
                    || stack.is(dev.jmiahman.hearthwind.survival.FlaskItems.IRON_LEATHER_FLASK)
                    || stack.is(dev.jmiahman.hearthwind.survival.FlaskItems.GOLDEN_LEATHER_FLASK)
                    || stack.is(dev.jmiahman.hearthwind.survival.FlaskItems.DIAMOND_LEATHER_FLASK)
                    || stack.is(dev.jmiahman.hearthwind.survival.FlaskItems.NETHERITE_LEATHER_FLASK)) {
                cir.setReturnValue(dev.jmiahman.hearthwind.survival.FlaskItems.onFlaskConsumed(player, stack));
                return;
            }
            if (!DietGates.allowed(player, stack)) {
                cir.setReturnValue(stack);
            }
        }
    }

    @Inject(method = "onConsume",
            at = @At("TAIL"))
    private void aged_survival$afterConsume(Level level, LivingEntity entity,
            ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof ServerPlayer player) {
            HearthwindSurvivalDiet.onEaten(player, stack);
            dev.jmiahman.hearthwind.survival.HydrationCorpus.hydrateOnConsume(player, stack);
        }
    }
}
