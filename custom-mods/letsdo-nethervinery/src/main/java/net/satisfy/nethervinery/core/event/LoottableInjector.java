package net.satisfy.nethervinery.core.event;

import dev.architectury.event.events.common.LootEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.satisfy.nethervinery.core.NetherVinery;

public class LoottableInjector {
    // 26.2: architectury LootEvent.ModifyLootTable is now
    // modifyLootTable(ResourceKey<LootTable>, LootTableModificationContext, boolean).
    public static void InjectLoot(ResourceKey<LootTable> id, LootEvent.LootTableModificationContext context) {
        String prefix = "minecraft:chests/";
        String name = id.identifier().toString();

        if (name.startsWith(prefix)) {
            String file = name.substring(name.indexOf(prefix) + prefix.length());
            switch (file) {
                case "bastion_treasure", "ruined_portal", "bastion_other",
                        "bastion_hoglin_stable" ->
                        context.addPool(getPool(file));
                default -> {
                }
            }
        }
    }

    // 26.2: addPool takes LootPool.Builder; LootTableReference is now
    // NestedLootTable.lootTableReference(ResourceKey<LootTable>).
    public static LootPool.Builder getPool(String entryName) {
        return LootPool.lootPool().add(getPoolEntry(entryName));
    }

    private static LootPoolSingletonContainer.Builder<?> getPoolEntry(String name) {
        ResourceKey<LootTable> table = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(NetherVinery.MODID, "chests/" + name));
        return NestedLootTable.lootTableReference(table);
    }
}
