package dev.jmiahman.hearthwind.world.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.world.SeasonCrops;

/**
 * Scales crop growth by the crop's per-season multiplier
 * (see {@link SeasonCrops}): 0.0 cancels growth entirely, 1.5 grows 1.5x as
 * fast. Bonemeal is deliberately left at vanilla speed.
 */
@Mixin(CropBlock.class)
public abstract class HearthwindSeasonalCropMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void hearthwind_seasonalCrops$blockDeadSeasons(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        if (SeasonCrops.multiplier(state.getBlock(), level) <= 0.0) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "randomTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    )
    private int hearthwind_seasonalCrops$redirectNextInt(
            RandomSource instance, int bound,
            BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        int scaled = SeasonCrops.scaleRandomTickBound(bound, state.getBlock(), level);
        return instance.nextInt(Math.max(1, scaled));
    }
}
