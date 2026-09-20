package net.dungeonz.init;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.util.DungeonHelper;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

public class EventInit {

    public static void init() {
        EntityElytraEvents.ALLOW.register((entity) -> {
            if (entity instanceof Player) {
                Player playerEntity = (Player) entity;
                if (playerEntity != null && !playerEntity.isCreative() && playerEntity.level().dimension() == DimensionInit.DUNGEON_WORLD) {
                    if (!playerEntity.level().isClientSide()) {
                        if (DungeonHelper.getCurrentDungeon((ServerPlayer) playerEntity) != null) {
                            return DungeonHelper.getCurrentDungeon((ServerPlayer) playerEntity).isElytraAllowed();
                        }
                    } else {
                        return ((ClientPlayerAccess) playerEntity).isElytraAllowed();
                    }
                }
            }
            return true;
        });
        ServerPlayerEvents.COPY_FROM.register((ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) -> {
            if (((ServerPlayerAccess) oldPlayer).getOldServerWorld() != null) {
                ((ServerPlayerAccess) newPlayer).setDungeonInfo(((ServerPlayerAccess) oldPlayer).getOldServerWorld(), ((ServerPlayerAccess) oldPlayer).getDungeonPortalBlockPos(),
                        ((ServerPlayerAccess) oldPlayer).getDungeonSpawnBlockPos());
            }
            if (oldPlayer.level().dimension() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getCurrentDungeon(oldPlayer) != null
                    && DungeonHelper.getCurrentDungeon(oldPlayer).isKeepInventory()) {
                newPlayer.getInventory().replaceWith(oldPlayer.getInventory());
            }
        });
    }

}
