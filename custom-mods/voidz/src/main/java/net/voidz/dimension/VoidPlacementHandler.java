package net.voidz.dimension;

import net.adventurez.entity.VoidShadowEntity;
import net.adventurez.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.voidz.access.ServerPlayerAccess;
import net.voidz.init.BlockInit;

public class VoidPlacementHandler {
   public static final BlockPos VOID_SPAWN_POS = new BlockPos(0, 100, 0);

   public static TeleportTransition enter(ServerPlayer serverPlayerEntity, ServerLevel serverWorld, BlockPos portalPos) {
      ((ServerPlayerAccess)serverPlayerEntity).setVoidPortingBlockPos(serverPlayerEntity.blockPosition());
      spawnVoidPlatform(serverWorld, VOID_SPAWN_POS.below());
      return new TeleportTransition(
         serverWorld, Vec3.atLowerCornerOf(VOID_SPAWN_POS).add(0.5, 0.0, 0.5), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING
      );
   }

   public static TeleportTransition leave(ServerPlayer serverPlayerEntity, ServerLevel serverWorld, BlockPos portalPos) {
      return new TeleportTransition(
         serverWorld,
         Vec3.atLowerCornerOf(((ServerPlayerAccess)serverPlayerEntity).getVoidPortingBlockPos()).add(0.5, 0.0, 0.5),
         Vec3.ZERO,
         serverWorld.getRandom().nextFloat() * 360.0F,
         0.0F,
         TeleportTransition.DO_NOTHING
      );
   }

   private static void spawnVoidPlatform(ServerLevel world, BlockPos pos) {
      if (world.getBlockState(pos).getBlock() != BlockInit.PORTAL) {
         BlockState platformBlock = BlockInit.VOID_STONE.defaultBlockState();

         for (float u = 0.0F; u < Math.PI * 2; u += 0.012271847F) {
            for (int i = 0; i < 40; i++) {
               BlockPos blockPos = pos.offset((int)(Math.sin(u) * i), 0, (int)(Math.cos(u) * i));
               if (world.getBlockState(blockPos).isAir()) {
                  world.setBlockAndUpdate(blockPos, platformBlock);
               }
            }
         }

         world.setBlockAndUpdate(pos, BlockInit.PORTAL.defaultBlockState());
         spawnVoidBoss(world, pos.above());
      }
   }

   public static void spawnVoidBoss(ServerLevel world, BlockPos spawnPos) {
      VoidShadowEntity voidShadowEntity = (VoidShadowEntity)EntityInit.VOID_SHADOW.create(world, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
      voidShadowEntity.setVoidMiddle(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
      voidShadowEntity.snapTo(spawnPos.above().north(40), 0.0F, 0.0F);
      world.addFreshEntity(voidShadowEntity);
   }
}
