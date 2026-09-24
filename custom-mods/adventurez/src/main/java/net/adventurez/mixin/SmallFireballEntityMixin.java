package net.adventurez.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adventurez.entity.BlazeGuardianEntity;
import net.adventurez.entity.nonliving.BlazeGuardianShieldEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(SmallFireball.class)
public abstract class SmallFireballEntityMixin extends AbstractHurtingProjectile {

    public SmallFireballEntityMixin(EntityType<? extends AbstractHurtingProjectile> entityType, double d, double e, double f, Vec3 vec3d, Level level) {
        super(entityType, d, e, f, vec3d, level);
    }

    @Inject(method = "onHit", at = @At(value = "HEAD"), cancellable = true)
    protected void onHit(HitResult hitResult, CallbackInfo info) {
        if (this.getOwner() != null && this.getOwner() instanceof BlazeGuardianEntity && hitResult.getType() == HitResult.Type.ENTITY
                && ((net.minecraft.world.phys.EntityHitResult) hitResult).getEntity() instanceof BlazeGuardianShieldEntity) {
            info.cancel();
        }
    }
}
