package net.dungeonz.network.packet;

import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonInfoPacket(List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) implements class_8710 {

    public static final class_8710.class_9154<DungeonInfoPacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_info_packet"));

    public static final class_9139<class_9129, DungeonInfoPacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_34062(value.breakableBlockIdList, class_2540::method_53002);
        buf.method_34062(value.placeableBlockIdList, class_2540::method_53002);
        buf.method_52964(value.allowElytra);
    }, buf -> new DungeonInfoPacket(buf.method_34066(class_2540::readInt), buf.method_34066(class_2540::readInt), buf.readBoolean()));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
