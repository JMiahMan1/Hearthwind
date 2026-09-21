package dev.jmiahman.hearthwind.survival;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Clean-room reimplementation of the EnvironmentZ 2.0.8 data model (Aged 3.1.2
 * ship version; upstream Globox1997/EnvironmentZ data loaders, GPL-3.0 studied,
 * values mirrored, files reauthored in this repo's data pack). Reads the same
 * {@code data/<ns>/manager}, {@code environment_blocks} and
 * {@code environment_items} schemas so the migrated corpus applies unchanged.
 *
 * <p>All numbers live in data: body/wetness/protection bands, biome
 * thresholds, thermometer bands, acclimatization tables, dimension
 * day/night/armor/soaked/wett/sweat/shadow/height rows, effect rows and the
 * block/fluid/item temperature tables.
 */
public final class EnvironmentCorpus {

    /** Climate bands: 0 very_cold, 1 cold, 2 normal, 3 hot, 4 very_hot. */
    public static final int BANDS = 5;
    public static final Identifier OVERWORLD = Identifier.fromNamespaceAndPath("minecraft", "overworld");
    /** Reference sentinel: dimension row absent -> use the global acclimatization logic. */
    public static final int NO_DIMENSION_ACCLIMATIZATION = 1997;

    /** Per-block entry: max contributing count, distance table and optional state gate. */
    public record BlockTemp(int maxCount, int[] byDistance, String property) {
        public int at(int distance) {
            return distance >= 0 && distance < byDistance.length ? byDistance[distance] : 0;
        }
    }

    /** Carried/equipped item: temperature plus optional wear and protection pools. */
    public record ItemTemp(int temperature, int damage, int heatProtection, int coldProtection) {}

    /**
     * One dimension's modifier rows, normalised to per-band arrays (basic
     * dimensions fill every band with their scalar). {@code height} holds
     * very_low/low/high/very_high values and {@code heightThresholds} their
     * very_low/low/high/very_high Y thresholds.
     */
    public record DimensionTable(boolean basic,
            int[] standard, int[] day, int[] night,
            int[] armor, int[] insulatedArmor, int[] icedArmor,
            int[] soaked, int[] wett, int[] shadow,
            int[] sweat, int[] height, int[] heightThresholds,
            int acclimatization) {

        private static int at(int[] row, int band) {
            return row != null && band >= 0 && band < row.length ? row[band] : 0;
        }

        public int standard(int band) { return at(standard, band); }
        public int day(int band) { return at(day, band); }
        public int night(int band) { return at(night, band); }
        public int armor(int band) { return at(armor, band); }
        public int insulatedArmor(int band) { return at(insulatedArmor, band); }
        public int icedArmor(int band) { return at(icedArmor, band); }
        public int soaked(int band) { return at(soaked, band); }
        public int wett(int band) { return at(wett, band); }
        public int shadow(int band) { return at(shadow, band); }
        /** environmentCode - 3: 0 hot, 1 very_hot. */
        public int sweat(int band) { return at(sweat, band); }

        public boolean hasHeight() {
            return height != null && height.length == 4
                    && heightThresholds != null && heightThresholds.length == 4;
        }

        /** Reference {@code playerHeight}: strict below/above threshold comparisons. */
        public int heightAt(int y) {
            if (!hasHeight()) {
                return 0;
            }
            if (y < heightThresholds[1]) {
                return y < heightThresholds[0] ? height[0] : height[1];
            }
            if (y > heightThresholds[2]) {
                return y > heightThresholds[3] ? height[3] : height[2];
            }
            return 0;
        }
    }

    // ---- tables (defaults = EnvironmentZ 2.0.8 / Aged 3.1.2 values) ----
    private static int[] bodyTemperatures = {-2400, -1800, -240, 0, 240, 1800, 2400};
    private static int[] wetnessBands = {200, 180, 100, 1, -1};
    private static int[] protectionBands = {600, 600, 600, 600};
    private static float[] biomeTemperatures = {0.2F, 0.4F, 1.2F, 1.6F};
    private static int[] thermometerBands = {-6, -3, 3, 6};
    private static int[] acclimatizationBands = {180, -10, 1600, -15, -180, 10, -1600, 15};

    private static final Map<Block, BlockTemp> BLOCKS = new HashMap<>();
    private static final Map<Fluid, BlockTemp> FLUIDS = new HashMap<>();
    private static final Map<Item, ItemTemp> ITEMS = new HashMap<>();
    private static final Map<Identifier, DimensionTable> DIMENSIONS = new HashMap<>();
    private static final Map<Identifier, int[]> EFFECTS = new HashMap<>();

    private static int loadedBlocks = 0;
    private static int loadedItems = 0;
    private static int loadedDimensions = 0;
    private static Object loadedFrom = null;

    private EnvironmentCorpus() {}

    // ---- accessors -----------------------------------------------------
    /** body_temperature: 0 max_very_cold .. 6 max_very_hot. */
    public static int bodyTemperature(int code) { return at(bodyTemperatures, code); }
    /** body_wetness: 0 max, 1 soaked, 2 water, 3 rain, 4 dry. */
    public static int wetness(int code) { return at(wetnessBands, code); }
    /** body_protection: 0 max_heat, 1 max_cold, 2 max_heat_resistance, 3 max_cold_resistance. */
    public static int protection(int code) { return at(protectionBands, code); }
    /** biome_temperature: 0 very_cold, 1 cold, 2 hot, 3 very_hot. */
    public static float biomeTemperature(int code) {
        return code >= 0 && code < biomeTemperatures.length ? biomeTemperatures[code] : 0F;
    }
    /** thermometer: 0 very_cold, 1 cold, 2 hot, 3 very_hot. */
    public static int thermometerTemperature(int code) { return at(thermometerBands, code); }
    /** acclimatization: 0 hot temp, 1 hot adj, 2 very_hot temp, 3 adj, 4 cold temp, 5 adj, 6 very_cold temp, 7 adj. */
    public static int acclimatization(int code) { return at(acclimatizationBands, code); }
    public static int[] bodyTemperatures() { return bodyTemperatures.clone(); }
    public static int[] thermometerBands() { return thermometerBands.clone(); }
    public static int[] acclimatizationBands() { return acclimatizationBands.clone(); }

    public static int blockCount() { return loadedBlocks; }
    public static int itemCount() { return loadedItems; }
    public static int dimensionCount() { return loadedDimensions; }

    public static BlockTemp tempFor(Block block) { return BLOCKS.get(block); }
    public static BlockTemp tempFor(Fluid fluid) { return FLUIDS.get(fluid); }
    public static ItemTemp item(Item item) { return ITEMS.get(item); }
    public static DimensionTable dimension(Identifier id) { return DIMENSIONS.get(id); }
    /** Effect row: 0 temperature, 1 heat_protection, 2 cold_protection. */
    public static int[] effect(Identifier id) { return EFFECTS.get(id); }

    /** Climate band for a biome from the loaded biome_temperature thresholds. */
    public static int band(Holder<Biome> biome) {
        return band(biome.value().getBaseTemperature());
    }

    public static int band(float temperature) {
        if (temperature < biomeTemperature(1)) {
            return temperature < biomeTemperature(0) ? 0 : 1;
        }
        if (temperature > biomeTemperature(2)) {
            return temperature > biomeTemperature(3) ? 4 : 3;
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

    /** Reference shouldUseOverworldTemperatures: unknown/flat dimensions fall back to overworld. */
    public static boolean shouldUseOverworldTemperatures(Identifier id) {
        DimensionTable table = DIMENSIONS.get(id);
        if (table == null) {
            return true;
        }
        return table.standard(0) == 0 && table.standard(4) == 0
                && table.day(0) == 0 && table.day(4) == 0;
    }

    /** Reference shouldUseStandardTemperatures: no day/night distinction. */
    public static boolean shouldUseStandardTemperatures(Identifier id) {
        DimensionTable table = DIMENSIONS.get(id);
        return table == null || (table.day(0) == 0 && table.day(4) == 0);
    }

    // ---- loading -------------------------------------------------------
    /** Loads every environmentz data file. Idempotent per resource manager. */
    public static synchronized void load(net.minecraft.server.packs.resources.ResourceManager manager) {
        if (loadedFrom == manager) {
            return;
        }
        BLOCKS.clear();
        FLUIDS.clear();
        ITEMS.clear();
        DIMENSIONS.clear();
        EFFECTS.clear();
        loadedBlocks = 0;
        loadedItems = 0;
        loadedDimensions = 0;

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
                Identifier id = Identifier.tryParse(entry.getKey());
                if (id == null) {
                    continue;
                }
                int[] values = new int[9];
                for (int i = 0; i < values.length; i++) {
                    values[i] = obj.has(String.valueOf(i)) ? obj.get(String.valueOf(i)).getAsInt() : 0;
                }
                String property = obj.has("property") ? obj.get("property").getAsString() : null;
                BlockTemp table = new BlockTemp(obj.get("max_count").getAsInt(), values, property);
                Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
                if (block != null) {
                    BLOCKS.put(block, table);
                    loadedBlocks++;
                    continue;
                }
                Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
                if (fluid != null) {
                    FLUIDS.put(fluid, table);
                    loadedBlocks++;
                }
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
                Item item = BuiltInRegistries.ITEM
                        .getOptional(Identifier.parse(entry.getKey())).orElse(null);
                if (item == null) {
                    continue;
                }
                ITEMS.put(item, new ItemTemp(
                        intOf(obj, "temperature", 0),
                        intOf(obj, "damage", 0),
                        intOf(obj, "heat_protection", 0),
                        intOf(obj, "cold_protection", 0)));
                loadedItems++;
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("[survival] environmentz item file failed: {}", e.toString());
        }
    }

    private static void loadManager(net.minecraft.server.packs.resources.Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

            if (data.has("body_temperature")) {
                JsonObject obj = data.getAsJsonObject("body_temperature");
                bodyTemperatures = new int[]{
                        intOf(obj, "max_very_cold", -2400), intOf(obj, "max_cold", -1800),
                        intOf(obj, "min_cold", -240), intOf(obj, "normal", 0),
                        intOf(obj, "min_hot", 240), intOf(obj, "max_hot", 1800),
                        intOf(obj, "max_very_hot", 2400)};
            }
            if (data.has("body_wetness")) {
                JsonObject obj = data.getAsJsonObject("body_wetness");
                wetnessBands = new int[]{
                        intOf(obj, "max_wetness", 200), intOf(obj, "soaked", 180),
                        intOf(obj, "water", 100), intOf(obj, "rain", 1), intOf(obj, "dry", -1)};
            }
            if (data.has("body_protection")) {
                JsonObject obj = data.getAsJsonObject("body_protection");
                protectionBands = new int[]{
                        intOf(obj, "max_heat", 600), intOf(obj, "max_cold", 600),
                        intOf(obj, "max_heat_resistance", 600), intOf(obj, "max_cold_resistance", 600)};
            }
            if (data.has("biome_temperature")) {
                JsonObject obj = data.getAsJsonObject("biome_temperature");
                biomeTemperatures = new float[]{
                        floatOf(obj, "very_cold", 0.2F), floatOf(obj, "cold", 0.4F),
                        floatOf(obj, "hot", 1.2F), floatOf(obj, "very_hot", 1.6F)};
            }
            if (data.has("thermometer_temperature")) {
                JsonObject obj = data.getAsJsonObject("thermometer_temperature");
                thermometerBands = new int[]{
                        intOf(obj, "very_cold", -6), intOf(obj, "cold", -3),
                        intOf(obj, "hot", 3), intOf(obj, "very_hot", 6)};
            }
            if (data.has("acclimatization")) {
                JsonObject obj = data.getAsJsonObject("acclimatization");
                acclimatizationBands = new int[]{
                        intOf(obj, "hot_body_temperature", 180), intOf(obj, "hot_body", -10),
                        intOf(obj, "very_hot_body_temperature", 1600), intOf(obj, "very_hot_body", -15),
                        intOf(obj, "cold_body_temperature", -180), intOf(obj, "cold_body", 10),
                        intOf(obj, "very_cold_body_temperature", -1600), intOf(obj, "very_cold_body", 15)};
            }
            if (data.has("effect")) {
                JsonObject effects = data.getAsJsonObject("effect");
                for (Map.Entry<String, JsonElement> entry : sortedEntries(effects)) {
                    if (!entry.getValue().isJsonObject()) {
                        continue;
                    }
                    Identifier id = Identifier.tryParse(entry.getKey());
                    if (id == null || BuiltInRegistries.MOB_EFFECT.getOptional(id).isEmpty()) {
                        continue;
                    }
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    EFFECTS.put(id, new int[]{
                            intOf(obj, "temperature", 0),
                            intOf(obj, "heat_protection", 0),
                            intOf(obj, "cold_protection", 0)});
                }
            }
            for (Map.Entry<String, JsonElement> entry : sortedEntries(data)) {
                if (!entry.getValue().isJsonObject() || !entry.getKey().contains(":")) {
                    continue;
                }
                Identifier id = Identifier.tryParse(entry.getKey());
                if (id == null) {
                    continue;
                }
                DIMENSIONS.put(id, readDimension(entry.getValue().getAsJsonObject()));
                loadedDimensions++;
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("[survival] environmentz manager file failed: {}", e.toString());
        }
    }

    private static DimensionTable readDimension(JsonObject obj) {
        boolean basic = obj.has("basic") && obj.get("basic").getAsBoolean();
        if (basic) {
            int standard = intOf(obj, "standard", 0);
            int armor = (int) floatOf(obj, "armor", 0F);
            int insulated = (int) floatOf(obj, "insulated_armor", 0F);
            int iced = (int) floatOf(obj, "iced_armor", 0F);
            int soaked = intOf(obj, "soaked", 0);
            int wett = intOf(obj, "wett", 0);
            int sweat = intOf(obj, "sweat", 0);
            int shadow = intOf(obj, "shadow", 0);
            int height = intOf(obj, "height", 0);
            return new DimensionTable(true,
                    fill(standard), fill(0), fill(0),
                    fill(armor), fill(insulated), fill(iced),
                    fill(soaked), fill(wett), fill(shadow),
                    new int[]{sweat, sweat}, fill4(height), fill4(height),
                    intOf(obj, "acclimatization", NO_DIMENSION_ACCLIMATIZATION));
        }
        return new DimensionTable(false,
                readBands(obj, "standard"), readBands(obj, "day"), readBands(obj, "night"),
                readBands(obj, "armor"), readBands(obj, "insulated_armor"), readBands(obj, "iced_armor"),
                readBands(obj, "soaked"), readBands(obj, "wett"), readBands(obj, "shadow"),
                new int[]{
                        readBand(obj, "sweat", "hot", 0),
                        readBand(obj, "sweat", "very_hot", 0)},
                new int[]{
                        readBand(obj, "height", "very_low", 0),
                        readBand(obj, "height", "low", 0),
                        readBand(obj, "height", "high", 0),
                        readBand(obj, "height", "very_high", 0)},
                new int[]{
                        readBand(obj, "height", "very_low_height", 0),
                        readBand(obj, "height", "low_height", 0),
                        readBand(obj, "height", "high_height", 0),
                        readBand(obj, "height", "very_high_height", 0)},
                intOf(obj, "acclimatization", NO_DIMENSION_ACCLIMATIZATION));
    }

    private static int[] readBands(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) {
            return new int[BANDS];
        }
        JsonObject row = parent.getAsJsonObject(key);
        int[] values = new int[BANDS];
        for (int i = 0; i < BANDS; i++) {
            values[i] = intOf(row, bandName(i), 0);
        }
        return values;
    }

    private static int readBand(JsonObject parent, String key, String band, int fallback) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) {
            return fallback;
        }
        return intOf(parent.getAsJsonObject(key), band, fallback);
    }

    private static int[] fill(int value) {
        int[] values = new int[BANDS];
        java.util.Arrays.fill(values, value);
        return values;
    }

    private static int[] fill4(int value) {
        int[] values = new int[4];
        java.util.Arrays.fill(values, value);
        return values;
    }

    private static int at(int[] row, int index) {
        return row != null && index >= 0 && index < row.length ? row[index] : 0;
    }

    private static int intOf(JsonObject obj, String key, int fallback) {
        return obj.has(key) ? obj.get(key).getAsInt() : fallback;
    }

    private static float floatOf(JsonObject obj, String key, float fallback) {
        return obj.has(key) ? obj.get(key).getAsFloat() : fallback;
    }

    // ---- block/fluid scan ---------------------------------------------
    /** Summed contribution of nearby heating/cooling blocks and fluids. */
    public static int blockHeat(Player player) {
        return blockHeat(player, HearthwindSurvivalConfig.get().temperature.heatBlockRadius);
    }

    public static int blockHeat(Player player, int heatBlockRadius) {
        if (BLOCKS.isEmpty() && FLUIDS.isEmpty()) {
            return 0;
        }
        Level level = player.level();
        BlockPos origin = player.blockPosition();
        Map<Block, Integer> blockCounts = new HashMap<>();
        Map<Fluid, Integer> fluidCounts = new HashMap<>();
        Vec3 eye = new Vec3(player.getX(), player.getY() + player.getBbHeight() / 2.0F, player.getZ());

        int radius = heatBlockRadius + 2;
        int half = radius / 2;
        int total = 0;
        for (int i = 0; i <= heatBlockRadius * 2; i++) {
            int dy = i > heatBlockRadius ? -(i - heatBlockRadius) : i;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    int distance = (int) Math.sqrt(origin.distSqr(pos));
                    BlockTemp block = BLOCKS.get(state.getBlock());
                    if (block != null) {
                        if (block.property() != null && !propertyTrue(state, block.property())) {
                            continue;
                        }
                        if (!visible(level, player, eye, pos)) {
                            continue;
                        }
                        int count = blockCounts.merge(state.getBlock(), 1, Integer::sum);
                        if (count > block.maxCount()) {
                            continue;
                        }
                        total += block.at(distance);
                    } else if (!state.getFluidState().isEmpty()) {
                        Fluid fluid = state.getFluidState().getType();
                        BlockTemp fluidTable = FLUIDS.get(fluid);
                        if (fluidTable == null) {
                            continue;
                        }
                        int count = fluidCounts.merge(fluid, 1, Integer::sum);
                        if (count > fluidTable.maxCount()) {
                            continue;
                        }
                        total += fluidTable.at(distance);
                    }
                }
            }
        }
        return total;
    }

    private static boolean propertyTrue(BlockState state, String property) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equals(property) && prop instanceof BooleanProperty booleanProperty) {
                return state.getValue(booleanProperty);
            }
        }
        return false;
    }

    private static boolean visible(Level level, Player player, Vec3 from, BlockPos pos) {
        BlockHitResult hit = level.clip(new ClipContext(from,
                new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }

    public static List<String> summary() {
        List<String> lines = new ArrayList<>();
        lines.add("environmentz corpus: " + loadedBlocks + " heat/cold blocks+fluids, "
                + loadedItems + " items, " + loadedDimensions + " dimension tables, "
                + EFFECTS.size() + " effect rows");
        return lines;
    }

    /** True once at least one environmentz file was read. */
    public static boolean hasCorpus() {
        return loadedBlocks > 0 || loadedDimensions > 0;
    }
}
