package net.medievalweapons.access;

import net.minecraft.world.entity.Entity;

public interface PlayerAccess {
   void doOffhandAttack(Entity var1);

   float getAttackCooldownProgressOffhand(float var1);

   void resetLastAttackedOffhandTicks();
}
