package dev.jmiahman.hearthwind.client;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<Identifier, Requirement> BREAK_GATES = new HashMap<>();
    private static final Map<Identifier, Requirement> USE_GATES = new HashMap<>();
    private static boolean loaded = false;

    private ClientSkillGates() {}

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

    public static Requirement getBreakRequirement(Block block) {
        init();
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return BREAK_GATES.get(id);
    }

    public static Requirement getUseRequirement(Block block) {
        init();
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return USE_GATES.get(id);
    }

    public static Requirement getItemRequirement(ItemStack stack) {
        init();
        if (stack.getItem() instanceof BlockItem bi) {
            Requirement req = getBreakRequirement(bi.getBlock());
            if (req != null) return req;
            return getUseRequirement(bi.getBlock());
        }
        return null;
    }
}
