package dev.jmiahman.hearthwind.skills.mixin;

import dev.jmiahman.hearthwind.skills.MobScaling;
import dev.jmiahman.hearthwind.skills.SkillsConfig;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales a zombie that rolled {@code bigZombieChance} in
 * {@link MobScaling#apply}, so its hitbox and model render oversized.
 * rpgdifficulty 1.3.15 does the same thing by keeping a {@code BIG_ZOMBIE}
 * boolean in the entity's data tracker and returning
 * {@code getDimensions(pose).scaled(bigZombieSize)} - the tracker exists so
 * the client scales its copy too, which a synced attachment replaces here.
 *
 * <p>The injection lives on {@link LivingEntity} because 26.2 declares
 * {@code getDimensions} {@code final} there, so a mixin on {@code Zombie}
 * cannot target it at all. Only entities carrying the attachment are touched,
 * and only that attachment is ever set on a zombie.
 *
 * <p>26.2's {@code EntityDimensions} is a record with no {@code scaled}
 * helper, so the width, height and eye height are scaled here. Scaling the
 * eye height matters: it is where the mob's eyes sit, so leaving it alone
 * would put a giant zombie's gaze at chest height.
 */
@Mixin(LivingEntity.class)
public abstract class LivingBigScaleMixin {

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void hearthwind$scaleBigZombie(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!Boolean.TRUE.equals(self.getAttached(MobScaling.BIG_ZOMBIE))) {
            return;
        }
        double size = SkillsConfig.get().mobScaling.bigZombieSize;
        EntityDimensions base = cir.getReturnValue();
        cir.setReturnValue(new EntityDimensions(
                (float) (base.width() * size),
                (float) (base.height() * size),
                (float) (base.eyeHeight() * size),
                base.attachments(),
                base.fixed()));
    }
}
