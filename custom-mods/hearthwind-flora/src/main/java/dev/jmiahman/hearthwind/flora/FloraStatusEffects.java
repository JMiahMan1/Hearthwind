package dev.jmiahman.hearthwind.flora;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class FloraStatusEffects {
    // Vinery Effects
    public static Holder<MobEffect> JELLIE;
    public static Holder<MobEffect> MAGNET;
    public static Holder<MobEffect> IMPROVED_HEALTH;
    public static Holder<MobEffect> FROSTY_ARMOR;
    public static Holder<MobEffect> LAVA_WALKER;

    // Bakery Effects
    public static Holder<MobEffect> SUGAR_RUSH;
    public static Holder<MobEffect> VITALITY;

    // Brewery Effects
    public static Holder<MobEffect> INTOXICATION;
    public static Holder<MobEffect> STOUT_HEART;
    public static Holder<MobEffect> COMBUSTION;
    public static Holder<MobEffect> BLACKOUT;

    // Candlelight Effects
    public static Holder<MobEffect> WELL_SERVED;
    public static Holder<MobEffect> REFRESHED;

    // Farm and Charm Effects
    public static Holder<MobEffect> GRANDMAS_BLESSING;
    public static Holder<MobEffect> FARMERS_BLESSING;
    public static Holder<MobEffect> FEAST;
    public static Holder<MobEffect> SUSTENANCE;

    // HerbalBrews Effects
    public static Holder<MobEffect> BONDING;
    public static Holder<MobEffect> DEEPRUSH;
    public static Holder<MobEffect> LIFELEECH;
    public static Holder<MobEffect> TOUGH;

    public static void registerAll() {
        JELLIE = register("vinery", "jellie", MobEffectCategory.BENEFICIAL, 0xFFE082);
        MAGNET = register("vinery", "magnet", MobEffectCategory.BENEFICIAL, 0x81D4FA);
        IMPROVED_HEALTH = register("vinery", "improved_health", MobEffectCategory.BENEFICIAL, 0xE57373);
        FROSTY_ARMOR = register("vinery", "frosty_armor", MobEffectCategory.BENEFICIAL, 0xB2EBF2);
        LAVA_WALKER = register("vinery", "lava_walker", MobEffectCategory.BENEFICIAL, 0xFF8A65);

        SUGAR_RUSH = register("bakery", "sugar_rush", MobEffectCategory.BENEFICIAL, 0xF48FB1);
        VITALITY = register("bakery", "vitality", MobEffectCategory.BENEFICIAL, 0xA5D6A7);

        INTOXICATION = register("brewery", "intoxication", MobEffectCategory.HARMFUL, 0x90A4AE);
        STOUT_HEART = register("brewery", "stout_heart", MobEffectCategory.BENEFICIAL, 0xFFB74D);
        COMBUSTION = register("brewery", "combustion", MobEffectCategory.BENEFICIAL, 0xFF7043);
        BLACKOUT = register("brewery", "blackout", MobEffectCategory.HARMFUL, 0x37474F);

        WELL_SERVED = register("candlelight", "well_served", MobEffectCategory.BENEFICIAL, 0xFFD54F);
        REFRESHED = register("candlelight", "refreshed", MobEffectCategory.BENEFICIAL, 0x80CBC4);

        GRANDMAS_BLESSING = register("farm_and_charm", "grandmas_blessing", MobEffectCategory.BENEFICIAL, 0xFFCC80);
        FARMERS_BLESSING = register("farm_and_charm", "farmers_blessing", MobEffectCategory.BENEFICIAL, 0xC5E1A5);
        FEAST = register("farm_and_charm", "feast", MobEffectCategory.BENEFICIAL, 0xFFAB91);
        SUSTENANCE = register("farm_and_charm", "sustenance", MobEffectCategory.BENEFICIAL, 0xCE93D8);

        BONDING = register("herbalbrews", "bonding", MobEffectCategory.BENEFICIAL, 0x80DEEA);
        DEEPRUSH = register("herbalbrews", "deeprush", MobEffectCategory.BENEFICIAL, 0x4DB6AC);
        LIFELEECH = register("herbalbrews", "lifeleech", MobEffectCategory.BENEFICIAL, 0xEF5350);
        TOUGH = register("herbalbrews", "tough", MobEffectCategory.BENEFICIAL, 0x8D6E63);
    }

    private static Holder<MobEffect> register(String modId, String name, MobEffectCategory category, int color) {
        ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, Identifier.fromNamespaceAndPath(modId, name));
        MobEffect effect = new CustomFloraEffect(category, color);
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, key, effect);
    }

    public static class CustomFloraEffect extends MobEffect {
        public CustomFloraEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
