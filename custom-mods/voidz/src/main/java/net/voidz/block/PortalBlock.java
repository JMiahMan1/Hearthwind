package net.voidz.block;

import java.util.List;
import net.adventurez.entity.DragonEntity;
import net.adventurez.entity.VoidShadowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.voidz.block.entity.PortalBlockEntity;
import net.voidz.dimension.VoidPlacementHandler;
import net.voidz.init.BlockInit;
import net.voidz.init.ConfigInit;
import net.voidz.init.DimensionInit;
import org.jetbrains.annotations.Nullable;

public class PortalBlock extends Block implements EntityBlock {
   public PortalBlock(Properties settings) {
      super(settings);
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new PortalBlockEntity(pos, state);
   }

   @Nullable
   public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
      net.minecraft.world.level.Level world,
      net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.block.entity.BlockEntityType<T> type
   ) {
      return checkType(type, BlockInit.PORTAL_BLOCK_ENTITY, world.isClientSide() ? PortalBlockEntity::clientTick : PortalBlockEntity::serverTick);
   }

   public InteractionResult useWithoutItem(BlockState stateBlock, Level world, BlockPos blockPos, Player playerEntity, BlockHitResult blockHitResult) {
      if (!world.isClientSide()) {
         ServerLevel serverWorld = (ServerLevel)playerEntity.level();
         if (serverWorld.dimension() == DimensionInit.VOID_WORLD) {
            AABB box = new AABB(blockPos);
            List<VoidShadowEntity> list = world.getEntitiesOfClass(VoidShadowEntity.class, box.inflate(160.0), EntitySelector.NO_SPECTATORS);
            if (!playerEntity.isCreative() && !playerEntity.isSpectator() && !list.isEmpty()) {
               playerEntity.sendSystemMessage(Component.literal("Void Shadow: You can't escape the depths of this world as long as I am alive!"));
               return InteractionResult.FAIL;
            }

            if (ConfigInit.CONFIG.allow_boss_respawn && list.isEmpty()) {
               PortalBlockEntity portalBlockEntity = (PortalBlockEntity)world.getBlockEntity(blockPos);
               if (portalBlockEntity != null && portalBlockEntity.bossTime != 0) {
                  int timer = ConfigInit.CONFIG.boss_respawn_time - ((int)world.getLevelData().getGameTime() - portalBlockEntity.bossTime);
                  playerEntity.sendSystemMessage(Component.literal("The Void Shadow will respawn in " + timer + " ticks"));
               }
            }

            ServerLevel oldWorld = serverWorld.getServer().overworld();
            if (oldWorld != null) {
               if (playerEntity.isPassenger() && playerEntity.getVehicle() instanceof DragonEntity dragonEntity && playerEntity.canUsePortal(true)) {
                  playerEntity.stopRiding();
                  dragonEntity.teleport(VoidPlacementHandler.leave((ServerPlayer)playerEntity, oldWorld, blockPos));
                  return InteractionResult.SUCCESS;
               }

               playerEntity.teleport(VoidPlacementHandler.leave((ServerPlayer)playerEntity, oldWorld, blockPos));
               return InteractionResult.FAIL;
            }
         } else {
            ServerLevel voidWorld = serverWorld.getServer().getLevel(DimensionInit.VOID_WORLD);
            if (voidWorld == null) {
               playerEntity.sendSystemMessage(Component.literal("Failed to find void world, was it registered?"));
               return InteractionResult.FAIL;
            }

            playerEntity.teleport(VoidPlacementHandler.enter((ServerPlayer)playerEntity, voidWorld, blockPos));
         }
      }

      return InteractionResult.SUCCESS;
   }

   protected static <E extends net.minecraft.world.level.block.entity.BlockEntity, A extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<A> checkType(
      net.minecraft.world.level.block.entity.BlockEntityType<A> givenType,
      net.minecraft.world.level.block.entity.BlockEntityType<E> expectedType,
      net.minecraft.world.level.block.entity.BlockEntityTicker<? super E> ticker
   ) {
      return expectedType == givenType ? (net.minecraft.world.level.block.entity.BlockEntityTicker<A>)(net.minecraft.world.level.block.entity.BlockEntityTicker<?>)ticker : null;
   }
}
