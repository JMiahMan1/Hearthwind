package dev.jmiahman.hearthwind.client;

/**
 * Client-side copy of the five nutrient groups for HUD rendering.
 * Updated via hearthwind:diet payload from server.
 * Indices: 0=fruits, 1=vegetables, 2=grains, 3=proteins, 4=sugars.
 * Values are 0..100.
 */
public final class ClientDietData {
    private static float[] nutrients = new float[] {0f, 0f, 0f, 0f, 0f};

    private ClientDietData() {}

    public static void setNutrients(float[] vals) {
        for (int i = 0; i < 5 && i < vals.length; i++) {
            nutrients[i] = Math.max(0f, Math.min(100f, vals[i]));
        }
    }

    public static float getFruits() { return nutrients[0]; }
    public static float getVegetables() { return nutrients[1]; }
    public static float getGrains() { return nutrients[2]; }
    public static float getProteins() { return nutrients[3]; }
    public static float getSugars() { return nutrients[4]; }

    public static float get(int index) {
        if (index < 0 || index >= 5) return 0f;
        return nutrients[index];
    }

    public static boolean isBalanced() {
        for (float v : nutrients) {
            if (v < 50f) return false;
        }
        return true;
    }
}
