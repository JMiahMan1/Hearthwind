package net.dungeonz.network.packet;

import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonDifficultyPacket(class_2338 portalBlockPos) implements class_8710 {

    public static final class_8710.class_9154<DungeonDifficultyPacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_difficulty_packet"));

    public static final class_9139<class_9129, DungeonDifficultyPacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_10807(value.portalBlockPos);
    }, buf -> new DungeonDifficultyPacket(buf.method_10811()));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
