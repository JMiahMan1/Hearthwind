package dev.jmiahman.hearthwind.primitive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * All tunables for the primitive knapping minigame and beginner forgiveness
 * live in <code>config/hearthwind_primitive.json</code>.
 * Created with defaults on first boot; edit + restart to apply.
 */
public final class HearthwindPrimitiveConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "hearthwind_primitive.json";

    /** Number of rock hits needed to knap flint into tools. */
    public int craftRockCraftHits = 2;
    /** Max tracked progress value for the knapping UI. */
    public int craftRockMaxCraftHits = 80;
    /** New players can die this many times without losing items. */
    public int beginnerDeathCount = 3;
    /** Ticks between redstone sieve auto-sifts while powered. */
    public int redstoneSieveTicks = 30;
    /** Chance for each leaves loot table to gain an extra stick roll. */
    public float extraStickDropChance = 0.50f;
    /** Whether info tooltips are shown on blocks/items. */
    public boolean infoTooltips = true;
    /**
     * Remove the vanilla ore smelting/blasting recipes so the ore-piece
     * economy is the only route to ingots (ores drop pieces; pieces are
     * blasted, which is slower than vanilla smelting).
     */
    public boolean removeOreSmeltingRecipes = true;
    /**
     * Remove furnace cooking of food so food has to be cooked on a stove.
     * Off until the stove content is playable.
     */
    public boolean removeCookedFoodRecipes = false;

    private static HearthwindPrimitiveConfig instance;

    public static HearthwindPrimitiveConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static HearthwindPrimitiveConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        HearthwindPrimitiveConfig cfg = new HearthwindPrimitiveConfig();
        try {
            if (Files.exists(path)) {
                cfg = GSON.fromJson(Files.readString(path), HearthwindPrimitiveConfig.class);
                if (cfg == null) {
                    cfg = new HearthwindPrimitiveConfig();
                }
            }
            Files.writeString(path, GSON.toJson(cfg));
        } catch (IOException e) {
            HearthwindPrimitive.LOGGER.warn("Could not read/write {}: using defaults", path, e);
        }
        return cfg;
    }

    public HearthwindPrimitiveConfig() {}
}
