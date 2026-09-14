package net.dungeonz.init;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.util.DungeonHelper;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.class_1657;
import net.minecraft.class_3222;

public class EventInit {

    public static void init() {
        EntityElytraEvents.ALLOW.register((entity) -> {
            if (entity instanceof class_1657) {
                class_1657 playerEntity = (class_1657) entity;
                if (playerEntity != null && !playerEntity.method_7337() && playerEntity.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
                    if (!playerEntity.method_37908().method_8608()) {
                        if (DungeonHelper.getCurrentDungeon((class_3222) playerEntity) != null) {
                            return DungeonHelper.getCurrentDungeon((class_3222) playerEntity).isElytraAllowed();
                        }
                    } else {
                        return ((ClientPlayerAccess) playerEntity).isElytraAllowed();
                    }
                }
            }
            return true;
        });
        ServerPlayerEvents.COPY_FROM.register((class_3222 oldPlayer, class_3222 newPlayer, boolean alive) -> {
            if (((ServerPlayerAccess) oldPlayer).getOldServerWorld() != null) {
                ((ServerPlayerAccess) newPlayer).setDungeonInfo(((ServerPlayerAccess) oldPlayer).getOldServerWorld(), ((ServerPlayerAccess) oldPlayer).getDungeonPortalBlockPos(),
                        ((ServerPlayerAccess) oldPlayer).getDungeonSpawnBlockPos());
            }
            if (oldPlayer.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getCurrentDungeon(oldPlayer) != null
                    && DungeonHelper.getCurrentDungeon(oldPlayer).isKeepInventory()) {
                newPlayer.method_31548().method_7377(oldPlayer.method_31548());
            }
        });
    }

}
