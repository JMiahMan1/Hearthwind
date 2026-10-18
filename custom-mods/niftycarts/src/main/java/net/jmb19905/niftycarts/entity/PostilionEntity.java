package net.jmb19905.niftycarts.entity;

import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class PostilionEntity extends DummyLivingEntity {
   public PostilionEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> type, net.minecraft.world.level.Level world
   ) {
      super(type, world);
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide() && this.getCoachman() == null) {
         this.discard();
      }
   }

   @Nullable
   private LivingEntity getCoachman() {
      Entity mount = this.getVehicle();
      return mount != null ? NiftyWorld.get(this.level()).getDrawn(mount).map(AbstractDrawnEntity::getControllingPassenger).orElse(null) : null;
   }
}
