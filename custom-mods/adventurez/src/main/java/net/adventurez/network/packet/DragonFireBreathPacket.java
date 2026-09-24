package net.adventurez.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record DragonFireBreathPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DragonFireBreathPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("adventurez", "dragon_fire_breath_packet"));

    public static final StreamCodec<FriendlyByteBuf, DragonFireBreathPacket> PACKET_CODEC = StreamCodec.of((buf, value) -> {
    }, buf -> new DragonFireBreathPacket());

    @Override
    public CustomPacketPayload.Type<DragonFireBreathPacket> type() {
        return PACKET_ID;
    }

}
