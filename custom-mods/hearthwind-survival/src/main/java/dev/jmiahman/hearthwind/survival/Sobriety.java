package dev.jmiahman.hearthwind.survival;

/**
 * Read by letsdo ports to gate alcohol content (brewery beers/whiskey,
 * vinery wines, and any other alcoholic consumables).
 *
 * <p>Source of truth is {@code config/hearthwind_survival.json}
 * ({@code sobriety.removeAlcohol}, default true). If hearthwind-survival is
 * absent at runtime (standalone letsdo install), this returns false so no
 * content is silently stripped.
 */
public final class Sobriety {
    private Sobriety() {
    }

    public static boolean alcoholRemoved() {
        try {
            HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
            return cfg.sobriety == null || cfg.sobriety.removeAlcohol;
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            return false;
        }
    }
}
