package dev.jmiahman.hearthwind.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

public final class HearthwindWorldConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE = "hearthwind_world.json";

    public int daysPerSeason = 18;
    public double winterTempOffset = -3.0;
    public double summerTempOffset = 2.0;
    public double springTempOffset = 0.5;
    public double autumnTempOffset = 0.0;
    public double springCropMultiplier = 1.0;
    public double summerCropMultiplier = 1.2;
    public double autumnCropMultiplier = 0.9;
    public double winterCropMultiplier = 0.4;

    // Calendar rules (seasons config parity)
    public boolean animalsBreedInWinter = false;

    // HerdPanic parity tunables (herdspanic.json)
    public double herdPanicAlertRadius = 16.0;
    public double herdPanicSpeedMultiplier = 1.45;
    public boolean herdPanicShelterSeeking = true;

    // Couplings parity tunables (couplings.toml)
    public boolean coupleTrapdoors = false;

    private static HearthwindWorldConfig instance;
    public static HearthwindWorldConfig get() {
        if (instance == null) instance = load();
        return instance;
    }
    private static HearthwindWorldConfig load() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve(FILE);
        HearthwindWorldConfig cfg = new HearthwindWorldConfig();
        try {
            if (Files.exists(p)) {
                cfg = GSON.fromJson(Files.readString(p), HearthwindWorldConfig.class);
                if (cfg == null) cfg = new HearthwindWorldConfig();
            }
            Files.writeString(p, GSON.toJson(cfg));
        } catch (IOException e) {
            HearthwindWorld.LOGGER.warn("Could not read/write {}: {}", p, e);
        }
        return cfg;
    }
    public HearthwindWorldConfig() {}
}
