package net.medievalweapons.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.medievalweapons.init.ConfigInit;
import net.medievalweapons.init.ParticleInit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({AreaEffectCloud.class})
public abstract class AreaEffectCloudEntityMixin {
    @WrapOperation(
        method = {"Lnet/minecraft/world/entity/AreaEffectCloud;serverTick(Lnet/minecraft/server/level/ServerLevel;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/effect/MobEffect;applyInstantaneousEffect(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V"
        )}
    )
    private void tickMixin(
        MobEffect effect, ServerLevel level, Entity directEntity, Entity sourceEntity, LivingEntity livingEntity, int amplifier, double scale, Operation<Void> original
    ) {
        if (!ConfigInit.CONFIG.healing_staff_owner_healing
            && livingEntity.equals(this.getOwner())
            && this.getParticle().equals(ParticleInit.HEALING_AURA_PARTICLE)) {
            return;
        }
        original.call(effect, level, directEntity, sourceEntity, livingEntity, amplifier, scale);
    }

    @Shadow
    public ParticleOptions getParticle() {
        return null;
    }

    @Shadow
    public abstract LivingEntity getOwner();
}
