package net.adventurez.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record VelocityPacket(int entityId, float velocity) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VelocityPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("adventurez", "velocity_packet"));

    public static final StreamCodec<FriendlyByteBuf, VelocityPacket> PACKET_CODEC = StreamCodec.of((buf, value) -> {
        buf.writeInt(value.entityId);
        buf.writeFloat(value.velocity);
    }, buf -> new VelocityPacket(buf.readInt(), buf.readFloat()));

    @Override
    public CustomPacketPayload.Type<VelocityPacket> type() {
        return PACKET_ID;
    }

}
