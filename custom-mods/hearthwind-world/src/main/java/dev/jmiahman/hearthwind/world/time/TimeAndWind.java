package dev.jmiahman.hearthwind.world.time;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.jmiahman.hearthwind.world.HearthwindWorld;
import dev.jmiahman.hearthwind.world.mixin.ServerLevelTimeAccessor;
import dev.jmiahman.hearthwind.world.time.TimeAndWindConfig.TimeData;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;

/**
 * Time &amp; Wind Custom Ticker parity (W1).
 *
 * <p>Aged 3.1.2 ships the (GPL-3.0) Time &amp; Wind Custom Ticker with
 * {@code config/time-and-wind/config.json} (night-skip acceleration) and
 * {@code time-data.json} (overworld day 24000, night 12000). There is no
 * upstream build past 1.21.1, so Hearthwind reimplements the observable
 * behaviour on top of 26.2's world clocks: a phase that is twice as long
 * as vanilla runs the clock at half speed ({@code 12000 / duration} ticks
 * per tick), and players sleeping through the night race the clock at the
 * configured acceleration instead of vanilla's instant jump to dawn.
 *
 * <p>Rates are pushed through vanilla's own {@code ClientboundSetTimePacket}
 * (via {@link ServerClockManager#setRate}), so the sun, moon and sky follow
 * on every client without any client-side code.
 */
public final class TimeAndWind {
    public static final String DIRECTORY = "time-and-wind";
    public static final int VANILLA_PHASE_TICKS = 12000;
    public static final long VANILLA_CYCLE_TICKS = 24000L;
    private static final Type TIME_DATA_TYPE = new TypeToken<HashMap<String, TimeData>>() {}.getType();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, TimeData> DIMENSIONS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Boolean> SKIPPING = new HashMap<>();
    private static final Map<ResourceKey<Level>, Float> APPLIED_RATE = new HashMap<>();

    private static boolean accelerationEnabled;
    private static int accelerationSpeed = 30;
    private static boolean loadedAccelerationEnabled;
    private static int loadedAccelerationSpeed = 30;
    private static Map<String, TimeData> testBackup;

    private TimeAndWind() {
    }

    /** Reads Aged's two files, generating the upstream defaults when absent. */
    public static void load(Path configDir) {
        Path dir = configDir.resolve(DIRECTORY);
        try {
            Files.createDirectories(dir);
            TimeAndWindConfig config = readOrCreateConfig(dir);
            if (config.config_ver < 3) {
                // Upstream patches v1 configs to acceleration-on, speed 30.
                config.systemTimePerDimensions = false;
                config.enableNightSkipAcceleration = true;
                config.accelerationSpeed = 30;
                config.config_ver = 3;
                writeJson(dir.resolve("config.json"), config);
            }
            if (config.syncWithSystemTime) {
                HearthwindWorld.LOGGER.warn(
                        "time-and-wind config asks for system-time sync; Hearthwind's port keeps the custom day lengths only");
            }
            DIMENSIONS.clear();
            DIMENSIONS.putAll(readOrCreateTimeData(dir));
            loadedAccelerationEnabled = config.enableNightSkipAcceleration;
            loadedAccelerationSpeed = config.accelerationSpeed;
            accelerationEnabled = loadedAccelerationEnabled;
            accelerationSpeed = loadedAccelerationSpeed;
            APPLIED_RATE.clear();
            SKIPPING.clear();
            HearthwindWorld.LOGGER.info(
                    "Time & Wind parity: acceleration {}x={}, dimensions {}", accelerationEnabled,
                    accelerationSpeed, DIMENSIONS);
        } catch (IOException e) {
            HearthwindWorld.LOGGER.warn("time-and-wind config unavailable, keeping vanilla day lengths", e);
        }
    }

    private static TimeAndWindConfig readOrCreateConfig(Path dir) throws IOException {
        Path path = dir.resolve("config.json");
        if (!Files.isRegularFile(path)) {
            TimeAndWindConfig config = new TimeAndWindConfig();
            writeJson(path, config);
            return config;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            TimeAndWindConfig config = GSON.fromJson(reader, TimeAndWindConfig.class);
            return config == null ? new TimeAndWindConfig() : config;
        }
    }

