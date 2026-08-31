package dev.jmiahman.hearthwind.skills;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Content gating rebuilt in-house (levelz parity, advancement-free).
 *
 * Reads the migrated levelz corpus natively, so the tuning lives in the
 * datapack and stays editable without a rebuild:
 *
 * - <code>levelz/mining/NN.json</code>  -> blocks that cannot be MINED;
 * - <code>levelz/block/*.json</code>    -> blocks that cannot be USED;
 * - <code>levelz/item/*.json</code>     -> items that cannot be USED;
 * - <code>levelz/crafting/*.json</code> -> items that cannot be CRAFTED;
 * - <code>levelz/smithing/NN.json</code>-> smithing-table inputs (SMITHING);
 * - <code>levelz/brewing/NN.json</code> -> brewing-stand inputs (ALCHEMY);
 * - <code>levelz/entity/*.json</code>   -> entities that cannot be interacted with.
 *
 * The corpus ships in <code>conversion/datapacks/aged-server</code>, which the
 * dev world installs; when it is absent we fall back to the bundled
 * <code>data/aged_skills/gates/*.json</code> digest so gates never silently
 * disappear. Ids that do not resolve in the running registries are skipped,
 * mirroring the datapack's {"required": false} convention. When one entry
 * appears under several skills the LOWEST requirement wins (most permissive).
 */
public final class SkillGates {
    public record Gate(Skill skill, int level) {}

    private static final Map<Identifier, Gate> BREAK_GATES = new HashMap<>();
    private static final Map<Identifier, Gate> USE_GATES = new HashMap<>();
    private static final Map<Identifier, Gate> ITEM_GATES = new HashMap<>();
    private static final Map<Identifier, Gate> CRAFT_GATES = new HashMap<>();
    private static final Map<Identifier, Gate> SMITHING_GATES = new HashMap<>();
    private static final Map<Identifier, Gate> BREWING_GATES = new HashMap<>();
    private static final Map<Identifier, Gate> ENTITY_GATES = new HashMap<>();
    private static boolean loaded = false;

    private SkillGates() {}

