package net.dungeonz.network.packet;

import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonGroupPacket(class_2338 portalBlockPos, boolean privateGroup) implements class_8710 {

    public static final class_8710.class_9154<DungeonGroupPacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_group_packet"));

    public static final class_9139<class_9129, DungeonGroupPacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_10807(value.portalBlockPos);
        buf.method_52964(value.privateGroup);
    }, buf -> new DungeonGroupPacket(buf.method_10811(), buf.readBoolean()));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
