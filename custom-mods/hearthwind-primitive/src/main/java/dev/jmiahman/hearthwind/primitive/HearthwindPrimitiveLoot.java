package dev.jmiahman.hearthwind.primitive;

import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

/**
 * Progression gate, parity with earlystage's "no bare hands on stone":
 *  - stone drops 1-3 rock items instead of cobblestone (silk touch
 *    intentionally bypassed so the rock economy always applies)
 *  - gravel yields a bonus rock ~30% of the time
 */
public final class HearthwindPrimitiveLoot {

    private static boolean isBlockTable(ResourceKey<LootTable> key,
            Optional<ResourceKey<LootTable>> blockKey) {
        return blockKey.isPresent() && blockKey.get().equals(key);
    }

    public static void init() {
        LootTableEvents.REPLACE.register((key, original, source, registries) -> {
            if (isBlockTable(key, Blocks.STONE.getLootTable())) {
                return LootTable.lootTable()
                        .setParamSet(LootContextParamSets.BLOCK)
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1.0f, 3.0f))
                                .add(LootItem.lootTableItem(HearthwindPrimitiveItems.ROCK)))
                        .build();
            }
            return null;
        });

        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (isBlockTable(key, Blocks.GRAVEL.getLootTable())) {
                builder.withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0f, 1.0f))
                        .when(LootItemRandomChanceCondition.randomChance(0.3f))
                        .add(LootItem.lootTableItem(HearthwindPrimitiveItems.ROCK)));
            }
        });

        HearthwindPrimitive.LOGGER.info("aged-primitive: loot hooks installed (stone->rock, gravel bonus)");
    }

    private HearthwindPrimitiveLoot() {
    }
}