    private static void clear() {
        BREAK_GATES.clear();
        USE_GATES.clear();
        ITEM_GATES.clear();
        CRAFT_GATES.clear();
        SMITHING_GATES.clear();
        BREWING_GATES.clear();
        ENTITY_GATES.clear();
    }

    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        loadBundledFallback();
    }

    /** Guards against repeated reloads wiping maps other threads are reading. */
    private static ResourceManager loadedFrom;

    /**
     * Called from SERVER_STARTING (and by gametests): replaces the fallback
     * with live datapack data. Re-loading the same ResourceManager is a no-op,
     * so concurrent callers never observe a half-filled map.
     */
    public static synchronized void load(ResourceManager rm) {
        if (loadedFrom == rm) {
            return;
        }
        loadedFrom = rm;
        clear();
        int[] counts = loadLevelz(rm);
        int total = counts[0] + counts[1] + counts[2] + counts[3]
                + counts[4] + counts[5] + counts[6];
        HearthwindSkills.LOGGER.info(
                "Skill gates: {} mining, {} block-use, {} item-use, {} crafting,"
                + " {} smithing, {} brewing, {} entity ({} total)",
                counts[0], counts[1], counts[2], counts[3], counts[4],
                counts[5], counts[6], total);
        if (total == 0) {
            HearthwindSkills.LOGGER.warn(
                    "No data/levelz corpus found; keeping bundled gate digest");
            loadBundledFallback();
        }
        loaded = true;
    }

    private static int[] loadLevelz(ResourceManager rm) {
        int[] counts = new int[7];
        // mining/smithing/brewing files carry no "skill" field: the category
        // implies it, so those are the defaults that must be supplied here.
        counts[0] = loadCategory(rm, "mining", Skill.MINING, "block", BREAK_GATES);
        counts[1] = loadCategory(rm, "block", Skill.AGILITY, "block", USE_GATES);
        counts[2] = loadCategory(rm, "item", null, "item", ITEM_GATES);
        counts[3] = loadCategory(rm, "crafting", null, "item", CRAFT_GATES);
        counts[4] = loadCategory(rm, "smithing", Skill.SMITHING, "item",
                SMITHING_GATES);
        counts[5] = loadCategory(rm, "brewing", Skill.ALCHEMY, "item",
                BREWING_GATES);
        counts[6] = loadCategory(rm, "entity", null, "entity", ENTITY_GATES);
        return counts;
    }

    /**
     * Reads every <code>levelz/&lt;category&gt;/**.json</code>. Each file is
     * {@code {"replace", "skill", "level", "block"|"item"|"entity"}} where the
     * id field is either a single string or an array.
     *
     * Every file in the corpus merges. {@code "replace": true} overrides
     * levelz's own hard-coded defaults, which we do not ship, so there is
     * nothing for it to replace here; treating it as a wipe kept only whichever
     * file happened to be iterated last, and the entity corpus alone has eleven
     * {@code farming/0} files that all carry it. Files are visited in
     * identifier order so the result never depends on map iteration order.
     */
    private static int loadCategory(ResourceManager rm, String category,
            Skill defaultSkill, String idField, Map<Identifier, Gate> target) {
        int added = 0;
        try {
            var resources = rm.listResources(category,
                    path -> path.getPath().endsWith(".json"));
            // deterministic order: replace/merge results must not depend on
            // the resource manager's map iteration order
            var ordered = new java.util.ArrayList<>(resources.entrySet());
            ordered.sort(java.util.Map.Entry.comparingByKey());
            for (var entry : ordered) {
                if (!"levelz".equals(entry.getKey().getNamespace())) {
                    continue;
                }
                JsonObject root;
                try (InputStream is = entry.getValue().open()) {
                    root = JsonParser
                            .parseReader(new InputStreamReader(
                                    is, StandardCharsets.UTF_8))
                            .getAsJsonObject();
                } catch (Exception e) {
                    HearthwindSkills.LOGGER.warn("Bad levelz file {}: {}",
                            entry.getKey(), e.toString());
                    continue;
                }
                Skill skill = defaultSkill;
                if (root.has("skill")) {
                    try {
                        skill = Skill.byId(root.get("skill").getAsString());
                    } catch (IllegalArgumentException e) {
                        continue; // skill we do not implement
                    }
                }
                if (skill == null || !root.has("level") || !root.has(idField)) {
                    continue;
                }
                int level = root.get("level").getAsInt();
                for (String raw : readIds(root.get(idField))) {
                    Identifier id;
                    try {
                        id = Identifier.parse(raw);
                    } catch (Exception e) {
                        continue;
                    }
                    if (!resolves(idField, id)) {
                        continue; // not present in this world's registries
                    }
                    Gate existing = target.get(id);
                    // most permissive requirement wins on collisions
                    if (existing == null || level < existing.level()) {
                        target.put(id, new Gate(skill, level));
                        added++;
                    }
                }
            }
        } catch (Exception e) {
            HearthwindSkills.LOGGER.error("Failed to load levelz/{}: {}",
                    category, e.toString());
        }
        return added;
    }

    /** The id field is either a bare string or an array of strings. */
    private static List<String> readIds(JsonElement element) {
        List<String> out = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return out;
        }
        if (element.isJsonArray()) {
            for (JsonElement el : element.getAsJsonArray()) {
                if (el.isJsonPrimitive()) {
                    out.add(el.getAsString());
                }
            }
        } else if (element.isJsonPrimitive()) {
            out.add(element.getAsString());
        }
        return out;
    }

    /** Skip ids that no registry knows, like the datapack's optional entries. */
    private static boolean resolves(String idField, Identifier id) {
        return switch (idField) {
            case "block" -> BuiltInRegistries.BLOCK.getOptional(id).isPresent();
            case "item" -> BuiltInRegistries.ITEM.getOptional(id).isPresent();
            default -> BuiltInRegistries.ENTITY_TYPE.getOptional(id).isPresent();
        };
    }

    /**
     * Bundled digest of the same corpus, used until (or instead of) the
     * datapack. Keeps gates working in worlds without the pack installed.
     */
    private static void loadBundledFallback() {
        clear();
        try {
            Path dir = FabricLoader.getInstance()
                    .getModContainer("hearthwind_skills")
                    .flatMap(c -> c.findPath("data/aged_skills/gates"))
                    .orElseThrow();
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                        .forEach(SkillGates::loadBundledFile);
            }
            HearthwindSkills.LOGGER.info(
                    "Loaded bundled gate digest: {} break, {} use",
                    BREAK_GATES.size(), USE_GATES.size());
        } catch (Exception e) {
            HearthwindSkills.LOGGER.error("Failed to load skill gates", e);
        }
    }

    private static void loadBundledFile(Path path) {
        String skillId = path.getFileName().toString().replace(".json", "");
        Skill skill;
        try {
            skill = Skill.byId(skillId);
        } catch (IllegalArgumentException e) {
            HearthwindSkills.LOGGER.warn("Skipping unknown gate skill {}", skillId);
            return;
        }
        JsonObject root;
        try (InputStream in = Files.newInputStream(path)) {
            root = JsonParser.parseReader(new InputStreamReader(
                    in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            HearthwindSkills.LOGGER.warn("Bad gate file {}: {}", path, e.toString());
            return;
        }
        mergeBundled(root.getAsJsonArray("break"), skill, BREAK_GATES);
        mergeBundled(root.getAsJsonArray("use"), skill, USE_GATES);
    }

    private static void mergeBundled(JsonArray tiers, Skill skill,
            Map<Identifier, Gate> target) {
        if (tiers == null) {
            return;
        }
        for (JsonElement tierEl : tiers) {
            JsonObject tier = tierEl.getAsJsonObject();
            int level = tier.get("level").getAsInt();
            for (JsonElement idEl : tier.getAsJsonArray("blocks")) {
                Identifier id = Identifier.parse(idEl.getAsString());
                Gate existing = target.get(id);
                // most permissive requirement wins on collisions
                if (existing == null || level < existing.level()) {
                    target.put(id, new Gate(skill, level));
                }
            }
        }
    }

    /** Required skill level to mine {@code state}, or null if ungated. */
    public static synchronized Gate breakGate(BlockState state) {
        return BREAK_GATES.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    /** Required skill level to use {@code block}, or null if ungated. */
    public static synchronized Gate useGate(Block block) {
        return USE_GATES.get(BuiltInRegistries.BLOCK.getKey(block));
    }

    /** Required skill level to use {@code stack}, or null if ungated. */
    public static synchronized Gate itemGate(ItemStack stack) {
        return stack.isEmpty() ? null
                : ITEM_GATES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Required skill level to craft {@code stack}, or null if ungated. */
    public static synchronized Gate craftGate(ItemStack stack) {
        return stack.isEmpty() ? null
                : CRAFT_GATES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Required skill level to feed {@code stack} into a station input. */
    public static synchronized Gate smithingGate(ItemStack stack) {
        return stack.isEmpty() ? null
                : SMITHING_GATES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Required skill level to brew with {@code stack}. */
    public static synchronized Gate brewingGate(ItemStack stack) {
        return stack.isEmpty() ? null
                : BREWING_GATES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Required skill level to interact with {@code type}, or null if ungated. */
    public static synchronized Gate entityGate(EntityType<?> type) {
        return ENTITY_GATES.get(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    /**
     * Required skill level to interact with {@code entity}, or null. The
     * datapack lists breeders by plain mob type, so the gate rides on the type.
     */
    public static Gate entityGate(Entity entity) {
        return entity == null ? null : entityGate(entity.getType());
    }

    /** True when the player may perform the action (gate satisfied). */
    public static boolean allowed(ServerPlayer player, Gate gate) {
        return gate == null || !SkillsConfig.get().gates.enabled
                || SkillXp.level(player, gate.skill()) >= gate.level();
    }

    public static void register() {
        // Gates are SERVER-authoritative. Fabric fires these events on the
        // client thread too (singleplayer); touching server-only attachments
        // with a LocalPlayer crashed the client (NPE in SkillXp.xp), so bail
        // out before any gate check when the player is not a ServerPlayer.
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer sp)) {
                return true;
            }
            Gate gate = breakGate(state);
            if (!allowed(sp, gate)) {
                deny(sp, gate, "break");
                return false;
            }
            return true;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            Gate gate = useGate(world.getBlockState(hitResult.getBlockPos()).getBlock());
            if (gate != null && !allowed(sp, gate)) {
                deny(sp, gate, "use");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            ItemStack held = player.getItemInHand(hand);
            Gate gate = itemGate(held);
            if (gate != null && !allowed(sp, gate)) {
                deny(sp, gate, "use");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (!(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            Gate gate = entityGate(entity);
            if (gate != null && !allowed(sp, gate)) {
                deny(sp, gate, "interact with");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    private static void deny(ServerPlayer player, Gate gate, String verb) {
        if (player == null) {
            return;
        }
        player.sendOverlayMessage(Component.literal(
                "You need " + gate.skill().id + " level " + gate.level()
                        + " to " + verb + " this."));
    }

    /** For gametests: expose loaded counts without leaking internals. */
    static synchronized List<Map.Entry<Identifier, Gate>> debugEntries() {
        List<Map.Entry<Identifier, Gate>> all = new ArrayList<>(BREAK_GATES.entrySet());
        all.addAll(USE_GATES.entrySet());
        all.addAll(ITEM_GATES.entrySet());
        all.addAll(CRAFT_GATES.entrySet());
        all.addAll(SMITHING_GATES.entrySet());
        all.addAll(BREWING_GATES.entrySet());
        all.addAll(ENTITY_GATES.entrySet());
        return all;
    }

    static synchronized int[] debugCounts() {
        return new int[]{BREAK_GATES.size(), USE_GATES.size()};
    }

    /** Full per-category counts for gametests. */
    static synchronized int[] debugCategoryCounts() {
        return new int[]{BREAK_GATES.size(), USE_GATES.size(), ITEM_GATES.size(),
                CRAFT_GATES.size(), SMITHING_GATES.size(), BREWING_GATES.size(),
                ENTITY_GATES.size()};
    }
}
