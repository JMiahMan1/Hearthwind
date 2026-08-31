package dev.jmiahman.hearthwind.survival;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Data-driven hydration values from the migrated corpus
 * ({@code data/dehydration/hydration_items/*.json}).
 *
 * <p>Each file is an object keyed by the hydration tier ("1" .. "20", one
 * tier per hydration point on the 0..20 scale) whose value is
 * {@code {"replace": bool, "items": [item ids]}}. A tier carrying
 * {@code "replace": true} clears previously loaded entries for that tier
 * first, so a pack can override a tier wholesale; different files
 * otherwise merge.
 *
 * <p>Only items that exist in the current registry are catalogued - the
 * corpus was authored against a much larger mod set, so unresolvable ids
 * (for content we do not ship) are skipped rather than warned about on
 * every boot.
 *
 * <p>When the same item appears in several tiers the LOWEST tier wins,
 * matching the reference lookup which stops at the first match.
 */
public final class HydrationCorpus {

    private static final Map<Integer, Set<Item>> TIERS = new LinkedHashMap<>();
    private static final Map<Item, Integer> BY_ITEM = new LinkedHashMap<>();
    private static Object loadedFrom = null;
    private static int loadedItems = 0;

    private HydrationCorpus() {}

    public static synchronized void load(ResourceManager manager) {
        if (loadedFrom == manager) {
            return;
        }
        TIERS.clear();
        BY_ITEM.clear();
        loadedItems = 0;

        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                : manager.listResources("hydration_items", id -> id.getPath().endsWith(".json")).entrySet()) {
            loadFile(entry.getValue());
        }
        // Ascending tier order, so the lowest tier wins for duplicate items.
        List<Integer> tiers = new ArrayList<>(TIERS.keySet());
        tiers.sort(null);
        for (int tier : tiers) {
            for (Item item : TIERS.get(tier)) {
                BY_ITEM.putIfAbsent(item, tier);
            }
        }
        loadedItems = BY_ITEM.size();
        loadedFrom = manager;
    }

    private static void loadFile(net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : sortedEntries(data)) {
                int tier;
                try {
                    tier = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject obj = entry.getValue().getAsJsonObject();
                JsonElement items = obj.get("items");
                if (items == null || !items.isJsonArray()) {
                    continue;
                }
                if (obj.has("replace") && obj.get("replace").getAsBoolean()) {
                    TIERS.remove(tier);
                }
                Set<Item> bucket = TIERS.computeIfAbsent(tier, k -> new LinkedHashSet<>());
                items.getAsJsonArray().forEach(element -> {
                    Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(element.getAsString()))
                            .orElse(null);
                    if (item != null) {
                        bucket.add(item);
                    }
                });
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("hydration_items: could not read a file: {}", e.toString());
        }
    }

    /** JsonObject iteration order is unspecified; sort so results are deterministic. */
    private static List<Map.Entry<String, JsonElement>> sortedEntries(JsonObject obj) {
        List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(obj.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        return entries;
    }

    public static boolean hasCorpus() {
        return !BY_ITEM.isEmpty();
    }

    /** Number of catalogued items (registry-resolvable only). */
    public static int itemCount() {
        return loadedItems;
    }

    /** Number of tiers that hold at least one catalogued item. */
    public static int tierCount() {
        return TIERS.size();
    }

    /**
     * Hydration points (0..20 scale) granted by eating/drinking this stack,
     * or 0 when the item is not catalogued.
     */
    public static synchronized int quench(ItemStack stack) {
        if (stack.isEmpty() || !hasCorpus()) {
            return 0;
        }
        return BY_ITEM.getOrDefault(stack.getItem(), 0);
    }

    /**
     * Applies the catalogued hydration for a finished consumption.
     *
     * @return the hydration actually granted (0 when the item is not
     *         catalogued, the corpus is disabled or the player is full).
     */
    public static synchronized double hydrateOnConsume(ServerPlayer player, ItemStack stack) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        if (!cfg.thirst.useHydrationCorpus || !hasCorpus()) {
            return 0.0;
        }
        int tier = quench(stack);
        if (tier <= 0) {
            return 0.0;
        }
        double before = HearthwindSurvivalThirst.hydration(player);
        double after = Math.min(HearthwindSurvivalThirst.MAX_HYDRATION,
                before + tier * cfg.thirst.hydrationCorpusScale);
        if (after > before) {
            HearthwindSurvivalThirst.setHydration(player, after);
        }
        return after - before;
    }

    public static List<String> summary() {
        List<String> lines = new ArrayList<>();
        lines.add("hydration: " + loadedItems + " catalogued items across " + TIERS.size() + " tiers"
                + " (from dehydration/hydration_items)");
        return lines;
    }
}
