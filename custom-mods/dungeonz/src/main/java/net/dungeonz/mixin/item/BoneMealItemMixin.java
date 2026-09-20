package net.dungeonz.mixin.item;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {

    @Inject(method = "growCrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BonemealableBlock;performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"), cancellable = true)
    private static void useOnFertilizableMixin(ItemStack stack, Level world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (world.dimension() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "growWaterPlant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getRandom()Lnet/minecraft/util/RandomSource;"), cancellable = true)
    private static void useOnGroundMixin(ItemStack stack, Level world, BlockPos blockPos, @Nullable Direction facing, CallbackInfoReturnable<Boolean> info) {
        if (world.dimension() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }
}
