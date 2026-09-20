package net.dungeonz.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonSyncScreenPacket(BlockPos blockPos, String difficulty) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonSyncScreenPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_sync_screen_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonSyncScreenPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBlockPos(value.blockPos);
        buf.writeUtf(value.difficulty);
    }, buf -> new DungeonSyncScreenPacket(buf.readBlockPos(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