    private static Map<String, TimeData> readOrCreateTimeData(Path dir) throws IOException {
        Path path = dir.resolve("time-data.json");
        if (!Files.isRegularFile(path)) {
            Map<String, TimeData> defaults = new HashMap<>();
            defaults.put("minecraft:overworld", new TimeData());
            writeJson(path, defaults);
            return defaults;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Map<String, TimeData> data = GSON.fromJson(reader, TIME_DATA_TYPE);
            return data == null ? Map.of() : data;
        }
    }

    private static void writeJson(Path path, Object value) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(value, writer);
        }
    }

    public static boolean accelerationEnabled() {
        return accelerationEnabled;
    }

    /** True when this level's sleep should race the clock instead of jumping. */
    public static boolean blocksInstantSkip(ServerLevel level) {
        return accelerationEnabled && DIMENSIONS.containsKey(level.dimension().identifier().toString());
    }

    /** Phase length in world-clock ticks; vanilla for unconfigured dimensions. */
    public static long cycleTicks(String dimensionId) {
        TimeData data = DIMENSIONS.get(dimensionId);
        if (data == null || data.dayDuration <= 0 || data.nightDuration <= 0) {
            return VANILLA_CYCLE_TICKS;
        }
        return (long) data.dayDuration + data.nightDuration;
    }

    /** Same boundary Time & Wind uses: clock ticks 0..12000 are daylight. */
    public static boolean isDay(long totalTicks) {
        return Math.floorMod(totalTicks, 24000L) < 12001L;
    }

    /** Clock ticks added per server tick for a phase of {@code duration}. */
    public static float phaseRate(long totalTicks, TimeData data) {
        int duration = isDay(totalTicks) ? data.dayDuration : data.nightDuration;
        if (duration <= 0 || duration == VANILLA_PHASE_TICKS) {
            return 1.0F;
        }
        return (float) VANILLA_PHASE_TICKS / duration;
    }

    public static float rateFor(long totalTicks, TimeData data, boolean skipping, int speed) {
        return skipping ? speed : phaseRate(totalTicks, data);
    }

    /**
     * Applies the phase/acceleration rate to one level's daylight clock.
     * Called every server tick from {@link HearthwindWorld}; only talks to
     * the clock manager when the rate actually changes.
     */
    public static void tickLevel(ServerLevel level) {
        Holder<WorldClock> clock = level.dimensionType().defaultClock().orElse(null);
        if (clock == null) {
            return;
        }
        TimeData data = DIMENSIONS.get(level.dimension().identifier().toString());
        if (data == null) {
            return;
        }
        ServerClockManager clocks = level.getServer().clockManager();
        long ticks = clocks.getTotalTicks(clock);
        ResourceKey<Level> key = level.dimension();
        ServerLevelTimeAccessor accessor = (ServerLevelTimeAccessor) (Object) level;
        boolean skipping = Boolean.TRUE.equals(SKIPPING.get(key));

        if (skipping && isDay(ticks)) {
            skipping = false;
            SKIPPING.put(key, Boolean.FALSE);
            accessor.hearthwind$wakeUpAllPlayers();
            if (level.isRaining()) {
                level.resetWeatherCycle();
            }
        } else if (!skipping && accelerationEnabled) {
            int percentage = level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
            if (accessor.hearthwind$sleepStatus().areEnoughSleeping(percentage)) {
                skipping = true;
                SKIPPING.put(key, Boolean.TRUE);
            }
        }

        float rate = rateFor(ticks, data, skipping, accelerationSpeed);
        Float applied = APPLIED_RATE.get(key);
        if (applied == null || Math.abs(applied - rate) > 1.0E-4F) {
            clocks.setRate(clock, rate);
            APPLIED_RATE.put(key, rate);
        }
    }

    // ---- test seams (gametests run without Aged's config files) ----

    public static void setDimensionDataForTesting(String dimensionId, int dayDuration, int nightDuration) {
        if (testBackup == null) {
            testBackup = new HashMap<>(DIMENSIONS);
        }
        DIMENSIONS.put(dimensionId, new TimeData(dayDuration, nightDuration));
    }

    public static void setAccelerationForTesting(boolean enabled, int speed) {
        accelerationEnabled = enabled;
        accelerationSpeed = speed;
    }

    public static void resetForTesting() {
        if (testBackup != null) {
            DIMENSIONS.clear();
            DIMENSIONS.putAll(testBackup);
            testBackup = null;
        }
        accelerationEnabled = loadedAccelerationEnabled;
        accelerationSpeed = loadedAccelerationSpeed;
        APPLIED_RATE.clear();
        SKIPPING.clear();
    }
}
