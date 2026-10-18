package net.nameplate.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.Mob;
import net.nameplate.access.MobEntityAccess;
import net.nameplate.network.packet.LevelPacket;

@Environment(EnvType.CLIENT)
public class NameplateClientPacket {
   public static void init() {
      ClientPlayNetworking.registerGlobalReceiver(LevelPacket.PACKET_ID, (payload, context) -> {
         int mobLevel = payload.mobLevel();
         int mobId = payload.mobId();
         boolean hasRpgLabel = payload.hasRpgLabel();
         context.client().execute(() -> {
            if (context.client().level.getEntity(mobId) != null && context.client().level.getEntity(mobId) instanceof Mob mobEntity) {
               ((MobEntityAccess)mobEntity).setMobRpgLevel(mobLevel);
               ((MobEntityAccess)mobEntity).setShowMobRpgLabel(hasRpgLabel);
            }
         });
      });
   }
}
