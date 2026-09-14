package net.dungeonz.network.packet;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonTeleportCountdownPacket(int countdownTicks) implements class_8710 {

    public static final class_8710.class_9154<DungeonTeleportCountdownPacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_teleport_countdown_packet"));

    public static final class_9139<class_9129, DungeonTeleportCountdownPacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_53002(value.countdownTicks);
    }, buf -> new DungeonTeleportCountdownPacket(buf.readInt()));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
