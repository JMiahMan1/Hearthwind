package net.voidz.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.voidz.dimension.VoidPlacementHandler;
import net.voidz.init.BlockInit;
import net.voidz.init.ConfigInit;
import net.voidz.init.DimensionInit;

public class PortalBlockEntity extends TheEndPortalBlockEntity {
   private float particleTicker;
   private int spawnTicker;
   public int bossTime;

   public PortalBlockEntity(BlockPos pos, BlockState state) {
      super(BlockInit.PORTAL_BLOCK_ENTITY, pos, state);
   }

   public static void clientTick(Level world, BlockPos pos, BlockState state, PortalBlockEntity blockEntity) {
      blockEntity.update();
   }

   public static void serverTick(Level world, BlockPos pos, BlockState state, PortalBlockEntity blockEntity) {
      blockEntity.update();
   }

   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.bossTime = input.getIntOr("VoidShadowKilledTime", 0);
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("VoidShadowKilledTime", this.bossTime);
   }

   public boolean shouldRenderFace(Direction direction) {
      return true;
   }

   private void update() {
      if (this.level.isClientSide()) {
         this.particleTicker += 0.024543693F;
         double angle = (Math.PI * 2) * this.particleTicker;
         this.level
            .addParticle(
               ParticleTypes.PORTAL,
               this.getBlockPos().getX() + 0.5 + 1.5 * Math.sin(angle),
               this.getBlockPos().getY() + 0.5 + Math.sin(angle) / 2.0,
               this.getBlockPos().getZ() + 0.5 + 1.5 * Math.cos(angle),
               0.0,
               0.0,
               0.0
            );
         this.level
            .addParticle(
               ParticleTypes.PORTAL,
               this.getBlockPos().getX() + 0.5 + 1.5 * Math.sin(angle),
               this.getBlockPos().getY() + 0.5 + Math.sin(angle + Math.PI) / 2.0,
               this.getBlockPos().getZ() + 0.5 + 1.5 * Math.cos(angle),
               0.0,
               0.0,
               0.0
            );
      } else {
         this.spawnTicker++;
         if (this.spawnTicker % 100 == 0
            && this != null
            && ConfigInit.CONFIG.allow_boss_respawn
            && this.bossTime != 0
            && this.level.dimension() == DimensionInit.VOID_WORLD
            && (int)this.level.getLevelData().getGameTime() > this.bossTime + ConfigInit.CONFIG.boss_respawn_time) {
            ((ServerLevel)this.level)
               .playSound(
                  null,
                  this.worldPosition.getX(),
                  this.worldPosition.getY(),
                  this.worldPosition.getZ(),
                  SoundEvents.WITHER_SPAWN,
                  SoundSource.HOSTILE,
                  2.0F,
                  1.0F
               );
            this.bossTime = 0;
            VoidPlacementHandler.spawnVoidBoss((ServerLevel)this.level, this.worldPosition.above());
         }
      }
   }
}
