package net.smitherz.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.smitherz.util.LootHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {
   @Inject(
      method = {"dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V"},
      at = {@At("TAIL")}
   )
   protected void dropLootMixin(ServerLevel level, DamageSource damageSource, boolean causedByPlayer, CallbackInfo info) {
      LootHelper.dropGemLoot((LivingEntity)(Object)this, damageSource, causedByPlayer);
   }
}
