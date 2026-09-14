package net.dungeonz.network.packet;

import java.util.UUID;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;
import org.jetbrains.annotations.Nullable;

public record DungeonTeleportPacket(class_2338 dungeonPortalPos, boolean isMinGroupRequired, @Nullable UUID uuid) implements class_8710 {

    public static final class_8710.class_9154<DungeonTeleportPacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_teleport_packet"));

    public static final class_9139<class_9129, DungeonTeleportPacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_10807(value.dungeonPortalPos);
        buf.method_52964(value.isMinGroupRequired);
        if (value.isMinGroupRequired) {
            buf.method_10797(value.uuid);
        }
    }, buf -> {
        class_2338 dungeonPortalPos = buf.method_10811();
        boolean isMinGroupRequired = buf.readBoolean();
        UUID uuid = null;
        if (isMinGroupRequired) {
            uuid = buf.method_10790();
        }
        return new DungeonTeleportPacket(dungeonPortalPos, isMinGroupRequired, uuid);
    });

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
