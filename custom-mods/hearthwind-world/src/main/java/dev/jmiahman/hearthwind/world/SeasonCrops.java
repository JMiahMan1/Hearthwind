package dev.jmiahman.hearthwind.world;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;

/**
 * Per-crop seasonal growth multipliers, read from the world datapack at
 * {@code data/minecraft/seasons/crop/<block_id>.json}:
 *
 * <pre>{"spring": 0.5, "summer": 1.5, "fall": 1.0, "winter": 0.0}</pre>
 *
 * A multiplier of 0 means the crop does not grow at all in that season
 * (Aged's winter wheat, for example). Crops with no file fall back to the
 * per-season defaults in {@link HearthwindWorldConfig}.
 *
 * Loaded on server start, so the tuning lives in the datapack next to the
 * rest of the migrated corpus rather than in a second copy inside the mod.
 */
public final class SeasonCrops {
    private static final String ROOT = "seasons/crop";

    private static final Map<Block, float[]> MULTIPLIERS = new HashMap<>();
    private static ResourceManager loadedFrom = null;

    private SeasonCrops() {}

    /**
     * Idempotent: concurrent callers (gametests) must not wipe each other's
     * maps mid-read, so a reload for the same manager is a no-op.
     */
    public static synchronized void load(ResourceManager rm) {
        if (loadedFrom == rm) {
            return;
        }
        loadedFrom = rm;
        MULTIPLIERS.clear();

        Map<Identifier, Resource> found = rm.listResources(ROOT, id -> id.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : found.entrySet()) {
            Identifier file = entry.getKey();
            String path = file.getPath();
            String name = path.substring(ROOT.length() + 1, path.length() - ".json".length());
            Block block = BuiltInRegistries.BLOCK
                    .getOptional(Identifier.fromNamespaceAndPath(file.getNamespace(), name))
                    .orElse(null);
            if (block == null) {
                continue;
            }
            try (InputStream in = entry.getValue().open();
                    InputStreamReader reader = new InputStreamReader(in)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                float[] perSeason = new float[Season.values().length];
                for (Season season : Season.values()) {
                    perSeason[season.ordinal()] = season == Season.AUTUMN
                            ? read(json, "fall", 1.0f)
                            : read(json, season.name().toLowerCase(), 1.0f);
                }
                MULTIPLIERS.put(block, perSeason);
            } catch (Exception e) {
                HearthwindWorld.LOGGER.warn("Could not read season crop file {}: {}", file, e.getMessage());
            }
        }

        HearthwindWorld.LOGGER.info("Season crops: {} per-crop multipliers loaded from {}", MULTIPLIERS.size(), ROOT);
    }

    public static synchronized void clear() {
        loadedFrom = null;
        MULTIPLIERS.clear();
    }

    public static synchronized int count() {
        return MULTIPLIERS.size();
    }

    /**
     * Growth multiplier for one block in one season. Falls back to the
     * config's per-season defaults when the datapack has no file for it.
     */
    public static synchronized double multiplier(Block block, Season season) {
        float[] perSeason = MULTIPLIERS.get(block);
        if (perSeason != null) {
            return perSeason[season.ordinal()];
        }
        return season.cropMultiplier(HearthwindWorldConfig.get());
    }

    /** Convenience overload: resolves the season from the level's day. */
    public static double multiplier(Block block, net.minecraft.server.level.ServerLevel level) {
        return multiplier(block, Season.fromWorldTime(level.getGameTime(), HearthwindWorldConfig.get().daysPerSeason));
    }

    /**
     * Scales a random-tick bound so the growth probability is multiplied by
     * {@code multiplier}: halving growth doubles the bound, doubling growth
     * halves it. Returns 0 (never grow) when the multiplier is 0.
     */
    public static int scaleRandomTickBound(int bound, Block block, net.minecraft.server.level.ServerLevel level) {
        return scaleBound(bound, multiplier(block, level));
    }

    /** Pure scaling math, so gametests can assert it without a world clock. */
    public static int scaleBound(int bound, double multiplier) {
        if (multiplier <= 0.0) {
            return 0;
        }
        if (multiplier == 1.0) {
            return bound;
        }
        return Math.max(1, (int) Math.ceil(bound / multiplier));
    }

    private static float read(JsonObject json, String key, float fallback) {
        if (!json.has(key) || !json.get(key).isJsonPrimitive()) {
            return fallback;
        }
        return json.get(key).getAsFloat();
    }
}
