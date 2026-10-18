package net.travelerz.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.travelerz.TravelerzMain;
import net.travelerz.mixin.InGameHudAccess;
import net.travelerz.network.packet.TravelerPacket;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TravelerzClientPacket {
   public static void init() {
      ClientPlayNetworking.registerGlobalReceiver(TravelerPacket.PACKET_ID, (payload, context) -> {
         int mobLevel = payload.mobLevel();
         context.client().execute(() -> {
            Minecraft client = context.client();
            if (((InGameHudAccess)client.gui.hud).getTitle() == null) {
               Component biomeName = getBiomeName(client);
               if (biomeName != null) {
                  client.gui.hud.setTitle(Component.translatable("hud.travelerz.title", new Object[]{biomeName}));
                  if (mobLevel >= 0) {
                     client.gui.hud.setSubtitle(Component.translatable("hud.travelerz.subtitle", new Object[]{mobLevel}));
                  }
               }
            }
         });
      });
   }

   @Nullable
   private static Component getBiomeName(Minecraft client) {
      net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry = client.player
         .level()
         .registryAccess()
         .lookupOrThrow(Registries.BIOME);
      Identifier biomeIdentifier = registry.getKey(
         (Biome)client.player.level().getBiome(client.player.blockPosition()).value()
      );
      return TravelerzMain.CONFIG.excludedBiomes.contains(biomeIdentifier.getNamespace() + ":" + biomeIdentifier.getPath())
         ? null
         : Component.translatable("biome." + biomeIdentifier.getNamespace() + "." + biomeIdentifier.getPath());
   }
}
