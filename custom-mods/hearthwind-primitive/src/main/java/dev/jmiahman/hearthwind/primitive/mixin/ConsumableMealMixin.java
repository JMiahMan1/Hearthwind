package dev.jmiahman.hearthwind.primitive.mixin;

import dev.jmiahman.hearthwind.primitive.BeginnerForgiveness;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Beginner-forgiveness meal reset: finishing any FOOD consume clears the
 * earlystage death-count (Aged parity). Lives in primitive (not survival)
 * so the survival→primitive dependency direction stays one-way.
 * Flasks/water have no FOOD component and are excluded.
 */
@Mixin(Consumable.class)
public abstract class ConsumableMealMixin {
    @Inject(method = "onConsume", at = @At("TAIL"))
    private void hearthwind$onMeal(Level level, LivingEntity entity, ItemStack stack,
            CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof ServerPlayer player && stack.has(DataComponents.FOOD)) {
            BeginnerForgiveness.onMeal(player);
        }
    }
}
