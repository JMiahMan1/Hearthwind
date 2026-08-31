package dev.jmiahman.hearthwind.jobs;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Job definitions loaded from <code>data/hearthwind_jobs/jobs/&lt;job&gt;.json</code>
 * (generated from the migrated jobs-addon corpus). Each level lists the
 * entity/block/item ids that award job XP while the player holds that
 * job. Unknown ids never match - same convention as skill gates.
 */
public final class JobDefs {
    public record Level(int level, List<String> entities,
            List<String> blocks, List<String> items) {}

    public static final class JobDef {
        public final String id;
        public final List<Level> levels;

        JobDef(String id, List<Level> levels) {
            this.id = id;
            this.levels = levels;
        }

        public int maxLevel() {
            return levels.isEmpty() ? 0
                    : levels.get(levels.size() - 1).level();
        }
    }

    private static final Gson GSON = new Gson();
    private static final Map<String, JobDef> JOBS = new HashMap<>();
    private static boolean loaded = false;

    private JobDefs() {}

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
                        .forEach(JobDefs::loadFile);
            }
            HearthwindJobs.LOGGER.info("Loaded {} job definitions", JOBS.size());
        } catch (Exception e) {
            HearthwindJobs.LOGGER.error("Failed to load job definitions", e);
        }
    }

    private static void loadFile(Path path) {
        try (InputStreamReader r = new InputStreamReader(
                Files.newInputStream(path), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            String id = root.get("job").getAsString();
            List<Level> levels = new java.util.ArrayList<>();
            for (var lv : root.getAsJsonArray("levels")) {
                JsonObject o = lv.getAsJsonObject();
                levels.add(new Level(o.get("level").getAsInt(),
                        stringList(o, "entities"),
                        stringList(o, "blocks"),
                        stringList(o, "items")));
            }
            JOBS.put(id, new JobDef(id, levels));
        } catch (Exception e) {
            HearthwindJobs.LOGGER.warn("Bad job file {}: {}", path, e.toString());
        }
    }

    private static List<String> stringList(JsonObject o, String key) {
        if (!o.has(key)) {
            return List.of();
        }
        List<String> out = new java.util.ArrayList<>();
        o.getAsJsonArray(key).forEach(e -> out.add(e.getAsString()));
        return out;
    }

    public static JobDef byId(String id) {
        return JOBS.get(id);
    }

    public static Map<String, JobDef> all() {
        return JOBS;
    }
}
