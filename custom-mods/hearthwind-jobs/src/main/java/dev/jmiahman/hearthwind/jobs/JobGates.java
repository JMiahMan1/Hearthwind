package dev.jmiahman.hearthwind.jobs;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Job-restricted recipe gating: items listed in a job's per-level {@code items}
 * arrays can only be crafted when the player holds that job at the required
 * level (or higher). When an item appears under multiple jobs, the LOWEST
 * requirement wins (most permissive), matching the skill-gate convention.
 * Creative-mode players bypass all gates.
 */
public final class JobGates {
    public record Gate(String jobId, int level) {}

    private static final Map<Identifier, Gate> ITEM_GATES = new ConcurrentHashMap<>();
    private static boolean loaded = false;

    private JobGates() {}

    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            Path dir = FabricLoader.getInstance().getModContainer("hearthwind_jobs")
                    .flatMap(c -> c.findPath("data/hearthwind_jobs/jobs"))
                    .orElseThrow();
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                        .forEach(JobGates::loadFile);
            }
            HearthwindJobs.LOGGER.info("Loaded {} job crafting gates", ITEM_GATES.size());
        } catch (Exception e) {
            HearthwindJobs.LOGGER.error("Failed to load job crafting gates", e);
        }
    }

    private static void loadFile(Path path) {
        String jobId;
        try {
            jobId = path.getFileName().toString().replace(".json", "");
        } catch (Exception e) {
            HearthwindJobs.LOGGER.warn("Bad gate file name {}: {}", path, e.toString());
            return;
        }
        JsonObject root;
        try (InputStream in = Files.newInputStream(path)) {
            root = JsonParser.parseReader(new java.io.InputStreamReader(
                    in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            HearthwindJobs.LOGGER.warn("Bad job file {}: {}", path, e.toString());
            return;
        }
        JsonArray levels = root.getAsJsonArray("levels");
        if (levels == null) {
            return;
        }
        for (JsonElement tierEl : levels) {
            JsonObject tier = tierEl.getAsJsonObject();
            int level = tier.get("level").getAsInt();
            JsonArray items = tier.getAsJsonArray("items");
            if (items == null) {
                continue;
            }
            for (JsonElement idEl : items) {
                Identifier id = Identifier.parse(idEl.getAsString());
                Gate existing = ITEM_GATES.get(id);
                if (existing == null || level < existing.level()) {
                    ITEM_GATES.put(id, new Gate(jobId, level));
                }
            }
        }
    }

    /** Required job+level to craft an item matching {@code id}, or null if ungated. */
    public static Gate gate(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return ITEM_GATES.get(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** True when the player may craft the item (gate satisfied or none). */
    public static boolean allowed(ServerPlayer player, ItemStack stack) {
        if (player == null || stack.isEmpty()) {
            return true;
        }
        if (player.getAbilities().instabuild || !HearthwindJobsConfig.get().jobCraftGating) {
            return true;
        }
        Gate gate = gate(stack);
        if (gate == null) {
            return true;
        }
        return JobState.level(player) >= gate.level()
                && JobState.jobId(player).equals(gate.jobId());
    }

    /** For gametests: expose loaded counts without leaking internals. */
    static int gateCount() {
        return ITEM_GATES.size();
    }
}
