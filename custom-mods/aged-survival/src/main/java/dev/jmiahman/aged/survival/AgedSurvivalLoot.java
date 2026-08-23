package dev.jmiahman.aged.survival;

import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

/**
 * Sourcing loop for temperature items, parity with environmentz:
 *  - wolves drop 0-2 wolf_pelt
 *  - polar bears drop 1-3 polar_bear_fur (their vanilla table keeps
 *    its own drops; we only append)
 */
public final class AgedSurvivalLoot {

    private static boolean isEntityTable(Optional<ResourceKey<LootTable>> entityKey,
            ResourceKey<LootTable> key) {
        return entityKey.isPresent() && entityKey.get().equals(key);
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (isEntityTable(EntityTypes.WOLF.getDefaultLootTable(), key)) {
                builder.withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(0.0f, 2.0f))
                        .add(LootItem.lootTableItem(EnvironmentzItems.WOLF_PELT)));
            }
            if (isEntityTable(EntityTypes.POLAR_BEAR.getDefaultLootTable(), key)) {
                builder.withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0f, 3.0f))
                        .add(LootItem.lootTableItem(EnvironmentzItems.POLAR_BEAR_FUR)));
            }
        });

        AgedSurvival.LOGGER.info("aged-survival: loot hooks installed (wolf pelt, bear fur)");
    }

    private AgedSurvivalLoot() {
    }
}
