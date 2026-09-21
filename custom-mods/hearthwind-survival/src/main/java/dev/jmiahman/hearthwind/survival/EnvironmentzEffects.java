package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * EnvironmentZ 2.0.8 status effects, re-registered under the original
 * namespace so the migrated manager effect rows (environmentz:cooling -8
 * temperature / cold_protection 2, environmentz:warming +8 / heat_protection
 * 2, environmentz:comfort heat/cold protection 10) resolve. The effects
 * themselves are passive markers; the temperature driver in
 * {@link HearthwindSurvivalTemperature} reads the data rows.
 */
public final class EnvironmentzEffects {
    public static final ResourceKey<MobEffect> WARMING_KEY = key("warming");
    public static final ResourceKey<MobEffect> COOLING_KEY = key("cooling");
    public static final ResourceKey<MobEffect> COMFORT_KEY = key("comfort");

    public static Holder<MobEffect> WARMING;
    public static Holder<MobEffect> COOLING;
    public static Holder<MobEffect> COMFORT;

    private EnvironmentzEffects() {}

    private static ResourceKey<MobEffect> key(String path) {
        return ResourceKey.create(Registries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath("environmentz", path));
    }

    public static void register() {
        WARMING = register(WARMING_KEY, 16771455);
        COOLING = register(COOLING_KEY, 6541055);
        COMFORT = register(COMFORT_KEY, 0xE8732D);
    }

    private static Holder<MobEffect> register(ResourceKey<MobEffect> key, int color) {
        return net.minecraft.core.Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT, key,
                new MobEffect(MobEffectCategory.BENEFICIAL, color) {});
    }
}
