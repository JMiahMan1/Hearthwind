package net.satisfy.vinery.platform;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.satisfy.vinery.core.Vinery;

import me.shedaniel.autoconfig.AutoConfig;
import net.satisfy.vinery.fabric.config.VineryFabricConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.List;
import java.util.function.Supplier;

public class PlatformHelper {

    public static int getTotalFermentationTime() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.totalFermentationTime;
    }

    public static int getMaxFluidLevel() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.maxFluidLevel;
    }

    public static int getMaxFluidIncrease() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.maxFluidIncrease;
    }

    public static int getApplePressMashingTime() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.applePressMashingTime;
    }

    public static int getApplePressFermentationTime() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.applePressFermentationTime;
    }

    public static double getCherryGrowthChance() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.cherryGrowthChance;
    }

    public static double getAppleGrowthChance() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.appleGrowthChance;
    }

    public static double getGrapeGrowthChance() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.blocks.grapeGrowthChance;
    }

    public static int getWineMaxLevel() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.wine.maxLevel;
    }

    public static int getWineStartDuration() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.wine.startDuration;
    }

    public static int getWineDurationPerYear() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.wine.durationPerYear;
    }

    public static int getWineDaysPerYear() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.wine.daysPerYear;
    }

    public static int getWineYearsPerEffectLevel() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.wine.yearsPerEffectLevel;
    }

    public static int getWineMaxDuration() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.wine.maxDuration;
    }

    public static boolean shouldGiveEffect() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.banner.giveEffect;
    }

    public static boolean shouldShowTooltip() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.items.banner.isShowTooltipEnabled();
    }

    public static double getTraderSpawnChance() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.trader.spawnChance;
    }

    public static boolean shouldSpawnWithMules() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.trader.spawnWithMules;
    }

    public static int getTraderSpawnDelay() {
        VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
        return config.trader.spawnDelay;
    }

    private static final dev.architectury.registry.registries.DeferredRegister<EntityType<?>> BOAT_TYPES = dev.architectury.registry.registries.DeferredRegister.create(Vinery.MOD_ID, net.minecraft.core.registries.Registries.ENTITY_TYPE);

    public static void initBoats() {
        BOAT_TYPES.register();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Supplier<EntityType<T>> registerBoatType(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange) {
        return (Supplier<EntityType<T>>) (Supplier<?>) BOAT_TYPES.register(name,
                () -> EntityType.Builder.of(factory, category).sized(width, height).clientTrackingRange(clientTrackingRange)
                        .build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Vinery.identifier(name))));
    }
}