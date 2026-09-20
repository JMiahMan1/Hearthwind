package net.dungeonz.network.packet;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonCompassScreenPacket(String dungeonType, List<String> dungeonIdList) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonCompassScreenPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_compass_screen_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonCompassScreenPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeUtf(value.dungeonType);
        buf.writeCollection(value.dungeonIdList, FriendlyByteBuf::writeUtf);
    }, buf -> new DungeonCompassScreenPacket(buf.readUtf(), buf.readList(FriendlyByteBuf::readUtf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
