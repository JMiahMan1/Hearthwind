package dev.jmiahman.hearthwind.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.jmiahman.hearthwind.survival.NutritionEffectsPayload;
import dev.jmiahman.hearthwind.survival.NutritionItemMapPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Client mirror of the server's nutrition datapack: per-item nutrient values
 * (for the Shift tooltip) and threshold-effect translation keys (for the
 * Nutrients panel hover tooltips). Populated on join / datapack reload.
 */
public final class ClientNutritionData {
    private static final int COUNT = 5;

    private static final Map<Item, int[]> ITEM_MAP = new HashMap<>();
    private static List<List<String>> positive = emptyGroups();
    private static List<List<String>> negative = emptyGroups();

    private ClientNutritionData() {}

    public static void setItemMap(List<NutritionItemMapPayload.Entry> entries) {
        ITEM_MAP.clear();
        for (NutritionItemMapPayload.Entry entry : entries) {
            Item item = BuiltInRegistries.ITEM.getOptional(entry.item()).orElse(null);
            if (item == null) {
                continue;
            }
            ITEM_MAP.put(item, new int[] {
                    entry.carbohydrates(), entry.protein(), entry.fat(),
                    entry.vitamins(), entry.minerals() });
        }
    }

    public static void setEffects(List<List<String>> positiveGroups, List<List<String>> negativeGroups) {
        positive = copyGroups(positiveGroups);
        negative = copyGroups(negativeGroups);
    }

    private static List<List<String>> copyGroups(List<List<String>> groups) {
        List<List<String>> copy = emptyGroups();
        for (int i = 0; i < COUNT && i < groups.size(); i++) {
            copy.set(i, List.copyOf(groups.get(i)));
        }
        return copy;
    }

    private static List<List<String>> emptyGroups() {
        List<List<String>> groups = new ArrayList<>(COUNT);
        for (int i = 0; i < COUNT; i++) {
            groups.add(List.of());
        }
        return groups;
    }

    /** Five values for a stack, or null when the item carries no nutrition. */
    public static int[] nutritionOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return ITEM_MAP.get(stack.getItem());
    }

    /** Translation keys for one threshold-effect tooltip. */
    public static List<Component> effectTooltip(int index, boolean positiveSide) {
        if (index < 0 || index >= COUNT) {
            return List.of();
        }
        List<String> keys = positiveSide ? positive.get(index) : negative.get(index);
        List<Component> lines = new ArrayList<>(keys.size());
        for (String key : keys) {
            lines.add(Component.translatable(key));
        }
        return lines;
    }

    /**
     * Appends the NutritionZ Shift tooltip: blank line, "Nutrients:" header,
     * then one green line per nutrient with a value above zero.
     */
    public static void appendShiftTooltip(ItemStack stack, List<Component> lines) {
        int[] values = nutritionOf(stack);
        if (values == null) {
            return;
        }
        boolean addedHeader = false;
        for (int i = 0; i < COUNT; i++) {
            if (values[i] <= 0) {
                continue;
            }
            if (!addedHeader) {
                lines.add(Component.empty());
                lines.add(Component.translatable("item.nutritionz.nutrients"));
                addedHeader = true;
            }
            lines.add(Component.translatable(
                    "item.nutritionz." + dev.jmiahman.hearthwind.survival.HearthwindSurvivalDiet.NUTRIENT_NAMES[i],
                    values[i]).withStyle(net.minecraft.ChatFormatting.GREEN));
        }
    }
}
