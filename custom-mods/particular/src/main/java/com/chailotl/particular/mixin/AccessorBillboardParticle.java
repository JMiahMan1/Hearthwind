package com.chailotl.particular.mixin;

import net.minecraft.client.particle.SingleQuadParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({SingleQuadParticle.class})
public interface AccessorBillboardParticle {
   @Accessor("quadSize")
   void setScale(float var1);
}
