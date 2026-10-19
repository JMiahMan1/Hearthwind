package io.github.hexagonnico.spidercaves.entities;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.spider.Spider;
import org.jetbrains.annotations.NotNull;

public class BlackRecluse extends Spider {
   public BlackRecluse(net.minecraft.world.entity.EntityType<? extends BlackRecluse> type, net.minecraft.world.level.Level world) {
      super(type, world);
   }

   public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level, @NotNull Entity target) {
      if (super.doHurtTarget(level, target)) {
         if (target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, switch (this.level().getDifficulty()) {
               case PEACEFUL -> 0;
               case EASY -> 100;
               case NORMAL -> 200;
               case HARD -> 400;
               default -> throw new IncompatibleClassChangeError();
            }, 1), this);
         }

         return true;
      } else {
         return false;
      }
   }
}
