package dev.jmiahman.hearthwind.primitive.tiered;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.jmiahman.hearthwind.primitive.HearthwindPrimitive;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class TierRegistry {
    private static final Map<Identifier, TierDefinition> TIERS = new HashMap<>();
    private static final List<TierDefinition> ALL_TIERS = new ArrayList<>();

    private TierRegistry() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            load(server.getResourceManager());
        });
    }

    public static void load(ResourceManager rm) {
        TIERS.clear();
        ALL_TIERS.clear();

        try {
            var resources = rm.listResources("item_attributes", path -> path.getPath().endsWith(".json"));
            for (var entry : resources.entrySet()) {
                if (!entry.getKey().getNamespace().equals("tiered") && !entry.getKey().getNamespace().equals("hearthwind")) {
                    continue;
                }
                try (InputStream is = entry.getValue().open()) {
                    String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject json = JsonParser.parseString(text).getAsJsonObject();
                    TierDefinition def = parseTier(json);
                    if (def != null) {
                        TIERS.put(def.id(), def);
                        ALL_TIERS.add(def);
                    }
                } catch (Exception e) {
                    HearthwindPrimitive.LOGGER.warn("Failed to parse tier JSON {}: {}", entry.getKey(), e.getMessage());
                }
            }
        } catch (Exception e) {
            HearthwindPrimitive.LOGGER.error("Failed to load tiered definitions: {}", e.getMessage());
        }

        HearthwindPrimitive.LOGGER.info("Tiered: successfully loaded {} affix definitions", TIERS.size());
    }

    public static TierDefinition get(Identifier id) {
        return TIERS.get(id);
    }

    public static List<TierDefinition> getAll() {
        return Collections.unmodifiableList(ALL_TIERS);
    }

    public static TierDefinition rollTier(ItemStack stack, RandomSource random) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        List<TierDefinition> eligible = new ArrayList<>();
        int totalWeight = 0;

        for (TierDefinition def : ALL_TIERS) {
            if (def.weight() > 0 && def.matches(stack)) {
                eligible.add(def);
                totalWeight += def.weight();
            }
        }

        if (eligible.isEmpty() || totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (TierDefinition def : eligible) {
            current += def.weight();
            if (roll < current) {
                return def;
            }
        }
        return eligible.get(eligible.size() - 1);
    }

    private static TierDefinition parseTier(JsonObject json) {
        if (!json.has("id")) return null;
        String idStr = json.get("id").getAsString();
        Identifier id = Identifier.parse(idStr);

        int weight = json.has("weight") ? json.get("weight").getAsInt() : 10;
        String color = "gray";
        if (json.has("style") && json.get("style").isJsonObject()) {
            JsonObject style = json.getAsJsonObject("style");
            if (style.has("color")) {
                color = style.get("color").getAsString();
            }
        }

        List<TierDefinition.Verifier> verifiers = new ArrayList<>();
        if (json.has("verifiers") && json.get("verifiers").isJsonArray()) {
            for (JsonElement elem : json.getAsJsonArray("verifiers")) {
                if (elem.isJsonObject()) {
                    JsonObject vo = elem.getAsJsonObject();
                    String tag = vo.has("tag") ? vo.get("tag").getAsString() : null;
                    String item = vo.has("item") ? vo.get("item").getAsString() : null;
                    verifiers.add(new TierDefinition.Verifier(tag, item));
                }
            }
        }

        List<TierDefinition.AffixAttribute> attributes = new ArrayList<>();
        if (json.has("attributes") && json.get("attributes").isJsonArray()) {
            for (JsonElement elem : json.getAsJsonArray("attributes")) {
                if (elem.isJsonObject()) {
                    JsonObject ao = elem.getAsJsonObject();
                    String type = ao.get("type").getAsString();
                    JsonObject modObj = ao.getAsJsonObject("modifier");
                    String modName = modObj.has("name") ? modObj.get("name").getAsString() : idStr;
                    String opStr = modObj.get("operation").getAsString();
                    double amount = modObj.get("amount").getAsDouble();

                    Operation op = parseOperation(opStr);

                    List<EquipmentSlotGroup> slots = new ArrayList<>();
                    if (ao.has("optional_equipment_slots") && ao.get("optional_equipment_slots").isJsonArray()) {
                        for (JsonElement slotElem : ao.getAsJsonArray("optional_equipment_slots")) {
                            slots.add(parseSlot(slotElem.getAsString()));
                        }
                    }
                    if (slots.isEmpty()) {
                        slots.add(EquipmentSlotGroup.ANY);
                    }

                    attributes.add(new TierDefinition.AffixAttribute(type, modName, op, amount, slots));
                }
            }
        }

        return new TierDefinition(id, verifiers, weight, color, attributes);
    }

    public static Holder<Attribute> resolveAttribute(String type) {
        String clean = type.toLowerCase().trim();
        if (clean.startsWith("minecraft:")) {
            clean = clean.substring(10);
        }
        if (clean.startsWith("generic.")) {
            clean = clean.substring(8);
        }
        return switch (clean) {
            case "attack_damage" -> Attributes.ATTACK_DAMAGE;
            case "attack_speed" -> Attributes.ATTACK_SPEED;
            case "armor" -> Attributes.ARMOR;
            case "armor_toughness" -> Attributes.ARMOR_TOUGHNESS;
            case "max_health" -> Attributes.MAX_HEALTH;
            case "movement_speed" -> Attributes.MOVEMENT_SPEED;
            case "knockback_resistance" -> Attributes.KNOCKBACK_RESISTANCE;
            case "luck" -> Attributes.LUCK;
            case "mining_efficiency", "tiered:generic.dig_speed", "dig_speed" -> Attributes.MINING_EFFICIENCY;
            case "entity_interaction_range", "reach-entity-attributes:attack_range", "attack_range" -> Attributes.ENTITY_INTERACTION_RANGE;
            case "block_interaction_range", "reach-entity-attributes:reach", "reach" -> Attributes.BLOCK_INTERACTION_RANGE;
            case "sweeping_damage_ratio" -> Attributes.SWEEPING_DAMAGE_RATIO;
            default -> {
                try {
                    Identifier id = Identifier.parse(type);
                    yield BuiltInRegistries.ATTRIBUTE.get(id).map(ref -> (Holder<Attribute>) ref).orElse(null);
                } catch (Exception ignored) {
                    yield null;
                }
            }
        };
    }

    private static Operation parseOperation(String s) {
        if (s == null) return Operation.ADD_VALUE;
        return switch (s.toUpperCase().trim()) {
            case "ADDITION", "ADD_VALUE" -> Operation.ADD_VALUE;
            case "MULTIPLY_BASE", "ADD_MULTIPLIED_BASE" -> Operation.ADD_MULTIPLIED_BASE;
            case "MULTIPLY_TOTAL", "ADD_MULTIPLIED_TOTAL" -> Operation.ADD_MULTIPLIED_TOTAL;
            default -> Operation.ADD_VALUE;
        };
    }

    private static EquipmentSlotGroup parseSlot(String s) {
        if (s == null) return EquipmentSlotGroup.ANY;
        return switch (s.toUpperCase().trim()) {
            case "MAINHAND", "MAIN_HAND" -> EquipmentSlotGroup.MAINHAND;
            case "OFFHAND", "OFF_HAND" -> EquipmentSlotGroup.OFFHAND;
            case "FEET", "BOOTS" -> EquipmentSlotGroup.FEET;
            case "LEGS", "LEGGINGS" -> EquipmentSlotGroup.LEGS;
            case "CHEST", "CHESTPLATE" -> EquipmentSlotGroup.CHEST;
            case "HEAD", "HELMET" -> EquipmentSlotGroup.HEAD;
            case "ARMOR" -> EquipmentSlotGroup.ARMOR;
            case "BODY" -> EquipmentSlotGroup.BODY;
            case "HAND" -> EquipmentSlotGroup.HAND;
            default -> EquipmentSlotGroup.ANY;
        };
    }
}
