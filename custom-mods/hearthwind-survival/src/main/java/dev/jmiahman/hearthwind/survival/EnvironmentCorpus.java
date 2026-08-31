package dev.jmiahman.hearthwind.survival;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Data-driven heat/cold sources from the migrated corpus
 * ({@code data/environmentz/environment_blocks}, {@code environment_items},
 * {@code manager}).
 *
 * <p>Block semantics match the reference model:
 * <ul>
 *   <li>scan volume is {@code heatBlockRadius} (default 3) vertically and
 *       {@code radius = heatBlockRadius + 2} spiralled horizontally, clamped to
 *       {@code +/- radius/2};</li>
 *   <li>a block only contributes if its optional boolean {@code property} is
 *       TRUE (a lit furnace warms you, a cold one does not);</li>
 *   <li>line of sight is required - a wall between player and source blocks it;</li>
 *   <li>at most {@code max_count} blocks of the same type contribute;</li>
 *   <li>the contribution falls off with distance
 *       ({@code floor(sqrt(squaredDistance))} indices the value table).</li>
 * </ul>
 * Enclosed rooms (no sky access within {@code enclosedRadius}) multiply the
 * summed positive heat by {@code roomHeatFactor} (0.5), so a fire indoors is
 * worth far more than one in the open - the reason shelter matters.
 */
public final class EnvironmentCorpus {

    /** Climate bands: 0 very_cold, 1 cold, 2 normal, 3 hot, 4 very_hot. */
    public static final int BANDS = 5;

    /** Per-block entry: distance -> contribution, plus optional state gate. */
    public record BlockTemp(int maxCount, int[] byDistance, String property) {
        public int at(int distance) {
            return distance >= 0 && distance < byDistance.length ? byDistance[distance] : 0;
        }
    }

    /** Dimension modifier table keyed by modifier name (day, night, armor, ...). */
    public record DimensionTable(Map<String, int[]> modifiers, int standard, boolean basic,
            int[] heightValues, int[] heightThresholds) {
        public int modifier(String name, int band) {
            int[] row = modifiers.get(name);
            if (row == null || band < 0 || band >= row.length) {
                return 0;
            }
            return row[band];
        }

        public int standard(int band) {
            return band >= 0 && band < modifiers.size() && modifiers.containsKey("standard")
                    ? modifiers.get("standard")[band] : standard;
        }

        /**
         * Height contribution. Thresholds are lower bounds in ascending order
         * (very_low, low, high, very_high); anything below the first threshold
         * uses the very_low value (deep underground is warm).
         */
        public int heightAt(int y) {
            for (int i = heightThresholds.length - 1; i >= 1; i--) {
                if (y >= heightThresholds[i]) {
                    return heightValues[i];
                }
            }
            return heightValues.length > 0 ? heightValues[0] : 0;
        }

        public boolean hasHeight() {
            return heightValues.length > 0 && heightThresholds.length == heightValues.length;
        }
    }

    private static final Map<Block, BlockTemp> BLOCKS = new HashMap<>();
    private static final Map<Item, Integer> ITEMS = new HashMap<>();
    private static final Map<Identifier, DimensionTable> DIMENSIONS = new HashMap<>();
    private static final Map<String, int[]> EFFECTS = new HashMap<>();

    /** Thermometer bands: very_cold, cold, hot, very_hot. */
    private static int[] thermometer = new int[]{-6, -2, 2, 6};
    /** Acclimatization: hot body temp / hot adj, very_hot / adj, cold / adj, very_cold / adj. */
    private static int[] acclimatization = new int[8];

    private static int loadedBlocks = 0;
    private static int loadedItems = 0;
    private static int loadedDimensions = 0;
    private static Object loadedFrom = null;

    private EnvironmentCorpus() {}

    public static int blockCount() {
        return loadedBlocks;
    }

    public static int itemCount() {
        return loadedItems;
    }

