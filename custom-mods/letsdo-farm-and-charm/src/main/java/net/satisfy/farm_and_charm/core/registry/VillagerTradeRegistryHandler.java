package net.satisfy.farm_and_charm.core.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/**
 * 26.2 port: Architectury's TradeRegistry.registerVillagerTrade is gone from
 * architectury-fabric 21.0.7 and vanilla moved farmer trades to datapack JSON
 * (data/.../villager_trade/farmer/<level>/*.json + tags). The 7 original
 * trades (incl. 2 randomized-quantity grain buys) now live as JSON; the 2
 * randomized ones use fixed mid-range counts since the JSON schema has no RNG.
 * This class remains as the init hook / documentation anchor.
 */
public class VillagerTradeRegistryHandler {
    public static void init() {
        // Trades are data-driven now; validate the farmer profession is bound.
        BuiltInRegistries.VILLAGER_PROFESSION.getValue(VillagerProfession.FARMER);
    }
}
