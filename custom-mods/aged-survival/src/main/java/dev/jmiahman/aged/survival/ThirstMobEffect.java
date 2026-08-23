package dev.jmiahman.aged.survival;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ThirstMobEffect extends MobEffect {
    public static final ResourceKey<MobEffect> KEY =
            ResourceKey.create(Registries.MOB_EFFECT,
                    Identifier.fromNamespaceAndPath("dehydration", "thirst"));
    public static Holder<MobEffect> HOLDER;

    public ThirstMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x3A62C4);
    }

    public static void register() {
        HOLDER = net.minecraft.core.Registry.registerForHolder(
                net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT,
                KEY, new ThirstMobEffect());
    }
}
