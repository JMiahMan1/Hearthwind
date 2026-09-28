package draylar.inmis.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import draylar.inmis.Inmis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Inmis configuration, matching the upstream field names and Aged's effective
 * defaults (Aged ships no inmis.json, so every value here is the default the
 * 1.20.1 mod generated).
 */
public final class InmisConfig {

    public List<BackpackInfo> backpacks = List.of();
    public boolean unstackablesOnly = false;
    public boolean disableShulkers = true;
    public List<String> blacklist = List.of();
    public boolean playSound = true;
    public boolean requireArmorTrinketToOpen = false;
    /**
     * Upstream refuses to let a player take a non-empty backpack off a
     * trinket slot. The Trinkets fork we ship exposes no unequip/canUnequip
     * hook ({@code TrinketAttachment} and {@code TrinketInventory} have none),
     * so there is no place to enforce it for worn backpacks and we do not
     * pretend otherwise. Aged 3.1.2 ships this false, so the shipped
     * behaviour is identical either way; the key is carried so a config
     * copied from Aged still round-trips.
     */
    public boolean requireEmptyForUnequip = false;
    public boolean allowBackpacksInChestplate = true;
    public boolean enableTrinketCompatibility = true;
    public boolean spillArmorBackpacksOnDeath = false;
    public boolean spillMainBackpacksOnDeath = false;
    public boolean trinketRendering = true;
    public String guiTitleColor = "0x404040";

    public static InmisConfig defaults() {
        InmisConfig config = new InmisConfig();
        config.backpacks = List.of(
                new BackpackInfo("baby", 3, 1, false, "minecraft:item.armor.equip_leather", false),
                new BackpackInfo("frayed", 9, 1, false, "minecraft:item.armor.equip_leather", true),
                new BackpackInfo("plated", 9, 2, false, "minecraft:item.armor.equip_iron", false),
                new BackpackInfo("gilded", 9, 3, false, "minecraft:item.armor.equip_gold", false),
                new BackpackInfo("bejeweled", 9, 5, false, "minecraft:item.armor.equip_diamond", false),
                new BackpackInfo("blazing", 9, 6, true, "minecraft:item.armor.equip_leather", false),
                new BackpackInfo("withered", 11, 6, false, "minecraft:item.armor.equip_leather", false),
                new BackpackInfo("endless", 15, 6, false, "minecraft:item.armor.equip_leather", false));
        return config;
    }

    public static InmisConfig load(Path configDir) {
        Path file = configDir.resolve("inmis.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                InmisConfig loaded = parse(reader);
                if (loaded != null && loaded.backpacks != null && !loaded.backpacks.isEmpty()) {
                    return loaded;
                }
            } catch (Exception exception) {
                Inmis.LOGGER.warn("Could not read inmis.json, using defaults", exception);
            }
        }

        InmisConfig config = defaults();
        try {
            Files.createDirectories(configDir);
            try (Writer writer = Files.newBufferedWriter(file)) {
                gson.toJson(config, writer);
            }
        } catch (IOException exception) {
            Inmis.LOGGER.warn("Could not write inmis.json", exception);
        }
        return config;
    }

    /**
     * Parses config text the way the shipped file is read.
     *
     * <p>Lenient on purpose: {@code config/inmis.json} is Aged's file
     * verbatim and keeps its {@code //} comment lines, which strict Gson
     * rejects - and a rejected file falls back to the defaults and then gets
     * overwritten, so the comments have to parse rather than be stripped.
     */
    public static InmisConfig parse(Reader reader) {
        try {
            return new GsonBuilder().setPrettyPrinting().setLenient().create()
                    .fromJson(reader, InmisConfig.class);
        } catch (Exception exception) {
            Inmis.LOGGER.warn("Could not parse inmis.json", exception);
            return null;
        }
    }

    /** Same as {@link #parse(Reader)} for in-memory text. */
    public static InmisConfig parse(String json) {
        return parse(new StringReader(json));
    }
}
