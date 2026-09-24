package net.adventurez.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.adventurez.init.ItemInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlEntityMixin extends ThrowableItemProjectile {

    public EnderPearlEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), cancellable = true)
    protected void onCollisionMixin(HitResult hitResult, CallbackInfo info) {
        if (this.getItem().is(ItemInit.PRIME_EYE)) {
            this.getOwner().hurt(this.damageSources().fall(), 2.0F);
            this.discard();
            this.playTeleportSound((ServerLevel) this.level(), this.position());
            info.cancel();

        }
    }

    @Shadow
    private void playTeleportSound(Level level, Vec3 pos) {
    }
}
