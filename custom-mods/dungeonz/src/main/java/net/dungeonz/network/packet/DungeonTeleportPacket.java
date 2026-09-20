package net.dungeonz.network.packet;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public record DungeonTeleportPacket(BlockPos dungeonPortalPos, boolean isMinGroupRequired, @Nullable UUID uuid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonTeleportPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_teleport_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonTeleportPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBlockPos(value.dungeonPortalPos);
        buf.writeBoolean(value.isMinGroupRequired);
        if (value.isMinGroupRequired) {
            buf.writeUUID(value.uuid);
        }
    }, buf -> {
        BlockPos dungeonPortalPos = buf.readBlockPos();
        boolean isMinGroupRequired = buf.readBoolean();
        UUID uuid = null;
        if (isMinGroupRequired) {
            uuid = buf.readUUID();
        }
        return new DungeonTeleportPacket(dungeonPortalPos, isMinGroupRequired, uuid);
    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
