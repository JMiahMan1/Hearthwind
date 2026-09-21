package net.satisfy.vinery.core.util;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item.Properties;

public class WineSettings {
    
    private final Properties properties;
    private final int baseDuration;

    public WineSettings(Holder<MobEffect> effect, int duration, int strength) {
        this.baseDuration = duration;
        this.properties = new Properties()
                .food(createWineFoodComponent(effect, duration, strength));
    }

    public Properties getProperties() {
        return properties;
    }

    public int getBaseDuration() {
        return baseDuration;
    }


    private FoodProperties createWineFoodComponent(Holder<MobEffect> effect, int duration, int strength) {
        // 26.2: food effects live on Consumable; drink effects apply via
        // DrinkBlockItem.finishUsingItem, so the base food stays plain.
        return new FoodProperties.Builder().alwaysEdible().build();
    }
}
