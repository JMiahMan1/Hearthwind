package net.dungeonz.init;

import net.dungeonz.block.*;
import net.dungeonz.block.entity.*;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1814;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2378;
import net.minecraft.class_2591;
import net.minecraft.class_2960;
import net.minecraft.class_3917;
import net.minecraft.class_4970;
import net.minecraft.class_7923;

public class BlockInit {

    public static final class_2248 DUNGEON_PORTAL = register("dungeon_portal", new DungeonPortalBlock(class_4970.class_2251.method_9630(class_2246.field_10027)));
    public static final class_2248 DUNGEON_SPAWNER = register("dungeon_spawner", new DungeonSpawnerBlock(class_4970.class_2251.method_9630(class_2246.field_10260)));
    public static final class_2248 DUNGEON_GATE = register("dungeon_gate", new DungeonGateBlock(class_4970.class_2251.method_9630(class_2246.field_9987).method_22488()));

    public static class_2591<DungeonPortalEntity> DUNGEON_PORTAL_ENTITY;
    public static class_2591<DungeonSpawnerEntity> DUNGEON_SPAWNER_ENTITY;
    public static class_2591<DungeonGateEntity> DUNGEON_GATE_ENTITY;

  //  public static final ScreenHandlerType<DungeonPortalScreenHandler> PORTAL = new ExtendedScreenHandlerType<>(DungeonPortalScreenHandler::new);

    public static final class_3917<DungeonPortalScreenHandler> PORTAL = new ExtendedScreenHandlerType<DungeonPortalScreenHandler, DungeonPortalPacket>(
            (syncId, playerInventory, buf) -> new DungeonPortalScreenHandler(syncId, playerInventory, buf), DungeonPortalPacket.PACKET_CODEC);

    private static class_2248 register(String id, class_2248 block) {
        return register(class_2960.method_60655("dungeonz", id), block);
    }

    private static class_2248 register(class_2960 id, class_2248 block) {
        class_1792 item = class_2378.method_10230(class_7923.field_41178, id, new class_1747(block, new class_1792.class_1793().method_7894(class_1814.field_8904)));
        ItemGroupEvents.modifyEntriesEvent(ItemInit.DUNGEONZ_ITEM_GROUP).register(entries -> entries.method_45421(item));

        return class_2378.method_10230(class_7923.field_41175, id, block);
    }

    public static void init() {
        DUNGEON_PORTAL_ENTITY = class_2378.method_10226(class_7923.field_41181, "dungeonz:dungeon_portal_entity",
                class_2591.class_2592.method_20528(DungeonPortalEntity::new, DUNGEON_PORTAL).method_11034(null));
        DUNGEON_SPAWNER_ENTITY = class_2378.method_10226(class_7923.field_41181, "dungeonz:dungeon_spawner_entity",
                class_2591.class_2592.method_20528(DungeonSpawnerEntity::new, DUNGEON_SPAWNER).method_11034(null));
        DUNGEON_GATE_ENTITY = class_2378.method_10226(class_7923.field_41181, "dungeonz:dungeon_gate_entity", class_2591.class_2592.method_20528(DungeonGateEntity::new, DUNGEON_GATE).method_11034(null));

        class_2378.method_10226(class_7923.field_41187, "dungeonz:portal", PORTAL);
    }
}
