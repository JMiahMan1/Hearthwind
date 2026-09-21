package dev.jmiahman.hearthwind.client;

import net.minecraft.util.Mth;

/**
 * Client-side copy of the EnvironmentZ temperature values for HUD rendering.
 * Updated via the hearthwind_survival:temp payload from the server.
 *
 * <p>Scale (EnvironmentZ 2.0.8 / Aged 3.1.2 defaults): body temperature is an
 * integer in [-2400, 2400] with bands -2400/-1800/-240/0/240/1800/2400;
 * wetness is 0..200; the thermometer reading is roughly +-6 with bands
 * -6/-3/3/6. The band constants are the data defaults - the server-side
 * manager tables are not synced separately.
 */
public final class ClientTempData {
    public static final int BODY_MAX_VERY_COLD = -2400;
    public static final int BODY_MAX_COLD = -1800;
    public static final int BODY_MIN_COLD = -240;
    public static final int BODY_NORMAL = 0;
    public static final int BODY_MIN_HOT = 240;
    public static final int BODY_MAX_HOT = 1800;
    public static final int BODY_MAX_VERY_HOT = 2400;

    public static final int WETNESS_MAX = 200;
    public static final int WETNESS_SOAKED = 180;

    public static final int THERMOMETER_VERY_COLD = -6;
    public static final int THERMOMETER_COLD = -3;
    public static final int THERMOMETER_HOT = 3;
    public static final int THERMOMETER_VERY_HOT = 6;

    private static int bodyTemperature = 0;
    private static int wetness = 0;
    private static int thermometer = 0;

    private ClientTempData() {}

    public static void set(int body, int wetness, int thermometer) {
        bodyTemperature = Mth.clamp(body, BODY_MAX_VERY_COLD, BODY_MAX_VERY_HOT);
        ClientTempData.wetness = Mth.clamp(wetness, 0, WETNESS_MAX);
        ClientTempData.thermometer = thermometer;
    }

    public static int getBodyTemperature() {
        return bodyTemperature;
    }

    public static int getWetness() {
        return wetness;
    }

    public static int getThermometer() {
        return thermometer;
    }

    public static boolean isFreezing() {
        return bodyTemperature <= BODY_MAX_COLD;
    }

    public static boolean isOverheating() {
        return bodyTemperature >= BODY_MAX_HOT;
    }
}
