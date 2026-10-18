package net.jmb19905.niftycarts.entity;

import java.util.Optional;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.NiftyCartsConfig;
import net.jmb19905.niftycarts.advancement.NCCriteriaTriggers;
import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ReaperCartEntity extends AbstractDrawnEntity {
   private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> FOLDED = SynchedEntityData.defineId(
      ReaperCartEntity.class, EntityDataSerializers.BOOLEAN
   );

   public ReaperCartEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Entity> entityTypeIn, net.minecraft.world.level.Level worldIn
   ) {
      super(entityTypeIn, worldIn);
   }

   @Override
   protected double getSpacing() {
      return 1.3;
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(FOLDED, true);
   }

   public boolean isFolded() {
      return (Boolean)this.entityData.get(FOLDED);
   }

   @Override
   public void tick() {
      super.tick();
      Entity coachman = this.getControllingPassenger();
      Entity pulling = this.getPulling();
      boolean folded = pulling == null || coachman == null;
      if (folded != (Boolean)this.entityData.get(FOLDED)) {
         this.playSound(SoundEvents.WOODEN_TRAPDOOR_CLOSE);
      }

      this.entityData.set(FOLDED, folded);
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
   }

   public float getPassengersRidingOffsetY(EntityDimensions entityDimensions, float f) {
      return (entityDimensions.height() - 0.1875F) * f;
   }

   @NotNull
   protected Vec3 getPassengerAttachmentPoint(@NotNull Entity entity, @NotNull EntityDimensions entityDimensions, float f) {
      Vec3 forward = this.getLookAngle().scale(-0.45);
      return new Vec3(forward.x, this.getPassengersRidingOffsetY(entityDimensions, f) + forward.y, forward.z);
   }

   protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction moveFunction) {
      super.positionRider(passenger, moveFunction);
      if (this.hasPassenger(passenger)) {
         passenger.setYBodyRot(this.getYRot());
         float f2 = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
         float f1 = Mth.clamp(f2, -105.0F, 105.0F);
         passenger.yRotO += f1 - f2;
         passenger.setYRot(passenger.getYRot() + (f1 - f2));
         passenger.setYHeadRot(passenger.getYRot());
      }
   }

   @NotNull
   @Override
   public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand interactionHand, @NotNull Vec3 vec) {
      if (this.isLocked()) {
         return InteractionResult.FAIL;
      } else {
         if (!this.level().isClientSide()) {
            if (player.isSecondaryUseActive()) {
               player.sendOverlayMessage(Component.translatable("message.niftycarts.use_reaper"));
            } else if (!player.isSecondaryUseActive() && this.pulling != null && this.pulling != player && player.startRiding(this)) {
               return InteractionResult.CONSUME;
            }
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public void pulledTick() {
      super.pulledTick();
      if (this.getPulling() != null) {
         if (!this.level().isClientSide()) {
            Optional<net.minecraft.world.entity.Entity> pulling = NiftyWorld.get(this.level()).getCurrentlyPulling(this);
            if (pulling.isPresent()
               && this.getFirstPassenger() instanceof ServerPlayer pl
               && (this.xo != this.getX() || this.zo != this.getZ())) {
               this.harvest(pl);
            }
         }
      }
   }

   private void harvest(ServerPlayer player) {
      for (int i = 0; i <= 12; i += 2) {
         float f = 1.1F + i / 10.0F;
         double x = this.getX() + Mth.sin((float)Math.toRadians(this.getYRot() + 90.0F)) * f;
         double z = this.getZ() - Mth.cos((float)Math.toRadians(this.getYRot() + 90.0F)) * f;
         BlockPos blockPos = new BlockPos((int)Math.round(x - 0.5), (int)Math.round(this.getY() - 0.75), (int)Math.round(z - 0.5));
         BlockPos pos = blockPos.above();
         BlockState state = this.level().getBlockState(pos);
         if (state.is(NiftyCarts.REAPER_HARVESTABLE) && this.level().removeBlock(pos, false)) {
            NCCriteriaTriggers.REAPER_HARVEST.trigger(player, state);
            this.level().destroyBlock(pos, false);
            if (!state.requiresCorrectToolForDrops()) {
               Block.dropResources(state, this.level(), pos, this.level().getBlockEntity(pos), player, ItemStack.EMPTY);
            }
         }
      }
   }

   @Override
   public Item getCartItem() {
      return NiftyCarts.REAPER.get(this.getWoodType());
   }

   @Override
   public NiftyCartsConfig.CartConfig getConfig() {
      return NiftyCartsConfig.get().reaper;
   }
}
