package dev.jmiahman.hearthwind.client;

/**
 * Client-side copy of hydration for HUD rendering.
 * Updated via hearthwind:thirst payload from server.
 */
public final class ClientThirstData {
    private static float hydration = 20.0f;
    private static boolean buffered = false;

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

    /**
     * Whether the server's internal dehydration buffer is at or above 4.0. The
     * reference's droplet bob uses this to pick its cadence, so the HUD needs
     * it even though it is invisible on its own.
     */
    public static void setBuffered(boolean value) {
        buffered = value;
    }

    public static boolean isBuffered() {
        return buffered;
    }
}
