package com.github.legoatoom.connectiblechains.entity;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

public interface ChainLinkEntity {
   private static <E extends net.minecraft.world.entity.Entity & ChainLinkEntity> net.minecraft.world.InteractionResult onDamageFrom(
      E self, net.minecraft.world.damagesource.DamageSource source, net.minecraft.sounds.SoundEvent hitSound
   ) {
      if (self.level().isClientSide()) {
         return InteractionResult.PASS;
      } else if (self.isInvulnerable()) {
         return InteractionResult.FAIL;
      } else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
         return InteractionResult.SUCCESS;
      } else if (source.getWeaponItem() != null && source.getWeaponItem().is(ConventionalItemTags.SHEAR_TOOLS)) {
         return InteractionResult.SUCCESS;
      } else {
         if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
            self.playSound(hitSound, 0.5F, 1.0F);
         }

         return InteractionResult.FAIL;
      }
   }

   default InteractionResult onDamageFrom(DamageSource source, SoundEvent hitSound) {
      return onDamageFrom((Entity & ChainLinkEntity)this, source, hitSound);
   }

   void setSourceItem(Item var1);

   Item getSourceItem();
}
