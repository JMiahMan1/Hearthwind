package net.dungeonz.network.packet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DungeonPortalPacket(String dungeonType, BlockPos blockPos, List<UUID> playerUuids, List<UUID> deadPlayerUuids, List<String> difficulties, Map<String, List<ItemStack>> possibleLoot,
                                  Map<String, List<ItemStack>> requiredItemStacks, int maxGroupSize, int minGroupSize, int waitingPlayerCount, int requiredLevel, int cooldownTime, String difficulty,
                                  boolean allowEnderPearl, boolean allowPositiveEffects, boolean allowElytra, boolean allowRespawn, boolean keepInventory, boolean privateGroup, Optional<Identifier> backgroundId, DungeonAdmissionPacket admission)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DungeonPortalPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_portal_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonPortalPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeUtf(value.dungeonType);
        buf.writeBlockPos(value.blockPos);
        buf.writeCollection(value.playerUuids, (buffer, uuid) -> buffer.writeUUID(uuid));
        buf.writeCollection(value.deadPlayerUuids, (buffer, uuid) -> buffer.writeUUID(uuid));
        buf.writeCollection(value.difficulties, FriendlyByteBuf::writeUtf);
        buf.writeMap(value.possibleLoot, FriendlyByteBuf::writeUtf, (buffer, stacks) -> ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, stacks));
        buf.writeMap(value.requiredItemStacks, FriendlyByteBuf::writeUtf, (buffer, stacks) -> ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, stacks));
        buf.writeInt(value.maxGroupSize);
        buf.writeInt(value.minGroupSize);
        buf.writeInt(value.waitingPlayerCount);
        buf.writeInt(value.requiredLevel);
        buf.writeInt(value.cooldownTime);
        buf.writeUtf(value.difficulty);
        buf.writeBoolean(value.allowEnderPearl);
        buf.writeBoolean(value.allowPositiveEffects);
        buf.writeBoolean(value.allowElytra);
        buf.writeBoolean(value.allowRespawn);
        buf.writeBoolean(value.keepInventory);
        buf.writeBoolean(value.privateGroup);
        buf.writeOptional(value.backgroundId, FriendlyByteBuf::writeIdentifier);
        DungeonAdmissionPacket.PACKET_CODEC.encode(buf, value.admission);

    }, buf -> new DungeonPortalPacket(buf.readUtf(), buf.readBlockPos(), buf.readList((buffer) -> FriendlyByteBuf.readUUID(buffer)), buf.readList((buffer) -> FriendlyByteBuf.readUUID(buffer)),
            buf.readList(FriendlyByteBuf::readUtf), buf.readMap(FriendlyByteBuf::readUtf, (bufx) -> ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf)),
            buf.readMap(FriendlyByteBuf::readUtf, (bufx) -> ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf)), buf.readInt(),
            buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
            buf.readOptional(FriendlyByteBuf::readIdentifier), DungeonAdmissionPacket.PACKET_CODEC.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
