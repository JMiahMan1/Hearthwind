package dev.jmiahman.hearthwind.client;

/**
 * Client-side mirror of the five NutritionZ nutrients (0..maxNutrition),
 * updated from the server via the hearthwind_survival:diet payload.
 * Order: 0 carbohydrates, 1 protein, 2 fat, 3 vitamins, 4 minerals.
 */
public final class ClientDietData {
    private static int maxNutrition = 300;
    private static int[] levels = { 150, 150, 150, 150, 150 };

    private ClientDietData() {}

    public static void set(int[] values) {
        maxNutrition = dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig.get().diet.maxNutrition;
        for (int i = 0; i < levels.length && i < values.length; i++) {
            levels[i] = Math.max(0, Math.min(maxNutrition, values[i]));
        }
    }

    public static int get(int index) {
        if (index < 0 || index >= levels.length) {
            return 0;
        }
        return levels[index];
    }

    public static int max() {
        return maxNutrition;
    }

    public static boolean isLow(int index, int threshold) {
        return get(index) <= threshold;
    }

    public static boolean isHigh(int index, int threshold) {
        return get(index) >= threshold;
    }
}
