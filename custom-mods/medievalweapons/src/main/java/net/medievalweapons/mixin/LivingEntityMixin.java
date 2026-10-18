package net.medievalweapons.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.medievalweapons.init.CompatInit;
import net.medievalweapons.init.ConfigInit;
import net.medievalweapons.init.TagInit;
import net.medievalweapons.item.BigAxeItem;
import net.medievalweapons.item.LanceItem;
import net.medievalweapons.item.LongSwordItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin extends Entity {
   public LivingEntityMixin(EntityType<?> type, Level world) {
      super(type, world);
   }

   @Inject(
      method = {"Lnet/minecraft/world/entity/LivingEntity;getCurrentSwingDuration()I"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getHandSwingDuration(CallbackInfoReturnable<Integer> info) {
      LivingEntity livingEntity = (LivingEntity)(Object)this;
      ItemStack itemStack = livingEntity.getMainHandItem();
      if (!CompatInit.isBetterCombatLoaded && itemStack.is(TagInit.ACROSS_DOUBLE_HANDED_ITEMS)
         || itemStack.is(TagInit.DOUBLE_HANDED_ITEMS)
         || itemStack.getItem() instanceof LongSwordItem
         || itemStack.getItem() instanceof BigAxeItem
         || itemStack.getItem() instanceof LanceItem) {
         info.setReturnValue(10);
      }
   }

   @Inject(
      method = {"applyItemBlocking"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void blockedByShieldMixin(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Float> info) {
      LivingEntity livingEntity = (LivingEntity)(Object)this;
      ItemStack itemStack = livingEntity.getMainHandItem();
      if (itemStack.is(TagInit.ACROSS_DOUBLE_HANDED_ITEMS)
         || itemStack.is(TagInit.DOUBLE_HANDED_ITEMS)
         || itemStack.getItem() instanceof LongSwordItem
         || itemStack.getItem() instanceof BigAxeItem) {
         info.setReturnValue(0.0F);
      }
   }

   @WrapOperation(
      method = {"applyItemBlocking"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;blockUsingItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)V"
      )}
   )
   private void damageMixin(
      LivingEntity instance, ServerLevel level, LivingEntity entity, DamageSource source, float amount, Operation<Void> original
   ) {
      LivingEntity livingEntity = (LivingEntity)(Object)this;
      ItemStack itemStack = livingEntity.getUseItem();
      if (!itemStack.is(TagInit.ACROSS_DOUBLE_HANDED_ITEMS)
         && !itemStack.is(TagInit.DOUBLE_HANDED_ITEMS)
         && !(itemStack.getItem() instanceof LongSwordItem)
         && !(itemStack.getItem() instanceof BigAxeItem)) {
         original.call(instance, level, entity, source, amount);
      } else {
         if (livingEntity instanceof Player player) {
            player.getCooldowns().addCooldown(itemStack, ConfigInit.CONFIG.weapon_blocking_cooldown);
         }

         livingEntity.stopUsingItem();
         if (!this.level().isClientSide()) {
            livingEntity.getMainHandItem().hurtAndBreak(1, livingEntity, livingEntity.getUsedItemHand());
         }
      }
   }
}
