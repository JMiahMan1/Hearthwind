package net.dungeonz.init;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.util.DungeonHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2561;

public class CommandInit {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((class_2170.method_9247("dungeon").requires((serverCommandSource) -> {
                return serverCommandSource.method_9259(0);
            })).then(class_2170.method_9247("leave").executes((commandContext) -> {
                return executeDungeonCommand(commandContext.getSource());
            })));
        });
    }

    private static int executeDungeonCommand(class_2168 source) {
        if (source.method_44023() != null) {
            if (DungeonHelper.getCurrentDungeon(source.method_44023()) != null) {
                if (DungeonHelper.getDungeonPortalEntity(source.method_44023()) != null) {
                    DungeonPortalEntity dungeonPortalEntity = DungeonHelper.getDungeonPortalEntity(source.method_44023());
                    dungeonPortalEntity.getDungeonPlayerUuids().remove(source.method_44023().method_5667());
                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        dungeonPortalEntity.setCooldownTime(dungeonPortalEntity.getDungeon().getCooldown() + (int) source.method_44023().method_51469().method_8510());
                    }
                    dungeonPortalEntity.method_5431();
                }
                DungeonHelper.teleportOutOfDungeon(source.method_44023());
            } else {
                source.method_9226(() -> class_2561.method_43471("text.dungeonz.dungeon_missing"), false);
            }
        }

        return 1;
    }

}
