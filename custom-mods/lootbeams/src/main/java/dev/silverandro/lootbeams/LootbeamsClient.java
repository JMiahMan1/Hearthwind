package dev.silverandro.lootbeams;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

/**
 * Every client world tick, item entities inside the configured box around
 * the player get their beam particles. Upstream cached the colours in a
 * WeakHashMap and re-queried the same box each tick; querying fresh is
 * equivalent for on-screen behaviour.
 */
public final class LootbeamsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_LEVEL_TICK.register(this::onEndLevelTick);
    }

    private void onEndLevelTick(ClientLevel level) {
        Minecraft client = Minecraft.getInstance();
        LootbeamsConfig config = Lootbeams.CONFIG;
        if (client.player == null || config == null) {
            return;
        }
        AABB box = new AABB(client.player.blockPosition()).inflate(config.beamDistance);
        for (ItemEntity item : level.getEntities(EntityTypes.ITEM, box, entity -> true)) {
            int colour = LootbeamsColors.colorFor(item, config);
            if (!LootbeamsColors.shouldShow(item, colour, config)) {
                continue;
            }
            LootbeamsParticles.generateParticles(item, colour, config);
        }
    }
}
