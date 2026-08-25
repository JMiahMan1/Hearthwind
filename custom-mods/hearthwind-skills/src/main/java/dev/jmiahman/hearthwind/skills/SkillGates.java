package dev.jmiahman.hearthwind.skills;

import java.io.InputStream;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Content gating rebuilt in-house (levelz parity, advancement-free v1):
 *
 * - "break" gates: blocks that cannot be MINED until the skill reaches
 *   the required level (from migrated data/levelz/mining/NN.json);
 * - "use" gates: functional blocks (furnace, anvil, brewing stand, ...)
 *   that cannot be USED until the skill level is reached (from
 *   data/levelz/block/*.json).
 *
 * Definitions ship as <code>data/aged_skills/gates/&lt;skill&gt;.json</code>,
 * generated from the corpus by conversion/scripts/generate_skill_gates.py.
 * Unknown block ids never resolve and are ignored, mirroring the datapack's
 * {"required": false} convention. When one block appears under several
 * skills, the LOWEST requirement wins (most permissive).
 */
public final class SkillGates {
    public record Gate(Skill skill, int level) {}

    private static final Map<Identifier, Gate> BREAK_GATES = new HashMap<>();
    private static final Map<Identifier, Gate> USE_GATES = new HashMap<>();
    private static boolean loaded = false;

    private SkillGates() {}

    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            Path dir = FabricLoader.getInstance().getModContainer("hearthwind_skills")
                    .flatMap(c -> c.findPath("data/aged_skills/gates"))
                    .orElseThrow();
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                        .forEach(SkillGates::loadFile);
            }
            HearthwindSkills.LOGGER.info(
                    "Loaded {} break gates and {} use gates",
                    BREAK_GATES.size(), USE_GATES.size());
        } catch (Exception e) {
            HearthwindSkills.LOGGER.error("Failed to load skill gates", e);
        }
    }

    private static void loadFile(Path path) {
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
            root = JsonParser.parseReader(new java.io.InputStreamReader(
                    in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            HearthwindSkills.LOGGER.warn("Bad gate file {}: {}", path, e.toString());
            return;
        }
        merge(root.getAsJsonArray("break"), skill, BREAK_GATES);
        merge(root.getAsJsonArray("use"), skill, USE_GATES);
    }

    private static void merge(JsonArray tiers, Skill skill,
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
    public static Gate breakGate(BlockState state) {
        return BREAK_GATES.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    /** Required skill level to use {@code block}, or null if ungated. */
    public static Gate useGate(Block block) {
        return USE_GATES.get(BuiltInRegistries.BLOCK.getKey(block));
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
    static List<Map.Entry<Identifier, Gate>> debugEntries() {
        List<Map.Entry<Identifier, Gate>> all =
                new ArrayList<>(BREAK_GATES.entrySet());
        all.addAll(USE_GATES.entrySet());
        return all;
    }

    static int[] debugCounts() {
        return new int[]{BREAK_GATES.size(), USE_GATES.size()};
    }
}
