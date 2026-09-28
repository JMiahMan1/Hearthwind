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
    /**
     * The builder's earning list from {@code data/jobsaddon/tags/block/}:
     * block ids and tag names the reference pays for <em>placing</em>. Kept
     * as strings because the file mixes plain ids with tag references.
     */
    private static final Set<String> PLACEMENT_TAGS = new LinkedHashSet<>();
    /** The single corpus file that defines the builder's earning list. */
    private static final String PLACEMENT_TAG_FILE = "builder_placing_blocks.json";
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
        PLACEMENT_TAGS.clear();
        // Only the jobsaddon placement list is the builder's earnings list.
        // Other namespaces ship their own data/<ns>/tags/block files (aged
        // decor tags, earlystage rock-feature blocks) which are NOT placement
        // work and would silently widen what the builder earns.
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                : manager.listResources("tags/block", id -> id.getNamespace().equals("jobsaddon")
                        && id.getPath().equals("tags/block/" + PLACEMENT_TAG_FILE)).entrySet()) {
            loadPlacementTag(entry.getKey(), entry.getValue());
        }
        loadedFrom = manager;
    }

    /**
     * Reads {@code data/jobsaddon/tags/block/builder_placing_blocks.json}
     * into {@link #PLACEMENT_TAGS}. This tag is the builder's earning list in
     * the reference model and it is NOT part of any job ladder, so it has to
     * be loaded separately or the placement hook cannot know what Aged paid
     * for. Values may be plain ids or tag references, and
     * {@code {"id": ..., "required": false}} entries name mods we may not
     * ship - they are kept, because a tag that resolves to nothing never
     * matches a block state.
     */
    private static void loadPlacementTag(Identifier id,
            net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            JsonElement values = data.get("values");
            if (values == null || !values.isJsonArray()) {
                return;
            }
            for (JsonElement element : values.getAsJsonArray()) {
                String value;
                if (element.isJsonPrimitive()) {
                    value = element.getAsString();
                } else if (element.isJsonObject() && element.getAsJsonObject().has("id")) {
                    JsonObject entry = element.getAsJsonObject();
                    value = entry.get("id").getAsString();
                    // Optional entries name mods we may not ship; loading the
                    // tag anyway is harmless because a tag that resolves to
                    // nothing simply never matches a block state.
                } else {
                    continue;
                }
                PLACEMENT_TAGS.add(value);
            }
        } catch (Exception e) {
            HearthwindJobs.LOGGER.warn("jobsaddon: could not read placement tag {}: {}",
                    id, e.toString());
        }
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

    /** How many entries the builder placement tag contributed. */
    public static int placementTagCount() {
        return PLACEMENT_TAGS.size();
    }

    /**
     * True when placing {@code state} is builder work per the reference
     * placement tag. Values may be a plain block id
     * ({@code minecraft:obsidian}) or a tag ({@code #minecraft:planks}); a
     * tag that does not resolve in this install simply matches nothing.
     *
     * <p>An empty tag list means the corpus shipped no placement tag, and
     * then any block on the builder ladder still pays - the hook must not
     * stop working just because an optional tag file is missing.
     *
     * <p>Tag names resolve against the live block tags, so a tag from a mod
     * we do not ship simply never matches.
     */
    public static boolean isBuilderPlacement(net.minecraft.world.level.block.state.BlockState state) {
        String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(state.getBlock()).toString();
        if (PLACEMENT_TAGS.isEmpty()) {
            return levelFor("builder", id) > 0;
        }
        for (String value : PLACEMENT_TAGS) {
            if (value.charAt(0) == '#') {
                String tagName = value.substring(1);
                int colon = tagName.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag =
                        net.minecraft.tags.TagKey.create(
                                net.minecraft.core.registries.Registries.BLOCK,
                                net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                        tagName.substring(0, colon), tagName.substring(colon + 1)));
                if (state.is(tag)) {
                    return true;
                }
            } else if (value.equals(id)) {
                return true;
            }
        }
        return false;
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
