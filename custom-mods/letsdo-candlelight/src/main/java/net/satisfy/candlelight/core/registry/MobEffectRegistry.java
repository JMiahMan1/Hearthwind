package net.satisfy.candlelight.core.registry;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.effect.RefreshedEffect;
import net.satisfy.candlelight.core.effect.WellServedEffect;

import java.util.function.Supplier;

public class MobEffectRegistry {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Candlelight.MOD_ID, Registries.MOB_EFFECT);
    private static final Registrar<MobEffect> MOB_EFFECTS_REGISTRAR = MOB_EFFECTS.getRegistrar();

    public static final RegistrySupplier<MobEffect> REFRESHED;
    public static final RegistrySupplier<MobEffect> WELL_SERVED;

    private static RegistrySupplier<MobEffect> registerEffect(String name, Supplier<MobEffect> effect) {
        if (Platform.isNeoForge()) {
            return MOB_EFFECTS.register(name, effect);
        }
        return MOB_EFFECTS_REGISTRAR.register(Candlelight.identifier(name), effect);
    }

    public static void init() {
        MOB_EFFECTS.register();
    }

    public static Holder<MobEffect> holder(RegistrySupplier<MobEffect> supplier) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(supplier.get());
    }

    static {
        REFRESHED = registerEffect("refreshed", RefreshedEffect::new);
        WELL_SERVED = registerEffect("well_served", WellServedEffect::new);
    }
}