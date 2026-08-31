package dev.jmiahman.hearthwind.primitive.tiered;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.jmiahman.hearthwind.primitive.HearthwindPrimitive;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ReforgeRegistry {
    private static final Map<Item, List<Item>> RECIPES = new HashMap<>();

    private ReforgeRegistry() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            load(server.getResourceManager());
        });
    }

    public static void load(ResourceManager rm) {
        RECIPES.clear();

        try {
            var resources = rm.listResources("reforge_items", path -> path.getPath().endsWith(".json"));
            for (var entry : resources.entrySet()) {
                if (!entry.getKey().getNamespace().equals("tiered") && !entry.getKey().getNamespace().equals("hearthwind")) {
                    continue;
                }
                try (InputStream is = entry.getValue().open()) {
                    String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject json = JsonParser.parseString(text).getAsJsonObject();

                    List<Item> items = new ArrayList<>();
                    if (json.has("items") && json.get("items").isJsonArray()) {
                        for (JsonElement e : json.getAsJsonArray("items")) {
                            Identifier itemId = Identifier.parse(e.getAsString());
                            BuiltInRegistries.ITEM.getOptional(itemId).ifPresent(items::add);
                        }
                    }

                    List<Item> base = new ArrayList<>();
                    if (json.has("base") && json.get("base").isJsonArray()) {
                        for (JsonElement e : json.getAsJsonArray("base")) {
                            Identifier baseId = Identifier.parse(e.getAsString());
                            BuiltInRegistries.ITEM.getOptional(baseId).ifPresent(base::add);
                        }
                    }

                    for (Item it : items) {
                        RECIPES.computeIfAbsent(it, k -> new ArrayList<>()).addAll(base);
                    }
                } catch (Exception e) {
                    HearthwindPrimitive.LOGGER.warn("Failed to parse reforge recipe {}: {}", entry.getKey(), e.getMessage());
                }
            }
        } catch (Exception e) {
            HearthwindPrimitive.LOGGER.error("Failed to load reforge recipes: {}", e.getMessage());
        }

        HearthwindPrimitive.LOGGER.info("Tiered: loaded {} equipment reforge recipes", RECIPES.size());
    }

    public static boolean canReforge(ItemStack target, ItemStack ingredient) {
        if (target == null || target.isEmpty() || ingredient == null || ingredient.isEmpty()) {
            return false;
        }

        Item targetItem = target.getItem();
        Item ingItem = ingredient.getItem();

        // 1. Check data-driven recipes
        List<Item> allowed = RECIPES.get(targetItem);
        if (allowed != null && allowed.contains(ingItem)) {
            return true;
        }

        // 2. Universal catalyst: Amethyst Shard & Netherite Ingot can reforge ANY equipment!
        if (ingItem == Items.AMETHYST_SHARD || ingItem == Items.NETHERITE_INGOT) {
            return true;
        }

        // 3. Fallbacks based on material names for modded/vanilla tools
        String targetName = BuiltInRegistries.ITEM.getKey(targetItem).getPath();
        if (targetName.contains("diamond") && ingItem == Items.DIAMOND) return true;
        if (targetName.contains("iron") && ingItem == Items.IRON_INGOT) return true;
        if (targetName.contains("gold") && ingItem == Items.GOLD_INGOT) return true;
        if (targetName.contains("copper") && ingItem == Items.COPPER_INGOT) return true;
        if (targetName.contains("netherite") && (ingItem == Items.NETHERITE_INGOT || ingItem == Items.NETHERITE_SCRAP)) return true;
        if (targetName.contains("flint") && ingItem == Items.FLINT) return true;
        if (targetName.contains("steel") && (ingItem == Items.IRON_INGOT || ingItem.toString().contains("steel"))) return true;
        if (targetName.contains("leather") && ingItem == Items.LEATHER) return true;
        if (targetName.contains("chainmail") && ingItem == Items.IRON_INGOT) return true;
        if (targetName.contains("wood") && (ingItem == Items.STICK || ingItem.toString().contains("planks"))) return true;
        if (targetItem == Items.BOW || targetItem == Items.CROSSBOW || targetItem == Items.FISHING_ROD) {
            return ingItem == Items.STRING;
        }
        if (targetItem == Items.SHIELD) {
            return ingItem == Items.IRON_INGOT || ingItem.toString().contains("planks");
        }

        return false;
    }
}
