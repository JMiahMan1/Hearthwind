package net.satisfy.vinery.core.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.util.VillagerUtil;

import java.util.ArrayList;
import java.util.List;

public class WanderingWinemakerEntity extends WanderingTrader {
    // 26.2: code-built listings replaced the removed VillagerTrades API.
    // Same wares; the cherry-wine trade is skipped while sobriety is on.
    private static List<VillagerUtil.SellItemFactory> createTrades() {
        List<VillagerUtil.SellItemFactory> trades = new ArrayList<>();
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.RED_GRAPE_SEEDS.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.WHITE_GRAPE_SEEDS.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.TAIGA_RED_GRAPE_SEEDS.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.TAIGA_WHITE_GRAPE_SEEDS.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.SAVANNA_RED_GRAPE_SEEDS.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.SAVANNA_WHITE_GRAPE_SEEDS.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.JUNGLE_RED_GRAPE_SEEDS.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.JUNGLE_WHITE_GRAPE.get(), 1, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.DARK_CHERRY_SAPLING.get(), 3, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.APPLE_TREE_SAPLING.get(), 5, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.RED_GRAPE.get(), 2, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.RED_GRAPEJUICE.get(), 4, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.WHITE_GRAPEJUICE.get(), 4, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.RED_SAVANNA_GRAPEJUICE.get(), 4, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.WHITE_TAIGA_GRAPEJUICE.get(), 4, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.RED_JUNGLE_GRAPEJUICE.get(), 4, 1, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.COARSE_DIRT_SLAB.get(), 1, 3, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.GRASS_SLAB.get(), 1, 3, 8, 1));
        trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.DARK_CHERRY_PLANKS.get(), 3, 4, 8, 1));
        if (!dev.jmiahman.hearthwind.survival.Sobriety.alcoholRemoved()) {
            trades.add(new VillagerUtil.SellItemFactory(ObjectRegistry.CHERRY_WINE.get(), 1, 1, 8, 1));
        }
        return trades;
    }

    public WanderingWinemakerEntity(EntityType<? extends WanderingWinemakerEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void updateTrades(net.minecraft.server.level.ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        if (offers.isEmpty()) {
            for (VillagerUtil.SellItemFactory factory : createTrades()) {
                MerchantOffer offer = factory.getOffer(this, this.level().getRandom());
                if (offer != null) {
                    offers.add(offer);
                }
            }
        }
    }
}