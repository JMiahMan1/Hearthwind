package dev.jmiahman.hearthwind.jobs;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Job content ladders read from the migrated corpus
 * ({@code data/jobsaddon/<job>/*.json}) plus the restricted-recipe list
 * ({@code data/jobsaddon/restricted/*.json}).
 *
 * <p>Each job file is an object keyed by job level; the value carries
 * {@code blocks}, {@code items}, {@code entities} (and, for the brewer,
 * {@code effects}/{@code enchantments}) arrays of ids. An id therefore maps
 * to the job level at which that content is part of the trade - and, as in
 * the reference model, that level is also the size of the XP reward for
 * working it: breaking iron ore as a miner pays 7, diamond pays 20, while
 * anything outside the ladder pays the flat {@code xpPerAction} fallback.
 *
 * <p>The restricted list does NOT forbid crafting those recipes - it only
 * stops them paying crafting XP, so the piece-to-ingot conversions cannot be
 * cycled for infinite job XP.
 *
 * <p>Ids that are not in the current registry are skipped silently (the
 * corpus was authored against a much larger mod set).
 */
public final class JobCorpus {

    /** Job ids, also the corpus directory names. */
    public static final List<String> JOBS = List.of(
            "miner", "lumberjack", "farmer", "fisher",
            "warrior", "smither", "builder", "brewer");

    private static final Map<String, Map<String, Integer>> CONTENT = new HashMap<>();
    private static final Set<String> RESTRICTED_RECIPES = new LinkedHashSet<>();
    private static Object loadedFrom = null;

    private JobCorpus() {}

    public static synchronized void load(ResourceManager manager) {
        if (loadedFrom == manager) {
            return;
        }
        CONTENT.clear();
        RESTRICTED_RECIPES.clear();
        for (String job : JOBS) {
            Map<String, Integer> ladder = new HashMap<>();
            for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                    : manager.listResources(job, id -> id.getPath().endsWith(".json")).entrySet()) {
                // Only job ladders: the key set is numeric levels.
                loadLadder(entry.getValue(), ladder);
            }
            if (!ladder.isEmpty()) {
                CONTENT.put(job, ladder);
            }
        }
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                : manager.listResources("restricted", id -> id.getPath().endsWith(".json")).entrySet()) {
            loadRestricted(entry.getValue());
        }
        loadedFrom = manager;
    }

    private static void loadLadder(net.minecraft.server.packs.resources.Resource resource,
            Map<String, Integer> ladder) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : sortedEntries(data)) {
                int level;
                try {
                    level = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject obj = entry.getValue().getAsJsonObject();
                // "effects"/"enchantments" are the brewer's ladder (potions
                // brewed / enchantments applied); they are keyed by id string
                // like the rest, so they never collide with block/item ids.
                for (String key : List.of("blocks", "items", "entities", "crafting",
                        "effects", "enchantments")) {
                    JsonElement array = obj.get(key);
                    if (array == null || !array.isJsonArray()) {
                        continue;
                    }
                    array.getAsJsonArray().forEach(element -> {
                        String id = element.getAsString();
                        Integer existing = ladder.get(id);
                        if (existing == null || level < existing) {
                            ladder.put(id, level);
                        }
                    });
                }
            }
        } catch (Exception e) {
            HearthwindJobs.LOGGER.warn("jobsaddon: could not read a ladder file: {}", e.toString());
        }
    }

    private static void loadRestricted(net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            JsonElement recipes = data.get("recipes");
            if (recipes == null || !recipes.isJsonArray()) {
                return;
            }
            recipes.getAsJsonArray().forEach(element -> RESTRICTED_RECIPES.add(element.getAsString()));
        } catch (Exception e) {
            HearthwindJobs.LOGGER.warn("jobsaddon: could not read a restricted file: {}", e.toString());
        }
    }

    /** JsonObject iteration order is unspecified; sort so results are deterministic. */
    private static List<Map.Entry<String, JsonElement>> sortedEntries(JsonObject obj) {
        List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(obj.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        return entries;
    }

    public static boolean hasCorpus() {
        return !CONTENT.isEmpty();
    }

    /** Number of content ids catalogued for a job. */
    public static int contentCount(String job) {
        Map<String, Integer> ladder = CONTENT.get(job);
        return ladder == null ? 0 : ladder.size();
    }

    public static int jobCount() {
        return CONTENT.size();
    }

    /**
     * Job level a piece of content belongs to, or 0 when the job does not
     * track it (the flat {@code xpPerAction} fallback then applies).
     */
    public static synchronized int levelFor(String job, String id) {
        Map<String, Integer> ladder = CONTENT.get(job);
        return ladder == null ? 0 : ladder.getOrDefault(id, 0);
    }

    public static int restrictedCount() {
        return RESTRICTED_RECIPES.size();
    }

    /** True when crafting this recipe must not pay job XP. */
    public static boolean isRestrictedRecipe(Identifier recipeId) {
        return recipeId != null && RESTRICTED_RECIPES.contains(recipeId.toString());
    }

    public static List<String> summary() {
        List<String> lines = new ArrayList<>();
        StringBuilder counts = new StringBuilder();
        for (String job : JOBS) {
            if (CONTENT.containsKey(job)) {
                if (counts.length() > 0) {
                    counts.append(", ");
                }
                counts.append(job).append(' ').append(contentCount(job));
            }
        }
        lines.add("jobs: " + jobCount() + " ladders (" + counts + "), "
                + restrictedCount() + " recipes excluded from crafting XP");
        return lines;
    }
}
