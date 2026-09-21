package net.satisfy.vinery.core.registry;

import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.effect.*;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MobEffectRegistry {

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Vinery.MOD_ID,Registries.MOB_EFFECT);

    public static final Identifier ARMOR_EFFECT = Vinery.identifier("armor_effect");
    public static final Identifier HEALTH_EFFECT = Vinery.identifier("health_effect");
    public static final Identifier LUCK_EFFECT = Vinery.identifier("luck_effect");
    public static final Identifier RESISTANCE_EFFECT = Vinery.identifier("resistance_effect");
    public static final Identifier EXPERIENCE_EFFECT = Vinery.identifier("experience_effect");
    public static final Identifier IMPROVED_JUMP_BOOST = Vinery.identifier("double_jump");
    public static final Identifier PARTY_EFFECT = Vinery.identifier("party_effect");
    public static final Identifier TELEPORT = Vinery.identifier("teleport");
    public static final Identifier CREEPER_EFFECT = Vinery.identifier("creeper_effect");
    public static final Identifier CLIMBING_EFFECT = Vinery.identifier("climbing_effect");
    public static final Identifier FROSTY_ARMOR_EFFECT = Vinery.identifier("frosty_armor");
    public static final Identifier JELLIE = Vinery.identifier("jellie");
    public static final Identifier LAVA_WALKER = Vinery.identifier("lava_walker");
    public static final Identifier MAGNET = Vinery.identifier("magnet");
    public static final Identifier WATER_WALKER = Vinery.identifier("water_walker");

    public static void register() {
        EFFECTS.register();
        EFFECTS.register(ARMOR_EFFECT, () -> new ArmorEffect());
        EFFECTS.register(HEALTH_EFFECT, () -> new ImprovedHealthEffect());
        EFFECTS.register(LUCK_EFFECT, () -> new LuckEffect());
        EFFECTS.register(RESISTANCE_EFFECT, () -> new ResistanceEffect());
        EFFECTS.register(EXPERIENCE_EFFECT, () -> new ExpandableEffect(MobEffectCategory.BENEFICIAL, 0x00FF00));
        EFFECTS.register(IMPROVED_JUMP_BOOST, () -> new ExpandableEffect(MobEffectCategory.BENEFICIAL, 0x00FF00));
        EFFECTS.register(PARTY_EFFECT, () -> new ExpandableEffect(MobEffectCategory.BENEFICIAL, 0xFF0000));
        EFFECTS.register(TELEPORT, () -> new TeleportEffect());
        EFFECTS.register(CREEPER_EFFECT, () -> new CreeperEffect());
        EFFECTS.register(CLIMBING_EFFECT, () -> new ClimbingEffect());
        EFFECTS.register(FROSTY_ARMOR_EFFECT, () -> new FrostyArmorEffect());
        EFFECTS.register(JELLIE, () -> new JellieEffect());
        EFFECTS.register(LAVA_WALKER, () -> new LavaWalkerEffect());
        EFFECTS.register(MAGNET, () -> new MagnetEffect());
        EFFECTS.register(WATER_WALKER, () -> new WaterWalkerEffect());
    }

    public static Holder<MobEffect> getHolder(Identifier id) {
        Holder<MobEffect> holder = EFFECTS.getRegistrar().getHolder(id);
        if (holder == null) {
            throw new IllegalArgumentException("MobEffect with id " + id + " does not exist");
        }
        return holder;
    }
}
