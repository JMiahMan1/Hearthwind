package dev.jmiahman.aged.survival;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Spoilage system replacing spoiledz (parity-first):
 * perishable items in player inventories slowly rot into rotten flesh.
 * Perishables are the <code>spoiledz:perishable_items</code> tag; anything in
 * the migrated <code>spoiledz:non_spoiling_items</code> tag (honey, teas,
 * wines, ...) is always exempt. Hot biomes accelerate the process.
 */
public final class AgedSurvivalSpoilage {
    public static final TagKey<Item> PERISHABLE =
            TagKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath("spoiledz", "perishable_items"));
    public static final TagKey<Item> NON_SPOILING =
            TagKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath("spoiledz", "non_spoiling_items"));

    private AgedSurvivalSpoilage() {}

    public static void registerTickLoop() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            AgedSurvivalConfig.Spoilage cfg = AgedSurvivalConfig.get().spoilage;
            if (cfg.chancePerCheck <= 0 || server.getTickCount() % cfg.checkIntervalTicks != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player, cfg);
            }
        });
    }

    private static void tick(ServerPlayer player, AgedSurvivalConfig.Spoilage cfg) {
        if (player.getAbilities().invulnerable || player.getAbilities().instabuild) {
            return;
        }
        double chance = cfg.chancePerCheck;
        float biomeTemp = player.level().getBiome(player.blockPosition())
                .value().getBaseTemperature();
        if (biomeTemp > 1.5f) {
            chance *= cfg.hotBiomeMultiplier;
        }

        var inv = player.getInventory();
        int slots = inv.getContainerSize();
        for (int i = 0; i < slots; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !s.is(PERISHABLE) || s.is(NON_SPOILING)) {
                continue;
            }
            if (player.getRandom().nextDouble() >= chance) {
                continue;
            }
            ItemStack rot = rotStack(cfg);
            s.shrink(1);
            if (!rot.isEmpty()) {
                if (!player.getInventory().add(rot)) {
                    player.drop(rot, false);
                }
            }
        }
    }

    private static ItemStack rotStack(AgedSurvivalConfig.Spoilage cfg) {
        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getValue(Identifier.parse(cfg.rotsInto));
        return new ItemStack(item);
    }
}
