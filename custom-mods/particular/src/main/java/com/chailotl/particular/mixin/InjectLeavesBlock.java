package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LeavesBlock.class})
public class InjectLeavesBlock {
   @Inject(
      method = {"randomDisplayTick"},
      at = {@At("HEAD")}
   )
   private void dropLeaves(BlockState state, Level world, BlockPos pos, RandomSource random, CallbackInfo ci) {
      if (Main.CONFIG.fallingLeaves()) {
         if (random.nextInt(Main.CONFIG.fallingLeavesSettings.spawnChance()) == 0) {
            BlockPos blockPos = pos.below();
            BlockState blockState = world.getBlockState(blockPos);
            if (!Block.isFaceFull(blockState.getCollisionShape(world, blockPos), Direction.UP)) {
               double x = pos.getX() + 0.02 + random.nextDouble() * 0.96;
               double y = pos.getY() - 0.05;
               double z = pos.getZ() + 0.02 + random.nextDouble() * 0.96;
               Main.LeafData leafData = Main.getLeafData(state.getBlock());
               ParticleOptions particle = leafData.getParticle();
               if (particle == null) {
                  return;
               }

               Color color = leafData.getColor(world, pos);
               Particle leaf = Minecraft.getInstance().particleEngine.createParticle(particle, x, y, z, 0.0, 0.0, 0.0);
               if (leaf != null) {
                  ((net.minecraft.client.particle.SingleQuadParticle) leaf).setColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
               }
            }
         }
      }
   }
}
