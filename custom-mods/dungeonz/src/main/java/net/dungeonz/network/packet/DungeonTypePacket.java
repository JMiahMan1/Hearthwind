package net.dungeonz.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonTypePacket(BlockPos portalBlockPos, String dungeonType, String defaultDifficulty) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonTypePacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_type_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonTypePacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
        buf.writeUtf(value.dungeonType);
        buf.writeUtf(value.defaultDifficulty);
    }, buf -> new DungeonTypePacket(buf.readBlockPos(), buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
