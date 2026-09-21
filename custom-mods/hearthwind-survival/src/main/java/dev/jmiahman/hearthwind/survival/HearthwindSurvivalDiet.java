package dev.jmiahman.hearthwind.survival;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Faithful port of NutritionZ 1.0.11 (the version Aged 3.1.2 ships).
 *
 * <p>Five integer nutrients in the original order - carbohydrates, protein,
 * fat, vitamins, minerals - each 0..{@code maxNutrition} (300 by default,
 * negative/positive thresholds 30/270, starting value maxNutrition/2).
 *
 * <p>Item values come from every {@code data/<ns>/nutrition/*.json} in the
 * server resource manager (the vanilla map plus the flattened per-mod
 * compat packs). The original {@code replace:true} lock semantics are kept:
 * once an item is claimed by a replacing entry, later files may not
 * override it.
 *
 * <p>Decay matches the original injection point: one point from ALL five
 * nutrients each time the player loses a hunger point to exhaustion
 * ({@link dev.jmiahman.hearthwind.survival.mixin.FoodDataTickMixin}).
 * Threshold effects run every 20 ticks and come from the
 * {@code nutrition_manager} datapack ({@link NutritionEffects}).
 */
public final class HearthwindSurvivalDiet {
    public static final int NUTRIENT_COUNT = 5;
    public static final String[] NUTRIENT_NAMES = {
            "carbohydrates", "protein", "fat", "vitamins", "minerals" };

    /**
     * Persistent 5-int state. The record codec is tried first; the legacy
     * {@code Map<String, Double>} shape (the pre-rewrite invented taxonomy)
     * is still decodable so old player data does not poison the whole
     * Fabric attachment map - it simply maps back to defaults.
     */
    public record NutritionState(int carbohydrates, int protein, int fat, int vitamins, int minerals) {
        public int get(int index) {
            return switch (index) {
                case 0 -> this.carbohydrates;
                case 1 -> this.protein;
                case 2 -> this.fat;
                case 3 -> this.vitamins;
                case 4 -> this.minerals;
                default -> 0;
            };
        }

        public NutritionState with(int index, int value) {
            return switch (index) {
                case 0 -> new NutritionState(value, this.protein, this.fat, this.vitamins, this.minerals);
                case 1 -> new NutritionState(this.carbohydrates, value, this.fat, this.vitamins, this.minerals);
                case 2 -> new NutritionState(this.carbohydrates, this.protein, value, this.vitamins, this.minerals);
                case 3 -> new NutritionState(this.carbohydrates, this.protein, this.fat, value, this.minerals);
                case 4 -> new NutritionState(this.carbohydrates, this.protein, this.fat, this.vitamins, value);
                default -> this;
            };
        }

        public NutritionState decremented() {
            return new NutritionState(
                    Math.max(0, this.carbohydrates - 1),
                    Math.max(0, this.protein - 1),
                    Math.max(0, this.fat - 1),
                    Math.max(0, this.vitamins - 1),
                    Math.max(0, this.minerals - 1));
        }
    }

    private static final Codec<NutritionState> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("carbohydrates").forGetter(NutritionState::carbohydrates),
            Codec.INT.fieldOf("protein").forGetter(NutritionState::protein),
            Codec.INT.fieldOf("fat").forGetter(NutritionState::fat),
            Codec.INT.fieldOf("vitamins").forGetter(NutritionState::vitamins),
            Codec.INT.fieldOf("minerals").forGetter(NutritionState::minerals))
            .apply(instance, NutritionState::new));

    public static final Codec<NutritionState> CODEC = Codec
            .either(RECORD_CODEC, Codec.unboundedMap(Codec.STRING, Codec.DOUBLE))
            .xmap(
                    either -> either.map(state -> state, legacy -> defaultState()),
                    state -> Either.<NutritionState, Map<String, Double>>left(state));

    public static final AttachmentType<NutritionState> NUTRITION =
            AttachmentRegistry.<NutritionState>builder()
                    .persistent(CODEC)
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("nutritionz", "nutrients"));

    /** Item -> five nutrient values. Replaced atomically on datapack reload. */
    private static final Map<Item, int[]> ITEM_NUTRITION = new HashMap<>();
    private static int unresolvedEntries = 0;
    private static int loadedFiles = 0;

    private HearthwindSurvivalDiet() {}

    public static NutritionState defaultState() {
        int start = Math.max(0, HearthwindSurvivalConfig.get().diet.maxNutrition / 2);
        return new NutritionState(start, start, start, start, start);
    }

    private static NutritionState state(Entity entity) {
        NutritionState attached = entity.getAttached(NUTRITION);
        return attached == null ? defaultState() : attached;
    }

    public static int getLevel(Entity entity, int index) {
        return state(entity).get(index);
    }

    public static int[] getNutrients(Entity entity) {
        NutritionState state = state(entity);
        int[] out = new int[NUTRIENT_COUNT];
        for (int i = 0; i < NUTRIENT_COUNT; i++) {
            out[i] = state.get(i);
        }
        return out;
    }

    public static void setLevel(Entity entity, int index, int value) {
        if (index < 0 || index >= NUTRIENT_COUNT) {
            return;
        }
        int max = HearthwindSurvivalConfig.get().diet.maxNutrition;
        entity.setAttached(NUTRITION,
                state(entity).with(index, Math.max(0, Math.min(max, value))));
    }

    public static void addLevel(Entity entity, int index, int amount) {
        if (amount == 0) {
            return;
        }
        setLevel(entity, index, getLevel(entity, index) + amount);
    }

    /** Food hook (Consumable#onConsume TAIL). Adds positive values only. */
    public static void onEaten(Entity entity, ItemStack stack) {
        applyItemNutrition(entity, stack);
    }

    /**
     * Drink hook for the thirst system (flask + bare-hand paths), mirroring
     * NutritionZ's Dehydration {@code DrinkEvent} receiver. Adds positive
     * values only.
     */
    public static void onDrink(Entity entity, ItemStack stack) {
        applyItemNutrition(entity, stack);
    }

    private static void applyItemNutrition(Entity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        int[] values = nutritionOf(stack.getItem());
        if (values == null) {
            return;
        }
        for (int i = 0; i < NUTRIENT_COUNT; i++) {
            if (values[i] > 0) {
                addLevel(entity, i, values[i]);
            }
        }
    }

    /**
     * One hunger point lost to exhaustion costs one point from all five
     * nutrients (NutritionZ injects the same decrement after the
     * {@code Math.max(foodLevel - 1, 0)} assignment in FoodData.tick).
     */
    public static void applyDecay(Entity entity) {
        NutritionState current = state(entity);
        NutritionState next = current.decremented();
        if (!next.equals(current)) {
            entity.setAttached(NUTRITION, next);
        }
    }

    /** Threshold effects run once per second (original: worldTime % 20). */
    public static void registerTickLoop() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                NutritionEffects.apply(player);
            }
        });
    }

    // ------------------------------------------------------------------
    // Data loading
    // ------------------------------------------------------------------

    public static synchronized void loadCorpus(ResourceManager manager) {
        Map<Item, int[]> loaded = new HashMap<>();
        Set<Item> locked = new HashSet<>();
        Map<Identifier, Resource> resources = manager.listResources("nutrition",
                id -> id.getPath().endsWith(".json"));
        List<Identifier> ids = new ArrayList<>(resources.keySet());
        Collections.sort(ids);
        int unresolved = 0;
        int files = 0;
        for (Identifier id : ids) {
            // "nutrition_manager/..." also prefix-matches the "nutrition"
            // string; effect files are loaded by NutritionEffects instead.
            if (!id.getPath().startsWith("nutrition/")) {
                continue;
            }
            files++;
            unresolved += loadItemFile(id, resources.get(id), loaded, locked);
        }
        ITEM_NUTRITION.clear();
        ITEM_NUTRITION.putAll(loaded);
        unresolvedEntries = unresolved;
        loadedFiles = files;
        HearthwindSurvival.LOGGER.info(
                "nutrition: {} items from {} data files ({} unresolvable ids skipped)",
                ITEM_NUTRITION.size(), loadedFiles, unresolvedEntries);
    }

    private static int loadItemFile(Identifier fileId, Resource resource,
            Map<Item, int[]> loaded, Set<Item> locked) {
        int unresolved = 0;
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            List<String> itemIds = new ArrayList<>(data.keySet());
            Collections.sort(itemIds);
            for (String itemId : itemIds) {
                JsonElement element = data.get(itemId);
                if (element == null || !element.isJsonObject()) {
                    unresolved++;
                    continue;
                }
                Identifier identifier = Identifier.tryParse(itemId);
                if (identifier == null) {
                    unresolved++;
                    continue;
                }
                Item item = BuiltInRegistries.ITEM.getOptional(identifier).orElse(null);
                if (item == null) {
                    // The compat packs were authored against a much larger
                    // mod set; skip content we do not ship.
                    unresolved++;
                    continue;
                }
                if (locked.contains(item)) {
                    continue;
                }
                JsonObject values = element.getAsJsonObject();
                if (values.has("replace") && values.get("replace").getAsBoolean()) {
                    locked.add(item);
                }
                int[] nutrition = new int[NUTRIENT_COUNT];
                for (int i = 0; i < NUTRIENT_COUNT; i++) {
                    JsonElement value = values.get(NUTRIENT_NAMES[i]);
                    if (value != null && value.isJsonPrimitive()) {
                        nutrition[i] = value.getAsInt();
                    }
                }
                loaded.put(item, nutrition);
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("nutrition: could not read {}: {}", fileId, e.toString());
        }
        return unresolved;
    }

    /** Five nutrient values for an item, or null when not catalogued. */
    public static synchronized int[] nutritionOf(Item item) {
        int[] values = ITEM_NUTRITION.get(item);
        return values == null ? null : values.clone();
    }

    public static synchronized int itemCount() {
        return ITEM_NUTRITION.size();
    }

    public static synchronized int unresolvedCount() {
        return unresolvedEntries;
    }

    public static synchronized List<NutritionItemMapPayload.Entry> itemSnapshot() {
        List<NutritionItemMapPayload.Entry> out = new ArrayList<>(ITEM_NUTRITION.size());
        for (Map.Entry<Item, int[]> entry : ITEM_NUTRITION.entrySet()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(entry.getKey());
            int[] v = entry.getValue();
            out.add(new NutritionItemMapPayload.Entry(id, v[0], v[1], v[2], v[3], v[4]));
        }
        return out;
    }

    public static List<String> summary() {
        return List.of("nutrition: " + itemCount() + " catalogued items across " + loadedFiles
                + " data files (" + unresolvedEntries + " ids from unshipped mods skipped)");
    }

    static void debugStatus(ServerPlayer player) {
        StringBuilder sb = new StringBuilder("Nutrition:");
        int[] values = getNutrients(player);
        for (int i = 0; i < NUTRIENT_COUNT; i++) {
            sb.append(' ').append(NUTRIENT_NAMES[i]).append('=').append(values[i]);
        }
        player.sendSystemMessage(Component.literal(sb.toString()));
    }
}
