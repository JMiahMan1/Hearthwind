package dev.jmiahman.hearthwind.survival;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Age-gated nutrition: certain food groups or items require minimum skill
 * levels or Ages before the player may consume them. Enforced in the consume
 * mixin; violation sends a tooltip hint and denies the eat.
 *
 * Gate sources (in priority order, first match wins):
 *   1. <code>data/hearthwind_survival/diet_gates/&lt;group|item&gt;.json</code>
 *      (datapack-driven, retains parity with Aged's tuning)
 *   2. <code>config/hearthwind_survival.json:diet.gates</code> (tunable defaults)
 *
 * Gate types:
 *   - "skill": requires {@code skill} at {@code level}+
 *   - "age": requires Age {@code age}+
 *   - "job": requires holding {@code job} at {@code level}+
 */
public final class DietGates {
    private static final String[] GROUPS = {"fruits", "vegetables", "grains", "proteins", "sugars"};

    /** Gate kind + target. */
    public record Gate(String kind, String target, int threshold) {}

    /** Single requirement; all in a group's list must pass (AND). */
    public record Requirement(String kind, String target, int threshold) {}

    private static final Map<TagKey<Item>, List<Requirement>> GROUP_GATES = new HashMap<>();
    private static final Map<Identifier, List<Requirement>> ITEM_GATES = new HashMap<>();
    private static boolean loaded = false;

    private DietGates() {}

    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        loadDatapackGates();
        loadConfigGates();
        HearthwindSurvival.LOGGER.info("Loaded diet gates: {} group, {} item", GROUP_GATES.size(), ITEM_GATES.size());
    }

    private static void loadDatapackGates() {
        try {
            Path dir = FabricLoader.getInstance().getModContainer("hearthwind_survival")
                    .flatMap(c -> c.findPath("data/hearthwind_survival/diet_gates"))
                    .orElse(null);
            if (dir == null) {
                return;
            }
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                        .forEach(DietGates::loadGateFile);
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("Failed to load diet gates from datapack", e);
        }
    }

    private static void loadGateFile(Path path) {
        String name = path.getFileName().toString().replace(".json", "");
        try (InputStream in = Files.newInputStream(path)) {
            JsonObject root = JsonParser.parseReader(new java.io.InputStreamReader(
                    in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray reqs = root.getAsJsonArray("requirements");
            if (reqs == null || reqs.isEmpty()) {
                return;
            }
            List<Requirement> list = new java.util.ArrayList<>();
            for (JsonElement el : reqs) {
                JsonObject r = el.getAsJsonObject();
                list.add(new Requirement(
                        r.get("kind").getAsString(),
                        r.get("target").getAsString(),
                        r.get("threshold").getAsInt()));
            }
            // Try as group name first; fall back to item id
            boolean foundGroup = false;
            for (String g : GROUPS) {
                if (g.equals(name)) {
                    TagKey<Item> tag = switch (g) {
                        case "fruits" -> HearthwindSurvivalDiet.FRUITS;
                        case "vegetables" -> HearthwindSurvivalDiet.VEGETABLES;
                        case "grains" -> HearthwindSurvivalDiet.GRAINS;
                        case "proteins" -> HearthwindSurvivalDiet.PROTEINS;
                        case "sugars" -> HearthwindSurvivalDiet.SUGARS;
                        default -> null;
                    };
                    if (tag != null) {
                        GROUP_GATES.put(tag, list);
                        foundGroup = true;
                        break;
                    }
                }
            }
            if (!foundGroup) {
                Identifier id = Identifier.parse(name);
                ITEM_GATES.put(id, list);
            }
        } catch (Exception e) {
            HearthwindSurvival.LOGGER.warn("Bad diet gate file {}: {}", path, e);
        }
    }

    private static void loadConfigGates() {
        HearthwindSurvivalConfig.Diet cfg = HearthwindSurvivalConfig.get().diet;
        // Default gates: proteins require farming 3, grains require farming 1
        // (mirrors Aged's early-game restriction on cooked/meat foods)
        if (cfg.proteinsRequireFarming && !GROUP_GATES.containsKey(HearthwindSurvivalDiet.PROTEINS)) {
            GROUP_GATES.put(HearthwindSurvivalDiet.PROTEINS, List.of(
                    new Requirement("skill", "farming", 3)));
        }
        if (cfg.grainsRequireFarming && !GROUP_GATES.containsKey(HearthwindSurvivalDiet.GRAINS)) {
            GROUP_GATES.put(HearthwindSurvivalDiet.GRAINS, List.of(
                    new Requirement("skill", "farming", 1)));
        }
    }

    /**
     * Check whether {@code player} may consume {@code stack}.
     * Returns {@code true} when allowed; sends a tooltip hint and returns
     * {@code false} on violation.
     */
    public static boolean allowed(ServerPlayer player, ItemStack stack) {
        ensureLoaded();
        if (player == null || stack.isEmpty() || player.getAbilities().instabuild) {
            return true;
        }
        // Check item-level gate first (most specific)
        Identifier itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Requirement> itemReqs = ITEM_GATES.get(itemId);
        if (itemReqs != null && !passesAll(player, itemReqs)) {
            deny(player, itemId);
            return false;
        }
        // Check group-level gate
        for (String g : DietGates.GROUPS) {
            TagKey<Item> group = switch (g) {
                case "fruits" -> HearthwindSurvivalDiet.FRUITS;
                case "vegetables" -> HearthwindSurvivalDiet.VEGETABLES;
                case "grains" -> HearthwindSurvivalDiet.GRAINS;
                case "proteins" -> HearthwindSurvivalDiet.PROTEINS;
                case "sugars" -> HearthwindSurvivalDiet.SUGARS;
                default -> null;
            };
            if (group == null) continue;
            if (stack.is(group)) {
                List<Requirement> reqs = GROUP_GATES.get(group);
                if (reqs != null && !passesAll(player, reqs)) {
                    deny(player, group);
                    return false;
                }
                break;
            }
        }
        return true;
    }

    private static boolean passesAll(ServerPlayer player, List<Requirement> reqs) {
        for (Requirement r : reqs) {
            if (!passes(player, r)) {
                return false;
            }
        }
        return true;
    }

    private static boolean passes(ServerPlayer player, Requirement r) {
        return switch (r.kind()) {
            case "skill" -> {
                if (!FabricLoader.getInstance().isModLoaded("hearthwind_skills")) {
                    yield true;
                }
                yield checkSkill(player, r.target(), r.threshold());
            }
            case "age" -> {
                if (!FabricLoader.getInstance().isModLoaded("hearthwind_jobs")) {
                    yield true;
                }
                yield checkAge(player, r.threshold());
            }
            case "job" -> {
                if (!FabricLoader.getInstance().isModLoaded("hearthwind_jobs")) {
                    yield true;
                }
                yield checkJob(player, r.target(), r.threshold());
            }
            default -> true;
        };
    }

    private static boolean checkSkill(ServerPlayer player, String skillId, int threshold) {
        try {
            Class<?> skillCls = Class.forName("dev.jmiahman.hearthwind.skills.Skill");
            Class<?> xpCls = Class.forName("dev.jmiahman.hearthwind.skills.SkillXp");
            java.lang.reflect.Method byId = skillCls.getMethod("byId", String.class);
            java.lang.reflect.Method level = xpCls.getMethod("level", Entity.class, skillCls);
            Object skill = byId.invoke(null, skillId);
            int lvl = (int) level.invoke(null, player, skill);
            return lvl >= threshold;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean checkAge(ServerPlayer player, int threshold) {
        try {
            Class<?> cls = Class.forName("dev.jmiahman.hearthwind.jobs.AgeState");
            java.lang.reflect.Method get = cls.getMethod("get", Entity.class);
            int age = (int) get.invoke(null, player);
            return age >= threshold;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean checkJob(ServerPlayer player, String jobId, int threshold) {
        try {
            Class<?> defsCls = Class.forName("dev.jmiahman.hearthwind.jobs.JobDefs");
            Class<?> stateCls = Class.forName("dev.jmiahman.hearthwind.jobs.JobState");
            java.lang.reflect.Method byId = defsCls.getMethod("byId", String.class);
            java.lang.reflect.Method level = stateCls.getMethod("level", Entity.class);
            java.lang.reflect.Method id = stateCls.getMethod("jobId", Entity.class);
            Object def = byId.invoke(null, jobId);
            if (def == null) return false;
            int lvl = (int) level.invoke(null, player);
            String current = (String) id.invoke(null, player);
            return lvl >= threshold && jobId.equals(current);
        } catch (Exception e) {
            return true;
        }
    }

    private static void deny(ServerPlayer player, Object target) {
        Component msg;
        if (target instanceof TagKey<?>) {
            String path = ((TagKey<?>) target).location().getPath();
            msg = Component.literal("You need more experience to eat " + path + ".");
        } else {
            String id = ((Identifier) target).getNamespace() + ":" + ((Identifier) target).getPath();
            msg = Component.literal("You need more experience to eat " + id + ".");
        }
        player.sendSystemMessage(msg);
    }

    /** Exposed for gametests. */
    static int groupGateCount() { return GROUP_GATES.size(); }
    static int itemGateCount() { return ITEM_GATES.size(); }
}
