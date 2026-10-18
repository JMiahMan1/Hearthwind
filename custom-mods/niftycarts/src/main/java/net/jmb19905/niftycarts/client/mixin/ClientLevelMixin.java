package net.jmb19905.niftycarts.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.jmb19905.niftycarts.network.serverbound.RequestCartUpdatePayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ClientLevel.class})
public class ClientLevelMixin {
   @Inject(
      method = {"addEntity"},
      at = {@At("TAIL")}
   )
   public void addEntity(Entity entity, CallbackInfo ci) {
      if (entity instanceof AbstractDrawnEntity d) {
         ClientPlayNetworking.send(new RequestCartUpdatePayload(d.getId()));
      }
   }
}
