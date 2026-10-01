package dev.jmiahman.hearthwind.survival.mixin;

import dev.jmiahman.hearthwind.survival.additionz.AdditionZParity;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Every animal stays a baby for 252 000 ticks - three and a half real hours -
 * instead of vanilla's 20 000. The reference does it by patching the constant
 * that {@code getBabyStartAge} returns; so do we, and the value comes from
 * {@code additionz.json5}'s {@code baby_to_adult_time}.
 *
 * <p>This is the single biggest gameplay change in AdditionZ: breeding pairs
 * stay useless for hours, and livestock becomes a long project rather than an
 * afternoon one.
 *
 * <p>The handler must accept the original constant: mixin validates an
 * intValue-annotated @ModifyConstant as {@code (I)I} and throws "Not enough
 * arguments" for a no-arg handler.
 */
@Mixin(AgeableMob.class)
public abstract class AgeableMobBabyMixin {

    @ModifyConstant(method = "getBabyStartAge", constant = @Constant(intValue = -24000))
    private int hearthwind$babyForAges(int original) {
        return AdditionZParity.config().babyToAdultTime;
    }
}
