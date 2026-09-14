package net.dungeonz.network.packet;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonSyncGatePacket(Set<class_2338> dungeonGatesPosList, String blockId, String particleEffect, String unlockItem) implements class_8710 {

    public static final class_8710.class_9154<DungeonSyncGatePacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_sync_gate_packet"));

    public static final class_9139<class_9129, DungeonSyncGatePacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_34062(value.dungeonGatesPosList, class_2338.field_48404);
        buf.method_10814(value.blockId);
        buf.method_10814(value.particleEffect);
        buf.method_10814(value.unlockItem);
    }, buf -> new DungeonSyncGatePacket(buf.method_34068(HashSet::new, class_2338.field_48404), buf.method_19772(), buf.method_19772(), buf.method_19772()));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
