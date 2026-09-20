package net.dungeonz.network.packet;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonCompassPacket(String dungeonType) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonCompassPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_compass_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonCompassPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeUtf(value.dungeonType);
    }, buf -> new DungeonCompassPacket(buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