    public static int dimensionCount() {
        return loadedDimensions;
    }

    public static int[] thermometerBands() {
        return thermometer;
    }

    public static int[] acclimatization() {
        return acclimatization;
    }

    public static DimensionTable dimension(Identifier id) {
        return DIMENSIONS.get(id);
    }

    /** Loads every environmentz file in the world datapack. Idempotent per resource manager. */
    public static synchronized void load(net.minecraft.server.packs.resources.ResourceManager manager) {
        if (loadedFrom == manager) {
            return;
        }
        BLOCKS.clear();
        ITEMS.clear();
        DIMENSIONS.clear();
        EFFECTS.clear();
        loadedBlocks = 0;
        loadedItems = 0;
        loadedDimensions = 0;

        // "environment_blocks" -> every namespace, so mod-supplied files work too
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                : manager.listResources("environment_blocks", id -> id.getPath().endsWith(".json")).entrySet()) {
            loadBlocks(entry.getValue());
        }
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                : manager.listResources("environment_items", id -> id.getPath().endsWith(".json")).entrySet()) {
            loadItems(entry.getValue());
        }
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry
                : manager.listResources("manager", id -> id.getPath().endsWith(".json")).entrySet()) {
            loadManager(entry.getValue());
        }
        loadedFrom = manager;
    }

    /** JsonObject iteration order is unspecified; sort so results are deterministic. */
    private static List<Map.Entry<String, JsonElement>> sortedEntries(JsonObject obj) {
        List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(obj.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        return entries;
    }

    private static void loadBlocks(net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : sortedEntries(data)) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject obj = entry.getValue().getAsJsonObject();
                if (!obj.has("max_count")) {
                    continue;
                }
                Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(entry.getKey())).orElse(null);
                if (block == null) {
                    continue; // chipped/vinery entries we do not ship
                }
                int[] values = new int[BANDS];
                for (int i = 0; i < values.length; i++) {
                    values[i] = obj.has(String.valueOf(i)) ? obj.get(String.valueOf(i)).getAsInt() : 0;
                }
                String property = obj.has("property") ? obj.get("property").getAsString() : null;
                BLOCKS.put(block, new BlockTemp(obj.get("max_count").getAsInt(), values, property));
                loadedBlocks++;
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("[survival] environmentz block file failed: {}", e.toString());
        }
    }

    private static void loadItems(net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : sortedEntries(data)) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject obj = entry.getValue().getAsJsonObject();
                if (!obj.has("temperature")) {
                    continue;
                }
                Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(entry.getKey())).orElse(null);
                if (item == null) {
                    continue;
                }
                ITEMS.put(item, obj.get("temperature").getAsInt());
                loadedItems++;
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("[survival] environmentz item file failed: {}", e.toString());
        }
    }

    private static void loadManager(net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            if (data.has("thermometer_temperature")) {
                JsonObject obj = data.getAsJsonObject("thermometer_temperature");
                thermometer = new int[]{
                        intOf(obj, "very_cold", -6), intOf(obj, "cold", -2),
                        intOf(obj, "hot", 2), intOf(obj, "very_hot", 6)};
            }
            if (data.has("acclimatization")) {
                JsonObject obj = data.getAsJsonObject("acclimatization");
                acclimatization = new int[]{
                        intOf(obj, "hot_body_temperature", 0), intOf(obj, "hot_body", 0),
                        intOf(obj, "very_hot_body_temperature", 0), intOf(obj, "very_hot_body", 0),
                        intOf(obj, "cold_body_temperature", 0), intOf(obj, "cold_body", 0),
                        intOf(obj, "very_cold_body_temperature", 0), intOf(obj, "very_cold_body", 0)};
            }
            if (data.has("effect")) {
                JsonObject effects = data.getAsJsonObject("effect");
                for (Map.Entry<String, JsonElement> entry : sortedEntries(effects)) {
                    if (!entry.getValue().isJsonObject()) {
                        continue;
                    }
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    EFFECTS.put(entry.getKey(), new int[]{
                            intOf(obj, "temperature", 0),
                            intOf(obj, "heat_protection", 0),
                            intOf(obj, "cold_protection", 0)});
                }
            }
            for (Map.Entry<String, JsonElement> entry : sortedEntries(data)) {
                if (!entry.getValue().isJsonObject() || !entry.getKey().contains(":")) {
                    continue;
                }
                Identifier dim = Identifier.parse(entry.getKey());
                JsonObject obj = entry.getValue().getAsJsonObject();
                if (obj.has("basic") && obj.get("basic").getAsBoolean()) {
                    DIMENSIONS.put(dim, new DimensionTable(Map.of(), intOf(obj, "standard", 0), true,
                            new int[0], new int[0]));
                    loadedDimensions++;
                    continue;
                }
                Map<String, int[]> modifiers = new HashMap<>();
                int[] heightValues = new int[0];
                int[] heightThresholds = new int[0];
                for (Map.Entry<String, JsonElement> mod : obj.entrySet()) {
                    if (!mod.getValue().isJsonObject()) {
                        continue;
                    }
                    if (mod.getKey().equals("height")) {
                        JsonObject row = mod.getValue().getAsJsonObject();
                        String[] names = {"very_low", "low", "high", "very_high"};
                        heightValues = new int[names.length];
                        heightThresholds = new int[names.length];
                        for (int i = 0; i < names.length; i++) {
                            heightValues[i] = intOf(row, names[i], 0);
                            heightThresholds[i] = intOf(row, names[i] + "_height", 0);
                        }
                        continue;
                    }
                    JsonObject row = mod.getValue().getAsJsonObject();
                    int[] values = new int[BANDS];
                    boolean any = false;
                    for (int i = 0; i < BANDS; i++) {
                        String key = bandName(i);
                        if (row.has(key)) {
                            values[i] = row.get(key).getAsInt();
                            any = true;
                        }
                    }
                    if (any) {
                        modifiers.put(mod.getKey(), values);
                    }
                }
                DIMENSIONS.put(dim, new DimensionTable(modifiers, 0, false, heightValues, heightThresholds));
                loadedDimensions++;
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("[survival] environmentz manager file failed: {}", e.toString());
        }
    }

    private static int intOf(JsonObject obj, String key, int fallback) {
        return obj.has(key) ? obj.get(key).getAsInt() : fallback;
    }

    /** Climate band for a biome: 0 very_cold .. 4 very_hot. */
    public static int band(Holder<Biome> biome) {
        float temp = biome.value().getBaseTemperature();
        if (temp < 0.15f) {
            return 0;
        }
        if (temp < 0.5f) {
            return 1;
        }
        if (temp > 1.0f) {
            return 4;
        }
        if (temp > 0.8f) {
            return 3;
        }
        return 2;
    }

    public static String bandName(int band) {
        return switch (band) {
            case 0 -> "very_cold";
            case 1 -> "cold";
            case 2 -> "normal";
            case 3 -> "hot";
            default -> "very_hot";
        };
    }

    /** Summed contribution of nearby heating/cooling blocks. */
    public static int blockHeat(Player player) {
        return blockHeat(player, 3, 0.5f, 3);
    }

    public static int blockHeat(Player player, int heatBlockRadius, float roomHeatFactor, int enclosedRadius) {
        if (BLOCKS.isEmpty()) {
            return 0;
        }
        Level level = player.level();
        BlockPos origin = player.blockPosition();
        int total = 0;
        int heatSum = 0;
        Map<Block, Integer> counted = new HashMap<>();

        boolean enclosed = false;
        if (!level.canSeeSky(origin.above())) {
            int skyAccess = 0;
            List<BlockPos> checks = List.of(
                    origin.offset(enclosedRadius, 1, 0), origin.offset(-enclosedRadius, 1, 0),
                    origin.offset(0, 1, enclosedRadius), origin.offset(0, 1, -enclosedRadius));
            for (BlockPos pos : checks) {
                if (level.canSeeSky(pos)) {
                    skyAccess++;
                }
            }
            enclosed = skyAccess == 0;
        }

        Vec3 eye = new Vec3(player.getX(), player.getY() + player.getBbHeight() / 2.0f, player.getZ());
        int radius = heatBlockRadius + 2;
        int half = radius / 2;
        for (int i = 0; i <= heatBlockRadius * 2; i++) {
            int height = i > heatBlockRadius ? -(i - heatBlockRadius) : i;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    BlockPos pos = origin.offset(dx, height, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    BlockTemp temp = BLOCKS.get(state.getBlock());
                    if (temp == null) {
                        continue;
                    }
                    if (temp.property() != null && !propertyTrue(state, temp.property())) {
                        continue;
                    }
                    if (!visible(level, eye, pos)) {
                        continue;
                    }
                    int count = counted.merge(state.getBlock(), 1, Integer::sum);
                    if (count > temp.maxCount()) {
                        continue;
                    }
                    int distance = (int) Math.sqrt(origin.distSqr(pos));
                    int contribution = temp.at(distance);
                    total += contribution;
                    if (contribution > 0) {
                        heatSum += contribution;
                    }
                }
            }
        }
        if (enclosed && heatSum > 0) {
            total += (int) (heatSum * roomHeatFactor);
        }
        return total;
    }

    /** Summed contribution of carried heating/cooling items. */
    public static int itemHeat(Player player) {
        if (ITEMS.isEmpty()) {
            return 0;
        }
        int total = 0;
        net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                Integer temp = ITEMS.get(stack.getItem());
                if (temp != null) {
                    total += temp;
                }
            }
        }
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                Integer temp = ITEMS.get(stack.getItem());
                if (temp != null) {
                    total += temp;
                }
            }
        }
        return total;
    }

    /** Convenience for tests and the temperature tick. */
    public static int surroundings(Player player) {
        return blockHeat(player) + itemHeat(player);
    }

    private static boolean propertyTrue(BlockState state, String property) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equals(property) && prop instanceof BooleanProperty) {
                return state.getValue((BooleanProperty) prop);
            }
        }
        return false;
    }

    private static boolean visible(Level level, Vec3 from, BlockPos pos) {
        BlockHitResult hit = level.clip(new ClipContext(from,
                new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                net.minecraft.world.phys.shapes.CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }

    /** Acclimatization adjustment for a player sitting at {@code bodyTemperature}. */
    public static int acclimatizationFor(int band, int bodyTemperature) {
        if (acclimatization.length < 8) {
            return 0;
        }
        if (band == 1 && bodyTemperature < acclimatization[6]) {
            return acclimatization[7];
        }
        if (band == 2) {
            if (bodyTemperature < acclimatization[4]) {
                return acclimatization[5];
            }
            if (bodyTemperature > acclimatization[0]) {
                return acclimatization[1];
            }
        }
        if (band == 3 && bodyTemperature > acclimatization[2]) {
            return acclimatization[3];
        }
        return 0;
    }

    /** Effect-driven temperature/protection rows keyed by effect id. */
    public static int[] effect(String id) {
        return EFFECTS.get(id);
    }

    public static List<String> summary() {
        List<String> lines = new ArrayList<>();
        lines.add("environmentz: " + loadedBlocks + " heat/cold blocks, " + loadedItems + " items, "
                + loadedDimensions + " dimension tables");
        return lines;
    }

    /** True once at least one environmentz file was read. */
    public static boolean hasCorpus() {
        return loadedBlocks > 0 || loadedDimensions > 0;
    }

    /** Test/debug hook: the raw table for a block, if any. */
    public static BlockTemp tempFor(Block block) {
        return BLOCKS.get(block);
    }
}
