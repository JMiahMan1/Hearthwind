package dev.jmiahman.hearthwind.world.time;

/**
 * Mirrors the JSON layout of "Time & Wind Custom Ticker" so Aged's shipped
 * {@code config/time-and-wind/} files load byte-for-byte unchanged.
 */
public final class TimeAndWindConfig {
    public boolean patchSkyAngle = false;
    public boolean syncWithSystemTime = false;
    public boolean systemTimePerDimensions = false;
    public boolean enableNightSkipAcceleration = false;
    public int accelerationSpeed = 30;
    public int config_ver = 3;

    /** Per-dimension phase lengths, in world-clock ticks (vanilla = 12000). */
    public static final class TimeData {
        public int dayDuration = 12000;
        public int nightDuration = 12000;

        public TimeData() {
        }

        public TimeData(int dayDuration, int nightDuration) {
            this.dayDuration = dayDuration;
            this.nightDuration = nightDuration;
        }
    }
}
