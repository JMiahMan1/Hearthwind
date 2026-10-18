package net.jmb19905.niftycarts.mixin;

import net.jmb19905.niftycarts.entity.PostilionEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractHorse.class})
public abstract class AbstractHorseMixin extends LivingEntity {
   @Shadow
   @Nullable
   public abstract LivingEntity getControllingPassenger();

   protected AbstractHorseMixin(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> entityType, net.minecraft.world.level.Level level
   ) {
      super(entityType, level);
   }

   @Unique
   protected void customTickRidden(LivingEntity living, Vec3 vec3) {
      Vec2 vec2 = new Vec2(living.getXRot() * 0.5F, living.getYRot());
      this.setRot(vec2.y, vec2.x);
      this.yBodyRot = this.yHeadRot = this.getYRot();
      this.yRotO = this.yHeadRot;
      if (this.isLocalInstanceAuthoritative()) {
         if (vec3.z <= 0.0) {
            ((AbstractHorse)(Object)this).gallopSoundCounter = 0;
         }

         if (this.onGround()) {
            this.setJumping(false);
         }
      }
   }

   public void travel(@NotNull Vec3 vec3) {
      LivingEntity living = ((AbstractHorse)(Object)this).getControllingPassenger();
      if (living instanceof PostilionEntity) {
         float xxa = living.xxa * 0.5F;
         float zza = living.zza;
         if (zza <= 0.0F) {
            zza *= 0.25F;
         }

         Vec3 vec32 = new Vec3(xxa, 0.0, zza);
         this.customTickRidden(living, vec32);
         this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
         super.travel(vec32);
         if (!this.isLocalInstanceAuthoritative()) {
            this.calculateEntityAnimation(false);
            this.setDeltaMovement(Vec3.ZERO);
         }
      } else {
         super.travel(vec3);
      }
   }

   @Inject(
      method = {"getControllingPassenger"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getControllingPassenger(CallbackInfoReturnable<net.minecraft.world.entity.LivingEntity> cir) {
      if (this.getFirstPassenger() instanceof PostilionEntity dummy) {
         cir.setReturnValue(dummy);
      }
   }

   public float maxUpStep() {
      float f = super.maxUpStep();
      if (this.getControllingPassenger() instanceof PostilionEntity) {
         return Math.max(f, 1.2F);
      } else {
         return this.getControllingPassenger() instanceof Player ? Math.max(f, 1.0F) : f;
      }
   }
}
