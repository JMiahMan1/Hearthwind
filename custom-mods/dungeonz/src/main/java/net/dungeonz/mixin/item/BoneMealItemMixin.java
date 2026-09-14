package net.dungeonz.mixin.item;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.minecraft.class_1752;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;

@Mixin(class_1752.class)
public class BoneMealItemMixin {

    @Inject(method = "useOnFertilizable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Fertilizable;grow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"), cancellable = true)
    private static void useOnFertilizableMixin(class_1799 stack, class_1937 world, class_2338 pos, CallbackInfoReturnable<Boolean> info) {
        if (world.method_27983() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "useOnGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRandom()Lnet/minecraft/util/math/random/Random;"), cancellable = true)
    private static void useOnGroundMixin(class_1799 stack, class_1937 world, class_2338 blockPos, @Nullable class_2350 facing, CallbackInfoReturnable<Boolean> info) {
        if (world.method_27983() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }
}
