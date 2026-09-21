package net.satisfy.farm_and_charm.core.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;

// 26.2: adult cat/wolf/chicken vocal sounds moved from SoundEvents constants
// into per-variant sound sets (CAT_SOUND_VARIANT, WOLF_SOUND_VARIANT,
// CHICKEN_SOUND_VARIANT data components). Same sounds as before, resolved
// through each entity's own variant with automatic baby/adult selection.
public final class PetSounds {
    private PetSounds() {}

    private static net.minecraft.world.entity.animal.feline.CatSoundVariant.CatSoundSet catSet(Cat cat) {
        var variant = cat.get(DataComponents.CAT_SOUND_VARIANT).value();
        return cat.isBaby() ? variant.babySounds() : variant.adultSounds();
    }

    public static SoundEvent catEat(Cat cat) {
        return catSet(cat).eatSound().value();
    }

    public static SoundEvent catPurr(Cat cat) {
        return catSet(cat).purrSound().value();
    }

    public static SoundEvent catBeg(Cat cat) {
        return catSet(cat).begForFoodSound().value();
    }

    public static SoundEvent catHiss(Cat cat) {
        return catSet(cat).hissSound().value();
    }

    private static net.minecraft.world.entity.animal.wolf.WolfSoundVariant.WolfSoundSet wolfSet(Wolf wolf) {
        var variant = wolf.get(DataComponents.WOLF_SOUND_VARIANT).value();
        return wolf.isBaby() ? variant.babySounds() : variant.adultSounds();
    }

    public static SoundEvent wolfWhine(Wolf wolf) {
        return wolfSet(wolf).whineSound().value();
    }

    public static SoundEvent wolfPant(Wolf wolf) {
        return wolfSet(wolf).pantSound().value();
    }

    public static SoundEvent wolfGrowl(Wolf wolf) {
        return wolfSet(wolf).growlSound().value();
    }

    public static SoundEvent chickenAmbient(Chicken chicken) {
        var variant = chicken.get(DataComponents.CHICKEN_SOUND_VARIANT).value();
        return (chicken.isBaby() ? variant.babySounds() : variant.adultSounds()).ambientSound().value();
    }
}
