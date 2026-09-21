package net.satisfy.meadow.platform;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.fabric.core.config.MeadowFabricConfig;

import java.util.function.Supplier;

public class PlatformHelper {
    private static final dev.architectury.registry.registries.DeferredRegister<EntityType<?>> BOAT_TYPES =
            dev.architectury.registry.registries.DeferredRegister.create(Meadow.MOD_ID, net.minecraft.core.registries.Registries.ENTITY_TYPE);

    public static void initBoats() {
        BOAT_TYPES.register();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Supplier<EntityType<T>> registerBoatType(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange) {
        return (Supplier<EntityType<T>>) (Supplier<?>) BOAT_TYPES.register(name,
                () -> EntityType.Builder.of(factory, category).sized(width, height).clientTrackingRange(clientTrackingRange)
                        .build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Meadow.identifier(name))));
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static boolean shouldGiveEffect() {
        MeadowFabricConfig config = AutoConfig.getConfigHolder(MeadowFabricConfig.class).getConfig();
        return config.items.banner.giveEffect;
    }

    public static boolean shouldShowTooltip() {
        MeadowFabricConfig config = AutoConfig.getConfigHolder(MeadowFabricConfig.class).getConfig();
        return config.items.banner.isShowTooltipEnabled();
    }
}
