package dev.jmiahman.hearthwind.survival;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Threshold effects from {@code data/nutritionz/nutrition_manager/*.json}
 * (shipped verbatim; other namespaces may override with {@code replace}).
 *
 * <p>For each nutrient the original applies the negative list at or below
 * {@code negativeNutrition} and the positive list at or above
 * {@code positiveNutrition}, checked every 20 ticks. Status effects are
 * refreshed on a long duration; attribute modifiers are added as transient
 * modifiers (idempotent by id, so the 1s cadence is safe).
 */
public final class NutritionEffects {
    public record StatusEffect(Holder<MobEffect> effect, int duration, int amplifier) {}

    public record AttributeEffect(Holder<Attribute> attribute, AttributeModifier modifier) {}

    /** Parsed positive or negative list for one nutrient. */
    public static final class EffectSet {
        private final List<StatusEffect> statuses = new ArrayList<>();
        private final List<AttributeEffect> attributes = new ArrayList<>();

        public boolean isEmpty() {
            return this.statuses.isEmpty() && this.attributes.isEmpty();
        }

        public List<StatusEffect> statuses() {
            return this.statuses;
        }

        public List<AttributeEffect> attributes() {
            return this.attributes;
        }
    }

    private static final EffectSet[] POSITIVE = new EffectSet[HearthwindSurvivalDiet.NUTRIENT_COUNT];
    private static final EffectSet[] NEGATIVE = new EffectSet[HearthwindSurvivalDiet.NUTRIENT_COUNT];

    private NutritionEffects() {}

    public static synchronized void load(ResourceManager manager) {
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            POSITIVE[i] = new EffectSet();
            NEGATIVE[i] = new EffectSet();
        }
        boolean[] replaced = new boolean[HearthwindSurvivalDiet.NUTRIENT_COUNT];
        Map<Identifier, Resource> resources = manager.listResources("nutrition_manager",
                id -> id.getPath().endsWith(".json"));
        List<Identifier> ids = new ArrayList<>(resources.keySet());
        Collections.sort(ids);
        for (Identifier id : ids) {
            try (InputStream stream = resources.get(id).open()) {
                JsonObject data = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
                    String name = HearthwindSurvivalDiet.NUTRIENT_NAMES[i];
                    JsonElement element = data.get(name);
                    if (element == null || !element.isJsonObject() || replaced[i]) {
                        continue;
                    }
                    JsonObject nutrient = element.getAsJsonObject();
                    if (nutrient.has("replace") && nutrient.get("replace").getAsBoolean()) {
                        replaced[i] = true;
                    }
                    if (nutrient.has("positive") && nutrient.get("positive").isJsonObject()) {
                        readEffects(nutrient.getAsJsonObject("positive"), POSITIVE[i], i, "positive");
                    }
                    if (nutrient.has("negative") && nutrient.get("negative").isJsonObject()) {
                        readEffects(nutrient.getAsJsonObject("negative"), NEGATIVE[i], i, "negative");
                    }
                }
            } catch (Exception e) {
                HearthwindSurvival.LOGGER.warn("nutrition_manager: could not read {}: {}", id, e.toString());
            }
        }
        HearthwindSurvival.LOGGER.info("nutrition effects: {} positive / {} negative entries from {} manager files",
                entryCount(POSITIVE), entryCount(NEGATIVE), ids.size());
    }

    private static int entryCount(EffectSet[] sets) {
        int count = 0;
        for (EffectSet set : sets) {
            count += set.statuses.size() + set.attributes.size();
        }
        return count;
    }

    private static void readEffects(JsonObject effects, EffectSet target, int nutrient, String polarity) {
        List<String> keys = new ArrayList<>(effects.keySet());
        Collections.sort(keys);
        for (String rawId : keys) {
            JsonElement element = effects.get(rawId);
            if (element == null || !element.isJsonObject()) {
                continue;
            }
            Identifier id = Identifier.tryParse(rawId);
            if (id == null) {
                continue;
            }
            JsonObject effect = element.getAsJsonObject();
            Optional<Holder.Reference<MobEffect>> mob = BuiltInRegistries.MOB_EFFECT.get(id);
            if (mob.isPresent()) {
                int duration = effect.has("duration") ? effect.get("duration").getAsInt() : 0;
                int amplifier = effect.has("amplifier") ? effect.get("amplifier").getAsInt() : 0;
                target.statuses.add(new StatusEffect(mob.get(), duration, amplifier));
                continue;
            }
            Holder<Attribute> attribute = resolveAttribute(id);
            if (attribute == null) {
                HearthwindSurvival.LOGGER.info(
                        "nutrition_manager: {} is neither an effect nor an attribute; skipped", rawId);
                continue;
            }
            double value = effect.has("value") ? effect.get("value").getAsDouble() : 0.0;
            AttributeModifier.Operation operation = operation(
                    effect.has("operation") ? effect.get("operation").getAsString() : "add_value");
            Identifier modifierId = Identifier.fromNamespaceAndPath("hearthwind_survival",
                    "nutrition_" + HearthwindSurvivalDiet.NUTRIENT_NAMES[nutrient] + "_" + polarity
                            + "_" + id.getPath().replace('.', '_'));
            target.attributes.add(new AttributeEffect(attribute,
                    new AttributeModifier(modifierId, value, operation)));
        }
    }

    /**
     * 1.20.1 attribute ids such as {@code generic.attack_speed} lost their
     * {@code generic.} prefix in 26.2 ({@code minecraft:attack_speed}).
     */
    private static Holder<Attribute> resolveAttribute(Identifier id) {
        Optional<Holder.Reference<Attribute>> direct = BuiltInRegistries.ATTRIBUTE.get(id);
        if (direct.isPresent()) {
            return direct.get();
        }
        String path = id.getPath();
        if (path.startsWith("generic.")) {
            Identifier legacy = Identifier.fromNamespaceAndPath(id.getNamespace(),
                    path.substring("generic.".length()));
            return BuiltInRegistries.ATTRIBUTE.get(legacy).orElse(null);
        }
        return null;
    }

    private static AttributeModifier.Operation operation(String raw) {
        return switch (raw.toUpperCase(Locale.ROOT)) {
            case "MULTIPLY_BASE", "ADD_MULTIPLIED_BASE" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "MULTIPLY_TOTAL", "ADD_MULTIPLIED_TOTAL" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    /** Called once per second per player from the diet tick loop. */
    public static synchronized void apply(ServerPlayer player) {
        if (player.isCreative()) {
            return;
        }
        applyInternal(player);
    }

    /**
     * Gametest entry: GameTestHelper mock players hardcode
     * {@code gameMode() == CREATIVE}, so the parity guard above can never be
     * satisfied in tests; the effect logic itself is identical.
     */
    static synchronized void applyForTest(ServerPlayer player) {
        applyInternal(player);
    }

    private static void applyInternal(ServerPlayer player) {
        if (POSITIVE[0] == null || NEGATIVE[0] == null) {
            return;
        }
        HearthwindSurvivalConfig.Diet cfg = HearthwindSurvivalConfig.get().diet;
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            int level = HearthwindSurvivalDiet.getLevel(player, i);
            EffectSet active = null;
            if (level <= cfg.negativeNutrition) {
                active = NEGATIVE[i];
            } else if (level >= cfg.positiveNutrition) {
                active = POSITIVE[i];
            }
            if (active != null && !active.isEmpty()) {
                for (StatusEffect status : active.statuses) {
                    MobEffectInstance existing = player.getEffect(status.effect());
                    if (existing == null || existing.getDuration() < status.duration() - 50) {
                        player.addEffect(new MobEffectInstance(status.effect(), status.duration(),
                                status.amplifier(), false, false, true));
                    }
                }
                if (!active.attributes.isEmpty()) {
                    Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
                    for (AttributeEffect attribute : active.attributes) {
                        modifiers.put(attribute.attribute(), attribute.modifier());
                    }
                    player.getAttributes().addTransientAttributeModifiers(modifiers);
                }
            } else {
                Multimap<Holder<Attribute>, AttributeModifier> positiveModifiers = LinkedHashMultimap.create();
                for (AttributeEffect attribute : POSITIVE[i].attributes) {
                    positiveModifiers.put(attribute.attribute(), attribute.modifier());
                }
                Multimap<Holder<Attribute>, AttributeModifier> negativeModifiers = LinkedHashMultimap.create();
                for (AttributeEffect attribute : NEGATIVE[i].attributes) {
                    negativeModifiers.put(attribute.attribute(), attribute.modifier());
                }
                player.getAttributes().removeAttributeModifiers(positiveModifiers);
                player.getAttributes().removeAttributeModifiers(negativeModifiers);
            }
        }
    }

    /** Translation keys for the panel tooltips, index = nutrient. */
    public static synchronized List<List<String>> positiveKeys() {
        return keys(POSITIVE);
    }

    public static synchronized List<List<String>> negativeKeys() {
        return keys(NEGATIVE);
    }

    private static List<List<String>> keys(EffectSet[] sets) {
        List<List<String>> out = new ArrayList<>(HearthwindSurvivalDiet.NUTRIENT_COUNT);
        for (EffectSet set : sets) {
            List<String> keys = new ArrayList<>();
            for (StatusEffect status : set.statuses) {
                keys.add(status.effect().value().getDescriptionId());
            }
            for (AttributeEffect attribute : set.attributes) {
                keys.add(attribute.attribute().value().getDescriptionId());
            }
            out.add(keys);
        }
        return out;
    }

    public static synchronized int positiveCount() {
        return entryCount(POSITIVE);
    }

    public static synchronized int negativeCount() {
        return entryCount(NEGATIVE);
    }
}
