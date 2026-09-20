package net.dungeonz.init;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.util.DungeonHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandInit {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((Commands.literal("dungeon").requires(Commands.hasPermission(Commands.LEVEL_ALL))).then(Commands.literal("leave").executes((commandContext) -> {
                return executeDungeonCommand(commandContext.getSource());
            })));
        });
    }

    private static int executeDungeonCommand(CommandSourceStack source) {
        if (source.getPlayer() != null) {
            if (DungeonHelper.getCurrentDungeon(source.getPlayer()) != null) {
                if (DungeonHelper.getDungeonPortalEntity(source.getPlayer()) != null) {
                    DungeonPortalEntity dungeonPortalEntity = DungeonHelper.getDungeonPortalEntity(source.getPlayer());
                    dungeonPortalEntity.getDungeonPlayerUuids().remove(source.getPlayer().getUUID());
                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        dungeonPortalEntity.setCooldownTime(dungeonPortalEntity.getDungeon().getCooldown() + (int) source.getPlayer().level().getGameTime());
                    }
                    dungeonPortalEntity.setChanged();
                }
                DungeonHelper.teleportOutOfDungeon(source.getPlayer());
            } else {
                source.sendSuccess(() -> Component.translatable("text.dungeonz.dungeon_missing"), false);
            }
        }

        return 1;
    }

}
