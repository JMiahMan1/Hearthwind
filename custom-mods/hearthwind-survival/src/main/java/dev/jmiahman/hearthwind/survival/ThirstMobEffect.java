package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Upstream Dehydration {@code ThirstEffect}: every active tick it charges
 * the thirst dehydration buffer by {@code thirst_effect_factor *
 * (amplifier + 1)} (Aged override 0.03), which is why dirty water is a
 * slow drain rather than instant damage.
 */
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

    /** Upstream applies the effect every tick. */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        HearthwindSurvivalThirst.addDehydration(entity,
                (float) (HearthwindSurvivalConfig.get().thirst.thirstEffectFactor * (amplifier + 1)));
        return true;
    }
}
