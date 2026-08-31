package dev.jmiahman.hearthwind.primitive;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

/**
 * Loads tiered affix definitions from datapack JSON and applies them to
 * matching items when they enter the world (crafted, looted, etc.).
 *
 * JSON format (from tiered/item_attributes/melee_weapons/):
 * {
 *   "id": "tiered:common_melee_1",
 *   "verifiers": [{"tag": "c:swords"}, {"tag": "aged:melee"}],
 *   "weight": 50,
 *   "style": {"color": "gray"},
 *   "attributes": [
 *     {
 *       "type": "generic.attack_damage",
 *       "modifier": {"name": "tiered:common_melee_1", "operation": "MULTIPLY_TOTAL", "amount": -0.3},
 *       "optional_equipment_slots": ["MAINHAND"]
 *     }
 *   ]
 * }
 */
public final class TieredAffixes {

    public static final String ATTRIBUTES_DIR = "tiered/item_attributes";

    private static final Map<Identifier, TieredAffixDefinition> DEFINITIONS = new HashMap<>();
    private static final List<TieredAffixDefinition> ALL_DEFINITIONS = new ArrayList<>();

    static {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ResourceManager rm = server.getResourceManager();
            loadAll(rm);
        });
    }

    private TieredAffixes() {}

    public static void init() {
        // Class initialization triggers the static block which registers SERVER_STARTING
    }

    private static void loadAll(ResourceManager resourceManager) {
        DEFINITIONS.clear();
        ALL_DEFINITIONS.clear();

        for (String category : new String[]{"melee_weapons", "all_tools", "all_armor",
                "elytra", "fishing_rod", "ranged_weapons", "shields"}) {
            loadCategory(resourceManager, category);
        }

        HearthwindPrimitive.LOGGER.info("tiered: loaded {} affix definitions", DEFINITIONS.size());
    }

    private static void loadCategory(ResourceManager resourceManager, String category) {
        try {
            var resources = resourceManager.listResources("tiered/item_attributes/" + category,
                    p -> p.toString().endsWith(".json"));
            for (var entry : resources.entrySet()) {
                try (InputStream is = entry.getValue().open()) {
                    JsonObject json = JsonParser.parseString(new String(is.readAllBytes())).getAsJsonObject();
                    TieredAffixDefinition def = parseDefinition(json);
                    if (def != null) {
                        DEFINITIONS.put(def.id(), def);
                        ALL_DEFINITIONS.add(def);
                    }
                }
            }
        } catch (IOException e) {
            HearthwindPrimitive.LOGGER.warn("tiered: failed to load category {}: {}", category, e.getMessage());
        }
    }

    private static TieredAffixDefinition parseDefinition(JsonObject json) {
        String id = json.getAsJsonPrimitive("id").getAsString();
        int weight = json.has("weight") ? json.getAsJsonPrimitive("weight").getAsInt() : 1;
        String color = "gray";
        if (json.has("style") && json.get("style").isJsonObject()) {
            JsonObject style = json.getAsJsonObject("style");
            if (style.has("color")) {
                color = style.getAsJsonPrimitive("color").getAsString();
            }
        }

        List<AffixAttribute> attributes = new ArrayList<>();
        if (json.has("attributes") && json.get("attributes").isJsonArray()) {
            for (JsonElement attr : json.getAsJsonArray("attributes")) {
                AffixAttribute att = parseAttribute(attr.getAsJsonObject());
                if (att != null) {
                    attributes.add(att);
                }
            }
        }

        List<TagKey<Item>> verifiers = new ArrayList<>();
        if (json.has("verifiers") && json.get("verifiers").isJsonArray()) {
            for (JsonElement v : json.getAsJsonArray("verifiers")) {
                JsonObject verifier = v.getAsJsonObject();
                if (verifier.has("tag")) {
                    String tagStr = verifier.getAsJsonPrimitive("tag").getAsString();
                    Identifier tagId = Identifier.parse(tagStr);
                    verifiers.add(TagKey.create(Registries.ITEM, tagId));
                }
            }
        }

        return new TieredAffixDefinition(Identifier.fromNamespaceAndPath("tiered", id),
                verifiers, weight, color, attributes);
    }

    private static AffixAttribute parseAttribute(JsonObject json) {
        String type = json.getAsJsonPrimitive("type").getAsString();
        JsonObject modifierJson = json.getAsJsonObject("modifier");
        String modName = modifierJson.getAsJsonPrimitive("name").getAsString();
        String opStr = modifierJson.getAsJsonPrimitive("operation").getAsString();
        double amount = modifierJson.getAsJsonPrimitive("amount").getAsDouble();

        Operation op;
        try {
            op = Operation.valueOf(opStr);
        } catch (IllegalArgumentException e) {
            op = Operation.ADD_VALUE;
        }

        List<EquipmentSlotGroup> slots = new ArrayList<>();
        if (json.has("optional_equipment_slots") && json.get("optional_equipment_slots").isJsonArray()) {
            for (JsonElement slot : json.getAsJsonArray("optional_equipment_slots")) {
                try {
                    slots.add(EquipmentSlotGroup.valueOf(slot.getAsString()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (slots.isEmpty()) {
            slots.add(EquipmentSlotGroup.ANY);
        }

        return new AffixAttribute(type, modName, op, amount, slots);
    }

    /**
     * Try to find a matching affix for an item. Returns empty if none match.
     */
    public static Optional<TieredAffixDefinition> findMatchingAffix(ItemStack stack) {
        for (TieredAffixDefinition def : ALL_DEFINITIONS) {
            if (def.matches(stack)) {
                return Optional.of(def);
            }
        }
        return Optional.empty();
    }

    /**
     * Apply affix modifiers to an item stack if it matches any definition.
     * Returns true if modifiers were applied.
     */
    public static boolean applyAffix(ItemStack stack) {
        Optional<TieredAffixDefinition> opt = findMatchingAffix(stack);
        if (opt.isPresent()) {
            ItemAttributeModifiers newModifiers = opt.get().getModifiers();
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, newModifiers);
            return true;
        }
        return false;
    }

    public record TieredAffixDefinition(Identifier id, List<TagKey<Item>> verifiers,
                                         int weight, String color,
                                         List<AffixAttribute> attributes) {
        public boolean matches(ItemStack stack) {
            if (verifiers.isEmpty()) {
                return false;
            }
            for (TagKey<Item> tag : verifiers) {
                if (stack.is(tag)) {
                    return true;
                }
            }
            return false;
        }

        public ItemAttributeModifiers getModifiers() {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            for (AffixAttribute attr : attributes) {
                Holder<Attribute> attrHolder = getAttribute(attr.type());
                if (attrHolder != null) {
                    Identifier modId = Identifier.fromNamespaceAndPath("tiered", attr.modifierName());
                    AttributeModifier modifier = new AttributeModifier(modId, attr.amount(), attr.operation());
                    for (EquipmentSlotGroup slot : attr.slots()) {
                        builder.add(attrHolder, modifier, slot);
                    }
                }
            }
            return builder.build();
        }

        private static Holder<Attribute> getAttribute(String type) {
            try {
                Identifier id = Identifier.parse(type);
                return BuiltInRegistries.ATTRIBUTE.getOptional(id).map(Holder::direct).orElse(null);
            } catch (Exception e) {
                // custom attribute from another mod - skip
            }
            return null;
        }
    }

    public record AffixAttribute(String type, String modifierName,
                                  Operation operation, double amount,
                                  List<EquipmentSlotGroup> slots) {}
}
