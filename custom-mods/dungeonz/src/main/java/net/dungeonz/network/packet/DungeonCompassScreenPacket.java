package net.dungeonz.network.packet;

import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record DungeonCompassScreenPacket(String dungeonType, List<String> dungeonIdList) implements class_8710 {

    public static final class_8710.class_9154<DungeonCompassScreenPacket> PACKET_ID = new class_8710.class_9154<>(class_2960.method_60655("dungeonz", "dungeon_compass_screen_packet"));

    public static final class_9139<class_9129, DungeonCompassScreenPacket> PACKET_CODEC = class_9139.method_56438((value, buf) -> {
        buf.method_10814(value.dungeonType);
        buf.method_34062(value.dungeonIdList, class_2540::method_10814);
    }, buf -> new DungeonCompassScreenPacket(buf.method_19772(), buf.method_34066(class_2540::method_19772)));

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return PACKET_ID;
    }

}
