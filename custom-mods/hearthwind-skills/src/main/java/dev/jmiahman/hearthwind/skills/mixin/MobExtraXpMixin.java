package dev.jmiahman.hearthwind.skills.mixin;

import dev.jmiahman.hearthwind.skills.MobScaling;
import dev.jmiahman.hearthwind.skills.SkillsConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

/**
 * A tougher mob pays out more experience, exactly as RPGDifficulty does: the
 * mob's stored health factor scales the vanilla reward, capped at
 * {@code maxXPFactor} (Aged: 4.0). Without this a mob scaled to 4x health and
 * damage still drops vanilla XP, so the wilds stop being worth farming at the
 * moment they stop being survivable.
 */
@Mixin(Mob.class)
public abstract class MobExtraXpMixin {

    @ModifyReturnValue(method = "getBaseExperienceReward", at = @At("RETURN"))
    private int hearthwind$scaleXpByDistance(int original) {
        Mob mob = (Mob) (Object) this;
        if (!(mob.level() instanceof ServerLevel)) {
            return original;
        }
        double factor = mob.getAttached(MobScaling.HEALTH_FACTOR);
        if (factor <= 1.0) {
            return original;
        }
        return MobScaling.xpToDrop(factor, original, SkillsConfig.get().mobScaling);
    }
}
