package net.dungeonz.network.packet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.dungeonz.DungeonzMain;
import net.dungeonz.compat.HearthwindGroups;
import net.dungeonz.compat.HearthwindLevels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record DungeonAdmissionPacket(int containerId, boolean levelsEnabled, boolean groupsEnabled, int overallLevel,
        List<UUID> members, Optional<UUID> leader) implements CustomPacketPayload {
    public static final Type<DungeonAdmissionPacket> PACKET_ID = new Type<>(Identifier.parse("dungeonz:admission"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonAdmissionPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeVarInt(value.containerId);
        buf.writeBoolean(value.levelsEnabled);
        buf.writeBoolean(value.groupsEnabled);
        buf.writeVarInt(value.overallLevel);
        buf.writeCollection(value.members, (buffer, uuid) -> buffer.writeUUID(uuid));
        buf.writeOptional(value.leader, (buffer, uuid) -> buffer.writeUUID(uuid));
    }, buf -> new DungeonAdmissionPacket(buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt(),
            buf.readList(buffer -> buffer.readUUID()), buf.readOptional(buffer -> buffer.readUUID())));

    public DungeonAdmissionPacket {
        members = List.copyOf(members);
    }

    public static DungeonAdmissionPacket snapshot(ServerPlayer player, int containerId) {
        var group = HearthwindGroups.group(player);
        return new DungeonAdmissionPacket(containerId, DungeonzMain.isLevelZLoaded, DungeonzMain.isPartyAddonLoaded,
                HearthwindLevels.overallLevel(player), group.members(), group.leader());
    }

    public static DungeonAdmissionPacket empty() {
        return new DungeonAdmissionPacket(0, false, false, 0, List.of(), Optional.empty());
    }

    public boolean meetsRequiredLevel(int requiredLevel) {
        return !levelsEnabled || overallLevel >= requiredLevel;
    }

    public boolean admits(UUID firstOccupant) {
        return groupsEnabled && members.contains(firstOccupant);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
