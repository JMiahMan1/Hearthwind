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
        if (CampfirePurification.placeWaterBottle(level, source,
                (CampfireBlockEntity) (Object) this, placeItem)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void hearthwind$purifyWaterBottles(ServerLevel level, BlockPos pos,
            BlockState state, CampfireBlockEntity entity,
            RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache,
            CallbackInfo ci) {
        CampfirePurification.tickPurification(level, pos, state, entity);
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
