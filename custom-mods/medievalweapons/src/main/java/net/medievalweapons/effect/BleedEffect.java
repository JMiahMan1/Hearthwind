package net.medievalweapons.effect;

import net.medievalweapons.init.EntityInit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BleedEffect extends MobEffect {
   public BleedEffect(MobEffectCategory statusEffectCategory, int color) {
      super(statusEffectCategory, color);
   }

   public boolean applyEffectTick(LivingEntity entity, int amplifier) {
      entity.hurt(this.createDamageSource(entity), 1.0F);
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      int i = 40 >> amplifier;
      return i > 0 ? duration % i == 0 : true;
   }

   private DamageSource createDamageSource(Entity source) {
      return source.damageSources().source(EntityInit.BLEEDING);
   }
}
