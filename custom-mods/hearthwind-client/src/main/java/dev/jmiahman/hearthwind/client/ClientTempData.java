package dev.jmiahman.hearthwind.client;

/**
 * Client-side copy of body temperature for HUD rendering.
 * Updated via hearthwind:temp payload from server.
 * Scale: -10..+10, matching HearthwindSurvivalTemperature MIN/MAX.
 */
public final class ClientTempData {
    private static float temperature = 0f;
    // Trend tracking for the thermometer arrow: +1 warming, -1 cooling,
    // 0 stable. Arrow lingers briefly after the last change, like Aged.
    private static int trend = 0;
    private static long trendUntilMs = 0L;

    private ClientTempData() {}

    public static void setTemperature(float t) {
        float clamped = Math.max(-10f, Math.min(10f, t));
        if (clamped > temperature + 0.01f) {
            trend = 1;
            trendUntilMs = System.currentTimeMillis() + 3000L;
        } else if (clamped < temperature - 0.01f) {
            trend = -1;
            trendUntilMs = System.currentTimeMillis() + 3000L;
        }
        temperature = clamped;
    }

    public static float getTemperature() {
        return temperature;
    }

    /** +1 warming, -1 cooling, 0 no recent change. */
    public static int trendDirection() {
        return System.currentTimeMillis() > trendUntilMs ? 0 : trend;
    }

    public static boolean isFreezing() {
        return temperature <= -8f;
    }

    public static boolean isOverheating() {
        return temperature >= 9f;
    }
}
