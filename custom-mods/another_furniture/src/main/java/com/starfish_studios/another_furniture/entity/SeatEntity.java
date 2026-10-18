package com.starfish_studios.another_furniture.entity;

import com.starfish_studios.another_furniture.block.SeatBlock;
import com.starfish_studios.another_furniture.registry.AFEntityTypes;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SeatEntity extends Entity {
   public SeatEntity(Level level) {
      super((EntityType)AFEntityTypes.SEAT.get(), level);
      this.noPhysics = true;
   }

   public SeatEntity(Level level, BlockPos pos) {
      this(level);
      this.setPos(pos.getX() + 0.5, pos.getY() + 0.001, pos.getZ() + 0.5);
   }

   public void tick() {
      if (!this.level().isClientSide()) {
         BlockState state = this.level().getBlockState(this.blockPosition());
         boolean canSit;
         if (state.getBlock() instanceof SeatBlock seatBlock) {
            canSit = seatBlock.isSittable(state);
         } else {
            canSit = false;
         }

         if (!this.isVehicle() || !canSit) {
            this.discard();
            this.level().updateNeighbourForOutputSignal(this.blockPosition(), this.level().getBlockState(this.blockPosition()).getBlock());
         }
      }
   }

   protected void defineSynchedData(Builder builder) {
   }

   protected void readAdditionalSaveData(ValueInput input) {
   }

   protected void addAdditionalSaveData(ValueOutput output) {
   }

   public Vec3 getPassengerRidingPosition(Entity entity) {
      return this.position().add(new Vec3(0.0, this.getPassengersRidingOffset(), 0.0));
   }

   public double getPassengersRidingOffset() {
      List<net.minecraft.world.entity.Entity> passengers = this.getPassengers();
      if (passengers.isEmpty()) {
         return 0.0;
      } else {
         double seatHeight = 0.0;
         BlockState state = this.level().getBlockState(this.blockPosition());
         if (state.getBlock() instanceof SeatBlock seatBlock) {
            seatHeight = seatBlock.seatHeight(state);
         }

         return seatHeight + getEntitySeatOffset((Entity)passengers.getFirst());
      }
   }

   public static double getEntitySeatOffset(Entity entity) {
      if (entity instanceof Slime) {
         return 0.25;
      } else if (entity instanceof Parrot) {
         return 0.0625;
      } else if (entity instanceof Skeleton) {
         return 0.125;
      } else if (entity instanceof Creeper) {
         return 0.25;
      } else if (entity instanceof Cat) {
         return 0.125;
      } else {
         return entity instanceof Wolf ? 0.0625 : 0.0;
      }
   }

   public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
      return false;
   }

   protected boolean canRide(Entity entity) {
      return true;
   }

   public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket(
      net.minecraft.server.level.ServerEntity entity
   ) {
      return super.getAddEntityPacket(entity);
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity entity) {
      BlockPos pos = this.blockPosition();
      BlockState state = this.level().getBlockState(pos);
      if (state.getBlock() instanceof SeatBlock seatBlock) {
         Vec3 safeVec = DismountHelper.findSafeDismountLocation(
            entity.getType(), this.level(), seatBlock.primaryDismountLocation(this.level(), state, pos), false
         );
         if (safeVec != null) {
            return safeVec.add(0.0, 0.25, 0.0);
         }
      }

      Direction original = this.getDirection();
      Direction[] offsets = new Direction[]{original, original.getClockWise(), original.getCounterClockWise(), original.getOpposite()};

      for (Direction dir : offsets) {
         Vec3 safeVec = DismountHelper.findSafeDismountLocation(entity.getType(), this.level(), pos.relative(dir), false);
         if (safeVec != null) {
            return safeVec.add(0.0, 0.25, 0.0);
         }
      }

      return super.getDismountLocationForPassenger(entity);
   }

   protected void addPassenger(Entity passenger) {
      BlockPos pos = this.blockPosition();
      BlockState state = this.level().getBlockState(pos);
      if (state.getBlock() instanceof SeatBlock seatBlock) {
         passenger.setYRot(seatBlock.setRiderRotation(state, passenger));
      }

      super.addPassenger(passenger);
   }

   protected void removePassenger(Entity entity) {
      super.removePassenger(entity);
      if (entity instanceof TamableAnimal ta) {
         ta.setInSittingPose(false);
      }
   }
}
