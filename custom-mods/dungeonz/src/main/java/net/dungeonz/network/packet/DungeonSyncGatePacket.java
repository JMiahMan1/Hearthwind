package net.dungeonz.network.packet;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonSyncGatePacket(Set<BlockPos> dungeonGatesPosList, String blockId, String particleEffect, String unlockItem) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonSyncGatePacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_sync_gate_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonSyncGatePacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeCollection(value.dungeonGatesPosList, BlockPos.STREAM_CODEC);
        buf.writeUtf(value.blockId);
        buf.writeUtf(value.particleEffect);
        buf.writeUtf(value.unlockItem);
    }, buf -> new DungeonSyncGatePacket(buf.readCollection(HashSet::new, BlockPos.STREAM_CODEC), buf.readUtf(), buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
