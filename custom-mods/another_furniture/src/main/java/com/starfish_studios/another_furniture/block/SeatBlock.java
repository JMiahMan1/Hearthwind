package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.entity.SeatEntity;
import com.starfish_studios.another_furniture.registry.AFBlockTags;
import com.starfish_studios.another_furniture.registry.AFEntityTypeTags;
import com.starfish_studios.another_furniture.registry.AFRegistry;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public class SeatBlock extends Block {
   public SeatBlock(Properties properties) {
      super(properties);
   }

   public float seatHeight(BlockState state) {
      return 0.35F;
   }

   public boolean isSittable(BlockState state) {
      return true;
   }

   public BlockPos primaryDismountLocation(Level level, BlockState state, BlockPos pos) {
      return pos;
   }

   public float setRiderRotation(BlockState state, Entity entity) {
      return entity.getYRot();
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (AFRegistry.isFakePlayer(player)) {
         return InteractionResult.PASS;
      } else if (!level.mayInteract(player, pos)) {
         return InteractionResult.PASS;
      } else if (!this.isSittable(state) || player.isPassenger() || player.isCrouching()) {
         return InteractionResult.PASS;
      } else if (isSeatBlocked(level, pos)) {
         return InteractionResult.PASS;
      } else if (isSeatOccupied(level, pos)) {
         List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
         return ejectSeatedExceptPlayer(level, seats.get(0)) ? InteractionResult.SUCCESS : InteractionResult.PASS;
      } else if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         sitDown(level, pos, (Entity)getLeashed(player).orElse(player));
         return InteractionResult.SUCCESS;
      }
   }

   public static boolean isSeatBlocked(Level level, BlockPos pos) {
      return !level.getBlockState(pos.above()).getCollisionShape(level, pos).isEmpty()
         && !level.getBlockState(pos.above()).is(AFBlockTags.ABOVE_BYPASSES_SEAT_CHECK);
   }

   public static boolean isSeatOccupied(Level level, BlockPos pos) {
      return !level.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty();
   }

   public static Optional<net.minecraft.world.entity.Entity> getLeashed(net.minecraft.world.entity.player.Player player) {
      for (Entity e : player.level().getEntities((Entity)null, player.getBoundingBox().inflate(10.0), ex -> true)) {
         if (e instanceof Mob mob && mob.getLeashHolder() == player && canBePickedUp(e)) {
            return Optional.of(mob);
         }

         if (e instanceof LivingEntity living && !(e instanceof Mob) && canBePickedUp(e)) {
            return Optional.of(living);
         }
      }

      return Optional.empty();
   }

   public static boolean ejectSeatedExceptPlayer(Level level, SeatEntity seatEntity) {
      List<net.minecraft.world.entity.Entity> passengers = seatEntity.getPassengers();
      if (!passengers.isEmpty() && passengers.get(0) instanceof Player) {
         return false;
      } else {
         if (!level.isClientSide()) {
            seatEntity.ejectPassengers();
         }

         return true;
      }
   }

   public static boolean canBePickedUp(Entity passenger) {
      if (passenger instanceof Player) {
         return false;
      } else {
         return passenger instanceof TamableAnimal ta && !ta.isTame()
            ? false
            : passenger instanceof LivingEntity && passenger.getType().builtInRegistryHolder().is(AFEntityTypeTags.CAN_SIT_IN_SEATS);
      }
   }

   public static void sitDown(Level level, BlockPos pos, Entity entity) {
      if (!level.isClientSide()) {
         SeatEntity seat = new SeatEntity(level, pos);
         level.addFreshEntity(seat);
         entity.startRiding(seat);
         level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());
         if (entity instanceof TamableAnimal ta) {
            ta.setInSittingPose(true);
         }
      }
   }

   protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
      return false;
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return isSeatOccupied(level, pos) ? 15 : 0;
   }
}
