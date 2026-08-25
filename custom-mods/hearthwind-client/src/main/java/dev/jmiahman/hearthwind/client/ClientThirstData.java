package dev.jmiahman.hearthwind.client;

/**
 * Client-side copy of hydration for HUD rendering.
 * Updated via hearthwind:thirst payload from server.
 */
public final class ClientThirstData {
    private static float hydration = 20.0f;

    private ClientThirstData() {}

    public static void setHydration(float h) {
        hydration = Math.max(0f, Math.min(20f, h));
    }

    public static float getHydration() {
        return hydration;
    }

    public static int level() {
        return Math.round(hydration);
    }
}
