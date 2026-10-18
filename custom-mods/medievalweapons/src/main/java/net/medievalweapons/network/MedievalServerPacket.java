package net.medievalweapons.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.medievalweapons.access.PlayerAccess;
import net.medievalweapons.network.packet.AttackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class MedievalServerPacket {
   public static void init() {
      PayloadTypeRegistry.serverboundPlay().register(AttackPacket.PACKET_ID, AttackPacket.PACKET_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(AttackPacket.PACKET_ID, (payload, context) -> {
         int entityId = payload.entityId();
         ServerPlayer player = context.player();
         player.resetAttackStrengthTicker();
         Entity target = player.level().getEntity(entityId);
         if (target != null) {
            ((PlayerAccess)player).doOffhandAttack(target);
         }
      });
   }
}
