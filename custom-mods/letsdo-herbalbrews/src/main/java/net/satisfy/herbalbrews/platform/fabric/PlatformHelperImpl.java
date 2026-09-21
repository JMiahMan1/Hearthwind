package net.satisfy.herbalbrews.platform.fabric;

import net.satisfy.herbalbrews.platform.PlatformHelper;

public class PlatformHelperImpl extends PlatformHelper {
    public static boolean shouldGiveEffect() {
        return PlatformHelper.shouldGiveEffect();
    }

    public static boolean shouldShowTooltip() {
        return PlatformHelper.shouldShowTooltip();
    }

    public static int getDryingDuration() {
        return PlatformHelper.getDryingDuration();
    }

    public static int getBrewingDuration() {
        return PlatformHelper.getBrewingDuration();
    }

    public static boolean isHatDamageReductionEnabled() {
        return PlatformHelper.isHatDamageReductionEnabled();
    }

    public static int getHatDamageReductionAmount() {
        return PlatformHelper.getHatDamageReductionAmount();
    }

    public static int getJugEffectDuration() {
        return PlatformHelper.getJugEffectDuration();
    }
}
