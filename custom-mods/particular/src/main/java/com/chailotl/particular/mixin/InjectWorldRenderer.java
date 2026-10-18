package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class InjectWorldRenderer {
   @Inject(
      method = {"tickRainSplashing"},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/CampfireBlock;isLitCampfire(Lnet/minecraft/block/BlockState;)Z"
         ),
         to = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"
         )
      )},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/render/WorldRenderer;client:Lnet/minecraft/client/MinecraftClient;",
         opcode = 180
      )}
   )
   private void modifyParticleEffect(
      net.minecraft.client.Camera camera,
      CallbackInfo ci,
      @Local net.minecraft.world.level.material.FluidState fluidState,
      @Local LocalRef<net.minecraft.core.particles.ParticleOptions> particleEffect
   ) {
      if (fluidState.is(FluidTags.WATER) && Main.CONFIG.rainRipples()) {
         particleEffect.set(Particles.WATER_RIPPLE);
      }
   }
}
