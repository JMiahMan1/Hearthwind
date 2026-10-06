package dev.jmiahman.hearthwind.survival.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.hearthwind.survival.CampfirePurification;

/** Lets water bottles be placed on campfires and boiled into purified water. */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {
    @Inject(method = "placeFood", at = @At("HEAD"), cancellable = true)
    private void hearthwind$placeWaterBottle(ServerLevel level, @Nullable LivingEntity source,
            ItemStack placeItem, CallbackInfoReturnable<Boolean> cir) {
        if (!CampfirePurification.isWaterPotion(placeItem)) {
            return; // food: leave it to vanilla
        }
        // We own the water-bottle path: cancel whether it lands or not, so a
        // refused bottle is never placed by vanilla's own placeFood instead.
        cir.setReturnValue(CampfirePurification.placeWaterBottle(level, source,
                (CampfireBlockEntity) (Object) this, placeItem));
    }

    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void hearthwind$purifyWaterBottles(ServerLevel level, BlockPos pos,
            BlockState state, CampfireBlockEntity entity,
            RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache,
            CallbackInfo ci) {
        CampfirePurification.tickPurification(level, pos, state, entity);
    }

    /**
     * Aged parity: a boil in progress FREEZES when the fire goes out, it does
     * not decay. Vanilla 26.2's {@code cooldownTick} clamps progress down by
     * two every tick, so a bottle that reached 900 of 1000 and lost its fire
     * silently slid back to zero and could never finish. Take the tick over
     * whenever a water bottle is on the fire: freeze those slots, decay
     * everything else exactly as vanilla does.
     */
    @Inject(method = "cooldownTick", at = @At("HEAD"), cancellable = true)
    private static void hearthwind$freezeBoilOnDarkFire(net.minecraft.world.level.Level level,
            BlockPos pos, BlockState state, CampfireBlockEntity entity, CallbackInfo ci) {
        var accessor = (dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) entity;
        net.minecraft.core.NonNullList<ItemStack> items = accessor.hearthwind$items();
        boolean boiling = false;
        for (int slot = 0; slot < items.size(); slot++) {
            if (CampfirePurification.isWaterPotion(items.get(slot))) {
                boiling = true;
                break;
            }
        }
        if (!boiling) {
            return;
        }
        int[] progress = accessor.hearthwind$cookingProgress();
        int[] time = accessor.hearthwind$cookingTime();
        boolean changed = false;
        for (int slot = 0; slot < items.size(); slot++) {
            if (progress[slot] <= 0) {
                continue;
            }
            if (!CampfirePurification.isWaterPotion(items.get(slot))) {
                progress[slot] = net.minecraft.util.Mth.clamp(progress[slot] - 2, 0, time[slot]);
            }
            changed = true;
        }
        if (changed) {
            entity.setChanged();
        }
        ci.cancel();
    }

    /**
     * Vanilla spawns dark item smoke over every occupied campfire slot; for a
     * boiling water bottle we want steam, so pretend the slot is empty and let
     * {@link CampfirePurification} emit white particles instead.
     */
    @Redirect(method = "particleTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private static boolean hearthwind$steamInsteadOfSmoke(ItemStack stack) {
        return stack.isEmpty() || CampfirePurification.isWaterPotion(stack);
    }
}
