package com.chailotl.particular.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({LivingEntity.class})
public interface InvokerLivingEntity {
   @Invoker("spawnItemParticles")
   void spawnParticles(ItemStack var1, int var2);
}
