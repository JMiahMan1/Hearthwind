package dev.jmiahman.aged.survival;

import java.util.Map;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Diet system replacing nutritionz (parity-first):
 * five food groups tracked as 0..100 nutrients that decay over time.
 * Eating tagged food refills the matching groups. Deficient groups apply
 * debuffs; a fully balanced diet grants bonus absorption hearts.
 * Groups are plain item tags under <code>nutritionz:</code> so the migrated
 * datapack can retune them without code changes.
 */
public final class AgedSurvivalDiet {
    public static final double MAX_NUTRIENTS = 100.0;

    private static final int TICK_INTERVAL = 20; // once per second
    private static final int DEBUFF_DURATION_TICKS = 60;

    public static final TagKey<Item> FRUITS = tag("fruits");
    public static final TagKey<Item> GRAINS = tag("grains");
    public static final TagKey<Item> PROTEINS = tag("proteins");
    public static final TagKey<Item> SUGARS = tag("sugars");
    public static final TagKey<Item> VEGETABLES = tag("vegetables");

    private static final TagKey<Item>[] GROUPS = new TagKey[]{
            FRUITS, VEGETABLES, GRAINS, PROTEINS, SUGARS};

    public static final AttachmentType<Map<String, Double>> NUTRIENTS =
            AttachmentRegistry.<Map<String, Double>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.DOUBLE))
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("nutritionz", "nutrients"));

    private AgedSurvivalDiet() {}

    private static TagKey<Item> tag(String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                Identifier.fromNamespaceAndPath("nutritionz", path));
    }

    /** Nutrient value of one group for an entity (defaults to a fed start). */
    public static double level(Entity entity, TagKey<Item> group) {
        Map<String, Double> map = entity.getAttached(NUTRIENTS);
        if (map == null || !map.containsKey(pathOf(group))) {
            return MAX_NUTRIENTS;
        }
        return map.get(pathOf(group));
    }

    public static void setLevel(Entity entity, TagKey<Item> group, double value) {
        Map<String, Double> map = entity.getAttachedOrElse(NUTRIENTS,
                new java.util.HashMap<>());
        map.put(pathOf(group), Math.max(0.0, Math.min(MAX_NUTRIENTS, value)));
        entity.setAttached(NUTRIENTS, map);
    }

    private static String pathOf(TagKey<Item> group) {
        return group.location().getPath();
    }

    /** Called from the eat mixin after a player finishes eating {@code stack}. */
    public static void onEaten(Entity entity, ItemStack stack) {
        float nutrition = defaultNutrition(stack);
        if (nutrition <= 0) {
            return;
        }
        double amount = nutrition * AgedSurvivalConfig.get().diet.nutrientsPerFoodPoint;
        for (TagKey<Item> group : GROUPS) {
            if (stack.is(group)) {
                setLevel(entity, group,
                        Math.min(MAX_NUTRIENTS, level(entity, group) + amount));
            }
        }
    }

    private static float defaultNutrition(ItemStack stack) {
        var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        return food == null ? 0.0f : food.nutrition();
    }

    public static void registerTickLoop() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % TICK_INTERVAL != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });
    }

    private static void tick(ServerPlayer player) {
        if (player.getAbilities().invulnerable || player.getAbilities().instabuild) {
            return;
        }
        AgedSurvivalConfig.Diet cfg = AgedSurvivalConfig.get().diet;
        DietState state = applyDecay(player, cfg);
        if (state.deficient > 0) {
            applyDeficiencyDebuffs(player);
        } else {
            refreshBonusHearts(player, cfg.balancedBonusHearts);
        }
        if (!state.allBalanced) {
            player.removeEffect(MobEffects.ABSORPTION);
        }
    }

    /** Decay + classification step; exposed for gametests. */
    public static DietState applyDecay(Entity entity, AgedSurvivalConfig.Diet cfg) {
        boolean allBalanced = true;
        int deficient = 0;
        for (TagKey<Item> group : GROUPS) {
            double v = level(entity, group) - cfg.decayPerSecond * TICK_INTERVAL;
            v = Math.max(0.0, v);
            setLevel(entity, group, v);
            if (v < cfg.deficiencyThreshold) {
                deficient++;
                allBalanced = false;
            } else if (v < cfg.balanceThreshold) {
                allBalanced = false;
            }
        }
        return new DietState(deficient, allBalanced);
    }

    public record DietState(int deficient, boolean allBalanced) {}

    private static void applyDeficiencyDebuffs(ServerPlayer player) {
        AgedSurvivalConfig.Diet cfg = AgedSurvivalConfig.get().diet;
        if (level(player, FRUITS) < cfg.deficiencyThreshold) {
            apply(player, MobEffects.MINING_FATIGUE);
        }
        if (level(player, VEGETABLES) < cfg.deficiencyThreshold) {
            apply(player, MobEffects.WEAKNESS);
        }
        if (level(player, GRAINS) < cfg.deficiencyThreshold) {
            apply(player, MobEffects.SLOWNESS);
        }
        if (level(player, PROTEINS) < cfg.deficiencyThreshold) {
            apply(player, MobEffects.WEAKNESS);
        }
    }

    private static void apply(ServerPlayer player, Holder<MobEffect> effect) {
        player.addEffect(new MobEffectInstance(effect, DEBUFF_DURATION_TICKS, 0,
                true, false, true));
    }

    private static void refreshBonusHearts(ServerPlayer player, float hearts) {
        if (hearts <= 0) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.ABSORPTION);
        if (current == null || current.getDuration() <= DEBUFF_DURATION_TICKS / 2) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
                    DEBUFF_DURATION_TICKS, (int) (hearts - 1), true, false, true));
        }
    }

    static void debugStatus(ServerPlayer player) {
        StringBuilder sb = new StringBuilder("Diet:");
        for (TagKey<Item> group : GROUPS) {
            sb.append(' ').append(pathOf(group)).append('=')
                    .append(String.format("%.1f", level(player, group)));
        }
        player.sendSystemMessage(Component.literal(sb.toString()));
    }
}
