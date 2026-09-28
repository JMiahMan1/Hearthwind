package com.teamremastered.tlc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teamremastered.tlc.platform.Services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * The Lost Castle configuration.
 *
 * <p>Upstream 2.1.1 keeps a flat {@code DISABLE_VANILLA_STRONGHOLD} flag in
 * {@code config/TheLostCastle-Fabric/tlc.json}.  Aged 3.1.2 shipped 1.0.1,
 * which used a nested {@code {"common": {"GENERATE_STRONGHOLD": false}}}
 * schema in {@code config/tlc.json} with the flag inverted.  Both are read
 * so a player upgrading from Aged keeps the stronghold behaviour they had,
 * and so the pack can ship the Aged file verbatim.
 *
 * <p>An existing file is never rewritten; the default file is only written
 * when neither path exists.
 */
public final class ConfigOptions {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_NAME = "tlc.json";
    /** 2.1.x location, relative to the loader config dir. */
    private static final String CONFIG_FOLDER = "TheLostCastle-Fabric/";
    /** 1.0.x (and therefore Aged) location, relative to the config dir. */
    private static final String LEGACY_CONFIG = "tlc.json";

    private static ConfigOptions config;

    private final boolean disableVanillaStronghold;

    private ConfigOptions(boolean disableVanillaStronghold) {
        this.disableVanillaStronghold = disableVanillaStronghold;
    }

    public static synchronized ConfigOptions get() {
        if (config == null) {
            config = load();
        }
        return config;
    }

    public boolean isDisableVanillaStronghold() {
        return this.disableVanillaStronghold;
    }

    /** Reads the config directory from the platform helper, tolerating a
     *  missing service (unit tests, odd launchers) by returning ".". */
    private static String configDir() {
        try {
            return Services.CONFIG_HELPER.configDirectoryPath();
        } catch (RuntimeException e) {
            return ".";
        }
    }

    private static File currentFile() {
        return new File(configDir() + File.separator + CONFIG_FOLDER + CONFIG_NAME);
    }

    private static File legacyFile() {
        return new File(configDir() + File.separator + LEGACY_CONFIG);
    }

    public static synchronized void create() throws IOException {
        if (config != null) {
            return;
        }
        File file = currentFile();
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(new JsonObject(), writer);
            }
        }
        config = load();
    }

    private static ConfigOptions load() {
        boolean disable = false;
        boolean found = false;

        File legacy = legacyFile();
        if (legacy.isFile()) {
            // Aged / 1.0.x: nested "common" block, flag inverted.
            JsonObject root = readObject(legacy);
            JsonObject common = child(root, "common");
            JsonElement flag = common == null ? null : common.get("GENERATE_STRONGHOLD");
            if (flag != null && flag.isJsonPrimitive()) {
                disable = !flag.getAsBoolean();
                found = true;
            }
        }

        File current = currentFile();
        if (current.isFile()) {
            // 2.1.x: flat flag, same meaning as the legacy inversion.
            JsonObject root = readObject(current);
            JsonElement flag = root == null ? null : root.get("DISABLE_VANILLA_STRONGHOLD");
            if (flag != null && flag.isJsonPrimitive()) {
                disable = flag.getAsBoolean();
                found = true;
            }
        }

        if (!found && !current.exists() && !legacy.exists()) {
            return new ConfigOptions(false);
        }
        return new ConfigOptions(disable);
    }

    private static JsonObject child(JsonObject parent, String name) {
        if (parent == null) {
            return null;
        }
        JsonElement element = parent.get(name);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonObject readObject(File file) {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonElement parsed = GSON.fromJson(reader, JsonElement.class);
            return parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        } catch (IOException | RuntimeException e) {
            com.teamremastered.tlc.Constants.LOG.warn(
                    "Could not read {}; keeping the current setting", file, e);
            return null;
        }
    }
}
