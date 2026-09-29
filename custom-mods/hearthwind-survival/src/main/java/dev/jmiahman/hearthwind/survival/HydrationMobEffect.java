package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Upstream Dehydration {@code HydrationEffect}: the effect carried by the
 * {@code dehydration:hydration} potion, which is the best thirst item in
 * Aged. Two facts from the bytecode drive the whole class:
 *
 * <ul>
 *   <li>{@code shouldApplyUpdateEffect(amp, remaining)} is
 *       {@code int k = 50 >> amp; return k <= 0 || remaining % k == 0} - a
 *       dose ticks once every {@code 50 >> amplifier} ticks instead of every
 *       tick.</li>
 *   <li>{@code applyUpdateEffect} grants {@code amp + 1} thirst, server side,
 *       players only.</li>
 * </ul>
 *
 * <p>Aged's {@code dehydration:hydration} potion carries this effect for 900
 * ticks, so a full dose fires about 18 times and is worth roughly
 * {@code +18} thirst on top of the {@code +2} the drink itself grants - the
 * only way to top a player up from near-empty without a campfire.
 */
public final class HydrationMobEffect extends MobEffect {
    public static final ResourceKey<MobEffect> KEY =
            ResourceKey.create(Registries.MOB_EFFECT,
                    Identifier.fromNamespaceAndPath("dehydration", "hydration_effect"));
    public static Holder<MobEffect> HOLDER;

    /** Aged's potion duration for {@code dehydration:hydration}. */
    public static final int POTION_DURATION_TICKS = 900;

    public HydrationMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x2EC6B6);
    }

    public static void register() {
        HOLDER = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, KEY, new HydrationMobEffect());
    }

    /** Once every {@code 50 >> amplifier} ticks; amplifier 5 and up tick always. */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int every = 50 >> amplifier;
        return every <= 0 || duration % every == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        HearthwindSurvivalThirst.addThirst(entity, amplifier + 1);
        return true;
    }
}
