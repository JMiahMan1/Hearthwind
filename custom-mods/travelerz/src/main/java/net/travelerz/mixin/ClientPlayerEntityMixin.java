package net.travelerz.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.biome.Biome;
import net.travelerz.network.packet.TravelerPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({LocalPlayer.class})
public class ClientPlayerEntityMixin extends AbstractClientPlayer {
   @Unique
   @Nullable
   private Biome travelerBiome;

   public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
      super(world, profile);
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void tickMixin(CallbackInfo info) {
      Biome biome = (Biome)this.level().getBiomeManager().getBiome(this.blockPosition()).value();
      if (this.travelerBiome != biome) {
         ClientPlayNetworking.send(new TravelerPacket(0));
         this.travelerBiome = biome;
      }
   }
}
