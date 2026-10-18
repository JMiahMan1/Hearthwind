package net.medievalweapons.mixin;

import net.medievalweapons.item.RapierItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({Mob.class})
public abstract class MobEntityMixin extends LivingEntity {
   public MobEntityMixin(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> entityType, net.minecraft.world.level.Level world
   ) {
      super(entityType, world);
   }

   public void knockback(double strength, double x, double z, DamageSource source, float f, boolean bl) {
      if (this.getLastHurtByMob() == null
         || this.getLastHurtByMob().getMainHandItem().isEmpty()
         || !(this.getLastHurtByMob().getMainHandItem().getItem() instanceof RapierItem)) {
         super.knockback(strength, x, z, source, f, bl);
      }
   }
}
