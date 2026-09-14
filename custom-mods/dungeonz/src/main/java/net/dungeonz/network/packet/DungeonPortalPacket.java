package net.dungeonz.network.packet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonPortalPacket(String dungeonType, class_2338 blockPos, List<UUID> playerUuids, List<UUID> deadPlayerUuids, List<String> difficulties, Map<String, List<class_1799>> possibleLoot,
                                  Map<String, List<class_1799>> requiredItemStacks, int maxGroupSize, int minGroupSize, int waitingPlayerCount, int requiredLevel, int cooldownTime, String difficulty,
                                  boolean allowEnderPearl, boolean allowPositiveEffects, boolean allowElytra, boolean allowRespawn, boolean keepInventory, boolean privateGroup, Optional<class_2960> backgroundId)
        implements class_8710 {

    public static final class_8710.class_9154<DungeonPortalPacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_portal_packet"));

    public static final class_9139<class_9129, DungeonPortalPacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_10814(value.dungeonType);
        buf.method_10807(value.blockPos);
        buf.method_34062(value.playerUuids, (buffer, uuid) -> buffer.method_10797(uuid));
        buf.method_34062(value.deadPlayerUuids, (buffer, uuid) -> buffer.method_10797(uuid));
        buf.method_34062(value.difficulties, class_2540::method_10814);
        buf.method_34063(value.possibleLoot, class_2540::method_10814, (buffer, stacks) -> class_1799.field_48350.encode(buf, stacks));
        buf.method_34063(value.requiredItemStacks, class_2540::method_10814, (buffer, stacks) -> class_1799.field_48350.encode(buf, stacks));
        buf.method_53002(value.maxGroupSize);
        buf.method_53002(value.minGroupSize);
        buf.method_53002(value.waitingPlayerCount);
        buf.method_53002(value.requiredLevel);
        buf.method_53002(value.cooldownTime);
        buf.method_10814(value.difficulty);
        buf.method_52964(value.allowEnderPearl);
        buf.method_52964(value.allowPositiveEffects);
        buf.method_52964(value.allowElytra);
        buf.method_52964(value.allowRespawn);
        buf.method_52964(value.keepInventory);
        buf.method_52964(value.privateGroup);
        buf.method_37435(value.backgroundId, class_2540::method_10812);

    }, buf -> new DungeonPortalPacket(buf.method_19772(), buf.method_10811(), buf.method_34066((buffer) -> class_2540.method_56344(buffer)), buf.method_34066((buffer) -> class_2540.method_56344(buffer)),
            buf.method_34066(class_2540::method_19772), buf.method_34067(class_2540::method_19772, (bufx) -> class_1799.field_48350.decode(buf)),
            buf.method_34067(class_2540::method_19772, (bufx) -> class_1799.field_48350.decode(buf)), buf.readInt(),
            buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.method_19772(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
            buf.method_37436(class_2540::method_10810)));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
