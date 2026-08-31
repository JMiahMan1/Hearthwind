package dev.jmiahman.hearthwind.world.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.world.SeasonCrops;

/**
 * Seasonal growth for Saplings: 0.0 cancels growth in that season, 1.0 is
 * vanilla speed, above 1.0 grows faster (see {@link SeasonCrops}).
 */
@Mixin(SaplingBlock.class)
public abstract class HearthwindSeasonalSaplingMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void hearthwind_seasonalSaplings$blockDeadSeasons(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        if (SeasonCrops.multiplier(state.getBlock(), level) <= 0.0) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "randomTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    )
    private int hearthwind_seasonalSaplings$redirectNextInt(
            RandomSource instance, int bound,
            BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        return instance.nextInt(Math.max(1, SeasonCrops.scaleRandomTickBound(bound, state.getBlock(), level)));
    }
}
