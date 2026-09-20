package net.dungeonz.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonDifficultyPacket(BlockPos portalBlockPos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonDifficultyPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_difficulty_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonDifficultyPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
    }, buf -> new DungeonDifficultyPacket(buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
