package net.jmb19905.niftycarts.entity;

import java.util.List;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.NiftyCartsConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class AnimalCartEntity extends AbstractDrawnEntity {
   public AnimalCartEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Entity> entityTypeIn, net.minecraft.world.level.Level worldIn
   ) {
      super(entityTypeIn, worldIn);
   }

   @Override
   public NiftyCartsConfig.CartConfig getConfig() {
      return NiftyCartsConfig.get().animalCart;
   }

   @Override
   public void tick() {
      super.tick();
      Entity coachman = this.getControllingPassenger();
      Entity pulling = this.getPulling();
      if (pulling != null && coachman != null && pulling.getControllingPassenger() == null) {
         PostilionEntity postilion = (PostilionEntity)NiftyCarts.POSTILION_ENTITY.create(this.level(), EntitySpawnReason.SPAWN_ITEM_USE);
         if (postilion != null) {
            postilion.snapTo(pulling.getX(), pulling.getY(), pulling.getZ(), coachman.getYRot(), coachman.getXRot());
            if (postilion.startRiding(pulling)) {
               this.level().addFreshEntity(postilion);
            } else {
               postilion.discard();
            }
         }
      }

      if (!this.isLocked()) {
         List<net.minecraft.world.entity.Entity> list = this.level()
            .getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), EntitySelector.pushableBy(this));
         if (!list.isEmpty()) {
            boolean bl = !this.level().isClientSide() && !(this.getControllingPassenger() instanceof Player);

            for (Entity entity : list) {
               if (!entity.hasPassenger(this)
                  && bl
                  && this.canAddPassenger(entity)
                  && !entity.isPassenger()
                  && entity.getBbWidth() < this.getBbWidth()
                  && entity.getBbWidth() * entity.getBbHeight() < 1.5
                  && entity instanceof LivingEntity
                  && !(entity instanceof AgeableWaterCreature)
                  && !(entity instanceof Player)) {
                  if (entity instanceof TamableAnimal tamable) {
                     tamable.setInSittingPose(true);
                  }

                  entity.startRiding(this);
               }
            }
         }
      }
   }

   @NotNull
   @Override
   public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 vec) {
      if (this.isLocked()) {
         return InteractionResult.FAIL;
      } else if (!player.isSecondaryUseActive()) {
         InteractionResult bannerResult = this.useBanner(player, hand);
         if (bannerResult.consumesAction()) {
            return bannerResult;
         } else if (this.getPulling() != player) {
            if (!this.canAddPassenger(player)) {
               return InteractionResult.PASS;
            } else if (!this.level().isClientSide()) {
               return (InteractionResult)(player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS);
            } else {
               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.PASS;
         }
      } else {
         if (!this.level().isClientSide()) {
            for (Entity entity : this.getPassengers()) {
               if (!(entity instanceof Player)) {
                  entity.stopRiding();
               }
            }
         }

         return InteractionResult.SUCCESS;
      }
   }

   public void push(Entity entityIn) {
      if (!entityIn.hasPassenger(this)) {
         if (!this.level().isClientSide()
            && this.getPulling() != entityIn
            && this.getControllingPassenger() == null
            && this.getPassengers().size() < 2
            && !entityIn.isPassenger()
            && entityIn.getBbWidth() < this.getBbWidth()
            && entityIn instanceof LivingEntity
            && !(entityIn instanceof AgeableWaterCreature)
            && !(entityIn instanceof Player)) {
            entityIn.startRiding(this);
         } else {
            super.push(entityIn);
         }
      }
   }

   protected boolean canAddPassenger(@NotNull Entity passenger) {
      return this.getPassengers().size() < 2;
   }

   public float getPassengersRidingOffsetY(EntityDimensions entityDimensions, float f) {
      return (entityDimensions.height() - 0.5F) * f;
   }

   @NotNull
   protected Vec3 getPassengerAttachmentPoint(@NotNull Entity entity, @NotNull EntityDimensions entityDimensions, float f) {
      double f1 = -0.1;
      if (this.getPassengers().size() > 1) {
         f1 = this.getPassengers().indexOf(entity) == 0 ? 0.2 : -0.6;
         if (entity instanceof Animal) {
            f1 += 0.2;
         }
      }

      Vec3 forward = this.getLookAngle().scale(f1 + Mth.sin((float)Math.toRadians(this.getXRot())) * 0.7);
      return new Vec3(forward.x, this.getPassengersRidingOffsetY(entityDimensions, f) + forward.y, forward.z);
   }

   public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction moveFunction) {
      super.positionRider(passenger, moveFunction);
      if (this.hasPassenger(passenger)) {
         passenger.setYBodyRot(this.getYRot());
         float f2 = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
         float f1 = Mth.clamp(f2, -105.0F, 105.0F);
         passenger.yRotO += f1 - f2;
         passenger.setYRot(passenger.getYRot() + (f1 - f2));
         passenger.setYHeadRot(passenger.getYRot());
         if (passenger instanceof Animal && this.getPassengers().size() > 1) {
            int j = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setYBodyRot(((Animal)passenger).yBodyRot + j);
            passenger.setYHeadRot(passenger.getYHeadRot() + j);
         }
      }
   }

   @Override
   public Item getCartItem() {
      return NiftyCarts.ANIMAL_CART.get(this.getWoodType());
   }
}
