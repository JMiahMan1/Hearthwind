package dev.jmiahman.hearthwind.primitive;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * Recipes removed from the game, read from
 * {@code data/earlystage/recipe_removals/*.json} (bundled, so the pack can
 * add its own file through the world datapack).
 *
 * <p>Two groups, because they are gated separately in
 * {@code config/hearthwind_primitive.json}:
 * <ul>
 *   <li><b>ores</b> - every vanilla ore smelting/blasting recipe plus the
 *       nugget and flint-and-steel shortcuts. Ores drop PIECES, and turning
 *       pieces into ingots goes through the slower blasting recipes in the
 *       corpus, so leaving the vanilla 200-tick smelt in place would bypass
 *       the whole economy.</li>
 *   <li><b>cooking</b> - furnace cooking of food. Off by default: the intent
 *       is that food is cooked on stoves, and those are not playable yet.</li>
 * </ul>
 */
public final class RecipeRemovals {

    private static final Set<String> ORES = new LinkedHashSet<>();
    private static final Set<String> COOKING = new LinkedHashSet<>();
    private static Object loadedFrom = null;

    private RecipeRemovals() {}

    public static synchronized void load(ResourceManager manager) {
        if (loadedFrom == manager) {
            return;
        }
        ORES.clear();
        COOKING.clear();
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                : manager.listResources("recipe_removals", id -> id.getPath().endsWith(".json")).entrySet()) {
            loadFile(entry.getValue());
        }
        loadedFrom = manager;
    }

    private static void loadFile(net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            readGroup(data, "ores", ORES);
            readGroup(data, "cooking", COOKING);
        } catch (Exception e) {
            HearthwindPrimitive.LOGGER.warn("recipe_removals: could not read a file: {}", e.toString());
        }
    }

    private static void readGroup(JsonObject data, String key, Set<String> target) {
        JsonElement array = data.get(key);
        if (array == null || !array.isJsonArray()) {
            return;
        }
        array.getAsJsonArray().forEach(element -> target.add(element.getAsString()));
    }

    /** True when the recipe id is on a currently active removal list. */
    public static boolean isRemoved(Identifier id) {
        if (id == null) {
            return false;
        }
        String key = id.toString();
        HearthwindPrimitiveConfig cfg = HearthwindPrimitiveConfig.get();
        if (cfg.removeOreSmeltingRecipes && ORES.contains(key)) {
            return true;
        }
        return cfg.removeCookedFoodRecipes && COOKING.contains(key);
    }

    public static int oreCount() {
        return ORES.size();
    }

    public static int cookingCount() {
        return COOKING.size();
    }

    /** Rebuilds a recipe map without the removed recipes. */
    public static RecipeMap filter(RecipeMap map) {
        if (ORES.isEmpty() && COOKING.isEmpty()) {
            return map;
        }
        List<RecipeHolder<?>> kept = new ArrayList<>();
        int removed = 0;
        for (RecipeHolder<?> holder : map.values()) {
            if (isRemoved(holder.id().identifier())) {
                removed++;
            } else {
                kept.add(holder);
            }
        }
        if (removed > 0) {
            HearthwindPrimitive.LOGGER.info("Recipes: removed {} of {}", removed, kept.size() + removed);
        }
        return RecipeMap.create(kept);
    }

    public static List<String> summary() {
        List<String> lines = new ArrayList<>();
        lines.add("recipe removals: " + oreCount() + " ore/tech"
                + (HearthwindPrimitiveConfig.get().removeCookedFoodRecipes
                        ? " + " + cookingCount() + " cooking" : " (cooking removal disabled)"));
        return lines;
    }
}
