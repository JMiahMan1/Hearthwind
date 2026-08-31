package dev.jmiahman.hearthwind.survival;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Spoilage system replacing spoiledz (parity-first):
 * perishable items in player inventories and containers slowly rot into rotten
 * flesh. Perishables are the <code>spoiledz:perishable_items</code> tag;
 * anything in the <code>spoiledz:non_spoiling_items</code> tag (honey, teas,
 * wines, ...) is always exempt. Hot biomes accelerate the process.
 */
public final class HearthwindSurvivalSpoilage {
    public static final TagKey<Item> PERISHABLE =
            TagKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath("spoiledz", "perishable_items"));
    public static final TagKey<Item> NON_SPOILING =
            TagKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath("spoiledz", "non_spoiling_items"));

    /** Tracks containers (chests, furnaces, etc.) that need spoilage checks. */
    private static final Set<BlockEntity> CONTAINER_ENTITIES = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private HearthwindSurvivalSpoilage() {}

    public static void registerTickLoop() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, level) -> {
            if (be instanceof Container) {
                CONTAINER_ENTITIES.add(be);
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, level) -> {
            CONTAINER_ENTITIES.remove(be);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            HearthwindSurvivalConfig.Spoilage cfg = HearthwindSurvivalConfig.get().spoilage;
            if (cfg.chancePerCheck <= 0 || server.getTickCount() % cfg.checkIntervalTicks != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickPlayer(player, cfg);
            }
            tickContainers(server, cfg);
        });
    }

    private static void tickPlayer(ServerPlayer player, HearthwindSurvivalConfig.Spoilage cfg) {
        if (player.getAbilities().invulnerable || player.getAbilities().instabuild) {
            return;
        }
        double chance = cfg.chancePerCheck;
        float biomeTemp = player.level().getBiome(player.blockPosition())
                .value().getBaseTemperature();
        if (biomeTemp > 1.5f) {
            chance *= cfg.hotBiomeMultiplier;
        }

        spoilContainer(player.getInventory(), player.getRandom(), chance, cfg.rotsInto,
                rotted -> {
                    if (!player.getInventory().add(rotted)) {
                        player.drop(rotted, false);
                    }
                });
    }

    private static void tickContainers(net.minecraft.server.MinecraftServer server,
            HearthwindSurvivalConfig.Spoilage cfg) {
        for (BlockEntity be : CONTAINER_ENTITIES) {
            if (be.isRemoved() || be.getLevel() == null) {
                CONTAINER_ENTITIES.remove(be);
                continue;
            }
            if (!(be instanceof Container container)) {
                continue;
            }
            net.minecraft.world.level.Level level = be.getLevel();
            if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
                continue;
            }
            double chance = cfg.chancePerCheck;
            net.minecraft.core.BlockPos pos = be.getBlockPos();
            float biomeTemp = serverLevel.getBiome(pos)
                    .value().getBaseTemperature();
            if (biomeTemp > 1.5f) {
                chance *= cfg.hotBiomeMultiplier;
            }
            spoilContainer(container, serverLevel.getRandom(), chance, cfg.rotsInto,
                    rotted -> {
                        net.minecraft.world.entity.item.ItemEntity drop =
                                new net.minecraft.world.entity.item.ItemEntity(
                                        serverLevel,
                                        pos.getX() + 0.5,
                                        pos.getY() + 0.5,
                                        pos.getZ() + 0.5,
                                        rotted);
                        serverLevel.addFreshEntity(drop);
                    });
        }
    }

    /**
     * One spoilage pass over any container (player inventory, chest, ...).
     * Rotten output for each spoiled item is handed to {@code spill};
     * exposed for gametests and future container support.
     *
     * @return number of items that rotted this pass
     */
    public static int spoilContainer(Container container,
            net.minecraft.util.RandomSource random, double chance, String rotId,
            java.util.function.Consumer<ItemStack> spill) {
        int rotted = 0;
        int slots = container.getContainerSize();
        ItemStack rot = rotStack(rotId);
        for (int i = 0; i < slots; i++) {
            ItemStack s = container.getItem(i);
            if (s.isEmpty() || !s.is(PERISHABLE) || s.is(NON_SPOILING)) {
                continue;
            }
            if (random.nextDouble() >= chance) {
                continue;
            }
            s.shrink(1);
            rotted++;
            if (!rot.isEmpty()) {
                spill.accept(rot.copy());
            }
        }
        return rotted;
    }

    private static ItemStack rotStack(String rotId) {
        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getValue(Identifier.parse(rotId));
        return new ItemStack(item);
    }
}
