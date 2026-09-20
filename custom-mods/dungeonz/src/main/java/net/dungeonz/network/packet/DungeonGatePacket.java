package net.dungeonz.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonGatePacket(BlockPos portalBlockPos, String blockId, String particleId, String unlockItemId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonGatePacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_gate_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonGatePacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
        buf.writeUtf(value.blockId);
        buf.writeUtf(value.particleId);
        buf.writeUtf(value.unlockItemId);
    }, buf -> new DungeonGatePacket(buf.readBlockPos(), buf.readUtf(), buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
