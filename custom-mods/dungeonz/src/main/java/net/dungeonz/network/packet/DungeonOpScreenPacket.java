package net.dungeonz.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DungeonOpScreenPacket(BlockPos blockPos, String blockIdOrDungeonType, String particleEffectOrDifficulty, String unlockItem) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonOpScreenPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_op_screen_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonOpScreenPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {

        buf.writeBlockPos(value.blockPos);
        buf.writeUtf(value.blockIdOrDungeonType);
        buf.writeUtf(value.particleEffectOrDifficulty);
        buf.writeUtf(value.unlockItem);
    }, buf -> new DungeonOpScreenPacket(buf.readBlockPos(), buf.readUtf(), buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
