package net.dungeonz.network.packet;

import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonGatePacket(class_2338 portalBlockPos, String blockId, String particleId, String unlockItemId) implements class_8710 {

    public static final class_8710.class_9154<DungeonGatePacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_gate_packet"));

    public static final class_9139<class_9129, DungeonGatePacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_10807(value.portalBlockPos);
        buf.method_10814(value.blockId);
        buf.method_10814(value.particleId);
        buf.method_10814(value.unlockItemId);
    }, buf -> new DungeonGatePacket(buf.method_10811(), buf.method_19772(), buf.method_19772(), buf.method_19772()));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
