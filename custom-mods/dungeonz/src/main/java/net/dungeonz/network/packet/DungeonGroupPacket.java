package net.dungeonz.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonGroupPacket(BlockPos portalBlockPos, boolean privateGroup) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonGroupPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_group_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonGroupPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
        buf.writeBoolean(value.privateGroup);
    }, buf -> new DungeonGroupPacket(buf.readBlockPos(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
