package net.adventurez.init;

import java.util.List;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;

public class LootInit {

    private static final List<ResourceKey<LootTable>> ADDED_LOOT_TABLE = List.of(BuiltInLootTables.PIGLIN_BARTERING,
            BuiltInLootTables.BASTION_BRIDGE, BuiltInLootTables.BASTION_HOGLIN_STABLE, BuiltInLootTables.BASTION_OTHER,
            BuiltInLootTables.BASTION_TREASURE);

    private static boolean addedLootTable(ResourceKey<LootTable> lootTable) {
        return ADDED_LOOT_TABLE.contains(lootTable);
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (addedLootTable(key)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(ItemInit.GILDED_BLACKSTONE_SHARD))
                        .setRolls(BinomialDistributionGenerator.binomial(1, 0.01F)));
                if (BuiltInLootTables.BASTION_TREASURE.equals(key)) {
                    tableBuilder.withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(ItemInit.GILDED_UPGRADE_SMITHING_TEMPLATE))
                            .setRolls(BinomialDistributionGenerator.binomial(1, 0.5F)));
                }
            } else if ("minecraft:entities/piglin_brute".equals(key.identifier().toString())) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(ItemInit.GILDED_BLACKSTONE_SHARD))
                        .setRolls(BinomialDistributionGenerator.binomial(1, 0.1F)));
            }
        });
    }
}
