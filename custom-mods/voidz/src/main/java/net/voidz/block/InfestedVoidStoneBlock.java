package net.voidz.block;

import net.minecraft.util.RandomSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.voidz.init.BlockInit;

public class InfestedVoidStoneBlock extends Block {
   public InfestedVoidStoneBlock(Properties settings) {
      super(settings);
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
      if (random.nextInt(10) == 0) {
         world.addParticle(
            ParticleTypes.PORTAL,
            pos.getX() + random.nextDouble(),
            pos.getY() + 1.0 + random.nextDouble() / 10.0,
            pos.getZ() + random.nextDouble(),
            0.0,
            0.0,
            0.0
         );
      }
   }

   public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
      if (!world.isClientSide() && entity instanceof Player && world.getRandom().nextInt(12) == 0) {
         ((Player)entity).hurt(this.createDamageSource(entity), 2.0F);
      }
   }

   private DamageSource createDamageSource(Entity entity) {
      return entity.damageSources().source(BlockInit.INFESTED, entity);
   }
}
