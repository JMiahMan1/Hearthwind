package dev.jmiahman.hearthwind.survival.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.hearthwind.survival.PurifiedWater;

/**
 * Vanilla water agrees to be replaced by purified water, in any direction.
 *
 * <p>This is the second half of the reference's {@code WaterFluidMixin}, and the
 * half that actually decides whether purified water can flow. That mixin has two
 * handlers: it overrides {@code spreadTo} (ported as
 * {@code PurifiedWater.StillFluid.spreadTo}) and it {@code @Inject}s
 * {@code matchesType} so it returns true when asked about purified water,
 * guarded on Create not being loaded.
 *
 * <p>On 26.2 the same question is asked by
 * {@code WaterFluid#canBeReplacedWith}, which is
 * {@code direction == Direction.DOWN && !other.is(FluidTags.WATER)}. Because
 * {@code dehydration:purified_water} is deliberately in
 * {@code minecraft:tags/fluids/water} (so purified water counts as water for
 * sipping, bowl filling, flasks and bucket logic), that second clause refuses,
 * and the flow never reaches {@code spreadTo} at all. Note that 26.2's
 * {@code LiquidBlock} is NOT a {@code LiquidBlockContainer}, so once the gate
 * passes, vanilla's own {@code spreadTo} else-branch already writes the
 * incoming fluid - this gate is the whole of the 26.2 problem.
 *
 * <p>Injected into {@code WaterFluid} rather than written on our own fluid
 * because the call is {@code belowFluid.canBeReplacedWith(...)}, i.e. the fluid
 * ALREADY in the cell is asked, so vanilla water has to be the one that says
 * yes. The {@code this} guard keeps the rule one-directional - purified water
 * does not get to churn sideways into its own cells, which vanilla's DOWN-only
 * rule correctly prevents.
 */
@Mixin(WaterFluid.class)
public abstract class WaterFluidPurifiedMixin {
    @Inject(method = "canBeReplacedWith", at = @At("HEAD"), cancellable = true)
    private void hearthwind$purifiedMayReplace(FluidState state, BlockGetter level, BlockPos pos,
            Fluid other, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        // `this` inside a mixin is the mixin class, not the target, so an
        // instanceof against the fluid has to go through Object.
        if ((Object) this instanceof PurifiedWater.StillFluid) {
            return;
        }
        TagKey<Fluid> purified = PurifiedWater.PURIFIED_TAG;
        if (purified != null && other != null && other.is(purified)) {
            cir.setReturnValue(true);
        }
    }
}