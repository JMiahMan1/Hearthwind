package dev.silverandro.lootbeams;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Field-for-field port of the upstream MicroConfig data class; the
 * defaults below come from Lootbeams 2.1.1's constructor bytecode.
 * Gson keeps these values for keys missing from an existing file, which
 * matches MicroConfig's fill-the-gaps behaviour.
 */
public final class LootbeamsConfig {
    public boolean showWhiteItems = false;
    public int particleCount = 1;
    public double beamHeight = 0.8D;
    public double beamOffset = 0.2D;
    public int minimumAge = 12;
    public double beamDistance = 64.0D;
    public boolean enchantedParticles = true;
    public boolean useBaseColor = false;
    public boolean printErrors = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static LootbeamsConfig load(Path configDir) {
        Path path = configDir.resolve("lootbeams.json");
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                LootbeamsConfig config = GSON.fromJson(reader, LootbeamsConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException | RuntimeException e) {
                Lootbeams.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }
        LootbeamsConfig config = new LootbeamsConfig();
        try {
            Files.createDirectories(configDir);
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            Lootbeams.LOGGER.warn("Could not write {}", path, e);
        }
        return config;
    }
}
