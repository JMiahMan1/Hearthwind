package net.dungeonz.network.packet;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonTeleportCountdownPacket(int countdownTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonTeleportCountdownPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_teleport_countdown_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonTeleportCountdownPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeInt(value.countdownTicks);
    }, buf -> new DungeonTeleportCountdownPacket(buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
