package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public abstract class InjectEntity {
   @Shadow
   private Vec3 deltaMovement;
   @Shadow
   private EntityDimensions dimensions;
   @Shadow
   @Final
   protected RandomSource random;
   @Unique
   public Queue<Double> velocities = new LinkedList<>();

   @Shadow
   public abstract double getX();

   @Shadow
   public abstract double getY();

   @Shadow
   public abstract double getZ();

   @Shadow
   public abstract Level level();

   @Shadow
   public abstract Vec3 position();

   @Shadow
   public abstract BlockPos blockPosition();

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void onSetVelocity(CallbackInfo ci) {
      if (Main.CONFIG.waterSplash()) {
         this.velocities.offer(Math.abs(this.deltaMovement.y()));
         if (this.velocities.size() > 4) {
            this.velocities.poll();
         }
      }
   }

   @Inject(
      method = {"doWaterSplashEffect"},
      at = {@At("TAIL")}
   )
   private void waterParticles(CallbackInfo ci) {
      if (Main.CONFIG.waterSplash()) {
         if (!(((Object) this) instanceof Arrow) && this.level().isClientSide()) {
            float baseY = Mth.floor(this.getY());
            boolean foundSurface = false;
            FluidState prevState = Fluids.EMPTY.defaultFluidState();

            for (int i = 0; i < 5; i++) {
               FluidState nextState = this.level().getFluidState(this.blockPosition().offset(0, i, 0));
               if (prevState.is(Fluids.WATER) && nextState.is(Fluids.EMPTY)) {
                  baseY += i - 1;
                  foundSurface = true;
                  break;
               }

               prevState = nextState;
            }

            if (foundSurface) {
               if (this.velocities.size() >= 2) {
                  this.level()
                     .addParticle(
                        Particles.WATER_SPLASH_EMITTER,
                        this.getX(),
                        baseY + prevState.getOwnHeight(),
                        this.getZ(),
                        this.dimensions.width(),
                        Collections.max(this.velocities),
                        0.0
                     );
               }
            }
         }
      }
   }
}
