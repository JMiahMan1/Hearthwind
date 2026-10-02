package dev.jmiahman.hearthwind.survival.hydration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

/**
 * Dehydration 1.3.6's four sound events, ported verbatim.
 *
 * <p>Before this class every one of these moments used a VANILLA substitute -
 * {@code BOTTLE_FILL} for filling a flask, {@code BOTTLE_EMPTY} for drinking
 * one, {@code GENERIC_DRINK} for the bare-hand sip and
 * {@code BUBBLE_COLUMN_BUBBLE_POP} for the boiling cauldron. The reference
 * ships its own {@code .ogg} files for all four; we now do too, at the same
 * ids, with the same {@code sounds.json} entries, so the pack sounds the way
 * Aged does.
 *
 * <p>The files are Dehydration's, copied byte for byte. Dehydration is
 * GPL-3.0 and this project already ships its textures the same way; see
 * {@code ATTRIBUTION.md}. That is also why the ids stay in the
 * {@code dehydration} namespace rather than being renamed to
 * {@code hearthwind}: they are the reference's assets and they are
 * attributed.
 *
 * <p>Each constant carries the exact volume, pitch and category the reference
 * bytecode uses, because the pitch matters more than it looks - the bare-hand
 * sip is {@code 1.0 + random * 5.0}, a five-fold spread.
 */
public final class DehydrationSounds {
    private DehydrationSounds() {
    }

    /**
     * {@code SoundInit}: four ids in the {@code dehydration} namespace. Only
     * the sip has a randomised pitch, everything else is 1.0.
     */
    public static SoundEvent FILL_FLASK;
    public static SoundEvent WATER_SIP;
    public static SoundEvent EMPTY_FLASK;
    public static SoundEvent CAULDRON_BUBBLE;

    /** Reference {@code SoundInit#init} registers all four at once. */
    public static void register() {
        FILL_FLASK = register("fill_flask");
        WATER_SIP = register("water_sip");
        EMPTY_FLASK = register("empty_flask");
        CAULDRON_BUBBLE = register("cauldron_bubble");
    }

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("dehydration", name);
        ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, key,
                SoundEvent.createVariableRangeEvent(id));
    }
}