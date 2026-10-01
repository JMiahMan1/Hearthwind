package dev.jmiahman.hearthwind.survival.mixin;

import dev.jmiahman.hearthwind.survival.additionz.AdditionZParity;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * A player has to stay awake for 144 000 ticks - two in-game hours - before a
 * phantom can pick them, instead of vanilla's 72 000. The reference patches
 * this exact literal inside {@code PhantomSpawner.tick}; the 26.2 class still
 * has it, so the patch ports unchanged.
     * <p>The handler must accept the original constant: mixin validates it as
     * {@code (I)I} for an intValue-annotated @ModifyConstant and throws
     * "Not enough arguments" for a no-arg handler.
     */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 72000))
    private int hearthwind$awakeLonger(int original) {
        return AdditionZParity.config().phantomTickTime;
    }
}
