package net.dungeonz.network.packet;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonInfoPacket(List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonInfoPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_info_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonInfoPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeCollection(value.breakableBlockIdList, FriendlyByteBuf::writeInt);
        buf.writeCollection(value.placeableBlockIdList, FriendlyByteBuf::writeInt);
        buf.writeBoolean(value.allowElytra);
    }, buf -> new DungeonInfoPacket(buf.readList(FriendlyByteBuf::readInt), buf.readList(FriendlyByteBuf::readInt), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
