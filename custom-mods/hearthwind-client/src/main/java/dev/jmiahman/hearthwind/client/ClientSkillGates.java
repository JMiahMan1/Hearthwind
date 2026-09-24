package dev.jmiahman.hearthwind.client;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import dev.jmiahman.hearthwind.skills.SkillGatesSyncPayload;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Client-side skill gate registry for in-world block tooltips and inventory item tooltips.
 */
@Environment(EnvType.CLIENT)
public final class ClientSkillGates {
    public record Requirement(String skill, int level) {}

    private record Snapshot(
            Map<Identifier, Requirement> breakGates,
            Map<Identifier, Requirement> useGates,
            Map<Identifier, Requirement> itemGates,
            Map<Identifier, Requirement> craftGates,
            Map<Identifier, Requirement> smithingGates,
            Map<Identifier, Requirement> brewingGates,
            Map<Identifier, Requirement> entityGates) {}

    private static final Map<Identifier, Requirement> EMPTY = Map.of();
    private static volatile Map<Identifier, Requirement> BREAK_GATES = new HashMap<>();
    private static volatile Map<Identifier, Requirement> USE_GATES = new HashMap<>();
    private static volatile Map<Identifier, Requirement> ITEM_GATES = new HashMap<>();
    private static volatile Map<Identifier, Requirement> CRAFT_GATES = new HashMap<>();
    private static volatile Map<Identifier, Requirement> SMITHING_GATES = new HashMap<>();
    private static volatile Map<Identifier, Requirement> BREWING_GATES = new HashMap<>();
    private static volatile Map<Identifier, Requirement> ENTITY_GATES = new HashMap<>();
    private static volatile Snapshot serverSnapshot;
    private static volatile boolean loaded = false;
    private static volatile boolean serverSnapshotReceived = false;

    private ClientSkillGates() {}

    public static void replaceFrom(SkillGatesSyncPayload payload) {
        Map<Identifier, Requirement> breakGates = new HashMap<>();
        Map<Identifier, Requirement> useGates = new HashMap<>();
        Map<Identifier, Requirement> itemGates = new HashMap<>();
        Map<Identifier, Requirement> craftGates = new HashMap<>();
        Map<Identifier, Requirement> smithingGates = new HashMap<>();
        Map<Identifier, Requirement> brewingGates = new HashMap<>();
        Map<Identifier, Requirement> entityGates = new HashMap<>();
        if (payload.enabled()) {
            for (SkillGatesSyncPayload.Entry entry : payload.entries()) {
                Requirement requirement = new Requirement(entry.skill(), entry.level());
                switch (entry.kind()) {
                    case BREAK -> breakGates.put(entry.target(), requirement);
                    case BLOCK_USE -> useGates.put(entry.target(), requirement);
                    case ITEM_USE -> itemGates.put(entry.target(), requirement);
                    case CRAFT -> craftGates.put(entry.target(), requirement);
                    case SMITHING -> smithingGates.put(entry.target(), requirement);
                    case BREWING -> brewingGates.put(entry.target(), requirement);
                    case ENTITY -> entityGates.put(entry.target(), requirement);
                }
            }
        }
        serverSnapshot = new Snapshot(
                Map.copyOf(breakGates),
                Map.copyOf(useGates),
                Map.copyOf(itemGates),
                Map.copyOf(craftGates),
                Map.copyOf(smithingGates),
                Map.copyOf(brewingGates),
                Map.copyOf(entityGates));
        serverSnapshotReceived = true;
        loaded = true;
    }

    public static synchronized void init() {
        if (loaded) return;
        loaded = true;

        try {
            Path dir = FabricLoader.getInstance().getModContainer("hearthwind_skills")
                    .flatMap(c -> c.findPath("data/aged_skills/gates"))
                    .orElse(null);

            if (dir != null && Files.exists(dir)) {
                try (var stream = Files.walk(dir)) {
                    stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                            .forEach(ClientSkillGates::loadFile);
                }
            }
        } catch (Exception ignored) {}
    }

    private static void loadFile(Path path) {
        String skill = path.getFileName().toString().replace(".json", "");
        try (InputStream in = Files.newInputStream(path)) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            merge(root.getAsJsonArray("break"), skill, BREAK_GATES);
            merge(root.getAsJsonArray("use"), skill, USE_GATES);
        } catch (Exception ignored) {}
    }

    private static void merge(JsonArray tiers, String skill, Map<Identifier, Requirement> target) {
        if (tiers == null) return;
        for (JsonElement tierEl : tiers) {
            JsonObject tier = tierEl.getAsJsonObject();
            int level = tier.get("level").getAsInt();
            JsonArray blocks = tier.getAsJsonArray("blocks");
            if (blocks == null) continue;
            for (JsonElement b : blocks) {
                Identifier id = Identifier.tryParse(b.getAsString());
                if (id != null) {
                    target.putIfAbsent(id, new Requirement(skill, level));
                }
            }
        }
    }

    private static Snapshot activeSnapshot() {
        Snapshot snapshot = serverSnapshot;
        if (snapshot != null) {
            return snapshot;
        }
        init();
        return serverSnapshot;
    }

    public static Requirement getBreakRequirement(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Snapshot snapshot = activeSnapshot();
        return snapshot == null ? BREAK_GATES.get(id) : snapshot.breakGates().get(id);
    }

    public static Requirement getUseRequirement(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Snapshot snapshot = activeSnapshot();
        return snapshot == null ? USE_GATES.get(id) : snapshot.useGates().get(id);
    }

    public static boolean hasServerSnapshot() {
        return serverSnapshot != null;
    }

    public static Requirement getItemRequirement(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem bi) {
            Requirement req = getBreakRequirement(bi.getBlock());
            if (req != null) return req;
            return getUseRequirement(bi.getBlock());
        }
        Snapshot snapshot = activeSnapshot();
        return snapshot == null ? null : snapshot.itemGates().get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
