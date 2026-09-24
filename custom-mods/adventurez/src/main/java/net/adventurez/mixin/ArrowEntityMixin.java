package net.adventurez.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adventurez.init.ItemInit;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Arrow.class)
public abstract class ArrowEntityMixin extends AbstractArrow {

    @Shadow
    @Mutable
    @Final
    private static EntityDataAccessor<Integer> ID_EFFECT_COLOR;

    public ArrowEntityMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "Lnet/minecraft/world/entity/projectile/arrow/Arrow;<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void initMixin(Level level, LivingEntity owner, ItemStack stack, @Nullable ItemStack shotFrom, CallbackInfo info) {
        if (stack.is(ItemInit.IVORY_ARROW)) {
            this.setBaseDamage(6.0D);
        }
    }

    @Inject(method = "makeParticle", at = @At("HEAD"), cancellable = true)
    private void spawnParticlesMixin(int amount, CallbackInfo info) {
        if (this.getEntityData().get(ID_EFFECT_COLOR) == 14340520) {
            info.cancel();
        }
    }

    @Inject(method = "updateColor", at = @At("TAIL"), cancellable = true)
    private void initColorMixin(CallbackInfo info) {
        if (this.getPickupItemStackOrigin().is(ItemInit.IVORY_ARROW)) {
            this.entityData.set(ID_EFFECT_COLOR, 14340520);
        }
    }

}
