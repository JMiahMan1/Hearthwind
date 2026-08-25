package dev.jmiahman.hearthwind.jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class HearthwindJobsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "hearthwind_jobs.json";

    public int pointsPerLevel = 100;
    public int xpPerAction = 10;

    private static HearthwindJobsConfig instance;

    public static HearthwindJobsConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static HearthwindJobsConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        HearthwindJobsConfig cfg = new HearthwindJobsConfig();
        try {
            if (Files.exists(path)) {
                cfg = GSON.fromJson(Files.readString(path), HearthwindJobsConfig.class);
                if (cfg == null) cfg = new HearthwindJobsConfig();
            }
            Files.writeString(path, GSON.toJson(cfg));
        } catch (IOException e) {
            HearthwindJobs.LOGGER.warn("Could not read/write {}: using defaults", path, e);
        }
        return cfg;
    }

    public HearthwindJobsConfig() {}
}
