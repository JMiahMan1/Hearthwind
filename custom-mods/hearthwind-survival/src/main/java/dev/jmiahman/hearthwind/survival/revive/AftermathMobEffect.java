package dev.jmiahman.hearthwind.survival.revive;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Port of revive 1.0.7's {@code AftermathEffect} (the mod Aged 3.1.2 ships).
 *
 * <p>{@code ReviveServerPacket}'s revive handler applies it as
 *
 * <pre>{@code new StatusEffectInstance(AFTERMATH_EFFECT, CONFIG.effectAftermath,
 *         0, false, false, true)}</pre>
 *
 * (bytecode offsets 113-129 of the packet's {@code lambda$init$0}), so 600
 * ticks at amplifier 0, not ambient, no particles, icon shown.
 * {@code CONFIG.effectAftermath} defaults to 600 (ReviveConfig constructor
 * offset 80) and Aged's {@code revive.json5} does not set it, so 600 is the
 * effective value in both.
 *
 * <p>The effect's own behaviour in the reference is a passive marker - it
 * exists so other systems can ask "was this player just revived" - so this
 * port registers it and gives it the reference's category and colour without
 * inventing a per-tick gameplay effect that Aged does not have.
 */
public final class AftermathMobEffect extends MobEffect {
    public static final ResourceKey<MobEffect> KEY =
            ResourceKey.create(Registries.MOB_EFFECT,
                    Identifier.fromNamespaceAndPath("revive", "aftermath"));
    public static Holder<MobEffect> HOLDER;

    /** {@code ReviveConfig.effectAftermath}, which Aged leaves at its default. */
    public static final int DURATION_TICKS = 600;

    public AftermathMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9E5B8F);
    }

    public static void register() {
        HOLDER = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, KEY, new AftermathMobEffect());
    }
}