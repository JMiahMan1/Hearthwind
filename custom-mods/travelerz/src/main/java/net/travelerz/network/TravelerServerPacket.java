package net.travelerz.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.nameplate.util.NameplateTracker;
import net.travelerz.network.packet.TravelerPacket;

public class TravelerServerPacket {
   public static void init() {
      PayloadTypeRegistry.clientboundPlay().register(TravelerPacket.PACKET_ID, TravelerPacket.PACKET_CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TravelerPacket.PACKET_ID, TravelerPacket.PACKET_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(TravelerPacket.PACKET_ID, (payload, context) -> {
         payload.mobLevel();
         context.server().execute(() -> {
            Skeleton skeletonEntity = (Skeleton)EntityTypes.SKELETON.create(context.player().level(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            skeletonEntity.snapTo(context.player().getX(), context.player().getY(), context.player().getZ(), 0.0F, 0.0F);

            ServerPlayNetworking.send(context.player(), new TravelerPacket(NameplateTracker.getMobLevel(skeletonEntity)));
            skeletonEntity.discard();
         });
      });
   }
}
