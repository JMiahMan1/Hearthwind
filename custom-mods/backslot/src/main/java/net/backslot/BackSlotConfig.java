package net.backslot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mirrors the upstream cloth-config layout so Aged's {@code backslot.json5}
 * values (slot offsets) carry over unchanged. The shipped
 * {@code conversion/overrides/config/backslot.json} holds Aged's values.
 */
public class BackSlotConfig {

    public int backSlotX = 0;
    public int backSlotY = 0;
    public int beltSlotX = 0;
    public int beltSlotY = 0;
    public int hudSlotX = 0;
    public int hudSlotY = 0;
    public boolean disableBackslotHud = false;
    public boolean changeSlotArrangement = false;
    public float backslotScaling = 1.0f;
    public float beltslotScaling = 1.0f;
    public boolean dropHolding = true;

    /**
     * BackSlot Addon parity (upstream backslotaddon 1.1.1): the shield gets
     * its own back transform, two swords can share the back, and a lantern
     * on the belt hangs behind the shoulder.
     */
    public boolean doubleBackSword = true;
    public boolean allowShieldOnBack = true;
    public boolean shieldClipping = true;
    public boolean allowLanternOnBelt = true;
    public boolean lanternOnBack = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static BackSlotConfig load(Path configDir) {
        Path file = configDir.resolve("backslot.json");
        BackSlotConfig config = new BackSlotConfig();
        if (Files.exists(file)) {
            try {
                JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                config = GSON.fromJson(json, BackSlotConfig.class);
                if (config == null) {
                    config = new BackSlotConfig();
                }
                return config;
            } catch (Exception e) {
                BackSlot.LOGGER.warn("Could not read {} - using defaults: {}", file, e.toString());
                return config;
            }
        }
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, GSON.toJson(new BackSlotConfig()));
        } catch (IOException e) {
            BackSlot.LOGGER.warn("Could not write {}: {}", file, e.toString());
        }
        return config;
    }
}
