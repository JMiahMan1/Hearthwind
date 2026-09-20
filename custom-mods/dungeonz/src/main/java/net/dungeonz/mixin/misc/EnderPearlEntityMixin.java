package net.dungeonz.mixin.misc;

import net.dungeonz.util.DungeonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlEntityMixin extends ThrowableItemProjectile {

    public EnderPearlEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/throwableitemprojectile/ThrownEnderpearl;getOwner()Lnet/minecraft/world/entity/Entity;"), cancellable = true)
    protected void onCollisionMixin(HitResult hitResult, CallbackInfo info) {
        if (this.level().dimension() == DimensionInit.DUNGEON_WORLD) {
            Entity entity = this.getOwner();
            if (entity instanceof ServerPlayer serverPlayerEntity && DungeonHelper.getCurrentDungeon(serverPlayerEntity) != null && !DungeonHelper.getCurrentDungeon(serverPlayerEntity).isEnderPearlAllowed()) {
                this.discard();
                info.cancel();
            }
        }
    }
}
